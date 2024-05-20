package com.destroystokyo.paper;

import com.destroystokyo.paper.io.SyncLoadFinder;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MCUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MCUtil;
import net.minecraft.world.level.NaturalSpawner;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.MutablePair;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class PaperCommand extends Command {
    private static final String BASE_PERM = "bukkit.command.paper.";
    private static final ImmutableSet<String> SUBCOMMANDS = ImmutableSet.<String>builder().add("heap", "entity", "reload", "version", "debug", "chunkinfo", "fixlight", "syncloadinfo", "dumpitem", "mobcaps", "playermobcaps").build();

    public PaperCommand(String name) {
        super(name);
        this.description = "Paper related commands";
        this.usageMessage = "/paper [" + Joiner.on(" | ").join(SUBCOMMANDS) + "]";
        this.setPermission("bukkit.command.paper;" + Joiner.on(';').join(SUBCOMMANDS.stream().map(s -> BASE_PERM + s).collect(Collectors.toSet())));
    }

    private static boolean testPermission(CommandSender commandSender, String permission) {
        if (commandSender.hasPermission(BASE_PERM + permission) || commandSender.hasPermission("bukkit.command.paper")) return true;
        commandSender.sendMessage(Bukkit.getPermissionMessage()); // Sorry, kashike
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        if (args.length <= 1)
            return getListMatchingLast(sender, args, SUBCOMMANDS);

        switch (args[0].toLowerCase(Locale.ENGLISH))
        {
            case "entity":
                if (args.length == 2)
                    return getListMatchingLast(sender, args, "help", "list");
                if (args.length == 3)
                    return getListMatchingLast(sender, args, EntityType.getEntityNameList().stream().map(ResourceLocation::toString).sorted().toArray(String[]::new));
                break;
            case "debug":
                if (args.length == 2) {
                    return getListMatchingLast(sender, args, "help", "chunks");
                }
                break;
            case "mobcaps":
                return getListMatchingLast(sender, args, this.suggestMobcaps(sender, args));
            case "playermobcaps":
                return getListMatchingLast(sender, args, this.suggestPlayerMobcaps(sender, args));
            case "chunkinfo":
                List<String> worldNames = new ArrayList<>();
                worldNames.add("*");
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    worldNames.add(world.getName());
                }
                if (args.length == 2) {
                    return getListMatchingLast(sender, args, worldNames);
                }
                break;
            case "syncloadinfo":
                if (args.length == 2) {
                    return getListMatchingLast(sender, args, "clear");
                }
                break;
        }
        return Collections.emptyList();
    }

    // Code from Mojang - copyright them
    public static List<String> getListMatchingLast(CommandSender sender, String[] args, String... matches) {
        return getListMatchingLast(sender, args, (Collection) Arrays.asList(matches));
    }

    public static boolean matches(String s, String s1) {
        return s1.regionMatches(true, 0, s, 0, s.length());
    }

    public static List<String> getListMatchingLast(CommandSender sender, String[] strings, Collection<?> collection) {
        String last = strings[strings.length - 1];
        ArrayList<String> results = Lists.newArrayList();

        if (!collection.isEmpty()) {
            Iterator iterator = Iterables.transform(collection, Functions.toStringFunction()).iterator();

            while (iterator.hasNext()) {
                String s1 = (String) iterator.next();

                if (matches(last, s1) && (sender.hasPermission(BASE_PERM + s1) || sender.hasPermission("bukkit.command.paper"))) {
                    results.add(s1);
                }
            }

            if (results.isEmpty()) {
                iterator = collection.iterator();

                while (iterator.hasNext()) {
                    Object object = iterator.next();

                    if (object instanceof ResourceLocation && matches(last, ((ResourceLocation) object).getPath())) {
                        results.add(String.valueOf(object));
                    }
                }
            }
        }

        return results;
    }
    // end copy stuff

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }
        if (SUBCOMMANDS.contains(args[0].toLowerCase(Locale.ENGLISH))) {
            if (!testPermission(sender, args[0].toLowerCase(Locale.ENGLISH))) return true;
        }
        switch (args[0].toLowerCase(Locale.ENGLISH))  {
            case "heap":
                dumpHeap(sender);
                break;
            case "entity":
                listEntities(sender, args);
                break;
            case "reload":
                doReload(sender);
                break;
            case "dumpitem":
                doDumpItem(sender);
                break;
            case "debug":
                doDebug(sender, args);
                break;
            case "chunkinfo":
                doChunkInfo(sender, args);
                break;
            case "fixlight":
                this.doFixLight(sender, args);
                break;
            case "syncloadinfo":
                this.doSyncLoadInfo(sender, args);
                break;
            case "mobcaps":
                this.printMobcaps(sender, args);
                break;
            case "playermobcaps":
                this.printPlayerMobcaps(sender, args);
                break;
            case "ver":
                if (!testPermission(sender, "version")) break; // "ver" needs a special check because it's an alias. All other commands are checked up before the switch statement (because they are present in the SUBCOMMANDS set)
            case "version":
                Command ver = MinecraftServer.getServer().server.getCommandMap().getCommand("version");
                if (ver != null) {
                    ver.execute(sender, commandLabel, new String[0]);
                    break;
                }
                // else - fall through to default
            default:
                sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
                return false;
        }

        return true;
    }

    private void doSyncLoadInfo(CommandSender sender, String[] args) {
        if (!SyncLoadFinder.ENABLED) {
            sender.sendMessage(ChatColor.RED + "This command requires the server startup flag '-Dpaper.debug-sync-loads=true' to be set.");
            return;
        }

        if (args.length > 1 && args[1].equals("clear")) {
            SyncLoadFinder.clear();
            sender.sendMessage(ChatColor.GRAY + "Sync load data cleared.");
            return;
        }

        File file = new File(new File(new File("."), "debug"),
            "sync-load-info" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now()) + ".txt");
        file.getParentFile().mkdirs();
        sender.sendMessage(ChatColor.GREEN + "Writing sync load info to " + file.toString());


        try {
            final JsonObject data = SyncLoadFinder.serialize();

            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setIndent(" ");
            jsonWriter.setLenient(false);
            Streams.write(data, jsonWriter);

            String fileData = stringWriter.toString();

            try (
                PrintStream out = new PrintStream(new FileOutputStream(file), false, "UTF-8")
            ) {
                out.print(fileData);
            }
            sender.sendMessage(ChatColor.GREEN + "Successfully written sync load information!");
        } catch (Throwable thr) {
            sender.sendMessage(ChatColor.RED + "Failed to write sync load information");
            thr.printStackTrace();
        }
    }

    public static final Map<MobCategory, TextColor> MOB_CATEGORY_COLORS = ImmutableMap.<MobCategory, TextColor>builder()
        .put(MobCategory.MONSTER, NamedTextColor.RED)
        .put(MobCategory.CREATURE, NamedTextColor.GREEN)
        .put(MobCategory.AMBIENT, NamedTextColor.GRAY)
        .put(MobCategory.UNDERGROUND_WATER_CREATURE, TextColor.color(0x3541E6))
        .put(MobCategory.WATER_CREATURE, TextColor.color(0x006EFF))
        .put(MobCategory.WATER_AMBIENT, TextColor.color(0x00B3FF))
        .put(MobCategory.MISC, TextColor.color(0x636363))
        .build();

    private List<String> suggestMobcaps(CommandSender sender, String[] args) {
        if (args.length == 2) {
            final List<String> worlds = new ArrayList<>(Bukkit.getWorlds().stream().map(World::getName).toList());
            worlds.add("*");
            return worlds;
        }

        return Collections.emptyList();
    }

    private List<String> suggestPlayerMobcaps(CommandSender sender, String[] args) {
        if (args.length == 2) {
            final List<String> list = new ArrayList<>();
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (!(sender instanceof Player senderPlayer) || senderPlayer.canSee(player)) {
                    list.add(player.getName());
                }
            }
            return list;
        }

        return Collections.emptyList();
    }

    private void printMobcaps(CommandSender sender, String[] args) {
        final List<World> worlds;
        if (args.length == 1) {
            if (sender instanceof Player player) {
                worlds = List.of(player.getWorld());
            } else {
                sender.sendMessage(Component.text("Must specify a world! ex: '/paper mobcaps world'", NamedTextColor.RED));
                return;
            }
        } else if (args.length == 2) {
            final String input = args[1];
            if (input.equals("*")) {
                worlds = Bukkit.getWorlds();
            } else {
                final World world = Bukkit.getWorld(input);
                if (world == null) {
                    sender.sendMessage(Component.text("'" + input + "' is not a valid world!", NamedTextColor.RED));
                    return;
                } else {
                    worlds = List.of(world);
                }
            }
        } else {
            sender.sendMessage(Component.text("Too many arguments!", NamedTextColor.RED));
            return;
        }

        for (final World world : worlds) {
            final ServerLevel level = ((CraftWorld) world).getHandle();
            final NaturalSpawner.SpawnState state = level.getChunkSource().getLastSpawnState();

            final int chunks;
            if (state == null) {
                chunks = 0;
            } else {
                chunks = state.getSpawnableChunkCount();
            }
            sender.sendMessage(TextComponent.ofChildren(
                Component.text("Mobcaps for world: "),
                Component.text(world.getName(), NamedTextColor.AQUA),
                Component.text(" (" + chunks + " spawnable chunks)")
            ));

            sender.sendMessage(this.buildMobcapsComponent(
                category -> {
                    if (state == null) {
                        return 0;
                    } else {
                        return state.getMobCategoryCounts().getOrDefault(category, 0);
                    }
                },
                category -> NaturalSpawner.globalLimitForCategory(level, category, chunks)
            ));
        }
    }

    private void printPlayerMobcaps(CommandSender sender, String[] args) {
        final Player player;
        if (args.length == 1) {
            if (sender instanceof Player pl) {
                player = pl;
            } else {
                sender.sendMessage(Component.text("Must specify a player! ex: '/paper playermobcount playerName'", NamedTextColor.RED));
                return;
            }
        } else if (args.length == 2) {
            final String input = args[1];
            player = Bukkit.getPlayerExact(input);
            if (player == null) {
                sender.sendMessage(Component.text("Could not find player named '" + input + "'", NamedTextColor.RED));
                return;
            }
        } else {
            sender.sendMessage(Component.text("Too many arguments!", NamedTextColor.RED));
            return;
        }

        final ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        final ServerLevel level = serverPlayer.getLevel();

        if (!level.paperConfig.perPlayerMobSpawns) {
            sender.sendMessage(Component.text("Use '/paper mobcaps' for worlds where per-player mob spawning is disabled.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(TextComponent.ofChildren(Component.text("Mobcaps for player: "), Component.text(player.getName(), NamedTextColor.GREEN)));
        sender.sendMessage(this.buildMobcapsComponent(
            category -> level.chunkSource.chunkMap.getMobCountNear(serverPlayer, category),
            category -> NaturalSpawner.limitForCategory(level, category)
        ));
    }

    private Component buildMobcapsComponent(final ToIntFunction<MobCategory> countGetter, final ToIntFunction<MobCategory> limitGetter) {
        return MOB_CATEGORY_COLORS.entrySet().stream()
            .map(entry -> {
                final MobCategory category = entry.getKey();
                final TextColor color = entry.getValue();

                final Component categoryHover = TextComponent.ofChildren(
                    Component.text("Entity types in category ", TextColor.color(0xE0E0E0)),
                    Component.text(category.getName(), color),
                    Component.text(':', NamedTextColor.GRAY),
                    Component.newline(),
                    Component.newline(),
                    Registry.ENTITY_TYPE.entrySet().stream()
                        .filter(it -> it.getValue().getCategory() == category)
                        .map(it -> Component.translatable(it.getValue().getDescriptionId()))
                        .collect(Component.toComponent(Component.text(", ", NamedTextColor.GRAY)))
                );

                final Component categoryComponent = Component.text()
                    .content("  " + category.getName())
                    .color(color)
                    .hoverEvent(categoryHover)
                    .build();

                final TextComponent.Builder builder = Component.text()
                    .append(
                        categoryComponent,
                        Component.text(": ", NamedTextColor.GRAY)
                    );
                final int limit = limitGetter.applyAsInt(category);
                if (limit != -1) {
                    builder.append(
                        Component.text(countGetter.applyAsInt(category)),
                        Component.text("/", NamedTextColor.GRAY),
                        Component.text(limit)
                    );
                } else {
                    builder.append(Component.text()
                        .append(
                            Component.text('n'),
                            Component.text("/", NamedTextColor.GRAY),
                            Component.text('a')
                        )
                        .hoverEvent(Component.text("This category does not naturally spawn.")));
                }
                return builder;
            })
            .map(ComponentLike::asComponent)
            .collect(Component.toComponent(Component.newline()));
    }

    private void doChunkInfo(CommandSender sender, String[] args) {
        List<org.bukkit.World> worlds;
        if (args.length < 2 || args[1].equals("*")) {
            worlds = Bukkit.getWorlds();
        } else {
            worlds = new ArrayList<>(args.length - 1);
            for (int i = 1; i < args.length; ++i) {
                org.bukkit.World world = Bukkit.getWorld(args[i]);
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "World '" + args[i] + "' is invalid");
                    return;
                }
                worlds.add(world);
            }
        }

        int accumulatedTotal = 0;
        int accumulatedInactive = 0;
        int accumulatedBorder = 0;
        int accumulatedTicking = 0;
        int accumulatedEntityTicking = 0;

        for (org.bukkit.World bukkitWorld : worlds) {
            ServerLevel world = ((CraftWorld)bukkitWorld).getHandle();

            int total = 0;
            int inactive = 0;
            int border = 0;
            int ticking = 0;
            int entityTicking = 0;

            for (ChunkHolder chunk : world.getChunkSource().chunkMap.updatingChunks.getUpdatingMap().values()) { // Paper - change updating chunks map
                if (chunk.getFullChunkUnchecked() == null) {
                    continue;
                }

                ++total;

                ChunkHolder.FullChunkStatus state = ChunkHolder.getFullChunkStatus(chunk.getTicketLevel());

                switch (state) {
                    case INACCESSIBLE:
                        ++inactive;
                        continue;
                    case BORDER:
                        ++border;
                        continue;
                    case TICKING:
                        ++ticking;
                        continue;
                    case ENTITY_TICKING:
                        ++entityTicking;
                        continue;
                }
            }

            accumulatedTotal += total;
            accumulatedInactive += inactive;
            accumulatedBorder += border;
            accumulatedTicking += ticking;
            accumulatedEntityTicking += entityTicking;

            sender.sendMessage(ChatColor.BLUE + "Chunks in " + ChatColor.GREEN + bukkitWorld.getName() + ChatColor.DARK_AQUA + ":");
            sender.sendMessage(ChatColor.BLUE + "Total: " + ChatColor.DARK_AQUA + total + ChatColor.BLUE + " Inactive: " + ChatColor.DARK_AQUA
                               + inactive + ChatColor.BLUE + " Border: " + ChatColor.DARK_AQUA + border + ChatColor.BLUE + " Ticking: "
                               + ChatColor.DARK_AQUA + ticking + ChatColor.BLUE + " Entity: " + ChatColor.DARK_AQUA + entityTicking);
        }
        if (worlds.size() > 1) {
            sender.sendMessage(ChatColor.BLUE + "Chunks in " + ChatColor.GREEN + "all listed worlds" + ChatColor.DARK_AQUA + ":");
            sender.sendMessage(ChatColor.BLUE + "Total: " + ChatColor.DARK_AQUA + accumulatedTotal + ChatColor.BLUE + " Inactive: " + ChatColor.DARK_AQUA
                               + accumulatedInactive + ChatColor.BLUE + " Border: " + ChatColor.DARK_AQUA + accumulatedBorder + ChatColor.BLUE + " Ticking: "
                               + ChatColor.DARK_AQUA + accumulatedTicking + ChatColor.BLUE + " Entity: " + ChatColor.DARK_AQUA + accumulatedEntityTicking);
        }
    }

    private void doDebug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Use /paper debug [chunks] help for more information on a specific command");
            return;
        }

        String debugType = args[1].toLowerCase(Locale.ENGLISH);
        switch (debugType) {
            case "chunks":
                if (args.length >= 3 && args[2].toLowerCase(Locale.ENGLISH).equals("help")) {
                    sender.sendMessage(ChatColor.RED + "Use /paper debug chunks to dump loaded chunk information to a file");
                    break;
                }
                File file = new File(new File(new File("."), "debug"),
                    "chunks-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now()) + ".txt");
                sender.sendMessage(ChatColor.GREEN + "Writing chunk information dump to " + file.toString());
                try {
                    MCUtil.dumpChunks(file);
                    sender.sendMessage(ChatColor.GREEN + "Successfully written chunk information!");
                } catch (Throwable thr) {
                    MinecraftServer.LOGGER.warn("Failed to dump chunk information to file " + file.toString(), thr);
                    sender.sendMessage(ChatColor.RED + "Failed to dump chunk information, see console");
                }

                break;
            case "help":
                // fall through to default
            default:
                sender.sendMessage(ChatColor.RED + "Use /paper debug [chunks] help for more information on a specific command");
                return;
        }
    }

    /*
     * Ported from MinecraftForge - author: LexManos <LexManos@gmail.com> - License: LGPLv2.1
     */
    private void listEntities(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1].toLowerCase(Locale.ENGLISH).equals("help")) {
            sender.sendMessage(ChatColor.RED + "Use /paper entity [list] help for more information on a specific command.");
            return;
        }

        switch (args[1].toLowerCase(Locale.ENGLISH)) {
            case "list":
                String filter = "*";
                if (args.length > 2) {
                    if (args[2].toLowerCase(Locale.ENGLISH).equals("help")) {
                        sender.sendMessage(ChatColor.RED + "Use /paper entity list [filter] [worldName] to get entity info that matches the optional filter.");
                        return;
                    }
                    filter = args[2];
                }
                final String cleanfilter = filter.replace("?", ".?").replace("*", ".*?");
                Set<ResourceLocation> names = EntityType.getEntityNameList().stream()
                        .filter(n -> n.toString().matches(cleanfilter))
                        .collect(Collectors.toSet());

                if (names.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Invalid filter, does not match any entities. Use /paper entity list for a proper list");
                    sender.sendMessage(ChatColor.RED + "Usage: /paper entity list [filter] [worldName]");
                    return;
                }

                String worldName;
                if (args.length > 3) {
                    worldName = args[3];
                } else if (sender instanceof Player) {
                    worldName = ((Player) sender).getWorld().getName();
                } else {
                    sender.sendMessage(ChatColor.RED + "Please specify the name of a world");
                    sender.sendMessage(ChatColor.RED + "To do so without a filter, specify '*' as the filter");
                    sender.sendMessage(ChatColor.RED + "Usage: /paper entity list [filter] [worldName]");
                    return;
                }

                Map<ResourceLocation, MutablePair<Integer, Map<ChunkPos, Integer>>> list = Maps.newHashMap();
                World bukkitWorld = Bukkit.getWorld(worldName);
                if (bukkitWorld == null) {
                    sender.sendMessage(ChatColor.RED + "Could not load world for " + worldName + ". Please select a valid world.");
                    sender.sendMessage(ChatColor.RED + "Usage: /paper entity list [filter] [worldName]");
                    return;
                }
                ServerLevel world = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();

                Map<ResourceLocation, Integer> nonEntityTicking = Maps.newHashMap();
                ServerChunkCache chunkProviderServer = world.getChunkSource();

                world.getAllEntities().forEach(e -> {
                    ResourceLocation key = e.getMinecraftKey();

                    MutablePair<Integer, Map<ChunkPos, Integer>> info = list.computeIfAbsent(key, k -> MutablePair.of(0, Maps.newHashMap()));
                    ChunkPos chunk = e.chunkPosition();
                    info.left++;
                    info.right.put(chunk, info.right.getOrDefault(chunk, 0) + 1);
                    if (!chunkProviderServer.isPositionTicking(e)) {
                        nonEntityTicking.merge(key, Integer.valueOf(1), Integer::sum);
                    }
                });

                if (names.size() == 1) {
                    ResourceLocation name = names.iterator().next();
                    Pair<Integer, Map<ChunkPos, Integer>> info = list.get(name);
                    int nonTicking = nonEntityTicking.getOrDefault(name, Integer.valueOf(0)).intValue();
                    if (info == null) {
                        sender.sendMessage(ChatColor.RED + "No entities found.");
                        return;
                    }
                    sender.sendMessage("Entity: " + name + " Total Ticking: " + (info.getLeft() - nonTicking) + ", Total Non-Ticking: " + nonTicking);
                    info.getRight().entrySet().stream()
                            .sorted((a, b) -> !a.getValue().equals(b.getValue()) ? b.getValue() - a.getValue() : a.getKey().toString().compareTo(b.getKey().toString()))
                            .limit(10).forEach(e -> sender.sendMessage("  " + e.getValue() + ": " + e.getKey().x + ", " + e.getKey().z + (chunkProviderServer.isPositionTicking(e.getKey().toLong()) ? " (Ticking)" : " (Non-Ticking)")));
                } else {
                    List<Pair<ResourceLocation, Integer>> info = list.entrySet().stream()
                            .filter(e -> names.contains(e.getKey()))
                            .map(e -> Pair.of(e.getKey(), e.getValue().left))
                            .sorted((a, b) -> !a.getRight().equals(b.getRight()) ? b.getRight() - a.getRight() : a.getKey().toString().compareTo(b.getKey().toString()))
                            .collect(Collectors.toList());

                    if (info == null || info.size() == 0) {
                        sender.sendMessage(ChatColor.RED + "No entities found.");
                        return;
                    }

                    int count = info.stream().mapToInt(Pair::getRight).sum();
                    int nonTickingCount = nonEntityTicking.values().stream().mapToInt(Integer::intValue).sum();
                    sender.sendMessage("Total Ticking: " + (count - nonTickingCount) + ", Total Non-Ticking: " + nonTickingCount);
                    info.forEach(e -> {
                        int nonTicking = nonEntityTicking.getOrDefault(e.getKey(), Integer.valueOf(0)).intValue();
                        sender.sendMessage("  " + (e.getValue() - nonTicking) + " (" + nonTicking + ") " + ": " + e.getKey());
                    });
                    sender.sendMessage("* First number is ticking entities, second number is non-ticking entities");
                }
                break;
        }
    }

    private void dumpHeap(CommandSender sender) {
        java.nio.file.Path dir = java.nio.file.Paths.get("./dumps");
        String name = "heap-dump-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now());

        Command.broadcastCommandMessage(sender, ChatColor.YELLOW + "Writing JVM heap data...");

        java.nio.file.Path file = CraftServer.dumpHeap(dir, name);
        if (file != null) {
            Command.broadcastCommandMessage(sender, ChatColor.GREEN + "Heap dump saved to " + file);
        } else {
            Command.broadcastCommandMessage(sender, ChatColor.RED + "Failed to write heap dump, see sever log for details");
        }
    }

    private void doReload(CommandSender sender) {
        Command.broadcastCommandMessage(sender, ChatColor.RED + "Please note that this command is not supported and may cause issues.");
        Command.broadcastCommandMessage(sender, ChatColor.RED + "If you encounter any issues please use the /stop command to restart your server.");

        MinecraftServer console = MinecraftServer.getServer();
        com.destroystokyo.paper.PaperConfig.init((File) console.options.valueOf("paper-settings"));
        for (ServerLevel world : console.getAllLevels()) {
            world.paperConfig.init();
        }
        console.server.reloadCount++;

        Command.broadcastCommandMessage(sender, ChatColor.GREEN + "Paper config reload complete.");
    }
    private void doDumpItem(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command");
            return;
        }
        ItemStack itemInHand = ((CraftPlayer) sender).getItemInHand();
        net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(itemInHand);
        net.minecraft.nbt.CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            net.kyori.adventure.text.Component nbtComponent = io.papermc.paper.adventure.PaperAdventure.asAdventure(net.minecraft.nbt.NbtUtils.toPrettyComponent(tag));
            Bukkit.getConsoleSender().sendMessage(nbtComponent);
            sender.sendMessage(nbtComponent);
        } else {
            sender.sendMessage("Item does not have NBT");
        }
    }

    // Paper start - rewrite light engine
    private void starlightFixLight(ServerPlayer sender, ServerLevel world, ThreadedLevelLightEngine lightengine, int radius) {
        long start = System.nanoTime();
        java.util.LinkedHashSet<ChunkPos> chunks = new java.util.LinkedHashSet<>(MCUtil.getSpiralOutChunks(sender.blockPosition(), radius)); // getChunkCoordinates is actually just bad mappings, this function rets position as blockpos

        int[] pending = new int[1];
        for (java.util.Iterator<ChunkPos> iterator = chunks.iterator(); iterator.hasNext();) {
            final ChunkPos chunkPos = iterator.next();

            final net.minecraft.world.level.chunk.ChunkAccess chunk = world.getChunkSource().getChunkAtImmediately(chunkPos.x, chunkPos.z);
            if (chunk == null || !chunk.isLightCorrect() || !chunk.getStatus().isOrAfter(net.minecraft.world.level.chunk.ChunkStatus.LIGHT)) {
                // cannot relight this chunk
                iterator.remove();
                continue;
            }

            ++pending[0];
        }

        int[] relitChunks = new int[1];
        lightengine.relight(chunks,
                (ChunkPos chunkPos) -> {
                    ++relitChunks[0];
                    sender.getBukkitEntity().sendMessage(
                            ChatColor.BLUE + "Relit chunk " + ChatColor.DARK_AQUA + chunkPos + ChatColor.BLUE +
                                    ", progress: " + ChatColor.DARK_AQUA + (int)(Math.round(100.0 * (double)(relitChunks[0])/(double)pending[0])) + "%"
                    );
                },
                (int totalRelit) -> {
                    final long end = System.nanoTime();
                    final long diff = Math.round(1.0e-6*(end - start));
                    sender.getBukkitEntity().sendMessage(
                            ChatColor.BLUE + "Relit " + ChatColor.DARK_AQUA + totalRelit + ChatColor.BLUE + " chunks. Took " +
                                    ChatColor.DARK_AQUA + diff + "ms"
                    );
                });
        sender.getBukkitEntity().sendMessage(ChatColor.BLUE + "Relighting " + ChatColor.DARK_AQUA + pending[0] + ChatColor.BLUE + " chunks");
    }
    // Paper end - rewrite light engine

    private void doFixLight(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command");
            return;
        }
        int radius = 2;
        if (args.length > 1) {
            try {
                radius = Math.min(32, Integer.parseInt(args[1])); // Paper - MOOOOOORE
            } catch (Exception e) {
                sender.sendMessage("Not a number");
                return;
            }

        }

        CraftPlayer player = (CraftPlayer) sender;
        ServerPlayer handle = player.getHandle();
        ServerLevel world = (ServerLevel) handle.level;
        ThreadedLevelLightEngine lightengine = world.getChunkSource().getLightEngine();

        // Paper start - rewrite light engine
        if (true) {
            this.starlightFixLight(handle, world, lightengine, radius);
            return;
        }
        // Paper end - rewrite light engine

        net.minecraft.core.BlockPos center = MCUtil.toBlockPosition(player.getLocation());
        Deque<ChunkPos> queue = new ArrayDeque<>(MCUtil.getSpiralOutChunks(center, radius));
        updateLight(sender, world, lightengine, queue);
    }

    private void updateLight(CommandSender sender, ServerLevel world, ThreadedLevelLightEngine lightengine, Deque<ChunkPos> queue) {
        ChunkPos coord = queue.poll();
        if (coord == null) {
            sender.sendMessage("All Chunks Light updated");
            return;
        }
        world.getChunkSource().getChunkAtAsynchronously(coord.x, coord.z, false, false).whenCompleteAsync((either, ex) -> {
            if (ex != null) {
                sender.sendMessage("Error loading chunk " + coord);
                updateLight(sender, world, lightengine, queue);
                return;
            }
            net.minecraft.world.level.chunk.LevelChunk chunk = (net.minecraft.world.level.chunk.LevelChunk) either.left().orElse(null);
            if (chunk == null) {
                updateLight(sender, world, lightengine, queue);
                return;
            }
            lightengine.setTaskPerBatch(world.paperConfig.lightQueueSize + 16 * 256); // ensure full chunk can fit into queue
            sender.sendMessage("Updating Light " + coord);
            int cx = chunk.getPos().x << 4;
            int cz = chunk.getPos().z << 4;
            for (int y = 0; y < world.getHeight(); y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(cx + x, y, cz + z);
                        lightengine.checkBlock(pos);
                    }
                }
            }
            lightengine.tryScheduleUpdate();
            ChunkHolder visibleChunk = world.getChunkSource().chunkMap.getVisibleChunkIfPresent(chunk.coordinateKey);
            if (visibleChunk != null) {
                world.getChunkSource().chunkMap.addLightTask(visibleChunk, () -> {
                    MinecraftServer.getServer().processQueue.add(() -> {
                        visibleChunk.broadcast(new net.minecraft.network.protocol.game.ClientboundLightUpdatePacket(chunk.getPos(), lightengine, null, null, true), false);
                        updateLight(sender, world, lightengine, queue);
                    });
                });
            } else {
                updateLight(sender, world, lightengine, queue);
            }
            lightengine.setTaskPerBatch(world.paperConfig.lightQueueSize);
        }, MinecraftServer.getServer());
    }
}
