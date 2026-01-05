package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.camerazoom.client.CameraZoomState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Overlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WHY: Allow advanced third-person zoom via the scroll wheel.
 * PERF: Small math only when scrolling.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void primetooler$handleThirdPersonZoom(long window, double xOffset, double yOffset, CallbackInfo ci) {
		Overlay overlay = minecraft.getOverlay();
		if (overlay != null || minecraft.screen != null || minecraft.player == null) {
			return;
		}
		if (!CameraZoomState.isEnabled()) {
			return;
		}
		if (minecraft.player.isSpectator()) {
			return;
		}

		Options options = minecraft.options;
		if (options.getCameraType().isFirstPerson()) {
			return;
		}

		boolean discrete = options.discreteMouseScroll().get();
		double sensitivity = options.mouseWheelSensitivity().get();
		double scrollY = (discrete ? Math.signum(yOffset) : yOffset) * sensitivity;
		if (CameraZoomState.applyScroll(scrollY)) {
			if (scrollY > 0.0 && CameraZoomState.isAtMin()) {
				options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
			}
			ci.cancel();
		}
	}

}
