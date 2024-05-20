package org.bukkit.craftbukkit.v1_18_R2.entity;

import java.util.Random;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class CraftFirework extends CraftProjectile implements Firework {

    private final Random random = new Random();
    //private CraftItemStack item; // Paper - Remove usage, not accurate representation of current item.

    public CraftFirework(CraftServer server, FireworkRocketEntity entity) {
        super(server, entity);

//        Paper Start - Expose firework item directly
//        ItemStack item = this.getHandle().getEntityData().get(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM);
//
//        if (item.isEmpty()) {
//            item = new ItemStack(Items.FIREWORK_ROCKET);
//            this.getHandle().getEntityData().set(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM, item);
//        }
//
//        this.item = CraftItemStack.asCraftMirror(item);
//
//        // Ensure the item is a firework...
//        if (this.item.getType() != Material.FIREWORK_ROCKET) {
//            this.item.setType(Material.FIREWORK_ROCKET);
//        }
        // Paper End - Expose firework item directly
    }

    @Override
    public FireworkRocketEntity getHandle() {
        return (FireworkRocketEntity) entity;
    }

    @Override
    public String toString() {
        return "CraftFirework";
    }

    @Override
    public EntityType getType() {
        return EntityType.FIREWORK;
    }

    @Override
    public FireworkMeta getFireworkMeta() {
        return (FireworkMeta) CraftItemStack.getItemMeta(this.getHandle().getEntityData().get(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM), Material.FIREWORK_ROCKET); // Paper - Expose firework item directly
    }

    @Override
    public void setFireworkMeta(FireworkMeta meta) {
        applyFireworkEffect(meta); // Paper - Expose firework item directly

        // Copied from EntityFireworks constructor, update firework lifetime/power
        this.getHandle().lifetime = 10 * (1 + meta.getPower()) + this.random.nextInt(6) + this.random.nextInt(7);

        this.getHandle().getEntityData().markDirty(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM);
    }

    @Override
    public void detonate() {
        this.getHandle().lifetime = 0;
    }

    @Override
    public boolean isShotAtAngle() {
        return this.getHandle().isShotAtAngle();
    }

    @Override
    public void setShotAtAngle(boolean shotAtAngle) {
        this.getHandle().getEntityData().set(FireworkRocketEntity.DATA_SHOT_AT_ANGLE, shotAtAngle);
    }

    // Paper start
    @Override
    public java.util.UUID getSpawningEntity() {
        return getHandle().spawningEntity;
    }

    @Override
    public org.bukkit.entity.LivingEntity getBoostedEntity() {
        net.minecraft.world.entity.LivingEntity boostedEntity = getHandle().attachedToEntity;
        return boostedEntity != null ? (org.bukkit.entity.LivingEntity) boostedEntity.getBukkitEntity() : null;
    }
    // Paper end
    // Paper start - Expose firework item directly + manually setting flight
    @Override
    public org.bukkit.inventory.ItemStack getItem() {
        return CraftItemStack.asBukkitCopy(this.getHandle().getItem());
    }

    @Override
    public void setItem(org.bukkit.inventory.ItemStack itemStack) {
        FireworkMeta meta = getFireworkMeta();
        ItemStack nmsItem = itemStack == null ? ItemStack.EMPTY : CraftItemStack.asNMSCopy(itemStack);
        this.getHandle().getEntityData().set(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM, nmsItem);

        applyFireworkEffect(meta);
    }

    @Override
    public int getTicksFlown() {
        return this.getHandle().life;
    }

    @Override
    public void setTicksFlown(int ticks) {
        this.getHandle().life = ticks;
    }

    @Override
    public int getTicksToDetonate() {
        return this.getHandle().lifetime;
    }

    @Override
    public void setTicksToDetonate(int ticks) {
        this.getHandle().lifetime = ticks;
    }

    void applyFireworkEffect(FireworkMeta meta) {
        ItemStack item = this.getHandle().getItem();
        CraftItemStack.applyMetaToItem(item, meta);

        this.getHandle().getEntityData().set(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM, item);
    }
    // Paper end - Expose firework item directly + manually setting flight
}
