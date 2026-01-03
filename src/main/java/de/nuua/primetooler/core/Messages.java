package de.nuua.primetooler.core;

/**
 * WHY: Centralize all player-facing text in one place for fast edits and consistency.
 * PERF: Simple switch with minimal allocations; no regex or streams.
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
		LABEL_ZOOM,
		LABEL_DURABILITY,
		LABEL_CALC,
		LABEL_PACK,
		LABEL_LOCATOR,
		LABEL_SLOTLOCK,
		LABEL_SYNC,
		LABEL_AUTOSPAWN,
		LABEL_SPECIALNAMES,
		LABEL_FRONTCAM,
		LABEL_EFFECTS,
		LABEL_HUDEFFECTS,
		LABEL_NO_ACCESS,
		TOOLTIP_LOCKED,
		TOOLTIP_ZOOM,
		TOOLTIP_DURABILITY,
		TOOLTIP_CALC,
		TOOLTIP_PACK,
		TOOLTIP_LOCATOR,
		TOOLTIP_SLOTLOCK,
		TOOLTIP_SYNC,
		TOOLTIP_AUTOSPAWN,
		TOOLTIP_SPECIALNAMES,
		TOOLTIP_FRONTCAM,
		TOOLTIP_EFFECTS,
		TOOLTIP_HUDEFFECTS,
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
		DEBUG_SYNC_STATE,
		SYNC_ITEMS,
		LORE_PREFIX,
		LORE_SAVED_TEXT,
		LORE_SAVED_VISIBLE
	}

	private Messages() {
	}

	public static String get(Id id, Object... args) {
		return switch (id) {
			case MENU_TITLE -> "Prime Menü";
			case TAB_CONFIG -> "Konfig";
			case TAB_CHAT -> "Chat";
			case TAB_SPECIAL -> "SPEZIAL";
			case TAB_SPECIAL_RAINBOW -> "&c&lS&6&lP&e&lE&a&lZ&b&lI&9&lA&d&lL";
			case CONFIG_TITLE -> "&lKONFIG";
			case CONFIG_DESC_1 -> "Schalte PrimeTooler-Funktionen und Client-Helper";
			case CONFIG_DESC_2 -> "ein oder aus. Änderungen wirken sofort";
			case CONFIG_DESC_3 -> "und werden lokal gespeichert.";
			case CHAT_TITLE -> "&lCHAT NACHRICHTEN";
			case CHAT_DESC_1 -> "Schnelle Presets für Chat-Nachrichten zum Bearbeiten und Senden.";
			case CHAT_DESC_2 ->
				"Für häufige Texte; max &f&l5&7 Slots. &8&o(&f&l25 &8&oals Spezial-Mitglied.)";
			case CHAT_HINT_INPUT -> "Nachricht eingeben...";
			case CHAT_READY -> "&a&lBEREIT";
			case SPECIAL_TITLE -> "&lSPEZIELLE MITGLIEDER";
			case SPECIAL_DESC_1 -> "Spezielle Mitglieder sind Beta-Tester,";
			case SPECIAL_DESC_2 -> "Unterstützer und Freunde, die PrimeTooler";
			case SPECIAL_DESC_3 -> "mit frühem Feedback und Ideen mitgestalten.";
			case SPECIAL_DESC_4 -> "Danke für die Unterstützung und das Testen.";
			case SPECIAL_SECTION_ADMIN -> "&c★ &f&lMEOWER &8&o(Admin)";
			case SPECIAL_SECTION_SPECIAL -> "&f&lSPEZIAL";
			case BUTTON_DELETE -> "Löschen";
			case BUTTON_CHAT -> "CHAT";
			case BUTTON_SELF -> "AN MICH";
			case BUTTON_ADD -> "+";
			case BUTTON_SPECIAL_SUPPORT -> "&f&lPATREON";
			case STATE_ON -> "AN";
			case STATE_OFF -> "AUS";
			case LABEL_ZOOM -> "Unbegrenzter Zoom: ";
			case LABEL_DURABILITY -> "Haltbarkeits-Schutz: ";
			case LABEL_CALC -> "Inventar-Rechner: ";
			case LABEL_PACK -> "Server-Packs blockieren: ";
			case LABEL_LOCATOR -> "Locator-Leiste: ";
			case LABEL_SLOTLOCK -> "Slot-Sperre: ";
			case LABEL_SYNC -> "Item-Sync Debug: ";
			case LABEL_AUTOSPAWN -> "Auto /plot home (wenig Leben): ";
			case LABEL_SPECIALNAMES -> "Spezielle Namen: ";
			case LABEL_FRONTCAM -> "Frontkamera: ";
			case LABEL_EFFECTS -> "Effekte im Inventar: ";
			case LABEL_HUDEFFECTS -> "Effekte im HUD: ";
			case LABEL_NO_ACCESS -> "&8&oGesperrt";
			case TOOLTIP_ZOOM ->
				"&7Schaltet &f&lUnbegrenzter Zoom&7.";
			case TOOLTIP_DURABILITY ->
				"&7Schützt Items vor Bruch bei niedriger Haltbarkeit. &8• &7"
					+ "&7Blockiert die Nutzung am &f&lSchwellenwert 10%&7 und warnt sichtbar.";
			case TOOLTIP_CALC ->
				"&7Aktiviert das &f&lInventar-Rechner&7 Feld im GUI.";
			case TOOLTIP_PACK ->
				"&7Blockiert Server-Resourcepacks beim Join. &8• &7"
					+ "&7Behält den &f&llokalen Status&7 und startet keinen Download.";
			case TOOLTIP_LOCATOR ->
				"&7Schaltet die &f&lLocator-Leiste&7 Anzeige.";
			case TOOLTIP_SLOTLOCK ->
				"&7Sperrt ausgewählte Inventar-Slots gegen Verschieben. &8• &7"
					+ "&7Drücke &f&lL&7 auf einem Slot zum Umschalten; bleibt aktiv.";
			case TOOLTIP_SYNC ->
				"&7Zeigt zusätzliche &f&lItem-Sync&7 Debug-Infos. &8• &7"
					+ "&7Schreibt &f&lnur-Client&7 Logs in die Konsole zur Diagnose.";
			case TOOLTIP_AUTOSPAWN ->
				"&7Auto &f&l/plot home&7 bei wenig Leben. &8• &7"
					+ "&7Sendet den Befehl unterhalb des &f&lSchwellenwerts 2.5 Herzen&7.";
			case TOOLTIP_SPECIALNAMES ->
				"&7Schaltet &f&lSpezielle Namen&7 Styling um. &8• &7"
					+ "&7Gilt für &f&lTab & Nametags&7.";
			case TOOLTIP_FRONTCAM ->
				"&7Deaktiviert die &f&lFrontkamera&7 im F5. &8• &7"
					+ "&7Wechselt nur zwischen &f&lEgo-Perspektive&7 und &f&lRückansicht&7.";
			case TOOLTIP_EFFECTS ->
				"&7Blendet die Effektanzeige im Inventar ein oder aus. &8\u2022 &7"
					+ "&7Versteckt die &f&lTr\u00e4nkeffekte&7 auf der linken Seite.";
			case TOOLTIP_HUDEFFECTS ->
				"&7Blendet die Effektanzeige oben rechts im HUD ein oder aus. &8\u2022 &7"
					+ "&7Versteckt aktive &f&lTrankeffekte&7 im Overlay.";
			case TOOLTIP_MSG ->
				"&7Klicke zum Bearbeiten; Text wird direkt gespeichert.";
			case TOOLTIP_ADD ->
				"&7Fügt die Nachricht zur Liste hinzu. &8• &7"
					+ "&7Max. &f&l5&7 Slots. &8&o(&f&l25 &8&oals Spezial-Mitglied.)";
			case TOOLTIP_ADDONLY ->
				"&7Neue Schnell-Chat Nachricht anlegen.";
			case TOOLTIP_ADD_LOCKED ->
				"&7Für mehr benötigst du eine &f&lSPEZIAL-MITGLIEDSCHAFT&7. &8• &7"
					+ "&7Kein Zugriff auf zusätzliche &f&lSlots&7.";
			case TOOLTIP_DELETE ->
				"&7Löscht den aktuellen Slot.";
			case TOOLTIP_SENDCHAT ->
				"&7Sendet die Nachricht in den Chat. &8• &7"
					+ "&7Startet den &f&lCooldown&7.";
			case TOOLTIP_SENDOWN ->
				"&7Zeigt die Nachricht nur dir. &8• &7"
					+ "&7Ideal zum Testen von &f&lFormatierung&7.";
			case TOOLTIP_CHAT_INPUT ->
				"&f&lCOLORCODES/n&0&&00 &1&&11 &2&&22 &3&&33 &4&&44 &5&&55 &6&&66 &7&&77 &8&&88 &9&&99 "
					+ "&a&&aa &b&&bb &c&&cc &d&&dd &e&&ee &f&&ff &o&&oo&r&fKursiv &l&&ll&rDick &n&&nn&rUnterstrichen\n&m&&mm&rDurchstrichen &f&&fk&rMagisch&r &f&&frReset";
			case TOOLTIP_SPECIAL_SUPPORT ->
				"&7Öffnet die Support-Seite im Browser. &8• &7"
					+ "&7Danke für deinen &f&lSupport&7.";
			case TOOLTIP_LOCKED ->
				"&7Dieses Feature ist nur für &f&lSPEZIELLE MITGLIEDER&7 nutzbar. &8• &7"
					+ "&7Kein Zugriff auf diese &f&lEinstellung&7.";
			case CHECKITEM_NO_HAND -> "&cKein Item in der Haupthand.";
			case CHECKITEM_DUMP_HEADER -> "&aItem-Dump:";
			case CHECKITEM_ID -> "ID: " + arg(args, 0);
			case CHECKITEM_COUNT -> "Anzahl: " + arg(args, 0);
			case CHECKITEM_DAMAGE ->
				"Schaden: " + arg(args, 0) + "/" + arg(args, 1) + " (haltbar=" + arg(args, 2) + ")";
			case CHECKITEM_RARITY ->
				"Seltenheit: " + arg(args, 0) + ", Verzaubert=" + arg(args, 1) + ", Glanz=" + arg(args, 2);
			case CHECKITEM_NAME -> "Name: " + arg(args, 0);
			case CHECKITEM_COMPONENTS_HEADER -> "&eKomponenten (" + arg(args, 0) + "):";
			case CHECKITEM_COMPONENTS_EMPTY -> "&7(keine)";
			case CHECKITEM_COMPONENTS_ENTRY -> arg(args, 0) + " = " + arg(args, 1);
			case CHECKITEM_CUSTOM_EMPTY -> "&7Custom-Daten: (leer)";
			case CHECKITEM_CUSTOM_HEADER -> "&eCustom-Daten (SNBT):";
			case CHECKITEM_CUSTOM_RAW -> String.valueOf(arg(args, 0));
			case CHECKITEM_UUID_HEADER -> "&eUUID-Daten:";
			case CHECKITEM_UUID_ENTRY -> String.valueOf(arg(args, 0));
			case CHECKITEM_TOOLTIP_HEADER -> "&eTooltip (" + arg(args, 0) + "):";
			case CHECKITEM_SAVED -> "&aGESICHERT";
			case CHECKITEM_NOT_SAVED -> "&cNICHT GESICHERT";
			case CHECKITEM_ITEM_SAVED -> "&aItem gespeichert: " + arg(args, 0);
			case CHECKITEM_ITEM_UNSAVED -> "&eItem entfernt.";
			case CHECKITEM_SAVE_DENIED ->
				"&cItems können nur gespeichert werden, wenn Name, Lore oder Verzauberungen vorhanden sind.";
			case SLOT_LOCK -> "&aSlot gespeichert.";
			case SLOT_UNLOCK -> "&eSlot entfernt.";
			case DEBUG_SYNC_STATE -> "&7[PT] Sync-Debug: " + arg(args, 0);
			case SYNC_ITEMS -> "&7[PT] Sync Items.";
			case LORE_PREFIX -> "pt:";
			case LORE_SAVED_TEXT -> "&a\u2714 &8&oItem gesichert!";
			case LORE_SAVED_VISIBLE -> "\u2714 Item gesichert!";
		};
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
