package de.nuua.primetooler.features.chatmention.client;

import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * WARUM: Hebt Spieler-Namen in Chat-Komponenten hervor.
 * PERF: Lineares Scannen ohne Regex oder Streams.
 */
public final class ChatMentionHighlighter {
	public record Result(Component component, boolean matched) {
	}

	private ChatMentionHighlighter() {
	}

	public static Result highlight(Component input, String name) {
		if (input == null || name == null || name.isEmpty()) {
			return new Result(input, false);
		}
		String needleLower = name.toLowerCase(Locale.ROOT);
		int needleLen = needleLower.length();
		if (needleLen == 0) {
			return new Result(input, false);
		}
		MutableComponent out = Component.empty();
		boolean[] matched = new boolean[] { false };
		input.visit((style, text) -> {
			if (text == null || text.isEmpty()) {
				return Optional.empty();
			}
			appendHighlighted(out, style, text, needleLower, needleLen, matched);
			return Optional.empty();
		}, Style.EMPTY);
		return matched[0] ? new Result(out, true) : new Result(input, false);
	}

	private static void appendHighlighted(MutableComponent out, Style style, String text,
		String needleLower, int needleLen, boolean[] matched) {
		String lower = text.toLowerCase(Locale.ROOT);
		int index = 0;
		while (true) {
			int hit = lower.indexOf(needleLower, index);
			if (hit < 0) {
				break;
			}
			if (!isWordMatch(text, hit, needleLen)) {
				index = hit + 1;
				continue;
			}
			if (hit > index) {
				out.append(Component.literal(text.substring(index, hit)).setStyle(style));
			}
			Style highlightStyle = (style == null ? Style.EMPTY : style).withColor(ChatFormatting.AQUA);
			out.append(Component.literal(text.substring(hit, hit + needleLen)).setStyle(highlightStyle));
			matched[0] = true;
			index = hit + needleLen;
		}
		if (index < text.length()) {
			out.append(Component.literal(text.substring(index)).setStyle(style));
		}
	}

	private static boolean isWordMatch(String text, int start, int len) {
		if (start < 0 || len <= 0 || start + len > text.length()) {
			return false;
		}
		if (start > 0 && isWordChar(text.charAt(start - 1))) {
			return false;
		}
		int end = start + len;
		return end >= text.length() || !isWordChar(text.charAt(end));
	}

	private static boolean isWordChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}
}
