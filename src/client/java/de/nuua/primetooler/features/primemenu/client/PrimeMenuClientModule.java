package de.nuua.primetooler.features.primemenu.client;

import de.nuua.primetooler.core.event.ClientTickEvent;
import de.nuua.primetooler.core.event.EventBus;
import de.nuua.primetooler.core.lifecycle.Bootstrap;
import de.nuua.primetooler.core.lifecycle.Module;
import de.nuua.primetooler.platform.input.PrimeToolerKeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class PrimeMenuClientModule implements Module, ClientTickEvent {
	private KeyMapping openMenuKey;

	@Override
	public String id() {
		return "prime_menu_client";
	}

	@Override
	public void preInit(Bootstrap ctx) {
	}

	@Override
	public void init(Bootstrap ctx) {
		PrimeToolerKeyBindings.registerAll();
		openMenuKey = PrimeToolerKeyBindings.openMenuKey();
		EventBus bus = ctx.get(EventBus.class);
		bus.register(ClientTickEvent.class, this);
	}

	@Override
	public void postInit(Bootstrap ctx) {
	}

	@Override
	public void onClientTick() {
		if (openMenuKey == null || !openMenuKey.consumeClick()) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (client == null || client.screen instanceof PrimeMenuScreen) {
			return;
		}
		client.setScreen(new PrimeMenuScreen(client.screen));
	}
}
