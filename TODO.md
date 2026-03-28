# McDiscordBot — TODO

## V2

_(Well-defined features ready for development)_

- Change message format so that messages always arrive from "Rizzlebot" with a custom icon - make the name and icon configurable. The message content itself should represent the player taking the action (join/quit/die etc).
- Throttling Strategy: ensure that repeated joins and quits from the same player don't spam the server.
- Add some player stats into the posted message description, if available, e.g. join count, total play time, etc.
- Define colors in config file using standard RGB hex values, e.g. #FF0000

## Future

_(Ideas and exploration — not yet fully scoped)_

- **Custom Bukkit event support**: Allow config-driven listeners for arbitrary Bukkit events from other plugins (e.g., land claims, economy transactions). Design: map event class names to webhook message templates.
- **Automated testing strategy**: Determine approach for unit/integration tests (mock Bukkit API, webhook endpoint stubbing, etc.)
