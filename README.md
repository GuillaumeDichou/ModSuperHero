# Hero's Journey — Arc "Batman : Origine"

Mod NeoForge pour Minecraft **1.21.1** (Java 21). Premier arc d'un mod de super-héros à
questlines narratives : le joueur incarne Batman à travers une progression en 7 quêtes basée sur
les comics, **sans structure custom et sans PNJ parlant** - uniquement des mécaniques, mobs et
objets vanilla (seule exception : le Manoir Wayne, conservé en tant que décor, voir plus bas).

## ⚠️ Important : ce dépôt est développé sans accès à `gradle build` côté agent

Cet environnement de développement a une politique réseau qui bloque `maven.neoforged.net` (seul
Maven Central est joignable), donc **`./gradlew build` ne peut être exécuté et vérifié que chez
toi**, jamais ici. L'architecture générique (roster, moteur de quêtes, armure/gadgets, Manoir
Wayne en NBT) **a déjà été compilée et testée en jeu avec succès chez toi** après plusieurs
allers-retours de correction d'API - c'est du code éprouvé.

## Ce round : refonte complète de la questline ("Batman Begins" → "Origine")

Ce round remplace entièrement l'ancienne questline calquée sur *Batman Begins* de Nolan (prison,
monastère, asile, train, PNJ Ken/Henri, boss Ken/Scarecrow/Henri) par une nouvelle questline
**"Origine"** basée sur les comics, sans aucune structure custom ni PNJ. Voir "Ce qui a été
supprimé" ci-dessous pour le détail complet du nettoyage.

**Zones à risque non vérifiées par compilation réelle**, par ordre de priorité si une erreur
apparaît :
1. **`Stats.CUSTOM.get(Stats.JUMP)` / `Stats.SWIM_ONE_CM`** (`QuestManager#trackVanillaStatDelta`)
   - les quêtes 3b (sauts) et 3c (nage) lisent les statistiques vanilla natives du joueur plutôt
   que de ré-implémenter une détection de saut/nage à la main ; les noms exacts de ces stats sont
   ceux que j'ai en mémoire pour 1.21.1 mais n'ont pas pu être vérifiés par compilation.
2. **`Entity#getPersistentData()`** (`StealthApproachCondition`, quête 2) - API NeoForge standard
   pour stocker des données persistantes sur une entité (ici : quel mob a déjà été "approché"),
   mais jamais utilisée ailleurs dans ce mod jusqu'ici.
3. **`Level#getEntities(EntityType<?>, AABB, Predicate)`** (`ProximityToEntityCondition`, quête 6 -
   la chauve-souris) - repose sur le fait qu'`EntityType<T>` implémente `EntityTypeTest<Entity, T>`
   pour passer directement `EntityType.BAT` en premier argument.
4. **`LivingDeathEvent` sans acteur joueur** (`QuestManager#onNonPlayerDeath`, quête 1) - le
   filtre "un mob (pas le joueur, pas une chute/le feu/la lave/la noyade) tue un villageois" est
   fait via `event.getSource().getEntity() instanceof LivingEntity && !(... instanceof Player)`,
   ce qui exclut naturellement toutes les causes non-vivantes sans avoir besoin de lister des
   `DamageTypeTags` - logique simple, mais jamais exercée dans ce mod avant ce round.
5. Le reste (moteur de quête générique, réseau, GUI) réutilise des mécanismes déjà éprouvés dans
   les rounds précédents (voir historique des commits) - risque faible.

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

## Déroulé de test rapide de la questline "Origine"

1. Lancer le jeu, ouvrir le menu `K`, cliquer sur "Batman (Nolan)" puis "Activer ce héros".
2. `/heroesjourney progress <joueur> batman_nolan <N>` permet de sauter directement à l'étape `N`
   (0-indexé) sans avoir à tout rejouer - voir la section commandes ci-dessous. Pratique pour
   tester chaque étape isolément vu qu'aucune ne dépend plus d'une structure à générer.
