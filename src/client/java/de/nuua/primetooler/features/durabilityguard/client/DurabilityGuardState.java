package de.nuua.primetooler.features.durabilityguard.client;

import net.minecraft.world.item.ItemStack;

/**
 * WHY: Central toggle + threshold for client-side durability protection.
 * PERF: O(1) checks, no allocations.
 */
public final class DurabilityGuardState {
	private static final int MIN_REMAINING = 10;
	private static boolean enabled;

	private DurabilityGuardState() {
	}

	public static boolean shouldBlock(ItemStack stack) {
		if (!enabled || stack == null || stack.isEmpty()) {
			return false;
		}
		if (!stack.isDamageableItem()) {
			return false;
		}
		int remaining = stack.getMaxDamage() - stack.getDamageValue();
		return remaining < MIN_REMAINING;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static boolean toggleEnabled() {
		enabled = !enabled;
		return enabled;
	}

	public static void setEnabled(boolean value) {
		enabled = value;
	}
}
