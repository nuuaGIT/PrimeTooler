package de.nuua.primetooler.core.event;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class EventBus {
	private final Map<Class<?>, ListenerList<?>> listeners = new HashMap<>();

	public <T> void register(Class<T> type, T listener) {
		if (type == null || listener == null) {
			throw new IllegalArgumentException("Event type and listener must be non-null.");
		}
		ListenerList<T> list = getOrCreate(type);
		list.add(listener);
	}

	public <T> void post(Class<T> type, EventInvoker<T> invoker) {
		ListenerList<T> list = get(type);
		if (list == null) {
			return;
		}

		T[] snapshot = list.array();
		for (int i = 0; i < snapshot.length; i++) {
			invoker.invoke(snapshot[i]);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> ListenerList<T> get(Class<T> type) {
		return (ListenerList<T>) listeners.get(type);
	}

	private <T> ListenerList<T> getOrCreate(Class<T> type) {
		ListenerList<?> list = listeners.get(type);
		if (list != null) {
			@SuppressWarnings("unchecked")
			ListenerList<T> cast = (ListenerList<T>) list;
			return cast;
		}
		ListenerList<T> created = new ListenerList<>(type);
		listeners.put(type, created);
		return created;
	}

	private static final class ListenerList<T> {
		private final Class<T> type;
		private final ArrayList<T> mutable = new ArrayList<>(4);
		private T[] snapshot;

		private ListenerList(Class<T> type) {
			this.type = type;
			this.snapshot = newArray(0);
		}

		void add(T listener) {
			mutable.add(listener);
			snapshot = mutable.toArray(newArray(mutable.size()));
		}

		T[] array() {
			return snapshot;
		}

		@SuppressWarnings("unchecked")
		private T[] newArray(int size) {
			return (T[]) Array.newInstance(type, size);
		}
	}
}
