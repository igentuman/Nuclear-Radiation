---
navigation:
  title: Guide de la radiation nucléaire
  position: 1
---

# Guide de la radiation nucléaire

Un mod de radiation basé sur la physique pour Minecraft 1.21.1 (NeoForge). La radiation est modélisée avec des
unités réelles : **Bq** (activité de la source), **Gy** (dose absorbée) et **Sv** (dose biologique). Vous
accumulez une **dose cumulée** en Sieverts au fil du temps et subissez des effets de plus en plus graves à mesure
qu'elle augmente.

## Rubriques

<SubPages />

## Démarrage rapide : ne paniquez pas

1. **Fabriquez un <ItemLink id="geiger_counter" />** et gardez-le dans votre barre d'accès rapide. Il clique plus
   rapidement à mesure que la radiation augmente. Le HUD affiche votre débit de dose actuel en Sv/h et les impulsions
   par minute (CPM).
2. **Fabriquez un <ItemLink id="dosimeter" />** et portez-le n'importe où dans votre inventaire. Il ajoute une
   barre de dose à votre HUD affichant la dose totale accumulée en Sv et le débit actuel en Sv/h.
3. **Portez un <ItemLink id="hazmat_helmet" />** en entrant dans des zones contaminées. Une combinaison anti-radiations
   complète réduit considérablement tous les canaux de rayonnement (rayons X, alpha, bêta, neutrons).
4. **Éloignez les objets radioactifs** de vous lorsqu'ils ne sont pas nécessaires. Les objets dans votre inventaire vous
   irradiant aussi, pas seulement les blocs dans le monde.
5. **Portez des médicaments**. Les <ItemLink id="iodine_pill" /> et le <ItemLink id="prussian_blue" /> sont
   des contre-mesures peu coûteuses en début de partie. Le <ItemLink id="radaway" /> est le traitement le plus puissant.

## Comprendre la radiation

La radiation provient d'**isotopes radioactifs** assignés aux objets, blocs et fluides. Chaque isotope
émet une combinaison de quatre canaux :

| Canal | Bloqué par | Facteur de qualité | Notes |
|---|---|---|---|
| **Alpha** | Papier, cuir, quelques cm d'air | Q = 20 | Plus dangereux en cas d'inhalation/ingestion ; inoffensif à l'extérieur |
| **Bêta** | Plastique fin, tissu | Q = 1 | Pénétration modérée, dommages modérés |
| **Rayons X / Gamma** | Matériaux denses (plomb, béton, or) | Q = 1 | Pénètre la plupart des blocs ; danger externe principal |
| **Neutrons** | Matériaux riches en hydrogène (eau, plastique, béton) | Q = 10 | Très pénétrant, très dangereux ; seul un blindage lourd l'arrête |

Votre corps absorbe une **dose** mesurée en Sieverts (Sv). Le mod suit deux valeurs :

- **Sv/h (débit de dose)** - la vitesse à laquelle vous êtes irradié actuellement.
- **Total Sv (dose cumulée)** - l'exposition accumulée pendant la vie de votre personnage.

Les deux comptent : un débit de dose élevé déclenche des effets aigus, tandis qu'une dose cumulée élevée déclenche des
effets chroniques. La **pire** des deux détermine votre stade de dommage actuel.

## Stades de dose et effets

| Stade | Nom | Seuil Sv/h | Seuil dose cumul. Sv | Effets |
|---|---|---|---|---|
| 0 | Sûr | < 0,001 | < 0,5 | Aucun |
| 1 | Léger | >= 0,001 (1 mSv/h) | >= 0,5 Sv | Faiblesse, Malchance, Fatigue de minage, vomissements occasionnels |
| 2 | Modéré | >= 0,5 (500 mSv/h) | >= 2,0 Sv | Faiblesse II, Nausée, Fatigue de minage II, Lenteur, vomissements plus fréquents |
| 3 | Grave | >= 10 (10 Sv/h) | >= 5,0 Sv | Cécité, dégâts de radiation (1,5 cœurs par tick), vomissements fréquents |
| 4 | Létal | >= 100 (100 Sv/h) | >= 8,0 Sv | Flétrissure, dégâts massifs de radiation (20 cœurs par tick), vomissements constants |
