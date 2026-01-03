package de.nuua.primetooler.features.primemenu.client;

import de.nuua.primetooler.core.config.ChatInputsConfig;
import de.nuua.primetooler.core.config.ClientSettingsConfig;
import de.nuua.primetooler.core.Messages;
import de.nuua.primetooler.features.autospawn.client.AutoSpawnState;
import de.nuua.primetooler.api.v1.client.text.RainbowTextRenderer;
import de.nuua.primetooler.api.v1.client.text.RainbowTextStyle;
import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import de.nuua.primetooler.features.playermark.client.PlayerMarkRegistry;
import de.nuua.primetooler.features.playermark.client.SpecialNamesState;
import de.nuua.primetooler.features.playermark.client.ClanTagState;
import de.nuua.primetooler.features.sound.client.BeaconSoundState;
import de.nuua.primetooler.features.camerazoom.client.FrontCameraToggleState;
import de.nuua.primetooler.features.camerazoom.client.CameraZoomState;
import de.nuua.primetooler.features.durabilityguard.client.DurabilityGuardState;
import de.nuua.primetooler.features.inventorycalc.client.InventoryCalculatorState;
import de.nuua.primetooler.features.inventoryeffects.client.InventoryEffectsState;
import de.nuua.primetooler.features.inventoryeffects.client.HudEffectsState;
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
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.Tooltip;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.Util;

public final class PrimeMenuScreen extends Screen {
	private static final ResourceLocation TAB_HEADER_BACKGROUND =
		ResourceLocation.withDefaultNamespace("textures/gui/tab_header_background.png");
	private static final Button.OnPress NOOP_PRESS = button -> {
	};
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final int CONFIG_BUTTON_WIDTH = (int) (BUTTON_WIDTH * 1.2f);
	private static final int GRID_COLUMNS = 2;
	private static final int GRID_ROWS = 3;
	private static final int CHAT_MESSAGES_MAX_DEFAULT = 5;
	private static final int CHAT_MESSAGES_MAX_SPECIAL = 25;
	private static final int TAB_CONTENT_TOP_PADDING = 6;
	private static final int CHAT_ADD_BUTTON_WIDTH = 24;
	private static final int CHAT_DELETE_WIDTH = 64;
	private static final int CHAT_FIELD_WIDTH = 340;
	private static final int CHAT_ACTION_WIDTH = 54;
	private static final int CHAT_TIMER_WIDTH = 20;
	private static final int CHAT_LABEL_HEIGHT = 12;
	private static final int CONFIG_LABEL_HEIGHT = 12;
	private static final int CHAT_COLUMN_SPACING = 0;
	private static final int CHAT_ROW_SPACING = 3;
	private static final int CHAT_GAP_DELETE = 5;
	private static final int CHAT_GAP_INPUT = 5;
	private static final int CHAT_GAP_CHAT = 5;
	private static final int CHAT_GAP_TIMER = 5;
	private static final int CONFIG_LAYOUT_WIDTH = CONFIG_BUTTON_WIDTH * 2 + 12;
	private static final int CHAT_MAIN_WIDTH =
		CHAT_DELETE_WIDTH + CHAT_GAP_DELETE + CHAT_FIELD_WIDTH + CHAT_GAP_INPUT
			+ CHAT_ACTION_WIDTH + CHAT_GAP_CHAT + CHAT_ACTION_WIDTH;
	private static final int CHAT_LAYOUT_WIDTH = CHAT_MAIN_WIDTH + CHAT_GAP_TIMER + CHAT_TIMER_WIDTH;
	private static final long CHAT_COOLDOWN_MS = 30L * 60L * 1000L;
	private static final RainbowTextStyle TAB_TITLE_STYLE =
		new RainbowTextStyle(0.25f, 0.66f, 1.0f, 1.0f, 180.0f, true);
	private static final int[] RAINBOW_HEX = new int[] {
		0xFFFF4D4D,
		0xFFFFA64D,
		0xFFFFFF4D,
		0xFF4DFF4D,
		0xFF4DD2FF,
		0xFF4D4DFF,
		0xFFFF4DFF
	};

	private final Screen parent;
	private TabManager tabManager;
	private TabNavigationBar tabNavigationBar;
	private int headerHeight;
	private PrimeMenuHomeTab homeTab;
	private ChatToolsTab chatToolsTab;
	private SpecialMembersTab specialMembersTab;
	private Tab lastTab;

	public PrimeMenuScreen(Screen parent) {
		super(Component.literal(Messages.applyColorCodes(Messages.get(Messages.Id.MENU_TITLE))));
		this.parent = parent;
	}

