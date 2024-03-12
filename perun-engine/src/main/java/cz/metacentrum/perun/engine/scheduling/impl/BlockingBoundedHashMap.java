package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.engine.scheduling.BlockingBoundedMap;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import org.springframework.util.Assert;

/**
 * Implementation of BlockingBoundedMap<K,V> using ConcurrentHashMap and Semaphore. Allow holding only specified number
 * of keys. Any thread waits on blockingPut() call if full.
 * <p>
 * Used to hold currently executing tasks in Engine.
 *
 * @param <K> key class
 * @param <V> value class
 * @see BlockingBoundedMap
 * @see Semaphore
 */
@Deprecated
public class BlockingBoundedHashMap<K, V> implements BlockingBoundedMap<K, V> {

  private ConcurrentMap<K, V> map = new ConcurrentHashMap<>();
  private Semaphore semaphore;

  /**
   * Create new BlockingBoundedHashMap with specified size limit.
   *
   * @param limit Number of allowed keys
   */
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
  public Collection<K> keySet() {
    return map.keySet();
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
