# Hero's Journey — Arc "Batman : Origine"

Mod NeoForge pour Minecraft **1.21.1** (Java 21). Premier arc d'un mod de super-héros à
questlines narratives : le joueur incarne **Batman** à travers une progression en 7 quêtes basée
sur les comics, **sans structure custom et sans PNJ parlant** - uniquement des mécaniques, mobs et
objets vanilla.

## ⚠️ Important : ce dépôt est développé sans accès à `gradle build` côté agent

Cet environnement de développement a une politique réseau qui bloque `maven.neoforged.net` (seul
Maven Central est joignable), donc **`./gradlew build` ne peut être exécuté et vérifié que chez
toi**, jamais ici. Merci de recompiler et de me remonter les erreurs le cas échéant.

## Ce round : corrections et refonte de contenu

Ce round n'ajoute pas de nouvelle quête mais corrige plusieurs mécaniques signalées comme cassées,
renomme le héros et les quêtes, et remplace entièrement les mini-jeux du carnet d'énigmes. Détail
complet ci-dessous par thème.

### 1. Manoir Wayne : entièrement supprimé

Le Manoir Wayne + son cimetière n'étaient déjà plus utilisés par aucune quête depuis le round
précédent (gardés "en l'état, décoratifs"). Ce round les supprime **complètement**, plus aucune
quête n'en dépend :
- `structure/wayne/*` (WayneManorPiece, WayneCemeteryPiece, WayneManorStructure),
  `structure/HJStructures.java`, `structure/HJStructurePieceTypes.java` - le package
  `com.heroesjourney.structure` n'existe plus du tout.
- `content/batman/WayneManorProtection.java` et sa config (`WAYNE_MANOR_PROTECTION_ENABLED`/
  `_RADIUS`), le flag `FLAG_WAYNE_MANOR_UNLOCKED`.
- `item/HeroGraveBlock.java` et les deux blocs/items `wayne_grave_thomas`/`_martha` (plus leurs
  assets : blockstates, modèles, textures).
