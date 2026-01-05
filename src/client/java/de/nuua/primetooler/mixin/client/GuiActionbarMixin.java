package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.api.v1.client.hud.HudLayoutState;
import de.nuua.primetooler.features.actionbar.client.ActionbarMoveState;
import de.nuua.primetooler.features.actionbar.client.ActionbarHudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * WHY: Allow moving the actionbar overlay message via the HUD layout system.
 * PERF: O(1) math per actionbar render; no allocations.
 * ALT: No stable Fabric hook exists for actionbar positioning.
 */
@Mixin(Gui.class)
public abstract class GuiActionbarMixin {
	private static final int DEFAULT_TEXT_Y_OFFSET = 4;
	private static final int DEFAULT_TOP_Y_OFFSET = 72;

	@Shadow
	private Component overlayMessageString;

	@Shadow
	protected abstract Font getFont();

	@ModifyArgs(
		method = "renderOverlayMessage(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
		at = @At(
			value = "INVOKE",
			target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;"
		)
	)
	private void primetooler$moveActionbar(Args args) {
		if (!ActionbarMoveState.isEnabled()) {
			return;
		}
		Component msg = overlayMessageString;
		if (msg == null) {
			return;
		}
		Font font = getFont();
		if (font == null) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc == null || mc.getWindow() == null) {
			return;
		}
		int screenW = mc.getWindow().getGuiScaledWidth();
		int screenH = mc.getWindow().getGuiScaledHeight();
		if (screenW <= 0 || screenH <= 0) {
			return;
		}

		int w = font.width(msg);
		int elementH = font.lineHeight;
		int defaultX = (screenW - w) / 2;
		int defaultY = Math.max(0, screenH - DEFAULT_TOP_Y_OFFSET);

		int px = HudLayoutState.resolveX(ActionbarHudElement.ID, screenW, w, defaultX);
		int py = HudLayoutState.resolveY(ActionbarHudElement.ID, screenH, elementH, defaultY);

		args.set(0, (float) px + (w * 0.5f));
		args.set(1, (float) py + DEFAULT_TEXT_Y_OFFSET);
	}
}
