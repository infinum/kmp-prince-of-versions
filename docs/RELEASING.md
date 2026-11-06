# Releasing

## iOS (Swift Package Manager)

iOS releases are published by the [Release iOS](../.github/workflows/release_ios.yml) workflow.

1. In a pull request, bump `prince-of-versions` in `gradle/libs.versions.toml` (for example to `0.2.0`) and merge it.
2. Open **Actions → Release iOS → Run workflow** and run it on `main`.
3. The workflow:
   - builds the XCFramework and zips it as `PrinceOfVersions.xcframework.zip`
   - computes the checksum and writes the new URL and checksum into `Package.swift`
   - commits that change to a `release/v<version>` branch
   - publishes the GitHub release `v<version>` with the zip attached, which creates the `v<version>` tag on that commit
4. Open a pull request from `release/v<version>` into `main`. The run summary has the link. This keeps `Package.swift` on `main` pointing at the latest release.

After that, apps can depend on the version with an SPM version rule, for example "Up to Next Major Version".

The workflow stops if the tag already exists, so a version can't be released twice.

### Why it runs from a button instead of a tag push

SPM reads `Package.swift` from the tagged commit, so that commit must already contain the new zip's checksum. The checksum only exists after the XCFramework is built. If you pushed a tag first, its `Package.swift` would still point at the previous release. So the workflow builds first and creates the tag itself.

`main` is protected, so the workflow can't commit there. The tagged commit lives on the release branch, and step 4 brings the change into `main`.

## Android and JVM

This workflow only publishes the iOS release. The Maven artifact is published separately.
