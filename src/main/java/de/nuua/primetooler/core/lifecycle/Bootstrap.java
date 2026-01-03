package de.nuua.primetooler.core.lifecycle;

public interface Bootstrap {
	<T> T get(Class<T> type);

	boolean isClient();

	boolean isDev();
}
