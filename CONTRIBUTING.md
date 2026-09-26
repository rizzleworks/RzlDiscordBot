# Contributing

Thanks for taking an interest. This is a small plugin, so the process is light.

By participating you agree to the [Code of Conduct](CODE_OF_CONDUCT.md).

## Getting set up

You need JDK 21. The Gradle toolchain is pinned to 21, and CI builds on Corretto
21. Nothing else is required; the Gradle wrapper is checked in.

```bash
./gradlew test          # run the unit tests
./gradlew shadowJar     # build the plugin JAR into build/libs
```

The plugin targets Paper 1.21.11. `paper-api` is a `compileOnly` dependency, so
it is never bundled into the JAR; the server supplies it at runtime.

### Testing against a real server

To build and copy the JAR straight into a local Paper server, set `deployDir` in
`gradle.properties`, which is gitignored:

```properties
deployDir=../my-server/plugins
```

```bash
./gradlew deploy
```

The plugin writes `plugins/RzlDiscordBot/config.yml` on first start and logs a
warning until a webhook URL is set.

## Tests

JUnit 5 with Mockito and AssertJ. Please add tests with your change.

Coverage is reported on every pull request and currently has a floor of 80%
overall and 50% on changed files. The floors move up as coverage improves, so
new code that is hard to test may need a design change rather than an exemption.

Mockito is loaded as a `-javaagent` rather than self-attaching. If you run tests
from an IDE, set it to delegate to Gradle so the agent is applied.

## Pull requests

1. Branch off `main`. Use a short prefixed name, such as `fix/embed-color` or
   `feature/throttling`.
2. Open a pull request against `main`. Both CI jobs, `test` and `build`, must
   pass.
3. Pull requests are squash merged, so the pull request title becomes the commit
   subject. Write it as `type: imperative subject`, for example
   `fix: stop sending a join embed when the player has already quit`. Types in
   use are `feature`, `fix`, `docs`, `chore`, and `refactor`.
4. Apply one label. Release notes are generated from labels, so an unlabeled
   pull request lands in the wrong section.

| Label | Use for |
|---|---|
| `feature` | New behavior or configuration |
| `bug` | Fixes to existing behavior |
| `docs` | README, this file, other documentation |
| `chore` | Dependencies, CI, build, release plumbing |
| `refactor` | Internal change with no end-user effect |
| `unlisted` | Anything that should be excluded from release notes |

## Things that are easy to get wrong

`CLAUDE.md` holds the architecture notes and the Paper-specific constraints worth
reading before touching the listener: Adventure `Component` serialization, why
join messages are delayed a tick, and the fact that the `PLAY_ONE_MINUTE`
statistic counts ticks rather than minutes.

Webhook sends happen on a background thread. Only pre-extracted strings may
cross that boundary; calling the Bukkit API off the main thread is not safe.

## Reporting bugs and requesting features

Use the issue templates. For anything security related, do not open a public
issue; see [SECURITY.md](SECURITY.md).
