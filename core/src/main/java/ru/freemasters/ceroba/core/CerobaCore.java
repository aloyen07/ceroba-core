package ru.freemasters.ceroba.core;

import ru.freemasters.ceroba.core.event.bus.EventBus;
import ru.freemasters.ceroba.core.event.impl.ConfigurationLoadingEvent;
import ru.freemasters.ceroba.core.event.impl.CoreAboutToInitEvent;
import ru.freemasters.ceroba.core.log.CerobaLogger;

import java.io.File;

public class CerobaCore {

    @SuppressWarnings("deprecation")
    public static final EventBus EVENT_BUS = new EventBus();

    private static CerobaLogger logger;

    public static CerobaLogger getLogger() {
        return logger;
    }

    public static void initialize(CerobaLogger logger, File configFile) {
        CerobaCore.logger = logger;

        logger.info("Ceroba Core initializing...");
        CoreAboutToInitEvent event = new CoreAboutToInitEvent();
        EVENT_BUS.post(event);

        if (event.isCancelled()) {
            logger.error("Ceroba about to be init cancelled" + "e");
            return;
        }

        // Initializing configuration
        logger.info("Loading configuration...");
        EVENT_BUS.post(new ConfigurationLoadingEvent());


    }

    // Prevent instantiation
    private CerobaCore() {}
}
