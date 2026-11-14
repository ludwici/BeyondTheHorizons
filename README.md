# Ad Astra: Beyond The Horizons

Ad Astra: Beyond The Horizons is a work-in-progress add-on for [Ad Astra](https://github.com/terrarium-earth/Ad-Astra). This repository currently contains a minimal multi-loader project scaffold; no gameplay features have been implemented yet.

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
- `.github/workflows/build.yml` — NeoForge-only CI and downloadable JAR artifact.

## Build locally

Install Java 21. The project provides Gradle wrapper scripts; the wrapper binary is included in the repository.

On Windows:

```powershell
.\gradlew.bat build
```

On Linux/macOS:

```bash
./gradlew build
```

To build NeoForge only locally, run `./gradlew neoforgeIncludeJar` (Windows: `.\gradlew.bat neoforgeIncludeJar`).

The distributable NeoForge JAR is `build/libs/BeyondTheHorizons-neoforge-1.21.1-0.1.0.jar`. The mod display name is `Ad Astra: Beyond The Horizons`, while the JAR filename uses only `BeyondTheHorizons`.

## GitHub Actions

Open **Actions → Build NeoForge JAR → Run workflow** to build on demand. Builds run on pushes to `main` and pull requests. GitHub Actions builds **NeoForge only**; Fabric sources are kept for future work. Download the NeoForge JAR from **Artifacts** (retention **1 day**). Nothing is automatically published to CurseForge or Modrinth.

## Dependencies

Ad Astra is a required runtime dependency on both platforms. Fabric also requires Fabric API. Ad Astra's own additional required dependencies must be installed according to its release metadata.

## License

No open-source license has been granted for this add-on. Ad Astra is a separate mod owned by its respective authors.
