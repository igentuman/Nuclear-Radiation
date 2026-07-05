# Nuclear Radiation — KubeJS Integration

Script-driven customization of isotopes, radioactivity bindings, shielding, armor protection,
recipes, and radiation dose events. The plugin loads automatically when both **Nuclear Radiation**
and **KubeJS** are installed — no setup required.

- **Startup scripts** (`kubejs/startup_scripts/`) define content: isotopes, bindings, shielding, armor.
- **Server scripts** (`kubejs/server_scripts/`) define recipes and react to runtime dose events.

Everything you register survives `/reload`. Load order per reload is:
**built-in defaults → datapack JSON → your KubeJS additions → your KubeJS removals**, so your
scripts always win over the defaults and datapacks.

## Units cheat sheet

| Quantity | Unit | Where it appears |
|---|---|---|
| Source activity | **Bq** (decays/s), derived from *atoms* + half-life | bindings, `min_bq` |
| Absorbed dose | **Gy** | shielding/armor attenuation |
| Biological dose | **Sv** (total) and **Sv/h** (rate) | mutations, dose-phase event |

Emission fractions (`alpha`, `beta`, `xray`, `neutron`) are `0..1`. Protection/attenuation
coefficients are also `0..1` per meter (shielding) or per item (armor). Time is measured in game
ticks (20 ticks = 1 second).

Built-in isotope ids you can reference or decay into: `nr:u_238`, `nr:u_235`, `nr:pu_239`,
`nr:cs_137`, `nr:i_131`, `nr:sr_90`, `nr:y_90`, `nr:co_60`, `nr:cf_252`, `nr:h_3`, `nr:po_210`,
`nr:th_232`, `nr:am_241`, `nr:ra_226`-class actinides, and more (see `registry/Isotopes.java`).

---

## Startup: Isotopes

`NRStartupEvents.isotopes` — register new isotopes or remove existing ones.

```js
NRStartupEvents.isotopes(event => {
    // A simple gamma emitter with a 30-year half-life.
    event.add('nr:my_cobalt')
        .xray(0.8)        // 80% gamma/x-ray
        .beta(0.2)        // 20% beta
        .halfLifeYears(5.27)

    // An alpha emitter that decays into a built-in isotope.
    event.add('nr:my_plutonium')
        .alpha(0.95)
        .xray(0.05)
        .halfLifeYears(24110)
        .quality(1.0, 1.0, 20.0, 10.0) // Sv/Gy quality: xray, beta, alpha, neutron
        .decaysTo('nr:u_235')

    // Branching decay (probabilities need not sum to 1; they are relative weights).
    event.add('nr:my_branching')
        .beta(1.0)
        .halfLifeDays(2.6)
        .branch('nr:y_90', 0.7)
        .branch('nr:sr_90', 0.3)

    // Remove a built-in isotope entirely (also drops its decay edges).
    event.remove('nr:cf_252')
})
```

Half-life helpers (pick one): `.halfLife(ticks)`, `.halfLifeSeconds(s)`, `.halfLifeHours(h)`,
`.halfLifeDays(d)`, `.halfLifeYears(y)`. Isotopes whose effective half-life exceeds the configured
`static_half_life_years` are treated as static (no decrement, no daughter ingrowth).

---

## Startup: Bindings

`NRStartupEvents.bindings` — make items, blocks and fluids radioactive by attaching isotope atoms.
Activity (Bq) is derived from atom count and the isotope's half-life at runtime.

```js
NRStartupEvents.bindings(event => {
    // A single item with one isotope.
    event.item('minecraft:diamond').isotope('nr:co_60', 1.0e12)

    // Multiple isotopes on one target — chain .isotope(...).
    event.block('minecraft:iron_block')
        .isotope('nr:cs_137', 5.0e11)
        .isotope('nr:sr_90', 2.0e11)

    // Fluids.
    event.fluid('minecraft:water').isotope('nr:h_3', 1.0e9)

    // Tags (prefix optional): items, blocks, fluids.
    event.itemTag('c:ingots/uranium').isotope('nr:u_238', 8.0e11)
    event.blockTag('#c:ores').isotope('nr:u_238', 3.0e11)

    // Remove bindings (including built-ins / tag bindings).
    event.removeItem('minecraft:diamond')
    event.removeBlockTag('c:ores')
})
```