- Les gabarits NBT (`data/heroesjourney/structure/*.nbt`) et tout le datapack associé
  (`worldgen/structure`, `worldgen/structure_set`, `tags/worldgen/structure` - ces trois dossiers
  n'existent plus, ils ne contenaient que Wayne Manor).

Le mod n'a donc plus aucune structure custom ni aucun bloc décoratif propre à un lieu.

### 2. Renommage "Batman (Nolan)" → "Batman"

- `BatmanAbilities.HERO_ID` : `"batman_nolan"` → `"batman"` (donc `/heroesjourney progress <joueur>
  batman <étape>`, `/heroesjourney activate <joueur> batman`, etc. - voir la section commandes).
- Clé de traduction `hero.heroesjourney.batman_nolan` → `hero.heroesjourney.batman`, nom affiché
  simplifié en "Batman".
- Texture d'icône renommée `textures/gui/hero_batman_nolan.png` → `hero_batman.png`.
- `ArmorEffectsHandler` référence désormais `BatmanAbilities.HERO_ID` au lieu d'une constante
  dupliquée en dur (`"batman_nolan"`) - ça évite un risque de désynchronisation si l'id change à
  nouveau un jour.

### 3-4. Renommage des quêtes + texte narratif

Les 7 étapes ont des noms plus évocateurs et un texte narratif (au lieu d'un simple résumé
d'objectif) affiché dans le menu de progression, sous le titre de l'étape en cours - voir
`HeroDetailScreen`, qui ne rendait auparavant jamais `QuestStage#description()`.

| # | Ancien nom | Nouveau nom |
|---|---|---|
| 1 | La mort des parents | **Le Serment** |
| 2 | Espionnage | **Dans l'ombre** |
| 3 | Physique | **Corps et discipline** |
| 4 | Esprit | **Esprit** (inchangé) |
| 5 | Arts martiaux | **Le Combattant** |
| 6 | La chauve-souris | **Un signe dans la nuit** |
| 7 | Devenir Batman | **Naissance d'une légende** |

Les identifiants internes des étapes (`origin_witness`, `espionage`, etc., utilisés pour stocker la
progression) n'ont pas changé - seuls les noms/textes affichés au joueur ont bougé.

### 5. Compteur course/saut/nage corrigé

Le suivi de la distance de sprint était fait "à la main" (delta de position par tick, avec
plusieurs conditions comme `onGround()`/`isSprinting()` pouvant rater des transitions) - remplacé
par le stat vanilla `Stats.SPRINT_ONE_CM`, exactement comme les sauts (`Stats.JUMP`) et la nage
(`Stats.SWIM_ONE_CM`) l'étaient déjà. Les trois compteurs utilisent maintenant le même mécanisme
(`QuestManager#trackVanillaStatDelta`) : on lit le stat vanilla à chaque tick et on ne propage que
la différence depuis la dernière lecture, donc aucun risque de rater ou de compter deux fois un
mouvement. **Les stats vanilla de distance sont stockées en centimètres** - les deltas sont divisés
par 100 pour retrouver des blocs avant d'être comparés aux seuils de config (en blocs).

### 6. Nouveaux mini-jeux du carnet d'énigmes

Les 3 anciens mini-jeux (mémoire de couleurs, énigmes à choix multiples, chiffrement de lettres)
sont entièrement remplacés par 3 nouveaux, indépendants de toute langue et à thème Batman. Toujours
tirés au hasard à chaque utilisation du carnet.

- **Coffre-fort (Mastermind)** : le jeu choisit une combinaison secrète de symboles colorés
  (longueur et palette configurables, 4 symboles parmi 6 couleurs par défaut). Le joueur compose
  une proposition en cliquant les pastilles de couleur, valide, et reçoit un retour : nombre de
  symboles bien placés / présents mais mal placés (mécanique Mastermind classique). Tentatives
  illimitées, historique des essais affiché.
- **Reconstituer la preuve (taquin)** : une image (texture d'indice, 48x48, découpée en 3x3 tuiles
  de 16x16) est mélangée ; cliquer deux tuiles les échange. Contrairement à un vrai "15-puzzle" à
  glissement, les échanges sont libres (pas de case vide obligatoire) - ce qui évite le piège
  classique où seule une permutation sur deux du 15-puzzle est réellement résolvable ; ici toute
  disposition mélangée peut être ramenée à l'ordre d'origine.
- **Crochetage** : un curseur oscille sur une barre ; une zone verte (cible) doit être cliquée au
  bon moment. La zone se rétrécit à chaque goupille réussie (3 goupilles par défaut, largeur
  30% → 10% de la barre) pour augmenter la difficulté. Position de la zone tirée au hasard
  côté client à chaque goupille (rien à cacher niveau serveur ici, contrairement au code secret du
  coffre-fort ou à la disposition de la preuve).

### 7. Cape : chute lente enfin fonctionnelle

L'ancienne implémentation ajustait manuellement la vélocité verticale (`deltaMovement.y`) par petits
pas chaque tick - en pratique, la gravité vanilla est ré-appliquée dans le même sens juste avant que
ce code ne s'exécute, donc le "planeur" luttait en permanence contre sa propre gravité et le
ralentissement perçu était minime, voire imperceptible. Remplacé par l'effet vanilla **Slow
Falling** (chute lente) lui-même, ré-appliqué chaque tick tant que le joueur est accroupi et en
train de tomber avec le plastron caparaçonné porté - un mécanisme vanilla, testé par Mojang, ne
"lutte" pas contre la gravité de la même façon puisqu'il modifie directement la constante de
gravité appliquée. La dérive horizontale vers le regard du joueur (`GLIDE_HORIZONTAL_SPEED`) est
conservée par-dessus.

### 8. Grappin : se déclenche enfin visuellement

Le grappin ne faisait rien du tout quand un bloc se trouvait à courte portée (~4-6 blocs) dans la
ligne de mire - ce qui est le cas typique quand on teste un grappin près d'un mur ! En cause :
Minecraft n'appelle `Item#use()` (la méthode que `GrappleHookItem` implémentait seule) que quand
**rien** ne se trouve à portée d'interaction normale ; s'il y a un bloc à courte portée, c'est
`Item#useOn(UseOnContext)` qui est appelé à la place, et cette méthode n'était pas surchargée donc
ne faisait rien. Le grappin implémente maintenant les deux méthodes (courte portée via
`useOn`, longue portée via `use` avec le rayon configuré) pour se déclencher dans tous les cas.

### 9. Batarangs : comportement flèche, plus de retour automatique

Changement de design délibéré (pas un bug) : les batarangs ne reviennent plus tout seuls vers le
lanceur façon boomerang. S'ils ne touchent rien (ou après avoir touché un mob), ils restent au sol
et sont ramassables en marchant dessus (`Entity#playerTouch`, le même mécanisme que les flèches
vanilla), avec une limite de durée de vie (5 minutes) au cas où personne ne les ramasse.

### 10. Fumigènes : aveuglent tout le monde, lanceur compris

Le nuage ne blindait que les `Mob` (donc jamais un joueur, pas même celui qui l'a lancé). Corrigé
pour cibler toute `LivingEntity` dans le rayon - joueurs compris - et ne retirer la cible
(`setTarget(null)`) que pour les entités qui sont effectivement des `Mob`.

### 11. Vision nocturne (et autres effets à recharge courte) : ne clignotent plus

La cagoule (et, dans une moindre mesure, les bottes et les passifs course/nage) ré-appliquaient un
effet de potion avec une durée à peine plus longue que l'intervalle de rafraîchissement (~1s) - ce
qui faisait passer la durée restante sous le seuil d'avertissement "bientôt expiré" de Minecraft
juste avant chaque rafraîchissement, provoquant un clignotement continu (le plus visible sur la
Vision nocturne, qui affecte directement la luminosité de l'écran). Toutes les durées rafraîchies
sont désormais bien au-dessus de ce seuil (600 ticks / 30s, toujours réappliquées avant expiration),
donc plus aucun clignotement.

## Installation

1. Compiler (`./gradlew build`) ou récupérer le `.jar`.
2. Installer NeoForge `21.1.176` pour Minecraft 1.21.1.
3. Copier le `.jar` dans le dossier `mods/` de l'instance.

## Touches

| Touche par défaut | Action |
|---|---|
| `K` | Ouvre le menu Héros (roster + détail de la questline) |
| `V` | Utilise la capacité active du héros (lueur des coffres, une fois débloquée) |

Les deux touches sont reconfigurables dans **Options > Contrôles > Hero's Journey**.

## Commande de debug admin

```
/heroesjourney progress <joueur> <hero> <étape>     # force l'étape courante (0-indexé)
/heroesjourney activate <joueur> <hero>              # active un héros instantanément
/heroesjourney unlockability <joueur> <ability>      # débloque une capacité (ex: chest_glow)
/heroesjourney listheroes                            # liste les héros enregistrés
```
`hero` = `batman` pour cet arc (anciennement `batman_nolan`).

## Configuration

Toutes les valeurs d'équilibrage (rayons, distances, compteurs, cooldowns, bonus passifs, réglages
des 3 mini-jeux, etc.) sont dans `config/heroesjourney-common.toml`, généré au premier lancement.

---

## Déroulé de la questline, quête par quête

### 1. Le Serment
Trouve un village généré naturellement, laisse un mob hostile ou neutre tuer un villageois pendant
que tu es à proximité (`ORIGIN_VILLAGER_WITNESS_RADIUS`, 18 blocs par défaut) - la quête démarre
automatiquement, aucune action volontaire du joueur n'est requise pour ce déclencheur.

### 2. Dans l'ombre - comment fonctionne la détection
Accroupis-toi (`Shift`) à moins de `STEALTH_APPROACH_RADIUS` blocs (6 par défaut) d'un mob hostile,
et maintiens la position `STEALTH_APPROACH_CONSECUTIVE_SECONDS` secondes d'affilée (3 par défaut,
vérifié une fois par seconde) sans que le mob ne te cible. Il n'y a **pas de mécanique de
détection custom** : la condition s'appuie directement sur le système de ciblage IA de Minecraft.
- Un mob hostile ne cible le joueur que s'il peut le "voir" : ligne de vue dégagée + portée de
  détection propre à chaque type de mob.
- **S'accroupir réduit nativement cette portée de détection dans Minecraft** (mécanique vanilla
  déjà existante, pas ajoutée par ce mod) - c'est ce qui rend l'approche furtive possible.
- La condition vérifie simplement `mob.getTarget() != joueur` : si l'IA vanilla n'a pas choisi le
  joueur comme cible (parce qu'elle ne l'a pas "vu"), l'approche compte.
- Chaque mob ne compte qu'une fois : un tag est posé dans ses données persistantes dès que
  l'approche est validée, empêchant de refarmer le même mob immobile en boucle.
15 approches distinctes requises par défaut (`STEALTH_APPROACH_COUNT`).

### 3. Corps et discipline
Course, saut et nage progressent **en parallèle** (3 objectifs indépendants dans la même étape) :
3000 blocs en sprint, 500 sauts, 500 blocs à la nage par défaut. Chaque sous-catégorie débloque
son propre passif dès qu'elle est terminée individuellement (pas besoin d'attendre les deux
autres) grâce à `RewardOnCompleteCondition`. Une fois les trois faites, le **carnet d'énigmes**
est ajouté automatiquement à l'inventaire (ou lâché au sol si l'inventaire est plein).

