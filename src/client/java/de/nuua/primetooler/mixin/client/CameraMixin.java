package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.camerazoom.client.CameraZoomState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * WHY: Apply custom third-person zoom distance without collision clipping.
 * PERF: O(1), avoids extra allocations.
 */
@Mixin(Camera.class)
public class CameraMixin {
	@ModifyArg(
		method = "setup",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V"),
		index = 0
	)
	private float primetooler$useCustomZoom(float original) {
		if (!CameraZoomState.isEnabled()) {
			return original;
		}
		if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			return original;
		}
		return -CameraZoomState.getDistance();
	}
}
