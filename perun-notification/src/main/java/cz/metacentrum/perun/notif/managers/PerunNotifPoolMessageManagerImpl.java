package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.notif.utils.NotifUtils;
import cz.metacentrum.perun.notif.dao.PerunNotifPoolMessageDao;
import cz.metacentrum.perun.notif.dto.PoolMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifPoolMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import cz.metacentrum.perun.notif.utils.ParsedMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//TODO:optimalizace, preprocesing, pro vsechny template
@Service("perunNotifPoolMessageManager")
public class PerunNotifPoolMessageManagerImpl implements PerunNotifPoolMessageManager {

	public static final Logger logger = LoggerFactory.getLogger(PerunNotifPoolMessageManager.class);

	@Autowired
	private PerunNotifPoolMessageDao perunNotifPoolMessageDao;

	@Autowired
	private PerunNotifTemplateManager perunNotifTemplateManager;

	@Autowired
	private PerunBl perun;

	private PerunSession session;

	public static final String METHOD_CLASSNAME = "METHOD";

	public static final String DEFAULT_LOCALE = "en";

	private static final Map<String, ParsedMethod> parsedMethodCache = new ConcurrentHashMap<String, ParsedMethod>();

	@SuppressWarnings("unused")
	@PostConstruct
	private void init() throws Exception {
		if (!perun.isPerunReadOnly()) {
			perunNotifPoolMessageDao.setAllCreatedToNow();
		}
		session = NotifUtils.getPerunSession(perun);
	}

