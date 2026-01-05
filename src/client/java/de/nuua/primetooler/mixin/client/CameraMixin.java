package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.camerazoom.client.CameraZoomState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * WHY: Apply a custom third-person zoom distance while keeping vanilla collision clipping.
 * PERF: Matches vanilla camera raycast cost (O(1) per setup), no extra allocations.
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
		float clipped = ((CameraAccessor) (Object) this).primetooler$invokeGetMaxZoom(CameraZoomState.getDistance());
		return -clipped;
	}
}
