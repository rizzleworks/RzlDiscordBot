# RzlDiscordBot

Paper 1.21.11 plugin that posts player events to Discord via webhooks.

## Build & Deploy

```bash
./gradlew shadowJar                # build only
./gradlew deploy                   # build + copy to local server
```

Local deploy requires `deployDir` in `gradle.properties` (gitignored):
```properties
deployDir=../my-server/plugins
```

Config is generated at `plugins/RzlDiscordBot/config.yml` on first server start. The webhook URL must be set there.

## Architecture

Single Paper plugin, no external services or runtime dependencies beyond the JDK and Paper API.

- **Discord integration**: Webhook HTTP POST (no bot token needed). The webhook URL is the only credential.
- **Async HTTP**: `java.net.http.HttpClient.sendAsync()` — never block the main server thread.
- **JSON**: Built manually with string formatting. No Gson/Jackson.
- **Player skin avatars**: `https://mc-heads.net/avatar/{uuid}/64` — passed as `avatar_url` and embed thumbnail.
- **Event toggles and embed colors**: Configurable in `config.yml` or via `/rzldiscord` in-game commands (op-only).
- **NotificationEvent enum**: Centralizes event short names, config keys, and default colors. Used by both the command handler and event listener.

## Key Technical Constraints

- **Adventure Components**: Paper 1.21 uses Kyori Adventure. Death messages and advancement titles are `Component` objects — serialize with `PlainTextComponentSerializer.plainText().serialize(component)`.
- **Thread safety**: Only use pre-extracted strings (no Bukkit API calls) inside async webhook sends.
- **Advancement filtering**: `getAdvancement().getDisplay() == null` filters out recipe unlocks and hidden advancements.
- **Rate limits**: Discord webhooks allow 30 requests/60 seconds per URL.
- **Delayed join messages**: `onPlayerJoin` uses `runTaskLater(plugin, task, 20L)` (1-second delay) because `getFirstPlayed()` and `getStatistic(PLAY_ONE_MINUTE)` can return 0 if read immediately during `PlayerJoinEvent`. Always check `player.isOnline()` before sending — the player may disconnect during the delay.
- **PLAY_ONE_MINUTE**: Despite the name, this Bukkit statistic counts **ticks**, not minutes. Divide by 1200 for minutes (20 ticks/sec × 60 sec).

## Release

Versioning is derived from git tags via `git describe`. No manual file edits needed — both `build.gradle.kts` and `paper-plugin.yml` get the version automatically.

To create a release:
```bash
git tag v1.1.0
git push origin v1.1.0
```

The `.github/workflows/release.yml` workflow builds the JAR, runs tests, and creates a GitHub Release with the JAR attached and auto-generated release notes.

## Testing

```bash
./gradlew test
```

JUnit 5 + Mockito. Tests cover the `discord` package: `JsonUtil`, `Embed`, `WebhookPayload` (pure unit tests), `DiscordWebhookSender` (mocked `HttpClient`), and `PlayerEventListenerDeathTest` (mocked `Plugin` and `FileConfiguration`).
