package me.dancedown.twitchemotes.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IconToggleButton extends Button {

    private boolean value;
    private final boolean renderCheckbox;
    private final Identifier icon;
    private final Consumer<Boolean> onToggle;
    private final int spriteWidth;
    private final int spriteHeight;

    public IconToggleButton(
            int x, int y,
            int width, int height,
            Component label,
            Identifier icon,
            boolean initial,
            boolean renderCheckbox,
            Consumer<Boolean> onToggle
    ) {
        super(x, y, width, height, label, b -> {}, Button.DEFAULT_NARRATION);
        this.icon = icon;
        this.value = initial;
        this.renderCheckbox = renderCheckbox;
        this.onToggle = onToggle;
        TextureAtlas textureAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
        try (SpriteContents c = textureAtlas.getSprite(icon).contents()){
            this.spriteWidth = c.width();
            this.spriteHeight = c.height();
        }
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {

        // background
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        int bgColor = isHoveredOrFocused() ? 0x4F888888 : 0x4F000000;
        guiGraphics.fill(x, y, x+w, y+h, bgColor);

        // icon
        int maxIconWidth  = (int)(this.getWidth() * 0.8f);
        int maxIconHeight = (int)(this.getHeight() * 0.6f);
        float scale = Math.min(
                (float) maxIconWidth  / spriteWidth,
                (float) maxIconHeight / spriteHeight
        );
        int drawW = (int)(spriteWidth * scale);
        int drawH = (int)(spriteHeight * scale);
        int drawX = this.getX() + (this.getWidth() - drawW) / 2;
        int drawY = this.getY() + 6;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                icon,
                drawX,
                drawY,
                drawW,
                drawH
        );

        // text
        Font font = Minecraft.getInstance().font;
        int maxTextWidth = Math.max(1, width - 6);
        List<FormattedCharSequence> lines = new ArrayList<>(font.split(this.message, maxTextWidth));
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

            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    Identifier.withDefaultNamespace("pending_invite/" + (value ? "accept" : "reject")),
                    bx + 1, by + 1, boxSize - 1, boxSize - 1);
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
    public void onPress(@NonNull InputWithModifiers inputWithModifiers) {
        value = !value;
        onToggle.accept(value);
        super.onPress(inputWithModifiers);
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return false;
    }
}
