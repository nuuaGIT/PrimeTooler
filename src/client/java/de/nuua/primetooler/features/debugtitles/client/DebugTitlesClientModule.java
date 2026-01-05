package de.nuua.primetooler.features.debugtitles.client;

import de.nuua.primetooler.core.Messages;
import de.nuua.primetooler.core.event.ClientTickEvent;
import de.nuua.primetooler.core.event.EventBus;
import de.nuua.primetooler.core.lifecycle.Bootstrap;
import de.nuua.primetooler.core.lifecycle.Module;
import de.nuua.primetooler.features.playermark.client.PlayerMarkRegistry;
import de.nuua.primetooler.mixin.client.GuiTitleAccessor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;

/**
 * WHY: Quick debug helper to inspect active title/subtitle text.
 * PERF: Tick listener is gated; chat output only every 10 ticks when enabled.
 */
public final class DebugTitlesClientModule implements Module, ClientTickEvent {
	private static final int INTERVAL_TICKS = 10; // 0.5s at 20 TPS

	private boolean enabled;
	private int tickCounter;

	@Override
	public String id() {
		return "debug_titles_client";
	}

	@Override
	public void preInit(Bootstrap ctx) {
	}

	@Override
	public void init(Bootstrap ctx) {
		EventBus bus = ctx.get(EventBus.class);
		bus.register(ClientTickEvent.class, this);

		DebugTitlesClientModule self = this;
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
				ClientCommandManager.literal("primetooler")
					.then(
						ClientCommandManager.literal("debugTitles")
							.requires(source -> PlayerMarkRegistry.currentUserRank() == PlayerMarkRegistry.Rank.ADMIN)
							.executes(context -> {
								self.toggle();
								return 1;
							})
							.then(ClientCommandManager.literal("on").executes(context -> {
								self.setEnabled(true);
								return 1;
							}))
							.then(ClientCommandManager.literal("off").executes(context -> {
								self.setEnabled(false);
								return 1;
							}))
					)
			);
		});
	}

	@Override
	public void postInit(Bootstrap ctx) {
	}

	@Override
	public void onClientTick() {
		if (!enabled) {
			return;
		}

		if (++tickCounter < INTERVAL_TICKS) {
			return;
		}
		tickCounter = 0;

		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null) {
			return;
		}
		Gui gui = client.gui;
		if (gui == null) {
			return;
		}

		GuiTitleAccessor accessor = (GuiTitleAccessor) gui;
		String titleText = componentText(accessor.primetooler$getTitle());
		String subtitleText = componentText(accessor.primetooler$getSubtitle());

		client.player.displayClientMessage(msg(Messages.Id.DEBUG_TITLES_TITLE, titleText), false);
		client.player.displayClientMessage(msg(Messages.Id.DEBUG_TITLES_SUBTITLE, subtitleText), false);
	}

	private void toggle() {
		setEnabled(!enabled);
	}

	private void setEnabled(boolean enabled) {
		this.enabled = enabled;
		this.tickCounter = 0;

		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null) {
			return;
		}
		client.player.displayClientMessage(
			msg(Messages.Id.DEBUG_TITLES_STATE, enabled ? Messages.get(Messages.Id.STATE_ON) : Messages.get(Messages.Id.STATE_OFF)),
			false
		);
	}

	private static String componentText(Component component) {
		if (component == null) {
			return Messages.get(Messages.Id.DEBUG_TITLES_EMPTY);
		}
		String value = component.getString();
		if (value == null || value.isEmpty()) {
			return Messages.get(Messages.Id.DEBUG_TITLES_EMPTY);
		}
		return value;
	}

	private static Component msg(Messages.Id id, Object... args) {
		return Component.literal(Messages.applyColorCodes(Messages.get(id, args)));
	}
}
