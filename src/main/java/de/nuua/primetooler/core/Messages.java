package de.nuua.primetooler.core;

/**
 * WARUM: Zentrale Message-Keys und Farbcodes an einer Stelle.
 * PERF: Minimale Logik; Auflösung passiert im Sprache-System.
 */
public final class Messages {
	public enum Id {
		MENU_TITLE,
		TAB_CONFIG,
		TAB_CHAT,
		TAB_SPECIAL,
		TAB_SPECIAL_RAINBOW,
		CONFIG_TITLE,
		CONFIG_DESC_1,
		CONFIG_DESC_2,
		CONFIG_DESC_3,
		CONFIG_SEARCH_HINT,
		CONFIG_SECTION_GAMEPLAY,
		CONFIG_SECTION_VISUAL,
		CONFIG_SECTION_SOUND,
		CHAT_TITLE,
		CHAT_DESC_1,
		CHAT_DESC_2,
		CHAT_HINT_INPUT,
		CHAT_READY,
		SPECIAL_TITLE,
		SPECIAL_DESC_1,
		SPECIAL_DESC_2,
		SPECIAL_DESC_3,
		SPECIAL_DESC_4,
		SPECIAL_SECTION_ADMIN,
		SPECIAL_SECTION_SPECIAL,
		BUTTON_DELETE,
		BUTTON_CHAT,
		BUTTON_SELF,
		BUTTON_ADD,
		BUTTON_SPECIAL_SUPPORT,
		STATE_ON,
		STATE_OFF,
		STATE_ALL,
		STATE_RARE,
		LABEL_ZOOM,
		LABEL_DURABILITY,
		LABEL_CALC,
		LABEL_PACK,
		LABEL_LOCATOR,
		LABEL_SLOTLOCK,
		LABEL_DOUBLE_DROP,
		LABEL_SYNC,
		LABEL_AUTOSPAWN,
		LABEL_SPECIALNAMES,
		LABEL_CLANTAG,
		LABEL_FRONTCAM,
		LABEL_EFFECTS,
		LABEL_HUDEFFECTS,
		LABEL_BEACON_SOUND,
		LABEL_JACKPOT_SOUND,
		LABEL_WARNING_SOUND,
		LABEL_NO_ACCESS,
		TOOLTIP_LOCKED,
		TOOLTIP_ZOOM,
		TOOLTIP_DURABILITY,
		TOOLTIP_CALC,
		TOOLTIP_PACK,
		TOOLTIP_LOCATOR,
		TOOLTIP_SLOTLOCK,
		TOOLTIP_DOUBLE_DROP,
		TOOLTIP_SYNC,
		TOOLTIP_AUTOSPAWN,
		TOOLTIP_SPECIALNAMES,
		TOOLTIP_CLANTAG,
		TOOLTIP_FRONTCAM,
		TOOLTIP_EFFECTS,
		TOOLTIP_HUDEFFECTS,
		TOOLTIP_BEACON_SOUND,
		TOOLTIP_JACKPOT_SOUND,
		TOOLTIP_WARNING_SOUND,
		TOOLTIP_MSG,
		TOOLTIP_ADD,
		TOOLTIP_ADDONLY,
		TOOLTIP_ADD_LOCKED,
		TOOLTIP_DELETE,
		TOOLTIP_SENDCHAT,
		TOOLTIP_SENDOWN,
		TOOLTIP_CHAT_INPUT,
		TOOLTIP_SPECIAL_SUPPORT,
		CHECKITEM_NO_HAND,
		CHECKITEM_DUMP_HEADER,
		CHECKITEM_ID,
		CHECKITEM_COUNT,
		CHECKITEM_DAMAGE,
		CHECKITEM_RARITY,
		CHECKITEM_NAME,
		CHECKITEM_COMPONENTS_HEADER,
		CHECKITEM_COMPONENTS_EMPTY,
		CHECKITEM_COMPONENTS_ENTRY,
		CHECKITEM_CUSTOM_EMPTY,
		CHECKITEM_CUSTOM_HEADER,
		CHECKITEM_CUSTOM_RAW,
		CHECKITEM_UUID_HEADER,
		CHECKITEM_UUID_ENTRY,
		CHECKITEM_TOOLTIP_HEADER,
		CHECKITEM_SAVED,
		CHECKITEM_NOT_SAVED,
		CHECKITEM_ITEM_SAVED,
		CHECKITEM_ITEM_UNSAVED,
		CHECKITEM_SAVE_DENIED,
		SLOT_LOCK,
		SLOT_UNLOCK,
		DOUBLE_DROP_CONFIRM,
		DEBUG_SYNC_STATE,
		SYNC_ITEMS,
		LORE_PREFIX,
		LORE_SAVED_TEXT,
		LORE_SAVED_VISIBLE,
		ARMOR_DURABILITY_LOW,
	}

	private Messages() {
	}

	public static String key(Id id) {
		return "primetooler.msg." + id.name().toLowerCase(java.util.Locale.ROOT);
	}

	public static String get(Id id, Object... args) {
		return net.minecraft.network.chat.Component.translatable(key(id), args).getString();
	}

	public static String applyColorCodes(String value) {
		if (value == null || value.indexOf('&') < 0) {
			return value == null ? "" : value;
		}
		StringBuilder builder = null;
		int length = value.length();
		for (int i = 0; i < length; i++) {
			char c = value.charAt(i);
			if (c == '&' && i + 1 < length) {
				char next = value.charAt(i + 1);
				if (isFormatCode(next)) {
					if (builder == null) {
						builder = new StringBuilder(length);
						builder.append(value, 0, i);
					}
					builder.append('\u00A7');
					i++;
					builder.append(next);
					continue;
				}
			}
			if (builder != null) {
				builder.append(c);
			}
		}
		return builder == null ? value : builder.toString();
	}

	private static boolean isFormatCode(char code) {
		char lower = code >= 'A' && code <= 'Z' ? (char) (code + 32) : code;
		return (lower >= '0' && lower <= '9')
			|| (lower >= 'a' && lower <= 'f')
			|| lower == 'k'
			|| lower == 'l'
			|| lower == 'm'
			|| lower == 'n'
			|| lower == 'o'
			|| lower == 'r';
	}

	private static Object arg(Object[] args, int index) {
		if (args == null || index < 0 || index >= args.length) {
			return "";
		}
		Object value = args[index];
		return value == null ? "" : value;
	}
}















