package com.github.ipecter.rtuserver.lib.bukkit.plugin.util.compatible;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import dev.lone.itemsadder.api.CustomBlock;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.mechanics.Mechanic;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import javax.annotation.Nullable;

// Supported Plugins: ItemsAdder, Oraxen
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockCompat {

    @Nullable
    public static BlockData from(String data) {
        String[] split = data.split(":");
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
                Material material = Material.getMaterial(id.toUpperCase());
                return material != null ? material.createBlockData() : null;
            }
        }
    }

    public static String to(Block block) {
        if (RSLib.getInstance().isEnabledDependency("Oraxen")) {
            Mechanic oraxen = OraxenBlocks.getOraxenBlock(block.getBlockData());
            if (oraxen != null)
                return "oraxen:" + oraxen.getItemID();
        }
        if (RSLib.getInstance().isEnabledDependency("ItemsAdder")) {
            CustomBlock itemsAdder = CustomBlock.byAlreadyPlaced(block);
            if (itemsAdder != null)
                return "itemsadder:" + itemsAdder.getNamespacedID();
        }
        return "minecraft:" + block.getBlockData().getMaterial().toString().toLowerCase();
    }

    public static boolean place(Location location, String namespacedID) {
        String[] split = namespacedID.split(":");
        String platform = split[0].toLowerCase();
        switch (platform) {
            case "oraxen" -> {
                if (RSLib.getInstance().isEnabledDependency("Oraxen")) {
                    if (OraxenBlocks.isOraxenBlock(namespacedID)) OraxenBlocks.place(split[1], location);
                    else return false;
                } else return false;
                return true;
            }
            case "itemsadder" -> {
                if (RSLib.getInstance().isEnabledDependency("ItemsAdder")) {
                    String block = split[1] + ":" + split[2];
                    if (CustomBlock.isInRegistry(block)) CustomBlock.place(block, location);
                    else return false;
                } else return false;
                return true;
            }
            default -> {
                String id = split.length > 1 ? split[1] : split[0];
                Material material = Material.getMaterial(id.toUpperCase());
                if (material != null) location.getWorld().setBlockData(location, material.createBlockData());
                else return false;
                return true;
            }
        }
    }
}
