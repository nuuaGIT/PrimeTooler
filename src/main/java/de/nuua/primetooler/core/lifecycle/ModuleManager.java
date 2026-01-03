package de.nuua.primetooler.core.lifecycle;

import java.util.ArrayList;

public final class ModuleManager {
	private final Bootstrap bootstrap;
	private final ArrayList<Module> modules = new ArrayList<>(8);

	public ModuleManager(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public void register(Module module) {
		if (module == null) {
			throw new IllegalArgumentException("Module must be non-null.");
		}
		modules.add(module);
	}

	public void run() {
		// 1) Prepare data (no side effects).
		for (int i = 0; i < modules.size(); i++) {
			modules.get(i).preInit(bootstrap);
		}

		// 2) Register content, events, and networking.
		for (int i = 0; i < modules.size(); i++) {
			modules.get(i).init(bootstrap);
		}

		// 3) Final wiring and dependency checks.
		for (int i = 0; i < modules.size(); i++) {
			modules.get(i).postInit(bootstrap);
		}
	}
}
