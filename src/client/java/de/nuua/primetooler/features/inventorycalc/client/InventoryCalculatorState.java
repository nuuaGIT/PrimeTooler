package de.nuua.primetooler.features.inventorycalc.client;

/**
 * WHY: Session toggle for the inventory calculator field.
 * PERF: Single boolean lookup.
 */
public final class InventoryCalculatorState {
	private static boolean enabled = true;

	private InventoryCalculatorState() {
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
