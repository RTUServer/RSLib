package io.papermc.paper.plugin.entrypoint.dependency;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.entrypoint.DependencyContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class MetaDependencyTree implements DependencyContext {

    private final MutableGraph<String> graph;

    // We need to upkeep a separate collection since when populating
    // a graph it adds nodes even if they are not present
    private final Set<String> dependencies = new HashSet<>();

    public MetaDependencyTree() {
        this(GraphBuilder.directed().build());
    }

    public MetaDependencyTree(MutableGraph<String> graph) {
        this.graph = graph;
    }

    public void add(PluginProvider<?> provider) {
        add(provider.getMeta());
    }

    public void remove(PluginProvider<?> provider) {
        remove(provider.getMeta());
    }

    public void add(PluginMeta configuration) {
        String identifier = configuration.getName();
        // Build a validated provider's dependencies into the graph
        for (String dependency : configuration.getPluginDependencies()) {
            this.graph.putEdge(identifier, dependency);
        }
        for (String dependency : configuration.getPluginSoftDependencies()) {
            this.graph.putEdge(identifier, dependency);
        }

        this.graph.addNode(identifier); // Make sure dependencies at least have a node

        // Add the provided plugins to the graph as well
        for (String provides : configuration.getProvidedPlugins()) {
            this.graph.putEdge(identifier, provides);
            this.dependencies.add(provides);
        }
        this.dependencies.add(identifier);
    }

    public void remove(PluginMeta configuration) {
        String identifier = configuration.getName();
        // Remove a validated provider's dependencies into the graph
        for (String dependency : configuration.getPluginDependencies()) {
            this.graph.removeEdge(identifier, dependency);
        }
        for (String dependency : configuration.getPluginSoftDependencies()) {
            this.graph.removeEdge(identifier, dependency);
        }

        this.graph.removeNode(identifier); // Remove root node

        // Remove the provided plugins to the graph as well
        for (String provides : configuration.getProvidedPlugins()) {
            this.graph.removeEdge(identifier, provides);
            this.dependencies.remove(provides);
        }
        this.dependencies.remove(identifier);
    }

    @Override
    public boolean isTransitiveDependency(@NotNull PluginMeta plugin, @NotNull PluginMeta depend) {
        String pluginIdentifier = plugin.getName();

        if (this.graph.nodes().contains(pluginIdentifier)) {
            Set<String> reachableNodes = Graphs.reachableNodes(this.graph, pluginIdentifier);
            if (reachableNodes.contains(depend.getName())) {
                return true;
            }
            for (String provided : depend.getProvidedPlugins()) {
                if (reachableNodes.contains(provided)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean hasDependency(@NotNull String pluginIdentifier) {
        return this.dependencies.contains(pluginIdentifier);
    }

    // Used by the legacy loader -- this isn't recommended
    public void addDirectDependency(String dependency) {
        this.dependencies.add(dependency);
    }

    @Override
    public String toString() {
        return "ProviderDependencyTree{" +
            "graph=" + this.graph +
            '}';
    }

    public MutableGraph<String> getGraph() {
        return graph;
    }
}
