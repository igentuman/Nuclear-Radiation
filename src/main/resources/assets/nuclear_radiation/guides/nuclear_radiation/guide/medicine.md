---
navigation:
  title: Medicine & Treatment
  parent: index.md
  position: 4
item_ids:
  - nuclear_radiation:iodine_pill
  - nuclear_radiation:prussian_blue
  - nuclear_radiation:rad_protection_potion
  - nuclear_radiation:rad_protection_potion_2
  - nuclear_radiation:anti_rad_injection
  - nuclear_radiation:radaway
---

# Medicine & Treatment

Radiation sickness is treatable. Different medicines address different aspects of contamination:
instant dose removal, ongoing protection, internal isotope purge, and lung pollution cleanup.

## Available Medicines

### Iodine Pill

<ItemGrid>
  <ItemIcon id="iodine_pill" />
</ItemGrid>

<RecipeFor id="iodine_pill" />

- **Instant effect**: Removes **0.5 Sv** from your career dose immediately.
- **Grants Iodine Protection** for 30 seconds - specifically drains internal I-131 contamination.
- **Also grants** Radiation Protection + Purge.
- **Cheap early-game** countermeasure.

### Prussian Blue

<ItemGrid>
  <ItemIcon id="prussian_blue" />
</ItemGrid>

<RecipeFor id="prussian_blue" />

- **Instant effect**: Removes **1.0 Sv** from your career dose immediately.
- **Grants Cesium Purge** for 2 minutes - accelerates Cs-137 removal.
- **Also grants** Radiation Protection + Purge (amplifier 1).

### Rad-Protection Potion

<ItemGrid>
  <ItemIcon id="rad_protection_potion" />
</ItemGrid>

<RecipeFor id="rad_protection_potion" />

- **No instant Sv removal**.
- **Grants Radiation Protection** for 5 minutes - reduces incoming dose by up to 95%.

### Rad-Protection Potion II

<ItemGrid>
  <ItemIcon id="rad_protection_potion_2" />
</ItemGrid>

<RecipeFor id="rad_protection_potion_2" />

- **No instant Sv removal**.
- **Grants stronger Radiation Protection** for 10 minutes (amplifier 1).

### Anti-Rad Injection

<ItemGrid>
  <ItemIcon id="anti_rad_injection" />
</ItemGrid>

<RecipeFor id="anti_rad_injection" />

- **No instant Sv removal**.
- **Grants extremely strong Purge** for 5 minutes (amplifier 21).
- Rapidly clears internal contamination and lung pollution.

### Radaway

<ItemGrid>
  <ItemIcon id="radaway" />
</ItemGrid>

<RecipeFor id="radaway" />

- **Instant effect**: Removes **1.0 Sv** from your career dose immediately.
- **Grants Radiation Protection + Purge** for 5 minutes.
- The strongest all-round treatment.

## How the Effects Work

- **Radiation Protection** - reduces incoming dose by up to 95% (scales with amplifier). You still
  accumulate dose, but much slower.
- **Radiation Purge** - increases the speed at which your career dose decays and your lungs recover.
  Higher amplifier = faster recovery.
- **Iodine Protection** - specifically drains internal I-131 contamination.
- **Cesium Purge** - specifically drains internal Cs-137 contamination.

## Recommended Usage

- **Before entering a hot zone**: drink a <ItemLink id="rad_protection_potion" /> for 5 minutes of
  reduced dose.
- **After exposure with I-131 contamination**: take <ItemLink id="iodine_pill" /> pills.
- **After exposure with Cs-137 contamination**: take <ItemLink id="prussian_blue" />.
- **Severe contamination / high career dose**: use <ItemLink id="radaway" /> or
  <ItemLink id="anti_rad_injection" /> for rapid cleanup.
- **Always carry a few** - radiation can come from unexpected sources.
