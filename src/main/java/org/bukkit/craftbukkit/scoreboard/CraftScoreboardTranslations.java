package org.bukkit.craftbukkit.scoreboard;

import com.google.common.collect.ImmutableBiMap;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;

final class CraftScoreboardTranslations {
    static final int MAX_DISPLAY_SLOT = 19;
    @Deprecated // Paper
    static final ImmutableBiMap<DisplaySlot, String> SLOTS = ImmutableBiMap.<DisplaySlot, String>builder()
            .put(DisplaySlot.BELOW_NAME, "below_name")
            .put(DisplaySlot.PLAYER_LIST, "list")
            .put(DisplaySlot.SIDEBAR, "sidebar")
            .buildOrThrow();

    private CraftScoreboardTranslations() {}

    static DisplaySlot toBukkitSlot(net.minecraft.world.scores.DisplaySlot minecraft) {
        if (true) return DisplaySlot.NAMES.value(minecraft.getSerializedName()); // Paper
        return SLOTS.inverse().get(minecraft.getSerializedName());
    }

    static net.minecraft.world.scores.DisplaySlot fromBukkitSlot(DisplaySlot slot) {
        if (true) return net.minecraft.world.scores.DisplaySlot.CODEC.byName(slot.getId()); // Paper
        return net.minecraft.world.scores.DisplaySlot.CODEC.byName(SLOTS.get(slot));
    }

    static RenderType toBukkitRender(ObjectiveCriteria.RenderType display) {
        return RenderType.valueOf(display.name());
    }

    static ObjectiveCriteria.RenderType fromBukkitRender(RenderType render) {
        return ObjectiveCriteria.RenderType.valueOf(render.name());
    }
}
