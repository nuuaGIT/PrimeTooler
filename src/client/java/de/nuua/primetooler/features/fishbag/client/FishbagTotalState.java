package de.nuua.primetooler.features.fishbag.client;

import de.nuua.primetooler.core.Messages;
import de.nuua.primetooler.core.util.CompactCountFormatter;
import de.nuua.primetooler.core.util.DotThousandsFormatter;
import de.nuua.primetooler.api.v1.client.tracking.PerHourTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

/**
 * WHY: Provide live fishbag totals by scanning the client inventory periodically.
 * PERF: Called by InventoryScanScheduler (every 0.5 seconds by default).
 */
public final class FishbagTotalState {
	private static final long HIDE_IDLE_NANOS = 60_000_000_000L;

	private static boolean totalEnabled;
	private static boolean weightEnabled;
	private static boolean coinsEnabled;
	private static boolean moneyTrackerEnabled;

	private static long currentTotal;
	private static long maxTotal;
	private static long totalWeight;
	private static long totalCoins;
	private static boolean hasBags;

	private static long lastTotalDisplay;
	private static long lastMaxDisplay;
	private static long totalLastChangeNanos;
	private static long weightLastChangeNanos;
	private static long coinsLastChangeNanos;
	private static long moneyLastChangeNanos;

	private static long lastWeight;
	private static long lastCoins;
	private static long deltaWeight;
	private static long deltaCoins;
	private static long deltaWeightUntilNanos;
	private static long deltaCoinsUntilNanos;
	private static boolean haveLastTotals;

	private static long moneyTrackerObservedFish;
	private static final PerHourTracker MONEY_TRACKER = new PerHourTracker(PerHourTracker.DecreaseMode.RESTART);
	private static String moneyTrackerLastRateDisplay = "0";
	private static String moneyTrackerText = "";

	private static String totalText = "";
	private static String weightText = "";
	private static String coinsText = "";

	private FishbagTotalState() {
	}

	public static boolean isTotalEnabled() {
		return totalEnabled;
	}

	public static boolean toggleTotalEnabled() {
		totalEnabled = !totalEnabled;
		return totalEnabled;
	}

	public static void setTotalEnabled(boolean value) {
		totalEnabled = value;
	}

	public static boolean isWeightEnabled() {
		return weightEnabled;
	}

	public static boolean toggleWeightEnabled() {
		weightEnabled = !weightEnabled;
		return weightEnabled;
	}

	public static void setWeightEnabled(boolean value) {
		weightEnabled = value;
	}

	public static boolean isCoinsEnabled() {
		return coinsEnabled;
	}

	public static boolean toggleCoinsEnabled() {
		coinsEnabled = !coinsEnabled;
		return coinsEnabled;
	}

	public static void setCoinsEnabled(boolean value) {
		coinsEnabled = value;
	}

	public static boolean isMoneyTrackerEnabled() {
		return moneyTrackerEnabled;
	}

	public static boolean toggleMoneyTrackerEnabled() {
		moneyTrackerEnabled = !moneyTrackerEnabled;
		if (!moneyTrackerEnabled) {
			resetMoneyTracker();
		}
		return moneyTrackerEnabled;
	}

	public static void setMoneyTrackerEnabled(boolean value) {
		moneyTrackerEnabled = value;
		if (!moneyTrackerEnabled) {
			resetMoneyTracker();
		}
	}

	public static boolean hasBags() {
		return hasBags;
	}

	public static String totalText() {
		return totalText;
	}

	public static String weightText() {
		return weightText;
	}

	public static String coinsText() {
		return coinsText;
	}

	public static String moneyTrackerText() {
		return moneyTrackerText;
	}

	public static void resetMoneyTrackerForCommand() {
		resetMoneyTracker();
	}

	public static boolean shouldRenderTotalHud() {
		if (!totalEnabled || !hasBags) {
			return false;
		}
		return shouldRenderByLastChange(totalLastChangeNanos);
	}

	public static boolean shouldRenderWeightHud() {
		if (!weightEnabled || !hasBags) {
			return false;
		}
		return shouldRenderByLastChange(weightLastChangeNanos);
	}

	public static boolean shouldRenderCoinsHud() {
		if (!coinsEnabled || !hasBags) {
			return false;
		}
		return shouldRenderByLastChange(coinsLastChangeNanos);
	}

