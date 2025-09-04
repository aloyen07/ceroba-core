package ru.freemasters.ceroba.core.event.bus;

public interface CancellableEvent<T> extends Event<T> {

    void setCancelled(boolean cancelled);

    boolean isCancelled();
}
