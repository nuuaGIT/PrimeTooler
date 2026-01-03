package de.nuua.primetooler.core.config;

import com.google.gson.annotations.SerializedName;

/**
 * WHY: Persist client-only toggle state across sessions.
 * PERF: Plain data holder, no logic.
 */
public final class ClientSettingsConfig {
	public boolean unlimitedZoom;
	public boolean durabilityGuard;
	public boolean inventoryCalc = true;
	public boolean blockServerPacks;
	public boolean locatorBar = true;
	public boolean slotLocking = true;
	public boolean autoSpawnLowHealth;
	public boolean specialNames = true;
	public boolean disableFrontCamera;
	public boolean inventoryEffects = true;
	public boolean hudEffects = true;
	public boolean hideClanTag;
	@SerializedName("muteBeaconSound")
	public boolean muteBoosterSound;
	public boolean muteJackpotSound;
	public boolean chatMention;
	public int doubleDropMode;
	public int warningSoundVolume = 100;

	public ClientSettingsConfig() {
	}

	public ClientSettingsConfig(
		boolean unlimitedZoom,
		boolean durabilityGuard,
		boolean inventoryCalc,
		boolean blockServerPacks,
		boolean locatorBar,
		boolean slotLocking,
		boolean autoSpawnLowHealth,
		boolean specialNames,
		boolean disableFrontCamera,
		boolean inventoryEffects,
		boolean hudEffects,
		boolean hideClanTag,
		boolean muteBoosterSound,
		boolean muteJackpotSound,
		boolean chatMention,
		int doubleDropMode,
		int warningSoundVolume
	) {
		this.unlimitedZoom = unlimitedZoom;
		this.durabilityGuard = durabilityGuard;
		this.inventoryCalc = inventoryCalc;
		this.blockServerPacks = blockServerPacks;
		this.locatorBar = locatorBar;
		this.slotLocking = slotLocking;
		this.autoSpawnLowHealth = autoSpawnLowHealth;
		this.specialNames = specialNames;
		this.disableFrontCamera = disableFrontCamera;
		this.inventoryEffects = inventoryEffects;
		this.hudEffects = hudEffects;
		this.hideClanTag = hideClanTag;
		this.muteBoosterSound = muteBoosterSound;
		this.muteJackpotSound = muteJackpotSound;
		this.chatMention = chatMention;
		this.doubleDropMode = doubleDropMode;
		this.warningSoundVolume = warningSoundVolume;
	}
}
