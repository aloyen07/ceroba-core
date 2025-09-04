package ru.freemasters.ceroba.core.event.bus;

import org.jetbrains.annotations.Nullable;
import ru.freemasters.ceroba.core.CerobaCore;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Event bus for registering and posting events.
 * </br>
 * You can create your own instance of EventBus, but it's recommended to use the singleton instance
 * {@link ru.freemasters.ceroba.core.CerobaCore#EVENT_BUS}.
 */
public class EventBus {

    protected final HashMap<Class<? extends Event<?>>, List<EventFunctionHandler>> listeners = new HashMap<>();

    /**
     * @deprecated Use instance in field {@link ru.freemasters.ceroba.core.CerobaCore#EVENT_BUS} instead.
     */
    @Deprecated(since = "1.0")
    public EventBus() {
        CerobaCore.getLogger().info("Ceroba bus created");
    }

    /**
     * Registers all methods of the given class annotated with {@link ru.freemasters.ceroba.core.event.bus.SubscribeEvent}
     * as event listeners (static methods only).
     * @param clazz class to register
     */
    public void registerStatic(Class<?> clazz) {
        addEvents(clazz, null);
        CerobaCore.getLogger().debug("Registered static listeners for class {}", clazz.getName());
    }

    /**
     * Registers all methods of the given object annotated with {@link ru.freemasters.ceroba.core.event.bus.SubscribeEvent}
     * as event listeners (instance methods only).
     * @param listener object to register
     * @throws IllegalAccessError if the listener does not have suitable methods
     * (not 1 parameter or parameter is not subclass of Event)
     */
    public void register(Object listener) {
        addEvents(listener.getClass(), listener);
        CerobaCore.getLogger().debug("Registered instance listeners for object {}", listener.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    protected void addEvents(Class<?> listenerClass, @Nullable Object instance) {

        for (Method method : listenerClass.getDeclaredMethods()) {

            if (!method.isAnnotationPresent(SubscribeEvent.class)) {
                continue; // Skip methods not annotated with @SubscribeEvent
            }

            boolean isStatic = Modifier.isStatic(method.getModifiers());
            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            method.setAccessible(true);
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) {
                throw new IllegalArgumentException("Method " + method + " must have exactly one parameter");
            }

            if (!Event.class.isAssignableFrom(params[0])) {
                throw new IllegalArgumentException("Method " + method + " parameter must be a subclass of Event");
            }

            Class<? extends Event<?>> eventClass = (Class<? extends Event<?>>) params[0];

            if (isStatic) {
                addListener(eventClass, new EventFunctionHandler(
                        annotation.priority(),
                        annotation.receiveCanceled(),
                        method,
                        null
                ));
            } else if (instance != null) {
                addListener(eventClass, new EventFunctionHandler(
                        annotation.priority(),
                        annotation.receiveCanceled(),
                        method,
                        instance
                ));
            }
        }
    }

    protected final void addListener(Class<? extends Event<?>> listenerClass, EventFunctionHandler handler) {
        List<EventFunctionHandler> list = listeners.computeIfAbsent(listenerClass,
                k -> new ArrayList<>());

        list.add(handler);
        sortByPriority(list);
    }

    protected final void sortByPriority(List<EventFunctionHandler> list) {
        list.sort((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()));
    }

    @SuppressWarnings("unchecked")
    public void post(Event<?> event) {

        Class<? extends Event<?>> eventClass = (Class<? extends Event<?>>) event.getClass();
        List<EventFunctionHandler> handlers = listeners.get(eventClass);
        if (handlers == null) {
            return; // No listeners for this event
        }

        for (EventFunctionHandler handler : handlers) {
            if (event instanceof CancellableEvent<?> cancellableEvent
                    && cancellableEvent.isCancelled() && !handler.isReceiveCanceled()) {
                continue; // Skip if event is canceled and listener does not want canceled events
            }

            try {
                handler.getMethod().invoke(handler.getInstance(), event);
            } catch (Exception e) {
                throw new RuntimeException("An error has been occurred while handling event " + event, e);
            }
        }
    }

    /**
     * Register a single event listener method.
     */
    // Do not convert it to record for Android capability!
    protected static class EventFunctionHandler {

        private final int priority;
        private final boolean receiveCanceled;
        private final Method method;
        private final Object instance;

        public EventFunctionHandler(int priority, boolean receiveCanceled, Method method, @Nullable Object instance) {
            this.priority = priority;
            this.receiveCanceled = receiveCanceled;
            this.method = method;
            this.instance = instance;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isReceiveCanceled() {
            return receiveCanceled;
        }

        public Method getMethod() {
            return method;
        }

        public Object getInstance() {
            return instance;
        }
    }
}
