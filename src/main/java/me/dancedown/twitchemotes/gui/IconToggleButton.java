package me.dancedown.twitchemotes.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IconToggleButton extends Button {

    private boolean value;
    private final boolean renderCheckbox;
    private final ResourceLocation icon;
    private final Consumer<Boolean> onToggle;
    private final int spriteWidth;
    private final int spriteHeight;

    public IconToggleButton(
            int x, int y,
            int width, int height,
            Component label,
            ResourceLocation icon,
            boolean initial,
            boolean renderCheckbox,
            Consumer<Boolean> onToggle
    ) {
        super(x, y, width, height, label, b -> {}, Button.DEFAULT_NARRATION);
        this.icon = icon;
        this.value = initial;
        this.renderCheckbox = renderCheckbox;
        this.onToggle = onToggle;
        this.spriteWidth = getSpriteWidth(icon);
        this.spriteHeight = getSpriteHeight(icon);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {

        // background
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        int bgColor = isHoveredOrFocused() ? 0x4F888888 : 0x4F000000;
        guiGraphics.fill(x, y, x+w, y+h, bgColor);

        // icon
        boolean iconOnly = this.getMessage().getString().isEmpty() && !renderCheckbox;
        int maxIconWidth  = (int)(this.getWidth() * 0.8f);
        int maxIconHeight = (int)(this.getHeight() * (iconOnly ? 0.8f : 0.6f));
        float scale = Math.min(
                (float) maxIconWidth  / spriteWidth,
                (float) maxIconHeight / spriteHeight
        );
        int drawW = (int)(spriteWidth * scale);
        int drawH = (int)(spriteHeight * scale);
        int drawX = this.getX() + (this.getWidth() - drawW) / 2;
        int drawY = iconOnly
                ? this.getY() + (this.getHeight() - drawH) / 2
                : this.getY() + 6;
        guiGraphics.blit(new ResourceLocation(icon.getNamespace(), "textures/gui/sprites/" + icon.getPath() + ".png"),
                drawX,
                drawY,
                drawW,
                drawH,
                0,
                0,
                spriteWidth,
                spriteHeight,
                spriteWidth,
                spriteHeight
        );

        // text
        if(!iconOnly) {
            Font font = Minecraft.getInstance().font;
            int maxTextWidth = Math.max(1, width - 6);
            List<FormattedCharSequence> lines = new ArrayList<>(font.split(this.getMessage(), maxTextWidth));
            if(lines.size() > 2) {
                lines = lines.subList(0, 2);
                String shortened = font.plainSubstrByWidth(
                        lines.get(1).toString(), maxTextWidth - font.width("...")
                ) + "...";
                lines.set(1, FormattedCharSequence.forward(shortened, Style.EMPTY));
            }
            int baseY = y + height - 4 - lines.size() * font.lineHeight;

            for(int index = 0; index < lines.size(); index++)
                guiGraphics.drawCenteredString(font, lines.get(index), x + width / 2, baseY + index * (font.lineHeight + 1), 0xFFFFFFFF);
        }

        // checkbox
        if(renderCheckbox) {
            int boxSize = 9;
            int bx = this.getX() + this.getWidth() - boxSize - 4;
            int by = this.getY() + 4;

            guiGraphics.fill(
                    bx, by,
                    bx + boxSize, by + boxSize,
                    0xFF202020
            );

            guiGraphics.hLine(bx, bx + boxSize, by, Color.DARK_GRAY.getRGB());
            guiGraphics.hLine(bx, bx + boxSize, by + boxSize, Color.DARK_GRAY.getRGB());
            guiGraphics.vLine(bx, by, by + boxSize, Color.DARK_GRAY.getRGB());
            guiGraphics.vLine(bx + boxSize, by, by + boxSize, Color.DARK_GRAY.getRGB());

            renderToggleMark(guiGraphics, bx + 1, by + 1, boxSize - 1, value);
        }

        // frame
        int frameColor = this.isHovered()
                ? 0xFFFFFFFF
                : 0xFF888888;

        guiGraphics.hLine(x, x + w - 1, y, frameColor);
        guiGraphics.hLine(x, x + w - 1, y + h - 1, frameColor);
        guiGraphics.vLine(x, y, y + h - 1, frameColor);
        guiGraphics.vLine(x + w - 1, y, y + h - 1, frameColor);
    }

    @Override
    public void onPress() {
        value = !value;
        onToggle.accept(value);
        super.onPress();
    }

    public static void renderToggleMark(GuiGraphics guiGraphics, int x, int y, int size, boolean value) {
        int color = value ? 0xFF55FF55 : 0xFFFF5555;
        int padding = Math.max(1, size / 6);
        int thickness = Math.max(1, size / 6);
        if(value) {
            int leftX = x + padding;
            int leftY = y + size / 2;
            int centerX = x + size / 2 - thickness;
            int centerY = y + size - padding - thickness;
            int rightX = x + size - padding - thickness;
            int rightY = y + padding;

            drawLine(guiGraphics, leftX, leftY, centerX, centerY, thickness, color);
            drawLine(guiGraphics, centerX, centerY, rightX, rightY, thickness, color);
        } else {
            drawLine(guiGraphics, x + padding, y + padding,
                    x + size - padding - thickness, y + size - padding - thickness,
                    thickness, color);
            drawLine(guiGraphics, x + size - padding - thickness, y + padding,
                    x + padding, y + size - padding - thickness,
                    thickness, color);
        }
    }

    private static void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int thickness, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if(steps == 0) {
            guiGraphics.fill(x1, y1, x1 + thickness, y1 + thickness, color);
            return;
        }

        for(int i = 0; i <= steps; i++) {
            int px = x1 + Math.round((x2 - x1) * (i / (float) steps));
            int py = y1 + Math.round((y2 - y1) * (i / (float) steps));
            guiGraphics.fill(px, py, px + thickness, py + thickness, color);
        }
    }

    private static int getSpriteWidth(ResourceLocation icon) {
        return switch (icon.getPath()) {
            case "7tv" -> 128;
            case "frankerfacez" -> 249;
            case "twitch" -> 1371;
            case "overlay" -> 200;
            default -> 512;
        };
    }

    private static int getSpriteHeight(ResourceLocation icon) {
        return switch (icon.getPath()) {
            case "7tv" -> 128;
            case "frankerfacez" -> 195;
            case "twitch" -> 1600;
            case "overlay" -> 200;
            default -> 512;
        };
    }
}
