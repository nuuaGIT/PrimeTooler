package de.nuua.primetooler.features.uwu.client;

import de.nuua.primetooler.core.Messages;
import de.nuua.primetooler.core.config.ClientSettingsConfig;
import de.nuua.primetooler.features.autoangelsystem.client.AutoAngelSystemState;
import de.nuua.primetooler.features.autospawn.client.AutoSpawnState;
import de.nuua.primetooler.platform.config.ClientConfigIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * WHY: Hidden menu entry for UWU-ranked users.
 * PERF: Pure UI screen; no ticking or polling logic.
 */
public final class UwuSecretScreen extends Screen {
	private final Screen parent;
	private Button autoAngelButton;
	private AutoSpawnThresholdWidget autoSpawnWidget;

	public UwuSecretScreen(Screen parent) {
		super(Component.literal("???"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = width / 2;
		int y = Math.max(20, (height / 2) - 30);

		autoAngelButton = Button.builder(labelWithState(Messages.get(Messages.Id.LABEL_AUTO_ANGEL), AutoAngelSystemState.isEnabled()),
			button -> {
				boolean enabled = AutoAngelSystemState.toggleEnabled();
				button.setMessage(labelWithState(Messages.get(Messages.Id.LABEL_AUTO_ANGEL), enabled));
				persist(config -> config.autoAngelSystem = enabled);
			})
			.size(220, 20)
			.pos(centerX - 110, y)
			.build();
		autoAngelButton.setTooltip(tooltip(Messages.get(Messages.Id.TOOLTIP_AUTO_ANGEL)));
		addRenderableWidget(autoAngelButton);

		y += 26;
		autoSpawnWidget = new AutoSpawnThresholdWidget(centerX - 110, y, 220, 20);
		addRenderableWidget(autoSpawnWidget);

		int w = 120;
		int x = Math.max(0, (width - w) / 2);
		int backY = Math.max(0, height - 28);
		addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> close())
			.size(w, 20)
			.pos(x, backY)
			.build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		// WHY: Avoid blur; some renderers already blur once per frame (crash otherwise).
		// PERF: Simple solid fill; no blur pipeline usage.
		graphics.fill(0, 0, this.width, this.height, 0xB0101010);
	}

	@Override
	public void onClose() {
		close();
	}

	private void close() {
		Minecraft mc = Minecraft.getInstance();
		if (mc != null) {
			mc.setScreen(parent);
		}
	}

	private static Component labelWithState(String label, boolean enabled) {
		return Component.literal(Messages.applyColorCodes(label))
			.append(enabled ? Component.translatable("options.on") : Component.translatable("options.off"));
	}

	private static Tooltip tooltip(String text) {
		return Tooltip.create(Component.literal(Messages.applyColorCodes(text)));
	}

	private static void persist(java.util.function.Consumer<ClientSettingsConfig> mutator) {
		ClientSettingsConfig config = ClientConfigIO.loadClientSettings();
		mutator.accept(config);
		ClientConfigIO.saveClientSettings(config);
	}

	private static final class AutoSpawnThresholdWidget extends AbstractWidget {
		private static final float DEFAULT_HEARTS = AutoSpawnState.DEFAULT_HEARTS_THRESHOLD;

		private final Button toggleButton;
		private final EditBox input;
		private boolean editing;

		private AutoSpawnThresholdWidget(int x, int y, int width, int height) {
			super(x, y, width, height, labelWithState(Messages.get(Messages.Id.LABEL_AUTOSPAWN), AutoSpawnState.isEnabled()));

			Button[] toggleRef = new Button[1];
			toggleRef[0] = Button.builder(labelWithState(Messages.get(Messages.Id.LABEL_AUTOSPAWN), AutoSpawnState.isEnabled()),
				button -> {
					boolean enabled = AutoSpawnState.toggleEnabled();
					button.setMessage(labelWithState(Messages.get(Messages.Id.LABEL_AUTOSPAWN), enabled));
					setMessage(button.getMessage());
					persist(config -> config.autoSpawnLowHealth = enabled);
				})
				.size(width, height)
				.build();
			toggleButton = toggleRef[0];

			Minecraft client = Minecraft.getInstance();
			input = new EditBox(client.font, 0, 0, width, 18, Component.empty());
			input.setMaxLength(16);
			input.setCanLoseFocus(true);
			input.visible = false;

			updateTooltip();
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			syncChildPositions();
			toggleButton.active = this.active;
			if (!editing) {
				toggleButton.setMessage(getMessage());
				toggleButton.render(graphics, mouseX, mouseY, delta);
				return;
			}
			input.render(graphics, mouseX, mouseY, delta);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean unknown) {
			if (!visible) {
				return false;
			}
			if (event == null) {
				return false;
			}
			syncChildPositions();
			double mx = event.x();
			double my = event.y();

			if (editing) {
				if (mx < getX() || mx > getX() + getWidth() || my < getY() || my > getY() + getHeight()) {
					commitAndClose();
					return false;
				}
				return input.mouseClicked(event, unknown);
			}

			if (!this.active) {
				return false;
			}
			if (mx < getX() || mx > getX() + getWidth() || my < getY() || my > getY() + getHeight()) {
				return false;
			}
			if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
				openEditor();
				return true;
			}
			return toggleButton.mouseClicked(event, unknown);
		}

		@Override
		public boolean mouseReleased(MouseButtonEvent event) {
			if (!visible) {
				return false;
			}
			syncChildPositions();
			if (editing) {
				return input.mouseReleased(event);
			}
			return toggleButton.mouseReleased(event);
		}

		@Override
		public boolean keyPressed(KeyEvent event) {
			if (!visible) {
				return false;
			}
			if (!editing) {
				return toggleButton.keyPressed(event);
			}
			if (event != null && (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER)) {
				commitAndClose();
				return true;
			}
			return input.keyPressed(event);
		}

		@Override
		public boolean charTyped(CharacterEvent event) {
			if (!visible || !editing) {
				return false;
			}
			return input.charTyped(event);
		}

		@Override
		public void setFocused(boolean focused) {
			super.setFocused(focused);
			if (editing && !focused) {
				commitAndClose();
			}
			input.setFocused(editing && focused);
		}

		private void openEditor() {
			editing = true;
			input.visible = true;
			input.setValue(formatHearts(AutoSpawnState.heartsThreshold()));
			input.moveCursorToEnd(false);
			setFocused(true);
			input.setFocused(true);
		}

		private void commitAndClose() {
			if (!editing) {
				return;
			}
			float value = parseHearts(input.getValue());
			AutoSpawnState.setHeartsThreshold(value);
			persist(config -> config.autoSpawnHeartsThreshold = AutoSpawnState.heartsThreshold());
			updateTooltip();
			editing = false;
			input.visible = false;
			input.setFocused(false);
			setFocused(false);
		}

		private void updateTooltip() {
			setTooltip(tooltip(Messages.get(
				Messages.Id.TOOLTIP_AUTOSPAWN,
				formatHearts(AutoSpawnState.heartsThreshold())
			)));
		}

		private static float parseHearts(String raw) {
			if (raw == null) {
				return DEFAULT_HEARTS;
			}
			String value = raw.trim();
			if (value.isEmpty()) {
				return DEFAULT_HEARTS;
			}
			value = value.replace(',', '.');
			try {
				float parsed = Float.parseFloat(value);
				if (!Float.isFinite(parsed) || parsed <= 0.0f) {
					return DEFAULT_HEARTS;
				}
				return parsed;
			} catch (NumberFormatException ignored) {
				return DEFAULT_HEARTS;
			}
		}

		private static String formatHearts(float value) {
			if (!Float.isFinite(value) || value <= 0.0f) {
				return Float.toString(DEFAULT_HEARTS);
			}
			return Float.toString(value);
		}

		private void syncChildPositions() {
			int x = getX();
			int y = getY();
			toggleButton.setX(x);
			toggleButton.setY(y);
			input.setX(x);
			input.setY(y);
		}

		@Override
		protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narration) {
		}
	}
}
