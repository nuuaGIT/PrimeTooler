package de.nuua.primetooler.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import de.nuua.primetooler.api.v1.client.text.RainbowTextRenderer;
import de.nuua.primetooler.api.v1.client.text.RainbowTextStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WHY: Add a small brand label on the main menu without maintaining a custom screen.
 * PERF: O(1) per frame, no allocations after class init.
 * ALT: No stable Fabric hook for title-screen draw-only overlay.
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {
private static final String TITLE_LINE = "PrimeTooler";
private static final String VERSION_LINE = "v26.0.1";
	private static final String AUTHOR_LINE = "By @nuua";
private static final int COLOR_AUTHOR = 0xFFCCCCCC;
private static final int COLOR_VERSION = 0xFFB0B0B0;
	private static final int PADDING_X = 4;
	private static final int PADDING_Y = 4;
private static final RainbowTextStyle TITLE_STYLE = new RainbowTextStyle(0.25f, 0.66f, 1.0f, 1.0f, 180.0f, true);
	private static final String REALMS_KEY = "menu.online";
	private static final String LANGUAGE_KEY = "menu.language";
	private static final String ACCESSIBILITY_KEY = "options.accessibility";
	private static final String OPTIONS_KEY = "menu.options";
	private static final String QUIT_KEY = "menu.quit";

	@Inject(method = "render", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/gui/screens/TitleScreen;renderPanorama(Lnet/minecraft/client/gui/GuiGraphics;F)V",
		shift = At.Shift.AFTER
	))
	private void primetooler$renderBlur(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		// Intentional: blur the panorama only, without the pause-menu darken overlay.
		((ScreenAccessor) (Object) this).primetooler$renderBlurredBackground(graphics);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void primetooler$render(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		var font = Minecraft.getInstance().font;
		float timeSeconds = (float) (System.nanoTime() * 1.0e-9);
		RainbowTextRenderer.draw(graphics, font, TITLE_LINE, PADDING_X, PADDING_Y, timeSeconds, TITLE_STYLE);
		int versionY = PADDING_Y + font.lineHeight + 2;
		graphics.drawString(font, VERSION_LINE, PADDING_X, versionY, COLOR_VERSION, true);
		int authorY = versionY + (font.lineHeight + 2) * 2;
		graphics.drawString(font, AUTHOR_LINE, PADDING_X, authorY, COLOR_AUTHOR, true);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void primetooler$removeRealmsButton(CallbackInfo ci) {
		ScreenAccessor accessor = (ScreenAccessor) (Object) this;
		adjustTitleButtons(accessor.primetooler$renderables(),
			accessor.primetooler$children(),
			accessor.primetooler$narratables(),
			accessor.primetooler$getWidth(),
			accessor.primetooler$getHeight());
	}

	private static void adjustTitleButtons(
		java.util.List<Renderable> renderables,
		java.util.List<GuiEventListener> children,
		java.util.List<NarratableEntry> narratables,
		int screenWidth,
		int screenHeight
	) {
		Button realmsButton = null;
		Button languageButton = null;
		Button accessibilityButton = null;
		Button optionsButton = null;
		Button quitButton = null;

		for (int i = 0; i < renderables.size(); i++) {
			Renderable renderable = renderables.get(i);
			if (!(renderable instanceof Button button)) {
				continue;
			}

			String key = getTranslationKey(button);
			if (REALMS_KEY.equals(key)) {
				realmsButton = button;
			} else if (LANGUAGE_KEY.equals(key)) {
				languageButton = button;
			} else if (ACCESSIBILITY_KEY.equals(key)) {
				accessibilityButton = button;
			} else if (OPTIONS_KEY.equals(key)) {
				optionsButton = button;
			} else if (QUIT_KEY.equals(key)) {
				quitButton = button;
			}
		}

		int targetY = realmsButton != null ? getButtonY(realmsButton) : -1;
		if (languageButton != null) {
			removeButton(languageButton, renderables, children, narratables);
		}
		if (accessibilityButton != null) {
			removeButton(accessibilityButton, renderables, children, narratables);
		}
		if (realmsButton != null) {
			removeButton(realmsButton, renderables, children, narratables);
		}
		if (targetY >= 0) {
			if (optionsButton != null) {
				setButtonY(optionsButton, targetY);
			}
			if (quitButton != null) {
				setButtonY(quitButton, targetY);
			}
		}

		removeSmallCornerButtons(renderables, children, narratables, screenWidth, screenHeight);
		removeLanguageIconButton(renderables, children, narratables);
	}

	private static void removeButton(
		Button button,
		java.util.List<Renderable> renderables,
		java.util.List<GuiEventListener> children,
		java.util.List<NarratableEntry> narratables
	) {
		renderables.remove(button);
		children.remove(button);
		narratables.remove(button);
	}

	private static String getTranslationKey(Button button) {
		Component message = button.getMessage();
		if (message == null) {
			return null;
		}
		if (message.getContents() instanceof TranslatableContents contents) {
			return contents.getKey();
		}
		return null;
	}

	private static int getButtonY(Button button) {
		return ((AbstractWidgetAccessor) (AbstractWidget) button).primetooler$getY();
	}

	private static void setButtonY(Button button, int y) {
		((AbstractWidgetAccessor) (AbstractWidget) button).primetooler$setY(y);
	}

	private static void removeSmallCornerButtons(
		java.util.List<Renderable> renderables,
		java.util.List<GuiEventListener> children,
		java.util.List<NarratableEntry> narratables,
		int screenWidth,
		int screenHeight
	) {
		int minX = 0;
		int maxX = 40;
		int minY = screenHeight - 80;
		int maxY = screenHeight;

		for (int i = renderables.size() - 1; i >= 0; i--) {
			Renderable renderable = renderables.get(i);
			if (!(renderable instanceof AbstractWidget widget)) {
				continue;
			}

			AbstractWidgetAccessor accessor = (AbstractWidgetAccessor) widget;
			int x = accessor.primetooler$getX();
			int y = accessor.primetooler$getY();
			int width = accessor.primetooler$getWidth();
			int height = accessor.primetooler$getHeight();

			if (width > 26 || height > 26) {
				continue;
			}
			if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
				renderables.remove(i);
				children.remove(widget);
				narratables.remove(widget);
			}
		}
	}

	private static void removeLanguageIconButton(
		java.util.List<Renderable> renderables,
		java.util.List<GuiEventListener> children,
		java.util.List<NarratableEntry> narratables
	) {
		for (int i = renderables.size() - 1; i >= 0; i--) {
			Renderable renderable = renderables.get(i);
			if (!(renderable instanceof AbstractWidget widget)) {
				continue;
			}
			String className = widget.getClass().getName();
			if (!className.contains("SpriteIconButton")) {
				continue;
			}
			String label = widget.getMessage() != null ? widget.getMessage().getString() : "";
			if (!label.startsWith("Language")) {
				continue;
			}
			renderables.remove(i);
			children.remove(widget);
			narratables.remove(widget);
		}
	}
}
