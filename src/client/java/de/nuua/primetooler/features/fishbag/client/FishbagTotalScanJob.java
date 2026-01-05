package de.nuua.primetooler.features.fishbag.client;

import de.nuua.primetooler.api.v1.client.inventoryscan.InventoryScanJob;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;

/**
 * WHY: Keep Fishbag totals updated by periodic inventory scans.
 * PERF: Runs every 0.5 seconds (10 ticks) and only scans the player inventory.
 */
public final class FishbagTotalScanJob implements InventoryScanJob {
	@Override
	public int intervalTicks() {
		return 10;
	}

	@Override
	public void scan(Minecraft client, Inventory inventory) {
		FishbagTotalState.scan(client, inventory);
	}
}
