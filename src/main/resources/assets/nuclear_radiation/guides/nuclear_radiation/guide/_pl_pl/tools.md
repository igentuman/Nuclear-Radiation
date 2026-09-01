---
navigation:
  title: Narzędzia
  parent: index.md
  position: 2
item_ids:
  - nuclear_radiation:geiger_counter
  - nuclear_radiation:dosimeter
---

# Narzędzia

Dwa niezbędne narzędzia pomagają monitorować ekspozycję na promieniowanie: **licznik Geigera** do wykrywania
promieniowania w otoczeniu i **dozymetr** do śledzenia skumulowanej dawki.

## Licznik Geigera

<ItemGrid>
  <ItemIcon id="geiger_counter" />
</ItemGrid>

<RecipeFor id="geiger_counter" />

- Trzymaj w **głównej lub drugiej ręce** aby aktywować.
- Emituje słyszalne **kliknięcia**, które przyspieszają i rosną tonem w miarę wzrostu mocy dawki.
- HUD pokazuje aktualną **moc dawki (Sv/h)** i **CPM** (impulsów na minutę).
- Cichy poniżej ~1 µSv/h (poziom tła).
- Reakcja licznika zależy od mocy dawki (Sv/h), a nie od czystej aktywności pola (Bq) - duże pole w Bq
  może dawać niską dawkę, i licznik poprawnie wskazuje poziom bezpieczny.

### Kod kolorów mocy dawki na HUD

| Kolor | Zakres mocy dawki | Znaczenie |
|---|---|---|
| Zielony | < 0,1 µSv/h | Tło - bezpiecznie |
| Szary | 0,1 – 10 µSv/h | Minimalny |
| Żółty | 10 µSv/h – 1 mSv/h | Niski |
| Pomarańczowy | 1 – 100 mSv/h | Średni - załóż ochronę |
| Czerwony | 100 mSv/h – 10 Sv/h | Podwyższony - opuść strefę |
| Ciemnoczerwony | 10 – 100 Sv/h | Wysoki - śmiertelny bez ciężkiej osłony |
| Magenta | >= 100 Sv/h | Ekstremalny - śmierć nieunikniona |

## Dozymetr

<ItemGrid>
  <ItemIcon id="dosimeter" />
</ItemGrid>

<RecipeFor id="dosimeter" />

- Noś **gdziekolwiek w ekwipunku** - nie trzeba trzymać w ręce.
- HUD pokazuje **pasek dawki** na dole na środku ekranu:
  - **Łącznie** skumulowane Sv (tekst nad paskiem)
  - **Moc** w Sv/h (tekst nad paskiem, kolor zależny od nasilenia)
  - Pasek wypełnia się do referencyjnego poziomu ostrej dawki śmiertelnej (~50 Sv); kolor zmienia się od zielonego
    do żółtego, pomarańczowego i czerwonego.
- Kliknij PPM trzymając dozymetr, aby otrzymać wiadomość na czacie z aktualnym łącznym poziomem i mocą dawki.

## Wskazówki

- **Zawsze noś oba.** Licznik Geigera mówi *gdzie* jest niebezpieczeństwo; dozymetr mówi *ile* przyjąłeś.
- Trzymaj licznik Geigera w drugiej ręce, aby w głównej móc trzymać narzędzia lub broń podczas eksploracji.
- Obserwuj pasek dawki: zielony = w porządku, żółty = zwolnij, pomarańczowy = wynoś się, czerwony = jesteś w
   poważnych kłopotach.
