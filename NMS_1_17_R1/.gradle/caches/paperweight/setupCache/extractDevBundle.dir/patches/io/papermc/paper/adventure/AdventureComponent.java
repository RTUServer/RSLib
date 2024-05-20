package io.papermc.paper.adventure;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class AdventureComponent implements net.minecraft.network.chat.Component {
    final Component wrapped;
    private net.minecraft.network.chat.@MonotonicNonNull Component converted;

    public AdventureComponent(final Component wrapped) {
        this.wrapped = wrapped;
    }

    public net.minecraft.network.chat.Component deepConverted() {
        net.minecraft.network.chat.Component converted = this.converted;
        if (converted == null) {
            converted = PaperAdventure.WRAPPER_AWARE_SERIALIZER.serialize(this.wrapped);
            this.converted = converted;
        }
        return converted;
    }

    public net.minecraft.network.chat.@Nullable Component deepConvertedIfPresent() {
        return this.converted;
    }

    @Override
    public Style getStyle() {
        return this.deepConverted().getStyle();
    }

    @Override
    public String getContents() {
        if (this.wrapped instanceof TextComponent) {
            return ((TextComponent) this.wrapped).content();
        } else {
            return this.deepConverted().getContents();
        }
    }

    @Override
    public String getString() {
        return PaperAdventure.PLAIN.serialize(this.wrapped);
    }

    @Override
    public List<net.minecraft.network.chat.Component> getSiblings() {
        return this.deepConverted().getSiblings();
    }

    @Override
    public MutableComponent plainCopy() {
        return this.deepConverted().plainCopy();
    }

    @Override
    public MutableComponent copy() {
        return this.deepConverted().copy();
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return this.deepConverted().getVisualOrderText();
    }

    public static class Serializer implements JsonSerializer<AdventureComponent> {
        @Override
        public JsonElement serialize(final AdventureComponent src, final Type type, final JsonSerializationContext context) {
            return PaperAdventure.GSON.serializer().toJsonTree(src.wrapped, Component.class);
        }
    }
}
