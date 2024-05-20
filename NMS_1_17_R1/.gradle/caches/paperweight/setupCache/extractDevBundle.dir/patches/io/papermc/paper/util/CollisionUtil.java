package io.papermc.paper.util;

import io.papermc.paper.voxel.AABBVoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class CollisionUtil {

    public static final double COLLISION_EPSILON = 1.0E-7;

    public static boolean isEmpty(final AABB aabb) {
        return (aabb.maxX - aabb.minX) < COLLISION_EPSILON && (aabb.maxY - aabb.minY) < COLLISION_EPSILON && (aabb.maxZ - aabb.minZ) < COLLISION_EPSILON;
    }

    public static boolean isEmpty(final double minX, final double minY, final double minZ,
                                  final double maxX, final double maxY, final double maxZ) {
        return (maxX - minX) < COLLISION_EPSILON && (maxY - minY) < COLLISION_EPSILON && (maxZ - minZ) < COLLISION_EPSILON;
    }

    public static AABB getBoxForChunk(final int chunkX, final int chunkZ) {
        double x = (double)(chunkX << 4);
        double z = (double)(chunkZ << 4);
        // use a bounding box bigger than the chunk to prevent entities from entering it on move
        return new AABB(x - 3*COLLISION_EPSILON, Double.NEGATIVE_INFINITY, z - 3*COLLISION_EPSILON,
            x + (16.0 + 3*COLLISION_EPSILON), Double.POSITIVE_INFINITY, z + (16.0 + 3*COLLISION_EPSILON), false);
    }

    /*
      A couple of rules for VoxelShape collisions:
      Two shapes only intersect if they are actually more than EPSILON units into each other. This also applies to movement
      checks.
      If the two shapes strictly collide, then the return value of a collide call will return a value in the opposite
      direction of the source move. However, this value will not be greater in magnitude than EPSILON. Collision code
      will automatically round it to 0.
     */

    public static boolean voxelShapeIntersect(final double minX1, final double minY1, final double minZ1, final double maxX1,
                                              final double maxY1, final double maxZ1, final double minX2, final double minY2,
                                              final double minZ2, final double maxX2, final double maxY2, final double maxZ2) {
        return (minX1 - maxX2) < -COLLISION_EPSILON && (maxX1 - minX2) > COLLISION_EPSILON &&
               (minY1 - maxY2) < -COLLISION_EPSILON && (maxY1 - minY2) > COLLISION_EPSILON &&
               (minZ1 - maxZ2) < -COLLISION_EPSILON && (maxZ1 - minZ2) > COLLISION_EPSILON;
    }

    public static boolean voxelShapeIntersect(final AABB box, final double minX, final double minY, final double minZ,
                                              final double maxX, final double maxY, final double maxZ) {
        return (box.minX - maxX) < -COLLISION_EPSILON && (box.maxX - minX) > COLLISION_EPSILON &&
               (box.minY - maxY) < -COLLISION_EPSILON && (box.maxY - minY) > COLLISION_EPSILON &&
               (box.minZ - maxZ) < -COLLISION_EPSILON && (box.maxZ - minZ) > COLLISION_EPSILON;
    }

    public static boolean voxelShapeIntersect(final AABB box1, final AABB box2) {
        return (box1.minX - box2.maxX) < -COLLISION_EPSILON && (box1.maxX - box2.minX) > COLLISION_EPSILON &&
               (box1.minY - box2.maxY) < -COLLISION_EPSILON && (box1.maxY - box2.minY) > COLLISION_EPSILON &&
               (box1.minZ - box2.maxZ) < -COLLISION_EPSILON && (box1.maxZ - box2.minZ) > COLLISION_EPSILON;
    }

    public static double collideX(final AABB target, final AABB source, final double source_move) {
        if (source_move == 0.0) {
            return 0.0;
        }

        if ((source.minY - target.maxY) < -COLLISION_EPSILON && (source.maxY - target.minY) > COLLISION_EPSILON &&
            (source.minZ - target.maxZ) < -COLLISION_EPSILON && (source.maxZ - target.minZ) > COLLISION_EPSILON) {
            if (source_move >= 0.0) {
                final double max_move = target.minX - source.maxX; // < 0.0 if no strict collision
                if (max_move < -COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.min(max_move, source_move);
            } else {
                final double max_move = target.maxX - source.minX; // > 0.0 if no strict collision
                if (max_move > COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.max(max_move, source_move);
            }
        }
        return source_move;
    }

    public static double collideY(final AABB target, final AABB source, final double source_move) {
        if (source_move == 0.0) {
            return 0.0;
        }

        if ((source.minX - target.maxX) < -COLLISION_EPSILON && (source.maxX - target.minX) > COLLISION_EPSILON &&
            (source.minZ - target.maxZ) < -COLLISION_EPSILON && (source.maxZ - target.minZ) > COLLISION_EPSILON) {
            if (source_move >= 0.0) {
                final double max_move = target.minY - source.maxY; // < 0.0 if no strict collision
                if (max_move < -COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.min(max_move, source_move);
            } else {
                final double max_move = target.maxY - source.minY; // > 0.0 if no strict collision
                if (max_move > COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.max(max_move, source_move);
            }
        }
        return source_move;
    }

    public static double collideZ(final AABB target, final AABB source, final double source_move) {
        if (source_move == 0.0) {
            return 0.0;
        }

        if ((source.minX - target.maxX) < -COLLISION_EPSILON && (source.maxX - target.minX) > COLLISION_EPSILON &&
            (source.minY - target.maxY) < -COLLISION_EPSILON && (source.maxY - target.minY) > COLLISION_EPSILON) {
            if (source_move >= 0.0) {
                final double max_move = target.minZ - source.maxZ; // < 0.0 if no strict collision
                if (max_move < -COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.min(max_move, source_move);
            } else {
                final double max_move = target.maxZ - source.minZ; // > 0.0 if no strict collision
                if (max_move > COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.max(max_move, source_move);
            }
        }
        return source_move;
    }

    public static AABB offsetX(final AABB box, final double dx) {
        return new AABB(box.minX + dx, box.minY, box.minZ, box.maxX + dx, box.maxY, box.maxZ, false);
    }

    public static AABB offsetY(final AABB box, final double dy) {
        return new AABB(box.minX, box.minY + dy, box.minZ, box.maxX, box.maxY + dy, box.maxZ, false);
    }

    public static AABB offsetZ(final AABB box, final double dz) {
        return new AABB(box.minX, box.minY, box.minZ + dz, box.maxX, box.maxY, box.maxZ + dz, false);
    }

    public static AABB expandRight(final AABB box, final double dx) { // dx > 0.0
        return new AABB(box.minX, box.minY, box.minZ, box.maxX + dx, box.maxY, box.maxZ, false);
    }

    public static AABB expandLeft(final AABB box, final double dx) { // dx < 0.0
        return new AABB(box.minX - dx, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, false);
    }

    public static AABB expandUpwards(final AABB box, final double dy) { // dy > 0.0
        return new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY + dy, box.maxZ, false);
    }

    public static AABB expandDownwards(final AABB box, final double dy) { // dy < 0.0
        return new AABB(box.minX, box.minY - dy, box.minZ, box.maxX, box.maxY, box.maxZ, false);
    }

    public static AABB expandForwards(final AABB box, final double dz) { // dz > 0.0
        return new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ + dz, false);
    }

    public static AABB expandBackwards(final AABB box, final double dz) { // dz < 0.0
        return new AABB(box.minX, box.minY, box.minZ - dz, box.maxX, box.maxY, box.maxZ, false);
    }

    public static AABB cutRight(final AABB box, final double dx) { // dx > 0.0
        return new AABB(box.maxX, box.minY, box.minZ, box.maxX + dx, box.maxY, box.maxZ, false);
    }

    public static AABB cutLeft(final AABB box, final double dx) { // dx < 0.0
        return new AABB(box.minX + dx, box.minY, box.minZ, box.minX, box.maxY, box.maxZ, false);
    }

    public static AABB cutUpwards(final AABB box, final double dy) { // dy > 0.0
        return new AABB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY + dy, box.maxZ, false);
    }

    public static AABB cutDownwards(final AABB box, final double dy) { // dy < 0.0
        return new AABB(box.minX, box.minY + dy, box.minZ, box.maxX, box.minY, box.maxZ, false);
    }

    public static AABB cutForwards(final AABB box, final double dz) { // dz > 0.0
        return new AABB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ + dz, false);
    }

    public static AABB cutBackwards(final AABB box, final double dz) { // dz < 0.0
        return new AABB(box.minX, box.minY, box.minZ + dz, box.maxX, box.maxY, box.minZ, false);
    }

    public static double performCollisionsX(final AABB currentBoundingBox, double value, final List<AABB> potentialCollisions) {
        for (int i = 0, len = potentialCollisions.size(); i < len; ++i) {
            final AABB target = potentialCollisions.get(i);
            value = collideX(target, currentBoundingBox, value);
        }

        return value;
    }

    public static double performCollisionsY(final AABB currentBoundingBox, double value, final List<AABB> potentialCollisions) {
        for (int i = 0, len = potentialCollisions.size(); i < len; ++i) {
            final AABB target = potentialCollisions.get(i);
            value = collideY(target, currentBoundingBox, value);
        }

        return value;
    }

    public static double performCollisionsZ(final AABB currentBoundingBox, double value, final List<AABB> potentialCollisions) {
        for (int i = 0, len = potentialCollisions.size(); i < len; ++i) {
            final AABB target = potentialCollisions.get(i);
            value = collideZ(target, currentBoundingBox, value);
        }

        return value;
    }

    public static Vec3 performCollisions(final Vec3 moveVector, AABB axisalignedbb, final List<AABB> potentialCollisions) {
        double x = moveVector.x;
        double y = moveVector.y;
        double z = moveVector.z;

        if (y != 0.0) {
            y = performCollisionsY(axisalignedbb, y, potentialCollisions);
            if (y != 0.0) {
                axisalignedbb = offsetY(axisalignedbb, y);
            }
        }

        final boolean xSmaller = Math.abs(x) < Math.abs(z);

        if (xSmaller && z != 0.0) {
            z = performCollisionsZ(axisalignedbb, z, potentialCollisions);
            if (z != 0.0) {
                axisalignedbb = offsetZ(axisalignedbb, z);
            }
        }

        if (x != 0.0) {
            x = performCollisionsX(axisalignedbb, x, potentialCollisions);
            if (!xSmaller && x != 0.0) {
                axisalignedbb = offsetX(axisalignedbb, x);
            }
        }

        if (!xSmaller && z != 0.0) {
            z = performCollisionsZ(axisalignedbb, z, potentialCollisions);
        }

        return new Vec3(x, y, z);
    }

    public static boolean addBoxesToIfIntersects(final VoxelShape shape, final AABB aabb, final List<AABB> list) {
        if (shape instanceof AABBVoxelShape) {
            final AABBVoxelShape shapeCasted = (AABBVoxelShape)shape;
            if (voxelShapeIntersect(shapeCasted.aabb, aabb) && !isEmpty(shapeCasted.aabb)) {
                list.add(shapeCasted.aabb);
                return true;
            }
            return false;
        } else if (shape instanceof ArrayVoxelShape) {
            final ArrayVoxelShape shapeCasted = (ArrayVoxelShape)shape;
            // this can be optimised by checking an "overall shape" first, but not needed

            final double offX = shapeCasted.getOffsetX();
            final double offY = shapeCasted.getOffsetY();
            final double offZ = shapeCasted.getOffsetZ();

            boolean ret = false;

            for (final AABB boundingBox : shapeCasted.getBoundingBoxesRepresentation()) {
                final double minX, minY, minZ, maxX, maxY, maxZ;
                if (voxelShapeIntersect(aabb, minX = boundingBox.minX + offX, minY = boundingBox.minY + offY, minZ = boundingBox.minZ + offZ,
                    maxX = boundingBox.maxX + offX, maxY = boundingBox.maxY + offY, maxZ = boundingBox.maxZ + offZ)
                    && !isEmpty(minX, minY, minZ, maxX, maxY, maxZ)) {
                    list.add(new AABB(minX, minY, minZ, maxX, maxY, maxZ, false));
                    ret = true;
                }
            }

            return ret;
        } else {
            final List<AABB> boxes = shape.toAabbs();

            boolean ret = false;

            for (int i = 0, len = boxes.size(); i < len; ++i) {
                final AABB box = boxes.get(i);
                if (voxelShapeIntersect(box, aabb) && !isEmpty(box)) {
                    list.add(box);
                    ret = true;
                }
            }

            return ret;
        }
    }

    public static void addBoxesTo(final VoxelShape shape, final List<AABB> list) {
        if (shape instanceof AABBVoxelShape) {
            final AABBVoxelShape shapeCasted = (AABBVoxelShape)shape;
            if (!isEmpty(shapeCasted.aabb)) {
                list.add(shapeCasted.aabb);
            }
        } else if (shape instanceof ArrayVoxelShape) {
            final ArrayVoxelShape shapeCasted = (ArrayVoxelShape)shape;

            final double offX = shapeCasted.getOffsetX();
            final double offY = shapeCasted.getOffsetY();
            final double offZ = shapeCasted.getOffsetZ();

            for (final AABB boundingBox : shapeCasted.getBoundingBoxesRepresentation()) {
                final AABB box = boundingBox.move(offX, offY, offZ);
                if (!isEmpty(box)) {
                    list.add(box);
                }
            }
        } else {
            final List<AABB> boxes = shape.toAabbs();
            for (int i = 0, len = boxes.size(); i < len; ++i) {
                final AABB box = boxes.get(i);
                if (!isEmpty(box)) {
                    list.add(box);
                }
            }
        }
    }

    public static boolean isAlmostCollidingOnBorder(final WorldBorder worldborder, final AABB boundingBox) {
        return isAlmostCollidingOnBorder(worldborder, boundingBox.minX, boundingBox.maxX, boundingBox.minZ, boundingBox.maxZ);
    }

    public static boolean isAlmostCollidingOnBorder(final WorldBorder worldborder, final double boxMinX, final double boxMaxX,
                                                    final double boxMinZ, final double boxMaxZ) {
        final double borderMinX = worldborder.getMinX(); // -X
        final double borderMaxX = worldborder.getMaxX(); // +X

        final double borderMinZ = worldborder.getMinZ(); // -Z
        final double borderMaxZ = worldborder.getMaxZ(); // +Z

        return
            // Not intersecting if we're smaller
            !voxelShapeIntersect(
                boxMinX + COLLISION_EPSILON, Double.NEGATIVE_INFINITY, boxMinZ + COLLISION_EPSILON,
                boxMaxX - COLLISION_EPSILON, Double.POSITIVE_INFINITY, boxMaxZ - COLLISION_EPSILON,
                borderMinX, Double.NEGATIVE_INFINITY, borderMinZ, borderMaxX, Double.POSITIVE_INFINITY, borderMaxZ
            )
            &&

            // Are intersecting if we're larger
            voxelShapeIntersect(
                boxMinX - COLLISION_EPSILON, Double.NEGATIVE_INFINITY, boxMinZ - COLLISION_EPSILON,
                boxMaxX + COLLISION_EPSILON, Double.POSITIVE_INFINITY, boxMaxZ + COLLISION_EPSILON,
                borderMinX, Double.NEGATIVE_INFINITY, borderMinZ, borderMaxX, Double.POSITIVE_INFINITY, borderMaxZ
            );
    }

    public static boolean isCollidingWithBorderEdge(final WorldBorder worldborder, final AABB boundingBox) {
        return isCollidingWithBorderEdge(worldborder, boundingBox.minX, boundingBox.maxX, boundingBox.minZ, boundingBox.maxZ);
    }

    public static boolean isCollidingWithBorderEdge(final WorldBorder worldborder, final double boxMinX, final double boxMaxX,
                                                    final double boxMinZ, final double boxMaxZ) {
        final double borderMinX = worldborder.getMinX() + COLLISION_EPSILON; // -X
        final double borderMaxX = worldborder.getMaxX() - COLLISION_EPSILON; // +X

        final double borderMinZ = worldborder.getMinZ() + COLLISION_EPSILON; // -Z
        final double borderMaxZ = worldborder.getMaxZ() - COLLISION_EPSILON; // +Z

        return boxMinX < borderMinX || boxMaxX > borderMaxX || boxMinZ < borderMinZ || boxMaxZ > borderMaxZ;
    }

    public static boolean getCollisionsForBlocksOrWorldBorder(final CollisionGetter getter, final Entity entity, final AABB aabb,
                                                              final List<AABB> into, final boolean loadChunks, final boolean collidesWithUnloaded,
                                                              final boolean checkBorder, final boolean checkOnly, final BiPredicate<BlockState, BlockPos> predicate) {
        boolean ret = false;

        if (checkBorder) {
            if (CollisionUtil.isAlmostCollidingOnBorder(getter.getWorldBorder(), aabb)) {
                if (checkOnly) {
                    return true;
                } else {
                    CollisionUtil.addBoxesTo(getter.getWorldBorder().getCollisionShape(), into);
                    ret = true;
                }
            }
        }

        int minBlockX = Mth.floor(aabb.minX - COLLISION_EPSILON) - 1;
        int maxBlockX = Mth.floor(aabb.maxX + COLLISION_EPSILON) + 1;

        int minBlockY = Mth.floor(aabb.minY - COLLISION_EPSILON) - 1;
        int maxBlockY = Mth.floor(aabb.maxY + COLLISION_EPSILON) + 1;

        int minBlockZ = Mth.floor(aabb.minZ - COLLISION_EPSILON) - 1;
        int maxBlockZ = Mth.floor(aabb.maxZ + COLLISION_EPSILON) + 1;

        final int minSection = WorldUtil.getMinSection(getter);
        final int maxSection = WorldUtil.getMaxSection(getter);
        final int minBlock = minSection << 4;
        final int maxBlock = (maxSection << 4) | 15;

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        CollisionContext collisionShape = null;

        // special cases:
        if (minBlockY > maxBlock || maxBlockY < minBlock) {
            // no point in checking
            return ret;
        }

        int minYIterate = Math.max(minBlock, minBlockY);
        int maxYIterate = Math.min(maxBlock, maxBlockY);

        int minChunkX = minBlockX >> 4;
        int maxChunkX = maxBlockX >> 4;

        int minChunkZ = minBlockZ >> 4;
        int maxChunkZ = maxBlockZ >> 4;

        ServerChunkCache chunkProvider;
        if (getter instanceof WorldGenRegion) {
            chunkProvider = null;
        } else if (getter instanceof ServerLevel) {
            chunkProvider = ((ServerLevel)getter).getChunkSource();
        } else {
            chunkProvider = null;
        }
        // TODO special case single chunk?

        for (int currChunkZ = minChunkZ; currChunkZ <= maxChunkZ; ++currChunkZ) {
            int minZ = currChunkZ == minChunkZ ? minBlockZ & 15 : 0; // coordinate in chunk
            int maxZ = currChunkZ == maxChunkZ ? maxBlockZ & 15 : 15; // coordinate in chunk

            for (int currChunkX = minChunkX; currChunkX <= maxChunkX; ++currChunkX) {
                int minX = currChunkX == minChunkX ? minBlockX & 15 : 0; // coordinate in chunk
                int maxX = currChunkX == maxChunkX ? maxBlockX & 15 : 15; // coordinate in chunk

                int chunkXGlobalPos = currChunkX << 4;
                int chunkZGlobalPos = currChunkZ << 4;
                ChunkAccess chunk;
                if (chunkProvider == null) {
                    chunk = (ChunkAccess)getter.getChunkForCollisions(currChunkX, currChunkZ);
                } else {
                    chunk = loadChunks ? chunkProvider.getChunk(currChunkX, currChunkZ, true) : chunkProvider.getChunkAtIfLoadedImmediately(currChunkX, currChunkZ);
                }


                if (chunk == null) {
                    if (collidesWithUnloaded) {
                        if (checkOnly) {
                            return true;
                        } else {
                            into.add(getBoxForChunk(currChunkX, currChunkZ));
                            ret = true;
                        }
                    }
                    continue;
                }

                LevelChunkSection[] sections = chunk.getSections();

                // bound y

                for (int currY = minYIterate; currY <= maxYIterate; ++currY) {
                    LevelChunkSection section = sections[(currY >> 4) - minSection];
                    if (section == null || section.isEmpty()) {
                        // empty
                        // skip to next section
                        currY = (currY & ~(15)) + 15; // increment by 15: iterator loop increments by the extra one
                        continue;
                    }

                    net.minecraft.world.level.chunk.PalettedContainer<BlockState> blocks = section.states;

                    for (int currZ = minZ; currZ <= maxZ; ++currZ) {
                        for (int currX = minX; currX <= maxX; ++currX) {
                            int localBlockIndex = (currX) | (currZ << 4) | ((currY & 15) << 8);
                            int blockX = currX | chunkXGlobalPos;
                            int blockY = currY;
                            int blockZ = currZ | chunkZGlobalPos;

                            int edgeCount = ((blockX == minBlockX || blockX == maxBlockX) ? 1 : 0) +
                                    ((blockY == minBlockY || blockY == maxBlockY) ? 1 : 0) +
                                    ((blockZ == minBlockZ || blockZ == maxBlockZ) ? 1 : 0);
                            if (edgeCount == 3) {
                                continue;
                            }

                            BlockState blockData = blocks.get(localBlockIndex);
                            if (blockData.isAir()) {
                                continue;
                            }

                            if ((edgeCount != 1 || blockData.shapeExceedsCube()) && (edgeCount != 2 || blockData.getBlock() == Blocks.MOVING_PISTON)) {
                                mutablePos.set(blockX, blockY, blockZ);
                                if (collisionShape == null) {
                                    collisionShape = new LazyEntityCollisionContext(entity);
                                }
                                VoxelShape voxelshape2 = blockData.getCollisionShape(getter, mutablePos, collisionShape);
                                if (voxelshape2 != Shapes.empty()) {
                                    VoxelShape voxelshape3 = voxelshape2.move((double)blockX, (double)blockY, (double)blockZ);

                                    if (predicate != null && !predicate.test(blockData, mutablePos)) {
                                        continue;
                                    }

                                    if (checkOnly) {
                                        if (voxelshape3.intersects(aabb)) {
                                            return true;
                                        }
                                    } else {
                                        ret |= addBoxesToIfIntersects(voxelshape3, aabb, into);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }

    public static boolean getEntityHardCollisions(final CollisionGetter getter, final Entity entity, AABB aabb,
                                                  final List<AABB> into, final boolean checkOnly, final Predicate<Entity> predicate) {
        if (isEmpty(aabb) || !(getter instanceof EntityGetter entityGetter)) {
            return false;
        }

        boolean ret = false;

        // to comply with vanilla intersection rules, expand by -epsilon so we only get stuff we definitely collide with.
        // Vanilla for hard collisions has this backwards, and they expand by +epsilon but this causes terrible problems
        // specifically with boat collisions.
        aabb = aabb.inflate(-COLLISION_EPSILON, -COLLISION_EPSILON, -COLLISION_EPSILON);
        final List<Entity> entities = CachedLists.getTempGetEntitiesList();
        try {
            if (entity != null && entity.hardCollides()) {
                entityGetter.getEntities(entity, aabb, predicate, entities);
            } else {
                entityGetter.getHardCollidingEntities(entity, aabb, predicate, entities);
            }

            for (int i = 0, len = entities.size(); i < len; ++i) {
                final Entity otherEntity = entities.get(i);

                if ((entity == null && otherEntity.canBeCollidedWith()) || (entity != null && entity.canCollideWith(otherEntity))) {
                    if (checkOnly) {
                        return true;
                    } else {
                        into.add(otherEntity.getBoundingBox());
                        ret = true;
                    }
                }
            }
        } finally {
            CachedLists.returnTempGetEntitiesList(entities);
        }

        return ret;
    }

    public static boolean getCollisions(final CollisionGetter view, final Entity entity, final AABB aabb,
                                        final List<AABB> into, final boolean loadChunks, final boolean collidesWithUnloadedChunks,
                                        final boolean checkBorder, final boolean checkOnly, final BiPredicate<BlockState, BlockPos> blockPredicate,
                                        final Predicate<Entity> entityPredicate) {
        if (checkOnly) {
            return getCollisionsForBlocksOrWorldBorder(view, entity, aabb, into, loadChunks, collidesWithUnloadedChunks, checkBorder, checkOnly, blockPredicate)
                || getEntityHardCollisions(view, entity, aabb, into, checkOnly, entityPredicate);
        } else {
            return getCollisionsForBlocksOrWorldBorder(view, entity, aabb, into, loadChunks, collidesWithUnloadedChunks, checkBorder, checkOnly, blockPredicate)
                | getEntityHardCollisions(view, entity, aabb, into, checkOnly, entityPredicate);
        }
    }

    public static final class LazyEntityCollisionContext extends EntityCollisionContext {

        private CollisionContext delegate;
        private final Entity entity;

        public LazyEntityCollisionContext(final Entity entity) {
            super(false, 0.0, null, null, null, Optional.ofNullable(entity));
            this.entity = entity;
        }

        public CollisionContext getDelegate() {
            return this.delegate == null ? this.delegate = (this.entity == null ? CollisionContext.empty() : CollisionContext.of(this.entity)) : this.delegate;
        }

        @Override
        public boolean isDescending() {
            return this.getDelegate().isDescending();
        }

        @Override
        public boolean isAbove(final VoxelShape shape, final BlockPos pos, final boolean defaultValue) {
            return this.getDelegate().isAbove(shape, pos, defaultValue);
        }

        @Override
        public boolean hasItemOnFeet(final Item item) {
            return this.getDelegate().hasItemOnFeet(item);
        }

        @Override
        public boolean isHoldingItem(final Item item) {
            return this.getDelegate().isHoldingItem(item);
        }

        @Override
        public boolean canStandOnFluid(final FluidState state, final FlowingFluid fluid) {
            return this.getDelegate().canStandOnFluid(state, fluid);
        }
    }

    private CollisionUtil() {
        throw new RuntimeException();
    }
}
