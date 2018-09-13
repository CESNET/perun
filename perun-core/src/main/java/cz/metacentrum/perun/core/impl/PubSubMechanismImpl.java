package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.implApi.PubSubMechanism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class represents simple publish-subscribe pattern to distribute messages
 *
 * @author Richard Husár 445238@mail.muni.cz
 * @author Vojtech Sassmann &lt;vojtech.sassmann@gmail.com&gt;
 */
public class PubSubMechanismImpl implements PubSubMechanism {
	private final static Logger log = LoggerFactory.getLogger(PubSubMechanismImpl.class);

	private Map<Class, Set<Listener>> listeners;
	private Map<Pair<Class, Listener>, List<String>> listOfParams;

	private final Object listenersEditLock = new Object();


	/**
	 * Interface for listener to implement
	 */
	public interface Listener {
		void onEventReceived(Object event);
	}

	private PubSubMechanismImpl() {
		listeners = new ConcurrentHashMap<>();
		listOfParams = new ConcurrentHashMap<>();
	}

	@Override
	public void addListener(PubSubMechanismImpl.Listener listener, Class eventType) {
		add(eventType, listener, new ArrayList<>());
	}

	@Override
	public void addListener(PubSubMechanismImpl.Listener listener, Class... eventTypes) {
		for (Class eventType : eventTypes) {
			addListener(listener, eventType);
		}
	}

	@Override
	public void addListener(PubSubMechanismImpl.Listener listener, Class eventType, List<String> params) {
		add(eventType, listener, params);
	}

	private void add(Class eventType, Listener listener, List<String> params) {
		synchronized (listenersEditLock) {
			Set<Listener> eventListeners = listeners.get(eventType);

			if (eventListeners == null) {
				eventListeners = new CopyOnWriteArraySet<>();
				listeners.put(eventType, eventListeners);
			}

			eventListeners.add(listener);
			listOfParams.put(new Pair<>(eventType, listener), new ArrayList<>(params));
		}
	}

	@Override
	public void removeListener(PubSubMechanismImpl.Listener listener, Class eventType) {
		remove(listener, eventType);
	}

	@Override
	public void removeListenerParameters(PubSubMechanismImpl.Listener listener, Class eventType, List<String> paramsToRemove) {
		List<String> paramsOfListener = listOfParams.get(new Pair<>(eventType, listener));

		if (paramsOfListener == null) {
			return;
		}

		paramsOfListener.removeIf(paramsToRemove::contains);
	}

	@Override
	public void removeListener(PubSubMechanismImpl.Listener listener, Class... eventTypes) {
		for (Class eventType : eventTypes) {
			removeListener(listener, eventType);
		}
	}

	private void remove(Listener listener, Class eventType) {
		synchronized (listenersEditLock) {
			Set<Listener> eventListeners = listeners.get(eventType);
			if (eventListeners != null) {
				eventListeners.remove(listener);
			}
			listOfParams.remove(new Pair<>(eventType, listener));
		}
	}

	@Async
	@Override
	public void publishAsync(Object event) {
		publishAndWait(event);
	}

	@Override
	public void publishAndWait(Object event) {

		if (event == null) {
			return;
		}

		Class eventType = event.getClass();

		Set<Listener> eventListeners = listeners.get(eventType);

		if (eventListeners == null || eventListeners.isEmpty()) {
			return;
		}

		for (Listener listener : eventListeners) {
			//apply filter for params
			if (listOfParams.containsKey(new Pair<>(eventType, listener))) {      //contains params for listener and topic
				if (!listOfParams.get(new Pair<>(eventType, listener)).isEmpty()) { //check if event type does not have specific params to filter
					
					boolean satisfiesParams = true; //message must satisfy all parameters (parameter validation is conjunctive)
					
					for (String param :	listOfParams.get(new Pair<>(eventType, listener))) {
						if (!satisfiesParams) {
							break;
						}
						satisfiesParams = checkParams(param, event);
					}

					// if all parametes are found in message, then send message to listener
					if (satisfiesParams) {
						listener.onEventReceived(event);
					}
				} else {
					//if listener does not have specific parameters then send all messages of this type
					listener.onEventReceived(event);
				}

			}
		}
	}

	/**
	 * Checks if object representing message is containing given parameter
	 *
	 * @param param  parameter to be checkd
	 * @param object object to be checked
	 * @return true if object contains parameter, false otherwise
	 */
	private boolean checkParams(String param, Object object) {
		Pair<String, String> parsedParam = parseParams(param);
		if (parsedParam == null) {
			return false;
		}

		String[] parts = parsedParam.getLeft().split("\\.");
		int i = 0;
		//iterate into object properties
		while (object != null && i < parts.length) {
			object = getProperty(object, parts[i]);
			i++;
		}

		if (object == null) {
			return false;
		}

		String result = object.toString();

		return result.equals(parsedParam.getRight());
	}

	/**
	 * Parse parameters to pair of key and value
	 * Parsing functions based on '=' character
	 *
	 * @param param parameter to be parsed
	 * @return pair of key and value from parsed parameter
	 */
	private Pair<String, String> parseParams(String param) {
		if (param.contains("=")) {
			return new Pair<>(param.substring(0, param.indexOf("=")), param.substring(param.indexOf("=") + 1));
		}
		return null;
	}

	/**
	 * Gets property from object, throws exception if not allowed to access property or property does not exist
	 *
	 * @param bean         object of which property should be returned
	 * @param propertyName name of the property to be returned
	 * @return property of the object with propertyName
	 */
	public Object getProperty(Object bean, String propertyName) {
		BeanInfo info;

		try {
			info = Introspector.getBeanInfo(bean.getClass(), Object.class);

			PropertyDescriptor[] props = info.getPropertyDescriptors();
			for (PropertyDescriptor pd : props) {
				String name = pd.getName();
				Method getter = pd.getReadMethod();

				Object value;
				value = getter.invoke(bean);
				if (propertyName.equals(name)) {
					return value;
				}
			}
		} catch (IntrospectionException e) {
			log.error("IntrospectionException when getting property {} of object: {}", propertyName, bean.getClass().getName());
		} catch (IllegalAccessException e) {
			log.error("Could not access property {} of the given object: {}.", propertyName, bean.getClass().getName());
		} catch (InvocationTargetException e) {
			log.error("InvocationTargetException when getting property {} of object: {}", propertyName, bean.getClass().getName());
		}
		return null;
	}
}
