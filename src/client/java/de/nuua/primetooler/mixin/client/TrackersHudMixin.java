package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.api.v1.client.hud.HudLayoutState;
import de.nuua.primetooler.features.jobtracker.client.JobMoneyTrackerHudElement;
import de.nuua.primetooler.features.jobtracker.client.JobXpTrackerHudElement;
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
 * WHY: Render tracker HUD elements in the hotbar/decorations stage (consistent with other overlays).
 * PERF: O(1) per frame; actual parsing/tracking is done in low-frequency tick modules.
 * ALT: No shared vanilla "HUD elements" render registry exists.
 */
@Mixin(Gui.class)
public final class TrackersHudMixin {
	@Unique
	private static final JobXpTrackerHudElement primetooler$jobXpElement = new JobXpTrackerHudElement();
	@Unique
	private static final JobMoneyTrackerHudElement primetooler$jobMoneyElement = new JobMoneyTrackerHudElement();

	@Inject(method = "render", at = @At("TAIL"))
	private void primetooler$renderTrackerHud(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null || minecraft.player == null) {
			return;
		}
		int width = minecraft.getWindow().getGuiScaledWidth();
		int height = minecraft.getWindow().getGuiScaledHeight();
		renderElement(graphics, minecraft, width, height, primetooler$jobXpElement);
		renderElement(graphics, minecraft, width, height, primetooler$jobMoneyElement);
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
		int x = HudLayoutState.resolveX(element.id(), width, elementWidth, defaultX);
		int y = HudLayoutState.resolveY(element.id(), height, elementHeight, defaultY);
		element.render(graphics, minecraft, x, y, 0.0f, false);
	}
}
