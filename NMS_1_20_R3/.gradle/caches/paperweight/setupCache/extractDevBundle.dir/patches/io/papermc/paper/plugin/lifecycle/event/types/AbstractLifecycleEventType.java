package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventRunner;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.AbstractLifecycleEventHandlerConfiguration;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.LifecycleEventHandlerConfiguration;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class AbstractLifecycleEventType<O extends LifecycleEventOwner, E extends LifecycleEvent, C extends LifecycleEventHandlerConfiguration<O>, CI extends AbstractLifecycleEventHandlerConfiguration<O, E, CI>> implements LifecycleEventType<O, E, C> {

    private final String name;
    private final Class<? extends O> ownerType;

    protected AbstractLifecycleEventType(final String name, final Class<? extends O> ownerType) {
        this.name = name;
        this.ownerType = ownerType;
    }

    @Override
    public String name() {
        return this.name;
    }

    private void verifyOwner(final O owner) {
        if (!this.ownerType.isInstance(owner)) {
            throw new IllegalArgumentException("You cannot register the lifecycle event '" + this.name + "' on " + owner);
        }
    }

    public abstract void forEachHandler(Consumer<? super RegisteredHandler<O, E>> consumer, Predicate<? super RegisteredHandler<O, E>> predicate);

    public abstract void removeMatching(Predicate<? super RegisteredHandler<O, E>> predicate);

    protected abstract void register(O owner, LifecycleEventHandler<? super E> handler, CI config);

    public final void tryRegister(final O owner, final LifecycleEventHandler<? super E> handler, final CI config) {
        this.verifyOwner(owner);
        LifecycleEventRunner.INSTANCE.checkRegisteredHandler(owner, this);
        this.register(owner, handler, config);
    }

    public record RegisteredHandler<O, E extends LifecycleEvent>(O owner, LifecycleEventHandler<? super E> lifecycleEventHandler) {
    }
}
