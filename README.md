# Beyond The Horizons

A work-in-progress add-on for [Ad Astra](https://github.com/terrarium-earth/Ad-Astra). This repository currently contains a minimal multi-loader project scaffold; no gameplay features have been implemented yet.

## Supported versions

| Component | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| Java | 21 |
| Ad Astra | 1.16.26 |
| NeoForge | 21.1.249 |
| Fabric Loader | 0.19.2 |
| Fabric API | 0.116.15 |
| Gradle | 9.4.0 |
| Cloche | 0.18.15 |

The loader and tooling versions follow the Ad Astra 1.21.1 branch.

## Project structure

- `src/main/java` — shared Java sources for both loaders.
- `src/fabric/java` — Fabric-specific entrypoint and future integrations.
- `src/neoforge/java` — NeoForge-specific entrypoint and future integrations.
- `build.gradle.kts` — multi-loader build using Cloche.
- `.github/workflows/build.yml` — CI builds and downloadable JAR artifacts.

## Build locally

Install Java 21. The project provides Gradle wrapper scripts; the first CI run will generate and commit the wrapper binary to the repository.

On Windows:

```powershell
.\gradlew.bat build
```

On Linux/macOS:

```bash
./gradlew build
```

Before the wrapper binary has been generated, use an installed Gradle 9.4.0 with `gradle build`.

Output files appear in `build/libs/`, with `-fabric.jar` and `-neoforge.jar` suffixes.

## GitHub Actions

Open **Actions → Build JARs → Run workflow** to build on demand. Builds also run on pushes to `main` and pull requests. Download the Fabric and NeoForge JARs from the run's **Artifacts** section. Nothing is automatically published to CurseForge or Modrinth.

## Dependencies

Ad Astra is a required runtime dependency on both platforms. Fabric also requires Fabric API. Ad Astra's own additional required dependencies must be installed according to its release metadata.

## License

No open-source license has been granted for this add-on. Ad Astra is a separate mod owned by its respective authors.
