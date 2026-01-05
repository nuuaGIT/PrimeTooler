package de.nuua.primetooler.features.checkitem.client;

import de.nuua.primetooler.core.config.ContainerCacheConfig;
import de.nuua.primetooler.core.config.SavedItemsConfig;
import de.nuua.primetooler.core.config.SavedSlotsConfig;
import de.nuua.primetooler.core.Messages;
import de.nuua.primetooler.core.lifecycle.Bootstrap;
import de.nuua.primetooler.core.lifecycle.Module;
import de.nuua.primetooler.features.fishbag.client.FishbagTotalState;
import de.nuua.primetooler.features.jobtracker.client.JobTrackerState;
import de.nuua.primetooler.features.playermark.client.PlayerMarkRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import com.mojang.serialization.JsonOps;
import de.nuua.primetooler.platform.config.ClientConfigIO;

/**
 * WHY: Client-only tool to inspect held item UUID data on demand.
 * PERF: Command-only execution; lightweight recursive scan of custom data.
 */
public final class CheckItemClientModule implements Module {
	private static final boolean ITEM_SAVE_ENABLED = false;
	private static final String SAVED_KEY = "primetooler_saved_uuid";
	private static final String LOCAL_LORE_PREFIX = Messages.get(Messages.Id.LORE_PREFIX);
	private static final String LOCAL_LORE_TEXT = Messages.get(Messages.Id.LORE_SAVED_TEXT);
	private static final String LOCAL_LORE_VISIBLE = Messages.get(Messages.Id.LORE_SAVED_VISIBLE);
	private static SavedItemsConfig savedItems;
	private static boolean savedItemsDirty;
	private static int refreshTickCounter;
	private static boolean debugSyncEnabled;
	private static boolean slotLockingEnabled = true;
	private static long clientTickCounter;
	private static int menuCacheCleanupCounter;
	private static final int MENU_CACHE_TTL_TICKS = 200;
	private static final int MENU_CACHE_CLEANUP_INTERVAL = 40;
	private static final HashMap<Long, MenuSlotCache> menuSlotCache = new HashMap<>();
	private static ContainerCacheConfig containerCache;
	private static boolean containerCacheDirty;
	private static final long CONTAINER_CACHE_TTL_MS = 7L * 24L * 60L * 60L * 1000L;
	private static SavedSlotsConfig savedSlots;
	private static boolean savedSlotsDirty;

	@Override
	public String id() {
		return "checkitem_client";
	}

	@Override
	public void preInit(Bootstrap ctx) {
	}

