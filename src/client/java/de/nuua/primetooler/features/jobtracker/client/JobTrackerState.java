package de.nuua.primetooler.features.jobtracker.client;

import de.nuua.primetooler.api.v1.client.actionbar.ActionbarOverlayReader;
import de.nuua.primetooler.api.v1.client.tracking.PerHourTracker;
import de.nuua.primetooler.core.Messages;
import de.nuua.primetooler.core.util.DotThousandsFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * WHY: Track job XP/hour and money/hour from actionbar ("current/next XP | current/next $").
 * PERF: Polled on a low interval; parses only the first value before '/' with manual scanning (no regex/streams).
 */
public final class JobTrackerState {
	private static final PerHourTracker XP_TRACKER = new PerHourTracker(PerHourTracker.DecreaseMode.WRAP);
	private static final PerHourTracker MONEY_TRACKER = new PerHourTracker(PerHourTracker.DecreaseMode.WRAP);

	private static final long HIDE_IDLE_NANOS = 60_000_000_000L;

	private static boolean xpEnabled;
	private static boolean moneyEnabled;

	private static boolean hasXp;
	private static boolean hasMoney;

	private static String xpText = "";
	private static String xpLastRateDisplay = "0";
	private static long xpLastMaxMilli;
	private static long xpLastValueMilli = -1L;
	private static long xpLastValueChangeNanos;

	private static String moneyText = "";
	private static String moneyLastRateDisplay = "0";
	private static long moneyLastMaxCenti;
	private static long moneyLastValueCenti = -1L;
	private static long moneyLastValueChangeNanos;

	private JobTrackerState() {
	}

	public static boolean isXpEnabled() {
		return xpEnabled;
	}

	public static boolean toggleXpEnabled() {
		setXpEnabled(!xpEnabled);
		return xpEnabled;
	}

	public static void resetXpTracker() {
		resetXp();
		if (!xpEnabled) {
			xpText = "";
			return;
		}
		xpText = Messages.applyColorCodes(Messages.getOrFallback(
			Messages.Id.JOB_XP_TRACKER_FORMAT,
			"&8| &aJob XP&7/Hour: &a%s",
			"0"
		));
	}

	public static void setXpEnabled(boolean value) {
		xpEnabled = value;
		resetXp();
		if (!anyEnabled()) {
			resetMoney();
		}
		if (!xpEnabled) {
			xpText = "";
			return;
		}
		xpText = Messages.applyColorCodes(Messages.getOrFallback(
			Messages.Id.JOB_XP_TRACKER_FORMAT,
			"&8| &aJob XP&7/Hour: &a%s",
			"0"
		));
	}

	public static boolean isMoneyEnabled() {
		return moneyEnabled;
	}

	public static boolean toggleMoneyEnabled() {
		setMoneyEnabled(!moneyEnabled);
		return moneyEnabled;
	}

	public static void resetMoneyTracker() {
		resetMoney();
		if (!moneyEnabled) {
			moneyText = "";
			return;
		}
		moneyText = Messages.applyColorCodes(Messages.getOrFallback(
			Messages.Id.JOB_MONEY_TRACKER_FORMAT,
			"&8| &6Job Money&7/Hour: &6%s",
			"0"
		));
	}

	public static void setMoneyEnabled(boolean value) {
		moneyEnabled = value;
		resetMoney();
		if (!anyEnabled()) {
			resetXp();
		}
		if (!moneyEnabled) {
			moneyText = "";
			return;
		}
		moneyText = Messages.applyColorCodes(Messages.getOrFallback(
			Messages.Id.JOB_MONEY_TRACKER_FORMAT,
			"&8| &6Job Money&7/Hour: &6%s",
			"0"
		));
	}

	public static boolean anyEnabled() {
		return xpEnabled || moneyEnabled;
	}

	public static boolean hasXp() {
		return hasXp;
	}

	public static boolean hasMoney() {
		return hasMoney;
	}

	public static boolean shouldRenderXpHud() {
		if (!xpEnabled || XP_TRACKER.perHourValue() <= 0L) {
			return false;
		}
		long lastChange = xpLastValueChangeNanos;
		if (lastChange <= 0L) {
			return false;
		}
		long now = System.nanoTime();
		return now > 0L && now - lastChange < HIDE_IDLE_NANOS;
	}

	public static boolean shouldRenderMoneyHud() {
		if (!moneyEnabled || MONEY_TRACKER.perHourValue() <= 0L) {
			return false;
		}
		long lastChange = moneyLastValueChangeNanos;
		if (lastChange <= 0L) {
			return false;
		}
		long now = System.nanoTime();
		return now > 0L && now - lastChange < HIDE_IDLE_NANOS;
	}

