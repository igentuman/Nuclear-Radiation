# Nuclear Radiation - Modpacker Guide

How to configure radiation, shielding, and armor **without writing Java**. Two ways to do
everything below:

- **Datapack JSON** - drop files under `data/<your_pack>/nuclear_radiation/<type>/*.json`. Reloads
  with `/reload`. `<your_pack>` is any namespace you like.
- **KubeJS** - scripts in `kubejs/startup_scripts/` (content) and `kubejs/server_scripts/` (recipes
  + events). See the full [KubeJS guide](KubeJS.md).
- **Config TOML** - `config/nuclear_radiation-radiation.toml` for background radiation and tuning.

**Load order** (later wins): built-in defaults → datapack JSON → KubeJS additions → KubeJS removals.

## FAQ

- [How do I make a block / item / fluid radioactive?](#how-do-i-make-a-block--item--fluid-radioactive)
- [There are pre-made radioactive tags - can I just use those?](#there-are-pre-made-radioactive-tags--can-i-just-use-those)
- [How do I set biome / dimension background radiation?](#how-do-i-set-biome--dimension-background-radiation)
- [How do I make a block shield radiation?](#how-do-i-make-a-block-shield-radiation)
- [How do I set armor shielding?](#how-do-i-set-armor-shielding)
- [How do I add or change an isotope?](#how-do-i-add-or-change-an-isotope)
- [How do I make radiation mutate mobs?](#how-do-i-make-radiation-mutate-mobs)
- [How do I make radiation transform blocks?](#how-do-i-make-radiation-transform-blocks)
- [How do I tune the whole simulation (thresholds, intervals, recovery)?](#how-do-i-tune-the-whole-simulation-thresholds-intervals-recovery)
- [How do I clear a player's accumulated dose?](#how-do-i-clear-a-players-accumulated-dose)
- [Can scripts react when a player gets irradiated?](#can-scripts-react-when-a-player-gets-irradiated)

### Units (quick)

| Unit | Meaning | Where |
|---|---|---|
| **Bq** | source activity (decays/s), derived from *atoms* + isotope half-life | bindings, `min_bq` |
| **Gy** | absorbed dose | shielding / armor attenuation |
| **Sv** / **Sv/h** | biological dose (total / rate) | mutations, dose thresholds |

Emission + coefficient values are all `0..1`. Time is game ticks (20 ticks = 1 s).

Built-in isotope ids: `nr:u_238`, `nr:u_235`, `nr:pu_239`, `nr:cs_137`, `nr:i_131`, `nr:sr_90`,
`nr:y_90`, `nr:co_60`, `nr:cf_252`, `nr:th_232`, `nr:am_241`, `nr:h_3`, `nr:po_210`, and ~30 more
(full list in the README).

---

## FAQ

### How do I make a block / item / fluid radioactive?

**Datapack** - `data/<pack>/nuclear_radiation/bindings/my_block.json`. `type` is `item`, `block`,
or `fluid`. `target` is an id, or `#namespace:tag` for a tag. Give it one or more isotopes by
**atom count** (Bq is computed from atoms + half-life at runtime).

```json
{
  "type": "block",
  "target": "minecraft:iron_block",
  "isotopes": {
    "nr:cs_137": { "atoms": 5.0e11 },
    "nr:sr_90":  { "atoms": 2.0e11 }
  }
}
```

Tag target example: `"target": "#c:ores/uranium"`.

**KubeJS** (`startup_scripts/`):

```js
NRStartupEvents.bindings(event => {
    event.block('minecraft:iron_block')
        .isotope('nr:cs_137', 5.0e11)
        .isotope('nr:sr_90', 2.0e11)
    event.item('minecraft:diamond').isotope('nr:co_60', 1.0e12)
    event.fluid('minecraft:water').isotope('nr:h_3', 1.0e9)
    event.itemTag('c:ingots/uranium').isotope('nr:u_238', 8.0e11)
})
```

> A block/item must clear `world_source_min_bq` (config, default 5 GBq) to emit into the world.
> Inventory exposure counts all items regardless.

### There are pre-made radioactive tags - can I just use those?

Yes. Add your item/block to a tag instead of writing a binding:

- Items: `nr:radioactive/{low,medium,high}`, `nr:radioactive/{uranium_ore,uranium_raw,uranium_ingot,uranium_dust,spent_fuel}`
- Blocks: `nr:radioactive/{low,medium,high,uranium_ore}`
- Fluids: `nr:radioactive`

`data/<pack>/tags/item/nr/radioactive/high.json`:

```json
{ "values": ["mymod:enriched_rod"] }
```

### How do I set biome / dimension background radiation?

This is **config**, not a datapack. Edit `config/nuclear_radiation-radiation.toml`, `[background]`.
Format is `"<id>=<uSv/h>"`. Biome beats dimension beats global default.

```toml
[background]
    default_usv_per_hour = 0.1
    # per dimension
    level_usv_per_hour = [
        "minecraft:the_nether=1.5",
        "mymod:wasteland=1500.5",
        "minecraft:the_end=1.3"
    ]
    # per biome (highest priority)
    biome_usv_per_hour = [
        "minecraft:nether_wastes=50.0",
        "minecraft:deep_dark=70.0",
        "mymod:fallout=1500.0"
    ]
```

### How do I make a block shield radiation?

**Datapack** - `data/<pack>/nuclear_radiation/shielding/my_shield.json`. Bind to a **tier preset**
(`light`, `mid`, `heavy`, `extra_heavy`), or give **raw** per-meter `(xray, neutron)` coefficients.

Tier bind:

```json
{ "target": "minecraft:iron_block", "tier": "heavy" }
```

Raw override (per-meter attenuation, Beer–Lambert):

```json
{ "target": "minecraft:clay", "xray": 0.15, "neutron": 0.40 }
```

Tag bind: `"target": "#c:storage_blocks/lead"`.

Default tier coefficients: `light=(0.10,0.30)`, `mid=(0.30,0.25)`, `heavy=(0.60,0.45)`,
`extra_heavy=(0.95,0.95)` `(xray, neutron)`. Retune a preset globally:

```json
{ "tier_preset": "mid", "xray": 0.35, "neutron": 0.30 }
```

**Or** just add blocks to the tier tags - `nr:shielding/{light,mid,heavy,extra_heavy}` (block tags),
no JSON binding needed.

**KubeJS**:

```js
NRStartupEvents.shielding(event => {
    event.block('minecraft:iron_block').tier('heavy')
    event.block('minecraft:clay').raw(0.15, 0.40)
    event.blockTag('c:storage_blocks/lead').tier('heavy')
    event.tierPreset('mid', 0.35, 0.30)   // retune a preset
})
```

### How do I set armor shielding?

**Datapack** - `data/<pack>/nuclear_radiation/armor_protection/my_armor.json`. Per-channel
`(xray, alpha, beta, neutron)`, each `0..1`. A worn set stacks multiplicatively. `gas_protection`
`true` blocks inhaled radioactive gas (radon/xenon).

```json
{
  "target": "minecraft:netherite_chestplate",
  "xray": 0.5,
  "alpha": 0.9,
  "beta": 0.7,
  "neutron": 0.3,
  "gas_protection": false
}
```

**KubeJS** - `add(itemId, xray, alpha, beta, neutron)`:

```js
NRStartupEvents.armor(event => {
    event.add('minecraft:netherite_chestplate', 0.5, 0.9, 0.7, 0.3)
    event.remove('minecraft:golden_boots')
})
```

Vanilla iron/gold/netherite and the mod hazmat set already have defaults.

### How do I add or change an isotope?

**Datapack** - `data/<pack>/nuclear_radiation/isotopes/my_isotope.json`. Emission fractions
`(alpha, beta, xray, neutron)`, `half_life_ticks`, and `quality` (Sv/Gy factor per channel).

```json
{
  "alpha": 0.0,
  "beta": 0.2,
  "xray": 0.8,
  "neutron": 0.0,
  "half_life_ticks": 3326044559,
  "quality": { "xray": 1.0, "beta": 1.0, "alpha": 20.0, "neutron": 10.0 }
}
```

**KubeJS** is easier (half-life helpers, decay chains):

```js
NRStartupEvents.isotopes(event => {
    event.add('nr:my_cobalt').xray(0.8).beta(0.2).halfLifeYears(5.27)
    event.add('nr:my_plutonium').alpha(0.95).xray(0.05)
        .halfLifeYears(24110).decaysTo('nr:u_235')
    event.remove('nr:cf_252')
})
```

### How do I make radiation mutate mobs?

Recipe type `nuclear_radiation:mutation`. Entity transforms once it has accumulated
`total_dose_sv` while its current rate is within `[min_sv_per_hour, max_sv_per_hour]`. One-shot per
entity. `chance` and `nbt` optional.

**Datapack** - `data/<pack>/recipe/mutate_cow.json`:

```json
{
  "type": "nuclear_radiation:mutation",
  "input":  { "entity": "minecraft:cow" },
  "result": { "entity": "minecraft:mooshroom" },
  "total_dose_sv": 0.1,
  "min_sv_per_hour": 0.05,
  "max_sv_per_hour": 2.0,
  "chance": 0.3
}
```

`nbt` match example (white sheep → red): `"input": { "entity": "minecraft:sheep", "nbt": "{Color:0b}" }`.

**KubeJS** (`server_scripts/`):

```js
ServerEvents.recipes(event => {
    event.recipes.nuclear_radiation.mutation(
        { entity: 'minecraft:cow' }, { entity: 'minecraft:mooshroom' },
        0.1, 0.05, 2.0, 0.3)   // totalSv, minSv/h, maxSv/h(opt), chance(opt)
})
```

### How do I make radiation transform blocks?

Recipe type `nuclear_radiation:block_irradiation`. A block near a strong source (local attenuated
activity ≥ `min_bq`) transforms into a weighted-random output. `weight` defaults to 1; `chance`
optional.

**Datapack** - `data/<pack>/recipe/irradiate_grass.json`:

```json
{
  "type": "nuclear_radiation:block_irradiation",
  "input": "minecraft:grass_block",
  "min_bq": 1.0e10,
  "chance": 0.02,
  "outputs": [
    { "block": "minecraft:coarse_dirt", "weight": 3 },
    { "block": "minecraft:gravel" }
  ]
}
```

`input` can be `"#tag"`. **KubeJS**:

```js
ServerEvents.recipes(event => {
    event.recipes.nuclear_radiation.block_irradiation(
        'minecraft:grass_block', 1.0e10,
        [{ block: 'minecraft:coarse_dirt', weight: 3 }, { block: 'minecraft:gravel' }]
    ).chance(0.02)
})
```

### How do I tune the whole simulation (thresholds, intervals, recovery)?

`config/nuclear_radiation-radiation.toml`. Key sections:

- `[radiation]` - sim intervals, `max_source_radius_m`
- `[thresholds_sv_per_hour]` - `mild` / `moderate` / `severe` / `lethal` phase cutoffs
- `[recovery]` - `base_decay_per_hour_sv` (how fast dose clears)
- `[inventory]` - `armor_blocks_inventory`, `inventory_alpha_pass`, `inventory_beta_pass`
- `[world_sources]` - `world_source_min_bq`, `static_half_life_years`
- `[block_irradiation]` - enable, interval, radius, rays

See the README "Configuration" section for the full list.

### How do I clear a player's accumulated dose?

`/nr clear <player>` (needs permission level 2). Resets career Sv, Sv/h, protection, and internal
contamination.

### Can scripts react when a player gets irradiated?

Yes, KubeJS `NRServerEvents.dosePhase` fires when an entity crosses into a new phase
(1 mild → 4 lethal). See the [KubeJS guide](KubeJS.md#server-dose-phase-event).

---

For programmatic (Java) integration, see the [Mod Developer Guide](ModDevelopers.md).
