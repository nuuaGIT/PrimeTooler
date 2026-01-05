package de.nuua.primetooler.features.autoangelsystem.client;

import de.nuua.primetooler.mixin.client.GuiTitleAccessor;
import de.nuua.primetooler.mixin.client.KeyMappingAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * WHY: Detect a specific private-use glyph in overlay/system messages for fishing automation feedback.
 * PERF: Polling is gated to 0.25s; each check is an O(n) scan on small strings.
 */
public final class AutoAngelSystemState {
	private static final char[] TRIGGER_GLYPHS = new char[] { '\uEA03', '\uEA13', '\uEA23' };
	private static final int USE_PRESS_TICKS = 2;

	private static boolean enabled;
	private static boolean lastPresent;
	private static int usePressTicksRemaining;
	private static InputConstants.Key pressedUseKey;

	private AutoAngelSystemState() {
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static boolean toggleEnabled() {
		setEnabled(!enabled);
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		AutoAngelSystemState.enabled = enabled;
		AutoAngelSystemState.lastPresent = false;
		if (!enabled) {
			cancelUsePress(Minecraft.getInstance());
		}
	}

	public static void pollClient() {
		if (!enabled) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null) {
			return;
		}
		if (!isHoldingFishingRod(client.player.getMainHandItem(), client.player.getOffhandItem())) {
			lastPresent = false;
			return;
		}
		Gui gui = client.gui;
		if (gui == null) {
			lastPresent = false;
			return;
		}

		GuiTitleAccessor titles = (GuiTitleAccessor) gui;

		boolean present = containsTrigger(titles.primetooler$getTitle());

		// 1) Edge-triggered to avoid spamming when the symbol stays visible.
		if (present && !lastPresent) {
			requestUsePress(client);
		}
		lastPresent = present;
	}

	public static void tickClient() {
		if (usePressTicksRemaining <= 0) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.options == null) {
			usePressTicksRemaining = 0;
			pressedUseKey = null;
			return;
		}
		KeyMapping useKey = client.options.keyUse;
		if (useKey == null) {
			usePressTicksRemaining = 0;
			pressedUseKey = null;
			return;
		}

		// 1) Keep "use" down briefly so vanilla input handling triggers as if physically pressed.
		if (pressedUseKey != null) {
			KeyMapping.set(pressedUseKey, true);
		} else {
			useKey.setDown(true);
		}

		// 2) Release once we're done, but never fight a real right-click hold.
		usePressTicksRemaining--;
		if (usePressTicksRemaining <= 0) {
			releaseUseKey(client, useKey);
			usePressTicksRemaining = 0;
			pressedUseKey = null;
		}
	}

	private static boolean containsTrigger(Component component) {
		if (component == null) {
			return false;
		}
		String text = component.getString();
		if (text == null || text.isEmpty()) {
			return false;
		}
		for (int i = 0; i < TRIGGER_GLYPHS.length; i++) {
			if (text.indexOf(TRIGGER_GLYPHS[i]) >= 0) {
				return true;
			}
		}
		return false;
	}

	private static boolean isHoldingFishingRod(ItemStack mainHand, ItemStack offhand) {
		return isFishingRod(mainHand) || isFishingRod(offhand);
	}

	private static boolean isFishingRod(ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.is(Items.FISHING_ROD);
	}

	private static void requestUsePress(Minecraft client) {
		if (usePressTicksRemaining > 0) {
			return;
		}
		if (client == null || client.player == null || client.options == null || client.options.keyUse == null) {
			return;
		}
		// Skip if the user is already holding right click: we don't need to simulate anything.
		if (client.mouseHandler != null && client.mouseHandler.isRightPressed()) {
			return;
		}
		KeyMapping useKey = client.options.keyUse;
		if (useKey.isDown()) {
			return;
		}

		// 1) Trigger a real "click" for the currently bound key, so vanilla treats it like an actual press.
		InputConstants.Key bound = boundKey(useKey);
		if (bound != null) {
			KeyMapping.click(bound);
			KeyMapping.set(bound, true);
			pressedUseKey = bound;
		} else {
			useKey.setDown(true);
			pressedUseKey = null;
		}
		usePressTicksRemaining = USE_PRESS_TICKS;
	}

	private static void cancelUsePress(Minecraft client) {
		usePressTicksRemaining = 0;
		pressedUseKey = null;
		if (client == null || client.options == null || client.options.keyUse == null) {
			return;
		}
		if (client.mouseHandler != null && client.mouseHandler.isRightPressed()) {
			return;
		}
		releaseUseKey(client, client.options.keyUse);
	}

	private static void releaseUseKey(Minecraft client, KeyMapping useKey) {
		if (client != null && client.mouseHandler != null && client.mouseHandler.isRightPressed()) {
			return;
		}
		if (pressedUseKey != null) {
			KeyMapping.set(pressedUseKey, false);
			return;
		}
		if (useKey != null) {
			useKey.setDown(false);
		}
	}

	private static InputConstants.Key boundKey(KeyMapping mapping) {
		if (mapping == null) {
			return null;
		}
		if (mapping instanceof KeyMappingAccessor accessor) {
			return accessor.primetooler$getKey();
		}
		return null;
	}
}
