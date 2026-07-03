# Hero's Journey — Arc "Batman Begins"

Mod NeoForge pour Minecraft **1.21.1** (Java 21). Premier arc d'un mod de super-héros à
questlines narratives : le joueur incarne Batman (version Nolan) via une progression en 7 quêtes,
sans craft d'armure "à effets" isolé.

## ⚠️ Important : ce build n'a pas pu être compilé dans l'environnement de développement

Le code a été écrit intégralement dans un environnement dont la politique réseau bloque
`maven.neoforged.net` (seul Maven Central était joignable) : **`gradle build` n'a donc pas pu être
exécuté ni vérifié ici**, et aucun `.jar` n'a pu être produit dans `build/libs/`. Tout le code a
été écrit avec le plus grand soin à partir du template officiel NeoForge 1.21.1 (MDK, NeoForge
`21.1.176`, plugin ModDevGradle `2.0.91`, tiré du dépôt `neoforged/MDK` pour fiabiliser les
versions et l'API), mais **il doit être compilé et testé en jeu avant tout usage**.

Pour compiler :
```bash
./gradlew build
```
Le `.jar` apparaîtra dans `build/libs/heroesjourney-0.1.0.jar`.

### Zones à risque si la compilation échoue en premier

Par ordre de probabilité décroissante :
1. **`HeroBuildingStructure#findGenerationPoint`** (`structure/HeroBuildingStructure.java`) — le
   calcul de hauteur via `ChunkGenerator#getFirstFreeHeight` est la partie de l'API worldgen la
   moins stable d'une version à l'autre.
2. **Armures custom** (`item/armor/BatSuitArmorMaterial.java`, `HJItems`) — l'API `ArmorMaterial`
   a été réécrite plusieurs fois entre 1.20.5 et 1.21.x.
3. **Réseau** (`network/*Payload.java`) — l'API `StreamCodec`/`RegisterPayloadHandlersEvent` est
   récente ; vérifier en premier si des erreurs de generics apparaissent côté `network`.
4. **Événements NeoForge** avec des noms très proches (`LivingDamageEvent.Pre`,
   `LivingChangeTargetEvent`, `MobSpawnEvent.PositionCheck`, `ExplosionEvent.Detonate`) dans
   `content/batman/BatmanCombatHandler.java` et `WayneManorProtection.java`.
5. **`BlockEntity`/`StructurePiece` NBT** (`saveAdditional`/`loadAdditional` signatures).

Aucune de ces zones ne remet en cause l'architecture générique ; ce sont des points d'API isolés,
faciles à corriger fichier par fichier une fois les vraies erreurs de compilation connues.

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
3. Trouver/générer (ou `/locate structure heroesjourney:wayne_manor`) un Manoir Wayne, s'approcher
   à moins de 5 blocs des tombes → quête 1 validée, une carte vers la prison est donnée.
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
  n'est pas validée.
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

- **Structures** : un seul système générique de "boîte simple avec porte/fenêtres/toit plat" (pas
  de manoir à 2-3 étages avec bibliothèque, pas de cellules détaillées, pas de train qui traverse
  plusieurs wagons distincts) — choix assumé de robustesse ("une version simple qui marche")
  plutôt que 5 structures ambitieuses risquant d'être bancales sans possibilité de test en jeu.
  Les bâtiments ne tournent pas (orientation fixe) pour la même raison.
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
