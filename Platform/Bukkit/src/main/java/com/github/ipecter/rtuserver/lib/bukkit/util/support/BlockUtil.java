package com.github.ipecter.rtuserver.lib.bukkit.util.support;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import dev.lone.itemsadder.api.CustomBlock;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.mechanics.Mechanic;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockUtil {

    @Nullable
    public static BlockData fromId(@NotNull String namespacedID) {
        String[] split = namespacedID.split(":");
        String platform = split[0].toLowerCase();
        switch (platform) {
            case "oraxen" -> {
                if (RSLib.getInstance().isEnabledDependency("Oraxen")) {
                    return OraxenBlocks.getOraxenBlockData(split[1]);
                } else return null;
            }
            case "itemsadder" -> {
                if (RSLib.getInstance().isEnabledDependency("ItemsAdder")) {
                    CustomBlock customBlock = CustomBlock.getInstance(split[1] + ":" + split[2]);
                    return customBlock != null ? customBlock.getBaseBlockData() : null;
                } else return null;
            }
            default -> {
                String id = split.length > 1 ? split[1] : split[0];
                Material material = Material.getMaterial(id);
                return material != null ? material.createBlockData() : null;
            }
        }
    }

    public static String fromBlock(@NotNull Block block) {
        if (RSLib.getInstance().isEnabledDependency("Oraxen")) {
            Mechanic oraxen = OraxenBlocks.getOraxenBlock(block.getBlockData());
            if (oraxen != null) return "oraxen:" + oraxen.getItemID();
        }
        if (RSLib.getInstance().isEnabledDependency("ItemsAdder")) {
            CustomBlock itemsAdder = CustomBlock.byAlreadyPlaced(block);
            if (itemsAdder != null) return "itemsadder:" + itemsAdder.getNamespacedID();
        }
        return "minecraft:" + block.getBlockData().getMaterial().toString().toLowerCase();
    }

}
