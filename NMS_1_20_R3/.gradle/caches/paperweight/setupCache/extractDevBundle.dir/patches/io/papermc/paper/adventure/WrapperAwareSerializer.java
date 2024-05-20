package io.papermc.paper.adventure;

import com.mojang.datafixers.util.Pair;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.JavaOps;

final class WrapperAwareSerializer implements ComponentSerializer<Component, Component, net.minecraft.network.chat.Component> {
    @Override
    public Component deserialize(final net.minecraft.network.chat.Component input) {
        if (input instanceof AdventureComponent) {
            return ((AdventureComponent) input).adventure;
        }
        final Object obj = ComponentSerialization.CODEC.encodeStart(JavaOps.INSTANCE, input)
            .get().map(Function.identity(), partial -> {
                throw new RuntimeException("Failed to encode Minecraft Component: " + input + "; " + partial.message());
            });
        final Pair<Component, Object> converted = AdventureCodecs.COMPONENT_CODEC.decode(JavaOps.INSTANCE, obj)
            .get().map(Function.identity(), partial -> {
                throw new RuntimeException("Failed to decode to adventure Component: " + obj + "; " + partial.message());
            });
        return converted.getFirst();
    }

    @Override
    public net.minecraft.network.chat.Component serialize(final Component component) {
        final Object obj = AdventureCodecs.COMPONENT_CODEC.encodeStart(JavaOps.INSTANCE, component)
            .get().map(Function.identity(), partial -> {
                throw new RuntimeException("Failed to encode adventure Component: " + component + "; " + partial.message());
            });
        final Pair<net.minecraft.network.chat.Component, Object> converted = ComponentSerialization.CODEC.decode(JavaOps.INSTANCE, obj)
            .get().map(Function.identity(), partial -> {
                throw new RuntimeException("Failed to decode to Minecraft Component: " + obj + "; " + partial.message());
            });
        return converted.getFirst();
    }
}
