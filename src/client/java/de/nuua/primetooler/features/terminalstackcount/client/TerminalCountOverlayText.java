package de.nuua.primetooler.features.terminalstackcount.client;

import de.nuua.primetooler.core.util.CompactCountFormatter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * WHY: Derive a compact stack overlay count from terminal-style item names like "13.824x Oak Log".
 * PERF: O(n) scan of the name string only when rendering item decorations; no regex, no boxing.
 */
public final class TerminalCountOverlayText {
	private TerminalCountOverlayText() {
	}

	public static String fromStack(ItemStack stack) {
		if (stack == null) {
			return null;
		}
		Component name = stack.getHoverName();
		if (name == null) {
			return null;
		}
		long value = parsePrefixValue(name);
		if (value <= 0L) {
			return null;
		}
		if (value < 1000L) {
			return Long.toString(value);
		}
		return CompactCountFormatter.format(value);
	}

	private static long parsePrefixValue(Component component) {
		if (component instanceof MutableComponent mutable) {
			long value = parsePrefixValueFromMutable(mutable);
			if (value > 0L) {
				return value;
			}
		}
		return parsePrefixValueFromString(component.getString());
	}

	private static long parsePrefixValueFromMutable(MutableComponent component) {
		int digitCount = 0;
		long value = 0L;
		boolean sawX = false;

		String rootText = segmentText(component);
		if (rootText == null) {
			return -1L;
		}
		for (int i = 0; i < rootText.length(); i++) {
			char ch = rootText.charAt(i);
			if (!sawX) {
				if (ch >= '0' && ch <= '9') {
					int digit = ch - '0';
					if (value > (Long.MAX_VALUE - digit) / 10L) {
						return -1L;
					}
					value = value * 10L + digit;
					digitCount++;
					continue;
				}
				if ((ch == '.' || ch == ',' || ch == ' ' || ch == '\'') && digitCount > 0) {
					continue;
				}
				if ((ch == 'x' || ch == 'X') && digitCount > 0) {
					sawX = true;
					continue;
				}
				return -1L;
			}
			return ch == ' ' ? value : -1L;
		}

		List<Component> siblings = component.getSiblings();
		for (int s = 0; s < siblings.size(); s++) {
			Component seg = siblings.get(s);
			String segText = segmentText(seg);
			if (segText == null) {
				segText = seg.getString();
			}
			for (int i = 0; i < segText.length(); i++) {
				char ch = segText.charAt(i);
				if (!sawX) {
					if (ch >= '0' && ch <= '9') {
						int digit = ch - '0';
						if (value > (Long.MAX_VALUE - digit) / 10L) {
							return -1L;
						}
						value = value * 10L + digit;
						digitCount++;
						continue;
					}
					if ((ch == '.' || ch == ',' || ch == ' ' || ch == '\'') && digitCount > 0) {
						continue;
					}
					if ((ch == 'x' || ch == 'X') && digitCount > 0) {
						sawX = true;
						continue;
					}
					return -1L;
				}
				return ch == ' ' ? value : -1L;
			}
		}

		return -1L;
	}

	private static long parsePrefixValueFromString(String text) {
		TerminalCountPrefix prefix = parsePrefix(text);
		return prefix == null ? -1L : prefix.value;
	}

	private static String segmentText(Component component) {
		if (component == null) {
			return null;
		}
		if (component instanceof MutableComponent mutable) {
			ComponentContents contents = mutable.getContents();
			if (contents instanceof PlainTextContents plain) {
				return plain.text();
			}
		}
		return null;
	}

	private static TerminalCountPrefix parsePrefix(String text) {
		int len = text.length();
		if (len < 4) {
			return null;
		}

		int i = 0;
		int digitCount = 0;
		while (i < len) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				digitCount++;
				i++;
				continue;
			}
			if ((ch == '.' || ch == ',' || ch == ' ' || ch == '\'') && digitCount > 0) {
				i++;
				continue;
			}
			break;
		}
		if (digitCount == 0 || i >= len) {
			return null;
		}
		char x = text.charAt(i);
		if (x != 'x' && x != 'X') {
			return null;
		}
		int xIndex = i;
		if (xIndex + 1 >= len || text.charAt(xIndex + 1) != ' ') {
			return null;
		}

		long value = 0L;
		for (int j = 0; j < xIndex; j++) {
			char ch = text.charAt(j);
			if (ch >= '0' && ch <= '9') {
				int digit = ch - '0';
				if (value > (Long.MAX_VALUE - digit) / 10L) {
					return null;
				}
				value = value * 10L + digit;
				continue;
			}
			if (ch == '.' || ch == ',' || ch == ' ' || ch == '\'') {
				continue;
			}
			return null;
		}
		return new TerminalCountPrefix(value);
	}

	private static final class TerminalCountPrefix {
		private final long value;

		private TerminalCountPrefix(long value) {
			this.value = value;
		}
	}
}