	@Override
	public void init(Bootstrap ctx) {
		ensureSavedSlots();
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			reloadSavedSlots();
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			saveSavedSlotsOnDisconnect();
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
				ClientCommandManager.literal("primetooler")
					.then(ClientCommandManager.literal("checkitem")
						.requires(source -> PlayerMarkRegistry.currentUserRank() == PlayerMarkRegistry.Rank.ADMIN)
						.executes(context -> {
						printHeldItemDetails();
						return 1;
					}))
					.then(ClientCommandManager.literal("debugsync")
						.requires(source -> PlayerMarkRegistry.currentUserRank() == PlayerMarkRegistry.Rank.ADMIN)
						.executes(context -> {
						toggleDebugSyncAndNotify();
						return 1;
					}))
					.then(ClientCommandManager.literal("resetTracker")
						.then(ClientCommandManager.literal("jobxp").executes(context -> {
							JobTrackerState.resetXpTracker();
							notifyReset("jobxp");
							return 1;
						}))
						.then(ClientCommandManager.literal("jobmoney").executes(context -> {
							JobTrackerState.resetMoneyTracker();
							notifyReset("jobmoney");
							return 1;
						}))
						.then(ClientCommandManager.literal("fishmoney").executes(context -> {
							FishbagTotalState.resetMoneyTrackerForCommand();
							notifyReset("fishmoney");
							return 1;
						}))
					)
			);
		});
	}

	@Override
	public void postInit(Bootstrap ctx) {
	}

	private static Component msg(Messages.Id id, Object... args) {
		return Component.literal(Messages.applyColorCodes(Messages.get(id, args)));
	}

	private static void notifyReset(String id) {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null) {
			return;
		}
		client.player.displayClientMessage(
			Component.literal("§7[PT] §aReset tracker: §f" + (id == null ? "" : id)),
			false
		);
	}

	private static void printHeldItemDetails() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		ItemStack stack = client.player.getMainHandItem();
		if (stack == null || stack.isEmpty()) {
			client.player.displayClientMessage(msg(Messages.Id.CHECKITEM_NO_HAND), false);
			return;
		}

		client.player.displayClientMessage(msg(Messages.Id.CHECKITEM_DUMP_HEADER), false);
		client.player.displayClientMessage(
			msg(Messages.Id.CHECKITEM_ID, BuiltInRegistries.ITEM.getKey(stack.getItem())),
			false
		);
		client.player.displayClientMessage(
			msg(Messages.Id.CHECKITEM_COUNT, stack.getCount()),
			false
		);
		client.player.displayClientMessage(
			msg(
				Messages.Id.CHECKITEM_DAMAGE,
				stack.getDamageValue(),
				stack.getMaxDamage(),
				stack.isDamageableItem()
			),
			false
		);
		client.player.displayClientMessage(
			msg(
				Messages.Id.CHECKITEM_RARITY,
				stack.getRarity(),
				stack.isEnchanted(),
				stack.hasFoil()
			),
			false
		);
		client.player.displayClientMessage(
			msg(Messages.Id.CHECKITEM_NAME, stack.getHoverName().getString()),
			false
		);

		printComponents(client, stack);
		printCustomData(client, stack);
		printTooltip(client, stack);
		printSavedStatus(client, stack);
	}

	private static CompoundTag getCustomDataTag(ItemStack stack) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		return customData == null ? null : customData.copyTag();
	}

	private static void saveHeldItem() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		ItemStack stack = client.player.getMainHandItem();
		if (stack == null || stack.isEmpty()) {
			client.player.displayClientMessage(msg(Messages.Id.CHECKITEM_NO_HAND), false);
			return;
		}
		if (!isDifferentiableForSave(stack)) {
			client.player.displayClientMessage(msg(Messages.Id.CHECKITEM_SAVE_DENIED), false);
			return;
		}
		SavedItemsConfig config = ensureSavedConfig();
		String savedUuid = getSavedUuid(stack);
		if (savedUuid == null) {
			savedUuid = UUID.randomUUID().toString();
		}
		CompoundTag tag = getCustomDataTag(stack);
		if (tag == null) {
			tag = new CompoundTag();
		}
		tag.putString(SAVED_KEY, savedUuid);
		CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
		SavedItemsConfig.SavedItemEntry entry = upsertSavedEntry(config, stack, savedUuid);
		applyLocalLore(stack, entry.id);
		int slot = findSlotForStack(client.player, stack);
		if (slot != -1) {
			updateEntrySlot(entry, slot);
		}
		saveSavedConfigIfDirty();
		client.player.displayClientMessage(
			msg(Messages.Id.CHECKITEM_ITEM_SAVED, savedUuid),
			false
		);
	}

	private static void unsaveHeldItem() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		ItemStack stack = client.player.getMainHandItem();
		if (stack == null || stack.isEmpty()) {
			client.player.displayClientMessage(msg(Messages.Id.CHECKITEM_NO_HAND), false);
			return;
		}
		SavedItemsConfig config = ensureSavedConfig();
		String savedUuid = getSavedUuid(stack);
		CompoundTag tag = getCustomDataTag(stack);
		if (tag != null && !tag.isEmpty()) {
			tag.remove(SAVED_KEY);
			CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
		}
		removeLocalMarkers(stack);
		if (removeSavedEntry(config, stack, savedUuid)) {
			saveSavedConfigIfDirty();
		}
		client.player.displayClientMessage(msg(Messages.Id.CHECKITEM_ITEM_UNSAVED), false);
	}

	private static void printComponents(Minecraft client, ItemStack stack) {
		DataComponentMap components = stack.getComponents();
		client.player.displayClientMessage(
			msg(Messages.Id.CHECKITEM_COMPONENTS_HEADER, components.size()),
			false
		);
		if (components.isEmpty()) {
			client.player.displayClientMessage(msg(Messages.Id.CHECKITEM_COMPONENTS_EMPTY), false);
			return;
		}
		for (DataComponentType<?> type : components.keySet()) {
			Object value = components.get(type);
			String typeId = componentId(type);
			client.player.displayClientMessage(
				msg(Messages.Id.CHECKITEM_COMPONENTS_ENTRY, typeId, formatComponentValue(value)),
				false
			);
		}
	}

	private static void printCustomData(Minecraft client, ItemStack stack) {
		CompoundTag tag = getCustomDataTag(stack);
		if (tag == null || tag.isEmpty()) {
			client.player.displayClientMessage(msg(Messages.Id.CHECKITEM_CUSTOM_EMPTY), false);
			return;
		}

		client.player.displayClientMessage(msg(Messages.Id.CHECKITEM_CUSTOM_HEADER), false);
		client.player.displayClientMessage(
			msg(Messages.Id.CHECKITEM_CUSTOM_RAW, tag.toString()),
			false
		);

		List<String> found = new ArrayList<>(4);
		collectUuids(tag, "", found);
		if (!found.isEmpty()) {
			client.player.displayClientMessage(msg(Messages.Id.CHECKITEM_UUID_HEADER), false);
			for (int i = 0; i < found.size(); i++) {
				client.player.displayClientMessage(
					msg(Messages.Id.CHECKITEM_UUID_ENTRY, found.get(i)),
					false
				);
			}
		}
	}

	private static void printTooltip(Minecraft client, ItemStack stack) {
		Item.TooltipContext context = client.level != null ? Item.TooltipContext.of(client.level) : Item.TooltipContext.EMPTY;
		List<Component> tooltip = stack.getTooltipLines(context, client.player, TooltipFlag.Default.ADVANCED);
		client.player.displayClientMessage(
			msg(Messages.Id.CHECKITEM_TOOLTIP_HEADER, tooltip.size()),
			false
		);
		for (int i = 0; i < tooltip.size(); i++) {
			Component line = tooltip.get(i);
			client.player.displayClientMessage(line, false);
		}
	}

	private static void printSavedStatus(Minecraft client, ItemStack stack) {
		boolean saved = isSelectedHotbarSlotSaved(client.player);
		client.player.displayClientMessage(
			msg(saved ? Messages.Id.CHECKITEM_SAVED : Messages.Id.CHECKITEM_NOT_SAVED),
			false
		);
	}

	public static boolean isDebugSyncEnabled() {
		return debugSyncEnabled;
	}

	public static boolean isSlotLockingEnabled() {
		return slotLockingEnabled;
	}

	public static void setSlotLockingEnabled(boolean enabled) {
		slotLockingEnabled = enabled;
	}

	public static boolean toggleSlotLocking() {
		slotLockingEnabled = !slotLockingEnabled;
		return slotLockingEnabled;
	}

	public static boolean toggleDebugSync() {
		debugSyncEnabled = !debugSyncEnabled;
		return debugSyncEnabled;
	}

	private static void toggleDebugSyncAndNotify() {
		debugSyncEnabled = !debugSyncEnabled;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		String state = debugSyncEnabled
			? Messages.get(Messages.Id.STATE_ON)
			: Messages.get(Messages.Id.STATE_OFF);
		client.player.displayClientMessage(
			msg(Messages.Id.DEBUG_SYNC_STATE, state),
			false
		);
	}

	public static boolean isItemSaveEnabled() {
		return ITEM_SAVE_ENABLED;
	}

	public static boolean isSlotSaved(Player player, Slot slot) {
		if (player == null || slot == null) {
			return false;
		}
		if (!isPlayerInventorySlot(player, slot)) {
			return false;
		}
		return isSlotIndexSaved(player, slot.getContainerSlot());
	}

	public static boolean isSelectedHotbarSlotSaved(Player player) {
		if (player == null) {
			return false;
		}
		ItemStack stack = player.getMainHandItem();
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		int size = player.getInventory().getContainerSize();
		for (int i = 0; i < size; i++) {
			ItemStack current = player.getInventory().getItem(i);
			if (current == stack) {
				return isSlotIndexSaved(player, i);
			}
		}
		return false;
	}

	public static boolean isStackInSavedSlot(Player player, ItemStack stack) {
		if (player == null || stack == null || stack.isEmpty()) {
			return false;
		}
		int size = player.getInventory().getContainerSize();
		for (int i = 0; i < size; i++) {
			ItemStack current = player.getInventory().getItem(i);
			if (current == stack) {
				return isSlotIndexSaved(player, i);
			}
		}
		return false;
	}

	public static boolean toggleSlotSaved(Player player, Slot slot) {
		if (player == null || slot == null) {
			return false;
		}
		if (!isPlayerInventorySlot(player, slot)) {
			return false;
		}
		int index = slot.getContainerSlot();
		if (index < 0) {
			return false;
		}
		SavedSlotsConfig config = ensureSavedSlots();
		boolean nowSaved;
		if (removeSlot(config, index)) {
			nowSaved = false;
		} else {
			addSlot(config, index);
			nowSaved = true;
		}
		savedSlotsDirty = true;
		saveSavedSlotsIfDirty();
		player.displayClientMessage(
			msg(nowSaved ? Messages.Id.SLOT_LOCK : Messages.Id.SLOT_UNLOCK),
			false
		);
		return true;
	}

	private static boolean isSlotIndexSaved(Player player, int slotIndex) {
		if (player == null) {
			return false;
		}
		if (slotIndex < 0) {
			return false;
		}
		SavedSlotsConfig config = ensureSavedSlots();
		List<Integer> slots = config.slots;
		for (int i = 0; i < slots.size(); i++) {
			Integer value = slots.get(i);
			if (value != null && value == slotIndex) {
				return true;
			}
		}
		return false;
	}

	private static void addSlot(SavedSlotsConfig config, int slotIndex) {
		List<Integer> slots = config.slots;
		for (int i = 0; i < slots.size(); i++) {
			Integer value = slots.get(i);
			if (value != null && value == slotIndex) {
				return;
			}
		}
		slots.add(slotIndex);
	}

	private static boolean removeSlot(SavedSlotsConfig config, int slotIndex) {
		List<Integer> slots = config.slots;
		for (int i = slots.size() - 1; i >= 0; i--) {
			Integer value = slots.get(i);
			if (value != null && value == slotIndex) {
				slots.remove(i);
				return true;
			}
		}
		return false;
	}

	private static boolean isPlayerInventorySlot(Player player, Slot slot) {
		return slot.container == player.getInventory();
	}

	public static boolean isItemSaved(ItemStack stack) {
		if (!ITEM_SAVE_ENABLED) {
			return false;
		}
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		if (!isDifferentiableForSave(stack)) {
			removeLocalMarkers(stack);
			return false;
		}
		SavedItemsConfig config = ensureSavedConfig();
		String tagUuid = getSavedUuid(stack);
		if (tagUuid != null) {
			SavedItemsConfig.SavedItemEntry entry = upsertSavedEntry(config, stack, tagUuid);
			applyLocalLore(stack, entry.id);
			saveSavedConfigIfDirty();
			return true;
		}
		String signature = strongSignature(stack);
		SavedItemsConfig.SavedItemEntry entry = findBySignature(config, signature);
		if (entry != null) {
			applyLocalLore(stack, entry.id);
			return true;
		}
		String weakSignature = weakSignature(stack, signature);
		String portableSignature = portableSignature(stack);
		entry = findByWeakSignature(config, weakSignature);
		if (entry != null) {
			entry.itemId = itemId(stack);
			entry.strongSignature = signature;
			entry.weakSignature = weakSignature;
			entry.portableSignature = portableSignature;
			savedItemsDirty = true;
			applyLocalLore(stack, entry.id);
			saveSavedConfigIfDirty();
			return true;
		}
		entry = findByPortableSignature(config, portableSignature);
		if (entry != null) {
			entry.itemId = itemId(stack);
			entry.strongSignature = signature;
			entry.weakSignature = weakSignature;
			entry.portableSignature = portableSignature;
			savedItemsDirty = true;
			applyLocalLore(stack, entry.id);
			saveSavedConfigIfDirty();
			return true;
		}
		String markerId = extractMarkerId(stack);
		if (markerId != null) {
			SavedItemsConfig.SavedItemEntry restored = upsertSavedEntry(config, stack, markerId);
			applyLocalLore(stack, restored.id);
			saveSavedConfigIfDirty();
			return true;
		}
		return false;
	}

	private static String getSavedUuid(ItemStack stack) {
		CompoundTag tag = getCustomDataTag(stack);
		if (tag == null || tag.isEmpty()) {
			return null;
		}
		String value = tag.getStringOr(SAVED_KEY, "");
		return value.isEmpty() ? null : value;
	}

	private static SavedItemsConfig ensureSavedConfig() {
		if (savedItems == null) {
			savedItems = ClientConfigIO.loadSavedItems();
			savedItemsDirty = false;
			repairSavedItems(savedItems);
			saveSavedConfigIfDirty();
		}
		return savedItems;
	}

	private static void saveSavedConfigIfDirty() {
		if (savedItems != null && savedItemsDirty) {
			ClientConfigIO.saveSavedItems(savedItems);
			savedItemsDirty = false;
		}
	}

	private static SavedSlotsConfig ensureSavedSlots() {
		if (savedSlots == null) {
			savedSlots = ClientConfigIO.loadSavedSlots();
			savedSlotsDirty = false;
		}
		return savedSlots;
	}

	private static void saveSavedSlotsIfDirty() {
		if (savedSlots != null && savedSlotsDirty) {
			ClientConfigIO.saveSavedSlots(savedSlots);
			savedSlotsDirty = false;
		}
	}

	private static void reloadSavedSlots() {
		savedSlots = ClientConfigIO.loadSavedSlots();
		savedSlotsDirty = false;
	}

	private static void saveSavedSlotsOnDisconnect() {
		if (savedSlots != null) {
			ClientConfigIO.saveSavedSlots(savedSlots);
			savedSlotsDirty = false;
		}
	}

	private static ContainerCacheConfig ensureContainerCache() {
		if (containerCache == null) {
			containerCache = ClientConfigIO.loadContainerCache();
			containerCacheDirty = false;
			repairContainerCache(containerCache);
			saveContainerCacheIfDirty();
		}
		return containerCache;
	}

	private static void saveContainerCacheIfDirty() {
		if (containerCache != null && containerCacheDirty) {
			ClientConfigIO.saveContainerCache(containerCache);
			containerCacheDirty = false;
		}
	}

	private static void repairContainerCache(ContainerCacheConfig config) {
		if (config == null || config.entries == null) {
			return;
		}
		long cutoff = System.currentTimeMillis() - CONTAINER_CACHE_TTL_MS;
		for (int i = config.entries.size() - 1; i >= 0; i--) {
			ContainerCacheConfig.ContainerCacheEntry entry = config.entries.get(i);
			if (entry == null) {
				config.entries.remove(i);
				containerCacheDirty = true;
				continue;
			}
			entry.itemId = normalizeText(entry.itemId);
			entry.signature = normalizeText(entry.signature);
			entry.savedId = normalizeText(entry.savedId);
			if (entry.itemId == null || entry.signature == null || entry.savedId == null) {
				config.entries.remove(i);
				containerCacheDirty = true;
				continue;
			}
			if (entry.lastSeenMs <= 0L || entry.lastSeenMs < cutoff) {
				config.entries.remove(i);
				containerCacheDirty = true;
			}
		}
	}

	private static void reloadSavedItems() {
		savedItems = ClientConfigIO.loadSavedItems();
		savedItemsDirty = false;
		repairSavedItems(savedItems);
		saveSavedConfigIfDirty();
	}

	private static void saveSavedItemsOnDisconnect() {
		if (savedItems != null) {
			ClientConfigIO.saveSavedItems(savedItems);
			savedItemsDirty = false;
		}
	}

	private static void refreshSavedFromInventory(Minecraft client) {
		if (!ITEM_SAVE_ENABLED) {
			return;
		}
		if (client == null || client.player == null) {
			return;
		}
		SavedItemsConfig config = ensureSavedConfig();
		Player player = client.player;
		refreshInventoryList(config, player);
		refreshMenuSlots(player.containerMenu);
		saveSavedConfigIfDirty();
	}

	private static void refreshInventoryList(SavedItemsConfig config, Player player) {
		int size = player.getInventory().getContainerSize();
		for (int i = 0; i < size; i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack == null || stack.isEmpty() || !isDifferentiableForSave(stack)) {
				continue;
			}
			String signature = strongSignature(stack);
			String weakSignature = weakSignature(stack, signature);
			String portableSignature = portableSignature(stack);
			SavedItemsConfig.SavedItemEntry entry = findBySignature(config, signature);
			if (entry != null) {
				applyLocalLore(stack, entry.id);
				updateEntrySlot(entry, i);
				continue;
			}
			entry = findByWeakSignature(config, weakSignature);
			if (entry != null) {
				entry.itemId = itemId(stack);
				entry.strongSignature = signature;
				entry.weakSignature = weakSignature;
				entry.portableSignature = portableSignature;
				savedItemsDirty = true;
				applyLocalLore(stack, entry.id);
				updateEntrySlot(entry, i);
				continue;
			}
			entry = findByPortableSignature(config, portableSignature);
			if (entry != null) {
				entry.itemId = itemId(stack);
				entry.strongSignature = signature;
				entry.weakSignature = weakSignature;
				entry.portableSignature = portableSignature;
				savedItemsDirty = true;
				applyLocalLore(stack, entry.id);
				updateEntrySlot(entry, i);
				continue;
			}
			String markerId = extractMarkerId(stack);
			if (markerId != null) {
				entry = upsertSavedEntry(config, stack, markerId);
				applyLocalLore(stack, entry.id);
				updateEntrySlot(entry, i);
				continue;
			}
			entry = findBySlotFallback(config, stack, i);
			if (entry != null) {
				entry.itemId = itemId(stack);
				entry.strongSignature = signature;
				savedItemsDirty = true;
				applyLocalLore(stack, entry.id);
				updateEntrySlot(entry, i);
			}
		}
	}

	public static void refreshMenuSlots(AbstractContainerMenu menu) {
		if (!ITEM_SAVE_ENABLED) {
			return;
		}
		if (menu == null || menu.slots == null) {
			return;
		}
		SavedItemsConfig config = ensureSavedConfig();
		ContainerCacheConfig cache = ensureContainerCache();
		for (int i = 0; i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			if (slot == null) {
				continue;
			}
			ItemStack stack = slot.getItem();
			if (stack == null || stack.isEmpty() || !isDifferentiableForSave(stack)) {
				continue;
			}
			long cacheKey = menuCacheKey(menu, i);
			String signature = strongSignature(stack);
			String weakSignature = weakSignature(stack, signature);
			String portableSignature = portableSignature(stack);
			SavedItemsConfig.SavedItemEntry entry = findBySignature(config, signature);
			if (entry != null) {
				applyLocalLore(stack, entry.id);
				cacheMenuSlot(cacheKey, stack, portableSignature, entry.id);
				updateContainerCache(cache, stack, portableSignature, entry.id);
				continue;
			}
			entry = findByWeakSignature(config, weakSignature);
			if (entry != null) {
				entry.itemId = itemId(stack);
				entry.strongSignature = signature;
				entry.weakSignature = weakSignature;
				entry.portableSignature = portableSignature;
				savedItemsDirty = true;
				applyLocalLore(stack, entry.id);
				cacheMenuSlot(cacheKey, stack, portableSignature, entry.id);
				updateContainerCache(cache, stack, portableSignature, entry.id);
				continue;
			}
			entry = findByPortableSignature(config, portableSignature);
			if (entry != null) {
				entry.itemId = itemId(stack);
				entry.strongSignature = signature;
				entry.weakSignature = weakSignature;
				entry.portableSignature = portableSignature;
				savedItemsDirty = true;
				applyLocalLore(stack, entry.id);
				cacheMenuSlot(cacheKey, stack, portableSignature, entry.id);
				updateContainerCache(cache, stack, portableSignature, entry.id);
				continue;
			}
			String markerId = extractMarkerId(stack);
			if (markerId != null) {
				entry = upsertSavedEntry(config, stack, markerId);
				applyLocalLore(stack, entry.id);
				cacheMenuSlot(cacheKey, stack, portableSignature, entry.id);
				updateContainerCache(cache, stack, portableSignature, entry.id);
				continue;
			}
			MenuSlotCache cached = menuSlotCache.get(cacheKey);
			if (cached != null && cached.isValid(clientTickCounter) && cached.matches(stack, portableSignature)) {
				applyLocalLore(stack, cached.savedId);
				updateContainerCache(cache, stack, portableSignature, cached.savedId);
				continue;
			}
			ContainerCacheConfig.ContainerCacheEntry persisted = findContainerCache(cache, stack, portableSignature);
			if (persisted != null) {
				applyLocalLore(stack, persisted.savedId);
			}
		}
		saveSavedConfigIfDirty();
		saveContainerCacheIfDirty();
	}

	public static void clearMenuCache(AbstractContainerMenu menu) {
		if (!ITEM_SAVE_ENABLED) {
			return;
		}
		if (menu == null) {
			return;
		}
		long menuId = ((long) menu.containerId) << 32;
		Iterator<Map.Entry<Long, MenuSlotCache>> it = menuSlotCache.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Long, MenuSlotCache> entry = it.next();
			if ((entry.getKey() & 0xFFFFFFFF00000000L) == menuId) {
				it.remove();
			}
		}
	}

	private static long menuCacheKey(AbstractContainerMenu menu, int slotIndex) {
		return (((long) menu.containerId) << 32) | (slotIndex & 0xFFFFFFFFL);
	}

	private static void cacheMenuSlot(long key, ItemStack stack, String portableSignature, String savedId) {
		if (savedId == null || savedId.isEmpty()) {
			return;
		}
		MenuSlotCache cache = menuSlotCache.get(key);
		if (cache == null) {
			cache = new MenuSlotCache();
			menuSlotCache.put(key, cache);
		}
		cache.itemId = itemId(stack);
		cache.portableSignature = portableSignature;
		cache.savedId = savedId;
		cache.lastTick = clientTickCounter;
	}

	private static ContainerCacheConfig.ContainerCacheEntry findContainerCache(ContainerCacheConfig cache, ItemStack stack, String portableSignature) {
		if (cache == null || cache.entries == null) {
			return null;
		}
		String itemId = itemId(stack);
		for (int i = 0; i < cache.entries.size(); i++) {
			ContainerCacheConfig.ContainerCacheEntry entry = cache.entries.get(i);
			if (entry == null) {
				continue;
			}
			if (itemId.equals(entry.itemId) && portableSignature.equals(entry.signature)) {
				entry.lastSeenMs = System.currentTimeMillis();
				containerCacheDirty = true;
				return entry;
			}
		}
		return null;
	}

	private static void updateContainerCache(ContainerCacheConfig cache, ItemStack stack, String portableSignature, String savedId) {
		if (cache == null || cache.entries == null || savedId == null || savedId.isEmpty()) {
			return;
		}
		String itemId = itemId(stack);
		for (int i = 0; i < cache.entries.size(); i++) {
			ContainerCacheConfig.ContainerCacheEntry entry = cache.entries.get(i);
			if (entry == null) {
				continue;
			}
			if (itemId.equals(entry.itemId) && portableSignature.equals(entry.signature)) {
				if (!savedId.equals(entry.savedId)) {
					entry.savedId = savedId;
					containerCacheDirty = true;
				}
				entry.lastSeenMs = System.currentTimeMillis();
				containerCacheDirty = true;
				return;
			}
		}
		ContainerCacheConfig.ContainerCacheEntry entry = new ContainerCacheConfig.ContainerCacheEntry();
		entry.itemId = itemId;
		entry.signature = portableSignature;
		entry.savedId = savedId;
		entry.lastSeenMs = System.currentTimeMillis();
		cache.entries.add(entry);
		containerCacheDirty = true;
	}

	private static void cleanupContainerCacheIfNeeded() {
		if (containerCache == null || containerCache.entries == null) {
			return;
		}
		long cutoff = System.currentTimeMillis() - CONTAINER_CACHE_TTL_MS;
		for (int i = containerCache.entries.size() - 1; i >= 0; i--) {
			ContainerCacheConfig.ContainerCacheEntry entry = containerCache.entries.get(i);
			if (entry == null || entry.lastSeenMs < cutoff) {
				containerCache.entries.remove(i);
				containerCacheDirty = true;
			}
		}
	}

	private static void cleanupMenuCacheIfNeeded() {
		menuCacheCleanupCounter++;
		if (menuCacheCleanupCounter < MENU_CACHE_CLEANUP_INTERVAL) {
			return;
		}
		menuCacheCleanupCounter = 0;
		long cutoff = clientTickCounter - MENU_CACHE_TTL_TICKS;
		Iterator<Map.Entry<Long, MenuSlotCache>> it = menuSlotCache.entrySet().iterator();
		while (it.hasNext()) {
			MenuSlotCache cache = it.next().getValue();
			if (cache == null || cache.lastTick < cutoff) {
				it.remove();
			}
		}
	}

	private static final class MenuSlotCache {
		private String itemId;
		private String portableSignature;
		private String savedId;
		private long lastTick;

		private boolean matches(ItemStack stack, String portableSignature) {
			return itemId != null
				&& itemId.equals(itemId(stack))
				&& this.portableSignature != null
				&& this.portableSignature.equals(portableSignature);
		}

		private boolean isValid(long nowTick) {
			return nowTick - lastTick <= MENU_CACHE_TTL_TICKS;
		}
	}

	private static SavedItemsConfig.SavedItemEntry findBySlotFallback(SavedItemsConfig config, ItemStack stack, int slot) {
		return null;
	}

	private static void updateEntrySlot(SavedItemsConfig.SavedItemEntry entry, int slot) {
		if (entry == null) {
			return;
		}
		if (!entry.hasSlot || entry.lastSlot != slot) {
			entry.lastSlot = slot;
			entry.hasSlot = true;
			savedItemsDirty = true;
		}
	}

	private static int findSlotForStack(Player player, ItemStack stack) {
		if (player == null) {
			return -1;
		}
		int size = player.getInventory().getContainerSize();
		for (int i = 0; i < size; i++) {
			if (player.getInventory().getItem(i) == stack) {
				return i;
			}
		}
		return -1;
	}

	private static void repairSavedItems(SavedItemsConfig config) {
		if (config == null || config.entries == null) {
			return;
		}
		java.util.HashSet<String> seen = new java.util.HashSet<>();
		for (int i = config.entries.size() - 1; i >= 0; i--) {
			SavedItemsConfig.SavedItemEntry entry = config.entries.get(i);
			if (entry == null) {
				config.entries.remove(i);
				savedItemsDirty = true;
				continue;
			}
			entry.id = normalizeText(entry.id);
			entry.itemId = normalizeText(entry.itemId);
			entry.strongSignature = normalizeText(entry.strongSignature);
			entry.weakSignature = normalizeText(entry.weakSignature);
			entry.portableSignature = normalizeText(entry.portableSignature);
			if (entry.id == null || entry.itemId == null) {
				config.entries.remove(i);
				savedItemsDirty = true;
				continue;
			}
			if (!seen.add(entry.id)) {
				config.entries.remove(i);
				savedItemsDirty = true;
				continue;
			}
			if (entry.strongSignature == null) {
				entry.strongSignature = "";
				savedItemsDirty = true;
			}
			if (entry.weakSignature == null) {
				entry.weakSignature = normalizeSignature(entry.strongSignature);
				savedItemsDirty = true;
			}
			if (entry.portableSignature == null) {
				entry.portableSignature = portableSignature(entry.strongSignature);
				savedItemsDirty = true;
			}
			if (!entry.hasSlot) {
				if (entry.lastSlot != -1) {
					entry.lastSlot = -1;
					savedItemsDirty = true;
				}
			}
		}
	}

	private static String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static SavedItemsConfig.SavedItemEntry upsertSavedEntry(SavedItemsConfig config, ItemStack stack, String id) {
		String signature = strongSignature(stack);
		String weakSignature = weakSignature(stack, signature);
		String portableSignature = portableSignature(stack);
		SavedItemsConfig.SavedItemEntry entry = findBySignature(config, signature);
		if (entry == null && id != null) {
			entry = findById(config, id);
		}
		if (entry == null) {
			entry = new SavedItemsConfig.SavedItemEntry();
			entry.id = id != null ? id : UUID.randomUUID().toString();
			config.entries.add(entry);
			savedItemsDirty = true;
		}
		String itemId = itemId(stack);
		if (!itemId.equals(entry.itemId)) {
			entry.itemId = itemId;
			savedItemsDirty = true;
		}
		if (!signature.equals(entry.strongSignature)) {
			entry.strongSignature = signature;
			savedItemsDirty = true;
		}
		if (!weakSignature.equals(entry.weakSignature)) {
			entry.weakSignature = weakSignature;
			savedItemsDirty = true;
		}
		if (!portableSignature.equals(entry.portableSignature)) {
			entry.portableSignature = portableSignature;
			savedItemsDirty = true;
		}
		return entry;
	}

	private static boolean removeSavedEntry(SavedItemsConfig config, ItemStack stack, String id) {
		boolean removed = false;
		if (id != null && config.entries != null) {
			for (int i = config.entries.size() - 1; i >= 0; i--) {
				SavedItemsConfig.SavedItemEntry entry = config.entries.get(i);
				if (entry != null && id.equals(entry.id)) {
					config.entries.remove(i);
					removed = true;
					savedItemsDirty = true;
				}
			}
		}
		String signature = strongSignature(stack);
		for (int i = config.entries.size() - 1; i >= 0; i--) {
			SavedItemsConfig.SavedItemEntry entry = config.entries.get(i);
			if (entry != null && signature.equals(entry.strongSignature)) {
				config.entries.remove(i);
				removed = true;
				savedItemsDirty = true;
			}
		}
		return removed;
	}

	private static SavedItemsConfig.SavedItemEntry findBySignature(SavedItemsConfig config, String signature) {
		if (config.entries == null) {
			return null;
		}
		for (int i = 0; i < config.entries.size(); i++) {
			SavedItemsConfig.SavedItemEntry entry = config.entries.get(i);
			if (entry != null && signature.equals(entry.strongSignature)) {
				return entry;
			}
		}
		return null;
	}

	private static SavedItemsConfig.SavedItemEntry findByWeakSignature(SavedItemsConfig config, String weakSignature) {
		if (config.entries == null) {
			return null;
		}
		for (int i = 0; i < config.entries.size(); i++) {
			SavedItemsConfig.SavedItemEntry entry = config.entries.get(i);
			if (entry != null && weakSignature.equals(entry.weakSignature)) {
				return entry;
			}
		}
		return null;
	}

	private static SavedItemsConfig.SavedItemEntry findByPortableSignature(SavedItemsConfig config, String portableSignature) {
		if (config.entries == null) {
			return null;
		}
		for (int i = 0; i < config.entries.size(); i++) {
			SavedItemsConfig.SavedItemEntry entry = config.entries.get(i);
			if (entry != null && portableSignature.equals(entry.portableSignature)) {
				return entry;
			}
		}
		return null;
	}

	private static SavedItemsConfig.SavedItemEntry findById(SavedItemsConfig config, String id) {
		if (config.entries == null) {
			return null;
		}
		for (int i = 0; i < config.entries.size(); i++) {
			SavedItemsConfig.SavedItemEntry entry = config.entries.get(i);
			if (entry != null && id.equals(entry.id)) {
				return entry;
			}
		}
		return null;
	}

	private static String itemId(ItemStack stack) {
		return String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
	}

	private static String strongSignature(ItemStack stack) {
		DataComponentMap components = stack.getComponents();
		int size = components.size();
		DataComponentType<?>[] types = new DataComponentType[size];
		int index = 0;
		for (DataComponentType<?> type : components.keySet()) {
			types[index++] = type;
		}
		java.util.Arrays.sort(types, java.util.Comparator.comparing(CheckItemClientModule::componentId));
		StringBuilder builder = new StringBuilder(256);
		builder.append(itemId(stack)).append('|');
		for (int i = 0; i < types.length; i++) {
			DataComponentType<?> type = types[i];
			if (type == null) {
				continue;
			}
			if (type == DataComponents.DAMAGE) {
				continue;
			}
			String typeId = componentId(type);
			Object value = components.get(type);
			if (type == DataComponents.LORE && value instanceof net.minecraft.world.item.component.ItemLore lore) {
				String loreValue = formatLoreValue(lore);
				if (!loreValue.isEmpty()) {
					builder.append(typeId).append('=').append(loreValue).append(';');
				}
				continue;
			}
			if (type == DataComponents.CUSTOM_DATA && value instanceof CustomData customData) {
				CompoundTag tag = customData.copyTag();
				tag.remove(SAVED_KEY);
				if (!tag.isEmpty()) {
					builder.append(typeId).append('=').append(tag.toString()).append(';');
				}
				continue;
			}
			builder.append(typeId).append('=').append(formatComponentValue(value)).append(';');
		}
		return builder.toString();
	}

	private static String weakSignature(ItemStack stack, String strongSignature) {
		if (strongSignature == null || strongSignature.isEmpty()) {
			return strongSignature(stack);
		}
		return normalizeSignature(strongSignature);
	}

	private static String portableSignature(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder(256);
		builder.append(itemId(stack)).append('|');
		Component customName = stack.get(DataComponents.CUSTOM_NAME);
		if (customName != null) {
			builder.append("name=").append(serializeComponent(customName)).append(';');
		}
		net.minecraft.world.item.component.ItemLore lore = stack.get(DataComponents.LORE);
		if (lore != null) {
			String loreValue = formatLoreValue(lore);
			if (!loreValue.isEmpty()) {
				builder.append("lore=").append(loreValue).append(';');
			}
		}
		Object enchants = stack.get(DataComponents.ENCHANTMENTS);
		if (enchants != null) {
			builder.append("ench=").append(formatComponentValue(enchants)).append(';');
		}
		Object stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
		if (stored != null) {
			builder.append("stored=").append(formatComponentValue(stored)).append(';');
		}
		return builder.toString();
	}

	private static String portableSignature(String strongSignature) {
		if (strongSignature == null || strongSignature.isEmpty()) {
			return "";
		}
		return filterSignatureKeys(strongSignature, "custom_name", "lore", "enchantments", "stored_enchantments");
	}

	private static String normalizeSignature(String signature) {
		if (signature == null || signature.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder(signature.length());
		int pipe = signature.indexOf('|');
		if (pipe < 0) {
			return signature;
		}
		builder.append(signature, 0, pipe + 1);
		int start = pipe + 1;
		while (start < signature.length()) {
			int end = signature.indexOf(';', start);
			if (end < 0) {
				end = signature.length();
			}
			if (end == start) {
				start = end + 1;
				continue;
			}
			String segment = signature.substring(start, end);
			int eq = segment.indexOf('=');
			if (eq > 0) {
				String key = segment.substring(0, eq);
				if (!key.endsWith(":custom_data")) {
					builder.append(segment).append(';');
				}
			} else {
				builder.append(segment).append(';');
			}
			start = end + 1;
		}
		return builder.toString();
	}

	private static String filterSignatureKeys(String signature, String... allowedSuffixes) {
		if (signature == null || signature.isEmpty()) {
			return "";
		}
		int pipe = signature.indexOf('|');
		if (pipe < 0) {
			return signature;
		}
		StringBuilder builder = new StringBuilder(signature.length());
		builder.append(signature, 0, pipe + 1);
		int start = pipe + 1;
		while (start < signature.length()) {
			int end = signature.indexOf(';', start);
			if (end < 0) {
				end = signature.length();
			}
			if (end == start) {
				start = end + 1;
				continue;
			}
			String segment = signature.substring(start, end);
			int eq = segment.indexOf('=');
			if (eq > 0) {
				String key = segment.substring(0, eq);
				if (matchesSuffix(key, allowedSuffixes)) {
					builder.append(segment).append(';');
				}
			}
			start = end + 1;
		}
		return builder.toString();
	}

	private static boolean matchesSuffix(String key, String... allowedSuffixes) {
		for (int i = 0; i < allowedSuffixes.length; i++) {
			String suffix = allowedSuffixes[i];
			if (key.endsWith(":" + suffix) || key.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}


	private static void applyLocalLore(ItemStack stack, String id) {
		if (id == null || id.isEmpty()) {
			return;
		}
		net.minecraft.world.item.component.ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) {
			lore = net.minecraft.world.item.component.ItemLore.EMPTY;
		}
		if (hasLocalMarker(lore, id)) {
			return;
		}
		lore = stripLocalLore(lore);
		Component marker = Component.literal(LOCAL_LORE_PREFIX + id).withStyle(ChatFormatting.BLACK);
		java.util.ArrayList<Component> lines = new java.util.ArrayList<>(lore.lines().size() + 1);
		lines.add(marker);
		lines.addAll(lore.lines());
		int max = net.minecraft.world.item.component.ItemLore.MAX_LINES;
		if (lines.size() > max) {
			lines.subList(max, lines.size()).clear();
		}
		stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lines));
	}

	private static void removeLocalLore(ItemStack stack, String id) {
		if (id == null || id.isEmpty()) {
			return;
		}
		net.minecraft.world.item.component.ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null || lore.lines().isEmpty()) {
			return;
		}
		String target = LOCAL_LORE_PREFIX + id;
		java.util.ArrayList<Component> filtered = new java.util.ArrayList<>(lore.lines().size());
		boolean removed = false;
		for (int i = 0; i < lore.lines().size(); i++) {
			Component line = lore.lines().get(i);
			if (line != null && target.equals(line.getString())) {
				removed = true;
				continue;
			}
			if (line != null && LOCAL_LORE_VISIBLE.equals(normalizeLoreText(line.getString()))) {
				removed = true;
				continue;
			}
			filtered.add(line);
		}
		if (!removed) {
			return;
		}
		if (filtered.isEmpty()) {
			stack.remove(DataComponents.LORE);
			return;
		}
		stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(filtered));
	}

	private static void removeLocalMarkers(ItemStack stack) {
		net.minecraft.world.item.component.ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null || lore.lines().isEmpty()) {
			return;
		}
		java.util.ArrayList<Component> filtered = new java.util.ArrayList<>(lore.lines().size());
		boolean removed = false;
		for (int i = 0; i < lore.lines().size(); i++) {
			Component line = lore.lines().get(i);
			if (line != null) {
				String text = normalizeLoreText(line.getString());
				if (text.startsWith(LOCAL_LORE_PREFIX) || LOCAL_LORE_VISIBLE.equals(text)) {
					removed = true;
					continue;
				}
			}
			filtered.add(line);
		}
		if (!removed) {
			return;
		}
		if (filtered.isEmpty()) {
			stack.remove(DataComponents.LORE);
			return;
		}
		stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(filtered));
	}

	private static net.minecraft.world.item.component.ItemLore stripLocalLore(net.minecraft.world.item.component.ItemLore lore) {
		if (lore == null || lore.lines().isEmpty()) {
			return lore;
		}
		String target = LOCAL_LORE_PREFIX;
		java.util.ArrayList<Component> filtered = new java.util.ArrayList<>(lore.lines().size());
		boolean removed = false;
		for (int i = 0; i < lore.lines().size(); i++) {
			Component line = lore.lines().get(i);
			if (line != null) {
				String text = normalizeLoreText(line.getString());
				if (text.startsWith(target) || LOCAL_LORE_VISIBLE.equals(text)) {
					removed = true;
					continue;
				}
			}
			filtered.add(line);
		}
		if (!removed) {
			return lore;
		}
		if (filtered.isEmpty()) {
			return net.minecraft.world.item.component.ItemLore.EMPTY;
		}
		return new net.minecraft.world.item.component.ItemLore(filtered);
	}

	private static boolean hasLocalMarker(net.minecraft.world.item.component.ItemLore lore, String id) {
		if (lore == null || lore.lines().isEmpty()) {
			return false;
		}
		String target = LOCAL_LORE_PREFIX + id;
		for (int i = 0; i < lore.lines().size(); i++) {
			Component line = lore.lines().get(i);
			if (line == null) {
				continue;
			}
			String text = normalizeLoreText(line.getString());
			if (target.equals(text)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasLocalMarker(ItemStack stack) {
		net.minecraft.world.item.component.ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null || lore.lines().isEmpty()) {
			return false;
		}
		for (int i = 0; i < lore.lines().size(); i++) {
			Component line = lore.lines().get(i);
			if (line == null) {
				continue;
			}
			String text = normalizeLoreText(line.getString());
			if (text.startsWith(LOCAL_LORE_PREFIX) || LOCAL_LORE_VISIBLE.equals(text)) {
				return true;
			}
		}
		return false;
	}

	private static String extractMarkerId(ItemStack stack) {
		net.minecraft.world.item.component.ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null || lore.lines().isEmpty()) {
			return null;
		}
		for (int i = 0; i < lore.lines().size(); i++) {
			Component line = lore.lines().get(i);
			if (line == null) {
				continue;
			}
			String text = normalizeLoreText(line.getString());
			if (text.startsWith(LOCAL_LORE_PREFIX)) {
				String id = text.substring(LOCAL_LORE_PREFIX.length()).trim();
				return id.isEmpty() ? null : id;
			}
		}
		return null;
	}

	private static String componentId(DataComponentType<?> type) {
		var key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
		return key == null ? String.valueOf(type) : key.toString();
	}

	private static String formatComponentValue(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof CustomData customData) {
			return customData.copyTag().toString();
		}
		if (value instanceof net.minecraft.world.item.component.ItemLore lore) {
			return formatLoreValue(lore);
		}
		if (value instanceof Component component) {
			return serializeComponent(component);
		}
		if (value instanceof int[] ints) {
			return Arrays.toString(ints);
		}
		if (value instanceof long[] longs) {
			return Arrays.toString(longs);
		}
		if (value instanceof byte[] bytes) {
			return Arrays.toString(bytes);
		}
		return String.valueOf(value);
	}

	private static String formatLoreValue(net.minecraft.world.item.component.ItemLore lore) {
		if (lore == null || lore.lines().isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder(128);
		for (int i = 0; i < lore.lines().size(); i++) {
			Component line = lore.lines().get(i);
			if (line == null) {
				continue;
			}
			String text = normalizeLoreText(line.getString());
			if (text.startsWith(LOCAL_LORE_PREFIX) || LOCAL_LORE_VISIBLE.equals(text)) {
				continue;
			}
			builder.append(serializeComponent(line)).append('|');
		}
		return builder.toString();
	}

	private static String normalizeLoreText(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		if (text.indexOf('\u00A7') < 0) {
			return text;
		}
		StringBuilder builder = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\u00A7' && i + 1 < text.length()) {
				i++;
				continue;
			}
			builder.append(c);
		}
		return builder.toString();
	}

	public static Component savedTooltipLine() {
		return Component.literal(Messages.applyColorCodes(LOCAL_LORE_TEXT));
	}

	public static boolean isLocalMarkerLine(Component line) {
		if (line == null) {
			return false;
		}
		String text = normalizeLoreText(line.getString());
		return text.startsWith(LOCAL_LORE_PREFIX);
	}

	public static boolean isSavedTooltipLine(Component line) {
		if (line == null) {
			return false;
		}
		String text = normalizeLoreText(line.getString());
		return LOCAL_LORE_VISIBLE.equals(text);
	}

	private static boolean isDifferentiableForSave(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		if (stack.getCount() != 1) {
			return false;
		}
		return hasCustomNameComponent(stack) || hasMeaningfulLore(stack) || stack.isEnchanted();
	}

	private static boolean hasCustomNameComponent(ItemStack stack) {
		return stack.get(DataComponents.CUSTOM_NAME) != null;
	}

	private static String serializeComponent(Component component) {
		if (component == null) {
			return "";
		}
		return net.minecraft.network.chat.ComponentSerialization.CODEC
			.encodeStart(JsonOps.INSTANCE, component)
			.result()
			.map(Object::toString)
			.orElse(component.getString());
	}

	private static boolean hasMeaningfulLore(ItemStack stack) {
		net.minecraft.world.item.component.ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null || lore.lines().isEmpty()) {
			return false;
		}
		for (int i = 0; i < lore.lines().size(); i++) {
			Component line = lore.lines().get(i);
			if (line == null) {
				continue;
			}
			String text = normalizeLoreText(line.getString());
			if (text.isEmpty()) {
				continue;
			}
			if (text.startsWith(LOCAL_LORE_PREFIX) || LOCAL_LORE_VISIBLE.equals(text)) {
				continue;
			}
			return true;
		}
		return false;
	}

	private static void collectUuids(CompoundTag tag, String path, List<String> out) {
		if (tag == null || tag.isEmpty()) {
			return;
		}
		for (String key : tag.keySet()) {
			Tag value = tag.get(key);
			if (value == null) {
				continue;
			}
			String nextPath = path.isEmpty() ? key : path + "." + key;
			if (value instanceof CompoundTag child) {
				collectUuids(child, nextPath, out);
				continue;
			}
			if (value instanceof IntArrayTag intArrayTag) {
				String uuid = uuidFromIntArray(intArrayTag);
				if (uuid != null) {
					out.add(nextPath + " = " + uuid);
				}
				continue;
			}
			if (value instanceof StringTag stringTag) {
				String uuid = uuidFromStringTag(stringTag);
				if (uuid != null) {
					out.add(nextPath + " = " + uuid);
				}
				continue;
			}
			if (value instanceof ListTag list) {
				for (int i = 0; i < list.size(); i++) {
					Tag entry = list.get(i);
					if (entry instanceof CompoundTag childTag) {
						collectUuids(childTag, nextPath + "[" + i + "]", out);
					}
					if (entry instanceof IntArrayTag intArrayTag) {
						String uuid = uuidFromIntArray(intArrayTag);
						if (uuid != null) {
							out.add(nextPath + "[" + i + "] = " + uuid);
						}
					}
					if (entry instanceof StringTag stringTag) {
						String uuid = uuidFromStringTag(stringTag);
						if (uuid != null) {
							out.add(nextPath + "[" + i + "] = " + uuid);
						}
					}
				}
			}
		}
	}

	private static String uuidFromIntArray(IntArrayTag tag) {
		int[] values = tag.getAsIntArray();
		if (values == null || values.length != 4) {
			return null;
		}
		long msb = ((long) values[0] << 32) | (values[1] & 0xFFFFFFFFL);
		long lsb = ((long) values[2] << 32) | (values[3] & 0xFFFFFFFFL);
		return new UUID(msb, lsb).toString();
	}

	private static String uuidFromStringTag(StringTag tag) {
		java.util.Optional<String> value = tag.asString();
		if (value.isEmpty()) {
			return null;
		}
		try {
			return UUID.fromString(value.get()).toString();
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}
}



