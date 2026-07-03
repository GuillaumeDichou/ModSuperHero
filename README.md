# Hero's Journey — Arc "Batman Begins"

Mod NeoForge pour Minecraft **1.21.1** (Java 21). Premier arc d'un mod de super-héros à
questlines narratives : le joueur incarne Batman (version Nolan) via une progression en 7 quêtes,
sans craft d'armure "à effets" isolé.

## ⚠️ Important : ce dépôt est développé sans accès à `gradle build` côté agent

Cet environnement de développement a une politique réseau qui bloque `maven.neoforged.net` (seul
Maven Central est joignable), donc **`./gradlew build` ne peut être exécuté et vérifié que chez
toi**, jamais ici. Le code de l'architecture générique et de l'arc Batman de base (roster, quêtes,
armure/gadgets, 3 boss, prison/monastère/asile/train) **a déjà été compilé et testé en jeu avec
succès chez toi** après plusieurs allers-retours de correction d'API (voir l'historique des
commits) - c'est du code éprouvé, pas une hypothèse.

Le **Manoir Wayne dédié** (`structure/wayne/WayneManorStructure.java`,
`structure/wayne/WayneManorPiece.java`, `structure/BuildUtil.java`) est en revanche du code neuf,
écrit avec le même soin mais **pas encore compilé ni testé en jeu**. Recompile
(`./gradlew build`) et relance avant de retester la quête 1 - voir "Le domaine Wayne Manor"
plus bas pour les repères en jeu. Points à vérifier en priorité si une erreur apparaît :
1. **`BedPart`/`BlockStateProperties.BED_PART`** (`WayneManorPiece#buildBedroom`) - API de blockstate la plus récemment ajoutée au fichier, jamais exercée ailleurs dans le mod.
2. **`WayneManorStructure#findGenerationPoint`** - même schéma que `HeroBuildingStructure` (déjà
   validé en jeu), donc risque faible, mais la `BoundingBox` couvre une emprise bien plus grande
   (90x90x50) qu'avant.
3. Les blocs de palette moins courants (`Blocks.SHORT_GRASS`, `Blocks.ANDESITE_WALL`,
   `Blocks.DEEPSLATE_TILE_SLAB`, `Blocks.WHITE_TERRACOTTA`, ...) - noms a priori corrects pour
   1.21.1 mais jamais utilisés ailleurs dans le code existant.

## Installation

1. Compiler (`./gradlew build`) ou récupérer le `.jar`.
2. Installer NeoForge `21.1.176` pour Minecraft 1.21.1.
3. Copier le `.jar` dans le dossier `mods/` de l'instance.

## Touches

| Touche par défaut | Action |
|---|---|
| `K` | Ouvre le menu Héros (roster + détail de la questline) |
| `V` | Utilise la capacité active du héros (ex. Sens du détective pour Batman) |

Les deux touches sont reconfigurables dans **Options > Contrôles > Hero's Journey**.

## Déroulé de test rapide de la questline Batman

1. Lancer le jeu, ouvrir le menu `K`, cliquer sur "Batman (Nolan)" puis "Activer ce héros".
2. `/heroesjourney progress <joueur> batman_nolan <N>` permet de sauter directement à l'étape `N`
   (0-indexé : 0 = quête 1 "tombes", 6 = quête 7 "train") sans avoir à tout rejouer — voir la
   section commandes ci-dessous.
3. Trouver/générer (ou `/locate structure heroesjourney:wayne_manor`, qui renvoie les coordonnées
   du centre du manoir) un domaine Wayne, entrer dans le petit cimetière familial à l'arrière du
   jardin (repère : ~28 blocs derrière/au nord du manoir, en dehors du bâtiment) et s'approcher à
   moins de 6 blocs des tombes → quête 1 validée, une carte vers la prison est donnée. Voir
   "Le domaine Wayne Manor" plus bas pour le détail du plan et des coordonnées relatives.
