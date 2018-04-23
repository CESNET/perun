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
@Deprecated
public interface BlockingBoundedMap<K, V> {

	/**
	 * Puts the value into the Map, possibly blocking until there is open space.
	 *
	 * @param value Value to be inserted into the map.
	 * @param key Key under which will be the value inserted
	 * @return Return value previously associated with the key, null if there was none.
	 * @throws InterruptedException Thrown when the Thread was interrupted while blocking.
	 */
	V blockingPut(K key, V value) throws InterruptedException;

	/**
	 * Removes key=value from the Map, releasing its space for another blockingPut()
	 *
	 * @param key Key to be removed
	 * @return Removed value
	 */
	V remove(K key);

	/**
	 * Get all values currently held by this blocking map
	 *
	 * @return All values
	 */
	Collection<V> values();

	/**
	 * Get all keys currently held by this blocking map
	 * @return
	 */
	Collection<K> keySet();

}