Targets: `item`, `block`, `fluid`, `itemTag`, `blockTag`, `fluidTag`.
Removals: `removeItem`, `removeBlock`, `removeFluid`, `removeItemTag`, `removeBlockTag`, `removeFluidTag`.

> Per-stack `RadiationComponent` data and datapack JSON bindings still apply; a stack component
> overrides a binding, and a direct id binding overrides a tag binding.

---

## Startup: Shielding

`NRStartupEvents.shielding` — control how blocks attenuate radiation (Beer–Lambert, per meter).
Bind to a **tier preset** (`light`, `mid`, `heavy`) or to **raw** `(xray, neutron)` coefficients.

```js
NRStartupEvents.shielding(event => {
    // Bind a block to a tier preset.
    event.block('minecraft:iron_block').tier('heavy')

    // Bind a block to raw per-meter coefficients (xray, neutron).
    event.block('minecraft:clay').raw(0.15, 0.40)

    // Bind a block tag.
    event.blockTag('c:storage_blocks/lead').tier('heavy')

    // Retune what a tier preset means globally (xray, neutron).
    event.tierPreset('mid', 0.35, 0.30)

    // Remove a shielding binding.
    event.remove('minecraft:clay')
    event.removeTag('c:storage_blocks/lead')
})
```

Default tier coefficients: `light = (0.10, 0.30)`, `mid = (0.30, 0.25)`, `heavy = (1.00, 0.25)`.

---

## Startup: Armor

`NRStartupEvents.armor` — give any wearable item radiation protection per channel `(xray, alpha,
beta, neutron)`, each `0..1`. A worn set stacks multiplicatively toward `1.0`.

```js
NRStartupEvents.armor(event => {
    // add(itemId, xray, alpha, beta, neutron)
    event.add('minecraft:netherite_chestplate', 0.5, 0.9, 0.7, 0.3)
    event.add('minecraft:iron_helmet', 0.2, 0.8, 0.5, 0.1)

    // Remove a protection entry (including built-in vanilla armor entries).
    event.remove('minecraft:golden_boots')
})
```

Items implementing `IRadiationArmor` declare their own protection and ignore these entries.

---

## Server: Recipes

Two non-crafting recipe types are exposed as typed builders in `ServerEvents.recipes`.

### Mutation

Replaces a living entity with another once it has accumulated enough dose while in a dose-rate
window. One-shot per entity per recipe.

```js
ServerEvents.recipes(event => {
    // mutation(input, result, totalDoseSv, minSvPerHour)
    event.recipes.nuclear_radiation.mutation(
        { entity: 'minecraft:cow' },
        { entity: 'minecraft:mooshroom' },
        5.0,   // total career dose (Sv) required
        1.0    // minimum current rate (Sv/h)
    )

    // Optional max rate and chance, plus entity NBT match/result.
    event.recipes.nuclear_radiation.mutation(
        { entity: 'minecraft:sheep', nbt: '{Color:0b}' },  // white sheep only
        { entity: 'minecraft:sheep', nbt: '{Color:14b}' }, // becomes red
        2.0,        // total_dose_sv
        0.5,        // min_sv_per_hour
        10.0,       // max_sv_per_hour (optional, default +inf)
        0.25        // chance (optional, default 1.0)
    )
})
```

### Block irradiation

Transforms blocks that receive at least `min_bq` of local attenuated activity. `outputs` is a
weighted list.

