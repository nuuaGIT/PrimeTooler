package de.nuua.primetooler.platform;

import de.nuua.primetooler.core.lifecycle.EnvironmentInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class PlatformEnvironment {
	private PlatformEnvironment() {
	}

	public static EnvironmentInfo current() {
		FabricLoader loader = FabricLoader.getInstance();
		boolean isClient = loader.getEnvironmentType() == EnvType.CLIENT;
		boolean isDev = loader.isDevelopmentEnvironment();
		return new EnvironmentInfo(isClient, isDev);
	}
}
