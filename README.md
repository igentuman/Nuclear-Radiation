# Nuclear Radiation

Physics-driven radiation simulation mod for Minecraft 1.21.1 (NeoForge).

Models radioactive isotopes, decay, contamination, dose, shielding, and medicine using real units (Bq, Gy, Sv) rather than ad-hoc "rads".

## Documentation

- **[Modpacker Guide](docs/Modpackers.md)** - no-code setup (datapack JSON, KubeJS, config). FAQ: make blocks/items radioactive, biome/dimension background, block & armor shielding, isotopes, mutations, block irradiation.
- **[Mod Developer Guide](docs/ModDevelopers.md)** - Java API: radiation sources, profiles, bindings, shielding, isotopes, dose sampling, and the `NREvents` integration bridge.
- **[KubeJS Guide](docs/KubeJS.md)** - full scripting reference.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.230+
- Java 21

## Features

### Units & Physics
- **Bq** - radioactive source activity (decays/sec)
- **Gy** - absorbed dose, used for shielding attenuation
- **Sv** - biological dose to entities (rate + career total)
- Per-type quality factors: alpha (Q=20), beta (Q=1), gamma/x-ray (Q=1), neutron (Q≈10)

### Radiation Sources
- Block radiation sources (placed radioactive blocks) - `BlockRadSource`
- Item entity sources (dropped radioactive stacks, with radioactive glow) - `ItemEntityRadSource`
- Fluid sources (radioactive fluids in world) - `FluidRadSource`
- Container sources (chests/barrels with radioactive contents - point source, no contamination spread) - `ContainerRadSource`
- Creative radiation source block - fully tunable α/β/x-ray/neutron emitter in MBq, GUI-configurable (`CreativeRadSourceBlock`, `CreativeRadSource`)
- Residual/leftover contamination source - `LeftOverRadSource`
- Per-chunk soil/air/water contamination profiles (`ChunkRadiationData`)

### Decay
- Absolute-expiry model - sources carry `expiryGameTime`, no per-tick decrement
- Lazy decay on sampled access (`atoms *= exp(-λ·Δt)`)
- Optional branching decay chains via `DecayEdge` probabilities
- Long-lived isotopes (effective half-life ≥ `static_half_life_years`) treated as static - Bq computed, no daughter ingrowth
- 40 built-in isotopes: U-233/234/235/238, Pu-238/239/241/242, Np-236/237, Am-241/242/243, Cm-243/245/246/247, Bk-247/248, Cf-249/250/251/252, Th-230/232, Ac-225, plus Cs-137, I-131, Sr-90, Y-90, Co-60, H-3, Po-210, Xe-133, Kr-85, Na-22, Ca-48, Be-7, Ir-192, Cn-291
- Isotopes are data-driven: defaults in `DefaultIsotopes`, overridable via datapack JSON (`data/<ns>/nuclear_radiation/isotopes/*.json`, `IsotopesReloadListener`) or KubeJS

### Exposure Pipeline
- External dose - sampled from per-subchunk `SubChunkRadVector` (6-direction cone field, not scalar)
- Inventory dose - per-slot contact factor (held/offhand/armor/main inv), alpha/beta pass-through configurable
- Internal dose - ingested/inhaled isotopes per entity
- Background dose - per-dimension and per-biome uSv/h overrides
- Stagger buckets prevent tick spikes (`entityId % interval`)

### Shielding
- Block attenuation via binding system: blocks bind to one of 3 tiers (`light`/`mid`/`heavy`, each a `(xray, neutron)` preset) through tags `nr:shielding/{light,mid,heavy}`, datapack JSON (`nuclear_radiation/shielding/*.json`, supports tier ref or raw override), or the `IShieldingBlock` interface. Resolved by `ShieldingBindings` (direct block binding wins over tag).
- Single-pass dual-attenuation voxel-DDA raycast (x-ray + neutron in one walk)
- Cached per `(sourceChunk, targetChunk)`, invalidated on block change
- Armor attenuation via `ArmorProtectionRegistry` - datapack-driven (`ArmorProtectionReloadListener`); defaults cover vanilla iron/gold/netherite sets with per-type (xray/alpha/beta/neutron) coefficients
- Mod-added **Hazmat Suit** (helmet/chestplate/leggings/boots, `NRArmorItems`) - full-body set for working in hot zones

### Medicine
- Iodine pill - blocks I-131 uptake (`iodine_protection` effect)
- Prussian blue - accelerates Cs-137 purge (`cesium_purge` effect)
- Anti-rad injection - strong purge boost
- Radaway - gradual purge
- Rad-protection potion item - generic `radiation_protection` effect
- MobEffects: `radiation_protection`, `radiation_purge`, `iodine_protection`, `cesium_purge`

### Tools
- **Geiger counter** - reads in-world Bq, audible clicks scale with activity
- **Dosimeter** - reads Sv total + Sv/h + breakdown (external/inventory/internal), HUD overlay

