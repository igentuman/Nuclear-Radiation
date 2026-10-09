# Repository Guidelines

## Project Structure & Module Organization

Nuclear Radiation is a Minecraft 1.21.1 NeoForge mod (`mod_id: nuclear_radiation`, group `igentuman.nr`) that models physics-driven radiation using real units (Bq, Gy, Sv). All Java sources live under `src/main/java/igentuman/nr/` with these key packages:

- **`api/`** - public interfaces (`RadiationProfile`, `IsotopeStack`, `Units`, `DecayGraph`, `NREvents`); integration bridge that keeps optional-mod types out of core
- **`radiation/`** - simulation engine: `simulation/` (subchunk vector field, worker thread, source spatial index), `shielding/` (voxel-DDA raycast, armor/block registries), `source/`, `storage/`, `irradiation/`
- **`integration/`** - optional mod hooks: `jei`, `kubejs`, `mekanism`, `nuclear_science`, `nuclearcraft`, `createnucleartech`, `emi`
- **`mixin/`** - core + client + Mekanism/Voltaic radiation mixins
- **`datagen/`** - data generators; output to `src/generated/resources/` (bindings, tags, armor protections, recipes)
- **`registry/`** - `IsotopeRegistry`, `DefaultIsotopes`, `IsotopesReloadListener`
- **`recipe/`** - `MutationRecipe`, `BlockIrradiationRecipe`, `NRRecipes`
- **`network/`** - payloads and client caches

Data-driven content (isotopes, bindings, shielding, armor, recipes) is authored as JSON under `data/<ns>/nuclear_radiation/` and loaded via reload listeners. Load order per `/reload`: built-in defaults -> datapack JSON -> KubeJS additions -> KubeJS removals.

## Build, Test, and Development Commands

```bash
./gradlew build        # full build, output in build/libs/
./gradlew runClient    # client dev environment
./gradlew runServer    # dedicated server dev environment
./gradlew runData      # run data generators (writes src/generated/resources/)
./gradlew test         # JUnit 5 unit tests
```

Run a single test class: `./gradlew test --tests "igentuman.nr.core.UnitsTest"`

## Coding Style & Naming Conventions

- **Java 21** toolchain; UTF-8 source encoding enforced in `build.gradle`
- No linter or formatter configs present; follow existing package conventions
- NeoForge moddev plugin with Parchment mappings (`2024.11.17` for MC `1.21.1`)
- Gradle config: daemon disabled, parallel + caching + configuration-cache enabled (see `gradle.properties`)

## Testing Guidelines

JUnit 5 (`org.junit.jupiter`), tests in `src/test/java/igentuman/nr/`. Tests are pure-Java unit tests (no Minecraft runtime required) covering core utilities like `Units` and builders.

## Commit & Pull Request Guidelines

Commit messages are sentence-case summaries with no conventional-commit prefix (e.g., "Fix radiation dose increase for no reason", "Entity iteration crash fix"). Multi-change commits use comma-separated descriptions. CI runs `./gradlew build` on JDK 21 for all pushes and pull requests via `.github/workflows/build.yml`.
