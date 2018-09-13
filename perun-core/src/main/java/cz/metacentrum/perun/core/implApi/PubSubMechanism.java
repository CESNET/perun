package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.impl.PubSubMechanismImpl;

import java.util.List;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface PubSubMechanism {

	/**
	 * Registers certain event type for listener.
	 *
	 * @param eventType type for which listener will receive messages
	 * @param listener listener
	 */
	void addListener(PubSubMechanismImpl.Listener listener, Class eventType);

	/**
	 * Register multiple event types for listener.
	 *
	 * @param listener listener
	 * @param eventTypes types for which listener will receive messages
	 */
	void addListener(PubSubMechanismImpl.Listener listener, Class... eventTypes);

	/**
	 * Registers certain event type for listener with specific parameters.
	 *
	 * @param eventType event type
	 * @param listener listener
	 * @param params list of parameters under which messages will be filtered (example: "user.id=43")
	 */
	void addListener(PubSubMechanismImpl.Listener listener, Class eventType, List<String> params);

	/**
	 * Removes subscription to event type for listener.
	 *
	 * @param eventType event type
	 * @param listener listener
	 */
	void removeListener(PubSubMechanismImpl.Listener listener, Class eventType);

	/**
	 * Removes only parameters from this subscription to certain event type for listener.
	 *
	 * @param eventType event type
	 * @param listener listener
	 * @param params parameters to be removed
	 */
	void removeListenerParameters(PubSubMechanismImpl.Listener listener, Class eventType, List<String> params);

	/**
	 * Removes subscription to more events for listener.
	 *
	 * @param listener listener
	 * @param eventTypes even types
	 */
	void removeListener(PubSubMechanismImpl.Listener listener, Class... eventTypes);

	/**
	 * Publishes message and wait until all listeners are notified.
	 *
	 * @param event actual message
	 */
	void publishAndWait(Object event);

	/**
	 * Publishes message asynchronously.
	 *
	 * @param event actual message
	 */
	void publishAsync(Object event);
}