```js
ServerEvents.recipes(event => {
    // block_irradiation(input, minBq, outputs)
    event.recipes.nuclear_radiation.block_irradiation(
        'minecraft:grass_block',            // block id, or '#tag'
        1.0e9,                              // min_bq
        [
            { block: 'minecraft:dirt', weight: 3 },
            { block: 'minecraft:coarse_dirt', weight: 1 }
        ]
    )

    // Tag input + single output, with a chance.
    event.recipes.nuclear_radiation.block_irradiation(
        '#minecraft:flowers',
        5.0e8,
        [{ block: 'minecraft:wither_rose' }]
    ).chance(0.1)
})
```

### Raw JSON fallback

Any recipe can also be added with `event.custom(...)` using the same field names the serializer
reads — handy for generated data or fields not surfaced by the builder:

```js
ServerEvents.recipes(event => {
    event.custom({
        type: 'nuclear_radiation:mutation',
        input: { entity: 'minecraft:villager' },
        result: { entity: 'minecraft:zombie_villager' },
        total_dose_sv: 8.0,
        min_sv_per_hour: 2.0,
        max_sv_per_hour: 1000.0,
        chance: 0.5
    })
})
```

### Removing / replacing

```js
ServerEvents.recipes(event => {
    event.remove({ type: 'nuclear_radiation:mutation' })          // all mutations
    event.remove({ id: 'nuclear_radiation:cow_to_mooshroom' })    // one recipe
})
```

---

## Server: Dose-phase event

`NRServerEvents.dosePhase` — fires **once** each time an entity's radiation dose stage rises into a
new phase: `1` mild, `2` moderate, `3` severe, `4` lethal. Call `event.cancel()` to suppress the
mod's own default harm effects for that tick and run your own logic instead.

```js
NRServerEvents.dosePhase(event => {
    const entity = event.entity
    const phase = event.phase           // 1..4
    const rate = event.svPerHour        // current Sv/h
    const total = event.totalDoseSv     // accumulated Sv

    if (entity.type === 'minecraft:player') {
        entity.tell(`Radiation phase ${phase}! Rate ${rate.toFixed(3)} Sv/h`)
    }

    // Replace the default lethal effects with something custom.
    if (phase >= 4) {
        entity.potionEffects.add('minecraft:glowing', 200, 0)
        event.cancel() // skip the mod's default harm this tick
    }
})
```

Accessors: `event.entity`, `event.phase`, `event.svPerHour`, `event.totalDoseSv`.

---

## Server: Meltdown particles

`NRServerUtils.emitMeltdown` — registers a persistent radiation-plume particle source at a block
position. The plume emits particles every server tick until the source expires. Sources survive
server restarts (stored in `SavedData`).

```js
// kubejs/server_scripts/meltdown.js

ServerEvents.tick(event => {
    // Trigger a plume at a fixed position for the default duration (2400 ticks = 2 minutes).
    if (event.server.tickCount === 1) {
        const level = event.server.overworld()
        NRServerUtils.emitMeltdown(level, 100, 64, 200)
    }
})

// With a custom duration (6000 ticks = 5 minutes).
NRServerEvents.dosePhase(event => {
    if (event.phase >= 4) {
        const level = event.entity.level
        const pos = event.entity.blockPosition()
        NRServerUtils.emitMeltdown(level, pos.x, pos.y, pos.z, 6000)
    }
})
```

Signatures:
- `NRServerUtils.emitMeltdown(level, x, y, z)` — default duration (2400 ticks).
- `NRServerUtils.emitMeltdown(level, x, y, z, durationTicks)` — custom duration in ticks.

`level` must be a `ServerLevel` (available as `event.server.overworld()`,
`event.entity.level`, etc.). The call is safe to make from any server-thread context.

---

## Notes

- Startup content is defined once at game load and re-applied automatically after every `/reload`.
- If an isotope id used in a binding or recipe isn't registered, that isotope is skipped (check the
  log). Register custom isotopes in `NRStartupEvents.isotopes` before referencing them.
- The dose-phase event runs on the server thread during entity ticking; keep handlers light.