### 4. Esprit
Clique-droit avec le carnet d'énigmes pour lancer un des 3 mini-jeux au hasard (coffre-fort,
reconstitution de preuve, crochetage - voir plus haut). Résous-en 10 (`PUZZLE_TARGET_COUNT`) pour
débloquer la capacité "lueur des coffres" (touche `V`).

### 5. Le Combattant
Élimine 40 mobs hostiles (`MARTIAL_ARTS_KILLS`) dont le coup fatal est porté à mains nues (aucun
item en main).

### 6. Un signe dans la nuit
Laisse une chauve-souris (`minecraft:bat`) passer à moins de `BAT_PROXIMITY_RADIUS` blocs (3 par
défaut) - n'importe où, aucun lieu particulier requis. Débloque les recettes de l'armure.

### 7. Naissance d'une légende
Crafte et porte simultanément la cagoule, le plastron caparaçonné (assemblé depuis le plastron de
combat + la cape), les jambières et les bottes. Débloque les recettes des gadgets et donne un kit
de départ (1 grappin, 8 batarangs, 3 fumigènes).

## Effets et bonus, avec leurs valeurs précises

Tous actifs uniquement quand Batman est le héros actif du joueur. Les valeurs entre parenthèses
sont les valeurs par défaut de config (`config/heroesjourney-common.toml`).

