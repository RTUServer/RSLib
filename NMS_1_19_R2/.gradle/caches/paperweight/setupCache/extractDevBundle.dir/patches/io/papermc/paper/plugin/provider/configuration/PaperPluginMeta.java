package io.papermc.paper.plugin.provider.configuration;

import com.google.common.collect.ImmutableList;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.configuration.constraint.Constraint;
import io.papermc.paper.configuration.serializer.ComponentSerializer;
import io.papermc.paper.configuration.serializer.EnumValueSerializer;
import io.papermc.paper.configuration.serializer.collections.MapSerializer;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.configuration.serializer.ImmutableListSerializer;
import io.papermc.paper.plugin.provider.configuration.serializer.PermissionConfigurationSerializer;
import io.papermc.paper.plugin.provider.configuration.serializer.constraints.PluginConfigConstraints;
import io.papermc.paper.plugin.provider.configuration.type.DependencyConfiguration;
import io.papermc.paper.plugin.provider.configuration.type.LoadConfiguration;
import io.papermc.paper.plugin.provider.configuration.type.PermissionConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.util.List;

@SuppressWarnings({"CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "NotNullFieldNotInitialized", "InnerClassMayBeStatic"})
@ConfigSerializable
public class PaperPluginMeta implements PluginMeta {

    @PluginConfigConstraints.PluginName
    @Required
    private String name;
    @Required
    @PluginConfigConstraints.PluginNameSpace
    private String main;
    @PluginConfigConstraints.PluginNameSpace
    private String bootstrapper;
    @PluginConfigConstraints.PluginNameSpace
    private String loader;
    private List<DependencyConfiguration> dependencies = List.of();
    private List<LoadConfiguration> loadBefore = List.of();
    private List<LoadConfiguration> loadAfter = List.of();
    private List<String> provides = List.of();
    private boolean hasOpenClassloader = false;
    @Required
    private String version;
    private String description;
    private List<String> authors = List.of();
    private List<String> contributors = List.of();
    private String website;
    private String prefix;
    private PluginLoadOrder load = PluginLoadOrder.POSTWORLD;
    @FlattenedResolver
    private PermissionConfiguration permissionConfiguration = new PermissionConfiguration(PermissionDefault.OP, List.of());
    @Required
    @PluginConfigConstraints.PluginVersion
    private String apiVersion;

    private transient String displayName;

    public PaperPluginMeta() {
    }

    public static PaperPluginMeta create(BufferedReader reader) throws ConfigurateException {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .indent(2)
            .nodeStyle(NodeStyle.BLOCK)
            .headerMode(HeaderMode.NONE)
            .source(() -> reader)
            .defaultOptions((options) -> {

                return options.serializers((serializers) -> {
                    serializers
                        .register(new EnumValueSerializer())
                        .register(MapSerializer.TYPE, new MapSerializer(false))
                        .register(new TypeToken<>() {
                        }, new ImmutableListSerializer())
                        .register(PermissionConfiguration.class, PermissionConfigurationSerializer.SERIALIZER)
                        .register(new ComponentSerializer())
                        .registerAnnotatedObjects(
                            ObjectMapper.factoryBuilder()
                                .addConstraint(Constraint.class, new Constraint.Factory())
                                .addConstraint(PluginConfigConstraints.PluginName.class, String.class, new PluginConfigConstraints.PluginName.Factory())
                                .addConstraint(PluginConfigConstraints.PluginVersion.class, String.class, new PluginConfigConstraints.PluginVersion.Factory())
                                .addConstraint(PluginConfigConstraints.PluginNameSpace.class, String.class, new PluginConfigConstraints.PluginNameSpace.Factory())
                                .addNodeResolver(new FlattenedResolver.Factory())
                                .build()
                        );

                });
            })
            .build();
        CommentedConfigurationNode node = loader.load();
        PaperPluginMeta pluginConfiguration = node.require(PaperPluginMeta.class);

        if (!node.node("author").virtual()) {
            pluginConfiguration.authors = ImmutableList.<String>builder()
                .addAll(pluginConfiguration.authors)
                .add(node.node("author").getString())
                .build();
        }

        pluginConfiguration.displayName = pluginConfiguration.name.replace('_', ' ');

        return pluginConfiguration;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public @NotNull String getMainClass() {
        return this.main;
    }

    @Override
    public @NotNull String getVersion() {
        return this.version;
    }

    @Override
    public @NotNull String getDisplayName() {
        return this.displayName;
    }

    @Override
    public @Nullable String getLoggerPrefix() {
        return this.prefix;
    }

    @Override
    public @NotNull List<String> getPluginDependencies() {
        return this.dependencies.stream().filter((dependency) -> dependency.required() && !dependency.bootstrap()).map(DependencyConfiguration::name).toList();
    }

    @Override
    public @NotNull List<String> getPluginSoftDependencies() {
        return this.dependencies.stream().filter((dependency) -> !dependency.required() && !dependency.bootstrap()).map(DependencyConfiguration::name).toList();
    }

    @Override
    public @NotNull List<String> getLoadBeforePlugins() {
        return this.loadBefore.stream().filter((dependency) -> !dependency.bootstrap()).map(LoadConfiguration::name).toList();
    }

    public @NotNull List<String> getLoadAfterPlugins() {
        return this.loadAfter.stream().filter((dependency) -> !dependency.bootstrap()).map(LoadConfiguration::name).toList();
    }

    public List<LoadConfiguration> getLoadAfter() {
        return this.loadAfter;
    }

    public List<LoadConfiguration> getLoadBefore() {
        return this.loadBefore;
    }

    @Override
    public @NotNull PluginLoadOrder getLoadOrder() {
        return this.load;
    }

    @Override
    public @NotNull String getDescription() {
        return this.description;
    }

    @Override
    public @NotNull List<String> getAuthors() {
        return this.authors;
    }

    @Override
    public @NotNull List<String> getContributors() {
        return this.contributors;
    }

    @Override
    public String getWebsite() {
        return this.website;
    }

    @Override
    public @NotNull List<Permission> getPermissions() {
        return this.permissionConfiguration.permissions();
    }

    @Override
    public @NotNull PermissionDefault getPermissionDefault() {
        return this.permissionConfiguration.defaultPerm();
    }

    @Override
    public @NotNull String getAPIVersion() {
        return this.apiVersion;
    }

    @Override
    public @NotNull List<String> getProvidedPlugins() {
        return this.provides;
    }

    public String getBootstrapper() {
        return this.bootstrapper;
    }

    public String getLoader() {
        return this.loader;
    }

    public boolean hasOpenClassloader() {
        return this.hasOpenClassloader;
    }

    public List<DependencyConfiguration> getDependencies() {
        return dependencies;
    }
}
