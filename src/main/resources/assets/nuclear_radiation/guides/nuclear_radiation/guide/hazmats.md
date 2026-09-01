---
navigation:
  title: Hazmat Suit
  parent: index.md
  position: 5
item_ids:
  - nuclear_radiation:hazmat_helmet
  - nuclear_radiation:hazmat_chestplate
  - nuclear_radiation:hazmat_leggings
  - nuclear_radiation:hazmat_boots
---

# Hazmat Suit

A full armor set designed for working in radioactive environments.

## Armor Pieces

<ItemGrid>
  <ItemIcon id="hazmat_helmet" />
  <ItemIcon id="hazmat_chestplate" />
  <ItemIcon id="hazmat_leggings" />
  <ItemIcon id="hazmat_boots" />
</ItemGrid>

### Helmet

<RecipeFor id="hazmat_helmet" />

- The helmet also provides **gas protection**, blocking inhaled radioactive gas and airborne
  contaminants.

### Chestplate

<RecipeFor id="hazmat_chestplate" />

### Leggings

<RecipeFor id="hazmat_leggings" />

### Boots

<RecipeFor id="hazmat_boots" />

## Protection

The Hazmat Suit provides radiation attenuation across all four channels (x-ray, alpha, beta,
neutron). Wearing more pieces stacks the protection multiplicatively.

| Channel | Protected | Notes |
|---|---|---|
| **X-ray / Gamma** | Yes | Reduced by hazmat material density |
| **Alpha** | Yes | Almost fully blocked |
| **Beta** | Yes | Significantly reduced |
| **Neutron** | Yes | Partially reduced; combine with block shielding for strong neutron sources |

The **helmet** also provides **gas protection**, which prevents:
- Inhaling radioactive gas clouds from strong sources (>= 200 GBq)
- Airborne contaminant items in your inventory from polluting your lungs

## Other Armor

Vanilla iron, gold, chain, and netherite armor also have built-in radiation protection values
(lower than hazmat). Check item tooltips to see exact percentages for each channel.

## Enhancing with Shielding Upgrades

You can further enhance hazmat armor with [shielding upgrades](shielding.md) at a Smithing Table.
Applying Dense Radiation Shielding (DPS) to a full hazmat set provides the highest possible armor
protection in the mod.

## Tips

- **Wear full hazmat** when handling radioactive materials. Even a few seconds near a strong source
  without protection can give you a dangerous dose.
- **Always wear the helmet** in areas with radioactive gas clouds - it's the only piece that
  provides gas protection.
- **Combine with medicine**: Wear hazmat for passive protection, and carry
  [medicine](medicine.md) for emergency treatment.
- **Check tooltips**: Each armor piece shows its per-channel protection percentages.