### Visual Feedback
- Radioactive **glow silhouette** post-shader on hot item entities (`GlowSilhouette`)
- **White-noise screen overlay** in intense fields - static scales with dose rate (`RadiationScreenLayer`)
- Debug renderers for subchunk vectors, contamination, and shielding rays (client caches + network payloads)

### Mutations & Block Irradiation
- **Mob mutations** - prolonged dose transforms mobs into other entities. Recipe-driven (`nuclear_radiation:mutation`), one-shot per entity per recipe, gated on total Sv + Sv/h window with optional chance (`MutationProcessor`, `MutationRecipe`)
- **Block irradiation** - sources transmute nearby blocks once local attenuated activity clears `min_bq`. Recipe-driven (`nuclear_radiation:block_irradiation`) with weighted outputs + chance (`BlockIrradiationRecipe`)
- Both recipe types registered in `NRRecipes`; authorable via datapack JSON or KubeJS

### Mod Integrations
- **JEI** (`ModJeiPlugin`) - custom info categories: Isotope Stats, Radioactive Items, Decay Graph, Armor Protection, Block Shielding, Mutation, Block Irradiation
- **KubeJS** - scriptable isotopes, bindings, shielding, armor, recipes, and dose events (see [KubeJS integration guide](docs/KubeJS.md))
- **Mekanism** - Mekanism radiation events mapped to an isotope cocktail (`MekRadiationManagerMixin`, `MekanismHelper`)
- **Nuclear Science / Voltaic** - Voltaic radiation sources mapped to isotope profiles (`NuclearScienceRadiationManagerMixin`, `NuclearScienceHelper`)
- Integration bridge `api/NREvents` keeps optional-mod types out of core - integrations register plain-Java callbacks; safe when a mod is absent

### Data-Driven Bindings
Three input paths for assigning `RadiationProfile` to items/blocks/fluids:
1. Datapack JSON (`data/<ns>/nuclear_radiation/bindings/*.json`) - handled by `RadiationBindingsReloadListener`
2. Tags (`nr:radioactive/low|medium|high`, `nr:radioactive/uranium_ore|uranium_raw|uranium_ingot|uranium_dust|spent_fuel`, fluid tag `nr:radioactive`)
3. Item `DataComponent` (`RadiationComponent`) - per-stack runtime profile

## Architecture Highlights

- **Threading**: main thread snapshots sources and enqueues `Job`s into a `LinkedBlockingQueue`; a single daemon background thread (`nuclear-radiation-sim`) blocks on that queue, runs subchunk vector accumulation and falloff math (`compute()`), then posts results back via a `ConcurrentLinkedQueue<Runnable>`; results are applied to `vectorByDim` on the main thread inside `drainMainThreadTasks()` each tick.
- **Persistence**: per-dimension `SavedData` (`RadiationLevelData`) + `AttachmentType`s on chunks/entities (`NRAttachments`: `chunk_radiation`, `entity_radiation`). No deprecated capabilities.
- **Performance**: subchunk vector cache reduces N entities x M sources from O(N·M) to O(N) per tick. Hard source-radius cutoff `MAX_SOURCE_RADIUS_M`.
- **Tick gating**: independent `world_sim_interval_ticks` and `entity_sim_interval_ticks`; doses scale by interval.

Source layout (`src/main/java/igentuman/nr/`):
```
api/               public interfaces, RadiationProfile, IsotopeStack, Units, DecayGraph, NREvents
armor/             NRArmorItems (hazmat set)
binding/           datapack loader, tag presets, DataComponent, tooltip
block/             CreativeRadSourceBlock + block entity + config screen
builder/           IsotopeBuilder, RadiationBindingBuilder, RadiationProfileBuilder
client/            GlowSilhouette shader, RadiationScreenLayer (white-noise overlay)
command/           NRCommands (/nr)
config/            GeneralConfig, RadiationConfig (TOML)
containers/        ContainerAttenuationRegistry, OpenContainerRegistry, ContainerRadiationTicker
datagen/           data generators (bindings, tags, armor protections, recipes)
entity/            EntityDoseProcessor, MutationProcessor, exposure events, ignore filter
integration/       jei, kubejs, mekanism, nuclear_science
inventory/         slot factor providers, inventory radiation cache
medicine/          items, MobEffects (radiation_protection, radiation_purge, iodine_protection, cesium_purge)
mixin/             core + Mekanism/Voltaic radiation mixins
network/           payloads, client caches (radiation, vector, shielding rays, contamination, creative source)
recipe/            MutationRecipe, BlockIrradiationRecipe, EntityIngredient/Result, NRRecipes
registry/          IsotopeRegistry, Isotopes, DefaultIsotopes, IsotopesReloadListener
shielding/         ShieldingRaycast, ShieldingRegistry, ArmorProtectionRegistry
simulation/        RadiationSimulator, SubChunkRadVector, SourceSpatialIndex, snapshot, worker
tools/             geiger, dosimeter, client HUD layers, debug renderer
util/              TextUtils, WorldUtil; util/tracking (world sources), util/persistence (SavedData, NRAttachments, codecs)
```

## Configuration

Common config: `config/nuclear_radiation-common.toml`
Radiation tuning: `config/nuclear_radiation-radiation.toml`

