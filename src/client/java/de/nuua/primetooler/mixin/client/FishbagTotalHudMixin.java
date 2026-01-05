package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.api.v1.client.hud.HudLayoutState;
import de.nuua.primetooler.features.fishbag.client.FishbagTotalHudElement;
import de.nuua.primetooler.features.fishbag.client.FishbagWeightHudElement;
import de.nuua.primetooler.features.fishbag.client.FishbagCoinsHudElement;
import de.nuua.primetooler.features.fishbag.client.FishMoneyTrackerHudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WHY: Render the fishbag total HUD element using the same hotbar overlay stage as other HUD elements.
 * PERF: O(1) per frame; inventory scan is gated by Inventory#getTimesChanged.
 */
@Mixin(Gui.class)
public class FishbagTotalHudMixin {
	@Unique
	private static final FishbagTotalHudElement primetooler$fishbagElement = new FishbagTotalHudElement();
	@Unique
	private static final FishbagWeightHudElement primetooler$fishbagWeightElement = new FishbagWeightHudElement();
	@Unique
	private static final FishbagCoinsHudElement primetooler$fishbagCoinsElement = new FishbagCoinsHudElement();
	@Unique
	private static final FishMoneyTrackerHudElement primetooler$fishMoneyTrackerElement = new FishMoneyTrackerHudElement();

	@Inject(method = "renderHotbarAndDecorations", at = @At("TAIL"))
	private void primetooler$renderFishbagTotal(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null || minecraft.player == null) {
			return;
		}
		int width = minecraft.getWindow().getGuiScaledWidth();
		int height = minecraft.getWindow().getGuiScaledHeight();
		renderElement(graphics, minecraft, width, height, primetooler$fishbagElement);
		renderElement(graphics, minecraft, width, height, primetooler$fishbagWeightElement);
		renderElement(graphics, minecraft, width, height, primetooler$fishbagCoinsElement);
		renderElement(graphics, minecraft, width, height, primetooler$fishMoneyTrackerElement);
	}

	private static void renderElement(GuiGraphics graphics, Minecraft minecraft, int width, int height,
		de.nuua.primetooler.api.v1.client.hud.HudElement element) {
		if (element == null) {
			return;
		}
		int elementWidth = element.width(minecraft);
		int elementHeight = element.height(minecraft);
		if (elementWidth <= 0 || elementHeight <= 0) {
			return;
		}
		int defaultX = element.defaultX(minecraft, width, height);
		int defaultY = element.defaultY(minecraft, width, height);
		int x = HudLayoutState.resolveXStableLeft(element.id(), width, elementWidth, defaultX);
		int y = HudLayoutState.resolveY(element.id(), height, elementHeight, defaultY);
		element.render(graphics, minecraft, x, y, 0.0f, false);
	}
}
