package org.spigotmc;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import co.aikar.timings.TimingsManager;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.AuthorNagException;
import org.jetbrains.annotations.NotNull;

/**
 * This is here for legacy purposes incase any plugin used it.
 *
 * If you use this, migrate ASAP as this will be removed in the future!
 *
 * @deprecated
 * @see co.aikar.timings.Timings#of
 */
@Deprecated(forRemoval = true)
public final class CustomTimingsHandler {
    private final Timing handler;

    public CustomTimingsHandler(@NotNull String name) {
        Timing timing;

        new AuthorNagException("Deprecated use of CustomTimingsHandler. Please Switch to Timings.of ASAP").printStackTrace();
        try {
            final Method ofSafe = TimingsManager.class.getDeclaredMethod("getHandler", String.class, String.class, Timing.class);
            ofSafe.setAccessible(true);
            timing = (Timing) ofSafe.invoke(null, "Minecraft", "(Deprecated API) " + name, null);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "This handler could not be registered");
            timing = Timings.NULL_HANDLER;
        }
        handler = timing;
    }

    public void startTiming() {
        handler.startTiming();
    }

    public void stopTiming() {
        handler.stopTiming();
    }
}
