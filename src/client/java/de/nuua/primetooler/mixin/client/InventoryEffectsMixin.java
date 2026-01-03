package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.inventoryeffects.client.InventoryEffectsState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WHY: Allow hiding the inventory effects overlay via a client toggle.
 * PERF: One boolean check per render call.
 * ALT: No stable Fabric hook to toggle effect rendering.
 */
@Mixin(EffectsInInventory.class)
public class InventoryEffectsMixin {
	@Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
	private void primetooler$disableEffectsOverlay(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
		if (!InventoryEffectsState.isEnabled()) {
			ci.cancel();
		}
	}
}
