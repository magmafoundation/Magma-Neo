package org.magmafoundation.magma;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Magma {
    private static final Logger LOGGER = LogManager.getLogger();

    private static String version = "";

    public Magma() {
        version = this.getClass().getPackage().getImplementationVersion();
    }

    public static String getVersion() {
        return version;
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
