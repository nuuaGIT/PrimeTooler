package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.playermark.client.PlayerMarkRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WHY: Prefix tab list names with mod marker icons.
 * PERF: One component concat per name render.
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
	@Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
	private void primetooler$prefixTabName(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
		Component name = cir.getReturnValue();
		if (name == null || info == null || info.getProfile() == null) {
			return;
		}
		float timeSeconds = (float) (System.nanoTime() * 1.0e-9);
		Component decorated = PlayerMarkRegistry.decorateMarkedName(
			info.getProfile().id(),
			name,
			info.getProfile().name(),
			timeSeconds,
			Minecraft.getInstance().font
		);
		cir.setReturnValue(decorated);
	}
}
