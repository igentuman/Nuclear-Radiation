---
navigation:
  title: Leki i leczenie
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

# Leki i leczenie

Choroba popromienna jest uleczalna. Różne leki różnią się działaniem na skażenie:
natychmiastowe usunięcie dawki, bieżącą ochronę, usuwanie izotopów wewnętrznych i oczyszczanie płuc.

## Dostępne leki

### Tabletka jodowa

<ItemGrid>
  <ItemIcon id="iodine_pill" />
</ItemGrid>

<RecipeFor id="iodine_pill" />

- **Natychmiastowy efekt**: Natychmiast usuwa **0,5 Sv** z dawki skumulowanej.
- **Nadaje blokadę jodową** na 30 sekund - specyficznie usuwa wewnętrzne skażenie I-131.
- **Nadaje również** ochronę przed promieniowaniem i usuwanie promieniowania.
- **Tani środek** na wczesnym etapie gry.

### Błękit pruski

<ItemGrid>
  <ItemIcon id="prussian_blue" />
</ItemGrid>

<RecipeFor id="prussian_blue" />

- **Natychmiastowy efekt**: Natychmiast usuwa **1,0 Sv** z dawki skumulowanej.
- **Nadaje usuwanie cezu** na 2 minuty - przyspiesza usuwanie Cs-137.
- **Nadaje również** ochronę przed promieniowaniem i usuwanie promieniowania (wzmacniacz 1).

### Mikstura ochrony przed promieniowaniem

<ItemGrid>
  <ItemIcon id="rad_protection_potion" />
</ItemGrid>

<RecipeFor id="rad_protection_potion" />

- **Brak natychmiastowego usunięcia Sv**.
- **Nadaje ochronę przed promieniowaniem** na 5 minut - redukuje przyjmowaną dawkę do 95%.

### Mikstura ochrony przed promieniowaniem II

<ItemGrid>
  <ItemIcon id="rad_protection_potion_2" />
</ItemGrid>

<RecipeFor id="rad_protection_potion_2" />

- **Brak natychmiastowego usunięcia Sv**.
- **Nadaje wzmocnioną ochronę przed promieniowaniem** na 10 minut (wzmacniacz 1).

### Zastrzyk antyradiacyjny

<ItemGrid>
  <ItemIcon id="anti_rad_injection" />
</ItemGrid>

<RecipeFor id="anti_rad_injection" />

- **Brak natychmiastowego usunięcia Sv**.
- **Nadaje niezwykle silne usuwanie promieniowania** na 5 minut (wzmacniacz 21).
- Szybko oczyszcza skażenie wewnętrzne i zanieczyszczenie płuc.

### Radaway

<ItemGrid>
  <ItemIcon id="radaway" />
</ItemGrid>

<RecipeFor id="radaway" />

- **Natychmiastowy efekt**: Natychmiast usuwa **1,0 Sv** z dawki skumulowanej.
- **Nadaje ochronę przed promieniowaniem i usuwanie promieniowania** na 5 minut.
- Najsilniejsze uniwersalne leczenie.

## Jak działają efekty

- **Ochrona przed promieniowaniem** - redukuje przyjmowaną dawkę do 95% (skaluje się ze wzmacniaczem). Nadal
  akumulujesz dawkę, ale znacznie wolniej.
- **Usuwanie promieniowania** - zwiększa szybkość rozpadu dawki skumulowanej i odzyskiwania płuc.
  Wyższy wzmacniacz = szybsze odzyskiwanie.
- **Blokada jodowa** - specyficznie usuwa wewnętrzne skażenie I-131.
- **Usuwanie cezu** - specyficznie usuwa wewnętrzne skażenie Cs-137.

## Zalecane użycie

- **Przed wejściem do gorącej strefy**: wypij <ItemLink id="rad_protection_potion" /> na 5 minut
  zmniejszonej dawki.
- **Po ekspozycji ze skażeniem I-131**: weź <ItemLink id="iodine_pill" />.
- **Po ekspozycji ze skażeniem Cs-137**: weź <ItemLink id="prussian_blue" />.
- **Ciężkie skażenie / wysoka dawka skumulowana**: użyj <ItemLink id="radaway" /> lub
  <ItemLink id="anti_rad_injection" /> do szybkiego oczyszczenia.
- **Zawsze noś kilka sztuk** - promieniowanie może pochodzić z nieoczekiwanych źródeł.