3. **Quête 1 (La mort des parents)** : trouve un village généré naturellement, laisse un mob
   hostile ou neutre tuer un villageois pendant que tu es à proximité (~18 blocs, configurable) -
   la quête démarre automatiquement.
4. **Quête 2 (Espionnage)** : accroupis-toi (`Shift`) à moins de 6 blocs d'un mob hostile qui ne
   t'a pas repéré, maintiens la position ~3 secondes ; répète sur 15 mobs distincts.
5. **Quête 3 (Physique)** : sprinte 3000 blocs, saute 500 fois, nage 500 blocs - les trois en
   parallèle, chacune débloquant son propre passif dès qu'elle est terminée. Une fois les trois
   faites, le **carnet d'énigmes** est ajouté à l'inventaire automatiquement.
6. **Quête 4 (Esprit)** : clique-droit avec le carnet d'énigmes pour lancer un mini-jeu aléatoire
   (mémoire / logique / code à casser) ; résous-en 10 pour débloquer la capacité "lueur des
   coffres" (touche `V`).
7. **Quête 5 (Arts martiaux)** : élimine 40 mobs hostiles à mains nues (item en main vide).
8. **Quête 6 (La chauve-souris)** : laisse une chauve-souris (`minecraft:bat`) passer à moins de 3
   blocs de toi - n'importe où, aucun lieu particulier requis. Débloque les recettes de l'armure.
9. **Quête 7 (Devenir Batman)** : crafte et porte simultanément la cagoule, le plastron
   caparaçonné (assemblé depuis le plastron de combat + la cape), les jambières et les bottes -
   débloque les recettes des gadgets et donne un kit de départ (grappin, 8 batarangs, 3
   fumigènes).

## Commande de debug admin

```
/heroesjourney progress <joueur> <hero> <étape>     # force l'étape courante (0-indexé)
/heroesjourney activate <joueur> <hero>              # active un héros instantanément
/heroesjourney unlockability <joueur> <ability>      # débloque une capacité (ex: chest_glow)
/heroesjourney listheroes                            # liste les héros enregistrés
```
`hero` = `batman_nolan` pour cet arc.

## Configuration

Toutes les valeurs d'équilibrage (rayons, distances, compteurs, cooldowns, bonus passifs, etc.)
sont dans `config/heroesjourney-common.toml`, généré au premier lancement. La **rareté et le
biome du Manoir Wayne**, en revanche, sont réglés dans les fichiers datapack
`data/heroesjourney/worldgen/structure_set/wayne_manor.json` (spacing/separation) et
`data/heroesjourney/worldgen/structure/wayne_manor.json` (biomes).

---

## Le Manoir Wayne : conservé, mais décoratif dans cet arc

Le Manoir Wayne + son cimetière familial (`wayne_manor.nbt` / `wayne_cemetery.nbt`, chargés via
`TemplateStructurePiece` - voir le round précédent) **restent dans le mod tels quels** : ils
génèrent toujours, sont toujours protégés (voir plus bas), mais **aucune quête de l'arc "Origine"
n'y pointe plus** - `/locate structure heroesjourney:wayne_manor` fonctionne toujours si tu veux
le retrouver pour l'explorer, mais ce n'est plus une étape obligatoire de la questline.

**Conséquence importante** : `WayneManorProtection` reste branchée sur
`BatmanAbilities.FLAG_WAYNE_MANOR_UNLOCKED`, mais **plus aucune récompense de quête ne pose ce
flag** dans cet arc - le manoir reste donc protégé (indestructible) indéfiniment pour tout joueur,
jusqu'à ce qu'un futur arc décide d'un nouveau mécanisme de déblocage. C'est un choix délibéré
("garde-le en l'état... on décidera de son rôle plus tard"), pas un oubli - mais il faut le savoir
avant de chercher à démolir le manoir en jeu.

## Ce qui a été supprimé lors du nettoyage (étape 0)

- **Structures** : `HeroBuildingStructure`/`HeroBuildingPiece` (système générique "boîte simple")
  et les 3 fichiers de layout (prison/monastère/asile/train), plus tous leurs fichiers datapack
  (`worldgen/structure`, `worldgen/structure_set`, `tags/worldgen/structure`) - il ne reste que
  Wayne Manor comme structure custom.
