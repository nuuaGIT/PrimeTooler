package de.nuua.primetooler.core.config;

import java.util.ArrayList;
import java.util.List;

/**
 * WHY: Persist container cache across sessions for faster client-only saved-item sync.
 * PERF: Small list, linear scans only on container updates.
 */
public final class ContainerCacheConfig {
	public List<ContainerCacheEntry> entries = new ArrayList<>();

	public static final class ContainerCacheEntry {
		public String itemId;
		public String signature;
		public String savedId;
		public long lastSeenMs;
	}
}
