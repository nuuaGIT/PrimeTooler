package de.nuua.primetooler.platform.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * WHY: Zentrale Registrierung der PrimeTooler-Keybinds mit eigener Kategorie.
 * PERF: Einmalige Registrierung, danach nur Referenzen.
 */
public final class PrimeToolerKeyBindings {
	public static final String CATEGORY_KEY = "key.categories.primetooler";
	public static final String KEY_OPEN_MENU = "key.primetooler.open_menu";
	public static final String KEY_SLOT_LOCK = "key.primetooler.slot_lock";

	private static KeyMapping openMenuKey;
	private static KeyMapping slotLockKey;

	private PrimeToolerKeyBindings() {
	}

	public static void registerAll() {
		if (openMenuKey != null) {
			return;
		}
		KeyMapping.Category category = registerCategory(CATEGORY_KEY);
		openMenuKey = KeyBindingRegistrar.register(
			KEY_OPEN_MENU,
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_P,
			category
		);
		slotLockKey = KeyBindingRegistrar.register(
			KEY_SLOT_LOCK,
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_L,
			category
		);
	}

	public static KeyMapping openMenuKey() {
		return openMenuKey;
	}

	public static KeyMapping slotLockKey() {
		return slotLockKey;
	}

	public static String keyName(KeyMapping key) {
		if (key == null) {
			return "";
		}
		Component component = key.getTranslatedKeyMessage();
		return component == null ? "" : component.getString();
	}

	private static KeyMapping.Category registerCategory(String key) {
		try {
			var method = KeyMapping.Category.class.getDeclaredMethod("register", String.class);
			method.setAccessible(true);
			Object value = method.invoke(null, key);
			return value instanceof KeyMapping.Category category ? category : KeyMapping.Category.MISC;
		} catch (ReflectiveOperationException ignored) {
			return KeyMapping.Category.MISC;
		}
	}
}
