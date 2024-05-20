package org.bukkit.craftbukkit.v1_20_R3.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public class CraftMetaSuspiciousStew extends CraftMetaItem implements SuspiciousStewMeta {

    static final ItemMetaKey DURATION = new ItemMetaKey("duration", "duration");
    static final ItemMetaKey EFFECTS = new ItemMetaKey("effects", "effects");
    static final ItemMetaKey ID = new ItemMetaKey("id", "id");

    private List<io.papermc.paper.potion.SuspiciousEffectEntry> customEffects; // Paper - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta

    CraftMetaSuspiciousStew(CraftMetaItem meta) {
        super(meta);
        if (!(meta instanceof CraftMetaSuspiciousStew stewMeta)) {
            return;
        }
        if (stewMeta.hasCustomEffects()) {
            this.customEffects = new ArrayList<>(stewMeta.customEffects);
        }
    }

    CraftMetaSuspiciousStew(CompoundTag tag) {
        super(tag);
        if (tag.contains(CraftMetaSuspiciousStew.EFFECTS.NBT)) {
            ListTag list = tag.getList(CraftMetaSuspiciousStew.EFFECTS.NBT, CraftMagicNumbers.NBT.TAG_COMPOUND);
            int length = list.size();
            this.customEffects = new ArrayList<>(length);

            for (int i = 0; i < length; i++) {
                CompoundTag effect = list.getCompound(i);
                PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.fromString(effect.getString(CraftMetaSuspiciousStew.ID.NBT)));
                if (type == null) {
                    continue;
                }
                // Paper start - default duration is 160
                final int duration;
                if (effect.contains(CraftMetaSuspiciousStew.DURATION.NBT)) {
                    duration = effect.getInt(CraftMetaSuspiciousStew.DURATION.NBT);
                } else {
                    duration = net.minecraft.world.item.SuspiciousStewItem.DEFAULT_DURATION;
                }
                // Paper end start - default duration is 160
                this.customEffects.add(io.papermc.paper.potion.SuspiciousEffectEntry.create(type, duration)); // Paper - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta
            }
        }
    }

    CraftMetaSuspiciousStew(Map<String, Object> map) {
        super(map);

        Iterable<?> rawEffectList = SerializableMeta.getObject(Iterable.class, map, CraftMetaSuspiciousStew.EFFECTS.BUKKIT, true);
        if (rawEffectList == null) {
            return;
        }

        for (Object obj : rawEffectList) {
            Preconditions.checkArgument(obj instanceof PotionEffect, "Object (%s) in effect list is not valid", obj.getClass());
            this.addCustomEffect((PotionEffect) obj, true);
        }
    }

    @Override
    void applyToItem(CompoundTag tag) {
        super.applyToItem(tag);

        if (this.customEffects != null) {
            ListTag effectList = new ListTag();
            tag.put(CraftMetaSuspiciousStew.EFFECTS.NBT, effectList);

            // Paper start - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta
            for (io.papermc.paper.potion.SuspiciousEffectEntry effect : this.customEffects) {
                CompoundTag effectData = new CompoundTag();
                effectData.putString(CraftMetaSuspiciousStew.ID.NBT, effect.effect().getKey().toString());
                if (effect.duration() != net.minecraft.world.item.SuspiciousStewItem.DEFAULT_DURATION) effectData.putInt(CraftMetaSuspiciousStew.DURATION.NBT, effect.duration()); // Paper - don't save duration if it's the default value
                effectList.add(effectData);
            }
            // Paper end - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta
        }
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && this.isStewEmpty();
    }

    boolean isStewEmpty() {
        return !this.hasCustomEffects();
    }

    @Override
    boolean applicableTo(Material type) {
        return type == Material.SUSPICIOUS_STEW;
    }

    @Override
    public CraftMetaSuspiciousStew clone() {
        CraftMetaSuspiciousStew clone = ((CraftMetaSuspiciousStew) super.clone());
        if (this.customEffects != null) {
            clone.customEffects = new ArrayList<>(this.customEffects);
        }
        return clone;
    }

    @Override
    public boolean hasCustomEffects() {
        return this.customEffects != null;
    }

    @Override
    public List<PotionEffect> getCustomEffects() {
        if (this.hasCustomEffects()) {
            return this.customEffects.stream().map(suspiciousEffectEntry -> suspiciousEffectEntry.effect().createEffect(suspiciousEffectEntry.duration(), 0)).toList(); // Paper - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta
        }
        return ImmutableList.of();
    }

    @Override
    public boolean addCustomEffect(PotionEffect effect, boolean overwrite) {
        Preconditions.checkArgument(effect != null, "Potion effect cannot be null");
        return addCustomEffect(io.papermc.paper.potion.SuspiciousEffectEntry.create(effect.getType(), effect.getDuration()), overwrite); // Paper - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta
    }

    // Paper start - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta
    @Override
    public boolean addCustomEffect(final io.papermc.paper.potion.SuspiciousEffectEntry suspiciousEffectEntry, final boolean overwrite) {
        Preconditions.checkArgument(suspiciousEffectEntry != null, "Suspicious effect entry cannot be null");
        int index = this.indexOfEffect(suspiciousEffectEntry.effect());
        if (index != -1) {
            if (overwrite) {
                io.papermc.paper.potion.SuspiciousEffectEntry old = this.customEffects.get(index);
                if (old.duration() == suspiciousEffectEntry.duration()) {
                    return false;
                }
                this.customEffects.set(index, suspiciousEffectEntry);
                return true;
            } else {
                return false;
            }
        } else {
            if (this.customEffects == null) {
                this.customEffects = new ArrayList<>();
            }
            this.customEffects.add(suspiciousEffectEntry);
            return true;
        }
    }
    // Paper end - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta

    @Override
    public boolean removeCustomEffect(PotionEffectType type) {
        Preconditions.checkArgument(type != null, "Potion effect type cannot be null");

        if (!this.hasCustomEffects()) {
            return false;
        }

        boolean changed = false;
        // Paper start - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta
        Iterator<io.papermc.paper.potion.SuspiciousEffectEntry> iterator = this.customEffects.iterator();
        while (iterator.hasNext()) {
            io.papermc.paper.potion.SuspiciousEffectEntry effect = iterator.next();
            if (type.equals(effect.effect())) {
        // Paper end - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta
                iterator.remove();
                changed = true;
            }
        }
        if (this.customEffects.isEmpty()) {
            this.customEffects = null;
        }
        return changed;
    }

    @Override
    public boolean hasCustomEffect(PotionEffectType type) {
        Preconditions.checkArgument(type != null, "Potion effect type cannot be null");
        return this.indexOfEffect(type) != -1;
    }

    private int indexOfEffect(PotionEffectType type) {
        if (!this.hasCustomEffects()) {
            return -1;
        }

        for (int i = 0; i < this.customEffects.size(); i++) {
            if (this.customEffects.get(i).effect().equals(type)) { // Paper - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean clearCustomEffects() {
        boolean changed = this.hasCustomEffects();
        this.customEffects = null;
        return changed;
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();
        if (this.hasCustomEffects()) {
            hash = 73 * hash + this.customEffects.hashCode();
        }
        return original != hash ? CraftMetaSuspiciousStew.class.hashCode() ^ hash : hash;
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaSuspiciousStew that) {
            return (this.hasCustomEffects() ? that.hasCustomEffects() && this.customEffects.equals(that.customEffects) : !that.hasCustomEffects());
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaSuspiciousStew || this.isStewEmpty());
    }

    @Override
    Builder<String, Object> serialize(Builder<String, Object> builder) {
        super.serialize(builder);

        if (this.hasCustomEffects()) {
            builder.put(CraftMetaSuspiciousStew.EFFECTS.BUKKIT, ImmutableList.copyOf(com.google.common.collect.Lists.transform(this.customEffects, s -> new PotionEffect(s.effect(), s.duration(), 0)))); // Paper - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta - convert back to potion effect for bukkit legacy item serialisation to maintain backwards compatibility for the written format.
        }

        return builder;
    }
}
