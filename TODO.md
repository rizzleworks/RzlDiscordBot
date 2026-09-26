# RzlDiscordBot — TODO

### Coverage notes

The 25 uncovered instructions in `RzlDiscordBotPlugin` are the command lifecycle
registration in `onEnable`. `LifecycleEvents.COMMANDS` initializes through a
`ServiceLoader` lookup that throws outside a running server, and the provider
interface is package-private, so there is no clean way to fake it. Those lines
have no branches; leave them.

### Reference: org and plan notes

- A free GitHub org supports unlimited private repos, with a limited feature set:
  no branch protection, no code scanning, no secret scanning on private repos
- Public repos on the free org tier get unlimited Actions minutes, rulesets, code
  scanning, secret scanning, Dependabot, and private vulnerability reporting
- GitHub Team is $4/user/month and is the cheapest tier that restores branch
  protection on a private repo
- GitHub Pro on a personal account does not apply to org-owned repos

### Closed since the 2026-09-05 assessment

- Build no longer fails outside a git checkout, with a CI job that proves it
- Fork PR permissions fixed by splitting reporting into `report.yml`
- `com.gradleup.shadow` pinned to a stable `9.6.1`
- Dependabot backlog cleared; `io.papermc.paper:*` now ignored, since its version
  is the target Minecraft version rather than a library version
- Death and advancement embeds verified in a live Discord channel
- Transferred to `rizzleworks/RzlDiscordBot`, local remote updated
- PR labels standardized and mapped to release note categories
- Superseded PR CI runs now cancel; release runs never do
- Mockito agent loaded with `-javaagent` instead of self-attaching
- Repo URLs in `README.md`, `SECURITY.md`, and `paper-plugin.yml` repointed to
  `rizzleworks/RzlDiscordBot`, and the `SECURITY.md` opener rewritten for a
  public repo
- Community health files added: `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, a pull
  request template, and bug and feature issue forms with blank issues disabled

---

## Soon

_(Well-defined features ready for development)_

- **Readable Colors** Define colors in config file using standard RGB hex values, e.g. #FF0000
- **Config validation**: as we ship refinements to the plugin that introduce new config fields, it would be good for users to know if new configuration options need to be added to their file. If there is a standard strategy for addressing this, it would be great.
- **Custom Player Colors**: Discord messages use a custom color for each player

## Later

- **Database?**: what options do we have for persisting state beyond config files?
- **Throttling Strategy**: ensure that repeated joins and quits from the same player don't spam the server.
- **CI Discord notifications**: GitHub Actions workflow that posts to the Discord webhook when a PR is merged (PR title, author, link). Use `curl` + the same webhook URL stored as a repo secret.
- **Event listener tests**: `PlayerEventListener` sits at 54% and blocks raising `min-coverage-changed-files` above 50%. Deferred past `v0.1.0`.
  - Planned route is MockBukkit (`com.github.seeseemelk:MockBukkit-v1.21:3.133.2` on Maven Central) with mock players and events. Check first whether it supports Paper's Brigadier lifecycle command registration, which `RzlDiscordCommand` uses.
  - Testcontainers was rejected: it records no JaCoCo coverage, since the plugin runs in another JVM. Its plan (`PLAN_TESTCONTAINERS.md` on the local-only branch `test/test-containers`) pins Testcontainers `2.0.4`, which does not exist (latest is `1.21.3`), and sets the server to `1.21.1` instead of `1.21.11`. If it is ever built, it belongs in `release.yml`, not CI.

## Much Later

_(Ideas and exploration — not yet fully scoped)_

- **Custom player messages**: custom text when a player joins or leaves
- **Custom Bukkit event support**: Allow config-driven listeners for arbitrary Bukkit events from other plugins (e.g., land claims, economy transactions). Design: map event class names to webhook message templates.
