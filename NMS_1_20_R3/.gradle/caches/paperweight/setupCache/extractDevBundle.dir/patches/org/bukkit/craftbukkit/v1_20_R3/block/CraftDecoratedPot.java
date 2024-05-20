package org.bukkit.craftbukkit.v1_20_R3.block;

import com.google.common.base.Preconditions;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.DecoratedPot;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftInventoryDecoratedPot;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemType;
import org.bukkit.inventory.DecoratedPotInventory;

public class CraftDecoratedPot extends CraftBlockEntityState<DecoratedPotBlockEntity> implements DecoratedPot {

    public CraftDecoratedPot(World world, DecoratedPotBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftDecoratedPot(CraftDecoratedPot state) {
        super(state);
    }

    @Override
    public DecoratedPotInventory getSnapshotInventory() {
        return new CraftInventoryDecoratedPot(this.getSnapshot());
    }

    @Override
    public DecoratedPotInventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventoryDecoratedPot(this.getTileEntity());
    }

    // Paper start - expose loot table
    @Override
    public void setLootTable(final org.bukkit.loot.LootTable table) {
        this.setLootTable(table, this.getSeed());
    }

    @Override
    public void setLootTable(org.bukkit.loot.LootTable table, long seed) {
        net.minecraft.resources.ResourceLocation key = (table == null) ? null : org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey.toMinecraft(table.getKey());
        this.getSnapshot().setLootTable(key, seed);
    }

    @Override
    public org.bukkit.loot.LootTable getLootTable() {
        if (this.getSnapshot().getLootTable() == null) {
            return null;
        }

        net.minecraft.resources.ResourceLocation key = this.getSnapshot().getLootTable();
        return org.bukkit.Bukkit.getLootTable(org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey.fromMinecraft(key));
    }

    @Override
    public void setSeed(final long seed) {
        this.getSnapshot().setLootTableSeed(seed);
    }

    @Override
    public long getSeed() {
        return this.getSnapshot().getLootTableSeed();
    }
    // Paper end - expose loot table

    @Override
    public void setSherd(Side face, Material sherd) {
        Preconditions.checkArgument(face != null, "face must not be null");
        Preconditions.checkArgument(sherd == null || sherd == Material.BRICK || Tag.ITEMS_DECORATED_POT_SHERDS.isTagged(sherd), "sherd is not a valid sherd material: %s", sherd);

        Item sherdItem = (sherd != null) ? CraftItemType.bukkitToMinecraft(sherd) : Items.BRICK;
        DecoratedPotBlockEntity.Decorations decorations = this.getSnapshot().getDecorations();

        switch (face) {
            case BACK -> this.getSnapshot().decorations = new DecoratedPotBlockEntity.Decorations(sherdItem, decorations.left(), decorations.right(), decorations.front());
            case LEFT -> this.getSnapshot().decorations = new DecoratedPotBlockEntity.Decorations(decorations.back(), sherdItem, decorations.right(), decorations.front());
            case RIGHT -> this.getSnapshot().decorations = new DecoratedPotBlockEntity.Decorations(decorations.back(), decorations.left(), sherdItem, decorations.front());
            case FRONT -> this.getSnapshot().decorations = new DecoratedPotBlockEntity.Decorations(decorations.back(), decorations.left(), decorations.right(), sherdItem);
            default -> throw new IllegalArgumentException("Unexpected value: " + face);
        }
    }

    @Override
    public Material getSherd(Side face) {
        Preconditions.checkArgument(face != null, "face must not be null");

        DecoratedPotBlockEntity.Decorations decorations = this.getSnapshot().getDecorations();
        Item sherdItem = switch (face) {
            case BACK -> decorations.back();
            case LEFT -> decorations.left();
            case RIGHT -> decorations.right();
            case FRONT -> decorations.front();
            default -> throw new IllegalArgumentException("Unexpected value: " + face);
        };

        return CraftItemType.minecraftToBukkit(sherdItem);
    }

    @Override
    public Map<Side, Material> getSherds() {
        DecoratedPotBlockEntity.Decorations decorations = this.getSnapshot().getDecorations();

        Map<Side, Material> sherds = new EnumMap<>(Side.class);
        sherds.put(Side.BACK, CraftItemType.minecraftToBukkit(decorations.back()));
        sherds.put(Side.LEFT, CraftItemType.minecraftToBukkit(decorations.left()));
        sherds.put(Side.RIGHT, CraftItemType.minecraftToBukkit(decorations.right()));
        sherds.put(Side.FRONT, CraftItemType.minecraftToBukkit(decorations.front()));
        return sherds;
    }

    @Override
    public List<Material> getShards() {
        return this.getSnapshot().getDecorations().sorted().map(CraftItemType::minecraftToBukkit).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public CraftDecoratedPot copy() {
        return new CraftDecoratedPot(this);
    }
}
