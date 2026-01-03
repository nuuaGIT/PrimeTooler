package de.nuua.primetooler.features.doubledrop.client;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

/**
 * WARUM: Schuetzt vor ungewollten Drops durch eine doppelte Bestaetigung.
 * PERF: Nur bei Drop-Aktionen aktiv, keine Tick-Arbeit.
 */
public final class DoubleDropState {
	public enum Mode {
		OFF,
		RARE,
		ALL
	}

	private static final long CONFIRM_WINDOW_MS = 1500L;
	private static final int REQUIRED_PRESSES = 3;
	private static final float MIN_PITCH = 0.7f;
	private static final float MAX_PITCH = 1.0f;
	private static Mode mode = Mode.OFF;
	private static long lastDropTimeMs;
	private static Item lastDropItem;
	private static int lastDropHash;
	private static int lastDropCount;

	private DoubleDropState() {
	}

	public static Mode getMode() {
		return mode;
	}

	public static void setMode(Mode value) {
		mode = value == null ? Mode.OFF : value;
	}

	public static Mode cycleMode() {
		mode = switch (mode) {
			case OFF -> Mode.RARE;
			case RARE -> Mode.ALL;
			case ALL -> Mode.OFF;
		};
		return mode;
	}

	public static int toConfigValue(Mode value) {
		if (value == null) {
			return 0;
		}
		return switch (value) {
			case OFF -> 0;
			case RARE -> 1;
			case ALL -> 2;
		};
	}

	public static Mode fromConfigValue(int value) {
		return switch (value) {
			case 1 -> Mode.RARE;
			case 2 -> Mode.ALL;
			default -> Mode.OFF;
		};
	}

	public static boolean allowDrop(ItemStack stack) {
		if (!needsConfirm(stack)) {
			return true;
		}
		long now = System.currentTimeMillis();
		Item item = stack.getItem();
		int hash = stackSignature(stack);
		if (item == lastDropItem && hash == lastDropHash && now - lastDropTimeMs <= CONFIRM_WINDOW_MS) {
			lastDropCount++;
		} else {
			lastDropItem = item;
			lastDropHash = hash;
			lastDropCount = 1;
		}
		lastDropTimeMs = now;
		if (lastDropCount >= REQUIRED_PRESSES) {
			lastDropTimeMs = 0L;
			lastDropCount = 0;
			lastDropItem = null;
			lastDropHash = 0;
			return true;
		}
		return false;
	}

	public static boolean isFirstAttempt() {
		return lastDropCount == 1;
	}

	public static int remainingPresses() {
		int remaining = REQUIRED_PRESSES - lastDropCount;
		return Math.max(0, remaining);
	}

	public static float blockedPitch() {
		if (lastDropCount <= 1) {
			return MIN_PITCH;
		}
		float step = (MAX_PITCH - MIN_PITCH) / (REQUIRED_PRESSES - 1);
		float pitch = MIN_PITCH + step * (lastDropCount - 1);
		return Math.min(MAX_PITCH, pitch);
	}

	private static boolean needsConfirm(ItemStack stack) {
		if (mode == Mode.OFF) {
			return false;
		}
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		if (mode == Mode.ALL) {
			return true;
		}
		return isRareStack(stack);
	}

	private static boolean isRareStack(ItemStack stack) {
		if (stack.get(DataComponents.CUSTOM_NAME) != null) {
			return true;
		}
		ItemLore lore = stack.get(DataComponents.LORE);
		return lore != null && !lore.lines().isEmpty();
	}

	private static int stackSignature(ItemStack stack) {
		int result = System.identityHashCode(stack.getItem());
		Component name = stack.get(DataComponents.CUSTOM_NAME);
		if (name != null) {
			result = 31 * result + name.getString().hashCode();
		}
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore != null && !lore.lines().isEmpty()) {
			result = 31 * result + lore.lines().size();
			Component line = lore.lines().get(0);
			if (line != null) {
				result = 31 * result + line.getString().hashCode();
			}
		}
		return result;
	}
}
