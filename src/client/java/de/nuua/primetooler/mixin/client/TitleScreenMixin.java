package de.nuua.primetooler.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import de.nuua.primetooler.api.v1.client.text.RainbowTextRenderer;
import de.nuua.primetooler.api.v1.client.text.RainbowTextStyle;
import de.nuua.primetooler.PrimeTooler;
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
private static final String AUTHOR_LINE = "By @nuua";
private static final int COLOR_AUTHOR = 0xFFCCCCCC;
private static final int COLOR_VERSION = 0xFFB0B0B0;
	private static final int PADDING_X = 4;
	private static final int PADDING_Y = 4;
private static final RainbowTextStyle TITLE_STYLE = new RainbowTextStyle(0.25f, 0.66f, 1.0f, 1.0f, 180.0f, true);
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
		graphics.drawString(font, "v" + PrimeTooler.VERSION, PADDING_X, versionY, COLOR_VERSION, true);
		int authorY = versionY + (font.lineHeight + 2) * 2;
		graphics.drawString(font, AUTHOR_LINE, PADDING_X, authorY, COLOR_AUTHOR, true);
	}

	// Intentionally left empty: we keep vanilla title-screen buttons.
}
