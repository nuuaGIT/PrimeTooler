package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.inventoryeffects.client.HudEffectsState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WHY: Allow hiding the HUD effects overlay via a client toggle.
 * PERF: One boolean check per render call.
 * ALT: No stable Fabric hook to toggle effect rendering.
 */
@Mixin(Gui.class)
public class HudEffectsMixin {
	@Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
	private void primetooler$disableHudEffects(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
		if (!HudEffectsState.isEnabled()) {
			ci.cancel();
		}
	}
}
