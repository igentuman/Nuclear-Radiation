---
navigation:
  title: Nuclear Radiation Guide
  position: 1
---

# Nuclear Radiation Guide

A physics-driven radiation mod for Minecraft 1.21.1 (NeoForge). Radiation is modelled with real
units: **Bq** (source activity), **Gy** (absorbed dose), and **Sv** (biological dose). You
accumulate a **career dose** in Sievert over time and suffer escalating effects as it rises.

## Topics

<SubPages />

## Quick Start: Don't Panic

1. **Craft a <ItemLink id="geiger_counter" />** and keep it in your hotbar. It clicks faster as
   radiation rises. The HUD shows your current dose rate in Sv/h and counts per minute (CPM).
2. **Craft a <ItemLink id="dosimeter" />** and carry it anywhere in your inventory. It adds a dose
   bar to your HUD showing total accumulated Sv and current Sv/h.
3. **Wear a <ItemLink id="hazmat_helmet" />** when entering contaminated areas. A full hazmat suit
   dramatically reduces all radiation channels (x-ray, alpha, beta, neutron).
4. **Keep radioactive items away from you** when not needed. Items in your inventory irradiate you
   too, not just blocks in the world.
5. **Carry medicine**. <ItemLink id="iodine_pill" /> pills and <ItemLink id="prussian_blue" /> are
   cheap early-game countermeasures. <ItemLink id="radaway" /> is the strongest treatment.

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

- **Sv/h (dose rate)** - how fast you're being irradiated right now.
- **Total Sv (career dose)** - accumulated exposure over your character's lifetime.

Both matter: high Sv/h triggers acute effects, while high career Sv triggers chronic effects. The
**worse** of the two determines your current harm stage.

## Dose Stages & Effects

| Stage | Name | Sv/h threshold | Career Sv threshold | Effects |
|---|---|---|---|---|
| 0 | Safe | < 0.001 | < 0.5 | None |
| 1 | Mild | >= 0.001 (1 mSv/h) | >= 0.5 Sv | Weakness, Unluck, Mining Fatigue, occasional vomiting |
| 2 | Moderate | >= 0.5 (500 mSv/h) | >= 2.0 Sv | Weakness II, Nausea, Mining Fatigue II, Slowness, more frequent vomiting |
| 3 | Severe | >= 10 (10 Sv/h) | >= 5.0 Sv | Blindness, radiation damage (1.5 hearts per tick), frequent vomiting |
| 4 | Lethal | >= 100 (100 Sv/h) | >= 8.0 Sv | Wither, massive radiation damage (20 hearts per tick), constant vomiting |
