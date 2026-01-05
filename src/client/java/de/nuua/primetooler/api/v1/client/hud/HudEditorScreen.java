package de.nuua.primetooler.api.v1.client.hud;

import de.nuua.primetooler.api.v1.client.hud.HudLayoutState.NormalizedPos;
import de.nuua.primetooler.core.config.HudLayoutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * WHY: Generic editor for repositionable HUD elements (drag & drop + persist on Done).
 * PERF: Editor-only screen; no impact on in-game render hotpaths.
 */
public final class HudEditorScreen extends Screen {
	private static final int DONE_BUTTON_WIDTH = 120;
	private static final int DONE_BUTTON_HEIGHT = 20;
	private static final int DONE_BOTTOM_PADDING = 8;
	private static final int FRAME_COLOR = 0x66FFFFFF;
	private static final int FRAME_COLOR_ACTIVE = 0x99FFFF55;

	private final Screen returnTo;
	private final java.util.function.Consumer<HudLayoutConfig> onDone;
	private final java.util.HashMap<String, NormalizedPos> positions = new java.util.HashMap<>();
	private HudElement[] elements = new HudElement[0];

	private HudElement dragging;
	private int dragOffsetX;
	private int dragOffsetY;
	private boolean finished;

	public HudEditorScreen(
		Component title,
		Screen returnTo,
		HudLayoutConfig initialLayout,
		java.util.function.Consumer<HudLayoutConfig> onDone
	) {
		super(title == null ? Component.empty() : title);
		this.returnTo = returnTo;
		this.onDone = onDone;
		if (initialLayout != null && initialLayout.entries != null) {
			for (int i = 0; i < initialLayout.entries.size(); i++) {
				HudLayoutConfig.Entry entry = initialLayout.entries.get(i);
				if (entry == null || entry.id == null || entry.id.isEmpty()) {
					continue;
				}
				float x = HudLayoutState.clamp01(entry.x);
				float y = HudLayoutState.clamp01(entry.y);
				positions.put(entry.id, new NormalizedPos(x, y));
			}
		}
	}

	@Override
	protected void init() {
		elements = HudElementRegistry.elements();

		int x = Math.max(0, (width - DONE_BUTTON_WIDTH) / 2);
		int y = Math.max(0, height - DONE_BOTTOM_PADDING - DONE_BUTTON_HEIGHT);
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> finish())
			.pos(x, y)
			.size(DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT)
			.build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		// 1) Draw our own background (no blur) to avoid "Can only blur once per frame" when switching screens.
		graphics.fill(0, 0, width, height, 0xAA000000);

		Minecraft client = minecraft;
		if (client != null) {
			for (int i = 0; i < elements.length; i++) {
				HudElement element = elements[i];
				if (element == null) {
					continue;
				}
				int ew = Math.max(0, element.width(client));
				int eh = Math.max(0, element.height(client));
				int px = resolveX(element, ew);
				int py = resolveY(element, eh);

				element.render(graphics, client, px, py, delta, true);
				drawFrame(graphics, px, py, ew, eh, element == dragging);
			}
		}
		super.render(graphics, mouseX, mouseY, delta);
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		// no-op: background is rendered in render() to avoid blur.
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean unknown) {
		if (event == null || event.button() != 0) {
			return super.mouseClicked(event, unknown);
		}
		double mouseX = event.x();
		double mouseY = event.y();
		Minecraft client = minecraft;
		if (client == null) {
			return super.mouseClicked(event, unknown);
		}
		for (int i = elements.length - 1; i >= 0; i--) {
			HudElement element = elements[i];
			if (element == null) {
				continue;
			}
			int ew = Math.max(0, element.width(client));
			int eh = Math.max(0, element.height(client));
			int px = resolveX(element, ew);
			int py = resolveY(element, eh);
			if (mouseX >= px && mouseX <= px + ew && mouseY >= py && mouseY <= py + eh) {
				dragging = element;
				dragOffsetX = (int) mouseX - px;
				dragOffsetY = (int) mouseY - py;
				return true;
			}
		}
		return super.mouseClicked(event, unknown);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (event == null || event.button() != 0 || dragging == null || minecraft == null) {
			return super.mouseDragged(event, dragX, dragY);
		}
		double mouseX = event.x();
		double mouseY = event.y();
		int ew = Math.max(0, dragging.width(minecraft));
		int eh = Math.max(0, dragging.height(minecraft));
		int maxX = Math.max(0, width - ew);
		int maxY = Math.max(0, height - eh);
		int px = clampInt((int) mouseX - dragOffsetX, 0, maxX);
		int py = clampInt((int) mouseY - dragOffsetY, 0, maxY);
		float nx = HudLayoutState.defaultNormalized(px, maxX);
		float ny = HudLayoutState.defaultNormalized(py, maxY);
		positions.put(dragging.id(), new NormalizedPos(nx, ny));
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event != null && event.button() == 0) {
			dragging = null;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void onClose() {
		finish();
	}

	private void finish() {
		if (finished) {
			return;
		}
		finished = true;

		HudLayoutConfig config = new HudLayoutConfig();
		if (!positions.isEmpty()) {
			java.util.ArrayList<HudLayoutConfig.Entry> entries = new java.util.ArrayList<>(positions.size());
			for (java.util.Map.Entry<String, NormalizedPos> e : positions.entrySet()) {
				NormalizedPos pos = e.getValue();
				if (pos == null) {
					continue;
				}
				entries.add(new HudLayoutConfig.Entry(e.getKey(), pos.x(), pos.y()));
			}
			entries.sort((a, b) -> {
				String ia = a == null ? "" : a.id;
				String ib = b == null ? "" : b.id;
				return ia.compareTo(ib);
			});
			config.entries = entries;
		}

		if (onDone != null) {
			onDone.accept(config);
		}
		if (minecraft != null) {
			minecraft.setScreen(returnTo);
		}
	}

	private int resolveX(HudElement element, int elementWidth) {
		String id = element.id();
		NormalizedPos pos = positions.get(id);
		int defaultX = element.defaultX(minecraft, width, height);
		int max = Math.max(0, width - elementWidth);
		float norm = pos == null ? HudLayoutState.defaultNormalized(defaultX, max) : pos.x();
		return (int) (HudLayoutState.clamp01(norm) * (float) max + 0.5f);
	}

	private int resolveY(HudElement element, int elementHeight) {
		String id = element.id();
		NormalizedPos pos = positions.get(id);
		int defaultY = element.defaultY(minecraft, width, height);
		int max = Math.max(0, height - elementHeight);
		float norm = pos == null ? HudLayoutState.defaultNormalized(defaultY, max) : pos.y();
		return (int) (HudLayoutState.clamp01(norm) * (float) max + 0.5f);
	}

	private static void drawFrame(GuiGraphics graphics, int x, int y, int w, int h, boolean active) {
		if (w <= 0 || h <= 0) {
			return;
		}
		int c = active ? FRAME_COLOR_ACTIVE : FRAME_COLOR;
		int x2 = x + w;
		int y2 = y + h;
		graphics.fill(x, y, x2, y + 1, c);
		graphics.fill(x, y2 - 1, x2, y2, c);
		graphics.fill(x, y, x + 1, y2, c);
		graphics.fill(x2 - 1, y, x2, y2, c);
	}

	private static int clampInt(int value, int min, int max) {
		if (value < min) {
			return min;
		}
		return Math.min(value, max);
	}
}
