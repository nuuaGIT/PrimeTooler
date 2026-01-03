package de.nuua.primetooler.features.chatmention.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * WARUM: Eigenes Toast fuer Chat-Markierung mit Custom-Icon.
 * PERF: Reines Rendern, keine teuren Allokationen im Hotpath.
 */
public final class ChatMentionToast implements Toast {
	private static final Object TOKEN = new Object();
	private static final ResourceLocation BACKGROUND_SPRITE =
		ResourceLocation.withDefaultNamespace("toast/now_playing");
	private static final ResourceLocation ICON_SPRITE =
		ResourceLocation.withDefaultNamespace("icon/draft_report");
	private static final int ICON_SIZE = 16;
	private static final int ICON_X = 6;
	private static final int ICON_Y = 6;
	private static final int TEXT_X = 28;
	private static final int TITLE_Y = 7;
	private static final int MESSAGE_Y = 18;
	private static final int COLOR_TITLE = 0xFFEEEEEE;
	private static final int COLOR_MESSAGE = 0xFFB8B8B8;
	private static final long DURATION_MS = 2000L;
	private static final int MIN_WIDTH = 200;
	private static final int MAX_WIDTH = 320;
	private static final int TEXT_PADDING = 6;

	private Component title;
	private Component message;
	private Toast.Visibility visibility = Toast.Visibility.SHOW;
	private long startTimeMs = -1L;
	private int width;

	private ChatMentionToast(Component title, Component message) {
		this.title = title == null ? Component.empty() : title;
		this.message = message == null ? Component.empty() : message;
		this.width = MIN_WIDTH;
	}

	public static void addOrUpdate(ToastManager manager, Component title, Component message) {
		if (manager == null) {
			return;
		}
		ChatMentionToast toast = manager.getToast(ChatMentionToast.class, TOKEN);
		if (toast == null) {
			manager.addToast(new ChatMentionToast(title, message));
			return;
		}
		toast.reset(title, message);
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public Toast.Visibility getWantedVisibility() {
		return visibility;
	}

	@Override
	public void update(ToastManager manager, long timeMs) {
		if (startTimeMs < 0L) {
			startTimeMs = timeMs;
		}
		double multiplier = manager == null ? 1.0 : manager.getNotificationDisplayTimeMultiplier();
		long duration = (long) (DURATION_MS * multiplier);
		visibility = (timeMs - startTimeMs) >= duration ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
	}

	@Override
	public void render(GuiGraphics graphics, Font font, long timeMs) {
		updateWidth(font);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, width(), height());
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON_SPRITE, ICON_X, ICON_Y, ICON_SIZE, ICON_SIZE);
		graphics.drawString(font, title, TEXT_X, TITLE_Y, COLOR_TITLE, false);
		graphics.drawString(font, message, TEXT_X, MESSAGE_Y, COLOR_MESSAGE, false);
	}

	@Override
	public Object getToken() {
		return TOKEN;
	}

	private void reset(Component title, Component message) {
		this.title = title == null ? Component.empty() : title;
		this.message = message == null ? Component.empty() : message;
		startTimeMs = -1L;
		visibility = Toast.Visibility.SHOW;
		width = MIN_WIDTH;
	}

	private void updateWidth(Font font) {
		if (font == null) {
			return;
		}
		int titleWidth = font.width(title);
		int messageWidth = font.width(message);
		int contentWidth = Math.max(titleWidth, messageWidth) + TEXT_X + TEXT_PADDING;
		if (contentWidth < MIN_WIDTH) {
			width = MIN_WIDTH;
		} else if (contentWidth > MAX_WIDTH) {
			width = MAX_WIDTH;
		} else {
			width = contentWidth;
		}
	}
}
