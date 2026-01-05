package de.nuua.primetooler.platform.inventory;

import de.nuua.primetooler.api.v1.client.inventoryscan.InventoryScanScheduler;
import de.nuua.primetooler.core.event.ClientTickEvent;
import de.nuua.primetooler.core.event.EventBus;
import de.nuua.primetooler.core.lifecycle.Bootstrap;
import de.nuua.primetooler.core.lifecycle.Module;
import net.minecraft.client.Minecraft;

/**
 * WHY: Drive inventory scan jobs from the internal client tick event.
 * PERF: Cheap per-tick scheduler check; jobs run at their own interval.
 */
public final class InventoryScanClientModule implements Module, ClientTickEvent {
	@Override
	public String id() {
		return "inventory_scan_client";
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
		InventoryScanScheduler.tick(Minecraft.getInstance());
	}
}

