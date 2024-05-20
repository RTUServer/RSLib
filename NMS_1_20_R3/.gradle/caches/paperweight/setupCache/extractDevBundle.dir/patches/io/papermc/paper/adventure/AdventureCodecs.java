package io.papermc.paper.adventure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.EntityNBTComponent;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.NBTComponentBuilder;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.StorageNBTComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.intellij.lang.annotations.Subst;

import static com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec;
import static java.util.function.Function.identity;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.TranslationArgument.bool;
import static net.kyori.adventure.text.TranslationArgument.component;
import static net.kyori.adventure.text.TranslationArgument.numeric;
import static net.minecraft.util.ExtraCodecs.recursive;
import static net.minecraft.util.ExtraCodecs.strictOptionalField;

@DefaultQualifier(NonNull.class)
public final class AdventureCodecs {

    public static final Codec<Component> COMPONENT_CODEC = recursive("adventure Component",  AdventureCodecs::createCodec);

    static final Codec<TextColor> TEXT_COLOR_CODEC = Codec.STRING.comapFlatMap(s -> {
        if (s.startsWith("#")) {
            @Nullable TextColor value = TextColor.fromHexString(s);
            return value != null ? DataResult.success(value) : DataResult.error(() -> "Cannot convert " + s + " to adventure TextColor");
        } else {
            final @Nullable NamedTextColor value = NamedTextColor.NAMES.value(s);
            return value != null ? DataResult.success(value) : DataResult.error(() -> "Cannot convert " + s + " to adventure NamedTextColor");
        }
    }, textColor -> {
        if (textColor instanceof NamedTextColor named) {
            return NamedTextColor.NAMES.keyOrThrow(named);
        } else {
            return textColor.asHexString();
        }
    });

    static final Codec<Key> KEY_CODEC = Codec.STRING.comapFlatMap(s -> {
        return Key.parseable(s) ? DataResult.success(Key.key(s)) : DataResult.error(() -> "Cannot convert " + s + " to adventure Key");
    }, Key::asString);

