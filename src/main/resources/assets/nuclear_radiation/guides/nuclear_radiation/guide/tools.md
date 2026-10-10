---
navigation:
  title: Tools
  parent: index.md
  position: 2
item_ids:
  - nuclear_radiation:geiger_counter
  - nuclear_radiation:dosimeter
---

# Tools

Two essential tools help you monitor radiation exposure: the **Geiger Counter** for detecting
radiation in the environment, and the **Dosimeter** for tracking your accumulated dose.

## Geiger Counter

<ItemGrid>
  <ItemIcon id="geiger_counter" />
</ItemGrid>

<RecipeFor id="geiger_counter" />

- Hold it in your **main or off-hand** to activate.
- Emits audible **click sounds** that speed up and rise in pitch as dose rate increases.
- HUD shows current **dose rate (Sv/h)** and **CPM** (counts per minute).
- Silent below ~1 uSv/h (background level).
- The Geiger response is driven by dose rate (Sv/h), not raw field activity (Bq) - a large Bq field
  can yield a low dose, so the meter correctly reads benign levels.

### HUD Rate Color Coding

| Color | Dose rate range | Meaning |
|---|---|---|
| Green | < 0.1 uSv/h | Background - safe |
| Gray | 0.1 - 10 uSv/h | Minimal |
| Yellow | 10 uSv/h - 1 mSv/h | Low |
| Orange | 1 - 100 mSv/h | Medium - wear protection |
| Red | 100 mSv/h - 10 Sv/h | Elevated - leave the area |
| Dark Red | 10 - 100 Sv/h | High - lethal without heavy shielding |
| Magenta | >= 100 Sv/h | Extreme - death imminent |

## Dosimeter

<ItemGrid>
  <ItemIcon id="dosimeter" />
</ItemGrid>

<RecipeFor id="dosimeter" />

- Keep it **anywhere in your inventory** - no need to hold it.
- HUD shows a **dose bar** at the bottom-center of your screen:
  - **Total** accumulated Sv (text above bar)
  - **Rate** in Sv/h (text above bar, color-coded by severity)
  - Bar fills toward the acute-lethal reference (~50 Sv); color shifts green to yellow to orange to
    red.
- Right-click while holding it to get a chat message with your current total and rate.

## Tips

- **Always carry both.** The Geiger tells you *where* the danger is; the Dosimeter tells you *how
  much* you've taken.
- Keep the Geiger in your off-hand so you can hold tools or weapons in your main hand while
  exploring.
- Watch your dose bar: green is fine, yellow means slow down, orange means get out, red means you're
  in serious trouble.
