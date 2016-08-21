package cz.metacentrum.perun.dispatcher.jms;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pool of Engine message queue producers for Engines.
 * Holds concurrent map of engine ID to instance of a message queue producer.
 *
 * @see cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "engineMessageProducerPool")
public class EngineMessageProducerPool {

	private ConcurrentHashMap<Integer, EngineMessageProducer> producersPool = new ConcurrentHashMap<Integer, EngineMessageProducer>();
	private Iterator<EngineMessageProducer> current = null;

	/**
	 * Return true if engine with specified ID has a message queue producer in a pool.
	 *
	 * @param clientID ID of Engine
	 * @return TRUE if engine has queue in pool / FALSE otherwise
	 */
	public boolean isThereProducerForClient(int clientID) {
		return producersPool.containsKey(clientID);
	}

	/**
	 * Get message queue producer for specified Engine ID.
	 *
	 * @param clientID ID of Engine
	 * @return Instance of a message queue producer
	 */
	public EngineMessageProducer getProducerByClient(int clientID) {
		if(clientID < 0) return null;
		return producersPool.get(clientID);
	}

	/**
	 * Add message queue producer to the pool
	 *
	 * @param engineMessageProducer Message producer queue to be added
	 */
	public void addProducer(EngineMessageProducer engineMessageProducer) {
		producersPool.put(engineMessageProducer.getClientID(), engineMessageProducer);
		current = null;
	}

	/**
	 * Remove message queue producer from the pool
	 *
	 * @param engineMessageProducer Message producer queue to be removed
	 */
	public void removeProducer(EngineMessageProducer engineMessageProducer) {
		current = null;
		producersPool.remove(engineMessageProducer.getClientID());
	}

	/**
	 * Return current size of the message queue producer pool
	 *
	 * @return Size of the message queue producer pool
	 */
	public int poolSize() {
		return producersPool.size();
	}

	/**
	 * Return all message queue producers in the pool as unmodifiable collection.
	 *
	 * @return All message queue producers from pool
	 */
	public Collection<EngineMessageProducer> getPool() {
		return Collections.unmodifiableCollection(producersPool.values());
	}

	/**
	 * Return first available message queue producer or null.
	 * Used when Task is going to be assigned to its first message queue producer.
	 *
	 * @return First available message queue producer from the pool or null
	 */
	public EngineMessageProducer getAvailableProducer() {
		if(producersPool.isEmpty()) {
			return null;
		}
		if(current == null || !current.hasNext()) {
			current = producersPool.values().iterator();
		}
		return current.next();
	}

	/**
	 * Remove message queue producer from the pool by ID of Engine.
	 *
	 * @param clientID ID of Engine
	 */
	public void removeProducer(int clientID) {
		current = null;
		producersPool.remove(clientID);
	}

}
