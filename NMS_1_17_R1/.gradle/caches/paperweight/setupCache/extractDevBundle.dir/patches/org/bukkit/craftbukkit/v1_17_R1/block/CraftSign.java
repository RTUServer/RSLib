package org.bukkit.craftbukkit.v1_17_R1.block;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;

public class CraftSign extends CraftBlockEntityState<SignBlockEntity> implements Sign {

    // Lazily initialized only if requested:
    // Paper start
    private java.util.ArrayList<net.kyori.adventure.text.Component> originalLines = null; // ArrayList for RandomAccess
    private java.util.ArrayList<net.kyori.adventure.text.Component> lines = null; // ArrayList for RandomAccess
    // Paper end

    public CraftSign(World world, SignBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    // Paper start
    @Override
    public java.util.List<net.kyori.adventure.text.Component> lines() {
        this.loadLines();
        return this.lines;
    }

    @Override
    public net.kyori.adventure.text.Component line(int index) {
        this.loadLines();
        return this.lines.get(index);
    }

    @Override
    public void line(int index, net.kyori.adventure.text.Component line) {
        this.loadLines();
        this.lines.set(index, line);
    }

    private void loadLines() {
        if (lines != null) {
            return;
        }
        // Lazy initialization:
        SignBlockEntity sign = this.getSnapshot();
        lines = io.papermc.paper.adventure.PaperAdventure.asAdventure(com.google.common.collect.Lists.newArrayList(sign.messages));
        originalLines = new java.util.ArrayList<>(lines);
    }
    // Paper end
    @Override
    public String[] getLines() {
        this.loadLines();
        return this.lines.stream().map(io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC::serialize).toArray(String[]::new); // Paper
    }

    @Override
    public String getLine(int index) throws IndexOutOfBoundsException {
        this.loadLines();
        return io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.serialize(this.lines.get(index)); // Paper
    }

    @Override
    public void setLine(int index, String line) throws IndexOutOfBoundsException {
        this.loadLines();
        this.lines.set(index, line != null ? io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.deserialize(line) : net.kyori.adventure.text.Component.empty()); // Paper
    }

    @Override
    public boolean isEditable() {
        return getSnapshot().isEditable;
    }

    @Override
    public void setEditable(boolean editable) {
        getSnapshot().isEditable = editable;
    }

    @Override
    public boolean isGlowingText() {
        return getSnapshot().hasGlowingText();
    }

    @Override
    public void setGlowingText(boolean glowing) {
        getSnapshot().setHasGlowingText(glowing);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.getByWoolData((byte) getSnapshot().getColor().getId());
    }

    @Override
    public void setColor(DyeColor color) {
        getSnapshot().setColor(net.minecraft.world.item.DyeColor.byId(color.getWoolData()));
    }

    @Override
    public void applyTo(SignBlockEntity sign) {
        super.applyTo(sign);

        if (this.lines != null) {
            // Paper start
            for (int i = 0; i < this.lines.size(); ++i) {
                net.kyori.adventure.text.Component component = this.lines.get(i);
                net.kyori.adventure.text.Component origComp = this.originalLines.get(i);
                if (component.equals(origComp)) {
                    continue; // The line contents are still the same, skip.
                }
                sign.setMessage(i, io.papermc.paper.adventure.PaperAdventure.asVanilla(component));
            }
            // Paper end
        }
        sign.isEditable = getSnapshot().isEditable; // Paper - copy manually
    }

    // Paper start
    public static Component[] sanitizeLines(java.util.List<net.kyori.adventure.text.Component> lines) {
        Component[] components = new Component[4];
        for (int i = 0; i < 4; i++) {
            if (i < lines.size() && lines.get(i) != null) {
                components[i] = io.papermc.paper.adventure.PaperAdventure.asVanilla(lines.get(i));
            } else {
                components[i] = new TextComponent("");
            }
        }
        return components;
    }
    // Paper end
    public static Component[] sanitizeLines(String[] lines) {
        Component[] components = new Component[4];

        for (int i = 0; i < 4; i++) {
            if (i < lines.length && lines[i] != null) {
                components[i] = CraftChatMessage.fromString(lines[i])[0];
            } else {
                components[i] = new TextComponent("");
            }
        }

        return components;
    }

    public static String[] revertComponents(Component[] components) {
        String[] lines = new String[components.length];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = CraftSign.revertComponent(components[i]);
        }
        return lines;
    }

    private static String revertComponent(Component component) {
        return CraftChatMessage.fromComponent(component);
    }
}
