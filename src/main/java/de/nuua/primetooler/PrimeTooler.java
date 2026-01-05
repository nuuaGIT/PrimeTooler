package de.nuua.primetooler;

import de.nuua.primetooler.core.lifecycle.Boot;
import de.nuua.primetooler.core.lifecycle.EnvironmentInfo;
import de.nuua.primetooler.platform.PlatformEnvironment;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimeTooler implements ModInitializer {
	public static final String MOD_ID = "primetooler";
	public static final String VERSION = "26.0.7";
	/**
	 * WHY: Dev/testing switch to unlock special-member features for everyone locally.
	 * PERF: Static final flag; checked only at init and in UI gates.
	 */
	public static final boolean FORCE_SPECIAL_ACCESS = false;

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		EnvironmentInfo env = PlatformEnvironment.current();
		Boot.bootCommon(env);
		LOGGER.info("PrimeTooler wurde geladen. :3");
	}
}
