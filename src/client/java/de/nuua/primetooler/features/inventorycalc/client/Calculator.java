package de.nuua.primetooler.features.inventorycalc.client;

/**
 * WHY: Lightweight expression evaluation for the inventory calculator field.
 * PERF: Single-pass parser, no regex, small allocations.
 */
public final class Calculator {
	private Calculator() {
	}

	public static Double tryEvaluate(String expression) {
		if (expression == null) {
			return null;
		}
		Parser parser = new Parser(expression);
		Double value = parser.parseExpression();
		if (value == null || !parser.isAtEnd()) {
			return null;
		}
		if (!Double.isFinite(value)) {
			return null;
		}
		return value;
	}

	public static String format(double value) {
		long rounded = Math.round(value);
		if (Math.abs(value - rounded) < 1.0e-9) {
			return Long.toString(rounded);
		}
		return Double.toString(value);
	}

	private static final class Parser {
		private final char[] chars;
		private int index;

		private Parser(String input) {
			this.chars = input.toCharArray();
		}

		boolean isAtEnd() {
			skipSpaces();
			return index >= chars.length;
		}

		Double parseExpression() {
			Double left = parseTerm();
			if (left == null) {
				return null;
			}
			while (true) {
				skipSpaces();
				if (match('+')) {
					Double right = parseTerm();
					if (right == null) {
						return null;
					}
					left = left + right;
				} else if (match('-')) {
					Double right = parseTerm();
					if (right == null) {
						return null;
					}
					left = left - right;
				} else {
					return left;
				}
			}
		}

		private Double parseTerm() {
			Double left = parseFactor();
			if (left == null) {
				return null;
			}
			while (true) {
				skipSpaces();
				if (match('*')) {
					Double right = parseFactor();
					if (right == null) {
						return null;
					}
					left = left * right;
				} else if (match('/')) {
					Double right = parseFactor();
					if (right == null) {
						return null;
					}
					left = left / right;
				} else {
					return left;
				}
			}
		}

		private Double parseFactor() {
			skipSpaces();
			if (match('+')) {
				return parseFactor();
			}
			if (match('-')) {
				Double value = parseFactor();
				return value == null ? null : -value;
			}
			if (match('(')) {
				Double inner = parseExpression();
				if (inner == null || !match(')')) {
					return null;
				}
				return inner;
			}
			return parseNumber();
		}

		private Double parseNumber() {
			skipSpaces();
			int start = index;
			boolean seenDot = false;
			while (index < chars.length) {
				char c = chars[index];
				if (c >= '0' && c <= '9') {
					index++;
					continue;
				}
				if (c == '.' && !seenDot) {
					seenDot = true;
					index++;
					continue;
				}
				break;
			}
			if (start == index) {
				return null;
			}
			String number = new String(chars, start, index - start);
			try {
				return Double.parseDouble(number);
			} catch (NumberFormatException ex) {
				return null;
			}
		}

		private boolean match(char expected) {
			if (index >= chars.length || chars[index] != expected) {
				return false;
			}
			index++;
			return true;
		}

		private void skipSpaces() {
			while (index < chars.length && chars[index] == ' ') {
				index++;
			}
		}
	}
}
