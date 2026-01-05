package de.nuua.primetooler.core.util;

/**
 * WHY: Render integer values with '.' thousands separators (e.g. 293434 -> 293.434) without abbreviations.
 * PERF: Allocation is bounded; intended for low-frequency UI text formatting.
 */
public final class DotThousandsFormatter {
	private DotThousandsFormatter() {
	}

	public static String format(long value) {
		if (value == 0L) {
			return "0";
		}

		// Handle Long.MIN_VALUE safely by falling back to string-based insertion.
		if (value == Long.MIN_VALUE) {
			return insertDots(Long.toString(value));
		}

		boolean negative = value < 0L;
		long abs = negative ? -value : value;

		int digits = countDigits(abs);
		int groups = (digits - 1) / 3;
		int len = digits + groups + (negative ? 1 : 0);
		char[] out = new char[len];

		int write = len - 1;
		int groupCount = 0;
		while (abs != 0L) {
			if (groupCount == 3) {
				out[write--] = '.';
				groupCount = 0;
			}
			long q = abs / 10L;
			int digit = (int) (abs - q * 10L);
			out[write--] = (char) ('0' + digit);
			abs = q;
			groupCount++;
		}
		if (negative) {
			out[0] = '-';
		}
		return new String(out);
	}

	private static int countDigits(long value) {
		int count = 0;
		long v = value;
		while (v != 0L) {
			v /= 10L;
			count++;
		}
		return count;
	}

	private static String insertDots(String raw) {
		if (raw == null || raw.isEmpty()) {
			return "0";
		}
		int start = raw.charAt(0) == '-' ? 1 : 0;
		int digits = raw.length() - start;
		if (digits <= 3) {
			return raw;
		}
		int groups = (digits - 1) / 3;
		int len = raw.length() + groups;
		char[] out = new char[len];
		int write = len - 1;
		int groupCount = 0;
		for (int i = raw.length() - 1; i >= start; i--) {
			if (groupCount == 3) {
				out[write--] = '.';
				groupCount = 0;
			}
			out[write--] = raw.charAt(i);
			groupCount++;
		}
		if (start == 1) {
			out[0] = '-';
		}
		return new String(out);
	}
}

