---
navigation:
  title: Blindage
  parent: index.md
  position: 3
item_ids:
  - nuclear_radiation:rad_shielding_light
  - nuclear_radiation:rad_shielding_medium
  - nuclear_radiation:rad_shielding_heavy
  - nuclear_radiation:rad_shielding_dps
---

# Blindage

Le blindage réduit la radiation qui vous atteint. Il existe deux systèmes : le **blindage de blocs** (blocs du monde
entre vous et une source) et les **améliorations de blindage d'armure** (améliorations sur une table de forge).

## Blindage de blocs

La radiation des sources dans le monde est atténuée par les blocs entre vous et la source. Le mod effectue un
**raycast voxel** à travers chaque bloc le long de la ligne et applique l'atténuation Beer-Lambert par mètre.

Les blocs sont classés dans l'un des quatre niveaux de blindage :

| Niveau | Exemples (varie selon le modpack) | Atténuation rayons X | Atténuation neutrons |
|---|---|---|---|
| **Léger** | Matériaux légers | 10%/m | 30%/m |
| **Moyen** | Densité moyenne | 30%/m | 25%/m |
| **Lourd** | Matériaux denses (fer, or) | 60%/m | 45%/m |
| **Extra lourd** | Très dense (plomb, etc.) | 95%/m | 95%/m |

Quelques mètres de blindage lourd ou extra lourd peuvent réduire la radiation de plusieurs ordres de grandeur.
Construisez des murs épais entre vous et les sources puissantes.

Les blocs avec blindage affichent **« Blindage anti-radiations »** dans leur infobulle.

## Améliorations de blindage d'armure (forge)

Vous pouvez ajouter un blindage anti-radiations à **n'importe quelle pièce d'armure** (vanilla ou moddée) sur une
**table de forge** :

1. Placez la pièce d'armure dans l'emplacement **base**.
2. Placez un objet de blindage dans l'emplacement **addition**.

Quatre niveaux de blindage sont disponibles :

### Blindage anti-radiations léger

<ItemGrid>
  <ItemIcon id="rad_shielding_light" />
</ItemGrid>

<RecipeFor id="rad_shielding_light" />

- **Valeur de blindage** : +2 %

### Blindage anti-radiations moyen

<ItemGrid>
  <ItemIcon id="rad_shielding_medium" />
</ItemGrid>

<RecipeFor id="rad_shielding_medium" />

- **Valeur de blindage** : +4 %

### Blindage anti-radiations lourd

<ItemGrid>
  <ItemIcon id="rad_shielding_heavy" />
</ItemGrid>

<RecipeFor id="rad_shielding_heavy" />

- **Valeur de blindage** : +7 %

### Blindage anti-radiations dense (DPS)

<ItemGrid>
  <ItemIcon id="rad_shielding_dps" />
</ItemGrid>

<RecipeFor id="rad_shielding_dps" />

- **Valeur de blindage** : +12 %
- L'amélioration de blindage la plus puissante disponible.

## Cumul des améliorations

Les améliorations de blindage s'ajoutent à la protection de base de l'armure. Appliquez-les à un ensemble complet
pour un bénéfice maximal. Chaque pièce d'armure peut recevoir une amélioration de blindage, et les effets se cumulent
sur les quatre emplacements.

## Conseils

- **Combinez avec l'armure anti-radiations** : une [combinaison anti-radiations](hazmats.md) complète avec blindage
  DPS offre la protection la plus élevée possible.
- **N'oubliez pas les murs de blocs** : même la meilleure armure n'arrêtera pas le rayonnement neutronique d'une
  source puissante. Utilisez un blindage de blocs extra lourd entre vous et la source.
- **Vérifiez les infobulles** : les pièces d'armure affichent leurs pourcentages de protection par canal. Les blocs
  de blindage affichent leur niveau.
