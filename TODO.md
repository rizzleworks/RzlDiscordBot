# RzlDiscordBot — TODO

## Release Blockers

Assessed 2026-09-05, ahead of open sourcing under the `rizzleworks` GitHub org.
Everything in this section should be closed before the repo goes public.

Current state, verified on that date. This project is in good shape and is the
closer of the two to release ready:

- `./gradlew test shadowJar` builds clean, 39 tests pass, 56% instruction coverage
- CI runs on every push and PR and is green
- Release automation works end to end. `v0.0.1` (2026-04-04) and `v0.0.2` (2026-04-19) are published with JARs attached
- MIT `LICENSE` and a `SECURITY.md` are present
- Repo is `cliffmeyers/minecraft-discord-bot`, currently private, default branch `main`

### 1. Build fails outside a git checkout

The only item here that is actually broken rather than unpolished.

`build.gradle.kts:10` derives the version from `git describe --tags --match v* --always`.
The `.getOrElse("0.0.0-dev")` fallback does not work, because `providers.exec`
throws instead of producing a recoverable value. Verified:

| Scenario | Result |
|---|---|
| Source tree with no `.git` | Hard failure: `Process 'command 'git'' finished with non-zero exit value 128` |
| Git repo with no tags (shallow clone, fresh fork) | Version becomes a bare commit hash, e.g. `8e3d07f`, which is then expanded into `paper-plugin.yml` |

This matters because GitHub attaches "Source code (zip)" to every release.
Anyone who downloads the source from the releases page and tries to build it hits
a hard failure. CI masks the problem because it checks out with `fetch-depth: 0`.

To close: catch the exec failure properly, or shell out with a `|| echo 0.0.0-dev`
style fallback, and add a test or CI job that builds from an exported tree with no
`.git` directory.

### 2. CI breaks on pull requests from forks

`.github/workflows/ci.yml` requests `checks: write` and `pull-requests: write`,
which the two reporting steps need:

- `dorny/test-reporter@v1` requires `checks: write`
- `madrapps/jacoco-report@v1.7.1` requires `pull-requests: write`

Pull requests from forks receive a read-only `GITHUB_TOKEN`, so both steps fail
for any outside contributor. On a private repo with no external PRs this never
surfaced. It will on the first community contribution.

To close: mark those steps `continue-on-error: true`, or split reporting into a
separate `workflow_run` triggered job. Avoid `pull_request_target` unless the
security implications are handled deliberately.

### 3. Beta build plugin in shipped artifacts

`build.gradle.kts` pins `com.gradleup.shadow` to `9.0.0-beta12`. A beta plugin is
producing the JARs attached to public releases.

Dependabot PR #25 offers `9.4.2-SNAPSHOT`, which is worse. Close that PR and pin
a stable 9.x release instead.

### 4. Dependabot backlog

Ten open PRs, the oldest from 2026-04-19. Several are major version bumps that
need review rather than a blind merge:

| PR | Bump |
|---|---|
| #29 | `actions/setup-java` 5 to 5.6.0 |
| #28 | `actions/checkout` 6 to 7 |
| #27 | `madrapps/jacoco-report` 1.7.2 to 1.8.0 |
| #25 | `com.gradleup.shadow` 9.0.0-beta12 to 9.4.2-SNAPSHOT (close, see item 3) |
| #24 | `junit-jupiter` 5.11.4 to 6.0.3 (major) |
| #23 | `mockito-core` 5.15.2 to 5.23.0 |
| #20 | `mockito-junit-jupiter` 5.15.2 to 5.23.0 |
| #18 | `assertj-core` 3.27.3 to 3.27.7 |
| #17 | `gradle/actions` 4 to 6 (major) |
| #16 | `softprops/action-gh-release` 2 to 3 (major) |

Clear this before going public so the repo does not open with a stale dependency
queue. Note that #16 changes the release workflow, so re-verify a release after
merging it.

### 5. Coverage gaps on user-facing code

Overall 56% instruction coverage. Per class:

| Class | Coverage |
|---|---|
| `WebhookPayload` | 100% (103/103) |
| `Embed` | 100% (85/85) |
| `JsonUtil` | 100% (21/21) |
| `DiscordWebhookSender` | 99% (127/128) |
| `NotificationEvent` | 93% (86/92) |
| `PlayerEventListener` | 54% (187/341) |
| `RzlDiscordBotPlugin` | **0% (0/96)** |
| `RzlDiscordCommand` | **0% (0/207)** |

`RzlDiscordCommand` is the largest untested class and is exactly what server
operators interact with. `RzlDiscordBotPlugin` covers config loading and startup,
including the missing-webhook warning path documented in the README.

The MockBukkit idea already recorded under "Much Later" below is the natural way
to close the `PlayerEventListener` gap.

