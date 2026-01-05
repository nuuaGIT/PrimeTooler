package de.nuua.primetooler.api.v1.client.scoreboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.Scoreboard;

/**
 * WHY: Provide a stable client API to read the current sidebar scoreboard lines.
 * PERF: Manual top-N selection (max 15 lines), no streams and no per-tick allocations required by callers.
 */
public final class SidebarScoreboardReader {
	public static final int MAX_LINES = 15;

	private SidebarScoreboardReader() {
	}

	/**
	 * Fills {@code out} with the sidebar lines from top to bottom.
	 *
	 * @return number of filled lines (0 if no sidebar scoreboard is visible)
	 */
	public static int fillSidebarLines(Minecraft client, Component[] out) {
		if (client == null || out == null || out.length == 0) {
			return 0;
		}
		int limit = out.length;
		if (limit > MAX_LINES) {
			limit = MAX_LINES;
		}
		ClientLevel level = client.level;
		if (level == null) {
			clear(out);
			return 0;
		}
		Scoreboard scoreboard = level.getScoreboard();
		if (scoreboard == null) {
			clear(out);
			return 0;
		}
		Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (objective == null) {
			clear(out);
			return 0;
		}

		java.util.Collection<PlayerScoreEntry> entries = scoreboard.listPlayerScores(objective);
		if (entries == null || entries.isEmpty()) {
			clear(out);
			return 0;
		}

		PlayerScoreEntry[] best = new PlayerScoreEntry[limit];
		int count = 0;
		for (PlayerScoreEntry entry : entries) {
			if (entry == null) {
				continue;
			}
			int insert = 0;
			while (insert < count && !comesBefore(entry, best[insert])) {
				insert++;
			}
			if (insert >= limit) {
				continue;
			}
			if (count < limit) {
				count++;
			}
			for (int j = count - 1; j > insert; j--) {
				best[j] = best[j - 1];
			}
			best[insert] = entry;
		}

		for (int i = 0; i < count; i++) {
			PlayerScoreEntry entry = best[i];
			out[i] = entry == null ? null : entry.ownerName();
		}
		for (int i = count; i < out.length; i++) {
			out[i] = null;
		}
		return count;
	}

	private static void clear(Component[] out) {
		for (int i = 0; i < out.length; i++) {
			out[i] = null;
		}
	}

	private static boolean comesBefore(PlayerScoreEntry a, PlayerScoreEntry b) {
		if (b == null) {
			return true;
		}
		int va = a.value();
		int vb = b.value();
		if (va != vb) {
			return va > vb;
		}
		String oa = a.owner();
		String ob = b.owner();
		if (oa == null) {
			oa = "";
		}
		if (ob == null) {
			ob = "";
		}
		return oa.compareTo(ob) < 0;
	}
}