	public static boolean shouldRenderMoneyTrackerHud() {
		if (!moneyTrackerEnabled || !hasBags) {
			return false;
		}
		return shouldRenderByLastChange(moneyLastChangeNanos);
	}

	static void scan(Minecraft client, Inventory inv) {
		if (!totalEnabled && !weightEnabled && !coinsEnabled && !moneyTrackerEnabled) {
			reset();
			return;
		}
		if (client == null || inv == null) {
			reset();
			return;
		}
		if (client.player == null) {
			reset();
			return;
		}

		long now = System.nanoTime();

		long previousTotal = currentTotal;
		long previousMax = maxTotal;
		long previousWeight = totalWeight;
		long previousCoins = totalCoins;
		boolean hadBags = hasBags;

		long current = 0L;
		long max = 0L;
		long weight = 0L;
		long coins = 0L;
		boolean found = false;
		boolean needCoins = coinsEnabled || moneyTrackerEnabled;
		int size = inv.getContainerSize();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inv.getItem(i);
			if (stack == null || stack.isEmpty()) {
				continue;
			}
			if (!stack.is(Items.GOLDEN_HORSE_ARMOR)) {
				continue;
			}
			ItemLore lore = stack.get(DataComponents.LORE);
			if (lore == null) {
				continue;
			}
			java.util.List<Component> lines = lore.lines();
			if (lines == null || lines.isEmpty()) {
				continue;
			}

			boolean isFishbag = false;
			long bagCurrent = 0L;
			long bagMax = 0L;
			long bagWeight = -1L;
			long bagCoins = -1L;
			long bagCoinsFallback = -1L;

			for (int li = 0; li < lines.size(); li++) {
				Component line = lines.get(li);
				if (line == null) {
					continue;
				}
				String raw = line.getString();
				if (raw == null || raw.isEmpty()) {
					continue;
				}

				// 1) Detect fish count "A/B" anywhere in lore (servers sometimes reorder lines).
				if (!isFishbag) {
					long parsedCurrent = parseFirstNumber(raw, 0);
					int slash = indexOfSlashAfterNumber(raw, 0);
					if (parsedCurrent >= 0L && slash >= 0) {
						long parsedMax = parseFirstNumber(raw, slash + 1);
						if (parsedMax >= 0L) {
							isFishbag = true;
							bagCurrent = parsedCurrent;
							bagMax = parsedMax;
							continue;
						}
					}
				}

				// 2) Detect weight by unit ("g"/"kg") anywhere in lore.
				if (weightEnabled && bagWeight < 0L && containsWeightUnit(raw)) {
					bagWeight = parseWeightGrams(raw);
					continue;
				}

				// 3) Detect coins by keyword ("coin(s)") or fallback to the first remaining numeric line.
				if (needCoins && bagCoins < 0L && containsCoinKeyword(raw)) {
					bagCoins = parseFirstNumber(raw, 0);
					continue;
				}
				if (needCoins && bagCoinsFallback < 0L && containsDigit(raw)
					&& raw.indexOf('/') < 0 && !containsWeightUnit(raw)) {
					bagCoinsFallback = parseFirstNumber(raw, 0);
				}
			}

			if (!isFishbag) {
				continue;
			}
			found = true;
			if (totalEnabled) {
				current += bagCurrent;
				max += bagMax;
			}
			if (weightEnabled && bagWeight >= 0L) {
				weight += bagWeight;
			}
			if (needCoins) {
				long coinValue = bagCoins >= 0L ? bagCoins : bagCoinsFallback;
				if (coinValue >= 0L) {
					coins += coinValue;
				}
			}
		}

		currentTotal = current;
		maxTotal = max;
		totalWeight = weight;
		totalCoins = coins;
		hasBags = found;

		updateHudChangeTimes(now, hadBags, previousTotal, previousMax, previousWeight, previousCoins);
		updateDeltas(now);
		updateMoneyTracker(now);

