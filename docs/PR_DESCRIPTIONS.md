# Dependabot PR descriptions: possible rework

Notes from 2026-09-26. Not scheduled; revisit if the current workflow causes
friction again.

## Current behavior

`.github/workflows/tidy-dependabot-pr.yml` acts only when the PR body still
contains Dependabot's original HTML (`<details>`). It then:

1. Moves the release notes into a PR comment, updating an earlier one if present.
2. Replaces the body with Dependabot's compare-view link, swapping SHA ranges for
   tags when both tags resolve to those exact commits. No link gives an empty
   body.

The body matters because squash merges use it as the commit body.

## The problem

The workflow transforms the body once and skips anything it has already
trimmed. When the output format changed, #41 kept its old-format body
(`Bumps ...` plus a SHA link) because the new version saw no `<details>` and did
nothing. The fix was `@dependabot recreate`, which restores the original body
and re-triggers the workflow.

## Proposed rework

Compute the body the PR should have on every run, and write it if it differs:

1. Fetch the current body, and the message of the PR's **first** commit (the
   head may be a merge from "Update branch").
2. Build the new body from the commit message's `- [Commits](...)` link, with the
   tag swap applied. Use it only if there is exactly one such line; otherwise
   the body is empty.
3. If the current body still has `<details>`, post or update the release notes
   comment, as today.
4. If the current body differs from the new body, update it.

The commit message is the source because the link is gone from the body once it
is trimmed. On #41, #32, and #23 the `[Commits]` link matched the body's
compare-view link exactly. #36 (Paper) has no link.

## Trade-offs

- About the same amount of code. Link extraction gets simpler: one Markdown
  line instead of an HTML regex that has to avoid the narrower compare links in
  quoted release notes.
- Each run's result no longer depends on earlier runs, so a format change
  applies to open PRs on their next push.
- One more API call, to list the PR's commits.
- Two conditions instead of one: `<details>` gates the comment, "differs" gates
  the body.
- Hand edits to a Dependabot PR body get reverted on the next push.
- A tag lookup that fails after an earlier success flips the body back to the
  SHA link until the next successful run.
- The grouped-update guard assumes Dependabot's grouped commit format has one
  `[Commits]` line per dependency. This repo has never had a grouped PR to
  test against.

## Testing, if built

Run the build step locally against all past Dependabot PRs twice: once from the
original body, and once from the first run's output, to confirm a second run is
a no-op.
