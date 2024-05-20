package org.bukkit.craftbukkit.v1_18_R1.scoreboard;

import com.google.common.collect.ImmutableBiMap;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;

public final class CraftScoreboardTranslations {
    static final int MAX_DISPLAY_SLOT = Scoreboard.getDisplaySlotNames().length; // Paper
    @Deprecated // Paper
    static ImmutableBiMap<DisplaySlot, String> SLOTS = ImmutableBiMap.of(
            DisplaySlot.BELOW_NAME, "belowName",
            DisplaySlot.PLAYER_LIST, "list",
            DisplaySlot.SIDEBAR, "sidebar");

    private CraftScoreboardTranslations() {}

    public static DisplaySlot toBukkitSlot(int i) {
        if (true) return org.bukkit.scoreboard.DisplaySlot.NAMES.value(Scoreboard.getDisplaySlotName(i)); // Paper
        return CraftScoreboardTranslations.SLOTS.inverse().get(Scoreboard.getDisplaySlotName(i));
    }

    public static int fromBukkitSlot(DisplaySlot slot) {
        if (true) return Scoreboard.getDisplaySlotByName(slot.getId()); // Paper
        return Scoreboard.getDisplaySlotByName(CraftScoreboardTranslations.SLOTS.get(slot));
    }

    static RenderType toBukkitRender(ObjectiveCriteria.RenderType display) {
        return RenderType.valueOf(display.name());
    }

    static ObjectiveCriteria.RenderType fromBukkitRender(RenderType render) {
        return ObjectiveCriteria.RenderType.valueOf(render.name());
    }
}
