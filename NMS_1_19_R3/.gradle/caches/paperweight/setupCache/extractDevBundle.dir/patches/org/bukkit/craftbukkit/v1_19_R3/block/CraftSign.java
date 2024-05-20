package org.bukkit.craftbukkit.v1_19_R3.block;

import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.craftbukkit.v1_19_R3.block.sign.CraftSignSide;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CraftSign<T extends SignBlockEntity> extends CraftBlockEntityState<T> implements Sign {

    private final CraftSignSide front;

    public CraftSign(World world, T tileEntity) {
        super(world, tileEntity);
        this.front = new CraftSignSide(this.getSnapshot());
    }

    // Paper start
    @Override
    public java.util.@NotNull List<net.kyori.adventure.text.Component> lines() {
        return this.front.lines();
    }

    @Override
    public net.kyori.adventure.text.@NotNull Component line(int index) {
        return this.front.line(index);
    }

    @Override
    public void line(int index, net.kyori.adventure.text.@NotNull Component line) {
        this.front.line(index, line);
    }
    // Paper end

    @Override
    public String[] getLines() {
        return this.front.getLines();
    }

    @Override
    public String getLine(int index) throws IndexOutOfBoundsException {
        return this.front.getLine(index);
    }

    @Override
    public void setLine(int index, String line) throws IndexOutOfBoundsException {
        this.front.setLine(index, line);
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
        return this.front.isGlowingText();
    }

    @Override
    public void setGlowingText(boolean glowing) {
        this.front.setGlowingText(glowing);
    }

    @NotNull
    @Override
    public SignSide getSide(Side side) {
        Preconditions.checkArgument(side != null, "side == null");

        return this.front;
    }

    @Override
    public DyeColor getColor() {
        return this.front.getColor();
    }

    @Override
    public void setColor(DyeColor color) {
        this.front.setColor(color);
    }

    @Override
    public void applyTo(T sign) {
        this.front.applyLegacyStringToSignSide();

        super.applyTo(sign);
        sign.isEditable = getSnapshot().isEditable; // Paper - copy manually
    }

    public static void openSign(Sign sign, org.bukkit.entity.HumanEntity player) { // Paper - change move open sign to HumanEntity
        Preconditions.checkArgument(sign != null, "sign == null");
        // Preconditions.checkArgument(sign.isPlaced(), "Sign must be placed"); // Paper - don't require placed
        Preconditions.checkArgument(sign.getWorld() == player.getWorld(), "Sign must be in same world as Player");

        SignBlockEntity handle = ((CraftSign<?>) sign).getTileEntity();
        handle.isEditable = true;

        ((org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity) player).getHandle().openTextEdit(handle); // Paper - change move open sign to HumanEntity
    }

    // Paper start
    public static Component[] sanitizeLines(java.util.List<? extends net.kyori.adventure.text.Component> lines) {
        Component[] components = new Component[4];
        for (int i = 0; i < 4; i++) {
            if (i < lines.size() && lines.get(i) != null) {
                components[i] = io.papermc.paper.adventure.PaperAdventure.asVanilla(lines.get(i));
            } else {
                components[i] = net.minecraft.network.chat.Component.literal("");
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
                components[i] = Component.empty();
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
