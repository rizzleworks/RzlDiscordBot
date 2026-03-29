# RzlDiscordBot — TODO

## Soon

_(Well-defined features ready for development)_

- **Testing**: need to test player death and achievement events, what do the embeds look like?
- **Session duration on leave**: Show how long the player was online in the quit message. Track `PLAY_ONE_MINUTE` ticks at join in a `Map<UUID, Integer>`, subtract on quit.
- **Readable Colors** Define colors in config file using standard RGB hex values, e.g. #FF0000
- **Config validation**: as we ship refinements to the plugin that introduce new config fields, it would be good for users to know if new configuration options need to be added to their file. If there is a standard strategy for addressing this, it would be great.
- **Custom Player Colors**: Discord messages use a custom color for each player

## Later

- **Database?**: what options do we have for persisting state beyond config files?
- **Mute via server command**: an admin player logged into the server can mute Discord messages via a slash command.
- **Mute via config change**: change a value in the config file that will mute any messages from the server. Ideally this doesn't require a server restart. Can the plugin periodically scan for config changes and pick them up? We want to be careful about how much of the config it would try to re-read as major config changes could put the plugin into a bad state.
- **Throttling Strategy**: ensure that repeated joins and quits from the same player don't spam the server.
- **CI Discord notifications**: GitHub Actions workflow that posts to the Discord webhook when a PR is merged (PR title, author, link). Use `curl` + the same webhook URL stored as a repo secret.

## Much Later

_(Ideas and exploration — not yet fully scoped)_

- **Custom player messages**: custom text when a player joins or leaves
- **Custom Bukkit event support**: Allow config-driven listeners for arbitrary Bukkit events from other plugins (e.g., land claims, economy transactions). Design: map event class names to webhook message templates.
- **Event listener tests**: Add MockBukkit to test `PlayerEventListener` with mock players and events
