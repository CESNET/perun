package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Beans Utilities.
 *
 */
public class BeansUtils {

	private final static Logger log = LoggerFactory.getLogger(BeansUtils.class);

	private final static Pattern patternForCommonNameParsing = Pattern.compile("(([\\w]*. )*)([\\p{L}-']+) ([\\p{L}-']+)[, ]*(.*)");
	private final static Pattern richBeanNamePattern = Pattern.compile("^Rich([A-Z].*$)");
	public static final char LIST_DELIMITER = ',';
	public static final char KEY_VALUE_DELIMITER = ':';
	private final static int MAX_SIZE_OF_ITEMS_IN_SQL_IN_CLAUSE = 1000;
	public final static String configurationsLocations = "/etc/perun/";
	private static Properties properties;
	private static Boolean isPerunReadOnly = null;

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
	public static String serializeMapToString(Map<String, String> map) {
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
	 * Converts attribute value to string (for storing into DB)
	 *
	 * @param attribute value of the attribute
	 * @return string representation of the value
	 *
	 * @throws InternalErrorException
	 */
	public static String attributeValueToString(Attribute attribute) throws InternalErrorException {
		if(attribute == null) throw new InternalErrorException(new NullPointerException("attribute is null"));
		if(attribute.getValue() == null) return null;

		if(!attribute.getType().equals(attribute.getValue().getClass().getName())) {
			throw new InternalErrorException("Attribute's type mismatch " + attribute + ". The type of attribute's value (" + attribute.getValue().getClass().getName() + ") doesn't match the type of attribute (" + attribute.getType() + ").");
		}

		if(attribute.getType().equals(String.class.getName())) {
			return (String) attribute.getValue();
		} else if(attribute.getType().equals(Integer.class.getName())) {
			return Integer.toString((Integer) attribute.getValue());
		} else if(attribute.getType().equals(Boolean.class.getName())) {
			return Boolean.toString((Boolean) attribute.getValue());
		} else if(attribute.getType().equals(ArrayList.class.getName())) {
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
		} else if(attribute.getType().equals(LinkedHashMap.class.getName())) {
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
	 * Converts string representation of an attribute value to correct java object
	 *
	 * @param stringValue string representation of the attribute value
	 * @param type type of the value ("Java.lang.String" for example)/
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	public static Object stringToAttributeValue(String stringValue, String type) throws InternalErrorException {
		if(stringValue == null || stringValue.isEmpty()) return null;

		Class<?> attributeClass;
		try {
			attributeClass = Class.forName(type);
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
			String[] array = stringValue.split(Character.toString(LIST_DELIMITER), -1);
			List<String> attributeValue =  new ArrayList<String>();

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
				attributeValue.add(item);
			}

			return attributeValue;
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
	 * @param beans List of ids to construct string with
	 * @return string representation of list of ids
	 */
	public static String beanIdsToString(List<Integer> beansIds) {
		StringBuilder stringBuilder = new StringBuilder();
		for(Integer beanId : beansIds) {
			stringBuilder.append(",");
			stringBuilder.append(beanId);
			}
		stringBuilder.deleteCharAt(0);
		return stringBuilder.toString();
	}

	/**
	 * Gets particular property from perun.properties file.
	 *
	 * @param propertyName name of the property
	 * @return value of the property
	 */
	public static String getPropertyFromConfiguration(String propertyName) throws InternalErrorException {
		log.trace("Entering getPropertyFromConfiguration: propertyName='" + propertyName + "'");
		notNull(propertyName, "propertyName");

		if(BeansUtils.properties == null) {
			// Load properties file with configuration
			Properties properties = new Properties();
			try {
				// Get the path to the perun.properties file
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(BeansUtils.configurationsLocations + "perun.properties"));
				properties.load(is);
				is.close();
			} catch (FileNotFoundException e) {
				throw new InternalErrorException("Cannot find perun.properties file", e);
			} catch (IOException e) {
				throw new InternalErrorException("Cannot read perun.properties file", e);
			}

			BeansUtils.properties = properties;
		}
		String property = BeansUtils.properties.getProperty(propertyName);
		if (property == null) {
			throw new InternalErrorException("Property " + propertyName + " cannot be found in the configuration file");
		}
		return property;
	}

	/**
	 * Gets particular property from custom property file.
	 *
	 * @param propertyFile name of properties file
	 * @param propertyName name of the property
	 * @return value of the property
	 */
	public static String getPropertyFromCustomConfiguration(String propertyFile, String propertyName) throws InternalErrorException {
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
	public static Map<String, String> getAllPropertiesFromCustomConfiguration(String propertyFile) throws InternalErrorException {
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
		//Set only if variable isPerunReadOnly is not set already
		if(isPerunReadOnly == null) {
			String readOnly;
			try {
				readOnly = BeansUtils.getPropertyFromConfiguration("perun.readOnlyPerun");
			} catch (Exception ex) {
				//If something wierd happens, set this to normal configuration (not readOnly) and log this exception
				log.error("Problem occures when trying to get readOnly configuration from perun properties file.", ex);
				log.debug("Read only configuration not found, set to false.");
				isPerunReadOnly = false;
				return isPerunReadOnly;
			}

			if(readOnly == null) {
				log.debug("Read only configuration is null, set to false.");
				isPerunReadOnly = false;
				return isPerunReadOnly;
			}

			if(readOnly.contains("true")) {
				log.debug("Read only configuration found='{}', set to true.", readOnly);
				isPerunReadOnly = true;
				return isPerunReadOnly;
			}

			log.debug("Read only configuration found='{}', set to false.", readOnly);
			isPerunReadOnly = false;
			return isPerunReadOnly;
		} else {
			return isPerunReadOnly;
		}
	}

	/**
	 * True if DB initializator is enabled, false if not
	 * Default is false
	 *
	 * @return true if enabled, false if disabled
	 */
	public static boolean initializatorEnabled() {
		String initializatorEnabled;
		try {
			initializatorEnabled = BeansUtils.getPropertyFromConfiguration("perun.DBInitializatorEnabled");
		} catch (Exception ex) {
			//If something wierd happens, set this to normal configuration (not readOnly) and log this exception
			log.error("Problem occures when trying to get DBInitializatorEnabled configuration from perun properties file.", ex);
			log.debug("DBInitializatorEnabled configuration not found, set to false.");
			return false;
		}

		if(initializatorEnabled == null) {
			log.debug("DBInitializatorEnabled configuration is null, set to false.");
			return false;
		}

		if(initializatorEnabled.contains("true")) {
			log.debug("DBInitializatorEnabled configuration found='{}', set to true.", initializatorEnabled);
			return true;
		}

		log.debug("Read only configuration found='{}', set to false.", initializatorEnabled);
		return false;
	}
	
	/**
	 * Checks whether the object is null or not.
	 *
	 * @param e
	 * @param name
	 * @throws InternalErrorException which wraps NullPointerException
	 */
	public static void notNull(Object e, String name) throws InternalErrorException {
		if(e == null){
			throw new InternalErrorException(new NullPointerException("'" + name + "' is null"));
		}
	}

	/**
	 * Set already filled-in properties (used by Spring container to inject properties bean)
	 *
	 * @param properties
	 * @return
	 */
	public static Properties setProperties(Properties properties) {
		BeansUtils.properties = properties;
		return BeansUtils.properties;
	}

	public static String getIDsOfPerunBeans(List<? extends PerunBean> listOfBeans) {
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
}
