package de.nuua.primetooler.core.lifecycle;

import java.util.HashMap;
import java.util.Map;

public final class ServiceRegistry {
	private final Map<Class<?>, Object> services = new HashMap<>();

	public <T> void register(Class<T> type, T instance) {
		if (type == null || instance == null) {
			throw new IllegalArgumentException("Service type and instance must be non-null.");
		}
		if (services.putIfAbsent(type, instance) != null) {
			throw new IllegalStateException("Service already registered: " + type.getName());
		}
	}

	public <T> T get(Class<T> type) {
		Object instance = services.get(type);
		if (instance == null) {
			throw new IllegalStateException("Missing service: " + type.getName());
		}
		return type.cast(instance);
	}
}
