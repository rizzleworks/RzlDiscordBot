# RzlDiscordBot

![CI](https://github.com/cliffmeyers/minecraft-discord-bot/actions/workflows/ci.yml/badge.svg)
![Release](https://img.shields.io/github/v/release/cliffmeyers/minecraft-discord-bot)

A Paper plugin that posts Minecraft server events to a Discord channel via webhooks. Player joins, leaves, deaths, and advancements show up as rich embeds with player skin avatars.

## Features

- **Player join/leave** — color-coded embeds (green/red) with player skin avatar
- **Player death** — dark embed with the death cause message
- **Player advancement** — gold embed when a player earns an advancement (recipe unlocks are filtered out)
- All events are individually toggleable via config or in-game commands

## Requirements

- Paper 1.21.11 (or compatible)
- Java 21+
- A Discord webhook URL

## Installation

1. Download the latest `RzlDiscordBot-<version>.jar` from the [Releases page](https://github.com/cliffmeyers/minecraft-discord-bot/releases).
2. Copy the JAR into your Paper server's `plugins/` directory.
3. Start the server once. The plugin generates `plugins/RzlDiscordBot/config.yml` and logs a warning that no webhook URL is configured.
4. Create a Discord webhook (see below) and paste the URL into `config.yml`.
5. Restart the server.

## Discord Webhook Setup

1. Open your Discord server and go to the channel where you want events posted.
2. **Channel Settings > Integrations > Webhooks > New Webhook**.
3. Copy the webhook URL — it looks like `https://discord.com/api/webhooks/1234567890/abcDEF_ghiJKL...`
4. Paste it into `plugins/RzlDiscordBot/config.yml` as the `webhook-url` value.

The webhook URL is the only credential needed. Treat it like a secret — anyone with the URL can post to that channel.

## Configuration

```yaml
webhook-url: "https://discord.com/api/webhooks/..."
bot-name: "Minecraft Server"
bot-icon-url: ""  # Optional — falls back to Discord's default webhook avatar

events:
    player-join: true
    player-leave: true
    player-death: true
    player-advancement: true

colors:
    join: 5763719       # Green
    leave: 15548997     # Red
    death: 2303786      # Dark
    advancement: 15844367  # Gold
```

All messages appear from the configured bot identity. Each embed includes the player's Minecraft skin as a thumbnail.

## Commands

The `/rzldiscord` command lets operators manage event notifications in-game. Requires the `rzldiscordbot.admin` permission (defaults to op).

| Command | Description |
|---|---|
| `/rzldiscord status` | Show which events are enabled or disabled |
| `/rzldiscord toggle <event>` | Toggle a specific event on or off |
| `/rzldiscord reload` | Reload configuration from disk |

Event names for `toggle`: `join`, `leave`, `death`, `advancement`

Changes made with `toggle` are saved to `config.yml` immediately and take effect without a server restart.

## Build and Dev

### First-time setup

`gradle.properties` is gitignored. Create one at the repo root with:

```properties
org.gradle.jvmargs=--enable-native-access=ALL-UNNAMED
```

Optionally, to use `./gradlew deploy` to copy the built JAR to a local Paper server, also add:

```properties
deployDir=../path/to/your/server/plugins
```

### Building

```bash
./gradlew shadowJar
```

Produces `build/libs/RzlDiscordBot-<version>.jar` (version is derived from the latest `git tag`).

### Testing

```bash
./gradlew test
```

To bypass the cache and force tests to re-run:

```bash
./gradlew cleanTest test
```

### Releasing

Releases are cut by pushing a `v*` tag:

```bash
git tag v1.2.0
git push origin v1.2.0
```

The `release` workflow builds the JAR, runs tests, and publishes a GitHub Release with auto-generated notes.