	@Override
	protected void init() {
		tabManager = new TabManager(this::addTabWidget, this::removeWidget);
		boolean specialAccess = PlayerMarkRegistry.isAuthorizedUser();
		int maxMessages = getChatMessagesMax(specialAccess);
		homeTab = new PrimeMenuHomeTab(this::addTabWidget, this::removeWidget);
		chatToolsTab = new ChatToolsTab(ClientConfigIO.loadChatInputs(maxMessages), maxMessages, specialAccess);
		specialMembersTab = new SpecialMembersTab();
		tabNavigationBar = TabNavigationBar.builder(tabManager, width)
			.addTabs(
				homeTab,
				chatToolsTab,
				specialMembersTab
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
		if (specialMembersTab != null && minecraft != null) {
			float timeSeconds = (float) (System.nanoTime() * 1.0e-9);
			specialMembersTab.updateAnimatedTitle(timeSeconds, minecraft.font);
			updateSpecialTabButtonTitle();
		}
		if (tabManager != null) {
			Tab current = tabManager.getCurrentTab();
			if (current != lastTab && lastTab == chatToolsTab && chatToolsTab != null) {
				chatToolsTab.resetUi();
			}
			if (current != lastTab && lastTab == homeTab && homeTab != null) {
				homeTab.setActive(false);
			}
			if (current != lastTab && current == homeTab && homeTab != null) {
				homeTab.setActive(true);
			}
			lastTab = current;
		}
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

	private void updateSpecialTabButtonTitle() {
		if (tabNavigationBar == null || specialMembersTab == null) {
			return;
		}
		for (var child : tabNavigationBar.children()) {
			if (child instanceof TabButton button && button.tab() == specialMembersTab) {
				button.setMessage(specialMembersTab.getTabTitle());
				return;
			}
		}
	}

	private void positionElements() {
		if (tabNavigationBar == null) {
			return;
		}
		tabNavigationBar.setWidth(width);
		tabNavigationBar.arrangeElements();
		headerHeight = tabNavigationBar.getRectangle().bottom();

		int topPadding = Math.max(0, TAB_CONTENT_TOP_PADDING);
		ScreenRectangle tabArea = new ScreenRectangle(
			0,
			headerHeight + topPadding,
			width,
			Math.max(0, height - headerHeight - topPadding)
		);
		tabManager.setTabArea(tabArea);
	}

	private static int getChatMessagesMax(boolean specialAccess) {
		return specialAccess ? CHAT_MESSAGES_MAX_SPECIAL : CHAT_MESSAGES_MAX_DEFAULT;
	}

	private static final class PlaceholderTab extends GridLayoutTab {
		private PlaceholderTab(Component title) {
			super(title);
			layout.rowSpacing(3);
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

	private static final class PrimeMenuHomeTab implements Tab {
		private static final String SECTION_SEARCH_GAMEPLAY = normalizeSearchText(Messages.get(Messages.Id.CONFIG_SECTION_GAMEPLAY));
		private static final String SECTION_SEARCH_VISUAL = normalizeSearchText(Messages.get(Messages.Id.CONFIG_SECTION_VISUAL));
		private static final String SECTION_SEARCH_SOUND = normalizeSearchText(Messages.get(Messages.Id.CONFIG_SECTION_SOUND));

		private final Component title = Component.literal(Messages.get(Messages.Id.TAB_CONFIG));
		private final java.util.function.Consumer<AbstractWidget> addWidget;
		private final java.util.function.Consumer<AbstractWidget> removeWidget;
		private boolean active;
		private final Minecraft client;
		private GridLayout contentLayout;
		private ScrollableLayout scrollLayout;
		private ScreenRectangle lastArea;
		private final java.util.List<ButtonEntry> gameplayEntries = new java.util.ArrayList<>();
		private final java.util.List<ButtonEntry> visualEntries = new java.util.ArrayList<>();
		private final java.util.List<ButtonEntry> soundEntries = new java.util.ArrayList<>();
		private final StringWidget header;
		private final StringWidget desc1;
		private final StringWidget desc2;
		private final StringWidget desc3;
		private final StringWidget gameplayHeader;
		private final StringWidget visualHeader;
		private final StringWidget soundHeader;
		private final EditBox searchBox;

		private PrimeMenuHomeTab(
			java.util.function.Consumer<AbstractWidget> addWidget,
			java.util.function.Consumer<AbstractWidget> removeWidget
		) {
			this.addWidget = addWidget;
			this.removeWidget = removeWidget;
			client = Minecraft.getInstance();

			boolean specialAccess = PlayerMarkRegistry.isAuthorizedUser();
			header = new StringWidget(
				CONFIG_LAYOUT_WIDTH,
				CONFIG_LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CONFIG_TITLE))),
				client.font
			);
			header.setColor(0xFFE6E6E6);

			desc1 = new StringWidget(
				CONFIG_LAYOUT_WIDTH,
				CONFIG_LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CONFIG_DESC_1))),
				client.font
			);
			desc1.setColor(0xFFB8B8B8);

