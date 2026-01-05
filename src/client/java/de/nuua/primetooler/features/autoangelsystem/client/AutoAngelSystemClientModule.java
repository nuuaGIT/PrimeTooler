package de.nuua.primetooler.features.autoangelsystem.client;

import de.nuua.primetooler.core.event.ClientTickEvent;
import de.nuua.primetooler.core.event.EventBus;
import de.nuua.primetooler.core.lifecycle.Bootstrap;
import de.nuua.primetooler.core.lifecycle.Module;

/**
 * WHY: Client-side runner for AutoAngelSystem.
 * PERF: Tick listener runs every 0.1s when enabled; constant-time overhead when disabled.
 */
public final class AutoAngelSystemClientModule implements Module, ClientTickEvent {
	private static final int INTERVAL_TICKS = 2; // 0.1s at 20 TPS

	private int tickCounter;

	@Override
	public String id() {
		return "auto_angel_system_client";
	}

	@Override
	public void preInit(Bootstrap ctx) {
	}

	@Override
	public void init(Bootstrap ctx) {
		EventBus bus = ctx.get(EventBus.class);
		bus.register(ClientTickEvent.class, this);
	}

	@Override
	public void postInit(Bootstrap ctx) {
	}

	@Override
	public void onClientTick() {
		// 1) Ensure any scheduled "use" press is applied/released in real time.
		AutoAngelSystemState.tickClient();

		if (!AutoAngelSystemState.isEnabled()) {
			tickCounter = 0;
			return;
		}
		if (++tickCounter < INTERVAL_TICKS) {
			return;
		}
		tickCounter = 0;
		AutoAngelSystemState.pollClient();
	}
}
