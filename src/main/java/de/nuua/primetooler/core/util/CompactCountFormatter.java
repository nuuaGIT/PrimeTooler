package de.nuua.primetooler.core.util;

/**
 * WHY: Format large integer counts in a compact human-readable form (k/m/b).
 * PERF: O(1) integer math, no allocations except the returned String.
 */
public final class CompactCountFormatter {
	private CompactCountFormatter() {
	}

	public static String format(long value) {
		if (value < 0L) {
			return "0";
		}
		if (value < 1000L) {
			return Long.toString(value);
		}
		if (value < 1_000_000L) {
			return formatScaled(value, 1000L, 'k');
		}
		if (value < 1_000_000_000L) {
			return formatScaled(value, 1_000_000L, 'm');
		}
		return formatScaled(value, 1_000_000_000L, 'b');
	}

	/**
	 * Formats a value in thousands (k) and never switches to m/b. For values >= 1,000k, it uses '.' thousands grouping
	 * (e.g., 2.384k) to stay readable.
	 */
	public static String formatKMax(long value) {
		if (value < 0L) {
			return "0";
		}
		if (value < 1000L) {
			return Long.toString(value);
		}

		long wholeK = value / 1000L;
		long rem = value - wholeK * 1000L;

		// Keep one decimal for small k values; for large values, keep it as grouped integer.
		if (wholeK < 1000L) {
			int tenth = (int) ((rem * 10L + 500L) / 1000L);
			if (tenth >= 10) {
				wholeK++;
				tenth = 0;
			}
			if (tenth == 0) {
				return wholeK + "k";
			}
			return wholeK + "." + tenth + "k";
		}

		return formatGrouped(wholeK) + "k";
	}

	private static String formatScaled(long value, long base, char suffix) {
		long whole = value / base;
		long rem = value - whole * base;
		int tenth = (int) ((rem * 10L + base / 2L) / base);
		if (tenth >= 10) {
			whole++;
			tenth = 0;
		}
		if (tenth == 0) {
			return whole + String.valueOf(suffix);
		}
		return whole + "." + tenth + suffix;
	}

	private static String formatGrouped(long value) {
		if (value < 0L) {
			value = 0L;
		}
		if (value < 1000L) {
			return Long.toString(value);
		}
		String s = Long.toString(value);
		int len = s.length();
		int seps = (len - 1) / 3;
		StringBuilder out = new StringBuilder(len + seps);
		int firstGroup = len % 3;
		if (firstGroup == 0) {
			firstGroup = 3;
		}
		out.append(s, 0, firstGroup);
		for (int i = firstGroup; i < len; i += 3) {
			out.append('.');
			out.append(s, i, i + 3);
		}
		return out.toString();
	}
}
