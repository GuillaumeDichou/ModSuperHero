# Hero's Journey — Arc "Batman : Origine"

Mod NeoForge pour Minecraft **1.21.1** (Java 21). Premier arc d'un mod de super-héros à
questlines narratives : le joueur incarne **Batman** à travers une progression en 7 quêtes basée
sur les comics, **sans structure custom et sans PNJ parlant** - uniquement des mécaniques, mobs et
objets vanilla.

## ⚠️ Important : ce dépôt est développé sans accès à `gradle build` côté agent

Cet environnement de développement a une politique réseau qui bloque `maven.neoforged.net` (seul
Maven Central est joignable), donc **`./gradlew build` ne peut être exécuté et vérifié que chez
toi**, jamais ici. Merci de recompiler et de me remonter les erreurs le cas échéant.

## Ce round : diagnostics, corrections de bugs signalés, et nouvelles fonctionnalités de test

### 1. Compteur course/nage : confirmation + vrai correctif

**Confirmation demandée** : il n'y a qu'**une seule** méthode de comptage active, `QuestManager`
lit exclusivement des stats vanilla natives via `trackVanillaStatDelta` - **aucun** système de
comptage maison tick-par-tick ne tourne en parallèle (le seul qui existait, l'ancien
`trackSprintDistance` à base de delta de position, a été supprimé il y a deux rounds). Vérifié en
relisant le fichier ligne à ligne avant de corriger quoi que ce soit.

**Le vrai bug trouvé** : pour la nage, seul `Stats.SWIM_ONE_CM` était lu. Or ce stat vanilla n'est
incrémenté que pendant la pose de nage "rapide" (sprint-nage) et mesure alors une distance en
3D - donc monter/descendre en sprint-nageant compte pleinement, exactement comme l'affiche l'écran
de statistiques vanilla ("Distance nagée") : ce n'est pas un bug de ce mod, c'est la définition même
de ce stat. En revanche, nager lentement/sans sprint (aucune pose de nage rapide) n'incrémentait
alors **aucun** stat suivi, d'où le "parfois pas du tout" signalé. Corrigé en sommant **trois**
stats vanilla : `SWIM_ONE_CM` + `WALK_ON_WATER_ONE_CM` (nage en surface, horizontal uniquement) +
`WALK_UNDER_WATER_ONE_CM` (nage immergée, horizontal uniquement) - la nage lente/casuelle compte
enfin, et la part "verticale" ne vient plus que de la composante sprint-nage, diluée dans le total.
Le sprint (course) reste sur `Stats.SPRINT_ONE_CM` seul, qui est déjà horizontal uniquement côté
vanilla - pas de changement nécessaire là.

### 2. Commande de debug pour voir les stats en jeu

`/heroesjourney stats <joueur>` (nouveau) affiche dans le chat : héros actif, étape en cours,
flags/capacités débloqués, valeurs réelles des attributs vitesse de déplacement / dégâts d'attaque /
vitesse d'attaque, effets de potion actifs, quelles pièces de la tenue sont portées, et l'état
sneak/onGround/vélocité verticale du moment. De quoi vérifier qu'un bonus de quête s'applique
réellement sans devoir l'inférer indirectement (ex. en allant taper un mob).

### 3. Lueur des coffres → repérage des menaces (changement de récompense, quête Esprit)

L'effet vanilla **Glowing** (utilisé par les flèches spectrales) ne s'applique **qu'aux entités**,
jamais aux blocs - il n'aurait donc jamais pu fonctionner sur des coffres tel quel, quelle que soit
l'implémentation. Rendre un bloc "visible à travers les murs" nécessiterait un rendu custom (contour
dessiné sans test de profondeur, façon ESP) - clairement signalé plutôt que retenté avec une solution
à particules inadaptée.

À la place, la récompense de la quête 4 est maintenant **"Repérage des menaces"** : la capacité
(touche `V`) applique le vrai effet vanilla Glowing à tous les mobs hostiles dans un rayon
configurable (`THREAT_GLOW_RADIUS`, 20 blocs par défaut) pendant **15 secondes**
(`THREAT_GLOW_DURATION_TICKS`, 300 ticks), cooldown 30s (`THREAT_GLOW_COOLDOWN_TICKS`, inchangé).
Comme Glowing fonctionne nativement sur les entités, on récupère le vrai rendu "silhouette visible
à travers les murs" sans aucun bricolage de rendu - thème : repérer les menaces avant qu'elles ne
te repèrent.

