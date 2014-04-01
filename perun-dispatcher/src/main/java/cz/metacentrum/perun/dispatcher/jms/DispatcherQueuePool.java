package cz.metacentrum.perun.dispatcher.jms;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Michal Karm Babacek
 * JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "dispatcherQueuePool")
public class DispatcherQueuePool {

	private ConcurrentHashMap<Integer, DispatcherQueue> dispatcherQueuePool = new ConcurrentHashMap<Integer, DispatcherQueue>();

	public boolean isThereDispatcherQueueForClient(String clientID) {
		return dispatcherQueuePool.containsKey(clientID);
	}

	public DispatcherQueue getDispatcherQueueByClient(int clientID) {
		return dispatcherQueuePool.get(clientID);
	}

	public void addDispatcherQueue(DispatcherQueue dispatcherQueue) {
		dispatcherQueuePool.put(dispatcherQueue.getClientID(), dispatcherQueue);
	}

	public void removeDispatcherQueue(DispatcherQueue dispatcherQueue) {
		dispatcherQueuePool.remove(dispatcherQueue.getClientID());
	}

	public int poolSize(){
		return dispatcherQueuePool.size();
	}

	public Collection<DispatcherQueue> getPool() {
		return Collections.unmodifiableCollection(dispatcherQueuePool.values());
	}
}
