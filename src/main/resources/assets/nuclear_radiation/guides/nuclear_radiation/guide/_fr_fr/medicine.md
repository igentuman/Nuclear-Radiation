---
navigation:
  title: Médicaments et traitement
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

# Médicaments et traitement

Le syndrome d'irradiation aiguë est traitable. Différents médicaments traitent différents aspects de la contamination :
élimination instantanée de la dose, protection continue, purge des isotopes internes et nettoyage de la pollution pulmonaire.

## Médicaments disponibles

### Comprimé d'iode

<ItemGrid>
  <ItemIcon id="iodine_pill" />
</ItemGrid>

<RecipeFor id="iodine_pill" />

- **Effet instantané** : Retire immédiatement **0,5 Sv** de votre dose cumulée.
- **Accorde une blocage à l'iode** pendant 30 secondes - draine spécifiquement la contamination interne en I-131.
- **Accorde également** une protection anti-radiations et une purge.
- **Contre-mesure peu coûteuse** en début de partie.

### Bleu de Prusse

<ItemGrid>
  <ItemIcon id="prussian_blue" />
</ItemGrid>

<RecipeFor id="prussian_blue" />

- **Effet instantané** : Retire immédiatement **1,0 Sv** de votre dose cumulée.
- **Accorde une purge du césium** pendant 2 minutes - accélère l'élimination du Cs-137.
- **Accorde également** une protection anti-radiations et une purge (amplificateur 1).

### Potion de protection anti-radiations

<ItemGrid>
  <ItemIcon id="rad_protection_potion" />
</ItemGrid>

<RecipeFor id="rad_protection_potion" />

- **Aucune élimination instantanée de Sv**.
- **Accorde une protection anti-radiations** pendant 5 minutes - réduit la dose reçue jusqu'à 95 %.

### Potion de protection anti-radiations II

<ItemGrid>
  <ItemIcon id="rad_protection_potion_2" />
</ItemGrid>

<RecipeFor id="rad_protection_potion_2" />

- **Aucune élimination instantanée de Sv**.
- **Accorde une protection anti-radiations renforcée** pendant 10 minutes (amplificateur 1).

### Injection anti-radiations

<ItemGrid>
  <ItemIcon id="anti_rad_injection" />
</ItemGrid>

<RecipeFor id="anti_rad_injection" />

- **Aucune élimination instantanée de Sv**.
- **Accorde une purge extrêmement puissante** pendant 5 minutes (amplificateur 21).
- Élimine rapidement la contamination interne et la pollution pulmonaire.

### Radaway

<ItemGrid>
  <ItemIcon id="radaway" />
</ItemGrid>

<RecipeFor id="radaway" />

- **Effet instantané** : Retire immédiatement **1,0 Sv** de votre dose cumulée.
- **Accorde une protection anti-radiations et une purge** pendant 5 minutes.
- Le traitement global le plus puissant.

## Fonctionnement des effets

- **Protection anti-radiations** - réduit la dose reçue jusqu'à 95 % (varie avec l'amplificateur). Vous accumulez
  toujours de la dose, mais beaucoup plus lentement.
- **Purge des radiations** - augmente la vitesse à laquelle votre dose cumulée diminue et vos poumons se rétablissent.
  Amplificateur plus élevé = récupération plus rapide.
- **Blocage à l'iode** - draine spécifiquement la contamination interne en I-131.
- **Purge du césium** - draine spécifiquement la contamination interne en Cs-137.

## Utilisation recommandée

- **Avant d'entrer dans une zone chaude** : buvez une <ItemLink id="rad_protection_potion" /> pour 5 minutes de
  dose réduite.
- **Après exposition avec contamination en I-131** : prenez des <ItemLink id="iodine_pill" />.
- **Après exposition avec contamination en Cs-137** : prenez du <ItemLink id="prussian_blue" />.
- **Contamination grave / dose cumulée élevée** : utilisez du <ItemLink id="radaway" /> ou une
  <ItemLink id="anti_rad_injection" /> pour un nettoyage rapide.
- **Portez-en toujours quelques-uns** - la radiation peut provenir de sources inattendues.
