---
navigation:
  title: Outils
  parent: index.md
  position: 2
item_ids:
  - nuclear_radiation:geiger_counter
  - nuclear_radiation:dosimeter
---

# Outils

Deux outils essentiels vous aident à surveiller l'exposition aux radiations : le **compteur Geiger** pour détecter
la radiation dans l'environnement, et le **dosimètre** pour suivre votre dose accumulée.

## Compteur Geiger

<ItemGrid>
  <ItemIcon id="geiger_counter" />
</ItemGrid>

<RecipeFor id="geiger_counter" />

- Tenez-le dans votre **main principale ou secondaire** pour l'activer.
- Émet des **clics audibles** qui s'accélèrent et montent en ton à mesure que le débit de dose augmente.
- Le HUD affiche le **débit de dose actuel (Sv/h)** et les **CPM** (impulsions par minute).
- Silencieux en dessous de ~1 µSv/h (niveau de fond).
- La réponse du compteur est basée sur le débit de dose (Sv/h), pas sur l'activité brute du champ (Bq) - un champ
  à forte activité en Bq peut donner une faible dose, et le compteur indique alors correctement un niveau bénin.

### Code couleur du débit sur le HUD

| Couleur | Plage de débit de dose | Signification |
|---|---|---|
| Vert | < 0,1 µSv/h | Fond - sûr |
| Gris | 0,1 – 10 µSv/h | Minimal |
| Jaune | 10 µSv/h – 1 mSv/h | Faible |
| Orange | 1 – 100 mSv/h | Moyen - portez une protection |
| Rouge | 100 mSv/h – 10 Sv/h | Élevé - quittez la zone |
| Rouge foncé | 10 – 100 Sv/h | Haut - mortel sans blindage lourd |
| Magenta | >= 100 Sv/h | Extrême - mort imminente |

## Dosimètre

<ItemGrid>
  <ItemIcon id="dosimeter" />
</ItemGrid>

<RecipeFor id="dosimeter" />

- Gardez-le **n'importe où dans votre inventaire** - pas besoin de le tenir en main.
- Le HUD affiche une **barre de dose** au bas-centre de l'écran :
  - **Total** de Sv accumulé (texte au-dessus de la barre)
  - **Débit** en Sv/h (texte au-dessus de la barre, couleur selon la gravité)
  - La barre se remplit vers la référence de dose létale aiguë (~50 Sv) ; la couleur passe du vert au jaune,
    puis à l'orange et au rouge.
- Clic droit en le tenant pour recevoir un message dans le chat avec votre total et débit actuels.

## Conseils

- **Portez toujours les deux.** Le compteur Geiger vous dit *où* se trouve le danger ; le dosimètre vous dit *combien*
  vous avez reçu.
- Gardez le compteur Geiger dans votre main secondaire pour pouvoir tenir des outils ou des armes dans votre main
  principale pendant l'exploration.
- Surveillez votre barre de dose : vert = ça va, jaune = ralentissez, orange = partez, rouge = vous êtes en
  sérieux danger.
