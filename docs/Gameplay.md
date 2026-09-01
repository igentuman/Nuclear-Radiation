# Nuclear Radiation - Player Guide

A physics-driven radiation mod for Minecraft 1.21.1 (NeoForge). Radiation is modelled with real
units: **Bq** (source activity), **Gy** (absorbed dose), and **Sv** (biological dose). You
accumulate a **career dose** in Sievert over time and suffer escalating effects as it rises.

---

## Table of Contents

- [Quick Start: Don't Panic](#quick-start-dont-panic)
- [Understanding Radiation](#understanding-radiation)
- [Dose Stages & Effects](#dose-stages--effects)
- [Lung Pollution](#lung-pollution)
- [Tools](#tools)
- [Hazmat Suit](#hazmat-suit)
- [Shielding Upgrades (Smithing)](#shielding-upgrades-smithing)
- [Block Shielding](#block-shielding)
- [Medicine & Treatment](#medicine--treatment)
- [Radioactive Items in Your Inventory](#radioactive-items-in-your-inventory)
- [Visual Warnings](#visual-warnings)
- [Block Irradiation](#block-irradiation)
- [Mob Mutations](#mob-mutations)
- [Corium](#corium)
- [Fallout Dust](#fallout-dust)
- [Creative Radiation Source](#creative-radiation-source)
- [Background Radiation](#background-radiation)
- [Death & Respawn](#death--respawn)
- [Commands](#commands)
- [JEI / EMI](#jei--emi)

---

## Quick Start: Don't Panic

1. **Craft a Geiger Counter** (iron, redstone, glass pane, copper) and keep it in your hotbar. It
   clicks faster as radiation rises. The HUD shows your current dose rate in Sv/h and counts per
   minute (CPM).
2. **Craft a Dosimeter** (iron, glass pane, comparator, redstone) and carry it anywhere in your
   inventory. It adds a dose bar to your HUD showing total accumulated Sv and current Sv/h.
3. **Wear a Hazmat Suit** when entering contaminated areas. It dramatically reduces all radiation
   channels (x-ray, alpha, beta, neutron).
4. **Keep radioactive items away from you** when not needed. Items in your inventory irradiate you
   too, not just blocks in the world.
5. **Carry medicine**. Iodine pills and Prussian Blue are cheap early-game countermeasures. Radaway
   is the strongest treatment.

---

## Understanding Radiation

Radiation comes from **radioactive isotopes** assigned to items, blocks, and fluids. Each isotope
emits some combination of four channels:

| Channel | Blocked by | Quality factor | Notes |
|---|---|---|---|
| **Alpha** | Paper, leather, a few cm of air | Q = 20 | Most damaging if inhaled/ingested; harmless externally |
| **Beta** | Thin plastic, cloth | Q = 1 | Moderate penetration, moderate damage |
| **X-ray / Gamma** | Dense materials (lead, concrete, gold) | Q = 1 | Penetrates most blocks; primary external hazard |
| **Neutron** | Hydrogen-rich materials (water, plastic, concrete) | Q = 10 | Highly penetrating, highly damaging; only heavy shielding stops it |

Your body absorbs a **dose** measured in Sievert (Sv). The mod tracks two numbers:

- **Sv/h (dose rate)** — how fast you're being irradiated right now. Driven by nearby sources,
  shielding, and armor.
- **Total Sv (career dose)** — accumulated exposure over your character's lifetime. Recovers slowly
  over time and is halved on death.

Both matter: high Sv/h triggers acute effects, while high career Sv triggers chronic effects. The
**worse** of the two determines your current harm stage.

---

## Dose Stages & Effects

Your harm stage is `max(rate band, career band)`. Effects escalate with each stage:

| Stage | Name | Sv/h threshold | Career Sv threshold | Effects |
|---|---|---|---|---|
| 0 | Safe | < 0.001 | < 0.5 | None |
| 1 | Mild | >= 0.001 (1 mSv/h) | >= 0.5 Sv | Weakness, Unluck, Mining Fatigue, occasional vomiting |
| 2 | Moderate | >= 0.5 (500 mSv/h) | >= 2.0 Sv | Weakness II, Nausea, Mining Fatigue II, Slowness, more frequent vomiting |
| 3 | Severe | >= 10 (10 Sv/h) | >= 5.0 Sv | Blindness, radiation damage (1.5 hearts per tick), frequent vomiting |
| 4 | Lethal | >= 100 (100 Sv/h) | >= 8.0 Sv | Wither, massive radiation damage (20 hearts per tick), constant vomiting |

> Vomiting is accompanied by a sound and particle effect. At higher stages it happens more often.

Your career dose slowly decays over time (base rate 0.0001 Sv/h, modified by config). Medicine and
purge effects accelerate this recovery.

---

## Lung Pollution

Separate from external dose, you can accumulate **lung pollution** by:

- **Inhaling radioactive gas clouds** — strong sources (>= 200 GBq) emit gas clouds. Standing in one
  without gas-protective headgear gives a chance per tick to inhale.
- **Carrying airborne contaminant items** — items tagged as `nr:airborne_contaminant` in your
  inventory slowly pollute your lungs if your helmet doesn't provide gas protection.

Lung pollution has its own stages:

| Stage | Pollution | Effects |
|---|---|---|
| Low | < 34% | None |
| Mid | >= 34% | Weakness, occasional coughing |
| High | >= 75% | Weakness II, lung cancer damage (2 hearts per 2 seconds), frequent coughing |

Lungs recover passively over time. **Purge effects** (from Radaway, Anti-Rad Injection, Prussian
Blue, etc.) speed up lung recovery.

---

## Tools

### Geiger Counter

- **Recipe**: Iron ingots + redstone + glass pane + copper ingot
- Hold it in your main or off-hand to activate.
- Emits audible **click sounds** that speed up and rise in pitch as dose rate increases.
- HUD shows current **dose rate (Sv/h)** and **CPM** (counts per minute).
- Silent below ~1 uSv/h (background level).

### Dosimeter

- **Recipe**: Iron ingot + glass panes + comparator + redstone
- Keep it **anywhere in your inventory** — no need to hold it.
- HUD shows a **dose bar** at the bottom-center of your screen:
  - **Total** accumulated Sv (text above bar)
  - **Rate** in Sv/h (text above bar, color-coded by severity)
  - Bar fills toward the acute-lethal reference (~50 Sv); color shifts green to yellow to orange to
    red.

---

## Hazmat Suit

A full armor set designed for working in radioactive environments. Crafted from **Phantom Membrane**
and **Leather armor pieces** at a crafting table.

| Piece | Recipe Core | Slots |
|---|---|---|
| Helmet | Leather Helmet + Phantom Membrane | Head |
| Chestplate | Leather Chestplate + Phantom Membrane | Chest |
| Leggings | Leather Leggings + Phantom Membrane | Legs |
| Boots | Leather Boots + Phantom Membrane | Feet |

The Hazmat Suit provides radiation attenuation across all four channels (x-ray, alpha, beta,
neutron). Wearing more pieces stacks the protection multiplicatively. The **helmet** also provides
**gas protection**, blocking inhaled radioactive gas and airborne contaminants.

> Vanilla iron, gold, chain, and netherite armor also have built-in radiation protection values
> (lower than hazmat). Check item tooltips to see exact percentages.

---

## Shielding Upgrades (Smithing)

You can add radiation shielding to **any armor piece** (vanilla or modded) at a **Smithing Table**:

1. Place the armor piece in the **base** slot.
2. Place a shielding item in the **addition** slot.

Four shielding tiers are available, each craftable:

| Item | Recipe | Shielding value |
|---|---|---|
| Rad Shielding (Light) | Leather + Iron Nuggets + Clay + Copper | +2% |
| Rad Shielding (Medium) | Iron + Lapis + Gold Block | +4% |
| Rad Shielding (Heavy) | Obsidian + Gold + Prismarine | +7% |
| Rad Shielding (DPS) | Diamonds + Netherite Ingot | +12% |

Shielding upgrades stack on top of the armor's base protection. Apply them to a full set for maximum
benefit.

---

## Block Shielding

Radiation from world sources is attenuated by blocks between you and the source. The mod performs a
**voxel raycast** through every block along the line and applies Beer-Lambert attenuation per meter.

Blocks are assigned to one of four shielding tiers:

| Tier | Examples (varies by modpack) | X-ray attenuation | Neutron attenuation |
|---|---|---|---|
| **Light** | Light materials | 10%/m | 30%/m |
| **Mid** | Medium density | 30%/m | 25%/m |
| **Heavy** | Dense materials (iron, gold) | 60%/m | 45%/m |
| **Extra Heavy** | Very dense (lead, etc.) | 95%/m | 95%/m |

> A few meters of Heavy or Extra Heavy shielding can reduce radiation by orders of magnitude. Build
> thick walls between yourself and strong sources.

Blocks with shielding show **"Block Shielding"** in their item tooltip.

---

## Medicine & Treatment

| Item | Recipe | Effect |
|---|---|---|
| **Iodine Pill** (x2) | Dried Kelp + Glowstone Dust + Sugar | Removes 0.5 Sv instantly. Grants Iodine Protection (blocks I-131 internal uptake) for 30s. Also grants Radiation Protection + Purge. |
| **Prussian Blue** | Lapis Lazuli + 2 Iron Nuggets | Removes 1.0 Sv instantly. Grants Cesium Purge (accelerates Cs-137 removal) for 2min. Also grants Radiation Protection + Purge (amplifier 1). |
| **Rad Protection Potion** | Glass Bottle + Iodine Pill + Prussian Blue | No instant Sv removal. Grants Radiation Protection for 5min. |
| **Rad Protection Potion II** | Rad Protection Potion + Glowstone Dust | No instant Sv removal. Grants stronger Radiation Protection for 10min (amplifier 1). |
| **Anti-Rad Injection** | Glass Bottle + Iron Nugget + Ghast Tear + Prussian Blue | No instant Sv removal. Grants extremely strong Purge for 5min (amplifier 21). Rapidly clears internal contamination and lung pollution. |
| **Radaway** | Glass Bottle + Ghast Tear + Redstone + Glowstone Dust | Removes 1.0 Sv instantly. Grants Radiation Protection + Purge for 5min. |

### How the effects work

- **Radiation Protection** — reduces incoming dose by up to 95% (scales with amplifier). You still
  accumulate dose, but much slower.
- **Radiation Purge** — increases the speed at which your career dose decays and your lungs recover.
  Higher amplifier = faster recovery.
- **Iodine Protection** — specifically drains internal I-131 contamination.
- **Cesium Purge** — specifically drains internal Cs-137 contamination.

### Recommended usage

- **Before entering a hot zone**: drink a Rad Protection Potion for 5 minutes of reduced dose.
- **After exposure with I-131 contamination**: take Iodine Pills.
- **After exposure with Cs-137 contamination**: take Prussian Blue.
- **Severe contamination / high career dose**: use Radaway or Anti-Rad Injection for rapid cleanup.
- **Always carry a few** — radiation can come from unexpected sources.

---

## Radioactive Items in Your Inventory

Items in your inventory can irradiate you through **inventory exposure**:

- **Alpha** radiation is mostly blocked by containers/clothing (default 0% pass-through).
- **Beta** radiation partially penetrates (default 20% pass-through).
- **X-ray / Gamma** and **Neutron** penetrate fully — armor protection applies.
- Wearing armor reduces inventory exposure. The armor's `armor_blocks_inventory` factor (default
  75%) further reduces inventory dose when you're wearing armor.

**Item tooltips** show:

- Total activity in Bq (color-coded: green < yellow < gold < red < dark red)
- Stack total activity (if stack size > 1)
- Per-channel percentage breakdown (alpha / beta / gamma / neutron)
- Shielding info for blocks
- Protection percentages for armor pieces

> A stack of 64 radioactive items is 64x as hot as a single one. Store radioactive materials in
> chests away from your base, not in your inventory.

---

## Visual Warnings

The mod provides several visual cues:

| Cue | Trigger | What it means |
|---|---|---|
| **White-noise screen overlay** | Dose rate >= 25 mSv/h | Screen static intensifies with dose rate. Get out of the area. |
| **Glow silhouette on dropped items** | Dropped item activity >= 5.5 GBq | A glowing aura around highly radioactive item entities on the ground. |
| **Geiger clicks** | Dose rate >= 1 uSv/h | Audible clicking from a held Geiger Counter. Faster = more dangerous. |
| **Vomit particles + sound** | Dose stage >= 1 | Your character vomits periodically. More frequent at higher stages. |
| **Coughing sound** | Lung pollution >= 34% | Periodic coughing. More frequent at high lung pollution. |
| **Dose bar (HUD)** | Dosimeter in inventory | Bottom-center bar showing total Sv. Color shifts green to red. |
| **Rate/CPM (HUD)** | Geiger Counter held | Top-left text showing Sv/h and CPM. Color-coded by severity. |

The **rate color coding** on the HUD:

| Color | Dose rate range | Meaning |
|---|---|---|
| Green | < 0.1 uSv/h | Background — safe |
| Gray | 0.1 - 10 uSv/h | Minimal |
| Yellow | 10 uSv/h - 1 mSv/h | Low |
| Orange | 1 - 100 mSv/h | Medium — wear protection |
| Red | 100 mSv/h - 10 Sv/h | Elevated — leave the area |
| Dark Red | 10 - 100 Sv/h | High — lethal without heavy shielding |
| Magenta | >= 100 Sv/h | Extreme — death imminent |

---

## Block Irradiation

Strong radiation sources (>= 5 GBq) can **transform nearby blocks** over time. This is recipe-driven
and can be customized by modpacks.

Default transformations:

| Input Block | Min Activity | Chance | Output |
|---|---|---|---|
| Grass Block | 10 GBq | 2% | Coarse Dirt (weight 3) or Gravel (weight 1) |
| Leaves (any) | 5 GBq | 2% | Air (destroyed) |

This means areas near strong sources gradually become barren — grass dies and turns to dirt, leaves
vanish. Keep sources away from areas you want to preserve.

---

## Mob Mutations

Prolonged radiation exposure can **mutate mobs** into different entities. Each mutation is one-shot
per entity (once mutated, it won't mutate again from the same recipe). Mutations require the mob to
have accumulated a certain career dose while its current dose rate is within a specified range.

Default mutations:

| Input | Output | Total Dose | Rate Range (Sv/h) | Chance |
|---|---|---|---|---|
| Cow | Mooshroom | 0.1 Sv | 0.05 - 2.0 | 30% |
| Villager | Zombie Villager | 0.5 Sv | 0.5 - 5.0 | 50% |
| White Sheep | Red Sheep | 0.1 Sv | 0.01 - 1.0 | 50% |
| Zombie | Skeleton | 0.5 Sv | 0.5 - 2.0 | 70% |
| Skeleton | Wither Skeleton | 1.0 Sv | 1.0 - 10.0 | 75% |

> Keep your livestock away from radiation sources, or you may lose them to mutations. On the flip
> side, mutations can be used strategically (e.g., farming Mooshrooms from cows).

---

## Corium

**Corium** is a molten fluid — the "corium" that forms during a nuclear meltdown. It is extremely
dangerous:

- **Temperature**: 3000K — flows like lava but slower and thicker.
- **Melts blocks**: Corium melts through the block beneath it. The melt chance depends on the
  block's hardness. Flowing corium melts much slower than source blocks.
- **Burns entities**: Any entity within the burn radius (default 5 blocks) is set on fire and takes
  direct damage (default 4 hearts per update).
- **Solidifies**: Corium source blocks solidify into **Corium Block** over time (default ~2 minutes)
  or when quenched with water (3 water contacts needed).
- **Bucket**: Molten corium can be picked up with a bucket.

Corium Block is a solid, stone-like block that can be mined and used as decoration (or stored safely
away from living areas). It is not radioactive by itself unless a modpack assigns it an isotope
binding.

---

## Fallout Dust

**Fallout Dust** is a thin layer block (2 pixels tall) that sits on top of solid blocks. It can be
placed manually or generated by modpack scripts as a contamination residue.

- When the block below it is removed or becomes non-supporting, the fallout dust drops as a
  **Fallout Dust item** (x2).
- It has low hardness (0.5) and a sandy sound.

---

## Creative Radiation Source

A **Creative-only** block for testing and modpack development. Right-click it to open a GUI where
you can set the activity (in MBq) for each of the four channels:

- Alpha (Bq)
- Beta (Bq)
- X-ray (Bq)
- Neutron (Bq)

The block registers as a world radiation source and will irradiate nearby entities, transform
blocks, and emit gas clouds just like any real source. Useful for testing shielding, medicine, and
survival scenarios without needing radioactive items.

---

## Background Radiation

All dimensions and biomes have a **background radiation level** in uSv/h. This is always present,
even without any radioactive items or blocks.

Default background levels:

| Location | uSv/h | Notes |
|---|---|---|
| Overworld (default) | 0.1 | Natural background, harmless |
| The Nether | 1.5 | Slightly elevated |
| The End | 1.3 | Slightly elevated |
| Deep Dark biome | 70.0 | Significant — wear protection |
| Nether Wastes biome | 50.0 | Significant |

Modpacks can override these per-dimension and per-biome. Some modpacks may set hostile dimensions to
extremely high levels (e.g., wasteland biomes at 1500 uSv/h = 1.5 mSv/h).

> Background radiation is reduced by armor protection, same as any other radiation source.

---

## Death & Respawn

When you die (for any reason, not just radiation), your **accumulated career dose is halved**. This
is a deliberate mercy mechanic — death is a reset, not a permanent sentence.

Your career dose also slowly decays over time even without dying. The base recovery rate is 0.0001
Sv/h (modified by config). Medicine with purge effects accelerates this.

---

## Commands

| Command | Permission | Effect |
|---|---|---|
| `/nr clear <player>` | 2 (op) | Resets the target player's career Sv, Sv/h, protection factor, decay multiplier, and internal contamination to zero. |

---

## JEI / EMI

If JEI or EMI is installed, the mod adds recipe/info categories:

- **Isotope Stats** — half-life, emission channels, quality factors for each isotope
- **Radioactive Items** — all items/blocks/fluids with radiation bindings, showing activity and
  channel breakdown
- **Decay Graph** — visual decay chains showing how isotopes transform over time
- **Armor Protection** — per-channel protection percentages for all registered armor
- **Block Shielding** — shielding tier and coefficients for registered blocks
- **Mutation** — entity-to-entity mutation recipes with dose/rate thresholds
- **Block Irradiation** — block-to-block irradiation recipes with activity thresholds
- **Shielding Upgrade** — smithing recipes for applying shielding items to armor

Use JEI/EMI to look up what's radioactive, what protects you, and what mutations are possible.

---

## Tips for Survival

1. **Always carry a Geiger Counter and Dosimeter.** The Geiger tells you *where* the danger is; the
   Dosimeter tells you *how much* you've taken.
2. **Wear full Hazmat when handling radioactive materials.** Even a few seconds near a strong source
   without protection can give you a dangerous dose.
3. **Build storage away from your base.** Radioactive items in chests still emit into the world if
   they exceed the world source threshold (5 GBq by default). Keep them far from where you live and
   work.
4. **Shield your base.** A few blocks of Heavy-tier shielding between you and a source can reduce
   dose by 90%+.
5. **Carry medicine at all times.** Iodine pills are cheap and remove 0.5 Sv. Radaway is expensive
   but removes 1.0 Sv and gives 5 minutes of protection.
6. **Watch your dose bar.** Green is fine. Yellow means slow down. Orange means get out. Red means
   you're in serious trouble.
7. **Don't stand in gas clouds.** Radioactive gas (from sources >= 200 GBq) pollutes your lungs. A
   hazmat helmet blocks inhalation.
8. **Death halves your dose.** If you're stuck at a high career dose and medicine isn't enough,
   dying is a (painful) option.
9. **Respawn doesn't clear everything.** Your dose is halved, not reset. Use `/nr clear` (if you
   have permission) for a full reset.
10. **Read item tooltips.** Radioactive items show their activity and channel breakdown. Armor shows
    protection percentages. Shielding blocks show their tier. This information is critical for
    survival.
