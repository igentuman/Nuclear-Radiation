---
navigation:
  title: Przewodnik po promieniowaniu jądrowym
  position: 1
---

# Przewodnik po promieniowaniu jądrowym

Mod oparty na fizyce promieniowania dla Minecraft 1.21.1 (NeoForge). Promieniowanie jest modelowane z użyciem
rzeczywistych jednostek: **Bq** (aktywność źródła), **Gy** (dawka pochłonięta) i **Sv** (dawka biologiczna).
Akumulujesz **dawkę skumulowaną** w Siwertach w czasie i doświadczasz narastających efektów w miarę jej wzrostu.

## Tematy

<SubPages />

## Szybki start: nie panikuj

1. **Wykonaj <ItemLink id="geiger_counter" />** i trzymaj go w pasku szybkiego dostępu. Klika szybciej w miarę
   wzrostu promieniowania. HUD pokazuje aktualną moc dawki w Sv/h i liczbę impulsów na minutę (CPM).
2. **Wykonaj <ItemLink id="dosimeter" />** i noś go gdziekolwiek w ekwipunku. Dodaje pasek dawki do HUD,
   pokazujący łączną skumulowaną dawkę w Sv i aktualną moc w Sv/h.
3. **Noś <ItemLink id="hazmat_helmet" />** wchodząc do skażonych stref. Pełny kombinezon ochronny
   znacznie redukuje wszystkie kanały promieniowania (rentgenowskie, alfa, beta, neutrony).
4. **Trzymaj przedmioty radioaktywne z dala** od siebie, gdy nie są potrzebne. Przedmioty w ekwipunku również
   Cię napromieniują, nie tylko bloki w świecie.
5. **Noś leki**. <ItemLink id="iodine_pill" /> i <ItemLink id="prussian_blue" /> to
   tanie środki zaradcze na wczesnym etapie gry. <ItemLink id="radaway" /> to najsilniejsze leczenie.

## Zrozumienie promieniowania

Promieniowanie pochodzi od **izotopów promieniotwórczych** przypisanych do przedmiotów, bloków i płynów. Każdy izotop
emituje pewną kombinację czterech kanałów:

| Kanał | Zablokowany przez | Współczynnik jakości | Uwagi |
|---|---|---|---|
| **Alfa** | Papier, skóra, kilka cm powietrza | Q = 20 | Najbardziej szkodliwy przy wdychaniu/połykaniu; nieszkodliwy zewnętrznie |
| **Beta** | Cienki plastik, tkanina | Q = 1 | Umiarkowana penetracja, umiarkowane uszkodzenia |
| **Rentgen / Gamma** | Gęste materiały (ołów, beton, złoto) | Q = 1 | Przenika większość bloków; główne zagrożenie zewnętrzne |
| **Neutrony** | Materiały bogate w wodór (woda, plastik, beton) | Q = 10 | Wysoce przenikliwe, bardzo szkodliwe; zatrzymuje je tylko ciężka osłona |

Twoje ciało pochłania **dawkę** mierzoną w Siwertach (Sv). Mod śledzi dwie wartości:

- **Sv/h (moc dawki)** - jak szybko jesteś napromieniowywany w tej chwili.
- **Razem Sv (dawka skumulowana)** - skumulowana ekspozycja przez całe życie postaci.

Obie mają znaczenie: wysoka moc dawki wywołuje efekty ostre, a wysoka dawka skumulowana wywołuje efekty przewlekłe.
**Gorsza** z tych dwóch określa Twój aktualny stopień szkodliwości.

## Stopnie dawki i efekty

| Stopień | Nazwa | Próg Sv/h | Próg dawki skumul. Sv | Efekty |
|---|---|---|---|---|
| 0 | Bezpiecznie | < 0,001 | < 0,5 | Brak |
| 1 | Łagodny | >= 0,001 (1 mSv/h) | >= 0,5 Sv | Osłabienie, Pech, Zmęczenie przy wydobywaniu, sporadyczne wymioty |
| 2 | Umiarkowany | >= 0,5 (500 mSv/h) | >= 2,0 Sv | Osłabienie II, Nudności, Zmęczenie przy wydobywaniu II, Powolność, częstsze wymioty |
| 3 | Ciężki | >= 10 (10 Sv/h) | >= 5,0 Sv | Ślepota, obrażenia od promieniowania (1,5 serca na tick), częste wymioty |
| 4 | Śmiertelny | >= 100 (100 Sv/h) | >= 8,0 Sv | Uschnięcie, masywne obrażenia od promieniowania (20 serc na tick), ciągłe wymioty |