### 6. The coverage gate is decorative

`.github/workflows/ci.yml` sets `min-coverage-overall: 0` and
`min-coverage-changed-files: 0`, so the JaCoCo report can never fail a build.
Set a real floor once item 5 has moved the number up.

### 7. Two shipped features have never been visually verified

Already tracked under "Soon" below, repeated here because it gates release:
player death and player advancement embeds are documented in the README as
working features, but the embeds have not been confirmed in a live Discord
channel. Verify both before strangers do.

### 8. Hardcoded repo URLs will break on transfer

All of these point at `cliffmeyers/minecraft-discord-bot`:

- `README.md`: CI badge
- `README.md`: Release badge
- `README.md`: Releases page link in the Installation section
- `SECURITY.md`: Security tab link in the vulnerability reporting steps

Sweep them as part of the move. Also update `SECURITY.md`, which currently opens
with "Just in case someone else downloads this", a line written for a private
repo.

### 9. Missing community health files

For a public repo: `CONTRIBUTING.md`, issue templates, a pull request template,
and `CODE_OF_CONDUCT.md`. No `CHANGELOG.md`, though `.github/release.yml` already
maps PR labels to categories in auto-generated release notes, which covers most of
the need.

Confirm the Dependabot `chore` label and the labels referenced in
`.github/release.yml` (`feature`, `bug`, `chore`, `refactor`, `documentation`,
`skip-changelog`) all exist in the destination repo, since labels do not survive
a transfer cleanly.

### 10. Transfer and rename mechanics

Target: `rizzleworks/RzlDiscordBot`. The directory name and the repo name differ
today (`minecraft-discord-bot`), and the plan is for them to match.

- Transfer preserves history, issues, and PRs, and sets up redirects from the old path
- GitHub Actions secrets do **not** transfer. Re-add any that exist
- Local remotes need updating: `git remote set-url origin git@github.com:rizzleworks/RzlDiscordBot.git`
- Open Dependabot branches carry over. Clearing item 4 first makes the move cleaner
- One commit is unmerged: `fb8df7c move dev stuff to the end of README.md` on `chore/more-tweaks`. Merge or drop it before the transfer

### 11. Org and plan notes

- A free GitHub org supports unlimited private repos. Transfer while private, close the blockers, cut a release under the new name, then flip to public. Moving to the org and open sourcing are separate decisions
- Public repos on the free org tier get unlimited Actions minutes, rulesets, code scanning, secret scanning, Dependabot, and private vulnerability reporting. GitHub Team adds essentially nothing for a public open-source repo
- GitHub Pro on a personal account does not apply to org-owned repos
- `SECURITY.md` directs reporters to private vulnerability reporting, which must be enabled explicitly in repo settings under Security

### Suggested order

1. Fix the git describe fallback (item 1) and add a no-`.git` build check
2. Fix the fork PR permissions in CI (item 2)
3. Pin shadow to a stable release and close PR #25 (item 3)
4. Work through the rest of the Dependabot backlog (item 4)
5. Merge or drop `chore/more-tweaks`
6. Verify the death and advancement embeds in a live channel (item 7)
7. Transfer to `rizzleworks` and rename to `RzlDiscordBot`, still private
8. Sweep the hardcoded URLs and rewrite the SECURITY.md opener (item 8)
9. Add the community health files and recreate the labels (item 9)
10. Enable private vulnerability reporting
11. Cut `v0.1.0` and verify the release artifact under the new name
12. Add tests for `RzlDiscordCommand` and `RzlDiscordBotPlugin`, then set a real coverage floor (items 5 and 6)
13. Flip to public

---

## Soon

_(Well-defined features ready for development)_

- **Testing**: need to test player death and achievement events, what do the embeds look like?
- **Readable Colors** Define colors in config file using standard RGB hex values, e.g. #FF0000
- **Config validation**: as we ship refinements to the plugin that introduce new config fields, it would be good for users to know if new configuration options need to be added to their file. If there is a standard strategy for addressing this, it would be great.
- **Custom Player Colors**: Discord messages use a custom color for each player

## Later

- **Database?**: what options do we have for persisting state beyond config files?
- **Throttling Strategy**: ensure that repeated joins and quits from the same player don't spam the server.
- **CI Discord notifications**: GitHub Actions workflow that posts to the Discord webhook when a PR is merged (PR title, author, link). Use `curl` + the same webhook URL stored as a repo secret.

## Much Later

_(Ideas and exploration — not yet fully scoped)_

- **Custom player messages**: custom text when a player joins or leaves
- **Custom Bukkit event support**: Allow config-driven listeners for arbitrary Bukkit events from other plugins (e.g., land claims, economy transactions). Design: map event class names to webhook message templates.
- **Event listener tests**: Add MockBukkit to test `PlayerEventListener` with mock players and events
