package me.dancedown.twitchemotes.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public final class ToastNotification {

    /**
     * Creates and shows a Minecraft Notification Popup (Toast)
     * @param transTitle A translation key for the title of the Toast
     * @param transDesc A translation key for the description of the Toast or <code>null</code> if not needed
     */
    public static void toast(@NotNull String transTitle, @Nullable String transDesc, Color color) {
        toast(Component.translatable(transTitle).withStyle(style -> style.withColor(color.getRGB())),
                transDesc != null ? Component.translatable(transDesc).withStyle(style -> style.withColor(color.getRGB())) : null);
    }

    public static void toast(@NotNull MutableComponent title, @Nullable MutableComponent desc) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.getToasts().addToast(
                new SystemToast(SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, title, desc))
        );
    }

}
