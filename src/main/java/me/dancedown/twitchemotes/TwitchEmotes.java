package me.dancedown.twitchemotes;

import me.dancedown.twitchemotes.config.TwitchEmotesConfig;
import me.dancedown.twitchemotes.emote.image.EmoteImageCache;
import me.dancedown.twitchemotes.emote.type.Emote;
import me.dancedown.twitchemotes.emote.EmoteRegistry;
import me.dancedown.twitchemotes.emote.image.EmoteImage;
import me.dancedown.twitchemotes.emote.type.LoadState;
import me.dancedown.twitchemotes.emote.type.EmoteScope;
import me.dancedown.twitchemotes.gui.ToastNotification;
import me.dancedown.twitchemotes.network.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class TwitchEmotes implements ClientModInitializer {

    private static final int ticksPerChatRefresh = 10;

    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("twitchemotes").orElseThrow(RuntimeException::new);
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(MOD_NAME + ".json").toFile();
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static TwitchEmotesConfig CONFIG = TwitchEmotesConfig.load(CONFIG_FILE);

    private static final ExecutorService LOAD_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final ExecutorService REGISTRY_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Map<ProviderType, EmoteProvider> PROVIDERS = new EnumMap<>(ProviderType.class);
    private static final Map<ProviderType, LoadState> LOAD_STATES = new EnumMap<>(ProviderType.class);

    public static final EmoteRegistry EMOTE_REGISTRY = new EmoteRegistry();
    public static final EmoteImageCache EMOTE_IMAGE_CACHE = new EmoteImageCache();

    public static final AtomicBoolean chatRefreshNeeded = new AtomicBoolean(false);
    private static int refreshCounter = 0;

    @Override
    public void onInitializeClient() {
        if(!CONFIG.isValid())
            CONFIG = TwitchEmotesConfig.create(CONFIG_FILE);
        PROVIDERS.put(ProviderType.TWITCH, new TwitchEmoteProvider());
        PROVIDERS.put(ProviderType.FFZ, new FFZEmoteProvider());
        PROVIDERS.put(ProviderType.BTTV, new BTTVEmoteProvider());
        PROVIDERS.put(ProviderType.STV, new STVEmoteProvider());
        for (ProviderType type : ProviderType.values())
            LOAD_STATES.put(type, new LoadState(false, null));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOAD_EXECUTOR.shutdownNow();
            REGISTRY_EXECUTOR.shutdownNow();
        }));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        refresh(false);
    }

    /**
     * Runs <code>tick()</code> on all emotes in the cache if animation is enabled in the config.
     * This is used to update the current frame of animated emotes
     * @param minecraft The client (ignored)
     */
    private void onTick(Minecraft minecraft) {
        // update animation frames
        if(CONFIG.animateEmotes)
            EMOTE_IMAGE_CACHE.foreach(EmoteImage::tick);

        // reload chat lines if necessary
        if(refreshCounter++ >= ticksPerChatRefresh) {
            refreshCounter = 0;
            if(chatRefreshNeeded.compareAndSet(true, false))
                minecraft.gui.getChat().rescaleChat();
        }
    }

    public static void execute(Runnable runnable) {
        LOAD_EXECUTOR.submit(runnable);
    }

    /**
     * Refreshes all the changes made in the config
     * @param feedback Whether to show toasts or not
     */
    public static void refresh(boolean feedback) {
        ReloadResult reloadResult = new ReloadResult();
        for (ProviderType type : ProviderType.values()) {
            ProviderResult res = reloadProviderIfNeeded(type);
            if(res.wasRequested())
                reloadResult.add(type, res);
        }
        if (feedback)
            showReloadFeedback(reloadResult);
        TwitchEmotes.chatRefreshNeeded.set(true);
    }

    /**
     * Forces all providers to reload their emotes
     * @param feedback Whether to show result toasts or not
     */
    public static void reload(boolean feedback) {
        ReloadResult reloadResult = new ReloadResult();
        for (ProviderType type : ProviderType.values())
            reloadResult.add(type, reloadProvider(type));
        if (feedback)
            showReloadFeedback(reloadResult);
        TwitchEmotes.chatRefreshNeeded.set(true);
    }

    /**
     * Forces the provider to reload its emotes (used for twitch login)
     * @param type The provider that is supposed to reload its emotes
     * @param feedback Whether it should show feedback or not
     */
    public static void reloadProvider(ProviderType type, boolean feedback) {
        ReloadResult reloadResult = new ReloadResult();
        reloadResult.add(type, reloadProvider(type));
        if (feedback)
            showReloadFeedback(reloadResult);
        TwitchEmotes.chatRefreshNeeded.set(true);
    }

    /**
     * Shows toasts with information about the data retrieved e.g. if everything was successful
     * @param result The result of the loader
     */
    private static void showReloadFeedback(ReloadResult result) {
        // Nothing changed -> show nothing.
        if (result.providerResults.isEmpty())
            return;

        if(result.providerResults.size() > 1) {
            if (result.successful()) {
                ToastNotification.toast(
                        Component.translatable("toast.twitchemotes.reloaded")
                                .withColor(Color.GREEN.getRGB()),
                        null
                );
                return;
            } else if (result.failed()) {
                ToastNotification.toast(
                        Component.translatable("toast.twitchemotes.reloadfailed")
                                .withColor(Color.RED.getRGB()),
                        null
                );
                return;
            }
        }

        // go through individual provider results
        for (Map.Entry<ProviderType, ProviderResult> entry : result.providerResults.entrySet()) {

            ProviderType type = entry.getKey();
            ProviderResult providerResult = entry.getValue();

            if(!providerResult.wasRequested())
                continue;

            if(providerResult.scopeResults.size() > 1) {
                if (providerResult.successful()) {
                    ToastNotification.toast(
                            Component.translatable("toast.twitchemotes.reloadedprovider", type.getDisplayName())
                                    .withColor(Color.GREEN.getRGB()),
                            null
                    );
                    continue;
                } else if (providerResult.failed()) {
                    ToastNotification.toast(
                            Component.translatable("toast.twitchemotes.reloadproviderfailed", type.getDisplayName())
                                    .withColor(Color.RED.getRGB()),
                            null
                    );
                    continue;
                }
            }

            // go through scope results
            for(Map.Entry<EmoteScope, ScopeResult> scopeEntry : providerResult.scopeResults.entrySet()) {
                EmoteScope scope = scopeEntry.getKey();
                ScopeResult scopeResult = scopeEntry.getValue();
                if(!scopeResult.wasRequested())
                    continue;
                if(scopeResult.successful())
                    ToastNotification.toast(
                            Component.translatable("toast.twitchemotes.reloadedproviderscope", type.getDisplayName(), scope.getDisplayName())
                                    .withColor(Color.GREEN.getRGB()),
                            null
                    );
                else
                    ToastNotification.toast(
                            Component.translatable("toast.twitchemotes.reloadproviderscopefailed", type.getDisplayName(), scope.getDisplayName())
                                    .withColor(Color.RED.getRGB()),
                            null
                    );
            }
        }
    }

    /**
     * Reloads all emotes from a provider regardless if they're already loaded
     * @param type The provider to reload
     */
    private static ProviderResult reloadProvider(ProviderType type) {
        if(CONFIG.isProviderEnabled(type))
            LOAD_STATES.put(type, new LoadState(false, null));
        return reloadProviderIfNeeded(type);
    }

    /**
     * Reloads channel and global emotes of a specific provider if provider has been toggled in the config
     * @param type The provider type to change
     */
    private static ProviderResult reloadProviderIfNeeded(ProviderType type) {

        boolean enabled = CONFIG.isProviderEnabled(type);
        LoadState state = LOAD_STATES.get(type);
        ProviderResult result = new ProviderResult();

        if (!enabled) {
            if (state.globalsLoaded() || state.channelLoaded() != null) {
                LOGGER.info("Removing all {} emotes!",  type.getDisplayName());
                registryExecuteAndWait(() -> EMOTE_REGISTRY.removeProvider(type));
            }
            LOAD_STATES.put(type, new LoadState(false, null));
            return result;
        }
        result.add(EmoteScope.GLOBAL, reloadProviderGlobalsIfNeeded(type));
        result.add(EmoteScope.CHANNEL, reloadProviderChannelIfNeeded(type));
        return result;
    }

    /**
     * Reloads channel emotes from a provider if channel has changed
     * @param type The provider type
     */
    private static ScopeResult reloadProviderChannelIfNeeded(ProviderType type) {
        LoadState state = LOAD_STATES.get(type);
        String channel = CONFIG.twitchChannelName;
        if (channel == null || channel.isBlank()) {
            if (state.channelLoaded() != null) {
                LOGGER.info("Removing {} channel emotes!",  type.getDisplayName());
                boolean res = registryExecuteAndWait(() -> EMOTE_REGISTRY.removeAll(type, EmoteScope.CHANNEL));
                LOAD_STATES.put(type, new LoadState(state.globalsLoaded(), null));
                return new ScopeResult(res, true);
            } else return new ScopeResult(true, false);
        }
        if (channel.equals(state.channelLoaded()))
            return new ScopeResult(true, false);

        LOGGER.info("Loading {} channel emotes!",  type.getDisplayName());
        try {
            List<Emote> channelEmotes = PROVIDERS.get(type).getChannelEmotes(CONFIG);
            if(registryExecuteAndWait(() -> EMOTE_REGISTRY.replaceAll(type, EmoteScope.CHANNEL, channelEmotes))) {
                LOAD_STATES.put(type, new LoadState(state.globalsLoaded(), channel));
                return new ScopeResult(true,  true);
            }
        } catch (Exception e) {
            TwitchEmotes.LOGGER.warn("Failed to load {} channel emotes", type.getDisplayName());
        }
        return new ScopeResult(false, true);
    }

    /**
     * Reloads global emotes from a provider
     * @param type The provider type
     */
    private static ScopeResult reloadProviderGlobalsIfNeeded(ProviderType type) {
        LoadState state = LOAD_STATES.get(type);
        if (state.globalsLoaded())
            return new ScopeResult(true, false);
        LOGGER.info("Loading {} global emotes!",  type.getDisplayName());
        try {
            List<Emote> globals = PROVIDERS.get(type).getGlobalEmotes(CONFIG);
            if(registryExecuteAndWait(() -> EMOTE_REGISTRY.replaceAll(type, EmoteScope.GLOBAL, globals))) {
                LOAD_STATES.put(type, new LoadState(true, state.channelLoaded()));
                return new ScopeResult(true, true);
            }
        } catch (Exception ignored) {
            TwitchEmotes.LOGGER.warn("Failed to load {} global emotes", type.getDisplayName());
        }
        return new ScopeResult(false, true);
    }

    /**
     * Executes runnable in the registry executor to prevent async writes. Waits for the runnable to complete.
     * @param runnable The runnable to execute by the registry executor
     * @return <code>true</code> if execution completed successfully, else <code>false</code>
     */
    private static boolean registryExecuteAndWait(@NotNull Runnable runnable) {
        try {
            REGISTRY_EXECUTOR.submit(runnable).get(10, TimeUnit.SECONDS);
        } catch (RejectedExecutionException | CancellationException | ExecutionException | InterruptedException | TimeoutException e) {
            LOGGER.error("Emote registry execution failed", e);
            return false;
        }
        return true;
    }
}

