---
navigation:
  title: Ekranowanie
  parent: index.md
  position: 3
item_ids:
  - nuclear_radiation:rad_shielding_light
  - nuclear_radiation:rad_shielding_medium
  - nuclear_radiation:rad_shielding_heavy
  - nuclear_radiation:rad_shielding_dps
---

# Ekranowanie

Ekranowanie zmniejsza promieniowanie, które do Ciebie dociera. Istnieją dwa systemy: **ekranowanie blokami**
(bloki świata między Tobą a źródłem) i **ulepszenia ekranowania pancerza** (ulepszenia na stole kowalskim).

## Ekranowanie blokami

Promieniowanie ze źródeł w świecie jest tłumione przez bloki między Tobą a źródłem. Mod wykonuje
**raycast wokselowy** przez każdy blok wzdłuż linii i stosuje tłumienie Beer-Lamberta na metr.

Bloki są przypisane do jednego z czterech poziomów ekranowania:

| Poziom | Przykłady (zależnie od modpacka) | Tłumienie rentgenu | Tłumienie neutronów |
|---|---|---|---|
| **Lekki** | Lekkie materiały | 10%/m | 30%/m |
| **Średni** | Średnia gęstość | 30%/m | 25%/m |
| **Ciężki** | Gęste materiały (żelazo, złoto) | 60%/m | 45%/m |
| **Bardzo ciężki** | Bardzo gęste (ołów itd.) | 95%/m | 95%/m |

Kilka metrów ciężkiego lub bardzo ciężkiego ekranowania może zmniejszyć promieniowanie o rzędy wielkości. Buduj
grube ściany między sobą a silnymi źródłami.

Bloki z ekranowaniem pokazują **„Ekranowanie przed promieniowaniem"** w podpowiedzi przedmiotu.

## Ulepszenia ekranowania pancerza (kowalstwo)

Możesz dodać ekranowanie przeciwradiacyjne do **dowolnej części pancerza** (vanilla lub z modów) na **stole kowalskim**:

1. Umieść część pancerza w slocie **bazy**.
2. Umieść przedmiot ekranujący w slocie **dodatku**.

Dostępne są cztery poziomy ekranowania:

### Lekkie ekranowanie przed promieniowaniem

<ItemGrid>
  <ItemIcon id="rad_shielding_light" />
</ItemGrid>

<RecipeFor id="rad_shielding_light" />

- **Wartość ekranowania**: +2%

### Średnie ekranowanie przed promieniowaniem

<ItemGrid>
  <ItemIcon id="rad_shielding_medium" />
</ItemGrid>

<RecipeFor id="rad_shielding_medium" />

- **Wartość ekranowania**: +4%

### Ciężkie ekranowanie przed promieniowaniem

<ItemGrid>
  <ItemIcon id="rad_shielding_heavy" />
</ItemGrid>

<RecipeFor id="rad_shielding_heavy" />

- **Wartość ekranowania**: +7%

### Gęste ekranowanie przed promieniowaniem (DPS)

<ItemGrid>
  <ItemIcon id="rad_shielding_dps" />
</ItemGrid>

<RecipeFor id="rad_shielding_dps" />

- **Wartość ekranowania**: +12%
- Najsilniejsze dostępne ulepszenie ekranowania.

## Jak kumulują się ulepszenia

Ulepszenia ekranowania nakładają się na bazową ochronę pancerza. Zastosuj je do pełnego zestawu dla
maksymalnego efektu. Każda część pancerza może otrzymać jedno ulepszenie ekranowania, a efekty kumulują się
na wszystkich czterech slotach.

## Wskazówki

- **Łącz z kombinezonem ochronnym**: pełny [kombinezon ochronny](hazmats.md) z ekranowaniem DPS
  zapewnia najwyższą możliwą ochronę.
- **Nie zapomnij o ścianach z bloków**: nawet najlepszy pancerz nie zatrzyma promieniowania neutronowego z
  silnego źródła. Użyj bardzo ciężkiego ekranowania blokami między sobą a źródłem.
- **Sprawdzaj podpowiedzi**: części pancerza pokazują swoje procenty ochrony dla każdego kanału. Bloki ekranujące
  pokazują swój poziom.
