# Nuclear Radiation

Physics-driven radiation simulation mod for Minecraft 1.21.1 (NeoForge).

Models radioactive isotopes, decay, contamination, dose, shielding, and medicine using real units (Bq, Gy, Sv) rather than ad-hoc "rads".

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.230+
- Java 21

## Features

### Units & Physics
- **Bq** — radioactive source activity (decays/sec)
- **Gy** — absorbed dose, used for shielding attenuation
- **Sv** — biological dose to entities (rate + career total)
- Per-type quality factors: alpha (Q=20), beta (Q=1), gamma/x-ray (Q=1), neutron (Q≈10)

### Radiation Sources
- Block radiation sources (placed radioactive blocks)
- Item entity sources (dropped radioactive stacks)
- Fluid sources (radioactive fluids in world)
- Container sources (chests/barrels with radioactive contents — point source, no contamination spread)
- Per-chunk soil/air/water contamination profiles

### Decay
- Absolute-expiry model — sources carry `expiryGameTime`, no per-tick decrement
- Lazy decay on sampled access (`atoms *= exp(-λ·Δt)`)
- Optional branching decay chains via `DecayEdge` probabilities
- Built-in isotopes: U-238, U-235, Pu-239, Cs-137, I-131, Sr-90, Co-60, Cf-252

### Exposure Pipeline
- External dose — sampled from per-chunk `ChunkRadVector` (directional gradient, not scalar)
- Inventory dose — per-slot contact factor (held/offhand/armor/main inv)
- Internal dose — ingested/inhaled isotopes per entity
- Stagger buckets prevent tick spikes (`entityId % interval`)

### Shielding
- Block tiers: lead, concrete, borated polyethylene, water tank, steel
- Single-pass dual-attenuation voxel-DDA raycast (x-ray + neutron in one walk)
- Cached per `(sourceChunk, targetChunk)`, invalidated on block change
- Armor sets: lead-lined suit, hazmat, reinforced hazmat (per-type protection)

### Medicine
- Iodine pill — blocks I-131 thyroid uptake
- Prussian blue — accelerates Cs-137 decay
- Anti-rad injection — strong `decayMultiplier` boost
- Brewable rad-protection potions
- Custom MobEffects: `RadiationProtection`, `RadiationPurge`, `IsotopeSpecificProtection`

### Tools
- **Geiger counter** — reads in-world Bq, audible clicks scale with activity
- **Dosimeter** — reads Sv total + Sv/h + breakdown (external/inventory/internal), HUD overlay

### Data-Driven Bindings
Three input paths for assigning `RadiationProfile` to items/blocks/fluids:
1. Datapack JSON (`data/<ns>/nuclear_radiation/bindings/*.json`)
2. Tags (`nr:radioactive/low|medium|high|uranium_ore|spent_fuel`)
3. Item `DataComponent` (`RadiationComponent`) — per-stack runtime profile

## Architecture Highlights

- **Threading**: single worker thread owns chunk vector recompute, decay math, raycasts. World writes always on main thread.
- **Persistence**: per-dimension `SavedData` (`RadiationLevelData`) + `AttachmentType` on chunks/entities. No deprecated capabilities.
- **Performance**: chunk vector cache reduces N entities × M sources from O(N·M) to O(N) per tick. Hard source-radius cutoff `R`.
- **Tick gating**: independent `world_sim_interval_ticks` and `entity_sim_interval_ticks`; doses scale by interval.

See `src/main/java/igentuman/nr/`:
```
api/          public interfaces
core/         RadiationProfile, IsotopeStack, units
simulation/   simulator, chunk vector cache, worker thread
persistence/  SavedData, AttachmentTypes
entity/       exposure, capability
shielding/    raycast, attenuation, armor
medicine/     items, potions, MobEffects
tools/        geiger, dosimeter, HUD
binding/      datapack loader, tag presets, DataComponent
tracking/     WorldSourceRegistry, event listeners
containers/   chest/barrel ContainerRadSource
builder/      IsotopeBuilder, RadiationBindingBuilder
registry/     IsotopeRegistry
config/       TOML bindings
```

## Configuration

Common config: `config/nuclear_radiation-common.toml`
Radiation tuning: `config/nuclear_radiation-radiation.toml`

Key sections:
- `[radiation]` — sim intervals, source radius, chunk vector TTL
- `[radiation.entities]` — ignore list, creative/spectator skip
- `[radiation.thresholds_sv_per_hour]` — mild/moderate/severe/lethal cutoffs
- `[radiation.inventory]` — slot factors, armor inventory attenuation
- `[radiation.world_sources]` — activity floor, contamination spread
- `[radiation.containers]` — auto-attach vanilla containers, open factor

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

## License

MIT
