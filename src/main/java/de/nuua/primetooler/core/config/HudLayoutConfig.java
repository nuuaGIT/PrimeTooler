package de.nuua.primetooler.core.config;

/**
 * WHY: Persist user-defined HUD element positions across sessions.
 * PERF: Plain data holder, no logic.
 */
public final class HudLayoutConfig {
	public java.util.List<Entry> entries;

	public HudLayoutConfig() {
	}

	public HudLayoutConfig(java.util.List<Entry> entries) {
		this.entries = entries;
	}

	public static final class Entry {
		public String id;
		public float x;
		public float y;

		public Entry() {
		}

		public Entry(String id, float x, float y) {
			this.id = id;
			this.x = x;
			this.y = y;
		}
	}
}

