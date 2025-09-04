package ru.freemasters.ceroba.core.event.bus;

public abstract class AbstractCancellableEvent<T> implements CancellableEvent<T> {

    protected T eventData;
    protected boolean wasCancelled = false;

    public AbstractCancellableEvent(T eventObj) {
        this.eventData = eventObj;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.wasCancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.wasCancelled;
    }
}