    static final Codec<ClickEvent.Action> CLICK_EVENT_ACTION_CODEC = Codec.STRING.comapFlatMap(s -> {
        final ClickEvent.@Nullable Action value = ClickEvent.Action.NAMES.value(s);
        return value != null ? DataResult.success(value) : DataResult.error(() -> "Cannot convert " + s + " to adventure ClickEvent$Action");
    }, ClickEvent.Action.NAMES::keyOrThrow);
    static final Codec<ClickEvent> CLICK_EVENT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
            CLICK_EVENT_ACTION_CODEC.fieldOf("action").forGetter(ClickEvent::action),
            Codec.STRING.fieldOf("value").forGetter(ClickEvent::value)
        ).apply(instance, ClickEvent::clickEvent);
    });

    static Codec<HoverEvent.ShowEntity> showEntityCodec(final Codec<Component> componentCodec) {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(
                KEY_CODEC.fieldOf("type").forGetter(HoverEvent.ShowEntity::type),
                UUIDUtil.LENIENT_CODEC.fieldOf("id").forGetter(HoverEvent.ShowEntity::id),
                strictOptionalField(componentCodec, "name").forGetter(he -> Optional.ofNullable(he.name()))
            ).apply(instance, (key, uuid, component) -> {
                return HoverEvent.ShowEntity.showEntity(key, uuid, component.orElse(null));
            });
        });
    }

    static Codec<HoverEvent.ShowItem> showItemCodec(final Codec<Component> componentCodec) {
        return net.minecraft.network.chat.HoverEvent.ItemStackInfo.CODEC.xmap(isi -> {
            @Subst("key") final String typeKey = BuiltInRegistries.ITEM.getKey(isi.item).toString();
            return HoverEvent.ShowItem.showItem(Key.key(typeKey), isi.count, PaperAdventure.asBinaryTagHolder(isi.tag.orElse(null)));
        }, si -> {
            final Item itemType = BuiltInRegistries.ITEM.get(PaperAdventure.asVanilla(si.item()));
            final ItemStack stack;
            try {
                final @Nullable CompoundTag tag = si.nbt() != null ? si.nbt().get(PaperAdventure.NBT_CODEC) : null;
                stack = new ItemStack(BuiltInRegistries.ITEM.wrapAsHolder(itemType), si.count(), Optional.ofNullable(tag));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return new net.minecraft.network.chat.HoverEvent.ItemStackInfo(stack);
        });
    }

    static final HoverEventType<HoverEvent.ShowEntity> SHOW_ENTITY_HOVER_EVENT_TYPE = new HoverEventType<>(AdventureCodecs::showEntityCodec, HoverEvent.Action.SHOW_ENTITY, "show_entity", AdventureCodecs::legacyDeserializeEntity);
    static final HoverEventType<HoverEvent.ShowItem> SHOW_ITEM_HOVER_EVENT_TYPE = new HoverEventType<>(AdventureCodecs::showItemCodec, HoverEvent.Action.SHOW_ITEM, "show_item", AdventureCodecs::legacyDeserializeItem);
    static final HoverEventType<Component> SHOW_TEXT_HOVER_EVENT_TYPE = new HoverEventType<>(identity(), HoverEvent.Action.SHOW_TEXT, "show_text", DataResult::success);
    static final Codec<HoverEventType<?>> HOVER_EVENT_TYPE_CODEC = StringRepresentable.fromValues(() -> new HoverEventType<?>[]{ SHOW_ENTITY_HOVER_EVENT_TYPE, SHOW_ITEM_HOVER_EVENT_TYPE, SHOW_TEXT_HOVER_EVENT_TYPE });

    static DataResult<HoverEvent.ShowEntity> legacyDeserializeEntity(final Component text) {
        try {
            final CompoundTag tag = TagParser.parseTag(PlainTextComponentSerializer.plainText().serialize(text));
            final @Nullable Component entityName = GsonComponentSerializer.gson().deserializeOrNull(tag.getString("name"));
            @Subst("key") final String keyString = tag.getString("type");
            final UUID entityUUID = UUID.fromString(tag.getString("id"));
            return DataResult.success(HoverEvent.ShowEntity.showEntity(Key.key(keyString), entityUUID, entityName));
        } catch (final Exception ex) {
            return DataResult.error(() -> "Failed to parse tooltip: " + ex.getMessage());
        }
    }

    static DataResult<HoverEvent.ShowItem> legacyDeserializeItem(final Component text) {
        try {
            final ItemStack stack = ItemStack.of(TagParser.parseTag(PlainTextComponentSerializer.plainText().serialize(text)));
            @Subst("key") final String keyString = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            return DataResult.success(HoverEvent.ShowItem.showItem(Key.key(keyString), stack.getCount(), stack.getTag() != null ? BinaryTagHolder.encode(stack.getTag(), PaperAdventure.NBT_CODEC) : null));
        } catch (final CommandSyntaxException | IOException ex) {
            return DataResult.error(() -> "Failed to parse item tag: " + ex.getMessage());
        }
    }

    record HoverEventType<V>(Function<Codec<Component>, Codec<HoverEvent<V>>> codec, String id, Function<Codec<Component>, Codec<HoverEvent<V>>> legacyCodec) implements StringRepresentable {
        HoverEventType(final Function<Codec<Component>, Codec<V>> contentCodec, final HoverEvent.Action<V> action, final String id, final Function<Component, DataResult<V>> legacyDeserializer) {
            this(cc -> contentCodec.apply(cc).xmap(v -> HoverEvent.hoverEvent(action, v), HoverEvent::value).fieldOf("contents").codec(),
                id,
                codec -> Codec.of(
                    Encoder.error("Can't encode in legacy format"),
                    codec.flatMap(legacyDeserializer).map(text -> HoverEvent.hoverEvent(action, text))
                )
            );
        }
        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    private static final Function<HoverEvent<?>, HoverEventType<?>> GET_HOVER_EVENT_TYPE = he -> {
        if (he.action() == HoverEvent.Action.SHOW_ENTITY) {
            return SHOW_ENTITY_HOVER_EVENT_TYPE;
        } else if (he.action() == HoverEvent.Action.SHOW_ITEM) {
            return SHOW_ITEM_HOVER_EVENT_TYPE;
        } else if (he.action() == HoverEvent.Action.SHOW_TEXT) {
            return SHOW_TEXT_HOVER_EVENT_TYPE;
        } else {
            throw new IllegalStateException();
        }
    };
    static final Codec<HoverEvent<?>> HOVER_EVENT_CODEC = Codec.either(
        HOVER_EVENT_TYPE_CODEC.<HoverEvent<?>>dispatchMap("action", GET_HOVER_EVENT_TYPE, het -> het.codec.apply(COMPONENT_CODEC)).codec(),
        HOVER_EVENT_TYPE_CODEC.<HoverEvent<?>>dispatchMap("action", GET_HOVER_EVENT_TYPE, het -> het.legacyCodec.apply(COMPONENT_CODEC)).codec()
    ).xmap(either -> either.map(identity(), identity()), Either::left);

    public static final MapCodec<Style> STYLE_MAP_CODEC = mapCodec((instance) -> {
        return instance.group(
            strictOptionalField(TEXT_COLOR_CODEC, "color").forGetter(nullableGetter(Style::color)),
            strictOptionalField(Codec.BOOL, "bold").forGetter(decorationGetter(TextDecoration.BOLD)),
            strictOptionalField(Codec.BOOL, "italic").forGetter(decorationGetter(TextDecoration.ITALIC)),
            strictOptionalField(Codec.BOOL, "underlined").forGetter(decorationGetter(TextDecoration.UNDERLINED)),
            strictOptionalField(Codec.BOOL, "strikethrough").forGetter(decorationGetter(TextDecoration.STRIKETHROUGH)),
            strictOptionalField(Codec.BOOL, "obfuscated").forGetter(decorationGetter(TextDecoration.OBFUSCATED)),
            strictOptionalField(CLICK_EVENT_CODEC, "clickEvent").forGetter(nullableGetter(Style::clickEvent)),
            strictOptionalField(HOVER_EVENT_CODEC, "hoverEvent").forGetter(nullableGetter(Style::hoverEvent)),
            strictOptionalField(Codec.STRING, "insertion").forGetter(nullableGetter(Style::insertion)),
            strictOptionalField(KEY_CODEC, "font").forGetter(nullableGetter(Style::font))
        ).apply(instance, (textColor, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent, insertion, font) -> {
            return Style.style(builder -> {
                textColor.ifPresent(builder::color);
                bold.ifPresent(styleBooleanConsumer(builder, TextDecoration.BOLD));
                italic.ifPresent(styleBooleanConsumer(builder, TextDecoration.ITALIC));
                underlined.ifPresent(styleBooleanConsumer(builder, TextDecoration.UNDERLINED));
                strikethrough.ifPresent(styleBooleanConsumer(builder, TextDecoration.STRIKETHROUGH));
                obfuscated.ifPresent(styleBooleanConsumer(builder, TextDecoration.OBFUSCATED));
                clickEvent.ifPresent(builder::clickEvent);
                hoverEvent.ifPresent(builder::hoverEvent);
                insertion.ifPresent(builder::insertion);
                font.ifPresent(builder::font);
            });
        });
    });
    static Consumer<Boolean> styleBooleanConsumer(final Style.Builder builder, final TextDecoration decoration) {
        return b -> builder.decoration(decoration, b);
    }

    static Function<Style, Optional<Boolean>> decorationGetter(final TextDecoration decoration) {
        return style -> Optional.ofNullable(style.decoration(decoration) == TextDecoration.State.NOT_SET ? null : style.decoration(decoration) == TextDecoration.State.TRUE);
    }

    static <R, T> Function<R, Optional<T>> nullableGetter(final Function<R, @Nullable T> getter) {
        return style -> Optional.ofNullable(getter.apply(style));
    }

    static final MapCodec<TextComponent> TEXT_COMPONENT_MAP_CODEC = mapCodec((instance) -> {
        return instance.group(Codec.STRING.fieldOf("text").forGetter(TextComponent::content)).apply(instance, Component::text);
    });
    static final Codec<Object> PRIMITIVE_ARG_CODEC = ExtraCodecs.validate(ExtraCodecs.JAVA, TranslatableContents::filterAllowedArguments);
    static final Codec<TranslationArgument> ARG_CODEC = Codec.either(PRIMITIVE_ARG_CODEC, COMPONENT_CODEC).flatXmap((primitiveOrComponent) -> {
        return primitiveOrComponent.map(o -> {
            final TranslationArgument arg;
            if (o instanceof String s) {
                arg = component(text(s));
            } else if (o instanceof Boolean bool) {
                arg = bool(bool);
            } else if (o instanceof Number num) {
                arg = numeric(num);
            } else {
                return DataResult.error(() -> o + " is not a valid translation argument primitive");
            }
            return DataResult.success(arg);
        }, component -> DataResult.success(component(component)));
    }, translationArgument -> {
        if (translationArgument.value() instanceof Number || translationArgument.value() instanceof Boolean) {
            return DataResult.success(Either.left(translationArgument.value()));
        }
        final Component component = translationArgument.asComponent();
        final @Nullable String collapsed = tryCollapseToString(component);
        if (collapsed != null) {
            return DataResult.success(Either.left(collapsed)); // attempt to collapse all text components to strings
        }
        return DataResult.success(Either.right(component));
    });
    static final MapCodec<TranslatableComponent> TRANSLATABLE_COMPONENT_MAP_CODEC = mapCodec((instance) -> {
        return instance.group(
            Codec.STRING.fieldOf("translate").forGetter(TranslatableComponent::key),
            Codec.STRING.optionalFieldOf("fallback").forGetter(nullableGetter(TranslatableComponent::fallback)),
            strictOptionalField(ARG_CODEC.listOf(), "with").forGetter(c -> c.arguments().isEmpty() ? Optional.empty() : Optional.of(c.arguments()))
        ).apply(instance, (key, fallback, components) -> {
            return Component.translatable(key, components.orElse(Collections.emptyList())).fallback(fallback.orElse(null));
        });
    });

    static final MapCodec<KeybindComponent> KEYBIND_COMPONENT_MAP_CODEC = KeybindContents.CODEC.xmap(k -> Component.keybind(k.getName()), k -> new KeybindContents(k.keybind()));
    static final MapCodec<ScoreComponent> SCORE_COMPONENT_INNER_MAP_CODEC = ScoreContents.INNER_CODEC.xmap(s -> Component.score(s.getName(), s.getObjective()), s -> new ScoreContents(s.name(), s.objective()));
    static final MapCodec<ScoreComponent> SCORE_COMPONENT_MAP_CODEC = SCORE_COMPONENT_INNER_MAP_CODEC.fieldOf("score");
    static final MapCodec<SelectorComponent> SELECTOR_COMPONENT_MAP_CODEC = mapCodec((instance) -> {
        return instance.group(
            Codec.STRING.fieldOf("selector").forGetter(SelectorComponent::pattern),
            strictOptionalField(COMPONENT_CODEC, "separator").forGetter(nullableGetter(SelectorComponent::separator))
        ).apply(instance, (selector, component) -> Component.selector(selector, component.orElse(null)));
    });

    interface NbtComponentDataSource {
        NBTComponentBuilder<?, ?> builder();

        DataSourceType<?> type();
    }

    record StorageDataSource(Key storage) implements NbtComponentDataSource {
        @Override
        public NBTComponentBuilder<?, ?> builder() {
            return Component.storageNBT().storage(this.storage());
        }

        @Override
        public DataSourceType<?> type() {
            return STORAGE_DATA_SOURCE_TYPE;
        }
    }

    record BlockDataSource(String posPattern) implements NbtComponentDataSource {
        @Override
        public NBTComponentBuilder<?, ?> builder() {
            return Component.blockNBT().pos(BlockNBTComponent.Pos.fromString(this.posPattern));
        }

        @Override
        public DataSourceType<?> type() {
            return BLOCK_DATA_SOURCE_TYPE;
        }
    }

    record EntityDataSource(String selectorPattern) implements NbtComponentDataSource {
        @Override
        public NBTComponentBuilder<?, ?> builder() {
            return Component.entityNBT().selector(this.selectorPattern());
        }

        @Override
        public DataSourceType<?> type() {
            return ENTITY_DATA_SOURCE_TYPE;
        }
    }

    static final DataSourceType<StorageDataSource> STORAGE_DATA_SOURCE_TYPE = new DataSourceType<>(mapCodec((instance) -> instance.group(KEY_CODEC.fieldOf("storage").forGetter(StorageDataSource::storage)).apply(instance, StorageDataSource::new)), "storage");
    static final DataSourceType<BlockDataSource> BLOCK_DATA_SOURCE_TYPE = new DataSourceType<>(mapCodec((instance) -> instance.group(Codec.STRING.fieldOf("block").forGetter(BlockDataSource::posPattern)).apply(instance, BlockDataSource::new)), "block");
    static final DataSourceType<EntityDataSource> ENTITY_DATA_SOURCE_TYPE = new DataSourceType<>(mapCodec((instance) -> instance.group(Codec.STRING.fieldOf("entity").forGetter(EntityDataSource::selectorPattern)).apply(instance, EntityDataSource::new)), "entity");

    static final MapCodec<NbtComponentDataSource> NBT_COMPONENT_DATA_SOURCE_CODEC = ComponentSerialization.createLegacyComponentMatcher(new DataSourceType<?>[]{ENTITY_DATA_SOURCE_TYPE, BLOCK_DATA_SOURCE_TYPE, STORAGE_DATA_SOURCE_TYPE}, DataSourceType::codec, NbtComponentDataSource::type, "source");

    record DataSourceType<D extends NbtComponentDataSource>(MapCodec<D> codec, String id) implements StringRepresentable {
        @Override
        public String getSerializedName() {
            return this.id();
        }
    }

    static final MapCodec<NBTComponent<?, ?>> NBT_COMPONENT_MAP_CODEC = mapCodec((instance) -> {
        return instance.group(
            Codec.STRING.fieldOf("nbt").forGetter(NBTComponent::nbtPath),
            Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NBTComponent::interpret),
            COMPONENT_CODEC.optionalFieldOf("separator").forGetter(nullableGetter(NBTComponent::separator)),
            NBT_COMPONENT_DATA_SOURCE_CODEC.forGetter(nbtComponent -> {
                if (nbtComponent instanceof final EntityNBTComponent entityNBTComponent) {
                    return new EntityDataSource(entityNBTComponent.selector());
                } else if (nbtComponent instanceof final BlockNBTComponent blockNBTComponent) {
                    return new BlockDataSource(blockNBTComponent.pos().asString());
                } else if (nbtComponent instanceof final StorageNBTComponent storageNBTComponent) {
                    return new StorageDataSource(storageNBTComponent.storage());
                } else {
                    throw new IllegalArgumentException(nbtComponent + " isn't a valid nbt component");
                }
            })
        ).apply(instance, (nbtPath, interpret, separator, dataSource) -> {
            return dataSource.builder().nbtPath(nbtPath).interpret(interpret).separator(separator.orElse(null)).build();
        });
    });

    @SuppressWarnings("NonExtendableApiUsage")
    record ComponentType<C extends Component>(MapCodec<C> codec, Predicate<Component> test, String id) implements StringRepresentable {
        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    static final ComponentType<TextComponent> PLAIN = new ComponentType<>(TEXT_COMPONENT_MAP_CODEC, TextComponent.class::isInstance, "text");
    static final ComponentType<TranslatableComponent> TRANSLATABLE = new ComponentType<>(TRANSLATABLE_COMPONENT_MAP_CODEC, TranslatableComponent.class::isInstance, "translatable");
    static final ComponentType<KeybindComponent> KEYBIND = new ComponentType<>(KEYBIND_COMPONENT_MAP_CODEC, KeybindComponent.class::isInstance, "keybind");
    static final ComponentType<ScoreComponent> SCORE = new ComponentType<>(SCORE_COMPONENT_MAP_CODEC, ScoreComponent.class::isInstance, "score");
    static final ComponentType<SelectorComponent> SELECTOR = new ComponentType<>(SELECTOR_COMPONENT_MAP_CODEC, SelectorComponent.class::isInstance, "selector");
    static final ComponentType<NBTComponent<?, ?>> NBT = new ComponentType<>(NBT_COMPONENT_MAP_CODEC, NBTComponent.class::isInstance, "nbt");

    static Codec<Component> createCodec(final Codec<Component> selfCodec) {
        final ComponentType<?>[] types = new ComponentType<?>[]{PLAIN, TRANSLATABLE, KEYBIND, SCORE, SELECTOR, NBT};
        final MapCodec<Component> legacyCodec = ComponentSerialization.createLegacyComponentMatcher(types, ComponentType::codec, component -> {
            for (final ComponentType<?> type : types) {
                if (type.test().test(component)) {
                    return type;
                }
            }
            throw new IllegalStateException("Unexpected component type " + component);
        }, "type");

        final Codec<Component> directCodec = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                legacyCodec.forGetter(identity()),
                strictOptionalField(ExtraCodecs.nonEmptyList(selfCodec.listOf()), "extra", List.of()).forGetter(Component::children),
                STYLE_MAP_CODEC.forGetter(Component::style)
            ).apply(instance, (component, children, style) -> {
                return component.style(style).children(children);
            });
        });

        return Codec.either(Codec.either(Codec.STRING, ExtraCodecs.nonEmptyList(selfCodec.listOf())), directCodec).xmap((stringOrListOrComponent) -> {
            return stringOrListOrComponent.map((stringOrList) -> stringOrList.map(Component::text, AdventureCodecs::createFromList), identity());
        }, (text) -> {
            final @Nullable String string = tryCollapseToString(text);
            return string != null ? Either.left(Either.left(string)) : Either.right(text);
        });
    }

    public static @Nullable String tryCollapseToString(final Component component) {
        if (component instanceof final TextComponent textComponent) {
            if (component.children().isEmpty() && component.style().isEmpty()) {
                return textComponent.content();
            }
        }
        return null;
    }

    static Component createFromList(final List<? extends Component> components) {
        Component component = components.get(0);
        for (int i = 1; i < components.size(); i++) {
            component = component.append(components.get(i));
        }
        return component;
    }

    private AdventureCodecs() {
    }
}
