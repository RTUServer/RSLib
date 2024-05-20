package org.bukkit.craftbukkit.v1_20_R3.enchantments;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.BindingCurseEnchantment;
import net.minecraft.world.item.enchantment.VanishingCurseEnchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.v1_20_R3.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.util.Handleable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

public class CraftEnchantment extends Enchantment implements Handleable<net.minecraft.world.item.enchantment.Enchantment> {

    public static Enchantment minecraftToBukkit(net.minecraft.world.item.enchantment.Enchantment minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.ENCHANTMENT, Registry.ENCHANTMENT);
    }

    public static net.minecraft.world.item.enchantment.Enchantment bukkitToMinecraft(Enchantment bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    private final NamespacedKey key;
    private final net.minecraft.world.item.enchantment.Enchantment handle;
    private final int id;

    public CraftEnchantment(NamespacedKey key, net.minecraft.world.item.enchantment.Enchantment handle) {
        this.key = key;
        this.handle = handle;
        this.id = BuiltInRegistries.ENCHANTMENT.getId(handle);
    }

    @Override
    public net.minecraft.world.item.enchantment.Enchantment getHandle() {
        return this.handle;
    }

    @Override
    public NamespacedKey getKey() {
        return this.key;
    }

    @Override
    public int getMaxLevel() {
        return this.handle.getMaxLevel();
    }

    @Override
    public int getStartLevel() {
        return this.handle.getMinLevel();
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return switch (this.handle.category) {
            case ARMOR -> EnchantmentTarget.ARMOR;
            case ARMOR_FEET -> EnchantmentTarget.ARMOR_FEET;
            case ARMOR_HEAD -> EnchantmentTarget.ARMOR_HEAD;
            case ARMOR_LEGS -> EnchantmentTarget.ARMOR_LEGS;
            case ARMOR_CHEST -> EnchantmentTarget.ARMOR_TORSO;
            case DIGGER -> EnchantmentTarget.TOOL;
            case WEAPON -> EnchantmentTarget.WEAPON;
            case BOW -> EnchantmentTarget.BOW;
            case FISHING_ROD -> EnchantmentTarget.FISHING_ROD;
            case BREAKABLE -> EnchantmentTarget.BREAKABLE;
            case WEARABLE -> EnchantmentTarget.WEARABLE;
            case TRIDENT -> EnchantmentTarget.TRIDENT;
            case CROSSBOW -> EnchantmentTarget.CROSSBOW;
            case VANISHABLE -> EnchantmentTarget.VANISHABLE;
        };
    }

    @Override
    public boolean isTreasure() {
        return this.handle.isTreasureOnly();
    }

    @Override
    public boolean isCursed() {
        return this.handle.isCurse(); // Paper - More Enchantment API
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return this.handle.canEnchant(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public String getName() {
        // PAIL: migration paths
        return switch (this.id) {
            case 0 -> "PROTECTION_ENVIRONMENTAL";
            case 1 -> "PROTECTION_FIRE";
            case 2 -> "PROTECTION_FALL";
            case 3 -> "PROTECTION_EXPLOSIONS";
            case 4 -> "PROTECTION_PROJECTILE";
            case 5 -> "OXYGEN";
            case 6 -> "WATER_WORKER";
            case 7 -> "THORNS";
            case 8 -> "DEPTH_STRIDER";
            case 9 -> "FROST_WALKER";
            case 10 -> "BINDING_CURSE";
            case 11 -> "SOUL_SPEED";
            case 12 -> "SWIFT_SNEAK";
            case 13 -> "DAMAGE_ALL";
            case 14 -> "DAMAGE_UNDEAD";
            case 15 -> "DAMAGE_ARTHROPODS";
            case 16 -> "KNOCKBACK";
            case 17 -> "FIRE_ASPECT";
            case 18 -> "LOOT_BONUS_MOBS";
            case 19 -> "SWEEPING_EDGE";
            case 20 -> "DIG_SPEED";
            case 21 -> "SILK_TOUCH";
            case 22 -> "DURABILITY";
            case 23 -> "LOOT_BONUS_BLOCKS";
            case 24 -> "ARROW_DAMAGE";
            case 25 -> "ARROW_KNOCKBACK";
            case 26 -> "ARROW_FIRE";
            case 27 -> "ARROW_INFINITE";
            case 28 -> "LUCK";
            case 29 -> "LURE";
            case 30 -> "LOYALTY";
            case 31 -> "IMPALING";
            case 32 -> "RIPTIDE";
            case 33 -> "CHANNELING";
            case 34 -> "MULTISHOT";
            case 35 -> "QUICK_CHARGE";
            case 36 -> "PIERCING";
            case 37 -> "MENDING";
            case 38 -> "VANISHING_CURSE";
            default -> this.getKey().toString();
        };
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        if (other instanceof EnchantmentWrapper) {
            other = ((EnchantmentWrapper) other).getEnchantment();
        }
        if (!(other instanceof CraftEnchantment)) {
            return false;
        }
        CraftEnchantment ench = (CraftEnchantment) other;
        return !this.handle.isCompatibleWith(ench.getHandle());
    }
    // Paper start
    @Override
    public net.kyori.adventure.text.Component displayName(int level) {
        return io.papermc.paper.adventure.PaperAdventure.asAdventure(getHandle().getFullname(level));
    }

    @Override
    public String translationKey() {
        return this.handle.getDescriptionId();
    }

    @Override
    public boolean isTradeable() {
        return handle.isTradeable();
    }

    @Override
    public boolean isDiscoverable() {
        return handle.isDiscoverable();
    }

    @Override
    public int getMinModifiedCost(int level) {
        return handle.getMinCost(level);
    }

    @Override
    public int getMaxModifiedCost(int level) {
        return handle.getMaxCost(level);
    }

    @Override
    public io.papermc.paper.enchantments.EnchantmentRarity getRarity() {
        return fromNMSRarity(handle.getRarity());
    }

    @Override
    public float getDamageIncrease(int level, org.bukkit.entity.EntityCategory entityCategory) {
        return handle.getDamageBonus(level, org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity.fromBukkitEntityCategory(entityCategory));
    }

    @Override
    public java.util.Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
        return java.util.stream.Stream.of(handle.slots).map(org.bukkit.craftbukkit.v1_20_R3.CraftEquipmentSlot::getSlot).collect(java.util.stream.Collectors.toSet());
    }

    public static io.papermc.paper.enchantments.EnchantmentRarity fromNMSRarity(net.minecraft.world.item.enchantment.Enchantment.Rarity nmsRarity) {
        if (nmsRarity == net.minecraft.world.item.enchantment.Enchantment.Rarity.COMMON) {
            return io.papermc.paper.enchantments.EnchantmentRarity.COMMON;
        } else if (nmsRarity == net.minecraft.world.item.enchantment.Enchantment.Rarity.UNCOMMON) {
            return io.papermc.paper.enchantments.EnchantmentRarity.UNCOMMON;
        } else if (nmsRarity == net.minecraft.world.item.enchantment.Enchantment.Rarity.RARE) {
            return io.papermc.paper.enchantments.EnchantmentRarity.RARE;
        } else if (nmsRarity == net.minecraft.world.item.enchantment.Enchantment.Rarity.VERY_RARE) {
            return io.papermc.paper.enchantments.EnchantmentRarity.VERY_RARE;
        }

        throw new IllegalArgumentException(String.format("Unable to convert %s to a enum value of %s.", nmsRarity, io.papermc.paper.enchantments.EnchantmentRarity.class));
    }
    // Paper end

    @Override
    public String getTranslationKey() {
        return this.handle.getDescriptionId();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof CraftEnchantment)) {
            return false;
        }

        return this.getKey().equals(((Enchantment) other).getKey());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    @Override
    public String toString() {
        return "CraftEnchantment[" + this.getKey() + "]";
    }
}
