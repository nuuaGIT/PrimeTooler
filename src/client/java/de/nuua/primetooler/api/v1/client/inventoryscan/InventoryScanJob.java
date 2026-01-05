package de.nuua.primetooler.api.v1.client.inventoryscan;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;

/**
 * WHY: Reusable contract for periodic inventory scans (e.g. totals, counters, derived HUD data).
 * PERF: Implementations should avoid allocations and use fast parsing; called at most every interval.
 */
public interface InventoryScanJob {
	/**
	 * @return scan interval in client ticks (20 ticks = 1 second)
	 */
	int intervalTicks();

	void scan(Minecraft client, Inventory inventory);
}

