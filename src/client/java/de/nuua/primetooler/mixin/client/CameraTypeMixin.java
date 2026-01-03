package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.camerazoom.client.FrontCameraToggleState;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WHY: Allow a client toggle to skip the front camera in the F5 cycle.
 * PERF: O(1) branch on perspective switch.
 * ALT: No public hook for the cycle method.
 */
@Mixin(CameraType.class)
public class CameraTypeMixin {
	@Inject(method = "cycle", at = @At("HEAD"), cancellable = true)
	private void primetooler$skipFrontCamera(CallbackInfoReturnable<CameraType> cir) {
		if (!FrontCameraToggleState.isDisabled()) {
			return;
		}
		CameraType current = (CameraType) (Object) this;
		if (current == CameraType.FIRST_PERSON) {
			cir.setReturnValue(CameraType.THIRD_PERSON_BACK);
			return;
		}
		cir.setReturnValue(CameraType.FIRST_PERSON);
	}
}