			desc2 = new StringWidget(
				CONFIG_LAYOUT_WIDTH,
				CONFIG_LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CONFIG_DESC_2))),
				client.font
			);
			desc2.setColor(0xFFB8B8B8);

			desc3 = new StringWidget(
				CONFIG_LAYOUT_WIDTH,
				CONFIG_LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CONFIG_DESC_3))),
				client.font
			);
			desc3.setColor(0xFFB8B8B8);

			searchBox = new EditBox(
				client.font,
				0,
				0,
				CONFIG_LAYOUT_WIDTH,
				18,
				Component.empty()
			);
			searchBox.setMaxLength(64);
			searchBox.setHint(
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CONFIG_SEARCH_HINT)))
					.withStyle(ChatFormatting.GRAY)
			);
			searchBox.setResponder(this::applySearch);

			gameplayHeader = new StringWidget(
				CONFIG_LAYOUT_WIDTH,
				CONFIG_LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CONFIG_SECTION_GAMEPLAY))),
				client.font
			);
			gameplayHeader.setColor(0xFFE6E6E6);

			Button[] toggleRef = new Button[1];
			toggleRef[0] = Button.builder(zoomLabel(CameraZoomState.isEnabled()), button -> {
				if (!specialAccess) {
					return;
				}
				boolean enabled = CameraZoomState.toggleEnabled();
				toggleRef[0].setMessage(zoomLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			toggleRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_ZOOM)));
			if (!specialAccess) {
				toggleRef[0].active = false;
				toggleRef[0].setMessage(noAccessLabel());
				toggleRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_LOCKED)));
			}
			gameplayEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_ZOOM), toggleRef[0]));

			Button[] durabilityRef = new Button[1];
			durabilityRef[0] = Button.builder(durabilityLabel(DurabilityGuardState.isEnabled()), button -> {
				boolean enabled = DurabilityGuardState.toggleEnabled();
				durabilityRef[0].setMessage(durabilityLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			durabilityRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_DURABILITY)));
			gameplayEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_DURABILITY), durabilityRef[0]));

			Button[] calculatorRef = new Button[1];
			calculatorRef[0] = Button.builder(calculatorLabel(InventoryCalculatorState.isEnabled()), button -> {
				boolean enabled = InventoryCalculatorState.toggleEnabled();
				calculatorRef[0].setMessage(calculatorLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			calculatorRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_CALC)));
			gameplayEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_CALC), calculatorRef[0]));

			Button[] packGuardRef = new Button[1];
			packGuardRef[0] = Button.builder(packGuardLabel(ResourcePackGuardState.isEnabled()), button -> {
				boolean enabled = ResourcePackGuardState.toggleEnabled();
				ResourcePackGuardState.applyClientState(enabled);
				packGuardRef[0].setMessage(packGuardLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			packGuardRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_PACK)));
			gameplayEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_PACK), packGuardRef[0]));

			Button[] locatorRef = new Button[1];
			locatorRef[0] = Button.builder(locatorLabel(LocatorBarState.isEnabled()), button -> {
				boolean enabled = LocatorBarState.toggleEnabled();
				locatorRef[0].setMessage(locatorLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			locatorRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_LOCATOR)));
			gameplayEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_LOCATOR), locatorRef[0]));

			Button[] slotLockRef = new Button[1];
			slotLockRef[0] = Button.builder(slotLockLabel(CheckItemClientModule.isSlotLockingEnabled()), button -> {
				boolean enabled = CheckItemClientModule.toggleSlotLocking();
				slotLockRef[0].setMessage(slotLockLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			slotLockRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_SLOTLOCK)));
			gameplayEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_SLOTLOCK), slotLockRef[0]));

			Button[] syncDebugRef = new Button[1];
			syncDebugRef[0] = Button.builder(debugSyncLabel(CheckItemClientModule.isDebugSyncEnabled()), button -> {
				boolean enabled = CheckItemClientModule.toggleDebugSync();
				syncDebugRef[0].setMessage(debugSyncLabel(enabled));
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			syncDebugRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_SYNC)));
			gameplayEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_SYNC), syncDebugRef[0]));

			Button[] autoSpawnRef = new Button[1];
			autoSpawnRef[0] = Button.builder(autoSpawnLabel(AutoSpawnState.isEnabled()), button -> {
				if (!specialAccess) {
					return;
				}
				boolean enabled = AutoSpawnState.toggleEnabled();
				autoSpawnRef[0].setMessage(autoSpawnLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			autoSpawnRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_AUTOSPAWN)));
			if (!specialAccess) {
				autoSpawnRef[0].active = false;
				autoSpawnRef[0].setMessage(noAccessLabel());
				autoSpawnRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_LOCKED)));
			}
			gameplayEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_AUTOSPAWN), autoSpawnRef[0]));

			visualHeader = new StringWidget(
				CONFIG_LAYOUT_WIDTH,
				CONFIG_LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CONFIG_SECTION_VISUAL))),
				client.font
			);
			visualHeader.setColor(0xFFE6E6E6);

			Button[] specialNamesRef = new Button[1];
			specialNamesRef[0] = Button.builder(specialNamesLabel(SpecialNamesState.isEnabled()), button -> {
				boolean enabled = SpecialNamesState.toggleEnabled();
				specialNamesRef[0].setMessage(specialNamesLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			specialNamesRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_SPECIALNAMES)));
			visualEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_SPECIALNAMES), specialNamesRef[0]));

			Button[] clanTagRef = new Button[1];
			clanTagRef[0] = Button.builder(clanTagLabel(ClanTagState.isEnabled()), button -> {
				boolean enabled = ClanTagState.toggleEnabled();
				clanTagRef[0].setMessage(clanTagLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			clanTagRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_CLANTAG)));
			visualEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_CLANTAG), clanTagRef[0]));

			Button[] frontCameraRef = new Button[1];
			frontCameraRef[0] = Button.builder(frontCameraLabel(FrontCameraToggleState.isDisabled()), button -> {
				boolean disabled = FrontCameraToggleState.toggleDisabled();
				frontCameraRef[0].setMessage(frontCameraLabel(disabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			frontCameraRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_FRONTCAM)));
			visualEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_FRONTCAM), frontCameraRef[0]));

			Button[] inventoryEffectsRef = new Button[1];
			inventoryEffectsRef[0] = Button.builder(inventoryEffectsLabel(InventoryEffectsState.isEnabled()), button -> {
				boolean enabled = InventoryEffectsState.toggleEnabled();
				inventoryEffectsRef[0].setMessage(inventoryEffectsLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			inventoryEffectsRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_EFFECTS)));
			visualEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_EFFECTS), inventoryEffectsRef[0]));

			Button[] hudEffectsRef = new Button[1];
			hudEffectsRef[0] = Button.builder(hudEffectsLabel(HudEffectsState.isEnabled()), button -> {
				boolean enabled = HudEffectsState.toggleEnabled();
				hudEffectsRef[0].setMessage(hudEffectsLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			hudEffectsRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_HUDEFFECTS)));
			visualEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_HUDEFFECTS), hudEffectsRef[0]));

			soundHeader = new StringWidget(
				CONFIG_LAYOUT_WIDTH,
				CONFIG_LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CONFIG_SECTION_SOUND))),
				client.font
			);
			soundHeader.setColor(0xFFE6E6E6);

			Button[] beaconSoundRef = new Button[1];
			beaconSoundRef[0] = Button.builder(beaconSoundLabel(BeaconSoundState.isEnabled()), button -> {
				boolean enabled = BeaconSoundState.toggleEnabled();
				beaconSoundRef[0].setMessage(beaconSoundLabel(enabled));
				saveClientSettings();
			}).size(CONFIG_BUTTON_WIDTH, BUTTON_HEIGHT).build();
			beaconSoundRef[0].setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_BEACON_SOUND)));
			soundEntries.add(new ButtonEntry(Messages.get(Messages.Id.LABEL_BEACON_SOUND), beaconSoundRef[0]));

			rebuildContentLayout("");
		}

		private static final class ButtonEntry {
			private final String label;
			private final Button button;

			private ButtonEntry(String label, Button button) {
				this.label = label == null ? "" : label;
				this.button = button;
			}
		}

		private static int addSection(GridLayout layout, StringWidget header,
			java.util.List<ButtonEntry> entries, int row, String query, boolean showAll) {
			sortEntries(entries);
			java.util.List<ButtonEntry> visible = new java.util.ArrayList<>();
			int col = 0;
			for (int i = 0; i < entries.size(); i++) {
				ButtonEntry entry = entries.get(i);
				boolean match = query.isEmpty()
					|| showAll
					|| normalizeSearchText(entry.label).contains(query);
				entry.button.visible = match;
				if (match) {
					visible.add(entry);
				}
			}
			if (visible.isEmpty()) {
				header.visible = false;
				return row;
			}
			header.visible = true;
			layout.addChild(header, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());
			for (int i = 0; i < visible.size(); i++) {
				ButtonEntry entry = visible.get(i);
				layout.addChild(entry.button, row, col);
				col++;
				if (col >= 2) {
					col = 0;
					row++;
				}
			}
			if (col > 0) {
				row++;
			}
			return row;
		}

		private static void sortEntries(java.util.List<ButtonEntry> entries) {
			entries.sort((a, b) -> {
				String la = a.label.toLowerCase(java.util.Locale.ROOT);
				String lb = b.label.toLowerCase(java.util.Locale.ROOT);
				int cmp = la.compareTo(lb);
				return cmp != 0 ? cmp : a.label.compareTo(b.label);
			});
		}

		private void applySearch(String query) {
			String q = normalizeSearchText(query);
			rebuildContentLayout(q);
			ScrollableReflection.scrollToTop(scrollLayout);
			if (lastArea != null) {
				doLayout(lastArea);
			} else {
				scrollLayout.arrangeElements();
			}
		}

		private void setActive(boolean active) {
			this.active = active;
			if (active) {
				header.visible = true;
				desc1.visible = true;
				desc2.visible = true;
				desc3.visible = true;
				searchBox.visible = true;
				applySearch(searchBox.getValue());
				return;
			}
			searchBox.setFocused(false);
			header.visible = false;
			desc1.visible = false;
			desc2.visible = false;
			desc3.visible = false;
			searchBox.visible = false;
			gameplayHeader.visible = false;
			visualHeader.visible = false;
			soundHeader.visible = false;
			for (int i = 0; i < gameplayEntries.size(); i++) {
				gameplayEntries.get(i).button.visible = false;
			}
			for (int i = 0; i < visualEntries.size(); i++) {
				visualEntries.get(i).button.visible = false;
			}
			for (int i = 0; i < soundEntries.size(); i++) {
				soundEntries.get(i).button.visible = false;
			}
		}

		private static final class ScrollableReflection {
			private static java.lang.reflect.Field containerField;
			private static java.lang.reflect.Method setScrollAmount;
			private static boolean initialized;

			private static void scrollToTop(ScrollableLayout layout) {
				if (layout == null) {
					return;
				}
				if (!initialized) {
					initialized = true;
					try {
						containerField = ScrollableLayout.class.getDeclaredField("container");
						containerField.setAccessible(true);
						Class<?> containerClass =
							Class.forName("net.minecraft.client.gui.components.ScrollableLayout$Container");
						setScrollAmount = containerClass.getDeclaredMethod("setScrollAmount", double.class);
						setScrollAmount.setAccessible(true);
					} catch (ReflectiveOperationException ignored) {
						containerField = null;
						setScrollAmount = null;
						return;
					}
				}
				if (containerField == null || setScrollAmount == null) {
					return;
				}
				try {
					Object container = containerField.get(layout);
					if (container != null) {
						setScrollAmount.invoke(container, 0.0d);
					}
				} catch (ReflectiveOperationException ignored) {
					// Safe fallback: skip scrolling if reflection fails.
				}
			}
		}

		private void rebuildContentLayout(String query) {
			ScrollableLayout previousScroll = scrollLayout;
			String q = normalizeSearchText(query);
			header.visible = true;
			desc1.visible = true;
			desc2.visible = true;
			desc3.visible = true;
			searchBox.visible = true;
			GridLayout layout = new GridLayout();
			layout.rowSpacing(3);
			layout.columnSpacing(12);
			int row = 0;
			layout.addChild(header, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());
			layout.addChild(desc1, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());
			layout.addChild(desc2, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());
			layout.addChild(desc3, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());
			layout.addChild(searchBox, row++, 0, 1, 2);
			layout.addChild(new SimpleSpacerWidget(CONFIG_LAYOUT_WIDTH, 4), row++, 0, 1, 2);

			boolean gameplayAll = !q.isEmpty() && SECTION_SEARCH_GAMEPLAY.contains(q);
			boolean visualAll = !q.isEmpty() && SECTION_SEARCH_VISUAL.contains(q);
			boolean soundAll = !q.isEmpty() && SECTION_SEARCH_SOUND.contains(q);

			int before = row;
			row = addSection(layout, gameplayHeader, gameplayEntries, row, q, gameplayAll);
			if (row != before) {
				layout.addChild(new SimpleSpacerWidget(CONFIG_LAYOUT_WIDTH, 4), row++, 0, 1, 2);
			}
			before = row;
			row = addSection(layout, visualHeader, visualEntries, row, q, visualAll);
			if (row != before) {
				layout.addChild(new SimpleSpacerWidget(CONFIG_LAYOUT_WIDTH, 4), row++, 0, 1, 2);
			}
			addSection(layout, soundHeader, soundEntries, row, q, soundAll);

			contentLayout = layout;
			scrollLayout = new ScrollableLayout(client, contentLayout, 0);
			if (active) {
				if (previousScroll != null) {
					previousScroll.visitChildren(element -> {
						if (element instanceof AbstractWidget widget) {
							removeWidget.accept(widget);
						}
					});
				}
				scrollLayout.visitChildren(element -> {
					if (element instanceof AbstractWidget widget) {
						addWidget.accept(widget);
					}
				});
			}
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
			lastArea = area;
			int x = area.left() + Math.max(0, (area.width() - CONFIG_LAYOUT_WIDTH) / 2);
			scrollLayout.setX(x);
			scrollLayout.setY(area.top());
			scrollLayout.setMinWidth(CONFIG_LAYOUT_WIDTH);
			scrollLayout.setMaxHeight(area.height());
			scrollLayout.arrangeElements();
		}
	}

	private static Component zoomLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_ZOOM), enabled);
	}

	private static Component durabilityLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_DURABILITY), enabled);
	}

	private static Component calculatorLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_CALC), enabled);
	}

	private static Component packGuardLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_PACK), enabled);
	}

	private static Component locatorLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_LOCATOR), enabled);
	}

	private static Component slotLockLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_SLOTLOCK), enabled);
	}

	private static Component debugSyncLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_SYNC), enabled);
	}

	private static Component autoSpawnLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_AUTOSPAWN), enabled);
	}

	private static Component specialNamesLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_SPECIALNAMES), enabled);
	}

	private static Component clanTagLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_CLANTAG), enabled);
	}

	private static Component frontCameraLabel(boolean disabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_FRONTCAM), !disabled);
	}

	private static Component inventoryEffectsLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_EFFECTS), enabled);
	}

	private static Component hudEffectsLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_HUDEFFECTS), enabled);
	}

	private static Component beaconSoundLabel(boolean enabled) {
		return labelWithState(Messages.get(Messages.Id.LABEL_BEACON_SOUND), enabled);
	}

	private static Component noAccessLabel() {
		return Component.literal(Messages.applyColorCodes(Messages.get(Messages.Id.LABEL_NO_ACCESS)));
	}

	private static Component labelWithState(String label, boolean enabled) {
		MutableComponent base = Component.literal(Messages.applyColorCodes(label));
		Component state = enabled
			? Component.translatable("options.on")
			: Component.translatable("options.off");
		return base.append(state);
	}

	private static String normalizeSearchText(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		StringBuilder out = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if ((ch == '&' || ch == '\u00A7') && i + 1 < value.length()) {
				i++;
				continue;
			}
			out.append(ch);
		}
		return out.toString().trim().toLowerCase(java.util.Locale.ROOT);
	}

	private static void saveClientSettings() {
		ClientConfigIO.saveClientSettings(new ClientSettingsConfig(
			CameraZoomState.isEnabled(),
			DurabilityGuardState.isEnabled(),
			InventoryCalculatorState.isEnabled(),
			ResourcePackGuardState.isEnabled(),
			LocatorBarState.isEnabled(),
			CheckItemClientModule.isSlotLockingEnabled(),
			AutoSpawnState.isEnabled(),
			SpecialNamesState.isEnabled(),
			FrontCameraToggleState.isDisabled(),
			InventoryEffectsState.isEnabled(),
			HudEffectsState.isEnabled(),
			ClanTagState.isEnabled(),
			BeaconSoundState.isEnabled()
		));
	}

	private static final class ChatToolsTab implements Tab {
		private final Component title = Component.literal(Messages.get(Messages.Id.TAB_CHAT));
		private final GridLayout contentLayout = new GridLayout();
		private final GridLayout controlsLayout = new GridLayout();
		private final GridLayout messagesLayout = new GridLayout();
		private ScreenRectangle lastArea;
		private final int maxMessages;
		private final boolean specialAccess;
		private final Button[] messageButtons;
		private final StringWidget[] timerLabels;
		private final long[] cooldownEnds;
		private final String[] messages;
		private final EditBox addInput;
		private final Button addButton;
		private final Button addOnlyButton;
		private final Button deleteButton;
		private final Button editSendChatButton;
		private final Button editSendOwnButton;
		private boolean adding;
		private int editingIndex = -1;

		private ChatToolsTab(ChatInputsConfig config, int maxMessages, boolean specialAccess) {
			this.maxMessages = Math.max(1, maxMessages);
			this.specialAccess = specialAccess;
			Minecraft client = Minecraft.getInstance();
			contentLayout.rowSpacing(CHAT_ROW_SPACING);
			contentLayout.columnSpacing(CHAT_COLUMN_SPACING);
			controlsLayout.rowSpacing(0);
			controlsLayout.columnSpacing(0);
			messagesLayout.rowSpacing(CHAT_ROW_SPACING);
			messagesLayout.columnSpacing(0);
			messageButtons = new Button[this.maxMessages];
			timerLabels = new StringWidget[this.maxMessages];
			cooldownEnds = normalizeCooldowns(config != null ? config.cooldownEnds : null);
			messages = normalizeMessages(config != null ? config.lines : null);
			for (int i = 0; i < this.maxMessages; i++) {
				int row = i;
				int index = i;
				Button messageButton = Button.builder(Component.literal(""), button -> beginEdit(index))
					.size(CHAT_MAIN_WIDTH, BUTTON_HEIGHT)
					.build();
				updateMessageButton(messageButton, messages[i]);
				messageButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_MSG)));
				messageButtons[i] = messageButton;
				messagesLayout.addChild(messageButton, row, 0);

				StringWidget timer = new StringWidget(
					CHAT_TIMER_WIDTH,
					BUTTON_HEIGHT,
					formatCooldownLabel(getRemainingMs(i)),
					client.font
				);
				timer.setColor(0xFFB0B0B0);
				timer.visible = messageButton.visible;
				timerLabels[i] = timer;
				messagesLayout.addChild(timer, row, 1, settings -> settings.paddingLeft(CHAT_GAP_TIMER));
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
			addInput.setHint(
				Component.literal(Messages.get(Messages.Id.CHAT_HINT_INPUT)).withStyle(ChatFormatting.GRAY)
			);
			addInput.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_CHAT_INPUT)));

			addButton = Button.builder(Component.literal(Messages.get(Messages.Id.BUTTON_ADD)), button -> {
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
			addButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_ADD)));

			addOnlyButton = Button.builder(Component.literal(Messages.get(Messages.Id.BUTTON_ADD)), button -> setAdding(true))
				.size(20, BUTTON_HEIGHT)
				.build();
			addOnlyButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_ADDONLY)));

			deleteButton = Button.builder(Component.literal(Messages.get(Messages.Id.BUTTON_DELETE)), button -> deleteEditingMessage())
				.size(CHAT_DELETE_WIDTH, BUTTON_HEIGHT)
				.build();
			deleteButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_DELETE)));

			editSendChatButton = Button.builder(Component.literal(Messages.get(Messages.Id.BUTTON_CHAT)), button -> sendEditToChat())
				.size(CHAT_ACTION_WIDTH, BUTTON_HEIGHT)
				.build();
			editSendChatButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_SENDCHAT)));
			editSendOwnButton = Button.builder(Component.literal(Messages.get(Messages.Id.BUTTON_SELF)), button -> sendEditToSelf())
				.size(CHAT_ACTION_WIDTH, BUTTON_HEIGHT)
				.build();
			editSendOwnButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_SENDOWN)));

			updateAddButtonsState();

			StringWidget header = new StringWidget(
				CHAT_LAYOUT_WIDTH,
				CHAT_LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CHAT_TITLE))),
				client.font
			);
			header.setColor(0xFFE6E6E6);
			int row = 0;
			contentLayout.addChild(header, row++, 0, settings -> settings.alignHorizontallyCenter());

			StringWidget desc1 = new StringWidget(
				CHAT_LAYOUT_WIDTH,
				CHAT_LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CHAT_DESC_1))),
				client.font
			);
			desc1.setColor(0xFFB8B8B8);
			contentLayout.addChild(desc1, row++, 0, settings -> settings.alignHorizontallyCenter());

			StringWidget desc2 = new StringWidget(
				CHAT_LAYOUT_WIDTH,
				CHAT_LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.CHAT_DESC_2))),
				client.font
			);
			desc2.setColor(0xFFB8B8B8);
			contentLayout.addChild(desc2, row++, 0, settings -> settings.alignHorizontallyCenter());

			contentLayout.addChild(new SimpleSpacerWidget(CHAT_LAYOUT_WIDTH, 4), row++, 0);

			controlsLayout.addChild(deleteButton, 0, 0);
			controlsLayout.addChild(new GapWidget(CHAT_GAP_DELETE, BUTTON_HEIGHT), 0, 1);
			controlsLayout.addChild(addInput, 0, 2);
			controlsLayout.addChild(new GapWidget(CHAT_GAP_INPUT, BUTTON_HEIGHT), 0, 3);
			controlsLayout.addChild(addButton, 0, 4);
			controlsLayout.addChild(editSendChatButton, 0, 4);
			controlsLayout.addChild(new GapWidget(CHAT_GAP_CHAT, BUTTON_HEIGHT), 0, 5);
			controlsLayout.addChild(editSendOwnButton, 0, 6);
			controlsLayout.addChild(new GapWidget(CHAT_GAP_TIMER, BUTTON_HEIGHT), 0, 7);
			controlsLayout.addChild(new StringWidget(CHAT_TIMER_WIDTH, BUTTON_HEIGHT, Component.empty(), client.font), 0, 8);
			controlsLayout.addChild(addOnlyButton, 0, 0, 1, 9);

			contentLayout.addChild(controlsLayout, row++, 0);
			contentLayout.addChild(messagesLayout, row, 0);
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
			contentLayout.visitChildren(element -> {
				if (element instanceof AbstractWidget widget) {
					consumer.accept(widget);
				}
			});
		}

		@Override
		public void doLayout(ScreenRectangle area) {
			lastArea = area;
			updateScrollLayout();
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
			deleteButton.visible = false;
			editSendChatButton.visible = false;
			editSendOwnButton.visible = false;
			if (!adding) {
				editingIndex = -1;
			}
			if (adding) {
				addInput.setFocused(true);
			}
			updateAddButtonsState();
		}

		private void setEditing(boolean value) {
			if (value) {
				addInput.visible = true;
				addOnlyButton.visible = false;
				addButton.visible = false;
				deleteButton.visible = true;
				editSendChatButton.visible = true;
				editSendOwnButton.visible = true;
				editSendChatButton.active = !isCooldownActive(editingIndex);
				addInput.setFocused(true);
			} else if (!adding) {
				addInput.visible = false;
				addButton.visible = false;
				deleteButton.visible = false;
				editSendChatButton.visible = false;
				editSendOwnButton.visible = false;
				addOnlyButton.visible = true;
				editSendChatButton.active = true;
			}
			updateAddButtonsState();
		}

		private void resetUi() {
			adding = false;
			editingIndex = -1;
			addInput.setValue("");
			addInput.setFocused(false);
			setAdding(false);
			setEditing(false);
		}

		private void beginEdit(int index) {
			if (index < 0 || index >= messages.length) {
				return;
			}
			scrollToTop();
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

		private void scrollToTop() {
			// No scrolling in this tab.
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

		private void deleteEditingMessage() {
			if (editingIndex < 0 || editingIndex >= messages.length) {
				return;
			}
			messages[editingIndex] = "";
			cooldownEnds[editingIndex] = 0L;
			compactMessages();
			saveMessages();
			addInput.setValue("");
			setEditing(false);
			editingIndex = -1;
			updateAddButtonsState();
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
			updateAddButtonsState();
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

		private void compactMessages() {
			int write = 0;
			for (int i = 0; i < messages.length; i++) {
				String value = messages[i];
				if (value == null || value.isEmpty()) {
					continue;
				}
				if (write != i) {
					messages[write] = value;
					cooldownEnds[write] = cooldownEnds[i];
				}
				write++;
			}
			for (int i = write; i < messages.length; i++) {
				messages[i] = "";
				cooldownEnds[i] = 0L;
			}
			for (int i = 0; i < messages.length; i++) {
				updateMessageButton(messageButtons[i], messages[i]);
				boolean visible = messages[i] != null && !messages[i].isEmpty();
				timerLabels[i].visible = visible;
				if (visible) {
					updateCooldownLabel(i);
				}
			}
			updateAddButtonsState();
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
			updateScrollLayout();
			if (editingIndex >= 0) {
				editSendChatButton.active = !isCooldownActive(editingIndex);
			}
			for (int i = 0; i < timerLabels.length; i++) {
				if (!timerLabels[i].visible) {
					continue;
				}
				updateCooldownLabel(i);
			}
		}

		private void updateScrollLayout() {
			if (lastArea == null) {
				return;
			}
			int x = lastArea.left() + Math.max(0, (lastArea.width() - CHAT_LAYOUT_WIDTH) / 2);
			contentLayout.setX(x);
			contentLayout.setY(lastArea.top());
			controlsLayout.arrangeElements();
			messagesLayout.arrangeElements();
			contentLayout.arrangeElements();
		}

		private static final class GapWidget extends AbstractWidget {
			private GapWidget(int width, int height) {
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
				updateAddButtonsState();
			}
		}

		private void updateAddButtonsState() {
			boolean hasSlot = findEmptySlot() >= 0;
			addButton.active = hasSlot;
			addOnlyButton.active = hasSlot;
			if (!hasSlot && !specialAccess) {
				addButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_ADD_LOCKED)));
				addOnlyButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_ADD_LOCKED)));
			} else {
				addButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_ADD)));
				addOnlyButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_ADDONLY)));
			}
		}

		private String[] normalizeMessages(String[] input) {
			String[] normalized = new String[maxMessages];
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

		private static String formatCooldown(long remainingMs) {
			long totalSeconds = remainingMs / 1000L;
			long minutes = totalSeconds / 60L;
			long seconds = totalSeconds % 60L;
			return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds);
		}

		private static Component formatCooldownLabel(long remainingMs) {
			if (remainingMs <= 0L) {
				return Component.literal(translateColorCodes(Messages.get(Messages.Id.CHAT_READY)));
			}
			return Component.literal(formatCooldown(remainingMs));
		}

		private long[] normalizeCooldowns(long[] input) {
			long[] normalized = new long[maxMessages];
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
		private static final int LIST_WIDTH = 320;
		private static final int LABEL_HEIGHT = 12;
		private static final int COLUMN_SPACING = 2;
		private static final int NAME_COLUMN_WIDTH = (LIST_WIDTH - COLUMN_SPACING) / 2;
		private static final int RIGHT_COLUMN_INSET = 8;
		private static final int COLOR_TITLE = 0xFFE6E6E6;
		private static final int COLOR_SUBTITLE = 0xFFB8B8B8;
		private static final int COLOR_SECTION = 0xFFFF5555;
		private static final int COLOR_ADMIN_PREFIX = 0xFFFF5555;
		private static final String SUPPORT_URL = "https://www.patreon.com/cw/NuuaGifter";
		private static final String TITLE = Messages.get(Messages.Id.SPECIAL_TITLE);
		private static final String DESC_LINE_1 = Messages.get(Messages.Id.SPECIAL_DESC_1);
		private static final String DESC_LINE_2 = Messages.get(Messages.Id.SPECIAL_DESC_2);
		private static final String DESC_LINE_3 = Messages.get(Messages.Id.SPECIAL_DESC_3);
		private static final String DESC_LINE_4 = Messages.get(Messages.Id.SPECIAL_DESC_4);
		private static final RainbowTextStyle NAME_STYLE =
			new RainbowTextStyle(0.25f, 0.66f, 1.0f, 1.0f, 180.0f, true);

		private Component title = Component.literal(translateColorCodes(Messages.get(Messages.Id.TAB_SPECIAL_RAINBOW)));
		private final GridLayout contentLayout = new GridLayout();
		private final ScrollableLayout scrollLayout;

		private SpecialMembersTab() {
			Minecraft client = Minecraft.getInstance();
			contentLayout.rowSpacing(3);
			contentLayout.columnSpacing(COLUMN_SPACING);

			int row = 0;
			StringWidget header = new StringWidget(
				LIST_WIDTH,
				LABEL_HEIGHT,
				Component.literal(translateColorCodes(TITLE)),
				client.font
			);
			header.setColor(COLOR_TITLE);
			contentLayout.addChild(header, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());

			StringWidget desc1 = new StringWidget(
				LIST_WIDTH,
				LABEL_HEIGHT,
				Component.literal(DESC_LINE_1),
				client.font
			);
			desc1.setColor(COLOR_SUBTITLE);
			contentLayout.addChild(desc1, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());

			StringWidget desc2 = new StringWidget(
				LIST_WIDTH,
				LABEL_HEIGHT,
				Component.literal(DESC_LINE_2),
				client.font
			);
			desc2.setColor(COLOR_SUBTITLE);
			contentLayout.addChild(desc2, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());

			StringWidget desc3 = new StringWidget(
				LIST_WIDTH,
				LABEL_HEIGHT,
				Component.literal(DESC_LINE_3),
				client.font
			);
			desc3.setColor(COLOR_SUBTITLE);
			contentLayout.addChild(desc3, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());

			StringWidget desc4 = new StringWidget(
				LIST_WIDTH,
				LABEL_HEIGHT,
				Component.literal(DESC_LINE_4),
				client.font
			);
			desc4.setColor(COLOR_SUBTITLE);
			contentLayout.addChild(desc4, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());

			Button supportButton = Button.builder(
				Component.literal(Messages.applyColorCodes(Messages.get(Messages.Id.BUTTON_SPECIAL_SUPPORT))),
				button -> Util.getPlatform().openUri(SUPPORT_URL)
			).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
			supportButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_SPECIAL_SUPPORT)));
			contentLayout.addChild(supportButton, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());

			contentLayout.addChild(new SpacerWidget(LIST_WIDTH, 4), row++, 0, 1, 2);

			StringWidget adminHeader = new StringWidget(
				LIST_WIDTH,
				LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.SPECIAL_SECTION_ADMIN))),
				client.font
			);
			adminHeader.setColor(COLOR_SECTION);
			contentLayout.addChild(adminHeader, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());

			PlayerMarkRegistry.Member[] admins = PlayerMarkRegistry.adminMembers();
			for (int i = 0; i < admins.length; i++) {
				PlayerMarkRegistry.Member member = admins[i];
				contentLayout.addChild(new RainbowNameWidget(member.name(), true, NAME_COLUMN_WIDTH, false), row++, 0);
			}

			contentLayout.addChild(new SpacerWidget(LIST_WIDTH, 4), row++, 0, 1, 2);

			StringWidget specialHeader = new StringWidget(
				LIST_WIDTH,
				LABEL_HEIGHT,
				Component.literal(translateColorCodes(Messages.get(Messages.Id.SPECIAL_SECTION_SPECIAL))),
				client.font
			);
			specialHeader.setColor(COLOR_SECTION);
			contentLayout.addChild(specialHeader, row++, 0, 1, 2, settings -> settings.alignHorizontallyCenter());

			PlayerMarkRegistry.Member[] specials = PlayerMarkRegistry.specialMembers();
			sortMembers(specials);
			if (specials.length > 0) {
				int col = 0;
				for (int i = 0; i < specials.length; i++) {
					PlayerMarkRegistry.Member member = specials[i];
					boolean alignRight = col == 1;
					contentLayout.addChild(new RainbowNameWidget(member.name(), false, NAME_COLUMN_WIDTH, alignRight), row, col);
					col++;
					if (col >= 2) {
						col = 0;
						row++;
					}
				}
				if (col > 0) {
					row++;
				}
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
			int x = area.left() + Math.max(0, (area.width() - LIST_WIDTH) / 2);
			scrollLayout.setX(x);
			scrollLayout.setY(area.top());
			scrollLayout.setMinWidth(LIST_WIDTH);
			scrollLayout.setMaxHeight(area.height());
			scrollLayout.arrangeElements();
		}

		private void updateAnimatedTitle(float timeSeconds, Font font) {
			title = rainbowBoldComponent(Messages.get(Messages.Id.TAB_SPECIAL), timeSeconds, font, TAB_TITLE_STYLE);
		}

		private static void sortMembers(PlayerMarkRegistry.Member[] members) {
			java.util.Arrays.sort(members, (a, b) -> {
				String nameA = a.name();
				String nameB = b.name();
				boolean digitA = nameA != null && !nameA.isEmpty() && Character.isDigit(nameA.charAt(0));
				boolean digitB = nameB != null && !nameB.isEmpty() && Character.isDigit(nameB.charAt(0));
				if (digitA != digitB) {
					return digitA ? -1 : 1;
				}
				if (nameA == null) {
					return nameB == null ? 0 : -1;
				}
				if (nameB == null) {
					return 1;
				}
				int cmp = nameA.compareToIgnoreCase(nameB);
				return cmp != 0 ? cmp : nameA.compareTo(nameB);
			});
		}

		private static final class RainbowNameWidget extends AbstractWidget {
			private final String name;
			private final boolean admin;
			private final boolean alignRight;

			private RainbowNameWidget(String name, boolean admin, int width, boolean alignRight) {
				super(0, 0, width, LABEL_HEIGHT, Component.empty());
				this.name = name == null ? "" : name;
				this.admin = admin;
				this.alignRight = alignRight;
			}

			@Override
			public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
				Font font = Minecraft.getInstance().font;
				int x = getX();
				int y = getY();
				int cursorX = x;
				if (alignRight) {
					int nameWidth = font.width(name);
					cursorX = x + Math.max(0, getWidth() - nameWidth - RIGHT_COLUMN_INSET);
				}
				if (admin) {
					graphics.drawString(font, "", cursorX, y, COLOR_ADMIN_PREFIX, false);
					cursorX += font.width("");
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

	private static Tooltip tooltip(String text) {
		if (text == null || text.isEmpty()) {
			return Tooltip.create(Component.empty());
		}
		String normalized = text.replace("/n", "\n");
		return Tooltip.create(Component.literal(Messages.applyColorCodes(normalized)));
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

	private static Component rainbowBoldComponent(String text, float timeSeconds, Font font,
		RainbowTextStyle style) {
		if (text == null || text.isEmpty()) {
			return Component.empty();
		}
		if (font == null) {
			font = Minecraft.getInstance().font;
		}
		float speed = style.speed();
		float saturation = clamp01(style.saturation());
		float brightness = clamp01(style.brightness());
		float alpha = clamp01(style.alpha());
		float wavelength = style.wavelengthPx() <= 0.0f ? 1.0f : style.wavelengthPx();
		float timePhase = timeSeconds * speed;

		MutableComponent out = Component.empty();
		int cursorX = 0;
		for (int i = 0; i < text.length(); i++) {
			String glyph = String.valueOf(text.charAt(i));
			int charWidth = font.width(glyph);
			float centerX = cursorX + (charWidth * 0.5f);
			float hue = positiveMod1(1.0f - ((centerX / wavelength) + timePhase));
			int color = applySaturationBrightness(colorFromPalette(hue), saturation, brightness, alpha);
			out.append(Component.literal(glyph).setStyle(Style.EMPTY.withColor(color).withBold(true)));
			cursorX += charWidth;
		}
		return out;
	}

	private static float positiveMod1(float value) {
		float mod = value - (float) Math.floor(value);
		if (mod < 0.0f) {
			mod += 1.0f;
		}
		return mod;
	}

	private static int colorFromPalette(float hue) {
		int count = RAINBOW_HEX.length;
		float scaled = hue * count;
		int idx = (int) scaled;
		float t = scaled - idx;
		int c0 = RAINBOW_HEX[idx % count];
		int c1 = RAINBOW_HEX[(idx + 1) % count];
		return lerpColor(c0, c1, t);
	}

	private static int lerpColor(int c0, int c1, float t) {
		int r0 = (c0 >> 16) & 0xFF;
		int g0 = (c0 >> 8) & 0xFF;
		int b0 = c0 & 0xFF;
		int r1 = (c1 >> 16) & 0xFF;
		int g1 = (c1 >> 8) & 0xFF;
		int b1 = c1 & 0xFF;
		int r = (int) (r0 + (r1 - r0) * t);
		int g = (int) (g0 + (g1 - g0) * t);
		int b = (int) (b0 + (b1 - b0) * t);
		return (0xFF << 24) | (r << 16) | (g << 8) | b;
	}

	private static int applySaturationBrightness(int color, float saturation, float brightness, float alpha) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		float rf = r / 255.0f;
		float gf = g / 255.0f;
		float bf = b / 255.0f;
		float gray = (rf + gf + bf) / 3.0f;
		rf = gray + (rf - gray) * saturation;
		gf = gray + (gf - gray) * saturation;
		bf = gray + (bf - gray) * saturation;
		rf *= brightness;
		gf *= brightness;
		bf *= brightness;
		int ai = (int) (alpha * 255.0f) & 0xFF;
		int ri = (int) (clamp01(rf) * 255.0f) & 0xFF;
		int gi = (int) (clamp01(gf) * 255.0f) & 0xFF;
		int bi = (int) (clamp01(bf) * 255.0f) & 0xFF;
		return (ai << 24) | (ri << 16) | (gi << 8) | bi;
	}

	private static float clamp01(float value) {
		if (value < 0.0f) {
			return 0.0f;
		}
		if (value > 1.0f) {
			return 1.0f;
		}
		return value;
	}

	private static final class SimpleSpacerWidget extends AbstractWidget {
		private SimpleSpacerWidget(int width, int height) {
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





