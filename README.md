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

Le **Manoir Wayne** vient d'être **entièrement reconstruit une deuxième fois** : au lieu d'un
bâtiment généré procéduralement en Java (l'ancienne version, qui donnait un résultat visuel
insuffisant), il charge maintenant deux **gabarits NBT tout faits** que tu as fournis
(`data/heroesjourney/structure/wayne_manor.nbt` et `wayne_cemetery.nbt`) via le système vanilla
`TemplateStructurePiece` (celui utilisé par les Ruined Portals, igloos, ocean ruins, etc. - donc
le "coller un NBT dans le monde" en lui-même est du code vanilla éprouvé, pas quelque chose
d'écrit à la main ici). Ce qui EST neuf et **pas encore compilé** :
1. **`WayneManorPiece`/`WayneCemeteryPiece` (`structure/wayne/*.java`)** - héritent de
   `TemplateStructurePiece` avec un constructeur de désérialisation qui prend un
   `StructurePieceSerializationContext` et appelle `context.structureTemplateManager()`. C'est
   la zone à plus haut risque de ce round : je n'ai pas pu vérifier par compilation réelle que
   `structureTemplateManager()` existe bien sur `StructurePieceSerializationContext` avec ce nom
   exact (c'est la même méthode que les structures jigsaw vanilla utilisent pour charger leurs
   pools, donc probable, mais pas confirmé ici).
2. **`HJStructurePieceTypes`** - `WAYNE_MANOR`/`WAYNE_CEMETERY` sont maintenant enregistrés comme
   `StructurePieceType` "de base" (contexte + tag), pas `ContextlessType` comme `HERO_BUILDING` -
   à vérifier si le type de retour du lambda `WayneManorPiece::new` s'infère correctement.
3. **`WayneManorStructure#findGenerationPoint`** - place maintenant deux pièces
   (`WayneManorPiece` + `WayneCemeteryPiece`) dans le même `StructurePiecesBuilder`, avec un
   décalage fixe pour le cimetière (voir "Le domaine Wayne Manor" plus bas).
4. Les deux fichiers `.nbt` eux-mêmes : je les ai vérifiés comme des NBT gzip valides
   (DataVersion 3955 = 1.21.1) mais je ne peux pas garantir qu'ils ne contiennent aucun blockstate
   invalide (portes, lits, escaliers, connexions de vitres/murets) - Minecraft log une erreur par
   bloc invalide au chargement sans crasher, donc regarde les logs au premier chargement du monde
   si des blocs semblent manquants dans le bâtiment généré.

Recompile (`./gradlew build`) et relance avant de retester la quête 1 - voir "Le domaine Wayne
Manor" plus bas pour les repères en jeu.

**Sur le bug "deux bâtiments fusionnés"** que tu as signalé : le code de l'ancien manoir
procédural a été entièrement supprimé (plus aucune référence à `WayneManorPiece#buildGrave`, à
`BuildUtil`, ni à un `BuildingLayout` "wayne_manor" dans `BuildingRegistry` - une seule source de
génération existe maintenant). Mais si tu as déjà exploré/generé la zone en jeu avec un ancien
`.jar`, les chunks concernés sont **définitivement figés** avec l'ancien contenu - Minecraft ne
régénère jamais un chunk déjà généré, donc un nouveau `.jar` ne peut pas "effacer" l'ancien
bâtiment sur une zone déjà visitée. J'ai changé le `salt` du `structure_set` (741001 → 741002)
pour que le nouveau manoir vise une grille de chunks différente et ne retombe pas sur l'ancien
site par coïncidence, mais le vrai correctif est de **tester sur un monde neuf, ou dans une zone
jamais explorée** de ton monde actuel.

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
sa **propre structure dédiée** (`com.heroesjourney.structure.wayne.WayneManorStructure`), qui
place **deux gabarits NBT** (fournis par toi, pas générés en code) dans la même génération :

- **`wayne_manor.nbt`** (71x28x47, façade au sud) — le manoir lui-même.
- **`wayne_cemetery.nbt`** (15x6x11, entrée au sud) — le cimetière familial, placé automatiquement
  **15 blocs à l'est** du manoir (`CEMETERY_OFFSET_X = MANOR_SIZE_X + 15` dans
  `WayneManorStructure.java`), centré sur la même profondeur (`z`) que le manoir, au même niveau
  de sol.

