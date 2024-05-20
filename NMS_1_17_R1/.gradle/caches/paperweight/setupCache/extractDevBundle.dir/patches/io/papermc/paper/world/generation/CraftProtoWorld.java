package io.papermc.paper.world.generation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftMetaBlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

@Deprecated
public class CraftProtoWorld implements ProtoWorld {
    private WorldGenRegion region;

    public CraftProtoWorld(WorldGenRegion region) {
        this.region = region;
    }

    public void clearReference() {
        region = null;
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull BlockData data) {
        BlockPos position = new BlockPos(x, y, z);
        getDelegate().setBlock(position, ((CraftBlockData) data).getState(), 3);
    }

    @Override
    public void setBlockState(int x, int y, int z, @NotNull BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if(!state.getBlockData().matches(getDelegate().getBlockState(pos).createCraftBlockData())) {
            throw new IllegalArgumentException("BlockData does not match! Expected " + state.getBlockData().getAsString(false) + ", got " + getDelegate().getBlockState(pos).createCraftBlockData().getAsString(false));
        }
        getDelegate().getBlockEntity(pos).load(((CraftBlockEntityState<?>) state).getSnapshotNBT());
    }

    @Override
    public @NotNull BlockState getBlockState(int x, int y, int z) {
        BlockEntity entity = getDelegate().getBlockEntity(new BlockPos(x, y, z));
        return CraftMetaBlockState.createBlockState(entity.getBlockState().getBukkitMaterial(), entity.save(new CompoundTag()));
    }

    @Override
    public void scheduleBlockUpdate(int x, int y, int z) {
        BlockPos position = new BlockPos(x, y, z);
        getDelegate().getBlockTicks().scheduleTick(position, getDelegate().getBlockIfLoaded(position), 0);
    }

    @Override
    public void scheduleFluidUpdate(int x, int y, int z) {
        BlockPos position = new BlockPos(x, y, z);
        getDelegate().getLiquidTicks().scheduleTick(position, getDelegate().getFluidState(position).getType(), 0);
    }

    @Override
    public @NotNull World getWorld() {
        // reading/writing the returned Minecraft world causes a deadlock.
        // By implementing this, and covering it in warnings, we're assuming people won't be stupid, and
        // if they are stupid, they'll figure it out pretty fast.
        return getDelegate().getMinecraftWorld().getWorld();
    }

    @Override
    public @NotNull BlockData getBlockData(int x, int y, int z) {
        return CraftBlockData.fromData(getDelegate().getBlockState(new BlockPos(x, y, z)));
    }

    @Override
    public int getCenterChunkX() {
        return getDelegate().getCenter().x;
    }

    @Override
    public int getCenterChunkZ() {
        return getDelegate().getCenter().z;
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    public <T extends Entity> @NotNull T spawn(@NotNull Vector location, @NotNull Class<T> clazz, @Nullable Consumer<T> function, CreatureSpawnEvent.@NotNull SpawnReason reason) throws IllegalArgumentException {
        net.minecraft.world.entity.Entity entity = getDelegate().getMinecraftWorld().getWorld().createEntity(location.toLocation(getWorld()), clazz);
        Objects.requireNonNull(entity, "Cannot spawn null entity");
        if (entity instanceof Mob mob) {
            mob.finalizeSpawn(getDelegate(), getDelegate().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, (SpawnGroupData) null, null);
        }

        if (function != null) {
            function.accept((T) entity.getBukkitEntity());
        }

        getDelegate().addEntity(entity, reason);
        return (T) entity.getBukkitEntity();
    }

    @NotNull
    private WorldGenRegion getDelegate() {
        return Objects.requireNonNull(region, "Cannot access ProtoWorld after generation!");
    }
}

