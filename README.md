# Hero's Journey — arc "Batman : Origine"

Mod NeoForge pour Minecraft **1.21.1** (Java 21, NeoForge `21.1.176`). C'est le premier arc d'un
mod de super-héros à questlines narratives : le joueur incarne **Batman** à travers une progression
en **7 quêtes**, écrite pour coller au lore des comics (Année Un / origine classique), **sans
structure custom et sans PNJ parlant** - tout repose sur des mobs, stats et objets vanilla détournés
intelligemment. L'architecture (moteur de quête générique, registre de héros, système d'effets) est
volontairement générique : ajouter un futur héros = écrire une classe `content.<héros>` de plus,
sans toucher au moteur.

> ⚠️ **Aucune compilation possible depuis l'environnement de l'agent** (accès réseau restreint à
> `maven.neoforged.net`) - tout ce qui suit a été écrit et relu avec soin mais doit être compilé et
> testé en jeu chez toi (`./gradlew build`) avant d'être considéré fiable à 100%.

## Sommaire

- [Installation](#installation)
- [Touches](#touches)
- [Commandes admin/debug](#commandes-admindebug)
- [Le héros : Batman](#le-héros--batman)
- [La questline, quête par quête](#la-questline-quête-par-quête)
- [Effets et bonus (valeurs exactes)](#effets-et-bonus-valeurs-exactes)
- [Objets](#objets)
- [Recettes de craft](#recettes-de-craft)
- [Mobs et blocs utilisés](#mobs-et-blocs-utilisés)
- [Configuration complète](#configuration-complète)
- [Limitations connues](#limitations-connues)

## Installation

1. Compiler (`./gradlew build`) ou récupérer le `.jar` déjà construit.
2. Installer **NeoForge 21.1.176** pour **Minecraft 1.21.1**.
3. Copier le `.jar` du mod dans le dossier `mods/` de l'instance.

## Touches

| Touche par défaut | Action |
|---|---|
| `K` | Ouvre le menu Héros (liste des héros + détail de la questline en cours) |
| `V` | Utilise la capacité active du héros (ex : repérage des menaces, une fois débloquée) |

Les deux sont reconfigurables dans **Options > Contrôles > Hero's Journey**.

## Commandes admin/debug

Toutes sous `/heroesjourney`, réservées aux opérateurs (niveau de permission 2, comme les autres
commandes admin vanilla) :

| Commande | Effet |
|---|---|
| `/heroesjourney progress <joueur> <hero> <étape>` | Force l'étape courante de la questline (0 = première quête) |
| `/heroesjourney activate <joueur> <hero>` | Active un héros instantanément pour ce joueur |
| `/heroesjourney unlockability <joueur> <ability>` | Débloque une capacité (ex : `threat_glow`) sans passer par la quête |
| `/heroesjourney listheroes` | Liste les héros enregistrés et leur nombre d'étapes |
| `/heroesjourney stats <joueur>` | Affiche en jeu : héros actif, étape, flags/capacités, valeurs réelles des attributs (vitesse, dégâts, armure, résistance au recul), modificateurs actifs un par un, effets de potion, pièces d'armure portées, état sneak/sol/vélocité verticale, état du plané |
| `/heroesjourney resetattributes <joueur>` | Retire tout modificateur d'attribut du namespace du mod sur le joueur (toutes valeurs confondues), utile si un modificateur d'une ancienne version du mod restait accroché en sauvegarde |

`hero` = `batman` pour cet arc.

## Le héros : Batman

> *Devenez le Chevalier Noir de Gotham : un serment né d'un deuil, forgé par l'entraînement,
> l'enquête et la discipline.*

Accessible via le menu Héros (`K`). L'activer applique tous les bonus déjà débloqués par la
questline et retire ceux d'un héros précédent ; le désactiver (ou changer de héros) retire tout
instantanément, sans exception - rien ne doit rester actif quand Batman n'est pas le héros actif du
joueur, même brièvement.

## La questline, quête par quête

Chaque quête affiche, dans le menu Héros : son titre, son texte narratif (uniquement pour l'étape en
cours), ses objectifs avec la progression en direct, et - pour chaque objectif qui en accorde un -
sa récompense individuelle, en plus de la récompense de fin d'étape s'il y en a une. Tout ce texte
retourne à la ligne automatiquement à la largeur de l'écran.

### 1. Le Serment
> *Ce soir-là, en sortant du théâtre, mes parents ont croisé la mauvaise personne dans la mauvaise
> rue. Je n'ai rien pu faire. Je n'étais qu'un enfant, figé, incapable d'agir pendant qu'on leur
> arrachait la vie. Cette impuissance ne me quittera jamais - mais elle peut devenir autre chose. Un
> serment. Plus jamais je ne resterai immobile face à l'injustice.*

**Objectif :** être témoin de la mort d'un villageois tué par un mob hostile (jamais par le joueur,
une chute ou la lave), à moins de `ORIGIN_VILLAGER_WITNESS_RADIUS` blocs (18 par défaut). Démarre
automatiquement la questline dès que cet événement se produit - aucune récompense mécanique, c'est
le déclencheur du reste de l'arc.

### 2. Dans l'ombre
> *Avant les arts martiaux, avant le combat, il y a eu les leçons d'espionnage et de déguisement.
> Traverser une pièce sans un bruit, disparaître dans une foule, approcher une cible sans qu'elle ne
> sente jamais ma présence. Ce n'est pas la force qui m'a été enseignée en premier. C'est l'art de ne
> pas être vu.*

**Objectif :** approcher furtivement (accroupi, `Shift`) à moins de `STEALTH_APPROACH_RADIUS` blocs
(6) d'un mob hostile qui ne cible jamais le joueur, et tenir la position `STEALTH_APPROACH_CONSECUTIVE_SECONDS`
secondes d'affilée (3) - `STEALTH_APPROACH_COUNT` fois (1 pour l'instant, valeur de test). Chaque
mob ne compte qu'une seule fois (marqué sur son propre NBT persistant), pour empêcher le farm sur un
seul mob immobile.

**Récompense :** passif permanent - les mobs hostiles ont une chance réduite de cibler le joueur
(`MOB_DETECTION_RANGE_MULTIPLIER`, -30%).

### 3. Corps et discipline
> *Douze ans loin de Gotham. Cambridge, la Sorbonne, puis le monde entier - un tour du globe où
> chaque maître m'a poussé un peu plus loin que le précédent. Le FBI m'a paru trop lent, trop
> encadré ; j'ai abandonné après six semaines pour continuer seul. Le corps ne ment pas sur ce qu'on
> lui a fait subir. Le mien porte cette formation dans chaque muscle.*

Trois sous-objectifs **en parallèle**, chacun débloquant son propre passif dès qu'il est validé
individuellement, indépendamment des deux autres :

| Sous-objectif | Seuil actuel (config) | Récompense immédiate |
|---|---|---|
| Course (en sprint) | `RUN_TRAINING_DISTANCE` = 100 blocs | Bonus permanent de vitesse de déplacement |
| Saut | `JUMP_TRAINING_COUNT` = 1 saut | Réduction des dégâts de chute |
| Nage | `SWIM_TRAINING_DISTANCE` = 100 blocs | Bonus permanent de vitesse de nage |

**Récompense de fin d'étape** (les 3 sous-objectifs validés) : le **Carnet d'énigmes** est ajouté
automatiquement à l'inventaire.

### 4. Esprit
> *Criminologie, médecine légale, chimie, l'art du déguisement - j'ai étudié tout ce qui pouvait
> faire de moi un limier plutôt qu'un simple poing. Un criminel laisse toujours une trace, un
> raisonnement, une faille. Encore faut-il avoir appris à la voir avant lui.*

**Objectif :** résoudre les épreuves du Carnet d'énigmes (clic-droit avec l'objet en main) : un
**taquin** (reconstitution de preuve), un **crochetage**, un **coffre-fort** (Mastermind), toujours
dans cet ordre fixe et persistant (fermer le carnet et le rouvrir reprend exactement où on en était,
pas de remélange aléatoire). `PUZZLE_TARGET_COUNT` = 3, correspondant exactement aux 3 mini-jeux.

**Récompense :** capacité activable **"Repérage des menaces"** (touche `V`) - applique le vrai effet
vanilla *Glowing* (silhouette visible à travers les murs, comme les flèches spectrales) à tous les
mobs hostiles dans un rayon `THREAT_GLOW_RADIUS` (20 blocs) pendant `THREAT_GLOW_DURATION_TICKS`
(300 ticks = 15s), cooldown `THREAT_GLOW_COOLDOWN_TICKS` (600 ticks = 30s).

### 5. Le Combattant
> *Des maîtres d'arts martiaux d'Extrême-Orient à la Ligue des Assassins, j'ai appris à me battre
> auprès de ceux qui maîtrisaient la violence mieux que quiconque. Avec la Ligue, j'ai vu jusqu'où ce
> savoir pouvait mener - et j'ai choisi de garder les poings, sans jamais adopter leur volonté de
> tuer.*

**Objectif :** achever `MARTIAL_ARTS_KILLS` mobs hostiles (1 pour l'instant, valeur de test) au
corps à corps, le coup fatal devant être porté à mains nues (aucun objet en main).

**Récompense :** passif permanent - bonus **fixe et général** de dégâts (`MARTIAL_ARTS_DAMAGE_BONUS`
= +1,1, s'ajoute tel quel au-dessus des dégâts de n'importe quelle arme, poings compris - voir la
section Effets ci-dessous pour le détail du choix de conception).

### 6. Un signe dans la nuit
> *Assis dans le noir, à ressasser ce que j'allais devenir, j'ai entendu un battement d'ailes avant
> de le voir. Une chauve-souris, entrée par la fenêtre restée ouverte, qui a tourné une fois dans la
> pièce avant de reprendre son vol dans la nuit. Rien de plus qu'un animal égaré. Et pourtant, à cet
> instant précis, j'ai su exactement ce que j'allais devenir.*

**Objectif :** laisser une chauve-souris (mob vanilla) passer à moins de `BAT_PROXIMITY_RADIUS`
blocs (3).

**Récompense :** débloque les recettes des 4 pièces de l'armure (jusque-là physiquement bloquées au
craft, même en connaissant la recette).

### 7. Naissance d'un symbole
> *J'ai passé le plastron en dernier, après la cagoule, après les gants. Dans le reflet de la vitre,
> ce n'était plus mon visage. La ville en bas continuait de vivre sans savoir que quelque chose
> venait de changer dans l'obscurité au-dessus d'elle. Ce soir, Gotham cesse d'avoir un simple
> justicier : elle a un symbole.*

**Objectif :** crafter et porter simultanément les 4 pièces de l'armure (cagoule, plastron
caparaçonné, jambières, bottes).

**Récompense :** débloque les recettes des 3 gadgets + un kit de départ (1 grappin, 8 batarangs, 3
fumigènes). Chaque pièce de l'armure a son propre effet une fois équipée (vision nocturne avec la
cagoule, capacité de plané avec le plastron), et l'ensemble des 4 pièces confère en plus un bonus de
dégâts, d'armure et de résistance au recul (détails ci-dessous).

## Effets et bonus (valeurs exactes)

Toutes les valeurs ci-dessous sont lisibles/vérifiables en direct en jeu via
`/heroesjourney stats <joueur>`.

### Principe général de conception (important)

- **Tout bonus lié à Batman est conditionné au fait que Batman soit le héros actif du joueur, sans
  exception.** Changer de héros (ou désactiver Batman) retire tout instantanément - attributs et
  effets de statut compris, aucun n'est laissé à "s'user" tout seul.
- **Chaque bonus est implémenté comme un vrai modificateur d'attribut vanilla** (jamais un ajout de
  dégâts caché dans un event) quand c'est possible, précisément pour que
  `/heroesjourney stats` (et donc le joueur, indirectement) voie toujours la vraie valeur finale.
  Les deux seules exceptions sont la vision nocturne (aucun attribut vanilla équivalent n'existe) et
  le bonus de vitesse de nage (choisi en check-tick actif plutôt qu'un effet de statut, pour un
  réglage plus fin et un retrait plus instantané que Dolphin's Grace).
- **Les bonus de dégâts sont volontairement additifs et fixes** (opération `ADD_VALUE`, jamais un
  pourcentage) et **généraux** (n'importe quelle arme, poings compris) : l'idée initiale de rendre
  les poings nus compétitifs face à une épée a été abandonnée (le système de charge d'attaque de
  Minecraft, actif depuis la 1.9, ne s'y prête pas bien, et ce n'est de toute façon pas cohérent
  côté RP qu'une épée en netherite ne reste pas meilleure qu'un poing). Le design final est un simple
  petit coup de pouce aux dégâts du joueur, peu importe ce qu'il tient en main.

### Tableau des bonus

| Source | Effet | Valeur actuelle |
|---|---|---|
| Quête 2 (passif) | Un mob hostile a une chance de ne pas cibler le joueur | `MOB_DETECTION_RANGE_MULTIPLIER` : -30% de portée de détection |
| Quête 3a (passif) | Vitesse de déplacement permanente (attribut `movement_speed`, %) | `RUN_TRAINING_SPEED_BONUS` = +6,5% sans la tenue complète, `RUN_TRAINING_SPEED_BONUS_FULL_SUIT` = +4% avec (le poids de la tenue réduit légèrement le bonus, mais reste toujours strictement au-dessus de la vitesse vanilla de base) |
| Quête 3b (passif) | Réduction des dégâts de chute, cumulable avec celle des jambières | `JUMP_TRAINING_FALL_DAMAGE_REDUCTION` = -30% |
| Quête 3c (passif) | Vitesse de nage (attribut `water_movement_efficiency`, actif uniquement pendant que le joueur est dans l'eau, vérifié chaque tick) | `SWIM_TRAINING_SPEED_BONUS` = +0,8 sans la tenue complète, `SWIM_TRAINING_SPEED_BONUS_FULL_SUIT` = +0,6 avec |
| Quête 4 (capacité, touche `V`) | Glowing vanilla sur les mobs hostiles proches | Rayon 20 blocs, 15s, cooldown 30s |
| Quête 5 (passif) | Bonus **fixe** de dégâts, **peu importe l'arme en main** | `MARTIAL_ARTS_DAMAGE_BONUS` = **+1,1** |
| Cagoule (intrinsèque, tant que portée + Batman actif) | Vision nocturne permanente, retirée instantanément au retrait de la cagoule | - |
| Jambières (intrinsèque) | Réduction des dégâts de chute | `FALL_DAMAGE_REDUCTION` = -30% (indépendante de celle de la quête 3b, les deux se cumulent) |
| Plastron caparaçonné (intrinsèque) | Vrai vol plané façon élytre vanilla (voir ci-dessous) | - |
| Tenue complète (4 pièces + Batman actif) | Bonus **fixe** de dégâts, **peu importe l'arme en main** | `FULL_SUIT_DAMAGE_BONUS` = **+1,1** (cumulable avec le bonus de la quête 5 - un joueur entraîné et en tenue complète a donc **+2,2 dégâts fixes**, sur n'importe quelle arme) |
| Tenue complète (4 pièces + Batman actif) | Bonus de points d'armure | `FULL_SUIT_ARMOR_BONUS` = +2,0 |
| Tenue complète (4 pièces + Batman actif) | Résistance au recul | `FULL_SUIT_KNOCKBACK_RESISTANCE_BONUS` = +20% |
| Grappin | Portée / cooldown / accélération / vitesse max | 24 blocs / 2s / 0,18 bloc-tick² / 1,6 bloc/tick |
| Batarang | Dégâts / ralentissement (Lenteur II) au contact | 3,0 / 3s |
| Fumigène | Rayon / durée / aveuglement (Cécité I, réappliqué) | 6 blocs / 8s / 2s par pulsation (toutes les 5 ticks) |

**Exemples concrets de dégâts finaux** (attribut `attack_damage`, joueur entraîné arts martiaux +
tenue complète, soit +2,2 fixes) :

| Arme | Dégâts vanilla de base | Dégâts avec les 2 bonus actifs |
|---|---|---|
| Poings nus | 1,0 | **3,2** |
| Épée en bois | 4,0 | **6,2** |
| Épée en netherite | 8,0 | **10,2** |

(Les totaux évitent volontairement les nombres ronds, pour ne jamais tomber pile sur un multiple des
PV d'un mob courant.)

### Le plané (plastron caparaçonné)

Implémenté comme un **vrai vol plané façon élytre vanilla**, pas un effet bricolé :

- Le plastron déclare directement à Minecraft qu'il autorise le vol plané (point d'extension
  `canElytraFly`/`elytraFlightTick`), donc **toute la physique de vol** (piqué en regardant vers le
  bas, contrôle directionnel, décélération à l'atterrissage) est **exactement** celle d'une élytre
  vanilla - aucune vélocité custom recalculée à la main.
- **Démarrage :** une pression sur espace en l'air (en chute) déclenche le plané, qui persiste tout
  seul ensuite, sans avoir besoin de maintenir quoi que ce soit - identique à une vraie élytre.
- **Annulation manuelle :** s'accroupir (`Shift`) pendant que le plané est actif l'arrête
  immédiatement et fait retomber le joueur normalement - un comportement qui n'existe pas sur une
  élytre vanilla, ajouté spécifiquement pour ce mod (détection sur le *nouvel* appui, pas sur l'état
  déjà maintenu, pour ne pas annuler un plané qui vient de démarrer si le joueur était déjà accroupi
  juste avant de sauter).
- Conditions requises en continu : plastron caparaçonné porté, Batman actif - sinon le plané est
  simplement indisponible (le joueur retombe normalement).

## Objets

### Matériaux (non équipables)

| Objet | Description |
|---|---|
| Fibre de Kevlar | Matériau de base de toute la tenue |
| Tissu à mémoire de forme | Matériau de la cape |
| Composant électronique | Matériau de la cagoule et du grappin |
| Plastron de combat | Pièce intermédiaire, sert à crafter le plastron caparaçonné |
| Cape | Pièce intermédiaire, sert à crafter le plastron caparaçonné |

### Armure équipable

Matériau custom `BatSuitArmorMaterial`, positionné entre le fer et le diamant, réparable avec de la
fibre de Kevlar, enchantabilité 12, ténacité 1,5, son d'équipement identique au fer.

| Pièce | Défense (points d'armure) | Durabilité de base |
|---|---|---|
| Cagoule (casque) | 3 | 18 (× multiplicateur casque vanilla) |
| Plastron caparaçonné | 7 | 18 (× multiplicateur plastron) - rareté "Rare" |
| Jambières | 6 | 18 (× multiplicateur jambières) |
| Bottes | 3 | 18 (× multiplicateur bottes) |

Total : 19 points d'armure pour la tenue complète (hors bonus "tenue complète" de +2 supplémentaires).

### Gadgets

| Objet | Comportement |
|---|---|
| Grappin | Tire le joueur vers le point visé (tir en ligne droite, portée 24 blocs) ; une traînée de particules (End Rod) visualise le câble pendant la traction ; s'annule en s'accroupissant (nouvel appui) ou après 5 secondes, ou dès l'arrivée |
| Batarang | Lancé avec une trajectoire en arc (gravité, comme une boule de neige) ; inflige des dégâts + ralentissement au contact d'un mob et est alors consommé ; s'il touche un bloc, se pose au sol et reste ramassable (jusqu'à 5 minutes avant de disparaître) |
| Fumigène | Lancé, crée un nuage de fumée au sol qui aveugle et fait perdre leur cible à tout ce qui s'y trouve, y compris le lanceur |
| Carnet d'énigmes | Non craftable - reçu automatiquement après la quête 3 ; clic-droit pour lancer le mini-jeu suivant de la séquence |

## Recettes de craft

Légende : **[Q6]** = physiquement impossible à crafter tant que la quête 6 n'est pas terminée
(la recette ne produit rien même si le joueur possède les bonnes cases remplies). **[Q7]** = pareil
mais pour la quête 7.

### Matériaux et intermédiaires (sans forme - peu importe l'emplacement dans la grille)

| Objet | Ingrédients | Résultat |
|---|---|---|
| Fibre de Kevlar | 4x Ficelle + 4x Lingot de fer + 1x Laine noire | x4 |
| Tissu à mémoire de forme | 4x Fibre de Kevlar + 4x Membrane de phantom + 1x Redstone | x4 |
| Composant électronique | 2x Redstone + 1x Lingot de fer + 1x Pépite d'or | x2 |

### Armure (recettes avec forme, gabarits vanilla standards)

**Plastron de combat** (gabarit plastron standard, 8 cases) :
```
[Kevlar][ vide ][Kevlar]
[Kevlar][Kevlar][Kevlar]
[Kevlar][Kevlar][Kevlar]
```

**Cape** (6 tissus à mémoire de forme, disposition "col + épaules") :
```
[ vide ][Tissu ][ vide ]
[Tissu ][Tissu ][Tissu ]
[Tissu ][ vide ][Tissu ]
```

**Plastron caparaçonné [Q6]** (sans forme - 1x Cape + 1x Plastron de combat)

**Cagoule [Q6]** (gabarit casque standard, composant électronique au centre) :
```
[Kevlar][Kevlar][Kevlar]
[Kevlar][Électro][Kevlar]
```

**Jambières [Q6]** (gabarit jambières standard, 7 cases) :
```
[Kevlar][Kevlar][Kevlar]
[Kevlar][ vide ][Kevlar]
[Kevlar][ vide ][Kevlar]
```

**Bottes [Q6]** (gabarit bottes standard, 4 cases) :
```
[Kevlar][ vide ][Kevlar]
[Kevlar][ vide ][Kevlar]
```

### Gadgets et divers (sans forme)

| Objet | Ingrédients | Résultat |
|---|---|---|
| Batarang [Q7] | 3x Lingot de fer + 1x Teinture noire | x4 |
| Grappin [Q7] | 2x Composant électronique + 3x Lingot de fer + 2x Ficelle + 1x Piston | x1 |
| Fumigène [Q7] | 2x Poudre à canon + 1x Charbon + 1x Bouteille en verre | x1 |

## Mobs et blocs utilisés

Aucun mob, entité ou structure custom n'est nécessaire pour progresser dans la questline - tout
repose sur des éléments 100% vanilla :

- **Villageois** (quête 1) : doit mourir de la main d'un mob hostile (jamais du joueur) à proximité.
- **Mobs hostiles** (`Enemy`, toute la questline) : cible des approches furtives (quête 2), des
  combats à mains nues (quête 5), du repérage des menaces (quête 4).
- **Chauve-souris** (`minecraft:bat`, quête 6) : doit simplement passer à proximité du joueur.
- **Bloc phantom membrane / laine noire / redstone / poudre à canon / etc.** : uniquement comme
  ingrédients de craft, aucune modification de leur comportement vanilla.

## Configuration complète

Toutes les valeurs ci-dessus (et plus) sont dans `config/heroesjourney-common.toml`, généré
automatiquement au premier lancement à partir des valeurs par défaut listées dans `HJConfig.java` -
ajustables sans recompiler. **Plusieurs seuils de quêtes sont volontairement réduits pour faciliter
les tests** (marqué dans leur commentaire respectif) :

| Config | Valeur actuelle | Notes |
|---|---|---|
| `origin.villagerWitnessRadius` | 18 | |
| `stealth.approachCount` | **1** (test) | |
| `stealth.approachRadius` | 6,0 | |
| `stealth.consecutiveSeconds` | 3 | |
| `training.runDistanceBlocks` | **100** (test) | |
| `training.jumpCount` | **1** (test) | |
| `training.swimDistanceBlocks` | **100** (test) | |
| `training.swimSpeedBonus` / `...FullSuit` | 0,8 / 0,6 | |
| `training.runSpeedBonus` / `...FullSuit` | 0,065 / 0,04 | |
| `training.jumpFallDamageReduction` | 0,3 | |
| `puzzle.targetCount` | 3 | valeur définitive, correspond aux 3 mini-jeux fixes |
| `puzzle.mastermindCodeLength` / `mastermindSymbolCount` | 4 / 6 | |
| `puzzle.lockpickPinCount` | 3 | |
| `puzzle.lockpickInitialZoneWidthPercent` / `lockpickMinZoneWidthPercent` | 30 / 10 | |
| `threatGlow.radius` / `durationTicks` / `cooldownTicks` | 20 / 300 / 600 | |
| `martialArts.kills` | **1** (test) | |
| `martialArts.damageBonus` | 1,1 | fixe, général (toute arme) |
| `stealth.mobDetectionRangeMultiplier` | 0,7 | |
| `bat.proximityRadius` | 3,0 | |
| `armor.fallDamageReduction` | 0,3 | |
| `armor.fullSuitDamageBonus` | 1,1 | fixe, général (toute arme) |
| `armor.fullSuitArmorBonus` | 2,0 | |
| `armor.fullSuitKnockbackResistanceBonus` | 0,2 | |
| `gadgets.grappleRange` / `grappleCooldownTicks` | 24 / 40 | |
| `gadgets.grapplePullAcceleration` / `grappleMaxSpeed` | 0,18 / 1,6 | |
| `gadgets.smokeDurationTicks` / `smokeRadius` | 160 / 6,0 | |
| `gadgets.batarangSlowDurationTicks` | 60 | |

## Limitations connues

- **Aucun `./gradlew build` n'a pu être exécuté depuis l'environnement de développement** (réseau
  restreint) - à valider chez toi avant toute mise en production.
- **Texture de l'armure** : les fichiers `bat_suit_layer_1.png`/`_layer_2.png` sont une recoloration
  (gris très foncé) du gabarit de l'armure en cuir vanilla, pour garantir un alignement UV correct
  (l'ancien placeholder ne couvrait que quelques pixels épars et donnait un rendu "pièces flottantes
  déconnectées"). C'est une base fonctionnelle et bien alignée, mais un vrai passage d'un·e
  artiste reste nécessaire pour un rendu détaillé final.
- **Seuils de test** : plusieurs compteurs de quête (approche furtive, course, saut, nage, kills à
  mains nues) sont réduits pour faciliter les essais - voir le tableau de configuration ci-dessus.