Les deux gabarits partagent la même génération (`StructurePiecesBuilder`), donc la même règle de
rareté/biome et la même zone de protection.

### L'ancre de la quête 1 (tombes de Thomas et Martha Wayne)

`WayneCemeteryPiece#postProcess` pose deux blocs marqueurs du mod
(`heroesjourney:wayne_grave_thomas`/`_martha`) directement sur les deux stèles du gabarit
(positions locales `(5,1,3)` et `(9,1,3)` dans le template, comme convenu), calculés à partir de
`this.templatePosition` — c'est-à-dire la position **réelle** de la pièce en monde, fixée une
seule fois au moment de la génération. La condition de quête (`ProximityToBlockCondition` sur
`wayne_grave_thomas`, rayon 6) n'a donc plus aucun décalage codé en dur : elle cherche juste "ce
bloc à proximité", où qu'il ait été réellement posé.

### Repères / coordonnées

- `/locate structure heroesjourney:wayne_manor` renvoie les coordonnées du **centre du manoir**
  (calculé dans `WayneManorStructure#findGenerationPoint`, indépendant du coin du domaine) — c'est
  le repère le plus pratique pour s'y téléporter.
- Le cimetière est à l'**est** du manoir : depuis le centre du manoir, avance d'environ
  `71/2 + 15 + 15/2 ≈ 58` blocs vers l'est (`+x`) pour l'atteindre.
- Protection du domaine : couvre l'emprise du manoir + celle du cimetière, chacune avec une marge
  de 8 blocs tout autour (`PROTECTION_MARGIN` dans `WayneManorPiece`/`WayneCemeteryPiece`).

Pour retester la quête 1 rapidement : `/heroesjourney progress <joueur> batman_nolan 0` remet le
joueur à l'étape 1, puis `/locate structure heroesjourney:wayne_manor` donne les coordonnées à
côté desquelles se téléporter (`/tp`), et il suffit de marcher ~58 blocs vers l'est jusqu'au
cimetière.

**Rappel important** : teste ceci sur un **monde neuf** ou dans une **zone jamais explorée** — un
monde où tu as déjà généré/visité l'ancien manoir procédural avec un `.jar` précédent gardera ces
anciens chunks tels quels (voir l'avertissement en haut du document).

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
- **Domaine Wayne Manor dédié** : manoir + cimetière familial séparé chargés depuis tes gabarits
  NBT (`wayne_manor.nbt`/`wayne_cemetery.nbt`), placés ensemble dans la même génération, ancre de
  quête 1 calculée dynamiquement depuis la position réelle du cimetière (voir plus haut) -
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
  tournent pas (orientation fixe) pour la même raison. Le Manoir Wayne, lui, utilise maintenant
  tes gabarits NBT tout faits (voir "Le domaine Wayne Manor" plus haut) ; il ne tourne pas non
  plus (`Rotation.NONE`/`Mirror.NONE` fixes, façade toujours au sud) — activer la rotation
  demanderait de vérifier que le gabarit se recadre proprement, pas fait ici par prudence.
- **Manoir Wayne - pierres tombales** : pas de texte gravé (`sign`) sur les tombes du gabarit -
  la tombe reste identifiable par sa position (voir repères ci-dessus) et par le bloc marqueur
  dédié que le mod pose par-dessus (`heroesjourney:wayne_grave_thomas`/`_martha`).
- **Manoir Wayne - terrain** : aucun nivellement/adoucissement du terrain autour des gabarits
  (contrairement à l'ancienne version procédurale) - le placement suit la heightmap au centre de
  l'emprise, donc sur un terrain accidenté le manoir ou le cimetière peuvent légèrement flotter ou
  s'enfoncer sur les bords ; à corriger dans une itération future si besoin (adaptation de terrain
  `beard_thin` dans `worldgen/structure/wayne_manor.json` atténue déjà une partie de l'effet).
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
