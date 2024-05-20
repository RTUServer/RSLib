package org.bukkit.craftbukkit.v1_18_R1.block;

import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.bukkit.World;
import org.bukkit.block.EnderChest;

public class CraftEnderChest extends CraftBlockEntityState<EnderChestBlockEntity> implements EnderChest {

    public CraftEnderChest(World world, EnderChestBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    // Paper start - More Lidded Block API
    @Override
    public void open() {
        requirePlaced();
        if (!getTileEntity().openersCounter.opened) {
            net.minecraft.world.level.Level world = getTileEntity().getLevel();
            world.blockEvent(getTileEntity().getBlockPos(), getTileEntity().getBlockState().getBlock(), 1, getTileEntity().openersCounter.getOpenerCount() + 1);
            world.playSound(null, getPosition(), net.minecraft.sounds.SoundEvents.ENDER_CHEST_OPEN, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
        }
        getTileEntity().openersCounter.opened = true;
    }

    @Override
    public void close() {
        requirePlaced();
        if (getTileEntity().openersCounter.opened) {
            net.minecraft.world.level.Level world = getTileEntity().getLevel();
            world.blockEvent(getTileEntity().getBlockPos(), getTileEntity().getBlockState().getBlock(), 1, 0);
            world.playSound(null, getPosition(), net.minecraft.sounds.SoundEvents.ENDER_CHEST_CLOSE, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
        }
        getTileEntity().openersCounter.opened = false;
    }

    @Override
    public boolean isOpen() {
        return getTileEntity().openersCounter.opened;
    }
    // Paper end - More Lidded Block API
}
