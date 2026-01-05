package de.nuua.primetooler.features.jobtracker.client;

import de.nuua.primetooler.core.event.ClientTickEvent;
import de.nuua.primetooler.core.event.EventBus;
import de.nuua.primetooler.core.lifecycle.Bootstrap;
import de.nuua.primetooler.core.lifecycle.Module;

/**
 * WHY: Client-side runner for the Jobs actionbar XP/money trackers.
 * PERF: Polls every 0.5s when enabled; constant-time overhead when disabled.
 */
public final class JobTrackerClientModule implements Module, ClientTickEvent {
	private static final int INTERVAL_TICKS = 10; // 0.5s at 20 TPS

	private int tickCounter;

	@Override
	public String id() {
		return "job_tracker_client";
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
		if (!JobTrackerState.anyEnabled()) {
			tickCounter = 0;
			return;
		}
		if (++tickCounter < INTERVAL_TICKS) {
			return;
		}
		tickCounter = 0;
		JobTrackerState.pollClient();
	}
}

