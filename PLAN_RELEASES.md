# Release and Versioning Automation

## Context

The plugin version is currently hardcoded as `1.0.0` in two places (`build.gradle.kts:7` and `paper-plugin.yml:2`). There are no releases, tags, or release automation. The goal is to add automated GitHub Releases with attached JARs, triggered by git tags, with zero manual file edits per release.

## Approach: Git Tag-Triggered Releases

**Why this over alternatives:**
- **release-please / semantic-release**: Overkill for a single-contributor hobby project. Adds dependencies and conventional-commit enforcement for little benefit.
- **Every merge = release**: Too noisy. Not every PR is release-worthy (docs, tests, refactors).
- **Version in a file + "release PRs"**: Ceremony for ceremony's sake, and the two-file sync problem remains.

**Git tags win** because: zero files to edit per release, the two-file sync problem disappears (both derive from the tag), and it's the standard Gradle approach.

## Changes

### 1. Derive version from git tag — `build.gradle.kts`

Replace `version = "1.0.0"` with:

```kotlin
version = providers.exec {
    commandLine("git", "describe", "--tags", "--match", "v*", "--always")
}.standardOutput.asText.map { it.trim().removePrefix("v") }.getOrElse("0.0.0-dev")
```

Add resource token replacement so `paper-plugin.yml` gets the version injected at build time:

```kotlin
tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}
```

### 2. Use version token — `src/main/resources/paper-plugin.yml`

Change line 2 from `version: '1.0.0'` to `version: '${version}'`.

### 3. Create release workflow — `.github/workflows/release.yml`

New workflow triggered on `v*` tags:

```yaml
name: Release

on:
  push:
    tags: ['v*']

permissions:
  contents: write

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: actions/setup-java@v4
        with:
          distribution: corretto
          java-version: 21

      - uses: gradle/actions/setup-gradle@v4

      - run: ./gradlew test shadowJar --no-daemon

      - uses: softprops/action-gh-release@v2
        with:
          files: build/libs/RzlDiscordBot-*.jar
          generate_release_notes: true
```

`generate_release_notes: true` auto-generates release notes from merged PRs since the last tag.

### 4. Update CI workflow — `.github/workflows/ci.yml`

Add `fetch-depth: 0` to the checkout step so `git describe` works in CI builds (otherwise version falls back to `0.0.0-dev`, which is harmless but messy).

### 5. Update `CLAUDE.md` with release process

Add a **Release** section to `CLAUDE.md` documenting:
- How versioning works (derived from git tags via `git describe`)
- How to create a release (`git tag v1.x.x && git push origin v1.x.x`)
- That the GitHub Actions release workflow builds and publishes the JAR automatically
- That `paper-plugin.yml` version is injected at build time (no manual sync needed)

## Day-to-Day Workflow

1. Develop on a branch, open PR, merge to main. No release happens.
2. When ready to release, push a tag:
   ```
   git tag v1.1.0
   git push origin v1.1.0
   ```
3. Release workflow builds, runs tests, creates a GitHub Release with the JAR attached and auto-generated notes.

## After Merging

Tag the current commit to establish the baseline:
```
git tag v1.0.0
git push origin v1.0.0
```

## Verification

1. `./gradlew shadowJar` locally — JAR should be named `RzlDiscordBot-<version>.jar` (version from git describe)
2. Unzip the JAR, check `paper-plugin.yml` inside has the correct version (not `${version}`)
3. After merging and tagging `v1.0.0`, confirm the GitHub Release appears with the JAR attached
