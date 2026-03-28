# McDiscordBot — TODO

## V2

_(Well-defined features ready for development)_

- Config validation: as we ship refinements to the plugin that introduce new config fields, it would be good for users to know if new configuration options need to be added to their file. If there is a standard strategy for addressing this, it would be great.
- Mute via config: change a value in the config file that will mute any messages from the server. Ideally this doesn't require a server restart. Can the plugin periodically scan for config changes and pick them up? We want to be careful about how much of the config it would try to re-read as major config changes could put the plugin into a bad state.
- Throttling Strategy: ensure that repeated joins and quits from the same player don't spam the server.
- Add some player stats into the posted message description, if available, e.g. join count, total play time, etc.
- Define colors in config file using standard RGB hex values, e.g. #FF0000

## Future

_(Ideas and exploration — not yet fully scoped)_

- **Custom Bukkit event support**: Allow config-driven listeners for arbitrary Bukkit events from other plugins (e.g., land claims, economy transactions). Design: map event class names to webhook message templates.
- **Event listener tests**: Add MockBukkit to test `PlayerEventListener` with mock players and events