### 4. Chute lente (cape) : logs de debug ajoutés

Aucun nouveau bug identifié par relecture du code (la logique - effet vanilla Slow Falling
rafraîchi tant que sneak+en l'air+plastron caparaçonné porté+Batman actif - semble correcte). Des
logs de debug ont été ajoutés dans `ArmorEffectsHandler#onPlayerTick` (préfixe `[glide-debug]`,
throttlés à 1 fois toutes les 10 ticks), qui affichent explicitement : héros actif, plastron
caparaçonné porté ou non, sneak, onGround, vélocité verticale, et le calcul final de "gliding".
**Si ça ne fonctionne toujours pas après ce round, ces logs (`logs/latest.log`, chercher
`glide-debug`) diront exactement quelle condition ne passe pas.**

### 5. Grappin : un vrai bug trouvé et corrigé + logs de debug ajoutés

**Bug trouvé** : `GrappleHandler` annulait la traction si `player.isShiftKeyDown()` était vrai -
et ça, dès le tout premier tick. Si le joueur était **déjà** en train de s'accroupir au moment de
tirer (courant : beaucoup de joueurs s'accroupissent par réflexe près d'un rebord avant de viser),
la traction s'annulait instantanément, avant même le moindre mouvement visible - ce qui correspond
exactement à "ne fonctionne pas du tout". Corrigé : l'annulation par sneak exige maintenant que le
joueur **appuie sur sneak pendant** la traction (transition, pas un état déjà présent au lancement).

Logs de debug ajoutés (préfixe `[grapple-debug]`) à chaque étape : `use()`/`useOn()` appelé (avec
l'état du cooldown), résultat du raytrace pour `use()`, `fireGrapple` déclenché, `startPull`
enregistré, et chaque tick de traction (âge, distance restante, mouvement appliqué, raison
d'annulation le cas échéant). De quoi voir précisément où la chaîne casse si ce correctif ne
suffit pas.

### 6. Batarangs : arc de chute façon boule de neige + non ramassable après avoir touché un mob

- Trajectoire : suppression de `isNoGravity()` (qui forçait un vol plat/rectiligne) - le batarang
  utilise maintenant la gravité par défaut de `ThrowableItemProjectile`, exactement comme une
  boule de neige.
- S'il touche un mob : dégâts + ralentissement infligés comme avant, puis l'entité est **retirée
  immédiatement** (`discard()`) - elle ne devient plus un objet au sol ramassable.
- S'il ne touche rien (atterrit sur un bloc) : comportement inchangé, il reste au sol et reste
  ramassable en marchant dessus.

### 7. Recettes : formes reconnaissables + emplacement exact dans la table

Les 4 pièces d'armure (les seules avec une forme vanilla "iconique" à respecter) utilisent
maintenant des recettes **avec forme** (`crafting_shaped`) suivant le gabarit standard de
Minecraft, matériaux du mod substitués aux emplacements habituels - voir le tableau détaillé plus
bas. Les intermédiaires (fibre de kevlar, tissu à mémoire de forme, composant électronique,
plastron de combat, cape) et les gadgets (batarang, grappin, fumigène) restent sans forme
(l'emplacement dans la grille n'a aucune importance pour ces objets-là, aucune forme vanilla de
référence n'existe pour eux).

**Effet de bord à noter** : suivre le gabarit standard du plastron a changé ses quantités
d'assemblage : 1 plastron de combat + 1 cape auparavant → **4 plastrons de combat + 2 capes**
maintenant (pour remplir un emplacement de forme reconnaissable). Les jambières sont passées de 6 à
**7 fibres de kevlar** pour la même raison (le gabarit standard des jambières a 7 cases remplies,
pas 6). Cowl (4 kevlar + 1 composant) et bottes (4 kevlar) sont inchangées, leurs quantités
originales correspondaient déjà exactement au nombre de cases du gabarit standard. Si ce
renchérissement de la tenue complète (plastron notamment) semble excessif en jeu, les JSON de
recette sont ajustables sans recompiler.

### 8. Carnet d'énigmes : séquence fixe et persistante (plus d'aléatoire)

Les 3 mini-jeux sont maintenant montrés dans un **ordre fixe** : 1) taquin (reconstitution de
preuve), 2) crochetage, 3) coffre-fort (Mastermind). La position dans la séquence est déduite de la
progression déjà enregistrée de l'objectif "résoudre des énigmes" (`QuestManager#currentPuzzleProgress`,
générique - ne connaît aucun id Batman en dur, réutilisable par un futur héros) : fermer le carnet
après avoir réussi le puzzle 1 et le rouvrir propose directement le puzzle 2, sans avoir besoin
d'un nouveau champ de sauvegarde dédié - c'est le compteur d'objectif de quête existant qui sert
déjà cet usage.

