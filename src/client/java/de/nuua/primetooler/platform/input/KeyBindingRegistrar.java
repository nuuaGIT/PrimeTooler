package de.nuua.primetooler.platform.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public final class KeyBindingRegistrar {
	private KeyBindingRegistrar() {
	}

	public static KeyMapping register(
		String translationKey,
		InputConstants.Type type,
		int keyCode,
		KeyMapping.Category category
	) {
		return KeyBindingHelper.registerKeyBinding(new KeyMapping(translationKey, type, keyCode, category));
	}
}
