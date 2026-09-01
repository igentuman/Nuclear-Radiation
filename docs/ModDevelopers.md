# Nuclear Radiation - Mod Developer Guide

Java integration guide for other NeoForge mods.

| Property | Value |
|---|---|
| Mod ID | `nuclear_radiation` |
| Package root | `igentuman.nr` |
| Maven group | `igentuman.nr` |
| Public API surface | `igentuman.nr.api.*` (plus facades in `igentuman.nr.events` and `igentuman.nr.radiation.*`) |

Depend on the mod as `compileOnly` and guard your hooks behind a mod-loaded check, or register
through the [`NREvents`](#7-integration-event-bridge) bridge (recommended - it survives `/reload`).

> **Isotope IDs are `nr:`-prefixed** (`nr:u_238`, `nr:cs_137`, …) even though the mod ID is
> `nuclear_radiation`. Constants live in `registry/Isotopes.java` (`Isotopes.U_238`,
> `Isotopes.CS_137`, …). Unknown isotope IDs are silently dropped by profile/component builders.

---

## Contents

1. [Units](#units)
2. [Radiation sources](#1-radiation-sources)
3. [Radiation profiles](#2-radiation-profiles)
4. [Bindings from Java](#3-bindings-from-java)
5. [Shielding from Java](#4-shielding-from-java)
6. [Isotopes from Java](#5-isotopes-from-java)
7. [Reading / sampling radiation](#6-reading--sampling-radiation)
8. [Integration event bridge](#7-integration-event-bridge)

---

## Units

| Unit | Meaning | Usage |
|---|---|---|
| **Bq** | Source activity (decays/s) | Derived from *atoms* + isotope half-life |
| **Gy** | Absorbed dose | Used for shielding/armor attenuation |
| **Sv** | Biological dose (career total) | Accumulated on entities |
| **Sv/h** | Biological dose rate | Current exposure rate on entities |

- Emission fractions and attenuation coefficients are `0..1`.
- Time is measured in ticks (20/s).

---

## 1. Radiation sources

### Interfaces

All interfaces live under `api/`:

- **`IRadiationSource`** (`api/shielding/`) - base interface:
  `UUID getId()`, `RadiationProfile getProfile()`, `BlockPos getPosition()`,
  `ResourceKey<Level> getDimension()`, `double activityBq()`, `boolean isActive()`.

- **`IPointRadiationSource`** (`api/`) - extends `IRadiationSource`, adds:
  `double radius()`, `Vec3 emissionCenter()`, `double xRayBq()`, `alphaBq()`, `betaBq()`,
  `neutronBq()`.

- **`DecayGraph.WorldRadSource`** (`api/`) - extends `IPointRadiationSource`, the type the
  registry accepts. Adds: `long spawnedTick()`, `boolean contaminatesArea()`,
  `default long expiryGameTime()`.

### Base class

`api/AbstractWorldRadSource` implements `DecayGraph.WorldRadSource`:

```java
protected AbstractWorldRadSource(UUID id, ResourceKey<Level> dim, BlockPos pos,
                                 RadiationProfile profile, long spawnedTick)
```

- Default `radius()` = `32.0`.
- Bq getters derive from the profile.
- Additional public methods: `advanceDecay(long)`, `refresh(RadiationProfile)`, `markDead()`.

### Concrete sources

Located in `radiation/source/`:

| Class | Constructor | Notes |
|---|---|---|
| `BlockRadSource` | `(id, dim, pos, profile, spawnedTick)` | Overload: `(…, boolean contaminatesArea)` |
| `FluidRadSource` | `(id, dim, pos, profile, spawnedTick)` | Always contaminates |
| `ItemEntityRadSource` | `(ItemEntity entity, profile, spawnedTick)` | Bq scaled by `count * 10` |
| `ContainerRadSource` | `(id, dim, pos, profile, spawnedTick, double containerAttenuation)` | Attenuates x-ray/neutron, zeroes α/β |

### Registry

`radiation/source/WorldSourceRegistry` (per-`ServerLevel`):

```java
static WorldSourceRegistry get(ServerLevel level)
static void unload(ServerLevel level)
void register(DecayGraph.WorldRadSource s)   // ignores sources below WORLD_SOURCE_MIN_BQ
void remove(UUID id)
DecayGraph.WorldRadSource atBlock(BlockPos pos)
ItemEntityRadSource forItemEntity(UUID entityUuid)
Collection<DecayGraph.WorldRadSource> all()
List<DecayGraph.WorldRadSource> queryRadius(Vec3 center, double radius)
void setChunkRadiation(BlockPos pos, RadiationProfile air, RadiationProfile water, RadiationProfile soil)
```

`register(...)` also feeds `RadiationSimulator`.

### Registering a custom source

Extend `AbstractWorldRadSource` (or use `BlockRadSource` directly) and register:

```java
ServerLevel level = ...;
RadiationProfile profile = RadiationProfileBuilder.create()
        .timestamp(level.getGameTime())
        .isotope(Isotopes.CS_137, 1.0e18)   // atoms
        .build();

WorldSourceRegistry.get(level).register(
        new BlockRadSource(UUID.randomUUID(), level.dimension(),
                pos.immutable(), profile, level.getGameTime()));
```

---

## 2. Radiation profiles

`api/RadiationProfile` - a map of `IsotopeStack` keyed by isotope ID:

```java
RadiationProfile()                 RadiationProfile(Map<String,IsotopeStack>)   static empty()
void put(IsotopeStack)             IsotopeStack get(String id) / get(Isotope)
Collection<IsotopeStack> stacks()  boolean isEmpty()
double totalActivityBq()           xRayActivityBq() / alphaActivityBq() / betaActivityBq() / neutronActivityBq()
RadiationProfile copy(long timestamp)
void mergeAtoms(RadiationProfile other, double scale, long timestamp)
long advanceDecay(long currentTick, double floorBq)     long expiryTick(double floorBq)
```

### Builder

`api/RadiationProfileBuilder` - the ergonomic way to build a profile:

```java
RadiationProfile p = RadiationProfileBuilder.create()
        .timestamp(level.getGameTime())
        .isotope(Isotopes.U_238, 1.0e18)     // by id string or Isotope
        .isotope(Isotopes.CS_137, 5.0e17)
        .build();
double bq = p.totalActivityBq();
```

### IsotopeStack

`api/isotope/IsotopeStack(Isotope isotope, double atoms, long timestamp)` - provides:
`atoms()`, `setAtoms(double)`, `currentActivityBq()`.

---

## 3. Bindings from Java

Attach a `RadiationProfile` to items/blocks/fluids, or stamp a per-stack component.

### Static binding registry

`api/binding/Bindings` - static registry; values are `Supplier<RadiationProfile>`:

```java
putItem(ResourceLocation, Supplier<RadiationProfile>)     putBlock(...)   putFluid(...)
putItemTag(TagKey<Item>, ...)   putBlockTag(...)   putFluidTag(...)
removeItem/removeBlock/... ;  getItem/getBlock/getFluid(ResourceLocation) ;  clear()
```

### Resolution facade

`api/binding/RadiationBindings` - resolves in order: component → id → tag:

```java
static RadiationProfile of(ItemStack)      // RadiationComponent wins, then binding, then tags
static RadiationProfile of(BlockState)     static RadiationProfile of(FluidState)
static boolean isRadioactive(ItemStack)    static Optional<RadiationProfile> forItem(ItemStack)
```

### Data component

`api/binding/RadiationComponent` - `DataComponent` registered under `nuclear_radiation:radiation`:

```java
static final Supplier<DataComponentType<RadiationComponent>> TYPE
static RadiationComponent fromProfile(RadiationProfile profile, long currentTick)
RadiationProfile toProfile(long timestamp)
Map<String,Double> atomsByIsotope()   long lastTick()   boolean isEmpty()
```

### Examples

```java
// A) Bind a profile to an item ID (re-run on reload - see NREvents)
Bindings.putItem(ResourceLocation.parse("yourmod:hot_ingot"),
        () -> RadiationProfileBuilder.create().isotope(Isotopes.CO_60, 1.0e17).build());

// B) Stamp a per-stack RadiationComponent
RadiationProfile p = RadiationProfileBuilder.create().isotope(Isotopes.CS_137, 1e18).build();
stack.set(RadiationComponent.TYPE.get(),
          RadiationComponent.fromProfile(p, level.getGameTime()));
```

---

## 4. Shielding from Java

### Self-declaring blocks and items

Implement the interface directly - no registration needed:

- **`IShieldingBlock`** (`api/shielding/`, on a `Block`):
  `double xrayAttenuationCoeff()`, `double neutronAttenuationCoeff()`,
  `default double gyAbsorbed()`.

- **`IRadiationArmor`** (`api/shielding/`, on an `Item`):
  `xrayProtection()`, `alphaProtection()`, `betaProtection()`, `neutronProtection()`,
  `boolean gasProtection()`, `EquipmentSlot slot()`. All values `0..1`.

### Registries

For vanilla or other-mod content that cannot implement the interfaces directly:

```java
// radiation/shielding/world/ShieldingRegistry
record Coeffs(double xray, double neutron)
static void register(Block block, double xray, double neutron)   // per-meter coeffs
static Coeffs get(BlockState state)      // honors IShieldingBlock first
static boolean shields(BlockState state)

// api/shielding/ArmorProtectionRegistry
record Protection(double xray, double alpha, double beta, double neutron, boolean protectsFromGas)
static void register(Item item, Protection p)     static void registerTag(TagKey<Item>, Protection p)
static Protection get(ItemStack)                   // honors IRadiationArmor first
static Protection summed(LivingEntity)             // multiplicative over armor slots
```

### Raycast

`radiation/shielding/world/ShieldingRaycast`:

```java
static AttenuationResult cast(Level level, Vec3 from, Vec3 to)
static AttenuationResult cast(ServerLevel level, Vec3 from, Vec3 to)
// AttenuationResult(double xrayPass, double neutronPass) - transmitted fractions exp(-Σcoeff)
```

### Examples

```java
ShieldingRegistry.register(YourBlocks.LEAD.get(), 2.5, 0.4);
ArmorProtectionRegistry.register(YourItems.HAZMAT_HELMET.get(),
        new ArmorProtectionRegistry.Protection(0.3, 0.9, 0.9, 0.1, true));

AttenuationResult a = ShieldingRaycast.cast(serverLevel, sourcePos, entityEyes);
double survivingXrayBq = xrayBq * a.xrayPass();
```

---

## 5. Isotopes from Java

### Registry

`api/isotope/IsotopeRegistry` - provides:
`register(Isotope)`, `get(String id)`, `contains(String)`, `remove(String)`, `all()`, `clear()`.

### Builder

`api/isotope/IsotopeBuilder` - builds, registers, and wires decay edges:

```java
static IsotopeBuilder create(String id)
xray/alpha/beta/neutron(float)   halfLife(long ticks)
decaysTo(String)                 branch(String targetId, double probability)
quality(float qXRay, float qBeta, float qAlpha, float qNeutron)
Isotope build()                  Isotope register()   // registers + adds DecayGraph edges
```

> **Reload wipe:** `IsotopesReloadListener` calls `IsotopeRegistry.clear()` + `DecayGraph.clear()`
> on every datapack reload, then re-applies defaults and fires `NREvents.AFTER_ISOTOPES_RELOAD`.
> Register custom isotopes **inside that hook**, not once at startup, or they vanish on `/reload`.

### Example

```java
NREvents.AFTER_ISOTOPES_RELOAD.add(() ->
    IsotopeBuilder.create("yourmod:custom_137")
        .alpha(0f).beta(0.4f).xray(0.6f).neutron(0f)
        .halfLife(631_128_000L)          // ticks (~1 yr; TICKS_PER_YEAR in DefaultIsotopes)
        .quality(1f, 1f, 20f, 10f)
        .branch("nr:cs_137", 1.0)
        .register());
```

### Half-life constants

Defined in `registry/DefaultIsotopes`:

| Constant | Value (ticks) | Real-world equivalent |
|---|---|---|
| `TICKS_PER_YEAR` | `631_128_000` | ~1 year |
| `TICKS_PER_DAY` | `24_000` | 1 Minecraft day |
| `TICKS_PER_HOUR` | `1_000` | 1 Minecraft hour |

---

## 6. Reading / sampling radiation

### Entity dose

Attachment `radiation/storage/EntityRadiationData`, registered as
`NRAttachments.ENTITY_RADIATION`:

```java
EntityRadiationData d = entity.getData(NRAttachments.ENTITY_RADIATION.get());
double careerSv  = d.svTotalCareer();
double svPerHour = d.svPerHour();
```

Additional accessors:

| Method | Returns |
|---|---|
| `svPerHourAmbient()` | Ambient dose rate |
| `protectionFactor()` | Current protection multiplier |
| `decayMultiplier()` | Current decay multiplier |
| `internalContamination()` | `Map<String, Double>` of isotope ID → atoms |
| `lastDoseStage()` | Last dose stage reached |
| `addSv(double)` | Add to career total |
| `addInternal(String isotopeId, double atoms)` | Add internal contamination |

### World Bq sampling

`radiation/simulation/RadiationSimulator`:

```java
static RadiationSimulator get()
SubChunkRadVector getChunkVector(ServerLevel level, ChunkPos pos, int cy)   // null if none
```

```java
SubChunkRadVector v = RadiationSimulator.get().getChunkVector(level, chunkPos, blockY >> 4);
double localXRayBq = v == null ? 0.0 : v.totalXRay();   // also v.totalNeutron(), v.maxBq
```

`SubChunkRadVector` fields and methods:

| Field / Method | Type | Description |
|---|---|---|
| `xRayBq` | `double[]` | 6 directional bins |
| `neutronBq` | `double[]` | 6 directional bins |
| `totalXRay()` | `double` | Sum of all x-ray bins |
| `totalNeutron()` | `double` | Sum of all neutron bins |
| `maxBq` | `double` | Maximum bin value |
| `isEmpty()` | `boolean` | Whether all bins are zero |
| `isExpired(long now)` | `boolean` | Whether the vector has expired |

For a simple radius scan, `WorldSourceRegistry.get(level).queryRadius(center, radius)` is the most
direct query.

---

## 7. Integration event bridge

`events/NREvents` (package `igentuman.nr.events`) - the recommended integration entry point. Its
reload hooks re-apply Java registrations that datapack reloads would otherwise wipe.

### Reload hooks

Public `List<Runnable>` fields - add your re-registration logic:

```java
NREvents.AFTER_ISOTOPES_RELOAD    NREvents.AFTER_BINDINGS_RELOAD
NREvents.AFTER_SHIELDING_RELOAD   NREvents.AFTER_ARMOR_RELOAD
```

### Dose-phase callback

Fires on the rising edge of dose stage 1–4. Return `true` to cancel default harm:

```java
interface DosePhaseListener {
    boolean onRise(LivingEntity e, int stage, double svPerHour, double svTotalCareer);
}
static void addDosePhaseListener(DosePhaseListener l)
```

### Examples

```java
// Re-apply bindings after each reload
NREvents.AFTER_BINDINGS_RELOAD.add(() ->
    Bindings.putItem(ResourceLocation.parse("yourmod:hot_ingot"),
        () -> RadiationProfileBuilder.create().isotope(Isotopes.CO_60, 1e17).build()));

// Custom dose-phase handling
NREvents.addDosePhaseListener((entity, stage, svPerHour, svTotal) -> {
    if (stage >= 3 && hasYourAntidote(entity)) return true;  // cancel mod's default harm
    return false;
});
```

---

## See also

- [Modpacker Guide](Modpackers.md) - no-code (datapack / config) configuration
- [KubeJS Guide](KubeJS.md) - scripting integration
- `integration/` package - existing mod bridges (`mekanism`, `nuclear_science`) as reference
  implementations of `NREvents` usage
