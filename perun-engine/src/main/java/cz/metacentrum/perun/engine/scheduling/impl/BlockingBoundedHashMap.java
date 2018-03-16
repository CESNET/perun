package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.engine.scheduling.BlockingBoundedMap;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

public class BlockingBoundedHashMap<K, V> implements BlockingBoundedMap<K, V> {

	private ConcurrentMap<K, V> map = new ConcurrentHashMap<>();
	private Semaphore semaphore;

	public BlockingBoundedHashMap(int limit) {
		semaphore = new Semaphore(limit);
	}

	@Override
	public V blockingPut(K key, V value) throws InterruptedException {
		Assert.isTrue(value != null);

		semaphore.acquire();
		return map.put(key, value);
	}

	@Override
	public V remove(K key) {
		V removed = map.remove(key);
		if (removed != null) {
			semaphore.release();
		}
		return removed;
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}
}