Top-level sections in the radiation config:
- `[radiation]` - `world_sim_interval_ticks`, `entity_sim_interval_ticks`, `max_source_radius_m`, `chunk_vector_ttl_ticks`, `stagger_entities`
- `[entities]` - `ignore_creative`, `ignore_spectator`, `ignored` (entity id list)
- `[thresholds_sv_per_hour]` - `mild`, `moderate`, `severe`, `lethal`
- `[recovery]` - `base_decay_per_hour_sv`
- `[conversion]` - `gy_per_bq_second`
- `[inventory]` - `armor_blocks_inventory`, `inventory_alpha_pass`, `inventory_beta_pass`
- `[background]` - `default_usv_per_hour`, `level_usv_per_hour`, `biome_usv_per_hour`
- `[debug]` - `debug_radiation_vectors`
- `[world_sources]` - `activity_floor_bq`, `world_source_min_bq`, `contamination_spread_factor`, `static_half_life_years`

## Build

```bash
./gradlew build
```

Output: `build/libs/nuclear_radiation-1.0.0.jar`

## Development

```bash
./gradlew runClient    # client dev environment
./gradlew runServer    # server dev environment
./gradlew runData      # data generators
./gradlew test         # unit tests
```

## Scripting (KubeJS)

When KubeJS is installed the plugin loads automatically (no setup). Scripts can define isotopes, radioactivity bindings, shielding, and armor protection (startup), plus mutation / block-irradiation recipes and react to dose-phase events (server). Load order per `/reload`: **built-in defaults → datapack JSON → KubeJS additions → KubeJS removals**, so scripts always win.

See the full guide: **[docs/KubeJS.md](docs/KubeJS.md)**.

## Commands

- `/nr clear <player>` - reset a player's accumulated dose (career Sv, Sv/h, protection, internal contamination). Requires permission level 2.

## How It Works

### Subchunk Radiation Vectors

A **subchunk** is a 16x16x16 block cube.

Every subchunk that contains a living entity gets one `SubChunkRadVector`. Each vector stores radiation pointing **outward in 6 directions** (+X, -X, +Y, -Y, +Z, -Z) - 6 **square pyramids** sharing an apex at the cube's center, with ribs passing through the corners of the corresponding subchunk face. The 6 pyramids tile R³ disjointly; every point outside the apex falls into exactly one. (Sometimes called "cones" loosely - the geometry is square pyramids.)

**Build steps each sim tick** (`RadiationSimulator.compute`):

1. Find all subchunks with living entities (`collectOccupiedSubChunks`).
2. For each one, the cube's center point is the "apex".
3. Walk every radiation source in the world. Skip any farther than `MAX_SOURCE_RADIUS_M`.
4. For each source, compute the vector from source → apex. `SubChunkRadVector.classify()` picks which of the 6 pyramids the source lives in (the biggest absolute axis wins).
5. Add the source's contribution to that pyramid:
   - `Bq * 1/(dist^2 + 1)` - inverse-square falloff.
   - Split into x-ray Bq + neutron Bq.
   - Track a weighted centroid → `tip[dir]` ("where the radiation comes from", used for debug arrows).
6. The resulting map (subchunk → vector) is posted back to the main thread and stored in `vectorByDim`.

When an entity samples radiation, it reads the vector for its own subchunk - no per-source loop. That makes it O(N) entities, not O(N·M) entities x sources.

A TTL prunes stale vectors. If a dimension has no sources, its whole map is dropped.

### In-World Shielding Raycast

`ShieldingRaycast.cast(level, from, to)` walks a straight line between two world points through the block grid using the **Amanatides–Woo voxel DDA** algorithm.

**Steps**:

1. Compute the direction vector and total length `dist`.
2. `tMaxX/Y/Z` = distance until the ray crosses the next block boundary on each axis. `tDelta` = step size per axis.
3. Loop up to 1024 steps:
   - `tNext` = nearest boundary. `seg` = length of ray inside the current block.
   - Look up the block state in the chunk (cached per chunk column so lookups aren't repeated).
   - `ShieldingRegistry.get(state)` returns coefficients `(xray, neutron)` - linear attenuation per meter.
   - Accumulate `sumX += xray * seg`, `sumN += neutron * seg`.
   - Advance to the next block on the axis with the smallest `tMax`.
4. Return `(exp(-sumX), exp(-sumN))`. Beer–Lambert law: each value is a 0..1 multiplier applied to incoming flux.

The result is two numbers: the fraction of x-rays that survive and the fraction of neutrons that survive. Multiply incoming Bq by these before dose calculation.

**Why it's fast**: chunk-column cache, a single pass walks both attenuations together, empty sections are skipped. The caller caches the result per `(sourceChunk, targetChunk)` pair and invalidates it when blocks change.

### Combined Pipeline

`Source(Bq) → vector accumulator builds 6-pyramid field per subchunk → entity sums all 6 pyramids in its subchunk → raycast from each pyramid's centroid to entity returns attenuation → multiply per channel (x-ray / neutron) → entity dose Sv/h.`

## License

MIT