### 9. Seuils réduits pour faciliter les tests (config, pas en dur)

| Config | Avant | Maintenant |
|---|---|---|
| `stealth.approachCount` (approches en sneak) | 15 | **1** |
| `training.runDistanceBlocks` (course) | 3000 | **100** (gardé > 1 exprès, pour pouvoir vérifier que le comptage est proportionnel) |
| `training.jumpCount` (sauts) | 500 | **1** |
| `training.swimDistanceBlocks` (nage) | 500 | **100** (même raison que la course) |
| `puzzle.targetCount` (nombre d'énigmes) | 10 | **3** (correspond exactement à la séquence fixe des 3 mini-jeux) |
| `martialArts.kills` (mobs à mains nues) | 40 | **1** |

## Installation

1. Compiler (`./gradlew build`) ou récupérer le `.jar`.
2. Installer NeoForge `21.1.176` pour Minecraft 1.21.1.
3. Copier le `.jar` dans le dossier `mods/` de l'instance.

## Touches

| Touche par défaut | Action |
|---|---|
| `K` | Ouvre le menu Héros (roster + détail de la questline) |
| `V` | Utilise la capacité active du héros (repérage des menaces, une fois débloquée) |

Les deux touches sont reconfigurables dans **Options > Contrôles > Hero's Journey**.

## Commandes de debug admin

```
/heroesjourney progress <joueur> <hero> <étape>     # force l'étape courante (0-indexé)
/heroesjourney activate <joueur> <hero>              # active un héros instantanément
/heroesjourney unlockability <joueur> <ability>      # débloque une capacité (ex: threat_glow)
/heroesjourney listheroes                            # liste les héros enregistrés
/heroesjourney stats <joueur>                        # affiche progression + attributs/tenue en jeu
```
`hero` = `batman` pour cet arc.

## Configuration

Toutes les valeurs d'équilibrage (rayons, distances, compteurs, cooldowns, bonus passifs, réglages
des 3 mini-jeux, etc.) sont dans `config/heroesjourney-common.toml`, généré au premier lancement -
voir la section "Seuils réduits pour faciliter les tests" plus haut pour les valeurs actuelles.

---

## Déroulé de la questline, quête par quête

### 1. Le Serment
Trouve un village généré naturellement, laisse un mob hostile ou neutre tuer un villageois pendant
que tu es à proximité (`ORIGIN_VILLAGER_WITNESS_RADIUS`, 18 blocs) - la quête démarre
automatiquement.

### 2. Dans l'ombre - comment fonctionne la détection
Accroupis-toi (`Shift`) à moins de `STEALTH_APPROACH_RADIUS` blocs (6 par défaut) d'un mob hostile,
et maintiens la position `STEALTH_APPROACH_CONSECUTIVE_SECONDS` secondes d'affilée (3 par défaut)
sans que le mob ne te cible - s'appuie directement sur le ciblage IA vanilla (ligne de vue + portée
de détection réduite par le sneak, mécaniques natives). Chaque mob ne compte qu'une fois. 1 approche
requise pour l'instant (config réduite pour les tests, normalement 15).

### 3. Corps et discipline
Course, saut et nage progressent **en parallèle**, chaque sous-catégorie débloquant son propre
passif dès qu'elle est individuellement terminée. Seuils actuels (réduits pour les tests) : 100
blocs en sprint, 1 saut, 100 blocs à la nage. Une fois les trois faites, le **carnet d'énigmes**
est ajouté automatiquement à l'inventaire.

### 4. Esprit
Clique-droit avec le carnet d'énigmes pour lancer, dans l'ordre, le taquin puis le crochetage puis
le coffre-fort (voir plus haut). Les 3 résolus débloquent la capacité "repérage des menaces"
(touche `V`).

