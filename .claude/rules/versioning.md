# Versioning

- The app version lives in `version.properties` (root) and is read by `app/build.gradle`; CI overrides it via `-PappVersionName`/`-PappVersionCode`. Never bump it by hand — the `Release` workflow does that.
- Version bumps in the `Release` workflow: `minor` is the default for regular releases, `patch` is for hotfixes only, `major` is reserved for breaking/disastrous changes.
