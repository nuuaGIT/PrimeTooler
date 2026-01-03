package de.nuua.primetooler.features.primemenu.client;

import de.nuua.primetooler.core.config.ChatInputsConfig;
import de.nuua.primetooler.core.config.ClientSettingsConfig;
import de.nuua.primetooler.features.autospawn.client.AutoSpawnState;
import de.nuua.primetooler.api.v1.client.text.RainbowTextRenderer;
import de.nuua.primetooler.api.v1.client.text.RainbowTextStyle;
import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import de.nuua.primetooler.features.playermark.client.PlayerMarkRegistry;
import de.nuua.primetooler.features.camerazoom.client.CameraZoomState;
import de.nuua.primetooler.features.durabilityguard.client.DurabilityGuardState;
import de.nuua.primetooler.features.inventorycalc.client.InventoryCalculatorState;
import de.nuua.primetooler.features.locatorbar.client.LocatorBarState;
import de.nuua.primetooler.features.resourcepackguard.client.ResourcePackGuardState;
import de.nuua.primetooler.platform.config.ClientConfigIO;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public final class PrimeMenuScreen extends Screen {
	private static final ResourceLocation TAB_HEADER_BACKGROUND =
		ResourceLocation.withDefaultNamespace("textures/gui/tab_header_background.png");
	private static final Button.OnPress NOOP_PRESS = button -> {
	};
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final int GRID_COLUMNS = 2;
	private static final int GRID_ROWS = 3;
	private static final int CHAT_MESSAGES_MAX = 99;
	private static final int CHAT_ADD_BUTTON_WIDTH = 24;
	private static final int CHAT_FIELD_WIDTH = 240;
	private static final int CHAT_ACTION_WIDTH = 88;
	private static final int CHAT_TIMER_WIDTH = 72;
	private static final long CHAT_COOLDOWN_MS = 30L * 60L * 1000L;

	private final Screen parent;
	private TabManager tabManager;
	private TabNavigationBar tabNavigationBar;
	private int headerHeight;
	private ChatToolsTab chatToolsTab;

	public PrimeMenuScreen(Screen parent) {
		super(Component.literal("Prime Menu"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		tabManager = new TabManager(this::addTabWidget, this::removeWidget);
		chatToolsTab = new ChatToolsTab(ClientConfigIO.loadChatInputs(CHAT_MESSAGES_MAX));
		tabNavigationBar = TabNavigationBar.builder(tabManager, width)
			.addTabs(
				new PrimeMenuHomeTab(),
				new PlaceholderTab(Component.literal("World")),
				new PlaceholderTab(Component.literal("More")),
				new SpecialMembersTab(),
				chatToolsTab
			)
			.build();
		addRenderableWidget(tabNavigationBar);
		tabNavigationBar.selectTab(0, false);
		positionElements();
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		positionElements();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (tabNavigationBar != null && tabNavigationBar.keyPressed(event)) {
			return true;
		}
		if (chatToolsTab != null && tabManager != null && tabManager.getCurrentTab() == chatToolsTab) {
			if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
				|| event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
				chatToolsTab.handleEnter();
				return true;
			}
		}
		return super.keyPressed(event);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		if (chatToolsTab != null) {
			chatToolsTab.updateCooldowns();
		}
	}

	@Override
	public void onClose() {
		if (chatToolsTab != null) {
			chatToolsTab.saveInputs();
		}
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	@Override
	protected void renderMenuBackground(GuiGraphics graphics) {
		int header = Math.max(0, headerHeight);
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			TAB_HEADER_BACKGROUND,
			0,
			0,
			0.0f,
			0.0f,
			width,
			header,
			16,
			16
		);
		renderMenuBackground(graphics, 0, header, width, height);
	}

	private void addTabWidget(AbstractWidget widget) {
		widget.setTabOrderGroup(1);
		addRenderableWidget(widget);
	}

	private void positionElements() {
		if (tabNavigationBar == null) {
			return;
		}
		tabNavigationBar.setWidth(width);
		tabNavigationBar.arrangeElements();
		headerHeight = tabNavigationBar.getRectangle().bottom();

		ScreenRectangle tabArea = new ScreenRectangle(0, headerHeight, width, height - headerHeight);
		tabManager.setTabArea(tabArea);
	}

	private static final class PlaceholderTab extends GridLayoutTab {
		private PlaceholderTab(Component title) {
			super(title);
			layout.rowSpacing(8);
			layout.columnSpacing(12);

			for (int row = 0; row < GRID_ROWS; row++) {
				for (int col = 0; col < GRID_COLUMNS; col++) {
					layout.addChild(
						Button.builder(Component.empty(), NOOP_PRESS)
							.size(BUTTON_WIDTH, BUTTON_HEIGHT)
							.build(),
						row,
						col
					);
				}
			}
		}
	}

	private static final class PrimeMenuHomeTab extends GridLayoutTab {

		private PrimeMenuHomeTab() {
			super(Component.literal("Game"));
			layout.rowSpacing(8);
			layout.columnSpacing(12);

			Button[] toggleRef = new Button[1];
			toggleRef[0] = Button.builder(zoomLabel(CameraZoomState.isEnabled()), button -> {
				boolean enabled = CameraZoomState.toggleEnabled();
				toggleRef[0].setMessage(zoomLabel(enabled));
				saveClientSettings();
			}).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
			layout.addChild(toggleRef[0], 0, 0);

			Button[] durabilityRef = new Button[1];
			durabilityRef[0] = Button.builder(durabilityLabel(DurabilityGuardState.isEnabled()), button -> {
				boolean enabled = DurabilityGuardState.toggleEnabled();
				durabilityRef[0].setMessage(durabilityLabel(enabled));
				saveClientSettings();
			}).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
			layout.addChild(durabilityRef[0], 0, 1);

			Button[] calculatorRef = new Button[1];
			calculatorRef[0] = Button.builder(calculatorLabel(InventoryCalculatorState.isEnabled()), button -> {
				boolean enabled = InventoryCalculatorState.toggleEnabled();
				calculatorRef[0].setMessage(calculatorLabel(enabled));
				saveClientSettings();
			}).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
			layout.addChild(calculatorRef[0], 1, 0);

			Button[] packGuardRef = new Button[1];
			packGuardRef[0] = Button.builder(packGuardLabel(ResourcePackGuardState.isEnabled()), button -> {
				boolean enabled = ResourcePackGuardState.toggleEnabled();
				ResourcePackGuardState.applyClientState(enabled);
				packGuardRef[0].setMessage(packGuardLabel(enabled));
				saveClientSettings();
			}).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
			layout.addChild(packGuardRef[0], 1, 1);

			Button[] locatorRef = new Button[1];
			locatorRef[0] = Button.builder(locatorLabel(LocatorBarState.isEnabled()), button -> {
				boolean enabled = LocatorBarState.toggleEnabled();
				locatorRef[0].setMessage(locatorLabel(enabled));
				saveClientSettings();
			}).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
			layout.addChild(locatorRef[0], 2, 0);

			Button[] slotLockRef = new Button[1];
			slotLockRef[0] = Button.builder(slotLockLabel(CheckItemClientModule.isSlotLockingEnabled()), button -> {
				boolean enabled = CheckItemClientModule.toggleSlotLocking();
				slotLockRef[0].setMessage(slotLockLabel(enabled));
				saveClientSettings();
			}).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
			layout.addChild(slotLockRef[0], 2, 1);

			Button[] syncDebugRef = new Button[1];
			syncDebugRef[0] = Button.builder(debugSyncLabel(CheckItemClientModule.isDebugSyncEnabled()), button -> {
				boolean enabled = CheckItemClientModule.toggleDebugSync();
				syncDebugRef[0].setMessage(debugSyncLabel(enabled));
			}).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
			layout.addChild(syncDebugRef[0], 3, 0);

			Button[] autoSpawnRef = new Button[1];
			autoSpawnRef[0] = Button.builder(autoSpawnLabel(AutoSpawnState.isEnabled()), button -> {
				boolean enabled = AutoSpawnState.toggleEnabled();
				autoSpawnRef[0].setMessage(autoSpawnLabel(enabled));
				saveClientSettings();
			}).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
			layout.addChild(autoSpawnRef[0], 3, 1);
		}

		private static Component zoomLabel(boolean enabled) {
			return Component.literal(enabled ? "Unlimited Zoom: ON" : "Unlimited Zoom: OFF");
		}

		private static Component durabilityLabel(boolean enabled) {
			return Component.literal(enabled ? "Durability Guard: ON" : "Durability Guard: OFF");
		}

		private static Component calculatorLabel(boolean enabled) {
			return Component.literal(enabled ? "Inventory Calc: ON" : "Inventory Calc: OFF");
		}

		private static Component packGuardLabel(boolean enabled) {
			return Component.literal(enabled ? "Block Server Packs: ON" : "Block Server Packs: OFF");
		}

		private static Component locatorLabel(boolean enabled) {
			return Component.literal(enabled ? "Locator Bar: ON" : "Locator Bar: OFF");
		}

		private static Component slotLockLabel(boolean enabled) {
			return Component.literal(enabled ? "Slot Locking: ON" : "Slot Locking: OFF");
		}

		private static Component debugSyncLabel(boolean enabled) {
			return Component.literal(enabled ? "Item Sync Debug: ON" : "Item Sync Debug: OFF");
		}

		private static Component autoSpawnLabel(boolean enabled) {
			return Component.literal(enabled ? "Auto /spawn (Low HP): ON" : "Auto /spawn (Low HP): OFF");
		}

		private static void saveClientSettings() {
			ClientConfigIO.saveClientSettings(new ClientSettingsConfig(
				CameraZoomState.isEnabled(),
				DurabilityGuardState.isEnabled(),
				InventoryCalculatorState.isEnabled(),
				ResourcePackGuardState.isEnabled(),
				LocatorBarState.isEnabled(),
				CheckItemClientModule.isSlotLockingEnabled(),
				AutoSpawnState.isEnabled()
			));
		}
	}

	private static final class ChatToolsTab implements Tab {
		private final Component title = Component.literal("Chat");
		private final GridLayout contentLayout = new GridLayout();
		private final ScrollableLayout scrollLayout;
		private final Button[] messageButtons;
		private final StringWidget[] timerLabels;
		private final long[] cooldownEnds;
		private final String[] messages;
		private final EditBox addInput;
		private final Button addButton;
		private final Button addOnlyButton;
		private final Button editSendChatButton;
		private final Button editSendOwnButton;
		private boolean adding;
		private int editingIndex = -1;

		private ChatToolsTab(ChatInputsConfig config) {
			Minecraft client = Minecraft.getInstance();
			contentLayout.rowSpacing(6);
			contentLayout.columnSpacing(6);
			messageButtons = new Button[CHAT_MESSAGES_MAX];
			timerLabels = new StringWidget[CHAT_MESSAGES_MAX];
			cooldownEnds = normalizeCooldowns(config != null ? config.cooldownEnds : null);
			messages = normalizeMessages(config != null ? config.lines : null);
			for (int i = 0; i < CHAT_MESSAGES_MAX; i++) {
				int row = i + 1;
				int index = i;
				Button messageButton = Button.builder(Component.literal(""), button -> beginEdit(index))
					.size(CHAT_FIELD_WIDTH + CHAT_ACTION_WIDTH * 2, BUTTON_HEIGHT)
					.build();
				updateMessageButton(messageButton, messages[i]);
				messageButtons[i] = messageButton;
				contentLayout.addChild(messageButton, row, 0, 1, 3);

				StringWidget timer = new StringWidget(
					CHAT_TIMER_WIDTH,
					BUTTON_HEIGHT,
					formatCooldownLabel(getRemainingMs(i)),
					client.font
				);
				timer.setColor(0xFFB0B0B0);
				timer.visible = messageButton.visible;
				timerLabels[i] = timer;
				contentLayout.addChild(timer, row, 3);
			}

			addInput = new EditBox(
				client.font,
				0,
				0,
				CHAT_FIELD_WIDTH,
				18,
				Component.empty()
			);
			addInput.setMaxLength(256);
			addInput.addFormatter(ChatToolsTab::formatForDisplay);
			addInput.setHint(Component.literal("Type Message...").withStyle(ChatFormatting.GRAY));

			addButton = Button.builder(Component.literal("+"), button -> {
				String value = addInput.getValue().trim();
				if (value.isEmpty()) {
					return;
				}
				int slot = findEmptySlot();
				if (slot < 0) {
					return;
				}
			messages[slot] = value;
			updateMessageButton(messageButtons[slot], value);
			timerLabels[slot].visible = true;
			setAdding(false);
			addInput.setValue("");
			saveMessages();
			}).size(CHAT_ADD_BUTTON_WIDTH, BUTTON_HEIGHT).build();

			addOnlyButton = Button.builder(Component.literal("+"), button -> setAdding(true))
				.size(20, BUTTON_HEIGHT)
				.build();

			editSendChatButton = Button.builder(Component.literal("CHAT"), button -> sendEditToChat())
				.size(CHAT_ACTION_WIDTH, BUTTON_HEIGHT)
				.build();
			editSendOwnButton = Button.builder(Component.literal("OWN"), button -> sendEditToSelf())
				.size(CHAT_ACTION_WIDTH, BUTTON_HEIGHT)
				.build();

			contentLayout.addChild(addInput, 0, 0);
			contentLayout.addChild(addButton, 0, 1);
			contentLayout.addChild(editSendChatButton, 0, 1);
			contentLayout.addChild(editSendOwnButton, 0, 2);
			contentLayout.addChild(addOnlyButton, 0, 0, 1, 4);
			contentLayout.addChild(new StringWidget(CHAT_TIMER_WIDTH, BUTTON_HEIGHT, Component.empty(), client.font), 0, 3);
			scrollLayout = new ScrollableLayout(client, contentLayout, 0);
			setAdding(false);
			setEditing(false);
		}

		@Override
		public Component getTabTitle() {
			return title;
		}

		@Override
		public Component getTabExtraNarration() {
			return Component.empty();
		}

		@Override
		public void visitChildren(java.util.function.Consumer<AbstractWidget> consumer) {
			scrollLayout.visitChildren(element -> {
				if (element instanceof AbstractWidget widget) {
					consumer.accept(widget);
				}
			});
		}

		@Override
		public void doLayout(ScreenRectangle area) {
			scrollLayout.setX(area.left());
			scrollLayout.setY(area.top());
			scrollLayout.setMinWidth(area.width());
			scrollLayout.setMaxHeight(area.height());
			scrollLayout.arrangeElements();
		}

		private void saveInputs() {
			saveMessages();
		}

		private void saveMessages() {
			ClientConfigIO.saveChatInputs(new ChatInputsConfig(normalizeMessages(messages), normalizeCooldowns(cooldownEnds)));
		}

		private void setAdding(boolean value) {
			adding = value;
			addInput.visible = adding;
			addButton.visible = adding;
			addOnlyButton.visible = !adding;
			editSendChatButton.visible = false;
			editSendOwnButton.visible = false;
			if (!adding) {
				editingIndex = -1;
			}
			if (adding) {
				addInput.setFocused(true);
			}
		}

		private void setEditing(boolean value) {
			if (value) {
				addInput.visible = true;
				addOnlyButton.visible = false;
				addButton.visible = false;
				editSendChatButton.visible = true;
				editSendOwnButton.visible = true;
				addInput.setFocused(true);
			} else if (!adding) {
				addInput.visible = false;
				addButton.visible = false;
				editSendChatButton.visible = false;
				editSendOwnButton.visible = false;
				addOnlyButton.visible = true;
			}
		}

		private void beginEdit(int index) {
			if (index < 0 || index >= messages.length) {
				return;
			}
			String value = messages[index];
			if (value == null) {
				value = "";
			}
			editingIndex = index;
			addInput.setValue(value);
			addInput.moveCursorToStart(false);
			adding = false;
			setEditing(true);
		}

		private void sendEditToChat() {
			Minecraft client = Minecraft.getInstance();
			String value = addInput.getValue().trim();
			if (value.isEmpty()) {
				return;
			}
			if (isCooldownActive(editingIndex)) {
				return;
			}
			if (client.getConnection() != null) {
				client.getConnection().sendChat(value);
			}
			startCooldown(editingIndex);
			commitEditValue(value);
		}

		private void sendEditToSelf() {
			Minecraft client = Minecraft.getInstance();
			String value = addInput.getValue().trim();
			if (value.isEmpty()) {
				return;
			}
			if (client.player != null) {
				client.player.displayClientMessage(Component.literal(translateColorCodes(value)), false);
			}
			commitEditValue(value);
		}

		private void commitEditValue(String value) {
			if (editingIndex < 0 || editingIndex >= messages.length) {
				return;
			}
			messages[editingIndex] = value;
			updateMessageButton(messageButtons[editingIndex], value);
			timerLabels[editingIndex].visible = true;
			saveMessages();
			addInput.setValue("");
			setEditing(false);
			editingIndex = -1;
		}

		private int findEmptySlot() {
			for (int i = 0; i < messages.length; i++) {
				if (messages[i] == null || messages[i].isEmpty()) {
					return i;
				}
			}
			return -1;
		}

		private static void updateMessageButton(Button button, String message) {
			if (message == null || message.isEmpty()) {
				button.visible = false;
				return;
			}
			button.visible = true;
			button.setMessage(Component.literal(translateColorCodes(message)));
		}

		private void startCooldown(int index) {
			if (index < 0 || index >= cooldownEnds.length) {
				return;
			}
			cooldownEnds[index] = System.currentTimeMillis() + CHAT_COOLDOWN_MS;
			updateCooldownLabel(index);
			saveMessages();
		}

		private void updateCooldownLabel(int index) {
			timerLabels[index].setMessage(formatCooldownLabel(getRemainingMs(index)));
		}

		private void updateCooldowns() {
			for (int i = 0; i < timerLabels.length; i++) {
				if (!timerLabels[i].visible) {
					continue;
				}
				updateCooldownLabel(i);
			}
		}

		private void handleEnter() {
			String value = addInput.getValue().trim();
			if (value.isEmpty()) {
				return;
			}
			if (editingIndex >= 0) {
				commitEditValue(value);
				return;
			}
			if (adding) {
				int slot = findEmptySlot();
				if (slot < 0) {
					return;
				}
				messages[slot] = value;
				updateMessageButton(messageButtons[slot], value);
				timerLabels[slot].visible = true;
				setAdding(false);
				addInput.setValue("");
				saveMessages();
			}
		}

		private static String[] normalizeMessages(String[] input) {
			String[] normalized = new String[CHAT_MESSAGES_MAX];
			if (input != null) {
				int count = Math.min(input.length, normalized.length);
				for (int i = 0; i < count; i++) {
					String value = input[i];
					normalized[i] = value == null ? "" : value;
				}
			}
			for (int i = 0; i < normalized.length; i++) {
				if (normalized[i] == null) {
					normalized[i] = "";
				}
			}
			return normalized;
		}

		private static String translateColorCodes(String value) {
			if (value.indexOf('&') < 0) {
				return value;
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


		private static String formatCooldown(long remainingMs) {
			long totalSeconds = remainingMs / 1000L;
			long minutes = totalSeconds / 60L;
			long seconds = totalSeconds % 60L;
			return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds);
		}

		private static Component formatCooldownLabel(long remainingMs) {
			if (remainingMs <= 0L) {
				return Component.literal(translateColorCodes("&aREADY"));
			}
			return Component.literal(formatCooldown(remainingMs));
		}

		private static long[] normalizeCooldowns(long[] input) {
			long[] normalized = new long[CHAT_MESSAGES_MAX];
			if (input != null) {
				int count = Math.min(input.length, normalized.length);
				for (int i = 0; i < count; i++) {
					long value = input[i];
					normalized[i] = value < 0L ? 0L : value;
				}
			}
			return normalized;
		}

		private long getRemainingMs(int index) {
			long end = cooldownEnds[index];
			if (end <= 0L) {
				return 0L;
			}
			long remaining = end - System.currentTimeMillis();
			return Math.max(remaining, 0L);
		}

		private boolean isCooldownActive(int index) {
			return index >= 0 && index < cooldownEnds.length && getRemainingMs(index) > 0L;
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

		private static FormattedCharSequence formatForDisplay(String value, int displayPos) {
			int length = value.length();
			if (length == 0) {
				return FormattedCharSequence.EMPTY;
			}

			int start = Math.max(0, Math.min(displayPos, length));
			Style style = Style.EMPTY;
			for (int i = 0; i < start; i++) {
				char c = value.charAt(i);
				if (c == '&' && i + 1 < length) {
					ChatFormatting formatting = ChatFormatting.getByCode(value.charAt(i + 1));
					if (formatting != null) {
						style = applyFormatting(style, formatting);
						i++;
						if (i >= start) {
							start = Math.min(i + 1, length);
							break;
						}
					}
				}
			}
			while (start + 1 < length && value.charAt(start) == '&') {
				ChatFormatting formatting = ChatFormatting.getByCode(value.charAt(start + 1));
				if (formatting == null) {
					break;
				}
				style = applyFormatting(style, formatting);
				start += 2;
			}

			java.util.ArrayList<FormattedCharSequence> parts = new java.util.ArrayList<>(4);
			StringBuilder segment = new StringBuilder();
			for (int i = start; i < length; i++) {
				char c = value.charAt(i);
				if (c == '&' && i + 1 < length) {
					ChatFormatting formatting = ChatFormatting.getByCode(value.charAt(i + 1));
					if (formatting != null) {
						if (segment.length() > 0) {
							parts.add(FormattedCharSequence.forward(segment.toString(), style));
							segment.setLength(0);
						}
						style = applyFormatting(style, formatting);
						i++;
						continue;
					}
				}
				segment.append(c);
			}

			if (segment.length() > 0) {
				parts.add(FormattedCharSequence.forward(segment.toString(), style));
			}
			if (parts.isEmpty()) {
				return FormattedCharSequence.EMPTY;
			}
			return FormattedCharSequence.composite(parts);
		}

		private static Style applyFormatting(Style current, ChatFormatting formatting) {
			if (formatting == ChatFormatting.RESET) {
				return Style.EMPTY;
			}
			if (formatting.isColor()) {
				return Style.EMPTY.withColor(formatting);
			}
			return current.applyFormat(formatting);
		}
	}

	private static final class SpecialMembersTab implements Tab {
		private static final int LIST_WIDTH = 260;
		private static final int LABEL_HEIGHT = 12;
		private static final int COLOR_TITLE = 0xFFE6E6E6;
		private static final int COLOR_SUBTITLE = 0xFFB8B8B8;
		private static final int COLOR_SECTION = 0xFFFF5555;
		private static final int COLOR_ADMIN_PREFIX = 0xFFFF5555;
		private static final String TITLE = "SPECIAL MEMBERS";
		private static final String DESC_LINE_1 =
			"Special Members are our beta testers, Patreon supporters, and friends";
		private static final String DESC_LINE_2 =
			"who help shape PrimeTooler with early feedback and ideas.";
		private static final RainbowTextStyle NAME_STYLE =
			new RainbowTextStyle(0.25f, 0.66f, 1.0f, 1.0f, 180.0f, true);

		private final Component title = Component.literal("SPECIAL");
		private final GridLayout contentLayout = new GridLayout();
		private final ScrollableLayout scrollLayout;

		private SpecialMembersTab() {
			Minecraft client = Minecraft.getInstance();
			contentLayout.rowSpacing(6);
			contentLayout.columnSpacing(6);

			int row = 0;
			StringWidget header = new StringWidget(LIST_WIDTH, LABEL_HEIGHT, Component.literal(TITLE), client.font);
			header.setColor(COLOR_TITLE);
			contentLayout.addChild(header, row++, 0);

			StringWidget desc1 = new StringWidget(LIST_WIDTH, LABEL_HEIGHT, Component.literal(DESC_LINE_1), client.font);
			desc1.setColor(COLOR_SUBTITLE);
			contentLayout.addChild(desc1, row++, 0);

			StringWidget desc2 = new StringWidget(LIST_WIDTH, LABEL_HEIGHT, Component.literal(DESC_LINE_2), client.font);
			desc2.setColor(COLOR_SUBTITLE);
			contentLayout.addChild(desc2, row++, 0);

			contentLayout.addChild(new SpacerWidget(LIST_WIDTH, 4), row++, 0);

			StringWidget adminHeader = new StringWidget(LIST_WIDTH, LABEL_HEIGHT, Component.literal("ADMIN"), client.font);
			adminHeader.setColor(COLOR_SECTION);
			contentLayout.addChild(adminHeader, row++, 0);

			PlayerMarkRegistry.Member[] admins = PlayerMarkRegistry.adminMembers();
			for (int i = 0; i < admins.length; i++) {
				PlayerMarkRegistry.Member member = admins[i];
				contentLayout.addChild(new RainbowNameWidget(member.name(), true), row++, 0);
			}

			contentLayout.addChild(new SpacerWidget(LIST_WIDTH, 4), row++, 0);

			StringWidget specialHeader = new StringWidget(LIST_WIDTH, LABEL_HEIGHT, Component.literal("SPECIAL"), client.font);
			specialHeader.setColor(COLOR_SECTION);
			contentLayout.addChild(specialHeader, row++, 0);

			PlayerMarkRegistry.Member[] specials = PlayerMarkRegistry.specialMembers();
			for (int i = 0; i < specials.length; i++) {
				PlayerMarkRegistry.Member member = specials[i];
				contentLayout.addChild(new RainbowNameWidget(member.name(), false), row++, 0);
			}

			scrollLayout = new ScrollableLayout(client, contentLayout, 0);
		}

		@Override
		public Component getTabTitle() {
			return title;
		}

		@Override
		public Component getTabExtraNarration() {
			return Component.empty();
		}

		@Override
		public void visitChildren(java.util.function.Consumer<AbstractWidget> consumer) {
			scrollLayout.visitChildren(element -> {
				if (element instanceof AbstractWidget widget) {
					consumer.accept(widget);
				}
			});
		}

		@Override
		public void doLayout(ScreenRectangle area) {
			scrollLayout.setX(area.left());
			scrollLayout.setY(area.top());
			scrollLayout.setMinWidth(area.width());
			scrollLayout.setMaxHeight(area.height());
			scrollLayout.arrangeElements();
		}

		private static final class RainbowNameWidget extends AbstractWidget {
			private final String name;
			private final boolean admin;

			private RainbowNameWidget(String name, boolean admin) {
				super(0, 0, LIST_WIDTH, LABEL_HEIGHT, Component.empty());
				this.name = name == null ? "" : name;
				this.admin = admin;
			}

			@Override
			public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
				Font font = Minecraft.getInstance().font;
				int x = getX();
				int y = getY();
				int cursorX = x;
				if (admin) {
					graphics.drawString(font, "★", cursorX, y, COLOR_ADMIN_PREFIX, false);
					cursorX += font.width("★");
				}
				float timeSeconds = (float) (System.nanoTime() * 1.0e-9);
				RainbowTextRenderer.draw(graphics, font, name, cursorX, y, timeSeconds, NAME_STYLE);
			}

			@Override
			protected void updateWidgetNarration(NarrationElementOutput narration) {
				// Intentional: decorative label only.
			}
		}

		private static final class SpacerWidget extends AbstractWidget {
			private SpacerWidget(int width, int height) {
				super(0, 0, width, height, Component.empty());
			}

			@Override
			public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			}

			@Override
			protected void updateWidgetNarration(NarrationElementOutput narration) {
				// Intentional: spacer has no narration.
			}
		}
	}
}