	public static String xpText() {
		return xpText;
	}

	public static String moneyText() {
		return moneyText;
	}

	public static void pollClient() {
		if (!anyEnabled()) {
			xpText = "";
			moneyText = "";
			return;
		}
		Minecraft client = Minecraft.getInstance();
		Component overlay = ActionbarOverlayReader.getOverlayMessage(client);
		String raw = overlay == null ? null : overlay.getString();
		if (raw == null || raw.isEmpty()) {
			hasXp = false;
			hasMoney = false;
			return;
		}

		long now = System.nanoTime();

		if (xpEnabled && xpLastValueChangeNanos > 0L && now - xpLastValueChangeNanos >= HIDE_IDLE_NANOS) {
			resetXpTracker();
		}
		if (moneyEnabled && moneyLastValueChangeNanos > 0L && now - moneyLastValueChangeNanos >= HIDE_IDLE_NANOS) {
			resetMoneyTracker();
		}

		if (xpEnabled) {
			int bar = raw.indexOf('|');
			int xpEnd = bar < 0 ? raw.length() : bar;
			int slash = lastIndexOf(raw, '/', 0, xpEnd);
			long xpMilli = slash < 0 ? -1L : parseFirstBeforeSlashFixed(raw, slash, 3);
			long maxMilli = slash < 0 ? -1L : parseFirstNumberFixed(raw, slash + 1, xpEnd, 3);
			long wrapAt = maxMilli > 0L ? maxMilli : xpLastMaxMilli;
			if (xpMilli >= 0L) {
				if (wrapAt > 0L && xpMilli > wrapAt) {
					// Invalid parse (current can't exceed max); ignore sample to avoid spikes.
					hasXp = false;
					return;
				}
				hasXp = true;
				long prev = xpLastValueMilli;
				if (prev != xpMilli) {
					xpLastValueMilli = xpMilli;
					xpLastValueChangeNanos = now;
				}
				long effectiveWrapAt = wrapAt;
				if (prev >= 0L && xpMilli < prev && !isLikelyWrap(prev, xpMilli, wrapAt, 10_000L)) {
					// Ignore small parse/formatting drops (e.g. losing decimal digits) to prevent fake "wrap" spikes.
					effectiveWrapAt = -1L;
				}
				boolean refresh = XP_TRACKER.tick(now, xpMilli, effectiveWrapAt, false);
				if (refresh) {
					xpLastRateDisplay = formatMilli(XP_TRACKER.perHourValue());
					String base = Messages.getOrFallback(
						Messages.Id.JOB_XP_TRACKER_FORMAT,
						"&8| &aJob XP&7/Hour: &a%s",
						xpLastRateDisplay
					);
					if (XP_TRACKER.isPaused()) {
						String paused = Messages.getOrFallback(Messages.Id.JOB_XP_TRACKER_PAUSED, "PAUSED");
						xpText = Messages.applyColorCodes(base + " &7(" + paused + ")");
					} else {
						xpText = Messages.applyColorCodes(base);
					}
				}
				if (maxMilli > 0L) {
					xpLastMaxMilli = maxMilli;
				}
			} else {
				hasXp = false;
			}
		} else {
			hasXp = false;
		}

		if (moneyEnabled) {
			int bar = raw.indexOf('|');
			int slash = bar < 0 ? -1 : raw.indexOf('/', bar);
			long moneyCenti = slash < 0 ? -1L : parseFirstBeforeSlashFixed(raw, slash, 2);
			long maxCenti = slash < 0 ? -1L : parseFirstNumberFixed(raw, slash + 1, raw.length(), 2);
			long wrapAt = maxCenti > 0L ? maxCenti : moneyLastMaxCenti;
			if (moneyCenti >= 0L) {
				if (wrapAt > 0L && moneyCenti > wrapAt) {
					hasMoney = false;
					return;
				}
				hasMoney = true;
				long prev = moneyLastValueCenti;
				if (prev != moneyCenti) {
					moneyLastValueCenti = moneyCenti;
					moneyLastValueChangeNanos = now;
				}
				long effectiveWrapAt = wrapAt;
				if (prev >= 0L && moneyCenti < prev && !isLikelyWrap(prev, moneyCenti, wrapAt, 100L)) {
					effectiveWrapAt = -1L;
				}
				boolean refresh = MONEY_TRACKER.tick(now, moneyCenti, effectiveWrapAt, false);
				if (refresh) {
					moneyLastRateDisplay = formatCenti(MONEY_TRACKER.perHourValue());
					String base = Messages.getOrFallback(
						Messages.Id.JOB_MONEY_TRACKER_FORMAT,
						"&8| &6Job Money&7/Hour: &6%s",
						moneyLastRateDisplay
					);
					if (MONEY_TRACKER.isPaused()) {
						String paused = Messages.getOrFallback(Messages.Id.JOB_MONEY_TRACKER_PAUSED, "PAUSED");
						moneyText = Messages.applyColorCodes(base + " &7(" + paused + ")");
					} else {
						moneyText = Messages.applyColorCodes(base);
					}
				}
				if (maxCenti > 0L) {
					moneyLastMaxCenti = maxCenti;
				}
			} else {
				hasMoney = false;
			}
		} else {
			hasMoney = false;
		}
	}