	public void savePerunNotifPoolMessages(List<PerunNotifPoolMessage> poolMessages) {

		for (PerunNotifPoolMessage message : poolMessages) {
			perunNotifPoolMessageDao.savePerunNotifPoolMessage(message);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public List<PerunNotifPoolMessage> createPerunNotifPoolMessagesForTemplates(Map<Integer, List<PerunNotifTemplate>> templatesWithRegexIds,
		PerunNotifAuditMessage perunAuditMessage) {

		List<PerunNotifPoolMessage> result = new ArrayList<PerunNotifPoolMessage>();
		// We parse recieved message from auditer to get all objects
		//List<PerunBean> retrievedObjects = ParseUtils.parseMessage(perunMessage.getMessage());
		List<PerunBean> retrievedObjects = AuditParser.parseLog(perunAuditMessage.getMessage());

		// Objects which can be later used when proccessing managerCalls
		Map<String, Object> usableObjects = parseRetrievedObjects(retrievedObjects);
		usableObjects.put(parseClassName(PerunSession.class.toString()), session);

		Map<String, String> retrievedProperties = new HashMap<String, String>();

		for (Integer regexId : templatesWithRegexIds.keySet()) {
			// We list through every regexId recognized in message
			List<PerunNotifTemplate> templates = templatesWithRegexIds.get(regexId);
			if ((templates != null) && (!templates.isEmpty())) {
				for (PerunNotifTemplate template : templates) {
					// We list through every template which uses regexId
					Map<String, String> retrievedPrimaryProperties = new HashMap<String, String>();
					Set<String> classNames = new HashSet<String>();
					classNames.addAll(template.getPrimaryProperties().keySet());
					for (String className : classNames) {

						if (className != null && !className.equals(METHOD_CLASSNAME)) {
							// Listing through all classNames
							try {
								logger.debug("Resolving class with name: " + className);
								Class resolvedClass = Class.forName(className);
								Object matchingObject = null;
								for (Object myObject : retrievedObjects) {
									if (resolvedClass.isAssignableFrom(myObject.getClass())) {
										matchingObject = myObject;
										logger.debug("Parsed object: " + matchingObject.toString() + " from message recognized for class: " + className);
									}
								}
								if (matchingObject != null) {
									if (template.getPrimaryProperties().get(className) != null) {

										List<String> methods = template.getPrimaryProperties().get(className);
										retrieveProperties(methods, className, retrievedPrimaryProperties, retrievedProperties, matchingObject);
									}
								} else {
									logger.error("No object recognized in objects from message for class: " + className);
								}
							} catch (ClassNotFoundException ex) {
								logger.error("Class from template cannot be resolved: " + className, ex);
							}
						}
					}

					if (template.getPrimaryProperties().get(METHOD_CLASSNAME) != null) {
						for (String methodName : template.getPrimaryProperties().get(METHOD_CLASSNAME)) {

							String value = retrieveMethodProperty(retrievedProperties, methodName, usableObjects);
							retrievedPrimaryProperties.put(methodName, value);
						}
					}

					if (retrievedPrimaryProperties != null && !retrievedPrimaryProperties.isEmpty()) {
						PerunNotifPoolMessage poolMessage = new PerunNotifPoolMessage();
						poolMessage.setCreated(Instant.now());
						poolMessage.setKeyAttributes(retrievedPrimaryProperties);
						poolMessage.setRegexId(regexId);
						poolMessage.setTemplateId(template.getId());
						poolMessage.setNotifMessage(perunAuditMessage.getMessage());

						result.add(poolMessage);
					}
				}
			} else {
				logger.info("No template for regex id: " + regexId + " found.");
			}
		}
		return result;
	}

	private void retrieveProperties(List<String> methods, String className, Map<String, String> resultProperties, Map<String, String> retrievedProperties, Object matchingObject) {

		for (String methodName : methods) {
			if (retrievedProperties.containsKey(className + "." + methodName)) {
				resultProperties.put(className + "." + methodName, retrievedProperties.get(className + "." + methodName));
				logger.debug("Method resolved from already retrievedProperties: " + className + "." + methodName);
			} else {

				Object methodResult = invokeMethodOnClassAndObject(methodName, matchingObject);
				if (methodResult != null) {
					resultProperties.put(className + "." + methodName, methodResult.toString());
					retrievedProperties.put(className + "." + methodName, methodResult.toString());
				}
			}
		}
	}

	private String retrieveMethodProperty(Map<String, String> retrievedProperties, String methodName, Map<String, Object> usableObjects) {

		if (retrievedProperties.containsKey(methodName)) {
			// Manager call retrieved from already retrieved
			// properties
			return retrievedProperties.get(methodName);
		} else {
			ParsedMethod parsedMethod = parsedMethodCache.get(methodName);
			if (parsedMethod == null) {
				parsedMethod = parseMethod(methodName, 0);
				if (parsedMethod != null) {
					parsedMethodCache.put(methodName, parsedMethod);
				}
			}
			Object value = processManagerCall(null, parsedMethod, retrievedProperties, usableObjects);
			if (value != null) {
				retrievedProperties.put(methodName, value.toString());
				return value.toString();
			}

			return null;
		}
	}

	private Map<String, Object> parseRetrievedObjects(List<PerunBean> retrievedObjects) {

		Map<String, Object> result = new HashMap<String, Object>();
		for (Object object : retrievedObjects) {
			result.put(parseClassName(object.getClass().toString()), object);
		}

		return result;
	}

	private String parseClassName(String className) {

		String result = className.replace("class", "");
		result = result.trim();

		return result;
	}

	@SuppressWarnings({"rawtypes"})
	private Object processManagerCall(Object target, ParsedMethod parsedMethod, Map<String, String> retrievedProperties, Map<String, Object> usableObjects) {

		try {
			switch (parsedMethod.getMethodType()) {
				case METHOD:
					Class partypes[] = new Class[parsedMethod.getParams().size()];
					Object argList[] = new Object[parsedMethod.getParams().size()];
					for (int i = 0; i < parsedMethod.getParams().size(); i++) {
						ParsedMethod param = parsedMethod.getParams().get(i);
						if (param != null) {
							// Parameters of methods are always in retrieved props.
							// Calling managers cannot be intersected
							Object paramResult = null;

							paramResult = processManagerCall(null, param, retrievedProperties, usableObjects);
							if (paramResult != null) {
								partypes[i] = paramResult.getClass();
								argList[i] = paramResult;
							}
						}
					}

					if (target == null) {
						target = perun;
					}
					Class targetClass = target.getClass();
					Method method = findMethod(targetClass, parsedMethod.getMethodName(), partypes);
					Object resultObject = method.invoke(target, argList);

					if (parsedMethod.getNextMethod() == null) {
						if (resultObject != null) {
							return resultObject;
						} else {
							logger.error("Result of " + parsedMethod.getMethodName() + " is null.");
							return null;
						}
					} else {
						return processManagerCall(resultObject, parsedMethod.getNextMethod(), retrievedProperties, usableObjects);
					}
				case CLASS:

					Object result = retrievedProperties.get(parsedMethod.getMethodName());
					if (result == null) {
						result = usableObjects.get(parsedMethod.getMethodName());
					}

					return result;
				case STRING_PARAM:
					return parsedMethod.getMethodName();
				case INTEGER_PARAM:
					Integer number = Integer.valueOf(parsedMethod.getMethodName());
					return number;
			}
		} catch (Exception ex) {
			logger.error("Error during processing manager call exception: " + ex.getCause(), ex);
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	private Method findMethod(Class targetClass, String methodName, Class[] partypes) {

		for (Method method : targetClass.getMethods()) {
			if (!method.getName().equals(methodName)) {
				continue;
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			boolean matches = true;
			for (int i = 0; i < parameterTypes.length; i++) {
				if (parameterTypes[i] != null && partypes[i] != null && !parameterTypes[i].isAssignableFrom(partypes[i])) {
					matches = false;
					break;
				}
			}
			if (matches) {
				return method;
			}
		}

		return null;
	}

	private ParsedMethod parseMethod(String className, Integer startPosition) {
		ParsedMethod result = new ParsedMethod();
		String methodName = "";
		for (int i = startPosition; i < className.length(); i++) {
			char character = className.charAt(i);
			if (character == '(') {
				result.setMethodName(methodName);
				result.setMethodType(ParsedMethod.MethodType.METHOD);

				Integer position = new Integer(i + 1);
				while (className.length() >= position && className.charAt(position) != ')') {
					ParsedMethod param = parseMethod(className, position);
					result.addParam(param);
					position = param.getLastPosition();
				}

				i = position;
			} else if (character == ')') {
				result.setMethodType(ParsedMethod.MethodType.STRING_PARAM);
				result.setMethodName(methodName);

				result.setLastPosition(i);
				return result;
			} else if (character == '.') {
				Integer position = new Integer(i + 1);
				ParsedMethod nextMethod = parseMethod(className, position);
				result.setNextMethod(nextMethod);
				result.setMethodName(methodName);
				if (className.charAt(i - 1) != ')') {
					result.setMethodType(ParsedMethod.MethodType.CLASS);
					result.setMethodName(result.getMethodName() + "." + nextMethod.getMethodName());
					result.setNextMethod(null);
				} else {
					result.setMethodType(ParsedMethod.MethodType.METHOD);
				}

				result.setLastPosition(nextMethod.getLastPosition());
				return result;
			} else if (character == ',') {
				if (result.getMethodType() == null || !(result.getMethodType().equals(ParsedMethod.MethodType.METHOD) && result.getParams() != null && result.getParams().size() > 0)) {
					result.setMethodType(ParsedMethod.MethodType.STRING_PARAM);
					result.setMethodName(methodName);
				}

				result.setLastPosition(i + 2);
				return result;
			} else if (character == '"') {
				StringBuilder builder = new StringBuilder();
				i++;
				character = className.charAt(i);
				while (character != '"') {
					builder.append(character);
					i++;
					character = className.charAt(i);
				}

				result.setLastPosition(i + 1);
				result.setMethodName(builder.toString());
				result.setMethodType(ParsedMethod.MethodType.STRING_PARAM);

				return result;
			} else if (Character.isDigit(character)) {
				StringBuilder number = new StringBuilder();
				number.append(character);
				i++;
				character = className.charAt(i);
				while (Character.isDigit(character)) {
					number.append(character);
					i++;
					character = className.charAt(i);
				}

				result.setMethodName(number.toString());
				result.setMethodType(ParsedMethod.MethodType.INTEGER_PARAM);
				result.setLastPosition(i + 1);

				return result;
			} else {
				methodName += character;
			}
		}

		return result;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Object invokeMethodOnClassAndObject(String methodName, Object matchingObject) {

		if (matchingObject == null) {
			return null;
		}
		Class resolvedClass = matchingObject.getClass();

		try {
			String preparedMethodName = prepareMethodName(methodName);
			logger.debug("Using reflection to get values for method: " + preparedMethodName);
			Method method = resolvedClass.getMethod(preparedMethodName);
			return method.invoke(matchingObject);
		} catch (NoSuchMethodException ex) {
			logger.error("Method for class: " + resolvedClass.toString() + " cannot be resolved: " + methodName);
		} catch (InvocationTargetException ex) {
			logger.error("Error during invocation of method: " + methodName, ex);
		} catch (IllegalAccessException ex) {
			logger.error("Illegal access using method: " + methodName + " on class: " + resolvedClass.toString(), ex);
		}

		return null;
	}

	private static String prepareMethodName(String methodName) {

		if (methodName != null && methodName.endsWith("()")) {

			return methodName.substring(0, methodName.length() - 2);
		}

		return methodName;
	}

	@Override
	public void processPerunNotifPoolMessagesFromDb() {

		//in format templateId = list<PoolMessage>
		Map<Integer, List<PoolMessage>> poolMessages = perunNotifPoolMessageDao.getAllPoolMessagesForProcessing();
		Set<Integer> proccessedIds = new HashSet<Integer>();
		for (Integer templateId : poolMessages.keySet()) {
			List<PoolMessage> notifMessages = poolMessages.get(templateId);
			// holds one message for user
			proccessedIds.addAll(perunNotifTemplateManager.processPoolMessages(templateId, notifMessages));
		}

		if (!proccessedIds.isEmpty()) {
			logger.info("Starting to remove procesed ids.");
			perunNotifPoolMessageDao.removeAllPoolMessages(proccessedIds);
		}
	}

	public PerunBl getPerun() {
		return perun;
	}

	public void setPerun(PerunBl perun) {
		this.perun = perun;
	}
}
