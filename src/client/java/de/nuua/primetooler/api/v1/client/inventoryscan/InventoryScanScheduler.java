package de.nuua.primetooler.api.v1.client.inventoryscan;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * WHY: Central scheduler for periodic inventory scans.
 * PERF: O(jobs) per tick with cheap due-check; scanning work runs at most every job interval.
 */
public final class InventoryScanScheduler {
	private static final Entry[] EMPTY = new Entry[0];
	private static volatile Entry[] entries = EMPTY;
	private static int globalTick;

	private InventoryScanScheduler() {
	}

	public static void register(InventoryScanJob job) {
		if (job == null) {
			return;
		}
		int interval = Math.max(1, job.intervalTicks());
		synchronized (InventoryScanScheduler.class) {
			Entry[] current = entries;
			int len = current.length;
			Entry[] next = new Entry[len + 1];
			System.arraycopy(current, 0, next, 0, len);
			next[len] = new Entry(job, interval);
			entries = next;
		}
	}

	public static void tick(Minecraft client) {
		// WHY: Use an internal monotonic counter instead of Player#tickCount so reconnect/respawn resets don't stall scans.
		// PERF: Single increment per tick; avoids long "catch-up" delays in multiplayer.
		int tick = ++globalTick;
		if (client == null) {
			return;
		}
		Player player = client.player;
		if (player == null) {
			return;
		}
		Inventory inventory = player.getInventory();
		if (inventory == null) {
			return;
		}

		Entry[] current = entries;
		for (int i = 0; i < current.length; i++) {
			Entry entry = current[i];
			if (entry == null) {
				continue;
			}
			int last = entry.lastScanTick;
			int diff = tick - last;
			if (last >= 0 && diff >= 0 && diff < entry.intervalTicks) {
				continue;
			}
			entry.lastScanTick = tick;
			entry.job.scan(client, inventory);
		}
	}

	private static final class Entry {
		private final InventoryScanJob job;
		private final int intervalTicks;
		private int lastScanTick = -1;

		private Entry(InventoryScanJob job, int intervalTicks) {
			this.job = job;
			this.intervalTicks = intervalTicks;
		}
	}
}
