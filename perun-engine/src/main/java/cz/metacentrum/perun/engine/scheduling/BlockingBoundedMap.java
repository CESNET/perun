package cz.metacentrum.perun.engine.scheduling;

import java.util.Collection;

/**
 * This class wraps classic Map class, making its size limited an providing blocking behaviour when the limit is reached.
 *
 * @param <K> Maps key
 * @param <V> Maps value
 *
 * @author David Šarman
 */
public interface BlockingBoundedMap<K, V> {

	/**
	 * Puts the value into the Map, possibly blocking until there is open space.
	 * @param value Value to be inserted into the map.
	 * @param key Key under which will be the value inserted
	 * @return Return value previously associated with the key, null if there was none.
	 * @throws InterruptedException Thrown when the Thread was interrupted while blocking.
	 */
	V blockingPut(K key, V value) throws InterruptedException;

	V remove(K key);

	Collection<V> values();
}
