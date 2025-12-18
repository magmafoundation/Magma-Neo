package org.magmafoundation.magma;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Magma {
    public static final Logger LOGGER = LogManager.getLogger();

    private static String version = "";
    private static final String BUKKIT_VERSION = "v1_21_R1";

    static {
        version = Magma.class.getPackage().getImplementationVersion();
    }

    public static String getVersion() {
        return version;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static String getBukkitVersion() {
        return BUKKIT_VERSION;
    }
}
