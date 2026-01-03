package de.nuua.primetooler.platform.chat;

import net.minecraft.network.chat.Component;

/**
 * WHY: Centralize player-facing messages for consistency and easy edits.
 * PERF: Simple switch-based formatting; no allocations beyond the Component.
 */
public final class PlayerMessages {
	public enum Id {
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
		SYNC_ITEMS
	}

	private PlayerMessages() {
	}

	public static Component text(Id id, Object... args) {
		return switch (id) {
			case CHECKITEM_NO_HAND ->
				literal("\u00A7cNo item in main hand.");
			case CHECKITEM_DUMP_HEADER ->
				literal("\u00A7aItem dump:");
			case CHECKITEM_ID ->
				literal("Id: " + arg(args, 0));
			case CHECKITEM_COUNT ->
				literal("Count: " + arg(args, 0));
			case CHECKITEM_DAMAGE ->
				literal("Damage: " + arg(args, 0));
			case CHECKITEM_RARITY ->
				literal("Rarity: " + arg(args, 0));
			case CHECKITEM_NAME ->
				literal("Name: " + arg(args, 0));
			case CHECKITEM_COMPONENTS_HEADER ->
				literal("\u00A7eComponents (" + arg(args, 0) + "):");
			case CHECKITEM_COMPONENTS_EMPTY ->
				literal("\u00A77(empty)");
			case CHECKITEM_COMPONENTS_ENTRY ->
				literal(arg(args, 0) + " = " + arg(args, 1));
			case CHECKITEM_CUSTOM_EMPTY ->
				literal("\u00A77Custom data: (empty)");
			case CHECKITEM_CUSTOM_HEADER ->
				literal("\u00A7eCustom data (SNBT):");
			case CHECKITEM_CUSTOM_RAW ->
				literal(String.valueOf(arg(args, 0)));
			case CHECKITEM_UUID_HEADER ->
				literal("\u00A7eUUID data:");
			case CHECKITEM_UUID_ENTRY ->
				literal(String.valueOf(arg(args, 0)));
			case CHECKITEM_TOOLTIP_HEADER ->
				literal("\u00A7eTooltip (" + arg(args, 0) + "):");
			case CHECKITEM_SAVED ->
				literal("\u00A7aGESICHTERT");
			case CHECKITEM_NOT_SAVED ->
				literal("\u00A7cNICHT GESICHERT");
			case CHECKITEM_ITEM_SAVED ->
				literal("\u00A7aItem saved: " + arg(args, 0));
			case CHECKITEM_ITEM_UNSAVED ->
				literal("\u00A7eItem unsaved.");
			case CHECKITEM_SAVE_DENIED ->
				literal("\u00A7cItems can only be saved as a single item with custom name, lore, or enchants.");
			case SLOT_LOCK ->
				literal("\u00A7aSlot saved.");
			case SLOT_UNLOCK ->
				literal("\u00A7eSlot unsaved.");
			case DEBUG_SYNC_STATE ->
				literal("\u00A77[PT] Sync debug: " + arg(args, 0));
			case SYNC_ITEMS ->
				literal("\u00A77[PT] Sync items.");
		};
	}

	private static Component literal(String value) {
		return Component.literal(value == null ? "" : value);
	}

	private static Object arg(Object[] args, int index) {
		if (args == null || index < 0 || index >= args.length) {
			return "";
		}
		Object value = args[index];
		return value == null ? "" : value;
	}
}
