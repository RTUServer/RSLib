package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.PrioritizedLifecycleEventHandlerConfiguration;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.PrioritizedLifecycleEventHandlerConfigurationImpl;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class PrioritizableLifecycleEventType<O extends LifecycleEventOwner, E extends LifecycleEvent> extends AbstractLifecycleEventType<O, E, PrioritizedLifecycleEventHandlerConfiguration<O>, PrioritizedLifecycleEventHandlerConfigurationImpl<O, E>> implements LifecycleEventType.Prioritizable<O, E> {

    private static final Comparator<PrioritizedHandler<?, ?>> COMPARATOR = Comparator.comparing(PrioritizedHandler::priority, (o1, o2) -> {
        if (o1.equals(o2)) {
            return 0;
        } else if (o1.isEmpty()) {
            return 1;
        } else if (o2.isEmpty()) {
            return -1;
        } else {
            return Integer.compare(o1.getAsInt(), o2.getAsInt());
        }
    });

    private final List<PrioritizedHandler<O, E>> handlers = new ArrayList<>();

    public PrioritizableLifecycleEventType(final String name, final Class<? extends O> ownerType) {
        super(name, ownerType);
    }

    @Override
    public PrioritizedLifecycleEventHandlerConfiguration<O> newHandler(final LifecycleEventHandler<? super E> handler) {
        return new PrioritizedLifecycleEventHandlerConfigurationImpl<>(handler, this);
    }

    @Override
    protected void register(final O owner, final LifecycleEventHandler<? super E> handler, final PrioritizedLifecycleEventHandlerConfigurationImpl<O, E> config) {
        this.handlers.add(new PrioritizedHandler<>(new RegisteredHandler<>(owner, handler), config.priority()));
        this.handlers.sort(COMPARATOR);
    }

    @Override
    public void forEachHandler(final Consumer<? super RegisteredHandler<O, E>> consumer, final Predicate<? super RegisteredHandler<O, E>> predicate) {
        for (final PrioritizedHandler<O, E> handler : this.handlers) {
            if (predicate.test(handler.handler())) {
                consumer.accept(handler.handler());
            }
        }
    }

    @Override
    public void removeMatching(final Predicate<? super RegisteredHandler<O, E>> predicate) {
        this.handlers.removeIf(prioritizedHandler -> predicate.test(prioritizedHandler.handler()));
    }

    private record PrioritizedHandler<O extends LifecycleEventOwner, E extends LifecycleEvent>(RegisteredHandler<O, E> handler, OptionalInt priority) {}
}
