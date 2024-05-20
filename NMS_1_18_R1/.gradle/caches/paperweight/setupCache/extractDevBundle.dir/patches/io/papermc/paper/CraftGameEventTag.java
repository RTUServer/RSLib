package io.papermc.paper;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;
import org.bukkit.GameEvent;
import org.bukkit.craftbukkit.v1_18_R1.tag.CraftTag;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CraftGameEventTag extends CraftTag<net.minecraft.world.level.gameevent.GameEvent, GameEvent> {

    public CraftGameEventTag(TagCollection<net.minecraft.world.level.gameevent.GameEvent> registry, ResourceLocation tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(@NotNull GameEvent item) {
        return this.getHandle().contains(Registry.GAME_EVENT.get(CraftNamespacedKey.toMinecraft(item.getKey())));
    }

    @Override
    public @NotNull Set<GameEvent> getValues() {
        return this.getHandle().getValues().stream().map(ge -> Objects.requireNonNull(GameEvent.getByKey(CraftNamespacedKey.fromMinecraft(Registry.GAME_EVENT.getKey(ge))), ge + " is not a recognized game event")).collect(Collectors.toUnmodifiableSet());
    }
}
