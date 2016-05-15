package cz.metacentrum.perun.dispatcher.jms;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "dispatcherQueuePool")
public class DispatcherQueuePool {

	private ConcurrentHashMap<Integer, DispatcherQueue> dispatcherQueuePool = new ConcurrentHashMap<Integer, DispatcherQueue>();
	private Iterator<DispatcherQueue> current = null;
	
	public boolean isThereDispatcherQueueForClient(int clientID) {
		return dispatcherQueuePool.containsKey(clientID);
	}

	public DispatcherQueue getDispatcherQueueByClient(int clientID) {
		if(clientID < 0) return null;
		return dispatcherQueuePool.get(clientID);
	}

	public void addDispatcherQueue(DispatcherQueue dispatcherQueue) {
		dispatcherQueuePool.put(dispatcherQueue.getClientID(), dispatcherQueue);
		current = null;
	}

	public void removeDispatcherQueue(DispatcherQueue dispatcherQueue) {
		current = null;
		dispatcherQueuePool.remove(dispatcherQueue.getClientID());
	}

	public int poolSize() {
		return dispatcherQueuePool.size();
	}

	public Collection<DispatcherQueue> getPool() {
		return Collections.unmodifiableCollection(dispatcherQueuePool.values());
	}

	public DispatcherQueue getAvailableQueue() {
		if(dispatcherQueuePool.isEmpty()) {
			return null;
		}
		if(current == null || !current.hasNext()) {
			current = dispatcherQueuePool.values().iterator();
		}
		return current.next();
	}

	public void removeDispatcherQueue(int clientID) {
		current = null;
		dispatcherQueuePool.remove(clientID);
	}
}