	private static void resetXp() {
		hasXp = false;
		XP_TRACKER.reset();
		xpLastRateDisplay = "0";
		xpLastMaxMilli = 0L;
		xpLastValueMilli = -1L;
		xpLastValueChangeNanos = 0L;
	}

	private static void resetMoney() {
		hasMoney = false;
		MONEY_TRACKER.reset();
		moneyLastRateDisplay = "0";
		moneyLastMaxCenti = 0L;
		moneyLastValueCenti = -1L;
		moneyLastValueChangeNanos = 0L;
	}

	private static long parseFirstBeforeSlashCenti(String text, int slashIndex) {
		if (text == null || slashIndex <= 0) {
			return -1L;
		}
		int start = slashIndex - 1;
		while (start >= 0) {
			char ch = text.charAt(start);
			if ((ch >= '0' && ch <= '9') || isSeparator(ch)) {
				start--;
				continue;
			}
			break;
		}
		start++;
		if (start >= slashIndex) {
			return -1L;
		}
		return parseDecimalFixed(text, start, slashIndex, 2);
	}

	private static long parseFirstBeforeSlashFixed(String text, int slashIndex, int maxFractionDigits) {
		if (text == null || slashIndex <= 0) {
			return -1L;
		}
		int start = slashIndex - 1;
		while (start >= 0) {
			char ch = text.charAt(start);
			if ((ch >= '0' && ch <= '9') || isSeparator(ch)) {
				start--;
				continue;
			}
			break;
		}
		start++;
		if (start >= slashIndex) {
			return -1L;
		}
		return parseDecimalFixed(text, start, slashIndex, maxFractionDigits);
	}

	private static long parseDecimalFixed(String text, int start, int endExclusive, int maxFractionDigits) {
		return parseDecimalFixedInternal(text, start, endExclusive, maxFractionDigits, false);
	}

