package com.heroesjourney.structure.wayne;

import com.heroesjourney.item.HJItems;
import com.heroesjourney.structure.BuildUtil;
import com.heroesjourney.structure.HJStructurePieceTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Builds the whole Wayne Manor estate: perimeter fence and gate, driveway, garden, family
 * cemetery, and the manor itself (two floors + attic void, double-height hall, five ground-floor
 * rooms, four first-floor rooms, hip roof, central frontispiece).
 * <p>
 * Everything is placed at fixed offsets from {@link #origin} (the estate's south-west ground
 * corner) - never from {@code RandomSource} - because {@link #postProcess} is called once per
 * chunk the structure overlaps as that chunk generates, potentially far apart in time, and needs
 * to place the exact same thing at the exact same spot regardless of call order.
 */
public class WayneManorPiece extends StructurePiece {

    // ---- Estate layout constants (local coordinates, origin = south-west ground corner) ----
    public static final int ESTATE_SIZE_X = 90;
    public static final int ESTATE_SIZE_Z = 90;

    public static final int MANOR_SIZE_X = 35;
    public static final int MANOR_SIZE_Z = 25;
    public static final int MANOR_ORIGIN_X = 27;
    public static final int MANOR_ORIGIN_Z = 38;

    private static final int GROUND_WALL_TOP = 5;   // ground floor walls: y 1..5
    private static final int FIRST_FLOOR_Y = 6;      // first floor slab / ground floor ceiling
    private static final int FIRST_WALL_TOP = 11;    // first floor walls: y 7..11
    private static final int ATTIC_FLOOR_Y = 12;      // attic slab / roof base
    private static final int ROOF_STEPS = 8;

    private static final int FENCE_INSET = 2;
    private static final int FENCE_HEIGHT = 3;

    private static final int CEMETERY_X0 = 8;
    private static final int CEMETERY_X1 = 26;
    private static final int CEMETERY_Z0 = 66;
    private static final int CEMETERY_Z1 = 84;

    private final BlockPos origin;

    public WayneManorPiece(StructurePieceType type, BlockPos origin, BoundingBox box) {
        super(type, 0, box);
        this.origin = origin;
    }

    public WayneManorPiece(CompoundTag tag) {
        super(HJStructurePieceTypes.WAYNE_MANOR.get(), tag);
        this.origin = new BlockPos(tag.getInt("OriginX"), tag.getInt("OriginY"), tag.getInt("OriginZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OriginX", origin.getX());
        tag.putInt("OriginY", origin.getY());
        tag.putInt("OriginZ", origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                             RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        levelGround(level, chunkBox);
        buildPerimeterFence(level, chunkBox);
        buildDriveway(level, chunkBox);
        buildGardenDecor(level, chunkBox);
        buildCemetery(level, chunkBox);
        buildManorShell(level, chunkBox);
        buildGroundFloor(level, chunkBox);
        buildFirstFloor(level, chunkBox);
        buildRoof(level, chunkBox);
    }

    // -----------------------------------------------------------------
    // World-coordinate helpers
    // -----------------------------------------------------------------

    private int wx(int x) {
        return origin.getX() + x;
    }

    private int wy(int y) {
        return origin.getY() + y;
    }

    private int wz(int z) {
        return origin.getZ() + z;
    }

    private void set(WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, BlockState state) {
        BuildUtil.set(level, chunkBox, wx(x), wy(y), wz(z), state);
    }

    private void fill(WorldGenLevel level, BoundingBox chunkBox, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        BuildUtil.fill(level, chunkBox, wx(x0), wy(y0), wz(z0), wx(x1), wy(y1), wz(z1), state);
    }

    private void hollow(WorldGenLevel level, BoundingBox chunkBox, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        BuildUtil.hollow(level, chunkBox, wx(x0), wy(y0), wz(z0), wx(x1), wy(y1), wz(z1), state);
    }

    // -----------------------------------------------------------------
    // Grounds
    // -----------------------------------------------------------------

    /** Grades the whole estate footprint to a flat plane so the garden/fence/driveway don't float or bury on rough terrain. */
    private void levelGround(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = -1; x <= ESTATE_SIZE_X; x++) {
            for (int z = -1; z <= ESTATE_SIZE_Z; z++) {
                set(level, chunkBox, x, 0, z, grass);
                for (int y = -1; y >= -5; y--) {
                    set(level, chunkBox, x, y, z, dirt);
                }
                for (int y = 1; y <= 24; y++) {
                    set(level, chunkBox, x, y, z, air);
                }
            }
        }
    }

    private void buildPerimeterFence(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState pier = Blocks.STONE_BRICK_WALL.defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState lantern = Blocks.LANTERN.defaultBlockState();

        int minX = FENCE_INSET;
        int maxX = ESTATE_SIZE_X - FENCE_INSET;
        int minZ = FENCE_INSET;
        int maxZ = ESTATE_SIZE_Z - FENCE_INSET;
        int gateMinX = MANOR_ORIGIN_X + MANOR_SIZE_X / 2 - 5;
        int gateMaxX = MANOR_ORIGIN_X + MANOR_SIZE_X / 2 + 4;

        // South wall (with driveway gate opening) and north wall.
        for (int x = minX; x <= maxX; x++) {
            boolean isGateGap = x >= gateMinX && x <= gateMaxX;
            if (!isGateGap) {
                buildFenceColumn(level, chunkBox, x, minZ, pier, bars, lantern);
            }
            buildFenceColumn(level, chunkBox, x, maxZ, pier, bars, lantern);
        }
        // East and west walls.
        for (int z = minZ; z <= maxZ; z++) {
            buildFenceColumn(level, chunkBox, minX, z, pier, bars, lantern);
            buildFenceColumn(level, chunkBox, maxX, z, pier, bars, lantern);
        }
        // Tall ornate gate piers flanking the driveway opening.
        buildGatePier(level, chunkBox, gateMinX - 1, minZ, pier, lantern);
        buildGatePier(level, chunkBox, gateMaxX + 1, minZ, pier, lantern);
    }

    private void buildFenceColumn(WorldGenLevel level, BoundingBox chunkBox, int x, int z, BlockState pier, BlockState bars, BlockState lantern) {
        boolean isPier = ((x + z) % 5 == 0);
        if (isPier) {
            fill(level, chunkBox, x, 1, z, x, FENCE_HEIGHT, z, pier);
            set(level, chunkBox, x, FENCE_HEIGHT + 1, z, lantern);
        } else {
            fill(level, chunkBox, x, 1, z, x, FENCE_HEIGHT, z, bars);
        }
    }

    private void buildGatePier(WorldGenLevel level, BoundingBox chunkBox, int x, int z, BlockState pier, BlockState lantern) {
        fill(level, chunkBox, x, 1, z, x, FENCE_HEIGHT + 3, z, pier);
        set(level, chunkBox, x, FENCE_HEIGHT + 4, z, lantern);
    }

    private void buildDriveway(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState path = Blocks.ANDESITE.defaultBlockState();
        BlockState edge = Blocks.STONE_BRICK_SLAB.defaultBlockState();
        int centerX = MANOR_ORIGIN_X + MANOR_SIZE_X / 2;
        int pathHalfWidth = 3;
        int startZ = FENCE_INSET + 1;
        int endZ = MANOR_ORIGIN_Z;
        fill(level, chunkBox, centerX - pathHalfWidth, 0, startZ, centerX + pathHalfWidth, 0, endZ, path);
        for (int z = startZ; z <= endZ; z++) {
            set(level, chunkBox, centerX - pathHalfWidth - 1, 0, z, edge);
            set(level, chunkBox, centerX + pathHalfWidth + 1, 0, z, edge);
        }
        // Topiary trees every 6 blocks along both sides of the driveway.
        for (int z = startZ + 2; z < endZ - 2; z += 6) {
            buildTopiary(level, chunkBox, centerX - pathHalfWidth - 3, z);
            buildTopiary(level, chunkBox, centerX + pathHalfWidth + 3, z);
        }
    }

    private void buildTopiary(WorldGenLevel level, BoundingBox chunkBox, int x, int z) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState();
        fill(level, chunkBox, x, 1, z, x, 2, z, log);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 3; dy <= 5; dy++) {
                    if (dy == 4 || (dx == 0 && dz == 0)) {
                        set(level, chunkBox, x + dx, dy, z + dz, leaves);
                    }
                }
            }
        }
        set(level, chunkBox, x, 6, z, leaves);
    }

    /** A handful of fixed-position garden trees and flower beds so the grounds don't read as an empty lawn. */
    private void buildGardenDecor(WorldGenLevel level, BoundingBox chunkBox) {
        int[][] oaks = {{6, 20}, {80, 22}, {8, 60}, {78, 55}, {50, 82}, {70, 78}, {12, 40}, {78, 40}};
        for (int[] pos : oaks) {
            buildOak(level, chunkBox, pos[0], pos[1]);
        }
        int[][] flowerBeds = {{20, 45}, {65, 45}, {30, 70}, {60, 68}};
        for (int[] pos : flowerBeds) {
            buildFlowerBed(level, chunkBox, pos[0], pos[1]);
        }
    }

    private void buildOak(WorldGenLevel level, BoundingBox chunkBox, int x, int z) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState();
        fill(level, chunkBox, x, 1, z, x, 4, z, log);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 4; dy <= 6; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) + Math.abs(dy - 5) <= 3) {
                        set(level, chunkBox, x + dx, dy, z + dz, leaves);
                    }
                }
            }
        }
    }

    private void buildFlowerBed(WorldGenLevel level, BoundingBox chunkBox, int x, int z) {
        BlockState[] flowers = {Blocks.POPPY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState()};
        int i = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(level, chunkBox, x + dx, 1, z + dz, flowers[i % flowers.length]);
                i++;
            }
        }
    }

    // -----------------------------------------------------------------
    // Cemetery
    // -----------------------------------------------------------------

    private void buildCemetery(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState wall = Blocks.ANDESITE_WALL.defaultBlockState();
        BlockState grass = Blocks.SHORT_GRASS.defaultBlockState();
        BlockState poppy = Blocks.POPPY.defaultBlockState();

        int gateX = (CEMETERY_X0 + CEMETERY_X1) / 2;
        for (int x = CEMETERY_X0; x <= CEMETERY_X1; x++) {
            if (x != gateX) {
                set(level, chunkBox, x, 1, CEMETERY_Z0, wall);
            }
            set(level, chunkBox, x, 1, CEMETERY_Z1, wall);
        }
        for (int z = CEMETERY_Z0; z <= CEMETERY_Z1; z++) {
            set(level, chunkBox, CEMETERY_X0, 1, z, wall);
            set(level, chunkBox, CEMETERY_X1, 1, z, wall);
        }

        int graveZ = (CEMETERY_Z0 + CEMETERY_Z1) / 2 + 2;
        int centerX = (CEMETERY_X0 + CEMETERY_X1) / 2;
        buildGrave(level, chunkBox, centerX - 2, graveZ, HJItems.WAYNE_GRAVE_THOMAS.get().defaultBlockState());
        buildGrave(level, chunkBox, centerX + 2, graveZ, HJItems.WAYNE_GRAVE_MARTHA.get().defaultBlockState());

        // A lone tree watching over the plot, and a little ground cover.
        buildOak(level, chunkBox, CEMETERY_X0 + 3, CEMETERY_Z1 - 3);
        for (int dx = -4; dx <= 4; dx += 2) {
            set(level, chunkBox, centerX + dx, 1, graveZ - 3, grass);
        }
        set(level, chunkBox, centerX, 1, graveZ + 2, poppy);
    }

    private void buildGrave(WorldGenLevel level, BoundingBox chunkBox, int x, int z, BlockState marker) {
        BlockState slab = Blocks.POLISHED_ANDESITE.defaultBlockState();
        BlockState headstone = Blocks.STONE_BRICK_WALL.defaultBlockState();
        fill(level, chunkBox, x - 1, 0, z - 1, x + 1, 0, z + 1, slab);
        set(level, chunkBox, x, 1, z, headstone);
        set(level, chunkBox, x, 2, z, headstone);
        // The marker block itself: this is what QuestObjective "visit_graves" scans for, so it
        // must always be the topmost/frontmost block of the grave, never buried under headstone.
        set(level, chunkBox, x, 1, z + 1, marker);
    }

    // -----------------------------------------------------------------
    // Manor shell
    // -----------------------------------------------------------------

    private void buildManorShell(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState wall = Blocks.POLISHED_ANDESITE.defaultBlockState();
        BlockState trim = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int x0 = MANOR_ORIGIN_X;
        int x1 = MANOR_ORIGIN_X + MANOR_SIZE_X - 1;
        int z0 = MANOR_ORIGIN_Z;
        int z1 = MANOR_ORIGIN_Z + MANOR_SIZE_Z - 1;

        // Foundation.
        fill(level, chunkBox, x0 - 1, -1, z0 - 1, x1 + 1, -1, z1 + 1, Blocks.STONE_BRICKS.defaultBlockState());
        // Floor slabs.
        fill(level, chunkBox, x0, 0, z0, x1, 0, z1, Blocks.SMOOTH_STONE.defaultBlockState());
        fill(level, chunkBox, x0, FIRST_FLOOR_Y, z0, x1, FIRST_FLOOR_Y, z1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        fill(level, chunkBox, x0, ATTIC_FLOOR_Y, z0, x1, ATTIC_FLOOR_Y, z1, Blocks.DARK_OAK_PLANKS.defaultBlockState());

        // Outer walls, both storeys, with corner pilasters in the trim block.
        hollow(level, chunkBox, x0, 1, z0, x1, GROUND_WALL_TOP, z1, wall);
        hollow(level, chunkBox, x0, FIRST_FLOOR_Y + 1, z0, x1, FIRST_WALL_TOP, z1, wall);
        for (int y = 1; y <= FIRST_WALL_TOP; y++) {
            if (y == FIRST_FLOOR_Y) {
                continue;
            }
            set(level, chunkBox, x0, y, z0, trim);
            set(level, chunkBox, x1, y, z0, trim);
            set(level, chunkBox, x0, y, z1, trim);
            set(level, chunkBox, x1, y, z1, trim);
        }

        // Windows: evenly spaced 1x2 openings on every long face, both floors.
        for (int x = x0 + 3; x < x1 - 2; x += 4) {
            buildWindow(level, chunkBox, x, 2, z0, glass, trim);
            buildWindow(level, chunkBox, x, 2, z1, glass, trim);
            buildWindow(level, chunkBox, x, FIRST_FLOOR_Y + 2, z0, glass, trim);
            buildWindow(level, chunkBox, x, FIRST_FLOOR_Y + 2, z1, glass, trim);
        }
        for (int z = z0 + 3; z < z1 - 2; z += 4) {
            buildWindowEastWest(level, chunkBox, x0, 2, z, glass, trim);
            buildWindowEastWest(level, chunkBox, x1, 2, z, glass, trim);
            buildWindowEastWest(level, chunkBox, x0, FIRST_FLOOR_Y + 2, z, glass, trim);
            buildWindowEastWest(level, chunkBox, x1, FIRST_FLOOR_Y + 2, z, glass, trim);
        }

        // Central frontispiece / tower: a projecting bay on the south (entrance) facade.
        int towerX0 = MANOR_ORIGIN_X + MANOR_SIZE_X / 2 - 5;
        int towerX1 = MANOR_ORIGIN_X + MANOR_SIZE_X / 2 + 4;
        int towerZ0 = z0 - 3;
        fill(level, chunkBox, towerX0, -1, towerZ0, towerX1, -1, z0 - 1, Blocks.STONE_BRICKS.defaultBlockState());
        fill(level, chunkBox, towerX0, 0, towerZ0, towerX1, 0, z0 - 1, Blocks.SMOOTH_STONE.defaultBlockState());
        hollow(level, chunkBox, towerX0, 1, towerZ0, towerX1, ATTIC_FLOOR_Y + 4, z0 - 1, wall);
        for (int y = 1; y <= ATTIC_FLOOR_Y + 4; y++) {
            set(level, chunkBox, towerX0, y, towerZ0, trim);
            set(level, chunkBox, towerX1, y, towerZ0, trim);
        }
        // Pointed cap on the tower.
        for (int s = 0; s < 5; s++) {
            fill(level, chunkBox, towerX0 + s, ATTIC_FLOOR_Y + 4 + 1 + s, towerZ0 + s, towerX1 - s, ATTIC_FLOOR_Y + 4 + 1 + s, z0 - 1 - s, Blocks.DEEPSLATE_TILES.defaultBlockState());
        }

        // Front entrance: double door opening, exterior stairs, flanking pilasters.
        int doorX0 = MANOR_ORIGIN_X + MANOR_SIZE_X / 2 - 1;
        int doorX1 = MANOR_ORIGIN_X + MANOR_SIZE_X / 2;
        fill(level, chunkBox, doorX0, 1, towerZ0, doorX1, 3, towerZ0, air);
        fill(level, chunkBox, doorX0 - 3, -1, towerZ0 - 3, doorX1 + 3, -1, towerZ0 - 1, Blocks.STONE_BRICK_STAIRS.defaultBlockState());
        set(level, chunkBox, doorX0 - 2, 0, towerZ0 - 1, trim);
        set(level, chunkBox, doorX1 + 2, 0, towerZ0 - 1, trim);
        fill(level, chunkBox, doorX0 - 2, 1, towerZ0 - 1, doorX0 - 2, 4, towerZ0 - 1, trim);
        fill(level, chunkBox, doorX1 + 2, 1, towerZ0 - 1, doorX1 + 2, 4, towerZ0 - 1, trim);

        // Service-wing chimneys (kitchen end of the building, north-east corner).
        buildChimney(level, chunkBox, x1 - 2, z1 - 2);
        buildChimney(level, chunkBox, x0 + 2, z1 - 2);
    }

    private void buildWindow(WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, BlockState glass, BlockState trim) {
        set(level, chunkBox, x, y, z, glass);
        set(level, chunkBox, x, y + 1, z, glass);
        set(level, chunkBox, x - 1, y - 1, z, trim);
        set(level, chunkBox, x + 1, y - 1, z, trim);
        set(level, chunkBox, x - 1, y + 2, z, trim);
        set(level, chunkBox, x + 1, y + 2, z, trim);
    }

    private void buildWindowEastWest(WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, BlockState glass, BlockState trim) {
        set(level, chunkBox, x, y, z, glass);
        set(level, chunkBox, x, y + 1, z, glass);
        set(level, chunkBox, x, y - 1, z - 1, trim);
        set(level, chunkBox, x, y - 1, z + 1, trim);
        set(level, chunkBox, x, y + 2, z - 1, trim);
        set(level, chunkBox, x, y + 2, z + 1, trim);
    }

    private void buildChimney(WorldGenLevel level, BoundingBox chunkBox, int x, int z) {
        fill(level, chunkBox, x, ATTIC_FLOOR_Y, z, x, ATTIC_FLOOR_Y + ROOF_STEPS + 6, z, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
    }

    private void buildRoof(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState slate = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState slateSlab = Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState();
        int x0 = MANOR_ORIGIN_X - 1;
        int x1 = MANOR_ORIGIN_X + MANOR_SIZE_X;
        int z0 = MANOR_ORIGIN_Z - 1;
        int z1 = MANOR_ORIGIN_Z + MANOR_SIZE_Z;

        for (int s = 0; s <= ROOF_STEPS; s++) {
            int rx0 = x0 + s;
            int rx1 = x1 - s;
            int rz0 = z0 + s;
            int rz1 = z1 - s;
            if (rx0 > rx1 || rz0 > rz1) {
                break;
            }
            int y = ATTIC_FLOOR_Y + s;
            // Eaves overhang: draw the ring for this step, one block wider than the step inset.
            fill(level, chunkBox, rx0, y, rz0, rx1, y, rz0, slate);
            fill(level, chunkBox, rx0, y, rz1, rx1, y, rz1, slate);
            fill(level, chunkBox, rx0, y, rz0, rx0, y, rz1, slate);
            fill(level, chunkBox, rx1, y, rz0, rx1, y, rz1, slate);
        }
        int capY = ATTIC_FLOOR_Y + ROOF_STEPS + 1;
        int capX0 = x0 + ROOF_STEPS;
        int capX1 = x1 - ROOF_STEPS;
        int capZ0 = z0 + ROOF_STEPS;
        int capZ1 = z1 - ROOF_STEPS;
        if (capX0 <= capX1 && capZ0 <= capZ1) {
            fill(level, chunkBox, capX0, capY, capZ0, capX1, capY, capZ1, slateSlab);
        }
    }

    // -----------------------------------------------------------------
    // Ground floor interior
    // -----------------------------------------------------------------

    private void buildGroundFloor(WorldGenLevel level, BoundingBox chunkBox) {
        int x0 = MANOR_ORIGIN_X + 1;
        int x1 = MANOR_ORIGIN_X + MANOR_SIZE_X - 2;
        int z0 = MANOR_ORIGIN_Z + 1;
        int z1 = MANOR_ORIGIN_Z + MANOR_SIZE_Z - 2;

        int hallX0 = MANOR_ORIGIN_X + 13;
        int hallX1 = MANOR_ORIGIN_X + 21;

        BlockState partition = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState carpet = Blocks.RED_CARPET.defaultBlockState();

        // Partition walls separating the hall from the side wings (with doorway gaps).
        for (int z = z0; z <= z1; z++) {
            boolean doorGap = z >= z0 + 5 && z <= z0 + 6;
            if (!doorGap) {
                fill(level, chunkBox, hallX0 - 1, 1, z, hallX0 - 1, GROUND_WALL_TOP - 1, z, partition);
                fill(level, chunkBox, hallX1 + 1, 1, z, hallX1 + 1, GROUND_WALL_TOP - 1, z, partition);
            }
        }

        // Hall floor: red carpet runner from the entrance.
        fill(level, chunkBox, hallX0, 0, z0, hallX1, 0, z0 + 12, carpet);

        // Grand staircase along the hall's east inner wall, leading up to the first-floor gallery.
        BlockState stairBlock = Blocks.DARK_OAK_STAIRS.defaultBlockState();
        for (int i = 0; i < GROUND_WALL_TOP + 1; i++) {
            int stairX = hallX1 - 1;
            int stairZ = z0 + 2 + i;
            fill(level, chunkBox, hallX0 + 1, 0, stairZ, stairX, i, stairZ, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            set(level, chunkBox, stairX, i + 1, stairZ, stairBlock);
        }

        buildLibrary(level, chunkBox, x0, z0, hallX0 - 2, z0 + 10);
        buildDiningRoom(level, chunkBox, hallX1 + 2, z0, x1, z0 + 10);
        buildSalon(level, chunkBox, x0, z0 + 12, hallX0 - 2, z1);
        buildKitchen(level, chunkBox, hallX1 + 2, z0 + 12, x1, z1);

        // Hall stays void up to the first floor slab to create the double-height space; punch
        // that opening through the ceiling slab laid down in buildManorShell().
        fill(level, chunkBox, hallX0, FIRST_FLOOR_Y, z0, hallX1, FIRST_FLOOR_Y, z0 + 13, air);
    }

    private void buildLibrary(WorldGenLevel level, BoundingBox chunkBox, int x0, int z0, int x1, int z1) {
        fill(level, chunkBox, x0, 0, z0, x1, 0, z1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        BlockState shelf = Blocks.BOOKSHELF.defaultBlockState();
        fill(level, chunkBox, x0, 1, z0, x1, 3, z0, shelf);
        fill(level, chunkBox, x0, 1, z0, x0, 3, z1, shelf);
        // Small hearth alcove on the far wall.
        set(level, chunkBox, x1, 1, z1 - 1, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        set(level, chunkBox, x1, 2, z1 - 1, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        set(level, chunkBox, x1 - 1, 1, z1 - 1, Blocks.LANTERN.defaultBlockState());
        // A reading table (fence "legs" + pressure plate "top") and two stair "armchairs".
        set(level, chunkBox, x0 + 4, 1, z0 + 4, Blocks.OAK_FENCE.defaultBlockState());
        set(level, chunkBox, x0 + 4, 2, z0 + 4, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
        set(level, chunkBox, x0 + 3, 1, z0 + 4, Blocks.DARK_OAK_STAIRS.defaultBlockState());
        set(level, chunkBox, x0 + 5, 1, z0 + 4, Blocks.DARK_OAK_STAIRS.defaultBlockState());
        set(level, chunkBox, x0 + 2, 1, z0 + 2, Blocks.CHEST.defaultBlockState());
    }

    private void buildDiningRoom(WorldGenLevel level, BoundingBox chunkBox, int x0, int z0, int x1, int z1) {
        fill(level, chunkBox, x0, 0, z0, x1, 0, z1, Blocks.POLISHED_ANDESITE.defaultBlockState());
        int midZ = (z0 + z1) / 2;
        // Long table: fence legs under a row of pressure plates, with stair chairs down both sides.
        fill(level, chunkBox, x0 + 2, 1, midZ, x1 - 2, 1, midZ, Blocks.OAK_FENCE.defaultBlockState());
        fill(level, chunkBox, x0 + 2, 2, midZ, x1 - 2, 2, midZ, Blocks.WHITE_CARPET.defaultBlockState());
        for (int x = x0 + 2; x <= x1 - 2; x += 2) {
            set(level, chunkBox, x, 1, midZ - 1, Blocks.DARK_OAK_STAIRS.defaultBlockState());
            set(level, chunkBox, x, 1, midZ + 1, Blocks.DARK_OAK_STAIRS.defaultBlockState());
        }
        // Buffet against the back wall.
        fill(level, chunkBox, x0 + 1, 1, z1, x1 - 1, 2, z1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
    }

    private void buildSalon(WorldGenLevel level, BoundingBox chunkBox, int x0, int z0, int x1, int z1) {
        fill(level, chunkBox, x0, 0, z0, x1, 0, z1, Blocks.POLISHED_ANDESITE.defaultBlockState());
        // Fireplace alcove.
        set(level, chunkBox, x0, 1, z0 + 2, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        set(level, chunkBox, x0, 2, z0 + 2, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        set(level, chunkBox, x0 + 1, 1, z0 + 2, Blocks.LANTERN.defaultBlockState());
        // Seating around a small rug.
        fill(level, chunkBox, x0 + 3, 0, z0 + 3, x0 + 6, 0, z0 + 6, Blocks.RED_CARPET.defaultBlockState());
        set(level, chunkBox, x0 + 2, 1, z0 + 4, Blocks.DARK_OAK_STAIRS.defaultBlockState());
        set(level, chunkBox, x0 + 7, 1, z0 + 4, Blocks.DARK_OAK_STAIRS.defaultBlockState());
    }

    private void buildKitchen(WorldGenLevel level, BoundingBox chunkBox, int x0, int z0, int x1, int z1) {
        fill(level, chunkBox, x0, 0, z0, x1, 0, z1, Blocks.SMOOTH_STONE.defaultBlockState());
        fill(level, chunkBox, x0 + 1, 1, z0 + 1, x0 + 4, 1, z0 + 1, Blocks.SMOKER.defaultBlockState());
        fill(level, chunkBox, x0 + 1, 1, z0 + 2, x0 + 4, 1, z0 + 2, Blocks.BARREL.defaultBlockState());
        set(level, chunkBox, x1 - 1, 1, z1 - 1, Blocks.CHEST.defaultBlockState());
        // Service corridor doorway back toward the dining room.
        fill(level, chunkBox, x0 - 1, 1, z0, x0 - 1, 2, z0 + 1, Blocks.AIR.defaultBlockState());
    }

    // -----------------------------------------------------------------
    // First floor interior
    // -----------------------------------------------------------------

    private void buildFirstFloor(WorldGenLevel level, BoundingBox chunkBox) {
        int x0 = MANOR_ORIGIN_X + 1;
        int x1 = MANOR_ORIGIN_X + MANOR_SIZE_X - 2;
        int z0 = MANOR_ORIGIN_Z + 1;
        int z1 = MANOR_ORIGIN_Z + MANOR_SIZE_Z - 2;
        int hallX0 = MANOR_ORIGIN_X + 13;
        int hallX1 = MANOR_ORIGIN_X + 21;

        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();

        // Gallery balustrade around the hall void.
        for (int x = hallX0; x <= hallX1; x++) {
            set(level, chunkBox, x, FIRST_FLOOR_Y + 1, MANOR_ORIGIN_Z + 14, fence);
        }
        for (int z = MANOR_ORIGIN_Z + 1; z <= MANOR_ORIGIN_Z + 14; z++) {
            set(level, chunkBox, hallX0, FIRST_FLOOR_Y + 1, z, fence);
            set(level, chunkBox, hallX1, FIRST_FLOOR_Y + 1, z, fence);
        }

        buildBedroom(level, chunkBox, x0, z0, hallX0 - 2, z0 + 10, Blocks.BLUE_BED.defaultBlockState());
        buildBedroom(level, chunkBox, hallX1 + 2, z0, x1, z0 + 10, Blocks.RED_BED.defaultBlockState());
        buildBedroom(level, chunkBox, x0, z0 + 12, hallX0 - 2, z1, Blocks.GREEN_BED.defaultBlockState());
        buildBathroom(level, chunkBox, hallX1 + 2, z0 + 12, x1, z0 + 17);
        buildBedroom(level, chunkBox, hallX1 + 2, z0 + 19, x1, z1, Blocks.PURPLE_BED.defaultBlockState());
    }

    private void buildBedroom(WorldGenLevel level, BoundingBox chunkBox, int x0, int z0, int x1, int z1, BlockState bed) {
        fill(level, chunkBox, x0, FIRST_FLOOR_Y, z0, x1, FIRST_FLOOR_Y, z1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        // Beds need a matching head/foot pair facing the same way, or they render broken.
        BlockState bedFoot = bed.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH).setValue(BlockStateProperties.BED_PART, BedPart.FOOT);
        BlockState bedHead = bed.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH).setValue(BlockStateProperties.BED_PART, BedPart.HEAD);
        set(level, chunkBox, x0 + 1, FIRST_FLOOR_Y + 1, z0 + 1, bedFoot);
        set(level, chunkBox, x0 + 1, FIRST_FLOOR_Y + 1, z0 + 2, bedHead);
        set(level, chunkBox, x0 + 3, FIRST_FLOOR_Y + 1, z0 + 1, Blocks.OAK_FENCE.defaultBlockState());
        set(level, chunkBox, x0 + 3, FIRST_FLOOR_Y + 2, z0 + 1, Blocks.LANTERN.defaultBlockState());
        set(level, chunkBox, x1 - 1, FIRST_FLOOR_Y + 1, z1 - 1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
    }

    private void buildBathroom(WorldGenLevel level, BoundingBox chunkBox, int x0, int z0, int x1, int z1) {
        fill(level, chunkBox, x0, FIRST_FLOOR_Y, z0, x1, FIRST_FLOOR_Y, z1, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        set(level, chunkBox, x0 + 1, FIRST_FLOOR_Y + 1, z0 + 1, Blocks.CAULDRON.defaultBlockState());
    }
}
