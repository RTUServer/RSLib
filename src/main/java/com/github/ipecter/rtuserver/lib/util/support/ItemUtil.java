package com.github.ipecter.rtuserver.lib.util.support;

import com.github.ipecter.rtuserver.lib.RSLib;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xerial.snappy.Snappy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class ItemUtil {

    @Nullable
    public static ItemStack fromId(@NotNull String namespacedID) {
        String[] split = namespacedID.split(":");
        String platform = split[0].toLowerCase();
        switch (platform) {
            case "oraxen" -> {
                if (RSLib.getInstance().isEnabledDependency("Oraxen")) {
                    ItemBuilder itemBuilder = OraxenItems.getItemById(split[1]);
                    return itemBuilder != null ? itemBuilder.build() : null;
                } else return null;
            }
            case "itemsadder" -> {
                if (RSLib.getInstance().isEnabledDependency("ItemsAdder")) {
                    CustomStack customStack = CustomStack.getInstance(split[1] + ":" + split[2]);
                    return customStack != null ? customStack.getItemStack() : null;
                } else return null;
            }
            case "custom" -> {
                if (split.length != 3) return null;
                Material material = Material.getMaterial(split[1].toUpperCase());
                if (material == null) return null;
                ItemStack itemStack = new ItemStack(material);
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta == null) return null;
                itemMeta.setCustomModelData(Integer.valueOf(split[2]));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
            default -> {
                String id = split.length > 1 ? split[1] : split[0];
                Material material = Material.getMaterial(id.toUpperCase());
                return material != null ? new ItemStack(material) : null;
            }
        }
    }


    public static String fromItemStack(@NotNull ItemStack itemStack) {
        if (RSLib.getInstance().isEnabledDependency("Oraxen")) {
            String oraxen = OraxenItems.getIdByItem(itemStack);
            if (oraxen != null) return "oraxen:" + oraxen;
        }
        if (RSLib.getInstance().isEnabledDependency("ItemsAdder")) {
            CustomStack itemsAdder = CustomStack.byItemStack(itemStack);
            if (itemsAdder != null) return "itemsadder:" + itemsAdder.getNamespacedID();
        }
        String result = "minecraft:" + itemStack.getType().toString().toLowerCase();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return result;
        if (!itemMeta.hasCustomModelData()) return result;
        return "custom:" + itemStack.getType().toString().toLowerCase() + itemMeta.getCustomModelData();
    }

    public static boolean isSimilar(ItemStack stack1, ItemStack stack2) {
        if (RSLib.getInstance().isEnabledDependency("Oraxen")) {
            String oraxen1 = OraxenItems.getIdByItem(stack1);
            String oraxen2 = OraxenItems.getIdByItem(stack2);
            if (oraxen1 != null && oraxen2 != null) {
                return oraxen1.equalsIgnoreCase(oraxen2);
            } else if (oraxen1 != null) {
                return OraxenItems.getItemById(oraxen1).build().isSimilar(stack2);
            } else if (oraxen2 != null) {
                return OraxenItems.getItemById(oraxen2).build().isSimilar(stack1);
            }
        }
        if (RSLib.getInstance().isEnabledDependency("ItemsAdder")) {
            CustomStack itemsAdder1 = CustomStack.byItemStack(stack1);
            CustomStack itemsAdder2 = CustomStack.byItemStack(stack2);
            if (itemsAdder1 != null && itemsAdder2 != null) {
                return itemsAdder1.getNamespacedID().equalsIgnoreCase(itemsAdder2.getNamespacedID());
            } else if (itemsAdder1 != null) {
                return itemsAdder1.getItemStack().isSimilar(stack2);
            } else if (itemsAdder2 != null) {
                return itemsAdder2.getItemStack().isSimilar(stack1);
            }
        }
        return stack1.isSimilar(stack2);
    }

    private static String encode(ItemStack itemStack) {
        try {
            final ByteArrayOutputStream str = new ByteArrayOutputStream();
            final BukkitObjectOutputStream data = new BukkitObjectOutputStream(str);
            data.writeObject(itemStack);
            data.close();
            return Base64.getEncoder().encodeToString(Snappy.compress(str.toByteArray()));
        } catch (final Exception e) {
            return "";
        }
    }

    public static ItemStack decode(String dataString) {
        ItemStack itemStack;
        try {
            final ByteArrayInputStream stream = new ByteArrayInputStream(Snappy.uncompress(Base64.getDecoder().decode(dataString)));
            final BukkitObjectInputStream data = new BukkitObjectInputStream(stream);
            itemStack = (ItemStack) data.readObject();
            data.close();
        } catch (final Exception e) {
            return null;
        }
        return itemStack;
    }
}
