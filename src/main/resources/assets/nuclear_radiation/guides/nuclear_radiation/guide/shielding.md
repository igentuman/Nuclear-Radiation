---
navigation:
  title: Shielding
  parent: index.md
  position: 3
item_ids:
  - nuclear_radiation:rad_shielding_light
  - nuclear_radiation:rad_shielding_medium
  - nuclear_radiation:rad_shielding_heavy
  - nuclear_radiation:rad_shielding_dps
---

# Shielding

Shielding reduces the radiation that reaches you. There are two systems: **block shielding**
(world blocks between you and a source) and **armor shielding upgrades** (smithing table
enhancements for your armor).

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

A few meters of Heavy or Extra Heavy shielding can reduce radiation by orders of magnitude. Build
thick walls between yourself and strong sources.

Blocks with shielding show **"Block Shielding"** in their item tooltip.

## Armor Shielding Upgrades (Smithing)

You can add radiation shielding to **any armor piece** (vanilla or modded) at a **Smithing Table**:

1. Place the armor piece in the **base** slot.
2. Place a shielding item in the **addition** slot.

Four shielding tiers are available:

### Light Radiation Shielding

<ItemGrid>
  <ItemIcon id="rad_shielding_light" />
</ItemGrid>

<RecipeFor id="rad_shielding_light" />

- **Shielding value**: +2%

### Medium Radiation Shielding

<ItemGrid>
  <ItemIcon id="rad_shielding_medium" />
</ItemGrid>

<RecipeFor id="rad_shielding_medium" />

- **Shielding value**: +4%

### Heavy Radiation Shielding

<ItemGrid>
  <ItemIcon id="rad_shielding_heavy" />
</ItemGrid>

<RecipeFor id="rad_shielding_heavy" />

- **Shielding value**: +7%

### Dense Radiation Shielding (DPS)

<ItemGrid>
  <ItemIcon id="rad_shielding_dps" />
</ItemGrid>

<RecipeFor id="rad_shielding_dps" />

- **Shielding value**: +12%
- The strongest shielding upgrade available.

## How Upgrades Stack

Shielding upgrades stack on top of the armor's base protection. Apply them to a full set for maximum
benefit. Each armor piece can receive one shielding upgrade, and the effects stack across all four
slots.

## Tips

- **Combine with hazmat armor**: A full [hazmat suit](hazmats.md) with DPS shielding provides the
  highest possible protection.
- **Don't forget block walls**: Even the best armor won't stop neutron radiation from a strong
  source. Use Extra Heavy block shielding between you and the source.
- **Check tooltips**: Armor pieces show their per-channel protection percentages. Shielding blocks
  show their tier.
