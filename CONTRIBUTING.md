# Contibution

## Contributions are welcome!

I will accept pull requests if they meet the standards and don't break the architecture of this project.
In case I reject a pull request, I will add a comment explaining, why I rejected it. It doesn't mean
that it's bad or that there is no chance of it ever being approved, but it often needs to be reworked
or tweaked a little.

It would be very much appreciated if you documented your code and describe your pull request as detailed
as possible, allowing me to understand your contribution as fast as possible. Contributors will be mentioned
in the `README.md` (except you don't want to be mentioned of course)!

---

## How the mod generally works
The mod loads emotes from the providers (Twitch, 7tv, BTTV, FFZ) and saves their information
(such as image URL, name, id, if it's an overlay and the scale) in the `EmoteRegistry`.
When the user receives a messages that is to be displayed in chat, the method `ComponentUtils.wrapComponents()`
is called to calculate where the chat lines need to be split. This method receives a `FormattedText` object and
I replace it with my own wrapper called `EmoteReplacingText`. This collects all components with their
style and replaces emote names with a custom character (`0xE000`) and a custom style containing the emote name in the
`insertion`. The name is only replaced if the name is contained in the registry **and** the image has already been
downloaded, registered and saved in the `EmoteImageCache`. If the image hasn't been downloaded, it will be downloaded
and put in the `EmoteImageCache`.

When the string is rendered, Minecraft iterates over all the characters and gets their corresponding `BakedGlyph` in
the `Font.getGlyph()`. The mod injects there and checks if it's the custom character (`0xE000`) and gets or creates
an instance of my baked glyph which handles the rendering.

> [!NOTE]
> Animated emote images are also just `EmoteImage` objects that are updated every tick in `TwitchEmotes.onTick()`

> [!NOTE]
> Overlay emote images are also just `EmoteImage` objects that reference two other `EmoteImage`s. Therefore,
> it is necessary to handle dependencies between `EmoteImage`s in the `EmoteImageCache`

> [!NOTE]
> When a download or composition is completed, it will set a flag `TwitchEmotes.chatRefreshNeeded`.
> This flag is read and reset every 10 ticks. And rebuilds the entire chat (calls `ChatComponent.rescaleChat()`)
---

## Tasks for developers

Currently, there are a few things on the list that crossed my mind, but that I decided against to implement.
Reasons may differ, but mostly because I wanted to release the mod quickly.
You can pick anything you want or suggest your own ideas!

---

### Cache
Currently, all the emote images (including overlays) are cached in the EmoteImageCache
when downloaded/composited. But these images are never unloaded except you change a provider or restart your game.

Having a limit of the cache would be useful, so that not too many emotes are loaded at the same time.

LRU would be enough, but overlay emotes would need to be taken into consideration.

---

### Caching
It can be quite annoying that, every time you restart Minecraft, emotes have to be downloaded again,
although no settings changed. So a method of caching the emote images on the disk would be reasonable.

---

### Improving animated emotes
Currently, each frame of an animation is saved in a separate NativeImage and the DynamicTexture updated every tick.
I could imagine, this is very bad and slow.

Uploading the entire animation to the GPU as a single NativeImage that never needs to be updated seems to be
  reasonable. The animation will play by only shifting the UVs accordingly.

As overlay emotes are composited on the CPU, this has to be taken into consideration.

---

### Improving overlay emotes
As I have not found a good way of just rendering 2 emotes on top of each other, every time an overlay is recognized,
a completely new OverlayEmoteImage is created. It just combines two `EmoteImage`s by compositing them on the CPU.
This is an extreme overhead.

Why don't I use the first emote with advance 0 and the overlay is just the next glyph?
Because if the overlay is wider than the actual base image, the base image has to be centered.

---

### Rebuilding chat

When using other mods such as Chat Patches which can show the entire history, it can take a long time for all
images to be downloaded. And every time an image is downloaded it sets the flag for rebuilding the chat which
resets the scrolling position. That basically means, you can't scroll in the first X seconds of joining.
Possible solutions would be:
- inject a custom `FormattedCharSequence` **after** line splitting is done, so that the chat doesn't need to be
  rebuilt and still shows the just loaded images and only rebuilds the chat after there are no more pending downloads.
  This is like heuristic: Lines might not be correctly formatted at first, but shows the emotes for the time being.
- only rebuild after there are no more pending downloads (could lead to users thinking the mod isn't working at first)

---

### Twitch Integration
I've already implemented a way to login, so it would be nice if there's another tab in the config screen 
where you can join a Twitch chat which is drawn directly into the Minecraft chat.

As the twitch login data is quite confidential I would rather not use a library and do it manually

Features include:
- Merging Twitch Chat with Minecraft Chat (they should be differentiable, e.g. customizable color)
- Twitch Badges in front of their names
- Chat back functionality (sending a message in Minecraft will send a message on Twitch)

---

### Your Ideas are welcome as well :}