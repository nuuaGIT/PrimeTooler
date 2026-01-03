package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.locatorbar.client.LocatorBarState;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WHY: Prefer the XP bar when locator bar is disabled.
 * PERF: Single conditional on contextual info selection.
 */
@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "nextContextualInfoState", at = @At("RETURN"), cancellable = true)
	private void primetooler$forceExperienceBar(CallbackInfoReturnable<Object> cir) {
		if (LocatorBarState.isEnabled()) {
			return;
		}
		Object value = cir.getReturnValue();
		if (value == null) {
			return;
		}
		if ("LOCATOR".equals(value.toString())) {
			Object experience = resolveEnum(value.getClass(), "EXPERIENCE");
			if (experience != null) {
				cir.setReturnValue(experience);
			}
		}
	}

	private static Object resolveEnum(Class<?> type, String name) {
		if (type == null || name == null) {
			return null;
		}
		if (!type.isEnum()) {
			return null;
		}
		Object[] constants = type.getEnumConstants();
		if (constants == null) {
			return null;
		}
		for (int i = 0; i < constants.length; i++) {
			Object constant = constants[i];
			if (constant != null && name.equals(constant.toString())) {
				return constant;
			}
		}
		return null;
	}
}