| Source | Effet | Valeur par défaut |
|---|---|---|
| Quête 2 (passif) | Chance qu'un mob hostile échoue à cibler le joueur, à chaque tentative | 30% (`1 - mobDetectionRangeMultiplier`, soit `1 - 0.7`) |
| Quête 3a (passif) | Vitesse de déplacement (`Speed`) permanente | Niveau I (+20%, `runSpeedAmplifier = 0`) |
| Quête 3b (passif) | Réduction des dégâts de chute, additionnelle à celle des jambières | 30% (`jumpFallDamageReduction`), ~51% cumulé avec les jambières |
| Quête 3c (passif) | Vitesse de nage (`Dauphin's Grace`) permanente | Niveau I |
| Quête 4 (capacité, touche `V`) | Révèle les coffres par pulsation de particules | Rayon 20 blocs, 5s de durée, 30s de cooldown |
| Quête 5 (passif) | Bonus de dégâts à mains nues | +2.0 (1 cœur) |
| Quête 5 (passif) | Bonus de vitesse d'attaque à mains nues | +1.0 (attribut brut, base vanilla 4.0, soit environ +25%) |
| Cagoule (intrinsèque) | Vision nocturne permanente tant que portée | - |
| Bottes (intrinsèque) | Vitesse de déplacement | Niveau I (+20%) |
| Jambières (intrinsèque) | Réduction des dégâts de chute | 30% (`fallDamageReduction`) |
| Plastron caparaçonné (intrinsèque) | Plané : chute lente (vanilla) + dérive horizontale vers le regard en sneak+chute | Vitesse horizontale 0.12 bloc/tick |
| Grappin | Portée / cooldown / vitesse de traction max / accélération | 24 blocs / 2s / 1.6 bloc-tick / 0.18 par tick |
| Batarang | Dégâts / ralentissement (Lenteur II) infligé à la cible touchée | 3.0 / 3s |
| Fumigène | Rayon du nuage / durée / aveuglement (Cécité II, pulsé toutes les 5 ticks) | 6 blocs / 8s / 2s par pulsation |