	private static long parseFirstNumberFixed(String text, int start, int endExclusive, int maxFractionDigits) {
		if (text == null) {
			return -1L;
		}
		int len = text.length();
		int end = endExclusive;
		if (end > len) {
			end = len;
		}
		int begin = Math.max(0, start);
		if (begin >= end) {
			return -1L;
		}
		int firstDigit = -1;
		for (int i = begin; i < end; i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				firstDigit = i;
				break;
			}
		}
		if (firstDigit < 0) {
			return -1L;
		}
		return parseDecimalFixedInternal(text, firstDigit, end, maxFractionDigits, true);
	}

	private static long parseDecimalFixedInternal(String text, int start, int endExclusive, int maxFractionDigits,
		boolean stopAfterFirstNumber) {
		// Prefer EU-style formatting where '.' is thousands separator and ',' is decimal separator.
		boolean hasComma = false;
		for (int i = start; i < endExclusive; i++) {
			if (text.charAt(i) == ',') {
				hasComma = true;
				break;
			}
		}

		long whole = 0L;
		long frac = 0L;
		int fracDigits = 0;
		boolean any = false;
		boolean inFraction = false;

		for (int i = start; i < endExclusive; i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				int digit = ch - '0';
				any = true;
				if (inFraction) {
					if (fracDigits < maxFractionDigits) {
						frac = frac * 10L + digit;
						fracDigits++;
					}
				} else {
					if (whole > (Long.MAX_VALUE - digit) / 10L) {
						return -1L;
					}
					whole = whole * 10L + digit;
				}
				continue;
			}
			if (ch == '.' || ch == ',') {
				if (!inFraction) {
					if (ch == ',') {
						int digitsAfter = countDigitsAfter(text, i + 1, endExclusive);
						if (digitsAfter > 0 && digitsAfter <= maxFractionDigits) {
							inFraction = true;
							continue;
						}
					} else if (!hasComma) {
						int digitsAfter = countDigitsAfter(text, i + 1, endExclusive);
						int dotMax = Math.min(2, maxFractionDigits);
						if (digitsAfter > 0 && digitsAfter <= dotMax) {
							inFraction = true;
							continue;
						}
					}
				}
				continue;
			}
			if (isSeparator(ch)) {
				continue;
			}
			if (stopAfterFirstNumber && any) {
				break;
			}
		}

		if (!any) {
			return -1L;
		}
		long scale = 1L;
		for (int i = 0; i < maxFractionDigits; i++) {
			if (scale > Long.MAX_VALUE / 10L) {
				return -1L;
			}
			scale *= 10L;
		}
		if (whole > Long.MAX_VALUE / scale) {
			return -1L;
		}
		long out = whole * scale;
		if (fracDigits > 0) {
			long mul = 1L;
			for (int i = fracDigits; i < maxFractionDigits; i++) {
				mul *= 10L;
			}
			if (frac > Long.MAX_VALUE / mul) {
				return -1L;
			}
			out += frac * mul;
		}
		return out;
	}

	private static boolean isLikelyWrap(long previous, long current, long wrapAt, long minTolerance) {
		if (previous < 0L || current < 0L || wrapAt <= 0L) {
			return false;
		}
		if (previous > wrapAt || current > wrapAt) {
			return false;
		}
		long tol = wrapAt / 20L; // 5%
		if (tol < minTolerance) {
			tol = minTolerance;
		}
		return previous >= wrapAt - tol && current <= tol;
	}

	private static int countDigitsAfter(String text, int start, int endExclusive) {
		int len = text.length();
		int end = endExclusive;
		if (end > len) {
			end = len;
		}
		int count = 0;
		for (int i = start; i < end; i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				count++;
				continue;
			}
			if (isSeparator(ch)) {
				continue;
			}
			break;
		}
		return count;
	}

	private static boolean isSeparator(char ch) {
		return ch == '.'
			|| ch == ','
			|| ch == '\''
			|| ch == '_';
	}

	private static int lastIndexOf(String text, char needle, int startInclusive, int endExclusive) {
		if (text == null) {
			return -1;
		}
		int start = Math.max(0, startInclusive);
		int end = Math.min(text.length(), endExclusive);
		for (int i = end - 1; i >= start; i--) {
			if (text.charAt(i) == needle) {
				return i;
			}
		}
		return -1;
	}

	private static int indexOfIgnoreCase(String text, String needle) {
		if (text == null || needle == null) {
			return -1;
		}
		int tLen = text.length();
		int nLen = needle.length();
		if (nLen == 0) {
			return 0;
		}
		if (nLen > tLen) {
			return -1;
		}
		for (int i = 0; i <= tLen - nLen; i++) {
			if (regionEqualsIgnoreCase(text, i, needle)) {
				return i;
			}
		}
		return -1;
	}

	private static boolean regionEqualsIgnoreCase(String text, int offset, String needle) {
		for (int i = 0; i < needle.length(); i++) {
			char a = text.charAt(offset + i);
			char b = needle.charAt(i);
			if (a == b) {
				continue;
			}
			if (Character.toLowerCase(a) != Character.toLowerCase(b)) {
				return false;
			}
		}
		return true;
	}

	private static String formatCenti(long centiPerHour) {
		long rounded = centiPerHour;
		if (rounded <= 0L) {
			return "0";
		}
		long whole = rounded / 100L;
		long frac = rounded - whole * 100L;
		String wholeStr = DotThousandsFormatter.format(whole);
		if (frac <= 0L) {
			return wholeStr;
		}
		if (frac < 10L) {
			return wholeStr + ",0" + frac;
		}
		return wholeStr + "," + frac;
	}

	private static String formatMilli(long milliPerHour) {
		long rounded = milliPerHour;
		if (rounded <= 0L) {
			return "0";
		}
		long whole = rounded / 1000L;
		long frac = rounded - whole * 1000L;
		String wholeStr = DotThousandsFormatter.format(whole);
		if (frac <= 0L) {
			return wholeStr;
		}
		// Trim trailing zeros.
		if ((frac % 10L) == 0L) {
			frac /= 10L;
			if ((frac % 10L) == 0L) {
				frac /= 10L;
				return wholeStr + "," + frac;
			}
			if (frac < 10L) {
				return wholeStr + ",0" + frac;
			}
			return wholeStr + "," + frac;
		}
		if (frac < 10L) {
			return wholeStr + ",00" + frac;
		}
		if (frac < 100L) {
			return wholeStr + ",0" + frac;
		}
		return wholeStr + "," + frac;
	}
}