4. Prison : parler aux deux prisonniers (un ment, un dit vrai), ouvrir le bon coffre pour récupérer
   la clé, s'approcher de la sortie (torche près de la porte) → Sens du détective débloqué.
5. Monastère : parler à Ken → briefing d'entraînement (30 kills à mains nues, 3000 blocs en sprint,
   15 sneak-attacks). Une fois les trois faites, reparler à Ken → dialogue "Le refus" → choisir "Je
   refuse" → Ken devient un boss. Le vaincre débloque les pouvoirs passifs et les recettes
   d'armure/gadgets.
6. Crafter la cagoule, le plastron de combat + la cape (assemblés en plastron caparaçonné), les
   jambières et les bottes ; les porter simultanément → quête 5 validée, kit de départ reçu.
7. Trouver/générer l'Asile d'Arkham, vaincre Scarecrow → carte vers le train.
8. Train de Gotham : vaincre Henri/Ra's al Ghul → le Manoir Wayne n'est plus protégé pour ce
   joueur, arc terminé (badge visible dans le menu).

## Commande de debug admin

```
/heroesjourney progress <joueur> <hero> <étape>     # force l'étape courante (0-indexé)
/heroesjourney activate <joueur> <hero>              # active un héros instantanément
/heroesjourney unlockability <joueur> <ability>      # débloque une capacité (ex: detective_sense)
/heroesjourney listheroes                            # liste les héros enregistrés
```
`hero` = `batman_nolan` pour cet arc.

## Configuration

Toutes les valeurs d'équilibrage (seuils d'entraînement, stats des boss, délai de respawn, rayon
du Sens du détective, portée du grappin, réduction de dégâts de chute, etc.) sont dans
`config/heroesjourney-common.toml`, généré au premier lancement. La **rareté et le biome des
structures**, en revanche, sont réglés dans les fichiers datapack
`data/heroesjourney/worldgen/structure_set/*.json` (spacing/separation) et
`data/heroesjourney/worldgen/structure/*.json` (biomes) — c'est la seule façon standard de faire
du placement de structures rechargeable sans recompiler.

---

## Le domaine Wayne Manor

Contrairement aux 4 autres structures (prison, monastère, asile, train), qui utilisent toutes le
système générique "boîte simple" (`HeroBuildingStructure`/`HeroBuildingPiece`), le Manoir Wayne a
sa **propre structure dédiée** (`com.heroesjourney.structure.wayne.WayneManorStructure` /
`WayneManorPiece`) : un domaine complet d'environ **90x90 blocs**, construit intégralement en
code (pas de gabarit NBT), avec :

- Un **manoir** de 35x25 au sol, 2 étages + attique, hall d'entrée en double hauteur avec grand
  escalier et galerie balustrée à l'étage, bibliothèque, salle à manger, salon, cuisine/office
  côté rez-de-chaussée ; chambre de Bruce + 3 autres chambres et une salle de bain à l'étage ;
  tourelle/avant-corps central en façade, toit en croupe (hip roof) à degrés en ardoise
  (`deepslate tiles`), cheminées, fenêtres à meneaux en verre.
- Un **jardin** clos sur tout le pourtour par une **grille "noble"** (piliers en `stone brick
  wall` + lanternes tous les 5 blocs, reliés par des `iron bars`), avec un **portail** plus large
  et plus orné dans l'axe de l'allée.
- Une **allée** en andesite bordée de topiaires, du portail jusqu'au perron d'entrée.
- Un **cimetière familial** dans un enclos séparé à l'arrière du jardin (muret bas + entrée),
  avec les tombes de Thomas et Martha Wayne côte à côte, un arbre isolé et un peu de végétation.

Le terrain est entièrement **nivelé à plat** avant construction (voir "simplifié" ci-dessous), et
la zone de protection (voir plus haut) couvre désormais toute cette emprise de 90x90, pas
seulement le bâtiment.

### Repères / coordonnées (relatives à l'origine du domaine, coin sud-ouest au niveau du sol)

