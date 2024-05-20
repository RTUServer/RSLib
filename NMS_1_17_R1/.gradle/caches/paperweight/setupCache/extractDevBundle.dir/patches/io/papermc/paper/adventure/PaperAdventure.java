package io.papermc.paper.adventure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PaperAdventure {
    public static final AttributeKey<Locale> LOCALE_ATTRIBUTE = AttributeKey.valueOf("adventure:locale");
    private static final Pattern LOCALIZATION_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?s");
    public static final ComponentFlattener FLATTENER = ComponentFlattener.basic().toBuilder()
        .complexMapper(TranslatableComponent.class, (translatable, consumer) -> {
            if (!Language.getInstance().has(translatable.key())) {
                for (final Translator source : GlobalTranslator.get().sources()) {
                    if (source instanceof TranslationRegistry registry && registry.contains(translatable.key())) {
                        consumer.accept(GlobalTranslator.render(translatable, Locale.US));
                        return;
                    }
                }
            }
            final @NonNull String translated = Language.getInstance().getOrDefault(translatable.key());

            final Matcher matcher = LOCALIZATION_PATTERN.matcher(translated);
            final List<Component> args = translatable.args();
            int argPosition = 0;
            int lastIdx = 0;
            while (matcher.find()) {
                // append prior
                if (lastIdx < matcher.start()) {
                    consumer.accept(Component.text(translated.substring(lastIdx, matcher.start())));
                }
                lastIdx = matcher.end();

                final @Nullable String argIdx = matcher.group(1);
                // calculate argument position
                if (argIdx != null) {
                    try {
                        final int idx = Integer.parseInt(argIdx) - 1;
                        if (idx < args.size()) {
                            consumer.accept(args.get(idx));
                        }
                    } catch (final NumberFormatException ex) {
                        // ignore, drop the format placeholder
                    }
                } else {
                    final int idx = argPosition++;
                    if (idx < args.size()) {
                        consumer.accept(args.get(idx));
                    }
                }
            }

            // append tail
            if (lastIdx < translated.length()) {
                consumer.accept(Component.text(translated.substring(lastIdx)));
            }
        })
        .build();
    public static final LegacyComponentSerializer LEGACY_SECTION_UXRC = LegacyComponentSerializer.builder().flattener(FLATTENER).hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    public static final PlainComponentSerializer PLAIN = PlainComponentSerializer.builder().flattener(FLATTENER).build();
    public static final GsonComponentSerializer GSON = GsonComponentSerializer.builder()
        .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.INSTANCE)
        .build();
    public static final GsonComponentSerializer COLOR_DOWNSAMPLING_GSON = GsonComponentSerializer.builder()
        .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.INSTANCE)
        .downsampleColors()
        .build();
    private static final Codec<CompoundTag, String, IOException, IOException> NBT_CODEC = new Codec<CompoundTag, String, IOException, IOException>() {
        @Override
        public @NonNull CompoundTag decode(final @NonNull String encoded) throws IOException {
            try {
                return TagParser.parseTag(encoded);
            } catch (final CommandSyntaxException e) {
                throw new IOException(e);
            }
        }

        @Override
        public @NonNull String encode(final @NonNull CompoundTag decoded) {
            return decoded.toString();
        }
    };
    static final WrapperAwareSerializer WRAPPER_AWARE_SERIALIZER = new WrapperAwareSerializer();

    private PaperAdventure() {
    }

    // Key

    public static ResourceLocation asVanilla(final Key key) {
        return new ResourceLocation(key.namespace(), key.value());
    }

    public static ResourceLocation asVanillaNullable(final Key key) {
        if (key == null) {
            return null;
        }
        return new ResourceLocation(key.namespace(), key.value());
    }

    // Component

    public static Component asAdventure(final net.minecraft.network.chat.Component component) {
        return component == null ? Component.empty() : GSON.serializer().fromJson(net.minecraft.network.chat.Component.Serializer.toJsonTree(component), Component.class);
    }

    public static ArrayList<Component> asAdventure(final List<net.minecraft.network.chat.Component> vanillas) {
        final ArrayList<Component> adventures = new ArrayList<>(vanillas.size());
        for (final net.minecraft.network.chat.Component vanilla : vanillas) {
            adventures.add(asAdventure(vanilla));
        }
        return adventures;
    }

    public static ArrayList<Component> asAdventureFromJson(final List<String> jsonStrings) {
        final ArrayList<Component> adventures = new ArrayList<>(jsonStrings.size());
        for (final String json : jsonStrings) {
            adventures.add(GsonComponentSerializer.gson().deserialize(json));
        }
        return adventures;
    }

    public static List<String> asJson(final List<Component> adventures) {
        final List<String> jsons = new ArrayList<>(adventures.size());
        for (final Component component : adventures) {
            jsons.add(GsonComponentSerializer.gson().serialize(component));
        }
        return jsons;
    }

    public static net.minecraft.network.chat.Component asVanilla(final Component component) {
        if (true) return new AdventureComponent(component);
        return net.minecraft.network.chat.Component.Serializer.fromJson(GSON.serializer().toJsonTree(component));
    }

    public static List<net.minecraft.network.chat.Component> asVanilla(final List<Component> adventures) {
        final List<net.minecraft.network.chat.Component> vanillas = new ArrayList<>(adventures.size());
        for (final Component adventure : adventures) {
            vanillas.add(asVanilla(adventure));
        }
        return vanillas;
    }

    public static String asJsonString(final Component component, final Locale locale) {
        return GSON.serialize(
            GlobalTranslator.render(
                component,
                // play it safe
                locale != null
                    ? locale
                    : Locale.US
            )
        );
    }

    public static String asJsonString(final net.minecraft.network.chat.Component component, final Locale locale) {
        if (component instanceof AdventureComponent) {
            return asJsonString(((AdventureComponent) component).wrapped, locale);
        }
        return net.minecraft.network.chat.Component.Serializer.toJson(component);
    }

    public static String asPlain(final Component component, final Locale locale) {
        return PLAIN.serialize(
            GlobalTranslator.render(
                component,
                // play it safe
                locale != null
                    ? locale
                    : Locale.US
            )
        );
    }

    // thank you for being worse than wet socks, Bukkit
    public static String superHackyLegacyRepresentationOfComponent(final Component component, final String string) {
        return LEGACY_SECTION_UXRC.serialize(component) + ChatColor.getLastColors(string);
    }

    // BossBar

    public static BossEvent.BossBarColor asVanilla(final BossBar.Color color) {
        if (color == BossBar.Color.PINK) {
            return BossEvent.BossBarColor.PINK;
        } else if (color == BossBar.Color.BLUE) {
            return BossEvent.BossBarColor.BLUE;
        } else if (color == BossBar.Color.RED) {
            return BossEvent.BossBarColor.RED;
        } else if (color == BossBar.Color.GREEN) {
            return BossEvent.BossBarColor.GREEN;
        } else if (color == BossBar.Color.YELLOW) {
            return BossEvent.BossBarColor.YELLOW;
        } else if (color == BossBar.Color.PURPLE) {
            return BossEvent.BossBarColor.PURPLE;
        } else if (color == BossBar.Color.WHITE) {
            return BossEvent.BossBarColor.WHITE;
        }
        throw new IllegalArgumentException(color.name());
    }

    public static BossBar.Color asAdventure(final BossEvent.BossBarColor color) {
        if(color == BossEvent.BossBarColor.PINK) {
            return BossBar.Color.PINK;
        } else if(color == BossEvent.BossBarColor.BLUE) {
            return BossBar.Color.BLUE;
        } else if(color == BossEvent.BossBarColor.RED) {
            return BossBar.Color.RED;
        } else if(color == BossEvent.BossBarColor.GREEN) {
            return BossBar.Color.GREEN;
        } else if(color == BossEvent.BossBarColor.YELLOW) {
            return BossBar.Color.YELLOW;
        } else if(color == BossEvent.BossBarColor.PURPLE) {
            return BossBar.Color.PURPLE;
        } else if(color == BossEvent.BossBarColor.WHITE) {
            return BossBar.Color.WHITE;
        }
        throw new IllegalArgumentException(color.name());
    }

    public static BossEvent.BossBarOverlay asVanilla(final BossBar.Overlay overlay) {
        if (overlay == BossBar.Overlay.PROGRESS) {
            return BossEvent.BossBarOverlay.PROGRESS;
        } else if (overlay == BossBar.Overlay.NOTCHED_6) {
            return BossEvent.BossBarOverlay.NOTCHED_6;
        } else if (overlay == BossBar.Overlay.NOTCHED_10) {
            return BossEvent.BossBarOverlay.NOTCHED_10;
        } else if (overlay == BossBar.Overlay.NOTCHED_12) {
            return BossEvent.BossBarOverlay.NOTCHED_12;
        } else if (overlay == BossBar.Overlay.NOTCHED_20) {
            return BossEvent.BossBarOverlay.NOTCHED_20;
        }
        throw new IllegalArgumentException(overlay.name());
    }

    public static BossBar.Overlay asAdventure(final BossEvent.BossBarOverlay overlay) {
        if (overlay == BossEvent.BossBarOverlay.PROGRESS) {
            return BossBar.Overlay.PROGRESS;
        } else if (overlay == BossEvent.BossBarOverlay.NOTCHED_6) {
            return BossBar.Overlay.NOTCHED_6;
        } else if (overlay == BossEvent.BossBarOverlay.NOTCHED_10) {
            return BossBar.Overlay.NOTCHED_10;
        } else if (overlay == BossEvent.BossBarOverlay.NOTCHED_12) {
            return BossBar.Overlay.NOTCHED_12;
        } else if (overlay == BossEvent.BossBarOverlay.NOTCHED_20) {
            return BossBar.Overlay.NOTCHED_20;
        }
        throw new IllegalArgumentException(overlay.name());
    }

    public static void setFlag(final BossBar bar, final BossBar.Flag flag, final boolean value) {
        if (value) {
            bar.addFlag(flag);
        } else {
            bar.removeFlag(flag);
        }
    }

    // Book

    public static ItemStack asItemStack(final Book book, final Locale locale) {
        final ItemStack item = new ItemStack(net.minecraft.world.item.Items.WRITTEN_BOOK, 1);
        final CompoundTag tag = item.getOrCreateTag();
        tag.putString(WrittenBookItem.TAG_TITLE, validateField(asPlain(book.title(), locale), WrittenBookItem.TITLE_MAX_LENGTH, WrittenBookItem.TAG_TITLE));
        tag.putString(WrittenBookItem.TAG_AUTHOR, asPlain(book.author(), locale));
        final ListTag pages = new ListTag();
        if (book.pages().size() > WrittenBookItem.MAX_PAGES) {
            throw new IllegalArgumentException("Book provided had " + book.pages().size() + " pages, but is only allowed a maximum of " + WrittenBookItem.MAX_PAGES);
        }
        for (final Component page : book.pages()) {
            pages.add(StringTag.valueOf(validateField(asJsonString(page, locale), WrittenBookItem.PAGE_LENGTH, "page")));
        }
        tag.put(WrittenBookItem.TAG_PAGES, pages);
        return item;
    }

    private static String validateField(final String content, final int length, final String name) {
        if (content == null) {
            return content;
        }

        final int actual = content.length();
        if (actual > length) {
            throw new IllegalArgumentException("Field '" + name + "' has a maximum length of " + length + " but was passed '" + content + "', which was " + actual + " characters long.");
        }
        return content;
    }

    // Sounds

    public static SoundSource asVanilla(final Sound.Source source) {
        if (source == Sound.Source.MASTER) {
            return SoundSource.MASTER;
        } else if (source == Sound.Source.MUSIC) {
            return SoundSource.MUSIC;
        } else if (source == Sound.Source.RECORD) {
            return SoundSource.RECORDS;
        } else if (source == Sound.Source.WEATHER) {
            return SoundSource.WEATHER;
        } else if (source == Sound.Source.BLOCK) {
            return SoundSource.BLOCKS;
        } else if (source == Sound.Source.HOSTILE) {
            return SoundSource.HOSTILE;
        } else if (source == Sound.Source.NEUTRAL) {
            return SoundSource.NEUTRAL;
        } else if (source == Sound.Source.PLAYER) {
            return SoundSource.PLAYERS;
        } else if (source == Sound.Source.AMBIENT) {
            return SoundSource.AMBIENT;
        } else if (source == Sound.Source.VOICE) {
            return SoundSource.VOICE;
        }
        throw new IllegalArgumentException(source.name());
    }

    public static @Nullable SoundSource asVanillaNullable(final Sound.@Nullable Source source) {
        if (source == null) {
            return null;
        }
        return asVanilla(source);
    }

    // NBT

    public static @Nullable BinaryTagHolder asBinaryTagHolder(final @Nullable CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        try {
            return BinaryTagHolder.encode(tag, NBT_CODEC);
        } catch (final IOException e) {
            return null;
        }
    }

    // Colors

    public static @NonNull TextColor asAdventure(ChatFormatting minecraftColor) {
        if (minecraftColor.getColor() == null) {
            throw new IllegalArgumentException("Not a valid color");
        }
        return TextColor.color(minecraftColor.getColor());
    }

    public static @Nullable ChatFormatting asVanilla(TextColor color) {
        return ChatFormatting.getByHexValue(color.value());
    }
}
