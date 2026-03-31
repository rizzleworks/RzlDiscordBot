# RzlDiscordBot

![CI](https://github.com/cliffmeyers/minecraft-discord-bot/actions/workflows/ci.yml/badge.svg)

A Paper plugin that posts Minecraft server events to a Discord channel via webhooks. Player joins, leaves, deaths, and advancements show up as rich embeds with player skin avatars.

## Features

- **Player join/leave** — color-coded embeds (green/red) with player skin avatar
- **Player death** — dark embed with the death cause message
- **Player advancement** — gold embed when a player earns an advancement (recipe unlocks are filtered out)
- All events are individually toggleable via config

## Requirements

- Paper 1.21.11 (or compatible)
- Java 21+
- A Discord webhook URL

## Building

```bash
./gradlew shadowJar
```

Produces `build/libs/RzlDiscordBot-1.0.0.jar`.

## Installation

1. Copy the JAR to your Paper server's `plugins/` directory.
2. Start the server. The plugin generates `plugins/RzlDiscordBot/config.yml` and logs a warning that no webhook URL is configured.
3. Set up a Discord webhook (see below) and paste the URL into `config.yml`.
4. Restart the server.

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

## Testing

```bash
./gradlew test
```

To bypass the cache and force tests to re-run:

```bash
./gradlew cleanTest test
```
