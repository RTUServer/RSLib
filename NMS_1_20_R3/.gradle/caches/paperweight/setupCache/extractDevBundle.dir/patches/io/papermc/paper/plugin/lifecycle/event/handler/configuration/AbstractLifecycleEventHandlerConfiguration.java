package io.papermc.paper.plugin.lifecycle.event.handler.configuration;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.types.AbstractLifecycleEventType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class AbstractLifecycleEventHandlerConfiguration<O extends LifecycleEventOwner, E extends LifecycleEvent, CI extends AbstractLifecycleEventHandlerConfiguration<O, E, CI>> implements LifecycleEventHandlerConfiguration<O> {

    private final LifecycleEventHandler<? super E> handler;
    private final AbstractLifecycleEventType<O, E, ?, CI> type;

    protected AbstractLifecycleEventHandlerConfiguration(final LifecycleEventHandler<? super E> handler, final AbstractLifecycleEventType<O, E, ?, CI> type) {
        this.handler = handler;
        this.type = type;
    }

    public abstract CI config();

    public final void registerFrom(final O owner) {
        this.type.tryRegister(owner, this.handler, this.config());
    }
}
