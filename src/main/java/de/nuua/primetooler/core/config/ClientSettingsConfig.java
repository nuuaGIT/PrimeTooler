package de.nuua.primetooler.core.config;

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
		boolean hudEffects
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
	}
}
