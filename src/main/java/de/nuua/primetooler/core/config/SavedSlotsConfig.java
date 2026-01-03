package de.nuua.primetooler.core.config;

import java.util.ArrayList;
import java.util.List;

/**
 * WHY: Persist client-only saved inventory slots across servers/sessions.
 * PERF: Small list, linear scans only on slot toggles.
 */
public final class SavedSlotsConfig {
	public List<Integer> slots = new ArrayList<>();
}
