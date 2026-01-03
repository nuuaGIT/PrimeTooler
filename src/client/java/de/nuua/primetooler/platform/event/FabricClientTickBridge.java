package de.nuua.primetooler.platform.event;

import de.nuua.primetooler.core.event.ClientTickEvent;
import de.nuua.primetooler.core.event.EventBus;
import de.nuua.primetooler.core.event.EventInvoker;
import de.nuua.primetooler.core.lifecycle.Bootstrap;
import de.nuua.primetooler.core.lifecycle.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class FabricClientTickBridge implements Module, ClientTickEvents.EndTick {
	private static final ClientTickInvoker INVOKER = new ClientTickInvoker();
	private EventBus bus;

	@Override
	public String id() {
		return "fabric_client_tick_bridge";
	}

	@Override
	public void preInit(Bootstrap ctx) {
	}

	@Override
	public void init(Bootstrap ctx) {
		bus = ctx.get(EventBus.class);
		ClientTickEvents.END_CLIENT_TICK.register(this);
	}

	@Override
	public void postInit(Bootstrap ctx) {
	}

	@Override
	public void onEndTick(Minecraft client) {
		if (bus == null) {
			return;
		}
		bus.post(ClientTickEvent.class, INVOKER);
	}

	private static final class ClientTickInvoker implements EventInvoker<ClientTickEvent> {
		@Override
		public void invoke(ClientTickEvent listener) {
			listener.onClientTick();
		}
	}
}