- **PNJ et dialogue** : `QuestNpcEntity`, tout le package `com.heroesjourney.dialogue` (arbre de
  dialogue, registre, vues), `DialogueScreen`, les payloads réseau associés
  (`OpenDialoguePayload`/`DialogueChoicePayload`) et `SimpleHumanoidRenderer` (son seul
  utilisateur restant était les PNJ/mobs supprimés).
- **Boss et mobs** : `KenBoss`, `ScarecrowBoss`, `HenriBoss`, leur classe de base
  `HeroBossEntity`, `BossRegistry`, `BossSpawnerBlock`/`BossSpawnerBlockEntity`,
  `FearToxinProjectile`, `NinjaMob`, `PrisonGuardMob` - aucun n'avait d'utilité évidente hors de
  l'arc "Batman Begins" (ils sont tous très spécifiquement Ken/Henri/prison/monastère/asile), donc
  supprimés plutôt que laissés branchés sur rien.
- **Objets devenus inutiles** : `RUSTY_CELL_KEY` (clé de la prison), `TREASURE_MAP` +
  `TreasureMapFactory` (plus aucune quête n'offre de carte au trésor dans cet arc).
- **Conditions de quête devenues inutiles** : `PrisonBreakCondition`, `BossKillCondition`,
  `NpcInteractCondition` (liées aux structures/PNJ/boss supprimés), `ProximityToBlockCondition`
  (utilisée par l'ancienne quête 1 sur les tombes du Manoir Wayne - la nouvelle quête 1 ne pointe
  plus vers Wayne Manor du tout), et - jamais utilisée par aucune version de la questline,
  trouvée lors de la vérification finale - `ItemObtainedCondition`. `ConditionUtil` et le record
  `QuestEvent.ItemObtained` ont suivi, n'ayant plus aucun appelant.
- **Config** : tous les seuils liés à l'entraînement Ligue des Ombres, aux PV des boss, au délai de
  respawn boss, et les passifs génériques Résistance/Vitesse accordés par la défaite de Ken (rien
  d'équivalent dans le nouvel arc).
- **Assets** : textures des PNJ/boss/mobs supprimés, blockstate/modèle/texture du bloc
  boss-spawner, textures/modèles de la clé de prison et de la carte au trésor.

**Ce qui a été gardé sans modification** : le moteur de quête générique
(`QuestStage`/`QuestObjective`/`QuestCondition`/`QuestReward`/`StageHook`), le registre de héros,
le système d'attachment `HeroData`/`HeroProgress`, tout le GUI (roster, détail de questline, HUD
tracker) - qui affiche cette nouvelle questline sans aucune modification de sa logique -, l'armure,
les gadgets (grappin/batarang/fumigène) et leurs mécaniques physiques, et le Manoir Wayne.

## Ce qui est fonctionnel

- **Les 7 quêtes de "Origine"** câblées de bout en bout, chacune pilotée par un mob/mécanique/objet
  vanilla (voir le déroulé de test plus haut) : mort d'un villageois à proximité, approches
  furtives, entraînement physique en 3 volets parallèles (chacun débloquant son propre passif dès
  sa propre complétion via `RewardOnCompleteCondition`, un petit ajout générique au moteur de
  quête), carnet d'énigmes (3 mini-jeux), 40 kills à mains nues, rencontre avec une chauve-souris,
  port de l'armure complète.
- **Carnet d'énigmes** : objet non craftable, clic-droit ouvre un des 3 mini-jeux au hasard
  (mémoire à reproduire, énigme de déduction à choix multiples parmi 16, message chiffré par
  décalage de César à décoder) - contenu généré côté serveur, validé côté client (comme le reste
  de l'état piloté par le client dans ce mod, ex. les cooldowns de capacité).
- **Déblocage de recettes en deux temps** via deux avancements cachés : l'armure (quête 6, "la
  chauve-souris") puis les gadgets (quête 7, port de la tenue) - le craft physique reste bloqué
  (pas seulement le carnet de recettes) tant que l'étape correspondante n'est pas franchie.
- **Armure et gadgets** avec leurs mécaniques inchangées : cagoule (vision nocturne), plané de la
  cape en sneak+chute, réduction des dégâts de chute (jambières), bottes (vitesse), batarang
  boomerang avec ralentissement, grappin avec traction physique progressive, fumigène en zone.
- **Manoir Wayne** : domaine complet (manoir + cimetière) chargé depuis les gabarits NBT, toujours
  protégé - voir la section dédiée plus haut pour son rôle (désormais purement décoratif) dans cet
  arc.
- **GUI complet** inchangé : roster, détail de questline avec statut/compteurs par objectif
  (y compris les 3 sous-compteurs de la quête 3, affichés séparément), tracker HUD optionnel.

## Ce qui est simplifié par rapport au cahier des charges

- **Mini-jeu "mémoire"** : la séquence à reproduire est affichée en clair (rangée de pastilles de
  couleur) plutôt que clignotée chronologiquement avant l'essai - évite une machine à états
  d'animation côté client ; le joueur doit quand même la reproduire dans le bon ordre.
- **Capacité "lueur des coffres"** : reprend le mécanisme à particules déjà éprouvé de l'ancienne
  capacité "Sens du détective" (pulsations de particules visibles seulement du joueur) plutôt
  qu'un vrai effet *Glowing* vanilla via une entité marqueur invisible porteuse de l'effet - cette
  dernière technique est plus fidèle au terme "Glowing" employé dans la demande mais n'a jamais été
  testée dans ce mod ; dis-moi si tu préfères que je l'implémente malgré le risque.
- **Mini-jeu "code à casser"** : les phrases à déchiffrer sont fournies uniquement en français - un
  cryptogramme généré ne peut pas passer par le système de traduction comme le reste du texte du
  mod, donc pas de version anglaise pour ce mini-jeu spécifiquement.
- **Quête 7 ("devenir Batman")** : validée en portant les 4 pièces d'armure simultanément
  (`EquipmentSetCondition`, déjà éprouvé) plutôt qu'en traquant le craft de chaque pièce
  isolément - il faut de toute façon avoir crafté les 4 pièces pour les porter, et cette condition
  lit un état stable (le fait de les porter) plutôt qu'un événement transitoire (le craft).
- **Quête 2 ("espionnage")** : la fenêtre "le mob ne doit pas cibler le joueur pendant un certain
  temps" est vérifiée une fois par seconde (heartbeat existant du moteur de quête) plutôt qu'en
  continu à chaque tick - un mob qui reperdrait puis retrouverait sa cible entre deux heartbeats
  ne serait pas détecté, cas limite jugé acceptable.
- **Manoir Wayne** : voir la section dédiée plus haut - reste dans le mod mais n'est plus une étape
  de quête ; son rôle futur reste à définir.
- **Pas de rotation des structures**, pas de mobilier détaillé pour le Manoir Wayne au-delà du
  gabarit NBT fourni.
- **`gradle build` non vérifié** (voir l'avertissement en haut de ce document).

## Recommandations pour la prochaine itération, par priorité

1. **Compiler et corriger** les zones à risque listées plus haut ; tester en priorité les quêtes
   3b/3c (stats vanilla) et 2 (persistent data sur les mobs), le reste réutilise des mécanismes
   déjà validés.
2. Décider du rôle futur du Manoir Wayne (déblocage de sa protection, nouvelle quête dédiée, ou
   rester purement décoratif) et l'implémenter en conséquence.
3. Si le vrai effet *Glowing* est souhaité pour la capacité de la quête 4, remplacer le mécanisme à
   particules par une entité marqueur invisible porteuse de l'effet.
4. Remplacer les textures placeholder par de vrais assets (la palette et l'organisation des
   fichiers sont prêtes pour ça), en particulier le nouveau `riddle_book`.
5. Étoffer le contenu généré : plus de riddles, plus de phrases à chiffrer, éventuellement une
   difficulté croissante pour le mini-jeu mémoire.
