package de.nuua.primetooler.core.event;

@FunctionalInterface
public interface EventInvoker<T> {
	void invoke(T listener);
}
