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
	public float autoSpawnHeartsThreshold = 2.5f;
	public boolean specialNames = true;
	public boolean disableFrontCamera;
	public boolean inventoryEffects = true;
	public boolean hudEffects = true;
	public boolean moveActionbar;
	public boolean terminalStackCount;
	public boolean fishbagTotal;
	public boolean fishbagWeight;
	public boolean fishbagCoins;
	public boolean fishMoneyTracker;
	public boolean overallCoinsTracker;
	public boolean jobXpTracker;
	public boolean jobMoneyTracker;
	public boolean autoAngelSystem;
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
		float autoSpawnHeartsThreshold,
		boolean specialNames,
		boolean disableFrontCamera,
		boolean inventoryEffects,
		boolean hudEffects,
		boolean moveActionbar,
		boolean terminalStackCount,
		boolean fishbagTotal,
		boolean fishbagWeight,
		boolean fishbagCoins,
		boolean fishMoneyTracker,
		boolean overallCoinsTracker,
		boolean jobXpTracker,
		boolean jobMoneyTracker,
		boolean autoAngelSystem,
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
		this.autoSpawnHeartsThreshold = autoSpawnHeartsThreshold;
		this.specialNames = specialNames;
		this.disableFrontCamera = disableFrontCamera;
		this.inventoryEffects = inventoryEffects;
		this.hudEffects = hudEffects;
		this.moveActionbar = moveActionbar;
		this.terminalStackCount = terminalStackCount;
		this.fishbagTotal = fishbagTotal;
		this.fishbagWeight = fishbagWeight;
		this.fishbagCoins = fishbagCoins;
		this.fishMoneyTracker = fishMoneyTracker;
		this.overallCoinsTracker = overallCoinsTracker;
		this.jobXpTracker = jobXpTracker;
		this.jobMoneyTracker = jobMoneyTracker;
		this.autoAngelSystem = autoAngelSystem;
		this.hideClanTag = hideClanTag;
		this.muteBoosterSound = muteBoosterSound;
		this.muteJackpotSound = muteJackpotSound;
		this.chatMention = chatMention;
		this.doubleDropMode = doubleDropMode;
		this.warningSoundVolume = warningSoundVolume;
	}
}