class ReloadResult {
    public final Map<ProviderType, ProviderResult> providerResults = new HashMap<>();

    /**
     * Adds a ProviderResult for a ProviderType
     * @param type The ProviderType
     * @param result The result of the reload of this provider type
     */
    public void add(ProviderType type, ProviderResult result) {
        providerResults.put(type, result);
    }

    /**
     * @return Whether all providers were successfully reloaded
     */
    public boolean successful() {
        return providerResults.values().stream().allMatch(ProviderResult::successful);
    }

    /**
     * @return If none of the providers were successful
     */
    public boolean failed() {
        return providerResults.values().stream().allMatch(ProviderResult::failed);
    }

}

class ProviderResult {
    public final Map<EmoteScope, ScopeResult> scopeResults = new HashMap<>();
    private final Predicate<ScopeResult> successFunction = scopeResult -> scopeResult.successful() || !scopeResult.wasRequested();

    /**
     * Adds a ScopeResult for an EmoteScope
     * @param scope The EmoteScope
     * @param result The result of the reload of this scope
     */
    public void add(EmoteScope scope, ScopeResult result) {
        scopeResults.put(scope, result);
    }

    /**
     * @return If all scopeResults are successful
     */
    public boolean successful() {
        return scopeResults.values().stream().allMatch(successFunction);
    }

    /**
     * @return If none of the scopes were successful
     */
    public boolean failed() {
        return scopeResults.values().stream().noneMatch(successFunction);
    }

    /**
     * @return If any of the scopes had changes
     */
    public boolean wasRequested() {
        return scopeResults.values().stream().anyMatch(ScopeResult::wasRequested);
    }
}

/**
 * The result of reloading a scope of a provider
 * @param successful Whether the reload has been successful or not
 * @param wasRequested If something even changed
 */
record ScopeResult(boolean successful, boolean wasRequested) {}
