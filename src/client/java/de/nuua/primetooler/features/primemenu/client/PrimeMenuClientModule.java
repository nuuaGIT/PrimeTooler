package de.nuua.primetooler.features.primemenu.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.nuua.primetooler.core.event.ClientTickEvent;
import de.nuua.primetooler.core.event.EventBus;
import de.nuua.primetooler.core.lifecycle.Bootstrap;
import de.nuua.primetooler.core.lifecycle.Module;
import de.nuua.primetooler.platform.input.KeyBindingRegistrar;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class PrimeMenuClientModule implements Module, ClientTickEvent {
	private static final String KEY_OPEN_MENU = "key.primetooler.open_menu";
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
		openMenuKey = KeyBindingRegistrar.register(
			KEY_OPEN_MENU,
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_P,
			KeyMapping.Category.MISC
		);
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
