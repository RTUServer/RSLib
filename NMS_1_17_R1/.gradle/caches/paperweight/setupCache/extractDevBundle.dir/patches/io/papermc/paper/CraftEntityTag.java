package io.papermc.paper;

import org.bukkit.craftbukkit.v1_17_R1.tag.CraftTag;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;

@Deprecated(forRemoval = true)
public class CraftEntityTag extends CraftTag<net.minecraft.world.entity.EntityType<?>, EntityType> {

    public CraftEntityTag(TagCollection<net.minecraft.world.entity.EntityType<?>> registry, ResourceLocation tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(EntityType item) {
        return getHandle().contains(CraftMagicNumbers.getEntityTypes(item));
    }

    @Override
    public Set<EntityType> getValues() {
        return Collections.unmodifiableSet(getHandle().getValues().stream().map(CraftMagicNumbers::getEntityType).collect(Collectors.toSet()));
    }
}