		totalText = Messages.applyColorCodes(Messages.get(Messages.Id.FISHBAG_TOTAL_FORMAT, currentTotal, maxTotal));
		String weightDisplay = formatWeightDisplay(totalWeight);
		String weightDelta = buildWeightDelta(now);
		weightText = Messages.applyColorCodes(Messages.get(Messages.Id.FISHBAG_WEIGHT_FORMAT, weightDisplay, weightDelta));
		coinsText = Messages.applyColorCodes(Messages.get(Messages.Id.FISHBAG_COINS_FORMAT,
			CompactCountFormatter.format(totalCoins),
			buildCoinsDelta(now)));

		lastWeight = totalWeight;
		lastCoins = totalCoins;
		haveLastTotals = true;
	}

	private static void reset() {
		currentTotal = 0L;
		maxTotal = 0L;
		totalWeight = 0L;
		totalCoins = 0L;
		hasBags = false;
		lastTotalDisplay = 0L;
		lastMaxDisplay = 0L;
		totalLastChangeNanos = 0L;
		weightLastChangeNanos = 0L;
		coinsLastChangeNanos = 0L;
		moneyLastChangeNanos = 0L;
		lastWeight = 0L;
		lastCoins = 0L;
		deltaWeight = 0L;
		deltaCoins = 0L;
		deltaWeightUntilNanos = 0L;
		deltaCoinsUntilNanos = 0L;
		haveLastTotals = false;
		resetMoneyTracker();
		totalText = Messages.applyColorCodes(Messages.get(Messages.Id.FISHBAG_TOTAL_FORMAT, 0, 0));
		weightText = Messages.applyColorCodes(Messages.get(Messages.Id.FISHBAG_WEIGHT_FORMAT, "0g", ""));
		coinsText = Messages.applyColorCodes(Messages.get(Messages.Id.FISHBAG_COINS_FORMAT, "0", ""));
	}

	private static void resetMoneyTracker() {
		moneyTrackerObservedFish = 0L;
		MONEY_TRACKER.reset();
		moneyTrackerLastRateDisplay = "0";
		moneyLastChangeNanos = 0L;
		if (moneyTrackerEnabled) {
			moneyTrackerText = Messages.applyColorCodes(Messages.getOrFallback(
				Messages.Id.FISH_MONEY_TRACKER_FORMAT,
				"&8| &eCoins&7/Hour: &e%s",
				moneyTrackerLastRateDisplay
			));
		} else {
			moneyTrackerText = "";
		}
	}

	private static void updateDeltas(long nowNanos) {
		if (!haveLastTotals) {
			deltaWeight = 0L;
			deltaCoins = 0L;
			deltaWeightUntilNanos = 0L;
			deltaCoinsUntilNanos = 0L;
			return;
		}

		if (totalWeight > lastWeight) {
			deltaWeight = totalWeight - lastWeight;
			deltaWeightUntilNanos = nowNanos + 3_000_000_000L;
		} else if (totalWeight < lastWeight) {
			deltaWeight = 0L;
			deltaWeightUntilNanos = 0L;
		}

		if (totalCoins > lastCoins) {
			deltaCoins = totalCoins - lastCoins;
			deltaCoinsUntilNanos = nowNanos + 3_000_000_000L;
		} else if (totalCoins < lastCoins) {
			deltaCoins = 0L;
			deltaCoinsUntilNanos = 0L;
		}
	}

	private static void updateMoneyTracker(long nowNanos) {
		if (!moneyTrackerEnabled) {
			moneyTrackerText = "";
			return;
		}
		if (!hasBags) {
			resetMoneyTracker();
			return;
		}
		if (moneyLastChangeNanos > 0L && nowNanos - moneyLastChangeNanos >= HIDE_IDLE_NANOS) {
			resetMoneyTracker();
			return;
		}
		boolean fishActivity = currentTotal > moneyTrackerObservedFish;
		moneyTrackerObservedFish = currentTotal;
		if (fishActivity) {
			moneyLastChangeNanos = nowNanos;
		}

		boolean refresh = MONEY_TRACKER.tick(nowNanos, totalCoins, -1L, fishActivity);
		if (!refresh) {
			return;
		}
		moneyTrackerLastRateDisplay = DotThousandsFormatter.format(MONEY_TRACKER.perHourValue());
		String base = Messages.getOrFallback(
			Messages.Id.FISH_MONEY_TRACKER_FORMAT,
			"&8| &eCoins&7/Hour: &e%s",
			moneyTrackerLastRateDisplay
		);
		if (MONEY_TRACKER.isPaused()) {
			String paused = Messages.getOrFallback(Messages.Id.FISH_MONEY_TRACKER_PAUSED, "PAUSED");
			moneyTrackerText = Messages.applyColorCodes(base + " &7(" + paused + ")");
			return;
		}
		moneyTrackerText = Messages.applyColorCodes(base);
	}

	private static void updateHudChangeTimes(long nowNanos, boolean hadBags, long previousTotal, long previousMax,
		long previousWeight, long previousCoins) {
		if (!hasBags) {
			totalLastChangeNanos = 0L;
			weightLastChangeNanos = 0L;
			coinsLastChangeNanos = 0L;
			moneyLastChangeNanos = 0L;
			lastTotalDisplay = 0L;
			lastMaxDisplay = 0L;
			return;
		}

		// Ensure newly detected bags show HUD immediately.
		if (!hadBags) {
			totalLastChangeNanos = nowNanos;
			weightLastChangeNanos = nowNanos;
			coinsLastChangeNanos = nowNanos;
			moneyLastChangeNanos = nowNanos;
			lastTotalDisplay = currentTotal;
			lastMaxDisplay = maxTotal;
			return;
		}

		if (previousTotal != currentTotal || previousMax != maxTotal || lastTotalDisplay != currentTotal || lastMaxDisplay != maxTotal) {
			totalLastChangeNanos = nowNanos;
			lastTotalDisplay = currentTotal;
			lastMaxDisplay = maxTotal;
		}
		if (previousWeight != totalWeight) {
			weightLastChangeNanos = nowNanos;
		}
		if (previousCoins != totalCoins) {
			coinsLastChangeNanos = nowNanos;
			if (moneyTrackerEnabled) {
				moneyLastChangeNanos = nowNanos;
			}
		}
	}

	private static boolean shouldRenderByLastChange(long lastChangeNanos) {
		if (lastChangeNanos <= 0L) {
			return false;
		}
		long now = System.nanoTime();
		return now > 0L && now - lastChangeNanos < HIDE_IDLE_NANOS;
	}

	private static String buildWeightDelta(long nowNanos) {
		if (deltaWeight <= 0L || nowNanos > deltaWeightUntilNanos) {
			return "";
		}
		// Parentheses in gray; delta in same dark-gray weight color.
		return " &7(+&8" + formatWeightDisplay(deltaWeight) + "&7)";
	}

	private static String buildCoinsDelta(long nowNanos) {
		if (deltaCoins <= 0L || nowNanos > deltaCoinsUntilNanos) {
			return "";
		}
		// Parentheses in gray; delta in same yellow coins color.
		return " &7(+&e" + CompactCountFormatter.format(deltaCoins) + "&7)";
	}

	private static String formatWeightDisplay(long totalGrams) {
		if (totalGrams < 0L) {
			return "0g";
		}
		if (totalGrams < 1000L) {
			return totalGrams + "g";
		}

		long kg = totalGrams / 1000L;
		long rem = totalGrams - kg * 1000L; // 0..999 grams

		// For larger totals, scale to tonnes (t). 1t = 1000kg = 1,000,000g.
		if (kg >= 1000L) {
			long tonnes = totalGrams / 1_000_000L;
			long remGrams = totalGrams - tonnes * 1_000_000L; // 0..999,999 grams

			// Very large totals: keep it simple (grouped tonnes, no decimals).
			if (tonnes >= 1000L) {
				return formatGrouped(tonnes) + "t";
			}

			// Up to 2 decimal digits in t, rounded to 0.01t (10kg).
			int hundredths = (int) ((remGrams + 5_000L) / 10_000L); // 0..100
			if (hundredths >= 100) {
				tonnes++;
				hundredths = 0;
			}
			if (hundredths == 0) {
				return tonnes + "t";
			}
			if ((hundredths % 10) == 0) {
				return tonnes + "." + (hundredths / 10) + "t";
			}
			if (hundredths < 10) {
				return tonnes + ".0" + hundredths + "t";
			}
			return tonnes + "." + hundredths + "t";
		}

		// Small totals: show up to 2 decimal digits in kg, rounded to 10g.
		int hundredths = (int) ((rem + 5L) / 10L); // 0..100
		if (hundredths >= 100) {
			kg++;
			hundredths = 0;
		}
		if (hundredths == 0) {
			return kg + "kg";
		}
		if ((hundredths % 10) == 0) {
			return kg + "." + (hundredths / 10) + "kg";
		}
		if (hundredths < 10) {
			return kg + ".0" + hundredths + "kg";
		}
		return kg + "." + hundredths + "kg";
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

	private static int indexOfSlashAfterNumber(String text, int startIndex) {
		if (text == null) {
			return -1;
		}
		int len = text.length();
		boolean seenDigit = false;
		for (int i = startIndex; i < len; i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				seenDigit = true;
				continue;
			}
			if (isSeparator(ch)) {
				continue;
			}
			if (ch == '/' && seenDigit) {
				return i;
			}
		}
		return -1;
	}

	private static long parseFirstNumber(String text, int startIndex) {
		if (text == null) {
			return -1L;
		}
		int len = text.length();
		int i = startIndex;
		while (i < len) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				break;
			}
			i++;
		}
		if (i >= len) {
			return -1L;
		}
		long value = 0L;
		boolean any = false;
		for (; i < len; i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				int digit = ch - '0';
				if (value > (Long.MAX_VALUE - digit) / 10L) {
					return -1L;
				}
				value = value * 10L + digit;
				any = true;
				continue;
			}
			if (isSeparator(ch)) {
				continue;
			}
			break;
		}
		return any ? value : -1L;
	}

	private static long parseWeightGrams(String text) {
		if (text == null || text.isEmpty()) {
			return -1L;
		}

		// 1) Locate "kg" (case-insensitive). If present, parse kilograms with optional decimals.
		int unitKg = indexOfKg(text);
		if (unitKg >= 0) {
			return parseKilogramsToGrams(text, unitKg);
		}

		// 2) Locate "t" (metric ton / "tonnen"). If present, parse tonnes with optional decimals.
		int unitT = indexOfTonnes(text);
		if (unitT >= 0) {
			return parseTonnesToGrams(text, unitT);
		}

		// 3) Fallback: treat as grams (first integer with separators).
		return parseFirstNumber(text, 0);
	}

	private static boolean containsWeightUnit(String text) {
		if (text == null || text.isEmpty()) {
			return false;
		}
		if (indexOfTonnes(text) >= 0) {
			return true;
		}
		int len = text.length();
		boolean seenDigit = false;
		for (int i = 0; i < len; i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				seenDigit = true;
				continue;
			}
			if (isSeparator(ch)) {
				continue;
			}
			if (ch == 'k' || ch == 'K') {
				if (i + 1 < len) {
					char next = text.charAt(i + 1);
					if (next == 'g' || next == 'G') {
						return true;
					}
				}
				seenDigit = false;
				continue;
			}
			if ((ch == 'g' || ch == 'G') && seenDigit) {
				return true;
			}
			seenDigit = false;
		}
		return false;
	}

	private static boolean containsCoinKeyword(String text) {
		if (text == null || text.isEmpty()) {
			return false;
		}
		int len = text.length();
		for (int i = 0; i < len; i++) {
			char ch = text.charAt(i);
			if (ch != 'c' && ch != 'C') {
				continue;
			}
			if (i + 3 >= len) {
				return false;
			}
			char o = text.charAt(i + 1);
			char i2 = text.charAt(i + 2);
			char n = text.charAt(i + 3);
			if ((o == 'o' || o == 'O') && (i2 == 'i' || i2 == 'I') && (n == 'n' || n == 'N')) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsDigit(String text) {
		if (text == null || text.isEmpty()) {
			return false;
		}
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				return true;
			}
		}
		return false;
	}

	private static int indexOfKg(String text) {
		int len = text.length();
		for (int i = 0; i + 1 < len; i++) {
			char a = text.charAt(i);
			if (a != 'k' && a != 'K') {
				continue;
			}
			char b = text.charAt(i + 1);
			if (b == 'g' || b == 'G') {
				return i;
			}
		}
		return -1;
	}

	private static int indexOfTonnes(String text) {
		if (text == null || text.isEmpty()) {
			return -1;
		}
		int len = text.length();
		boolean seenDigit = false;
		for (int i = 0; i < len; i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				seenDigit = true;
				continue;
			}
			if (isSeparator(ch)) {
				continue;
			}
			if (!seenDigit) {
				continue;
			}
			if (ch != 't' && ch != 'T') {
				seenDigit = false;
				continue;
			}
			// Accept: "1,34t", "1.34 t", "2ton", "2 tonnen"
			if (i + 1 >= len) {
				return i;
			}
			char next = text.charAt(i + 1);
			if (isSeparator(next) || next == 'o' || next == 'O') {
				return i;
			}
			return i;
		}
		return -1;
	}

	private static long parseKilogramsToGrams(String text, int unitIndex) {
		// WHY: Some servers format weight as "676.4kg" / "0,5 kg".
		// PERF: Manual parsing; no regex/streams.
		long wholeKg = 0L;
		long frac = 0L;
		int fracDigits = 0;
		boolean any = false;
		boolean inFraction = false;

		for (int i = 0; i < unitIndex; i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				int digit = ch - '0';
				any = true;
				if (inFraction) {
					// Accept up to 3 decimal digits (e.g., 676.49kg / 0.005kg).
					if (fracDigits < 3) {
						frac = frac * 10L + digit;
						fracDigits++;
					}
				} else {
					if (wholeKg > (Long.MAX_VALUE - digit) / 10L) {
						return -1L;
					}
					wholeKg = wholeKg * 10L + digit;
				}
				continue;
			}
			if (ch == '.' || ch == ',') {
				// Treat as decimal separator only if it likely is one: 1-3 digits after it before "kg".
				if (!inFraction) {
					int digitsAfter = countDigitsAfter(text, i + 1, unitIndex);
					if (digitsAfter > 0 && digitsAfter <= 3) {
						inFraction = true;
						continue;
					}
				}
				// Otherwise it's a thousands separator -> ignore.
				continue;
			}
			if (isSeparator(ch)) {
				continue;
			}
		}

		if (!any) {
			return -1L;
		}

		// kg -> g
		if (wholeKg > Long.MAX_VALUE / 1000L) {
			return -1L;
		}
		long grams = wholeKg * 1000L;
		if (fracDigits == 1) {
			grams += frac * 100L;
		} else if (fracDigits == 2) {
			grams += frac * 10L;
		} else if (fracDigits == 3) {
			grams += frac;
		}
		return grams;
	}

	private static long parseTonnesToGrams(String text, int unitIndex) {
		// WHY: Some servers format weight as "1,34t" / "2 tonnen".
		// PERF: Manual parsing; no regex/streams.
		long wholeTonnes = 0L;
		long frac = 0L;
		int fracDigits = 0;
		boolean any = false;
		boolean inFraction = false;

		for (int i = 0; i < unitIndex; i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				int digit = ch - '0';
				any = true;
				if (inFraction) {
					if (fracDigits < 3) {
						frac = frac * 10L + digit;
						fracDigits++;
					}
				} else {
					if (wholeTonnes > (Long.MAX_VALUE - digit) / 10L) {
						return -1L;
					}
					wholeTonnes = wholeTonnes * 10L + digit;
				}
				continue;
			}
			if (ch == '.' || ch == ',') {
				if (!inFraction) {
					int digitsAfter = countDigitsAfter(text, i + 1, unitIndex);
					if (digitsAfter > 0 && digitsAfter <= 3) {
						inFraction = true;
						continue;
					}
				}
				continue;
			}
			if (isSeparator(ch)) {
				continue;
			}
		}

		if (!any) {
			return -1L;
		}

		// t -> g (1t = 1,000,000g)
		if (wholeTonnes > Long.MAX_VALUE / 1_000_000L) {
			return -1L;
		}
		long grams = wholeTonnes * 1_000_000L;
		if (fracDigits == 1) {
			grams += frac * 100_000L;
		} else if (fracDigits == 2) {
			grams += frac * 10_000L;
		} else if (fracDigits == 3) {
			grams += frac * 1_000L;
		}
		return grams;
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
		// WHY: Servers may format large numbers with locale-specific thousands separators.
		// PERF: Single char checks; no regex/allocations.
		return ch == '.'
			|| ch == ','
			|| ch == ' '
			|| ch == '\''
			|| ch == '_'
			|| ch == '\u00A0'   // no-break space
			|| ch == '\u202F'   // narrow no-break space
			|| ch == '\u2009';  // thin space
	}
}