- `/locate structure heroesjourney:wayne_manor` renvoie les coordonnées du **centre du manoir**
  (pas du coin du domaine) — c'est le repère le plus pratique pour s'y téléporter.
- Le manoir occupe `x: 27 à 61`, `z: 38 à 62` (relatif à l'origine du domaine) ; l'entrée
  principale est au sud (côté portail/allée), sur la face `z = 38`.
- Le cimetière est à `x: 8 à 26`, `z: 66 à 84` — donc au nord-ouest du manoir, à l'écart du
  bâtiment mais dans l'enceinte. Les deux tombes sont au centre de cet enclos.
- Le portail principal est au sud du domaine (`z` proche de 2), dans l'axe de l'allée qui mène
  au perron du manoir.

Pour retester la quête 1 rapidement sans chercher le manoir : `/heroesjourney progress <joueur>
batman_nolan 0` remet le joueur à l'étape 1, puis `/locate structure heroesjourney:wayne_manor`
donne les coordonnées à côté desquelles se téléporter (`/tp`), et il suffit de marcher jusqu'au
cimetière (nord-ouest du manoir, à l'écart du bâtiment).

## Ce qui est fonctionnel

- **Architecture générique complète** : registre de héros (`HeroRegistry`), moteur de quêtes
  générique (`QuestStage`/`QuestObjective`/`QuestCondition`/`QuestReward`/`StageHook`), attachment
  `HeroData` persistant et synchronisé, système de dialogue générique (`DialogueTree`/NPC unique
  paramétré), boss générique respawnable avec bossbar et attribution de kill par dégâts récents,
  structures génériques (une seule classe `HeroBuildingStructure`/`HeroBuildingPiece` reparamétrée
  pour les 5 bâtiments).
- **GUI complet** : roster, détail de questline avec statut/compteurs par objectif, confirmation
  d'activation, tracker HUD optionnel, écran de dialogue.
- **Les 7 quêtes de l'arc Batman** sont câblées de bout en bout avec leurs récompenses réelles
  (carte, capacité, passifs, recettes, kit de départ, déblocage du manoir).
- **Protection du Manoir Wayne** par joueur (casse/pose/explosions/spawns) tant que la quête 7
  n'est pas validée - couvre désormais tout le domaine (90x90), pas juste le bâtiment.
- **Domaine Wayne Manor dédié** : manoir à 2 étages + attique avec pièces meublées, jardin clos
  d'une grille en pierre/fer forgé, allée à topiaires, cimetière familial séparé (voir plus haut) -
  pas encore compilé/testé en jeu, voir l'avertissement en haut du document.
- **Armure et gadgets** avec leurs mécaniques : cagoule (vision nocturne), plané de la cape en
  sneak+chute, réduction des dégâts de chute, bottes (vitesse), batarang boomerang avec
  ralentissement, grappin avec traction physique progressive, fumigène en zone.
- **Recettes verrouillées** : le craft physique est bloqué (pas seulement le carnet de recettes)
  tant que la quête 4 n'est pas terminée.
- **3 boss** avec attaques spéciales (téléportation + invocation pour Ken, attaque à distance +
  nuage de zone pour Scarecrow, cycle parade/vulnérabilité + escorte pour Henri).
- **Textures placeholder** générées pour tous les items, blocs, entités et l'armure (palette
  sombre/jaune cohérente), fichiers `fr_fr`/`en_us` complets.

## Ce qui est simplifié par rapport au cahier des charges

- **Structures autres que le Manoir Wayne** : prison, monastère, asile et train restent sur le
  système générique "boîte simple avec porte/fenêtres/toit plat" (pas de cellules détaillées, pas
  de train qui traverse plusieurs wagons distincts) — choix assumé de robustesse plutôt que 4
  structures ambitieuses risquant d'être bancales sans possibilité de test en jeu. Elles ne
  tournent pas (orientation fixe) pour la même raison. Le Manoir Wayne, lui, a maintenant sa
  propre structure dédiée et détaillée (voir "Le domaine Wayne Manor" plus haut) ; il ne tourne
  pas non plus (orientation fixe, façade toujours au sud) pour la même raison de robustesse.
- **Manoir Wayne - mobilier** : fauteuils/chaises simulés avec des `stairs`, table avec des
  `fence`+`pressure_plate`/`carpet`, cheminées avec une alcôve en pierre taillée + `lantern`
  plutôt qu'un vrai feu. Pas de lustre suspendu au-dessus du hall, pas de tableaux/`item_frame` -
  jugés trop risqués à placer correctement en code procédural (orientation, entités) pour le
  bénéfice visuel apporté ; à ajouter dans une itération future si souhaité.
- **Manoir Wayne - pierres tombales** : pas de texte gravé (`sign`) sur les tombes - l'API de
  configuration du texte d'un panneau par code n'a pas été jugée assez sûre à utiliser à l'aveugle
  ici ; la tombe reste identifiable par sa position (voir repères ci-dessus) et par son bloc
  marqueur dédié (`heroesjourney:wayne_grave_thomas`/`_martha`).
- **Manoir Wayne - terrain** : le domaine entier (90x90) est nivelé à plat avant construction
  (pas de suivi fin du relief) - un vrai adoucissement du terrain existant serait beaucoup plus
  complexe à coder correctement ; sur un biome plaine/prairie l'effet reste discret, mais un
  domaine généré à flanc de colline prononcée peut laisser une petite marche visible en bordure.
- **Manoir Wayne - portail** : ouverture symbolique encadrée de piliers plus hauts, sans vantail
  fonctionnel (pas de porte à ouvrir/fermer).
- **Carte au trésor** : au lieu d'une vraie carte Minecraft dessinée, l'objet reçu porte un nom et
  une description ("Direction : NE, ~800 blocs") calculés une fois au moment de la récompense,
  plutôt qu'un compas qui se met à jour en continu.
- **Sens du détective** : révèle les coffres proches par une pulsation de particules visibles
  seulement du joueur, plutôt qu'un contour (outline) shader sur les coffres.
- **Suppression des effets à la désactivation d'un héros** : les effets "à mains nues"
  (dégâts/vitesse d'attaque) et la réduction de détection sont instantanés (vérifiés en direct à
  chaque événement), mais les effets de potion (vision nocturne, vitesse, résistance) sont
  ré-appliqués par pulsations d'~1s et s'éteignent donc en 1-3s après un switch plutôt
  qu'instantanément à la frame près.
- **Illusions du Scarecrow** : rendues en particules ambiantes, pas en mobs fantômes distincts.
- **Prison** : l'énigme "qui ment, qui dit vrai" est représentée par deux PNJ aux indices
  contradictoires et un coffre réel/un coffre leurre, plutôt qu'un système d'indices combinables
  plus riche.
- **Pas de rotation des bâtiments**, pas de mobilier détaillé, pas de loot table dédiée (les
  coffres "décor" sont vides).
- **`gradle build` non vérifié** (voir l'avertissement en haut de ce document).

## Recommandations pour la prochaine itération, par priorité

1. **Compiler et corriger** les zones à risque listées plus haut ; lancer le jeu et vérifier en
   priorité le grappin et le plané de la cape (les deux mécaniques que le cahier des charges
   demande de soigner en premier).
2. **Tester le placement de structures en jeu** (`/locate structure heroesjourney:<id>`) et ajuster
   `getFirstFreeHeight`/l'orientation si les bâtiments spawnent mal encastrés dans le terrain.
3. Remplacer les textures placeholder par de vrais assets (la palette et l'organisation des
   fichiers sont prêtes pour ça).
4. Étoffer la prison (vrais indices multiples) et donner un vrai visuel de carte à `treasure_map`.
5. Ajouter la rotation des structures et des layouts plus élaborés (pièces multiples) une fois le
   système de base validé en jeu.
