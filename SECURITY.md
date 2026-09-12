# Security Policy

This policy applies to the RzlDiscordBot plugin itself. Vulnerabilities in
Paper or in Discord belong to those projects.

## Reporting a Vulnerability

Please do **not** open a public issue for security reports. Instead, use GitHub's
private vulnerability reporting:

1. Go to the [Security tab](https://github.com/rizzleworks/RzlDiscordBot/security) of this repository.
2. Click **Report a vulnerability**.
3. Fill out the form with reproduction steps and any relevant details.

You should receive an acknowledgment within a few days.

## Sensitive Data

The only credential this plugin handles is a Discord webhook URL, stored in
`plugins/RzlDiscordBot/config.yml`. Anyone with that URL can post arbitrary
messages to the associated channel.

When reporting an issue, **never include a live webhook URL** — redact it or
regenerate the webhook in Discord first. If you believe a webhook URL has been
leaked by this plugin's behavior, regenerate it in Discord (Channel Settings >
Integrations > Webhooks) before anything else.

## Supported Versions

Only the latest release receives security fixes.
