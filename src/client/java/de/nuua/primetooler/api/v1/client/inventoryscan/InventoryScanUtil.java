package de.nuua.primetooler.api.v1.client.inventoryscan;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

/**
 * WHY: Small helpers for inventory scan jobs.
 * PERF: Callers control when to allocate; helpers are branchy and avoid regex.
 */
public final class InventoryScanUtil {
	private InventoryScanUtil() {
	}

	public static String hoverName(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return "";
		}
		Component name = stack.getHoverName();
		return name == null ? "" : name.getString();
	}

	public static String loreLine(ItemStack stack, int lineIndex) {
		if (stack == null || stack.isEmpty() || lineIndex < 0) {
			return "";
		}
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) {
			return "";
		}
		java.util.List<Component> lines = lore.lines();
		if (lines == null || lineIndex >= lines.size()) {
			return "";
		}
		Component line = lines.get(lineIndex);
		return line == null ? "" : line.getString();
	}
}