### 5. Le Combattant
Élimine des mobs hostiles (1 pour l'instant, normalement 40) dont le coup fatal est à mains nues.

### 6. Un signe dans la nuit
Laisse une chauve-souris passer à moins de `BAT_PROXIMITY_RADIUS` blocs (3 par défaut). Débloque
les recettes de l'armure.

### 7. Naissance d'une légende
Crafte et porte simultanément la cagoule, le plastron caparaçonné, les jambières et les bottes.
Débloque les recettes des gadgets et donne un kit de départ (1 grappin, 8 batarangs, 3 fumigènes).

## Effets et bonus, avec leurs valeurs précises

| Source | Effet | Valeur par défaut |
|---|---|---|
| Quête 2 (passif) | Chance qu'un mob hostile échoue à cibler le joueur | 30% |
| Quête 3a (passif) | Vitesse de déplacement permanente | Niveau I (+20%) |
| Quête 3b (passif) | Réduction des dégâts de chute, en plus de celle des jambières | 30% (~51% cumulé) |
| Quête 3c (passif) | Vitesse de nage (Dauphin's Grace) permanente | Niveau I |
| Quête 4 (capacité, touche `V`) | Glowing vanilla sur les mobs hostiles proches | Rayon 20 blocs, 15s, 30s de cooldown |
| Quête 5 (passif) | Bonus de dégâts à mains nues | +2.0 (1 cœur) |
| Quête 5 (passif) | Bonus de vitesse d'attaque à mains nues | +1.0 (base vanilla 4.0, ~+25%) |
| Cagoule (intrinsèque) | Vision nocturne permanente | - |
| Bottes (intrinsèque) | Vitesse de déplacement | Niveau I |
| Jambières (intrinsèque) | Réduction des dégâts de chute | 30% |
| Plastron caparaçonné (intrinsèque) | Chute lente (vanilla) + dérive horizontale en sneak+chute | Vitesse horizontale 0.12 bloc/tick |
| Grappin | Portée / cooldown / vitesse max / accélération | 24 blocs / 2s / 1.6 bloc-tick / 0.18 par tick |
| Batarang | Dégâts / ralentissement (Lenteur II) | 3.0 / 3s |
| Fumigène | Rayon / durée / aveuglement (Cécité II) | 6 blocs / 8s / 2s par pulsation |

Utilise `/heroesjourney stats <joueur>` pour vérifier ces valeurs en direct en jeu.

## Comment obtenir/crafter chaque objet

### Pièces d'armure (recettes avec forme, emplacement précis dans la grille 3x3)

Légende : case vide = rien à mettre. **[Q6]** = craft physiquement bloqué tant que la quête 6
n'est pas terminée (`BatmanRecipeGate`).

**Cagoule [Q6]** (4x Fibre de Kevlar, 1x Composant électronique) :
```
[Kevlar ][Kevlar ][Kevlar ]
[Kevlar ][ vide  ][Électro]
[ vide  ][ vide  ][ vide  ]
```

**Plastron caparaçonné [Q6]** (4x Plastron de combat, 2x Cape) :
```
[ Cape  ][ vide  ][ Cape  ]
[Plastr.][Plastr.][Plastr.]
[Plastr.][ vide  ][ vide  ]
```
(le plastron de combat occupe la ligne du milieu + le bas-gauche ; la cape est aux deux coins
supérieurs, comme attachée aux épaules)

**Jambières [Q6]** (7x Fibre de Kevlar) :
```
[Kevlar ][Kevlar ][Kevlar ]
[Kevlar ][ vide  ][Kevlar ]
[Kevlar ][ vide  ][Kevlar ]
```

**Bottes [Q6]** (4x Fibre de Kevlar) :
```
[Kevlar ][ vide  ][Kevlar ]
[Kevlar ][ vide  ][Kevlar ]
```

### Autres objets (sans forme - peu importe l'emplacement dans la grille)

| Objet | Ingrédients | Résultat |
|---|---|---|
| Fibre de Kevlar **[Q6]** | 4x Ficelle + 4x Lingot de fer + 1x Laine noire | x4 |
| Tissu à mémoire de forme **[Q6]** | 4x Fibre de Kevlar + 4x Membrane de phantom + 1x Redstone | x1 |
| Composant électronique **[Q6]** | 2x Redstone + 1x Lingot de fer + 1x Pépite d'or | x2 |
| Plastron de combat **[Q6]** | 6x Fibre de Kevlar | x1 |
| Cape **[Q6]** | 5x Tissu à mémoire de forme | x1 |
| Batarang **[Q7]** | 3x Lingot de fer + 1x Teinture noire | x4 |
| Grappin **[Q7]** | 2x Composant électronique + 3x Lingot de fer + 2x Ficelle + 1x Piston | x1 |
| Fumigène **[Q7]** | 2x Poudre à canon + 1x Charbon + 1x Bouteille en verre | x1 |
| Carnet d'énigmes | **Non craftable** - reçu automatiquement à la fin de la quête 3 | - |

## Zones à risque non vérifiées par compilation réelle

**Nouvelles ce round** :
1. **`Stats.WALK_ON_WATER_ONE_CM` / `Stats.WALK_UNDER_WATER_ONE_CM`** (nage) - même famille que
   `Stats.SWIM_ONE_CM`/`SPRINT_ONE_CM`/`JUMP` déjà utilisés sans erreur signalée, risque faible par
   association, mais jamais compilés.
2. **`ThrowableItemProjectile` gravité par défaut** (batarang) - je pars du principe que ne pas
   surcharger `isNoGravity()` restaure le comportement gravité-activée par défaut de la classe
   parente (comme un œuf/une boule de neige vanilla), jamais vérifié par compilation dans ce mod.
3. **`minecraft:crafting_shaped` avec clé partagée sur plusieurs lignes** (les 4 recettes d'armure)
   - format standard, mais jamais utilisé ailleurs dans ce mod jusqu'ici (tout était
    `crafting_shapeless`).

**Restant des rounds précédents** (non encore confirmées) :
4. `Item#useOn(UseOnContext)`, `Entity#playerTouch(Player)` (grappin, batarang).
5. `MobEffects.SLOW_FALLING`, `MobEffects.GLOWING` appliqué à un `Mob` (chute lente, repérage des
   menaces) - mécaniques vanilla standards, risque faible.
6. `GuiGraphics#blit` 11-arguments (mini-jeu taquin).
7. `Entity#getPersistentData()` (quête 2), `Level#getEntities(EntityType<?>, ...)` (quête 6),
   `LivingDeathEvent` sans acteur joueur (quête 1).

## Ce qui est fonctionnel

- Les 7 quêtes de "Origine" avec titres et texte narratif affichés dans le menu.
- Compteurs course/saut/nage tous basés sur des stats vanilla natives, sans aucun comptage maison
  résiduel (confirmé par relecture) ; nage corrigée pour ne plus manquer la nage lente/casuelle.
- Commande `/heroesjourney stats` pour vérifier les bonus en direct.
- Capacité "repérage des menaces" : vrai effet Glowing vanilla sur les mobs hostiles proches.
- 3 mini-jeux dans une séquence fixe et persistante (taquin → crochetage → coffre-fort).
- Grappin corrigé (bug d'annulation instantanée par sneak), logs de debug pour cape/grappin.
- Batarangs en arc de chute, non ramassables après avoir touché un mob.
- Recettes d'armure avec formes reconnaissables + emplacement documenté.

## Ce qui est simplifié par rapport au cahier des charges

- **Logs de debug** (`[glide-debug]`, `[grapple-debug]`) sont volontairement verbeux/temporaires -
  à retirer ou réduire une fois les deux mécaniques confirmées fonctionnelles en jeu.
- **Coût de la tenue complète** a augmenté (voir section recettes) pour respecter les formes
  vanilla standards - ajustable directement dans les JSON si trop cher.
- **`gradle build` non vérifié** (voir l'avertissement en haut de ce document).

## Recommandations pour la prochaine itération, par priorité

1. **Compiler et corriger** les zones à risque listées plus haut.
2. Tester en jeu la cape et le grappin avec les logs de debug actifs ; si un problème persiste,
   coller les lignes `[glide-debug]`/`[grapple-debug]` correspondantes pour un diagnostic précis.
3. Une fois cape/grappin confirmés fonctionnels, retirer ou réduire les logs de debug ajoutés ce
   round.
4. Remettre les seuils de quête à leurs valeurs de production une fois les tests terminés.
5. Rééquilibrer le coût des recettes d'armure si le renchérissement du plastron/jambières s'avère
   excessif en jeu.
