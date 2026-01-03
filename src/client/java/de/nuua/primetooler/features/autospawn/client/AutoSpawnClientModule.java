package de.nuua.primetooler.features.autospawn.client;

import de.nuua.primetooler.core.lifecycle.Bootstrap;
import de.nuua.primetooler.core.lifecycle.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * WHY: Trigger /spawn automatically when health is critically low.
 * PERF: Single float check per tick when enabled.
 */
public final class AutoSpawnClientModule implements Module {
	private static final float TRIGGER_RATIO = 0.10f;
	private static final float RESET_RATIO = 0.15f;
	private static boolean triggered;

	@Override
	public String id() {
		return "autospawn_client";
	}

	@Override
	public void preInit(Bootstrap ctx) {
	}

	@Override
	public void init(Bootstrap ctx) {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!AutoSpawnState.isEnabled()) {
				triggered = false;
				return;
			}
			Player player = client.player;
			if (player == null || !player.isAlive()) {
				triggered = false;
				return;
			}
			float max = player.getMaxHealth();
			if (max <= 0.0f) {
				return;
			}
			float ratio = player.getHealth() / max;
			if (ratio <= TRIGGER_RATIO) {
				if (!triggered) {
					sendSpawnCommand(client);
					triggered = true;
				}
				return;
			}
			if (ratio >= RESET_RATIO) {
				triggered = false;
			}
		});
	}

	@Override
	public void postInit(Bootstrap ctx) {
	}

	private static void sendSpawnCommand(Minecraft client) {
		if (client == null || client.getConnection() == null) {
			return;
		}
		client.getConnection().sendChat("/spawn");
	}
}
