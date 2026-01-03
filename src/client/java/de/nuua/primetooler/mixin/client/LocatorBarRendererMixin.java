package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.locatorbar.client.LocatorBarState;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WHY: Client-side option to hide the locator bar UI.
 * PERF: Cheap early-out in render.
 */
@Mixin(LocatorBarRenderer.class)
public class LocatorBarRendererMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void primetooler$hideLocatorBar(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (!LocatorBarState.isEnabled()) {
			ci.cancel();
		}
	}
}
