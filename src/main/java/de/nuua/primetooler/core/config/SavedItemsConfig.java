package de.nuua.primetooler.core.config;

import java.util.ArrayList;
import java.util.List;

/**
 * WHY: Client-side persistence for saved items (MP-safe, no server tags).
 * PERF: Small lists, linear scans only on user actions.
 */
public final class SavedItemsConfig {
	public List<SavedItemEntry> entries = new ArrayList<>();

	public static final class SavedItemEntry {
		public String id;
		public String itemId;
		public String strongSignature;
		public String weakSignature;
		public String portableSignature;
		public int lastSlot = -1;
		public boolean hasSlot;
	}
}
