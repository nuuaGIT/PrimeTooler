package de.nuua.primetooler.features.durabilityguard.client;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * WHY: Central toggle + threshold for client-side durability protection.
 * PERF: O(1) checks, no allocations.
 */
public final class DurabilityGuardState {
	private static final int MIN_REMAINING = 10;
	private static final int ARMOR_LOW_PERCENT = 10;
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

	public static boolean shouldWarnArmor(Player player) {
		if (!enabled || player == null) {
			return false;
		}
		if (isArmorLow(player.getItemBySlot(EquipmentSlot.HEAD))) {
			return true;
		}
		if (isArmorLow(player.getItemBySlot(EquipmentSlot.CHEST))) {
			return true;
		}
		if (isArmorLow(player.getItemBySlot(EquipmentSlot.LEGS))) {
			return true;
		}
		if (isArmorLow(player.getItemBySlot(EquipmentSlot.FEET))) {
			return true;
		}
		return false;
	}

	private static boolean isArmorLow(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		if (!stack.isDamageableItem()) {
			return false;
		}
		int max = stack.getMaxDamage();
		if (max <= 0) {
			return false;
		}
		int remaining = max - stack.getDamageValue();
		return remaining * 100 < max * ARMOR_LOW_PERCENT;
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
