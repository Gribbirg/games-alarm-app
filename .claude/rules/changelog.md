# Changelog

- Every PR MUST add a changelog fragment: one new file `changelogs/unreleased/<short-slug>.md` with bullet(s) in Russian describing what changed **from the user's point of view** — no code details, class names, or refactoring notes.
- If the PR is purely technical, still add a file with a line like `- Технические улучшения стабильности и сборки.`
- See `changelogs/README.md` for the format. On release, the fragments are assembled into `CHANGELOG.md` and the GitHub Release body, then deleted — never edit `CHANGELOG.md` directly in a PR.
