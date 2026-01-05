package de.nuua.primetooler.mixin.client;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * WHY: Reuse vanilla third-person camera collision by invoking the internal zoom clamp.
 * PERF: Single raycast per camera setup, matching vanilla behavior.
 */
@Mixin(Camera.class)
public interface CameraAccessor {
	@Invoker("getMaxZoom")
	float primetooler$invokeGetMaxZoom(float desiredDistance);
}
