---
navigation:
  title: Combinaison anti-radiations
  parent: index.md
  position: 5
item_ids:
  - nuclear_radiation:hazmat_helmet
  - nuclear_radiation:hazmat_chestplate
  - nuclear_radiation:hazmat_leggings
  - nuclear_radiation:hazmat_boots
---

# Combinaison anti-radiations

Un ensemble d'armure complet conçu pour travailler dans des environnements radioactifs.

## Pièces d'armure

<ItemGrid>
  <ItemIcon id="hazmat_helmet" />
  <ItemIcon id="hazmat_chestplate" />
  <ItemIcon id="hazmat_leggings" />
  <ItemIcon id="hazmat_boots" />
</ItemGrid>

### Casque

<RecipeFor id="hazmat_helmet" />

- Le casque fournit également une **protection gazeuse**, bloquant les gaz radioactifs inhalés et les
  contaminants en suspension dans l'air.

### Combinaison

<RecipeFor id="hazmat_chestplate" />

### Jambières

<RecipeFor id="hazmat_leggings" />

### Bottes

<RecipeFor id="hazmat_boots" />

## Protection

La combinaison anti-radiations fournit une atténuation du rayonnement sur les quatre canaux (rayons X, alpha, bêta,
neutrons). Porter plusieurs pièces augmente la protection de manière multiplicative.

| Canal | Protégé | Notes |
|---|---|---|
| **Rayons X / Gamma** | Oui | Réduit par la densité du matériau de la combinaison |
| **Alpha** | Oui | Presque entièrement bloqué |
| **Bêta** | Oui | Significativement réduit |
| **Neutrons** | Oui | Partiellement réduit ; combiner avec un blindage de blocs pour les sources neutroniques puissantes |

Le **casque** fournit également une **protection gazeuse**, qui empêche :
- L'inhalation de nuages de gaz radioactif provenant de sources puissantes (>= 200 GBq)
- La pollution de vos poumons par des contaminants en suspension dans l'air présents dans votre inventaire

## Autres armures

Les armures vanilla en fer, or, maille et netherite ont également des valeurs de protection anti-radiation intégrées
(inférieures à celles de la combinaison). Consultez les infobulles des objets pour voir les pourcentages exacts par
canal.

## Amélioration avec du blindage

Vous pouvez également améliorer l'armure anti-radiations avec des [améliorations de blindage](shielding.md) sur une
table de forge. L'application de blindage anti-radiations dense (DPS) à un ensemble complet offre la protection
d'armure la plus élevée possible dans le mod.

## Conseils

- **Portez la combinaison complète** lors de la manipulation de matières radioactives. Même quelques secondes près
  d'une source puissante sans protection peuvent vous donner une dose dangereuse.
- **Portez toujours le casque** dans les zones avec des nuages de gaz radioactif - c'est la seule pièce qui
  offre une protection gazeuse.
- **Combinez avec des médicaments** : portez la combinaison pour une protection passive et transportez des
  [médicaments](medicine.md) pour un traitement d'urgence.
- **Vérifiez les infobulles** : chaque pièce d'armure affiche ses pourcentages de protection par canal.