## Comment obtenir/crafter chaque objet

Toutes les recettes ci-dessous sont des recettes **sans forme** (`crafting_shapeless`, n'importe
quel agencement dans la grille). Les objets marqués **[Q6]**/**[Q7]** ne peuvent être physiquement
craftés qu'une fois la quête 6 (armure) ou 7 (gadgets) terminée - la recette existe mais le craft
est annulé sinon (`BatmanRecipeGate`), pas seulement masqué dans le carnet de recettes.

| Objet | Recette | Résultat |
|---|---|---|
| Fibre de Kevlar **[Q6]** | 4x Ficelle + 4x Lingot de fer + 1x Laine noire | x4 |
| Tissu à mémoire de forme **[Q6]** | 4x Fibre de Kevlar + 4x Membrane de phantom + 1x Redstone | x1 |
| Composant électronique **[Q6]** | 2x Redstone + 1x Lingot de fer + 1x Pépite d'or | x2 |
| Plastron de combat **[Q6]** | 6x Fibre de Kevlar | x1 |
| Cape **[Q6]** | 5x Tissu à mémoire de forme | x1 |
| Cagoule de la chauve-souris **[Q6]** | 4x Fibre de Kevlar + 1x Composant électronique | x1 |
| Plastron caparaçonné **[Q6]** | 1x Plastron de combat + 1x Cape | x1 |
| Jambières blindées **[Q6]** | 6x Fibre de Kevlar | x1 |
| Bottes furtives **[Q6]** | 4x Fibre de Kevlar | x1 |
| Batarang **[Q7]** | 3x Lingot de fer + 1x Teinture noire | x4 |
| Grappin **[Q7]** | 2x Composant électronique + 3x Lingot de fer + 2x Ficelle + 1x Piston | x1 |
| Fumigène **[Q7]** | 2x Poudre à canon + 1x Charbon + 1x Bouteille en verre | x1 |
| Carnet d'énigmes | **Non craftable** - reçu automatiquement à la fin de la quête 3 | - |

## Zones à risque non vérifiées par compilation réelle

**Corrections de ce round** (nouvelles API introduites, non compilées) :
1. **`Item#useOn(UseOnContext)`** (grappin) - override standard vanilla, mais jamais utilisé
   ailleurs dans ce mod jusqu'ici.
2. **`Entity#playerTouch(Player)`** (batarang, ramassage au sol) - même remarque, mécanisme copié
   du comportement des flèches vanilla (`AbstractArrow`).
3. **`MobEffects.SLOW_FALLING`** (plané de la cape) - effet vanilla standard, risque faible.
4. **`GuiGraphics#blit(ResourceLocation, x, y, width, height, u, v, uWidth, vHeight, texW, texH)`**
   (mini-jeu de reconstitution de preuve, 11 arguments avec mise à l'échelle) - la variante précise
   de `blit` utilisée n'a pas pu être vérifiée par compilation ; si erreur, regarder les autres
   surcharges de `GuiGraphics#blit` disponibles dans les sources déclarées par l'erreur.
5. **`Stats.SPRINT_ONE_CM`** (course, quête 3a) - même famille que `Stats.JUMP`/`Stats.SWIM_ONE_CM`
   déjà utilisés et non signalés en erreur, donc risque faible par association.

**Restant du round précédent** (non encore confirmées) :
6. **`Entity#getPersistentData()`** (`StealthApproachCondition`, quête 2).
7. **`Level#getEntities(EntityType<?>, AABB, Predicate)`** (`ProximityToEntityCondition`, quête 6).
8. **`LivingDeathEvent` sans acteur joueur** (`QuestManager#onNonPlayerDeath`, quête 1).

Le reste (moteur de quête générique, réseau, GUI, armure/gadgets hors des points ci-dessus) réutilise
des mécanismes déjà éprouvés lors des rounds précédents.

## Ce qui est fonctionnel

- Les 7 quêtes de "Origine", renommées et dotées d'un texte narratif affiché dans le menu.
- Compteurs course/saut/nage tous basés sur les stats vanilla natives (fiables).
- 3 nouveaux mini-jeux indépendants de la langue (coffre-fort, reconstitution de preuve,
  crochetage), tirés aléatoirement, tentatives illimitées.
- Cape (chute lente vanilla), grappin (courte et longue portée), batarangs (façon flèche,
  ramassables), fumigènes (aveuglent tout le monde) : tous corrigés ce round.
- Effets à recharge courte (vision nocturne, etc.) stables, sans clignotement.
- GUI complet inchangé dans sa logique : roster, détail de questline (description narrative +
  objectifs + compteurs), tracker HUD optionnel.

## Ce qui est simplifié par rapport au cahier des charges

- **Mini-jeu de reconstitution** : échanges de tuiles libres (cliquer deux tuiles quelconques)
  plutôt qu'un vrai glissement avec case vide façon 15-puzzle - choix délibéré pour garantir que
  toute disposition mélangée reste résolvable (un vrai 15-puzzle a des permutations mathématiquement
  insolubles une fois sur deux).
- **Mini-jeu de crochetage** : la vitesse du curseur est constante d'une goupille à l'autre, seule
  la largeur de la zone cible diminue - une difficulté croissante par vitesse en plus de la largeur
  serait une amélioration possible.
- **Textures** : voir "Recommandations" plus bas.
- **`gradle build` non vérifié** (voir l'avertissement en haut de ce document).

## Recommandations pour la prochaine itération, par priorité

1. **Compiler et corriger** les zones à risque listées plus haut, en particulier les 4 nouvelles
   API introduites ce round (`useOn`, `playerTouch`, `blit`, `SLOW_FALLING`).
2. Tester en jeu chacune des 9 corrections de bugs de ce round individuellement (cape, grappin à
   courte ET longue portée, batarang au sol + ramassage, fumigène sur le lanceur, vision nocturne
   sur une session longue pour confirmer l'absence de clignotement).
3. Remplacer les textures placeholder par de vrais assets, en particulier `puzzle_evidence.png`
   (actuellement un simple symbole chauve-souris généré par script) qui gagnerait à être une vraie
   "photo de preuve" visuellement plus riche pour le mini-jeu de reconstitution.
4. Envisager une difficulté croissante pour le crochetage (vitesse du curseur en plus de la
   largeur de la zone).
