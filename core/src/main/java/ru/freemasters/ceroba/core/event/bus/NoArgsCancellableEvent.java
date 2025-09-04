package ru.freemasters.ceroba.core.event.bus;

public class NoArgsCancellableEvent extends AbstractCancellableEvent<Void> {

    public NoArgsCancellableEvent() {
        super(null);
    }
}
