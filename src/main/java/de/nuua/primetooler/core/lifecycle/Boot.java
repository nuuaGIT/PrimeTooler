package de.nuua.primetooler.core.lifecycle;

import de.nuua.primetooler.core.event.EventBus;

public final class Boot {
	private static final ServiceRegistry SERVICES = new ServiceRegistry();
	private static Bootstrap bootstrap;
	private static boolean commonBooted;
	private static boolean clientBooted;

	private Boot() {
	}

	public static void bootCommon(EnvironmentInfo env, Module... modules) {
		if (commonBooted) {
			return;
		}
		commonBooted = true;

		bootstrap = new DefaultBootstrap(SERVICES, env);
		SERVICES.register(EventBus.class, new EventBus());

		ModuleManager manager = new ModuleManager(bootstrap);
		registerModules(manager, modules);
		manager.run();
	}

	public static void bootClient(EnvironmentInfo env, Module... modules) {
		if (clientBooted) {
			return;
		}
		if (!commonBooted) {
			bootCommon(env);
		}
		clientBooted = true;

		if (bootstrap == null) {
			bootstrap = new DefaultBootstrap(SERVICES, env);
		}

		ModuleManager manager = new ModuleManager(bootstrap);
		registerModules(manager, modules);
		manager.run();
	}

	private static void registerModules(ModuleManager manager, Module[] modules) {
		if (modules == null) {
			return;
		}
		for (int i = 0; i < modules.length; i++) {
			manager.register(modules[i]);
		}
	}

	private static final class DefaultBootstrap implements Bootstrap {
		private final ServiceRegistry registry;
		private final EnvironmentInfo env;

		private DefaultBootstrap(ServiceRegistry registry, EnvironmentInfo env) {
			this.registry = registry;
			this.env = env;
		}

		@Override
		public <T> T get(Class<T> type) {
			return registry.get(type);
		}

		@Override
		public boolean isClient() {
			return env.isClient();
		}

		@Override
		public boolean isDev() {
			return env.isDev();
		}
	}
}
