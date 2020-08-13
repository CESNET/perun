package cz.metacentrum.perun.core.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Beans Utilities.
 *
 */
public class BeansUtils {

	private final static Logger log = LoggerFactory.getLogger(BeansUtils.class);

	private final static Pattern patternForCommonNameParsing = Pattern.compile("(([\\w]*. )*)([\\p{L}-']+) ([\\p{L}-']+)[, ]*(.*)");
	private final static Pattern richBeanNamePattern = Pattern.compile("^Rich([A-Z].*$)");
	private static final char LIST_DELIMITER = ',';
	private static final char KEY_VALUE_DELIMITER = ':';
	private final static int MAX_SIZE_OF_ITEMS_IN_SQL_IN_CLAUSE = 1000;
	private final static String MULTIVALUE_ATTRIBUTE_SEPARATOR_REGEX = ";";
	private final static String configurationsLocations = "/etc/perun/";
	public final static String largeStringClassName = "java.lang.LargeString";
	public final static String largeArrayListClassName = "java.util.LargeArrayList";

	private static CoreConfig coreConfig;

	private final static JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	private static boolean mailSenderInitialized = false;
	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Method create formatter with default settings for perun timestamps and set lenient on false
	 * Timestamp format:  "yyyy-MM-dd HH:mm:ss.S" - "ex. 2014-01-01 10:10:10.0"
	 *
	 * Lenient on false means that formatter will be more strict to creating timestamp from string
	 *
	 * IMPORTANT: SimpleDateFormat is not thread safe !!!
	 *
	 * @return date formatter
	 */
	public static DateFormat getDateFormatter() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		df.setLenient(false);
		return df;
	}

	/**
	 * Method create formatter with default settings for perun timestamps (only date without time)
	 * and set lenient on false.
	 *
	 * Timestamp format:  "yyyy-MM-dd" - "ex. 2014-01-01"
	 *
	 * Lenient on false means that formatter will be more strict to creating timestamp from string
	 *
	 * IMPORTANT: SimpleDateFormat is not thread safe !!!
	 *
	 * @return date formatter
	 */
	public static DateFormat getDateFormatterWithoutTime() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setLenient(false);
		return df;
	}

	/**
	 * This method take text and for every chars in "<>\" erase escaping
	 * also change '\0' to 'null' if it is escaped zero symbol.
	 *
	 * Escaping char is \.
	 * Expecting: Before using this method, text must be escaped by using method createEscaping.
	 *            So in text will never be string like "\\>", "\" or "\\\".
	 *
	 * For every \ in text it put \\ and for every < it put \< and for every > it put \>
	 *
	 * @param text text from which will be erase escaping
	 * @return nonescaped text
	 */
	public static String eraseEscaping(String text) {
		if(text == null || text.equals("\\0")) return null;
		//change \0 to null if zero is escaped
		text = text.replaceAll("((^|[^\\\\])(\\\\\\\\)*)(\\\\0)", "$1null");
		text = text.replace("\\>", ">");
		text = text.replace("\\<", "<");
		text = text.replace("\\\\", "\\");
		return text;
	}
	/**
	 * This method take text and for every chars in "<>\" create escaping
	 * Escaping char is \.
	 * For every \\ in text it put \ and for every \< it put < and for every \> it put >
	 *
	 * @param text text from which will be erase escaping
	 * @return escaped text
	 */
	public static String createEscaping(String text) {
		if(text == null) return "\\0";
		text = text.replace("\\", "\\\\");
		text = text.replace(">", "\\>");
		text = text.replace("<", "\\<");
		return text;
	}

	/**
	 * This method get text and all nonescaped characters < and > replace by apostrophe
	 *
	 * @param text
	 * @return text where nonescaped characters < and  > will be reaplace by apostrophe '
	 */
	public static String replacePointyBracketsByApostrophe(String text) {
		StringBuilder stringBuilder = new StringBuilder(text);
		for(int i=0; i<text.length(); i++) {
			if(text.charAt(i)=='<' || text.charAt(i)=='>') {
				if(!isEscaped(text, i-1)) {
					stringBuilder.setCharAt(i, '\'');
				}
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * This method get text and all escaped \0 replace for text null
	 *
	 * @param text
	 * @return text where \0 is replaced for null
	 */
	public static String replaceEscapedNullByStringNull(String text) {
		StringBuilder stringBuilder = new StringBuilder(text);
		for(int i=0; i<stringBuilder.length(); i++) {
			if(stringBuilder.charAt(i)=='0') {
				if(isEscaped(stringBuilder.toString(), i-1)) {
					stringBuilder.replace(i-1, i+1, "null");
					i=i+2;
				}
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * Return true, if char on position in text is escaped by '\' Return false,
	 * if not.
	 *
	 * @param text text in which will be searching
	 * @param position position in text <0-text.length>
	 * @return true if char is escaped, false if not
	 */
	public static boolean isEscaped(String text, int position) {
		boolean escaped = false;
		while (text.charAt(position) == '\\') {
			escaped = !escaped;
			position--;
			if (position < 0) {
				return escaped;
			}
		}
		return escaped;
	}

	/**
	 * Serialize map to string
	 *
	 * @param map
	 * @return string of escaped map
	 */
	static String serializeMapToString(Map<String, String> map) {
		if(map == null) return "\\0";
		Map<String, String> attrNew = new HashMap<String, String>(map);
		Set<String> keys = new HashSet<String>(attrNew.keySet());
		for(String s: keys) {
			attrNew.put("<" + BeansUtils.createEscaping(s) + ">", "<" + BeansUtils.createEscaping(attrNew.get(s)) + ">");
			attrNew.remove(s);
		}
		return attrNew.toString();
	}

	/**
	 * Converts attribute value to string (serialize object to string).
	 * This is a wrapper for passing value and type only for specific use.
	 * @see #attributeValueToString(Attribute)
	 *
	 * @param attributeValue value of the attribute
	 * @param type type of resulting attribute
	 * @return string representation of the value
	 *
	 * @throws InternalErrorException
	 */
	@SuppressWarnings("unchecked")
	public static String attributeValueToString(Object attributeValue, String type) {
		Attribute a = new Attribute();
		a.setType(type);
		a.setValue(attributeValue);
		return attributeValueToString(a);
	}


	/**
	 * Converts attribute value to string (for storing into DB)
	 *
	 * @param attribute value of the attribute
	 * @return string representation of the value
	 *
	 * @throws InternalErrorException
	 */
	@SuppressWarnings("unchecked")
	public static String attributeValueToString(Attribute attribute) {
		if(attribute == null) throw new InternalErrorException(new NullPointerException("attribute is null"));
		if(attribute.getValue() == null) return null;


		String attributeType = attribute.getType();
		// convert internal "large" types to generic java types
		if (Objects.equals(attributeType, BeansUtils.largeStringClassName)) attributeType = String.class.getName();
		if (Objects.equals(attributeType, BeansUtils.largeArrayListClassName)) attributeType = ArrayList.class.getName();

		if(!Objects.equals(attributeType, attribute.getValue().getClass().getName())) {
			throw new InternalErrorException("Attribute's type mismatch " + attribute + ". The type of attribute's value (" + attribute.getValue().getClass().getName() + ") doesn't match the type of attribute (" + attribute.getType() + ").");
		}

		if(Objects.equals(attributeType, String.class.getName())) {
			return (String) attribute.getValue();
		} else if(Objects.equals(attributeType, Integer.class.getName())) {
			return Integer.toString((Integer) attribute.getValue());
		} else if(Objects.equals(attributeType, Boolean.class.getName())) {
			return Boolean.toString((Boolean) attribute.getValue());
		} else if(Objects.equals(attributeType, ArrayList.class.getName())) {
			StringBuilder sb = new StringBuilder();
			for(String item : (List<String>) attribute.getValue()) {
				if(item == null) {
					item = "\\0";
				} else {
					item = item.replace("\\", "\\\\");   //escape char '\'
					item = item.replace(Character.toString(LIST_DELIMITER), "\\" + LIST_DELIMITER); //escape LIST_DELIMITER
				}
				sb.append(item);
				sb.append(LIST_DELIMITER);
			}
			return sb.toString();
		} else if(Objects.equals(attributeType, LinkedHashMap.class.getName())) {
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, String> entry : ((Map<String, String>) attribute.getValue()).entrySet()) {
				String key = entry.getKey();
				if(key == null) {
					key = "\\0";
				} else {
					key = key.replace("\\", "\\\\");   //escape char '\'
					key = key.replace(Character.toString(LIST_DELIMITER), "\\" + LIST_DELIMITER); //escape LIST_DELIMITER
					key = key.replace(Character.toString(KEY_VALUE_DELIMITER), "\\" + KEY_VALUE_DELIMITER); //escape KEY_VALUE_DELIMITER
				}

				String value = entry.getValue();
				if(value == null) {
					value = "\\0";
				} else {
					value = value.replace("\\", "\\\\");   //escape char '\'
					value = value.replace(Character.toString(LIST_DELIMITER), "\\" + LIST_DELIMITER); //escape LIST_DELIMITER
					value = value.replace(Character.toString(KEY_VALUE_DELIMITER), "\\" + KEY_VALUE_DELIMITER); //escape KEY_VALUE_DELIMITER
				}

				sb.append(key);
				sb.append(KEY_VALUE_DELIMITER);
				sb.append(value);
				sb.append(LIST_DELIMITER);
			}
			return sb.toString();
		} else throw new InternalErrorException("Unknown java type of attribute's value.");
	}

	/**
	 * This method get map created by example : {<key1>=<value1>, <key2>=<value2>}
	 * Keys and values are escaped for "\", "<" and ">"
	 * Example of escaping key="key\\s\>" is "key\s>"
	 * Return Map<String, String> attribute to value.
	 *
	 * @param text text from which will be parsed map
	 * @return map<string, string> attributes
	 */
	public static Map<String, String> deserializeStringToMap(String text) {
		if(text.equals("\\0")) return null;
		Map<String, String> map = new HashMap<String, String>();
		int startName = -1;
		int endName = -1;
		int startValue = -1;
		int endValue = -1;
		int pointyBrackets = 0;
		boolean notValue = true;

		for(int i = 0; i < text.length(); i++) {
			if(text.charAt(i) == '<' && notValue && startName == -1) {
				if (!BeansUtils.isEscaped(text, i - 1)) {
					startName = i;
				}
			}else if(text.charAt(i) == '>' && notValue && endName == -1) {
				if (!BeansUtils.isEscaped(text, i - 1)) {
					endName = i;
					notValue = false;
				}
			} else if(text.charAt(i) == '<' && !notValue && startValue == -1) {
				if (!BeansUtils.isEscaped(text, i - 1)) {
					startValue = i;
				}
			} else if(text.charAt(i) == '>' && !notValue && endValue == -1) {
				if (!BeansUtils.isEscaped(text, i - 1)) {
					endValue = i;
					notValue = true;
				}
			}
			if (startName != -1 && endName != -1 && startValue != -1 && endValue != -1) {
				map.put(BeansUtils.eraseEscaping(text.substring(startName + 1, endName)), BeansUtils.eraseEscaping(text.substring(startValue + 1, endValue)));
				startName = -1;
				endName = -1;
				startValue = -1;
				endValue = -1;
			}
		}
		return map;
	}

	/**
	 * From the given list, parses all values separated by the comma ',' char.
	 * The value has to end with the comma ',' char.
	 *
	 * All escaped commas '{backslash},' will not be used to split the value. If there is a
	 * backslash character:
	 *     * it must be either follow by the comma ',' meaning that this
	 *       comma is not used for split; or
	 *     * it must be paired with another backslash '{backslash}{backslash}'. In that case, this double
	 *       backslash in the result value would be replaced by a single back slash.
	 *
	 * Example:
	 *      input: 'value1,val{backslash}ue2,val{backslash},ue3,'
	 *      result: ['value1', 'val{backslash}ue2', val,ue3']
	 *
	 * @param value value to be parsed
	 * @return list of parsed values
	 */
	public static List<String> parseEscapedListValue(String value) {
		String[] array = value.split(Character.toString(LIST_DELIMITER), -1);
		List<String> listValue =  new ArrayList<String>();
		//join items which was splited on escaped LIST_DELIMITER
		for(int i = 0; i < array.length -1; i++) { //itarate to lenght -1  ... last array item is always empty
			String item = array[i];
			while(item.matches("^(.*[^\\\\])?(\\\\\\\\)*\\\\$")) { //item last char is '\' . Next item start with ',', so we need to concat this items.
				item = item.substring(0, item.length()-1);  //cut off last char ('\')
				try {
					item = item.concat(Character.toString(LIST_DELIMITER)).concat(array[i+1]);
					i++;
				} catch(ArrayIndexOutOfBoundsException ex) {
					throw new ConsistencyErrorException("Bad format in attribute value", ex);
				}
			}
			//unescape
			item = item.replaceAll("\\\\([\\\\" + Character.toString(LIST_DELIMITER) + "])", "$1");
			if(item.equals("\\0")) item = null;

			//return updated item back to list
			listValue.add(item);
		}
		return listValue;
	}

	/**
	 * Converts string representation of an attribute value to correct java object
	 *
	 * @param stringValue string representation of the attribute value
	 * @param type type of the value ("Java.lang.String" for example)/
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	public static Object stringToAttributeValue(String stringValue, String type) {
		if(stringValue == null || stringValue.isEmpty()) return null;

		Class<?> attributeClass;
		try {
			// convert internal "large" types to generic java types
			if (Objects.equals(type, BeansUtils.largeStringClassName)) {
				attributeClass = Class.forName(String.class.getName());
			} else if (Objects.equals(type, BeansUtils.largeArrayListClassName)) {
				attributeClass = Class.forName(ArrayList.class.getName());
			} else {
				// is already generic java type
				attributeClass = Class.forName(type);
			}
		} catch (ClassNotFoundException e) {
			throw new InternalErrorException("Unknown attribute type", e);
		} catch (NoClassDefFoundError e) {
			throw new InternalErrorException("Unknown attribute def type", e);
		}

		if(attributeClass.equals(String.class)) {
			return stringValue;
		} else if(attributeClass.equals(Integer.class)) {
			return Integer.parseInt(stringValue);
		} else if(attributeClass.equals(Boolean.class)) {
			return Boolean.parseBoolean(stringValue);
		} else if(attributeClass.equals(ArrayList.class)) {
			return parseEscapedListValue(stringValue);
		} else if(attributeClass.equals(LinkedHashMap.class)) {
			String[] array = stringValue.split(Character.toString(LIST_DELIMITER), -1);
			Map<String, String> attributeValue = new LinkedHashMap<String, String>();

			//join items which was splited on escaped LIST_DELIMITER
			for(int i = 0; i < array.length -1; i++) {  //itarate to lenght -1  ... last array item is always empty
				String mapEntry = array[i];

				while(mapEntry.matches("^(.*[^\\\\])?(\\\\\\\\)*\\\\$")) { //mapEntry last char is '\' . Next mapEntry start with ',', so we need to concat this mapEntries.
					mapEntry = mapEntry.substring(0, mapEntry.length()-1);  //cut off last char ('\')
					try {
						mapEntry = mapEntry.concat(Character.toString(LIST_DELIMITER)).concat(array[i+1]);
						i++;
					} catch(ArrayIndexOutOfBoundsException ex) {
						throw new ConsistencyErrorException("Bad format in attribute value", ex);
					}
				}

				boolean delimiterFound = false;
				int delimiterIndex = -1;


				while(!delimiterFound) {
					delimiterIndex++; //start searching at next char then last time
					delimiterIndex = mapEntry.indexOf(Character.toString(KEY_VALUE_DELIMITER), delimiterIndex);
					if(delimiterIndex == -1) throw new ConsistencyErrorException("Bad format in attribute value. KEY_VALUE_DELIMITER not found. Attribute value='" + stringValue + "', processed entry='" + mapEntry + "'");

					//check if this delimiter is not escaped
					boolean isEscaped = false;  //is delimiter escaped
					boolean stop = false;
					int processedIndex = delimiterIndex - 1;
					while(!stop && processedIndex >= 0) {
						if(mapEntry.charAt(processedIndex) == '\\') {
							isEscaped = !isEscaped;
						} else {
							stop = true;
						}
						processedIndex--;
					}
					if(!isEscaped) delimiterFound = true;
				}

				String key = mapEntry.substring(0, delimiterIndex);
				String value = mapEntry.substring(delimiterIndex+1);

				//unescape
				key = key.replaceAll("\\\\([\\\\" + Character.toString(LIST_DELIMITER) + Character.toString(KEY_VALUE_DELIMITER) + "])", "$1");
				value = value.replaceAll("\\\\([\\\\" + Character.toString(LIST_DELIMITER) + Character.toString(KEY_VALUE_DELIMITER) + "])", "$1");

				if(key.equals("\\0")) key = null;
				if(value.equals("\\0")) value = null;

				//return updated item back to list
				attributeValue.put(key, value);
			}

			return attributeValue;
		} else {
			throw new InternalErrorException("Unknown attribute type. ("+ attributeClass.toString() + ")");
		}
	}

	/**
	 * Converts string representation of an attribute value to the LinkedHashMap
	 *
	 * @param attributesAsString Map attribute in String representation.
	 * @return LinkedHashMap with key values pairs extracted from the input
	 *         or an empty map when the input parameter is an empty string or null.
	 */
	@SuppressWarnings("unchecked") // It is ok, we know that stringToAttributeValue always returns LinkedHashMap or null
	public static LinkedHashMap<String, String> stringToMapOfAttributes(String attributesAsString) {
		Object mapAsObject = BeansUtils.stringToAttributeValue(attributesAsString, LinkedHashMap.class.getName());
		if (mapAsObject == null) return new LinkedHashMap<>();
		return (LinkedHashMap<String, String>) mapAsObject;
	}

	/**
	 * Take perunBean name and if it is RichObject, convert it to simple name.
	 *
	 * RichObject mean: starts with "Rich" and continue with Upper Letter [A-Z]
	 *
	 * Ex.: RichGroup -> Group, RichUser -> User
	 * Ex.: RichardObject -> RichardObject
	 * Ex.: Null -> Null
	 *
	 * @param beanName bean Name of PerunBean (simple name of object)
	 *
	 * @return converted beanName (without Rich part)
	 */
	public static String convertRichBeanNameToBeanName(String beanName) {
		if(beanName == null || beanName.isEmpty()) return beanName;

		Matcher richBeanNameMatcher = richBeanNamePattern.matcher(beanName);
		if (richBeanNameMatcher.find()) {
			return richBeanNameMatcher.group(1);
		}

		return beanName;
	}

	/**
	 * Create a string with set of IN clause. Every in clause has maximum 1000 ids.
	 * Identifier means for what IN clause is calling (Like 'table.id')
	 *
	 * Reason for using is compatibility with oracle and other dbs.
	 *
	 * Example: " ( in (10,15,...) or in (...) or ... ) "
	 *
	 * @param beans list of perun beans
	 * @return string with some sql IN clause
	 */
	public static String prepareInSQLClause(String identifier, List<? extends PerunBean> beans) {
		//get Ids
		List<Integer> beansIds = new ArrayList<>();
		for(PerunBean pb: beans) {
			beansIds.add(pb.getId());
		}
		return BeansUtils.prepareInSQLClause(beansIds, identifier);
	}


	/**
	 * Create a string with set of IN clause. Every in clause has maximum 1000 ids.
	 * Identifier means for what IN clause is calling (Like 'table.id')
	 *
	 * Reason for using is compatibility with oracle and other dbs.
	 *
	 * Example: " ( in (10,15,...) or in (...) or ... ) "
	 *
	 * @param beansIds list of perun bean ids
	 * @return string with some sql IN clause
	 */
	public static String prepareInSQLClause(List<Integer> beansIds, String identifier) {
		StringBuilder sb = new StringBuilder();
		//use or in sql clause
		boolean useOr = false;
		//first bracket
		sb.append(" (");

		//for every maxSize of beans
		while(beansIds.size() > MAX_SIZE_OF_ITEMS_IN_SQL_IN_CLAUSE ) {

			if(useOr) sb.append(" or ");
			else useOr = true;

			sb.append(" ");
			sb.append(identifier);
			sb.append(" in (");
			List<Integer> partOfBeansIds = beansIds.subList(0, MAX_SIZE_OF_ITEMS_IN_SQL_IN_CLAUSE);
			sb.append(beanIdsToString(partOfBeansIds));
			sb.append(") ");
			partOfBeansIds.clear();
		}

		//for rest of beans less or equals to 1000
		if(useOr) sb.append(" or ");
		sb.append(" ");
		sb.append(identifier);
		sb.append(" in (");
		sb.append(beanIdsToString(beansIds));
		sb.append(") ");

		//last bracket
		sb.append(") ");
		return sb.toString();
	}

	/**
	 * Convert list of beans ids to one string with ',' between ids
	 *
	 * @param beansIds List of ids to construct string with
	 * @return string representation of list of ids
	 */
	private static String beanIdsToString(List<Integer> beansIds) {
		StringBuilder stringBuilder = new StringBuilder();
		for(Integer beanId : beansIds) {
			stringBuilder.append(",");
			stringBuilder.append(beanId);
			}
		stringBuilder.deleteCharAt(0);
		return stringBuilder.toString();
	}

	/**
	 * Gets particular property from custom property file.
	 *
	 * @param propertyFile name of properties file
	 * @param propertyName name of the property
	 * @return value of the property
	 */
	public static String getPropertyFromCustomConfiguration(String propertyFile, String propertyName) {
		log.trace("Entering getPropertyFromCustomConfiguration: propertyFile='" +  propertyFile + "' propertyName='" +  propertyName + "'");
		notNull(propertyName, "propertyName");
		notNull(propertyFile, "propertyFile");

		// Load properties file with configuration
		Properties properties = new Properties();
		try {
			// Get the path to the perun.properties file
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(BeansUtils.configurationsLocations + propertyFile));
			properties.load(is);
			is.close();

			String property = properties.getProperty(propertyName);
			if (property == null) {
				throw new InternalErrorException("Property " + propertyName + " cannot be found in the configuration file: "+propertyFile);
			}
			return property;
		} catch (FileNotFoundException e) {
			throw new InternalErrorException("Cannot find "+propertyFile+" file", e);
		} catch (IOException e) {
			throw new InternalErrorException("Cannot read "+propertyFile+" file", e);
		}
	}

	/**
	 * Gets all properties from custom property file.
	 *
	 * @param propertyFile name of properties file
	 * @return all properties with values
	 */
	public static Map<String, String> getAllPropertiesFromCustomConfiguration(String propertyFile) {
		log.trace("Entering getAllPropertiesFromCustomConfiguration: propertyFile='" + propertyFile + "'");
		notNull(propertyFile, "propertyFile");

		// Load properties file with configuration
		Properties properties = new Properties();
		try {

			// Get the path to the perun.properties file
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(BeansUtils.configurationsLocations + propertyFile));
			properties.load(is);
			is.close();

			Map<String, String> myMap = new HashMap<String, String>();
			for (Object key : properties.keySet()) {
				myMap.put(key.toString(), properties.get(key).toString());
			}
			return myMap;

		} catch (FileNotFoundException e) {
			throw new InternalErrorException("Cannot find "+propertyFile+" file", e);
		} catch (IOException e) {
			throw new InternalErrorException("Cannot read "+propertyFile+" file", e);
		}

	}


	/**
	 * True if this instance of perun is read only.
	 * False if not.
	 *
	 * @return true or false (readOnly or not)
	 */
	public static boolean isPerunReadOnly() {
		return coreConfig.isReadOnlyPerun();
	}

	/**
	 * True if DB initializator is enabled, false if not
	 * Default is false
	 *
	 * @return true if enabled, false if disabled
	 */
	public static boolean initializatorEnabled() {
		return coreConfig.isDbInitializatorEnabled();
	}

	/**
	 * Checks whether the object is null or not.
	 *
	 * @param e
	 * @param name
	 * @throws InternalErrorException which wraps NullPointerException
	 */
	public static void notNull(Object e, String name) {
		if(e == null){
			throw new InternalErrorException(new NullPointerException("'" + name + "' is null"));
		}
	}

	/**
	 * Set configuration that can be later queried by getCoreConfig().
	 *
	 */
	public static void setConfig(CoreConfig coreConfig) {
		BeansUtils.coreConfig = coreConfig;
	}

	public static CoreConfig getCoreConfig() {
		return coreConfig;
	}

	static String getIDsOfPerunBeans(List<? extends PerunBean> listOfBeans) {
		if (listOfBeans == null || listOfBeans.isEmpty()) {
			return "";
		}

		Boolean isFirstIteration = true;
		StringBuilder str = new StringBuilder();
		for(PerunBean perunBean : listOfBeans) {
			if(isFirstIteration) {
				str.append(perunBean.getId());
				isFirstIteration = false;
			}
			else {
				str.append(",").append(perunBean.getId());
			}
		}
		return str.toString();
	}

	public static Pair<Integer, Integer> getSinglePair(Set<Pair<Integer, Integer>> pairs) {
		if(pairs==null) return null;
		//noinspection LoopStatementThatDoesntLoop
		for (Pair<Integer, Integer> pair : pairs) {
			return pair;
		}
		return null;
	}

	public static Integer getSingleId(Set<Pair<Integer, Integer>> pairs) {
		if(pairs==null) return null;
		//noinspection LoopStatementThatDoesntLoop
		for (Pair<Integer, Integer> pair : pairs) {
			return pair.getLeft();
		}
		return null;
	}


	/**
	 * Returns abbreviation in format [Entity]:[V/D/C]:[friendlyName]
	 * [Entity] is something like 'U' for user, 'G-R' for group-resource etc.
	 *
	 * @param ad attribute definition
	 * @return abbreviation in format [Entity]:[V/D/C]:[friendlyName]
	 */
	public static String getAttributeDefinitionAbbreviation(AttributeDefinition ad) {
		String entity = parseEntityAbbreviation(ad);
		String type;
		if (ad.getNamespace().endsWith("virt")) {
			type = "V";
		} else if (ad.getNamespace().endsWith("def")) {
			type = "D";
		} else {
			type = "C";
		}

		String formattedFriendlyName;

		String[] splitFriendlyName = ad.getFriendlyName().split(":");
		formattedFriendlyName = splitFriendlyName[0];
		if (splitFriendlyName.length > 1) {
			formattedFriendlyName += ":*";
		}

		return entity + ":" + type + ":" + formattedFriendlyName;
	}

	public static String parseEntityAbbreviation(AttributeDefinition ad) {
		String entity = "";
		String[] split = ad.getEntity().split("_");

		if (split.length == 1) {
			entity = firstLetterCap(split[0]);
		} else if (split.length == 2) {
			entity = firstLetterCap(split[0]) + "-" + firstLetterCap(split[1]);
		}

		return entity;
	}

	private static String firstLetterCap(String s) {
		if (!s.isEmpty()) {
			s = s.substring(0, 1).toUpperCase();
		}

		return s;
	}

	/**
	 * Return instance of JavaMailSender with shared Perun configuration
	 * used to send mail notifications by.
	 *
	 * @return single instance of JavaMailSender
	 */
	public static JavaMailSender getDefaultMailSender() {

		if (mailSenderInitialized) {

			return mailSender;

		} else {

			// reconstruct props to prevent any future property name collision with perun.properties
			Properties mailProps = new Properties();
			mailProps.setProperty("mail.smtp.host", BeansUtils.getCoreConfig().getSmtpHost());
			mailProps.setProperty("mail.smtp.port", String.valueOf(BeansUtils.getCoreConfig().getSmtpPort()));
			mailProps.setProperty("mail.smtp.auth", String.valueOf(BeansUtils.getCoreConfig().isSmtpAuth()));
			mailProps.setProperty("mail.smtp.starttls.enable", String.valueOf(BeansUtils.getCoreConfig().isSmtpStartTls()));
			mailProps.setProperty("mail.debug", String.valueOf(BeansUtils.getCoreConfig().isMailDebug()));

			mailSender.setJavaMailProperties(mailProps);

			if (BeansUtils.getCoreConfig().isSmtpAuth()) {
				mailSender.setUsername(BeansUtils.getCoreConfig().getSmtpUser());
				mailSender.setPassword(BeansUtils.getCoreConfig().getSmtpPass());
			}

			mailSenderInitialized = true;

			return mailSender;

		}

	}

	/**
	 * Convert object richMember to object candidate.
	 * PrimaryUserExtSource is used as the main userExtSource for candidate object
	 *
	 * @param richMember
	 * @param primaryUserExtSource main userExtSource for candidate object
	 *
	 * @return converted object candidate from richMember
	 */
	public static Candidate convertRichMemberToCandidate(RichMember richMember, UserExtSource primaryUserExtSource) {
		if(richMember == null) throw new InternalErrorException("RichMember can't be null when converting to Candidate.");
		if(primaryUserExtSource == null) throw new InternalErrorException("PrimaryUserExtSource can't be null when converting to Candidate.");

		//Prepare additional userExtSources
		List<UserExtSource> additionalUserExtSources = new ArrayList<>();
		if(richMember.getUserExtSources() != null) additionalUserExtSources.addAll(richMember.getUserExtSources());
		//remove primaryUserExtSource from additional userExtSources
		additionalUserExtSources.remove(primaryUserExtSource);

		//Prepare attributes
		List<Attribute> allAttributes = new ArrayList<>();
		if(richMember.getMemberAttributes() != null) allAttributes.addAll(richMember.getMemberAttributes());
		if(richMember.getUserAttributes() != null) allAttributes.addAll(richMember.getUserAttributes());
		Map<String, String> candidateAttributes = new HashMap<>();
		for(Attribute attribute: allAttributes) {
			candidateAttributes.put(attribute.getName(), attributeValueToString(attribute));
		}

		Candidate candidate = new Candidate(richMember.getUser(), primaryUserExtSource);
		candidate.setAdditionalUserExtSources(additionalUserExtSources);
		candidate.setAttributes(candidateAttributes);

		return candidate;
	}

	/**
	 * creates an intersection of two additionalIdentifiers arrays given as string.
	 *
	 * @param firstAdditionalIdentifiers first parameter which contains additional identifiers
	 * @param secondAdditionalIdentifiers second parameter which contains additional identifiers
	 * @return Intersection of the given String parameters
	 */
	public static List<String> additionalIdentifiersIntersection(String firstAdditionalIdentifiers, String secondAdditionalIdentifiers) {
		String[] firstIdentifiersArray = {};
		if (firstAdditionalIdentifiers != null) {
			firstIdentifiersArray = firstAdditionalIdentifiers.split(MULTIVALUE_ATTRIBUTE_SEPARATOR_REGEX);
		}
		String[] secondIdentifiersArray = {};
		if (secondAdditionalIdentifiers != null) {
			secondIdentifiersArray = secondAdditionalIdentifiers.split(MULTIVALUE_ATTRIBUTE_SEPARATOR_REGEX);
		}
		HashSet<String> firstIdentifiersSet = new HashSet<>(Arrays.asList(firstIdentifiersArray));
		firstIdentifiersSet.retainAll(Arrays.asList(secondIdentifiersArray));
		return new ArrayList<>(firstIdentifiersSet);
	}
}
