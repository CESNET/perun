package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.utils.graphs.Graph;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.io.*;
import java.lang.IllegalArgumentException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities.
 */
public class Utils {

	private final static Logger log = LoggerFactory.getLogger(Utils.class);
	public final static String configurationsLocations = "/etc/perun/";
	public static final String TITLE_BEFORE = "titleBefore";
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String TITLE_AFTER = "titleAfter";
	private static Properties properties;
	public static final Pattern emailPattern = Pattern.compile("^[-_A-Za-z0-9+']+(\\.[-_A-Za-z0-9+']+)*@[-A-Za-z0-9]+(\\.[-A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

	private static final Pattern titleBeforePattern = Pattern.compile("^(([\\p{L}]+[.])|(et))$");
	private static final Pattern firstNamePattern = Pattern.compile("^[\\p{L}-']+$");
	private static final Pattern lastNamePattern = Pattern.compile("^(([\\p{L}-']+)|([\\p{L}][.]))$");

	private static final String userPhoneAttribute = "urn:perun:user:attribute-def:def:phone";
	private static final String memberPhoneAttribute = "urn:perun:member:attribute-def:def:phone";

	/**
	 * Replaces dangerous characters.
	 * Replaces : with - and spaces with _.
	 *
	 * @param str string to be normalized
	 * @return normalized string
	 */
	public static String normalizeString(String str) {
		log.trace("Entering normalizeString: str='{}'", str);
		return str.replace(':', '-').replace(' ', '_');
	}

	public static <T> boolean hasDuplicate(List<T> all) {
		Set<T> set = new HashSet<T>(all.size());
		// Set#add returns false if the set does not change, which
		// indicates that a duplicate element has been added.
		for (T each: all) if (!set.add(each)) return true;
		return false;
	}

	/**
	 * Joins Strings or any objects into a String. Use as
	 * <pre>
	 *  List<?> list = Arrays.asList("a", 1, 2.0);
	 *  String s = join(list,",");
	 * </pre>
	 * @param collection anything Iterable, like a {@link java.util.List} or {@link java.util.Collection}
	 * @param separator any separator, like a comma
	 * @return string with string representations of objects joined by separators
	 */
	public static String join(Iterable<?> collection, String separator) {
		Iterator<?> oIter;
		if (collection == null || (!(oIter = collection.iterator()).hasNext()))
			return "";
		StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
		while (oIter.hasNext())
			oBuilder.append(separator).append(oIter.next());
		return oBuilder.toString();
	}

	/**
	 * Joins Strings or any objects into a String. Use as
	 * <pre>
	 *  String[] sa = { "a", "b", "c"};
	 *  String s = join(list,",");
	 * </pre>
	 * @param objs array of objects
	 * @param separator any separator, like a comma
	 * @return string with string representations of objects joined by separators
	 */
	public static String join(Object[] objs, String separator) {
		log.trace("Entering join: objs='{}', separator='{}'", objs, separator);
		return join(Arrays.asList(objs),separator);
	}

	/**
	 * Integer row mapper
	 */
	public static final RowMapper<Integer> ID_MAPPER = new RowMapper<Integer>() {
		public Integer mapRow(ResultSet rs, int i) throws SQLException {
			return rs.getInt("id");
		}
	};

	/**
	 * String row mapper
	 */
	public static final RowMapper<String> STRING_MAPPER = new RowMapper<String>() {
		public String mapRow(ResultSet rs, int i) throws SQLException {
			return rs.getString("value");
		}
	};

	// FIXME prijde odstranit
	public static void checkPerunSession(PerunSession sess) throws InternalErrorException {
		notNull(sess, "sess");
	}

	/**
	 * Creates copy of given Map with Sets as values. The returned object contains a new Map
	 * and new Sets, the {@link T} objects remain the same.
	 *
	 * @param original original Map
	 * @param <T> parameter
	 * @return new Map with new Sets as values
	 */
	public static <T> Map<T, Set<T>> createDeepCopyOfMapWithSets(Map<T, Set<T>> original) {
		Map<T, Set<T>> copy = new HashMap<>();
		for (T key : original.keySet()) {
			Set<T> setCopy = original.get(key) == null ? null : new HashSet<>(original.get(key));
			copy.put(key, setCopy);
		}
		return copy;
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
	 * Throws a MinSizeExceededException if the given value does not specified minLength.
	 * If the value is null then MinSizeExceededException is thrown as well.
	 *
	 * @param propertyName name of checked property
	 * @param minLength minimal length
	 * @throws MinSizeExceededException when length of actualValue is lower than minLength or null
	 */
	public static void checkMinLength(String propertyName, String actualValue, int minLength) throws MinSizeExceededException {
		if (actualValue == null) {
			throw new MinSizeExceededException("The property '" + propertyName + "' does not have a minimal length equal to '" + minLength + "' because it is null.");
		}
		if (actualValue.length() < minLength) {
			throw new MinSizeExceededException("Length of '" + propertyName + "' is too short! MinLength=" + minLength + ", ActualLength=" + actualValue.length());
		}
	}

	/**
	 * Throws a MaxSizeExceededException if the given value is longer than maxLength.
	 * If the value is null then nothing happens.
	 *
	 * @param propertyName name of checked property
	 * @param maxLength max length
	 * @throws MaxSizeExceededException when length of actualValue is higher than maxLength
	 */
	public static void checkMaxLength(String propertyName, String actualValue, int maxLength) throws MaxSizeExceededException {
		if (actualValue == null) {
			return;
		}
		if (actualValue.length() > maxLength) {
			throw new MaxSizeExceededException("Length of '" + propertyName + "' is too long! MaxLength=" + maxLength + ", ActualLength=" + actualValue.length());
		}
	}

	/**
	 * Define, if some entity contain a diacritic symbol.
	 *
	 * @param name name of entity
	 * @throws DiacriticNotAllowedException
	 */
	public static void checkWithoutDiacritic(String name) throws DiacriticNotAllowedException{

		if(!Normalizer.isNormalized(name, Form.NFKD))throw new DiacriticNotAllowedException("Name of the entity is not in the normalized form NFKD (diacritic not allowed)!");;

	}

	/**
	 * Define, if some entity contain a special symbol
	 * Special symbol is everything except - numbers, letters and space
	 *
	 * @param name name of entity
	 * @throws SpecialCharsNotAllowedException
	 */
	public static void checkWithoutSpecialChars(String name) throws SpecialCharsNotAllowedException{

		if(!name.matches("^[0-9 \\p{L}]*$")) throw new SpecialCharsNotAllowedException("The special chars in the name of entity are not allowed!");
	}

	/**
	 * Define, if some entity contain a special symbol
	 * Special symbol is everything except - numbers, letters and space (and allowedSpecialChars)
	 * The allowedSpecialChars are on the end of regular expresion, so the same rule must be observed.
	 * (example, symbol - must be on the end of string) rules are the same like in regular expresion
	 *
	 * @param name name of entity
	 * @param allowedSpecialChars this String must contain only special chars which are allowed
	 * @throws SpecialCharsNotAllowedException
	 */
	public static void checkWithoutSpecialChars(String name, String allowedSpecialChars) throws SpecialCharsNotAllowedException{

		if(!name.matches("^([0-9 \\p{L}" + allowedSpecialChars + "])*$")) throw new SpecialCharsNotAllowedException("The special chars (except " + allowedSpecialChars + ") in the name of entity are not allowed!");
	}

	/**
	 * Define, if some entity contain a number
	 *
	 * @param name
	 * @throws NumbersNotAllowedException
	 */
	public static void checkWithoutNumbers(String name) throws NumbersNotAllowedException{

		if(!name.matches("^([^0-9])*$")) throw new NumbersNotAllowedException("The numbers in the name of entity are not allowed!");
	}

	/**
	 * Define, if some entity contain a space
	 *
	 * @param name
	 * @throws SpaceNotAllowedException
	 */
	public static void checkWithoutSpaces(String name)throws SpaceNotAllowedException{

		if(name.contains(" ")) throw new SpaceNotAllowedException("The spaces in the name of entity are not allowed!");
	}

	/**
	 * Define, if some number is in range.
	 * Example: number 4 is in range 4 - 12, number 3 is not
	 *
	 * @param number
	 * @param lowestValue
	 * @param highestValue
	 * @throws NumberNotInRangeException
	 */
	public static void checkRangeOfNumbers(int number, int lowestValue, int highestValue) throws NumberNotInRangeException {

		if(number<lowestValue || number>highestValue) throw new NumberNotInRangeException("Number is not in range, Lowest="+lowestValue+" < Number="+number+" < Highest="+highestValue);
	}

	/**
	 * Gets the next number from the sequence. This function hides differences in the databases engines.
	 *
	 * @param jdbc
	 * @param sequenceName
	 * @return new ID
	 * @throws InternalErrorException
	 */
	public static int getNewId(JdbcTemplate jdbc, String sequenceName) throws InternalErrorException {
		String dbType;
		String url = "";
		String query;
		// try to deduce database type from jdbc connection metadata
		try {
			DataSource ds = jdbc.getDataSource();
			if (ds instanceof BasicDataSource) {
				url = ((BasicDataSource) ds).getUrl();
			}
		} catch (Exception e) {
			log.error("cannot get JDBC url", e);
		}

		if (url.contains("hsqldb")) {
			dbType = "hsqldb";
		} else if (url.contains("oracle")) {
			dbType = "oracle";
		} else if (url.contains("postgresql")) {
			dbType = "postgresql";
		} else {
			dbType = BeansUtils.getCoreConfig().getDbType();
		}

		switch (dbType) {
			case "oracle":
				query = "select " + sequenceName + ".nextval from dual";
				break;
			case "postgresql":
				query = "select nextval('" + sequenceName + "')";
				break;
			case "hsqldb":
				query = "call next value for " + sequenceName + ";";
				break;
			default:
				throw new InternalErrorException("Unsupported DB type");
		}

		// Decide which type of the JdbcTemplate is provided
		try {
			return jdbc.queryForObject(query, Integer.class);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Returns current time in millis. Result of this call can then be used by function getRunningTime().
	 *
	 * @return current time in millis.
	 */
	public static long startTimer() {
		return System.currentTimeMillis();
	}

	/**
	 * Returns difference between startTime and current time in millis.
	 *
	 * @param startTime
	 * @return difference between current time in millis and startTime.
	 */
	public static long getRunningTime(long startTime) {
		return System.currentTimeMillis()-startTime;
	}


	/**
	 * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	private static String limit(String s,int limit) {
		if(s==null) return null;
		return s.length() > limit ? s.substring(0, limit) : s;
	}

	/**
	 * Creates a new instance of User with names initialized from parsed rawName.
	 * Imposes limit on leghts of fields.
	 * @see #parseCommonName(String)
	 * @param rawName raw name
	 * @return user
	 */
	public static User parseUserFromCommonName(String rawName) {
		Map<String, String> m = parseCommonName(rawName);
		User user = new User();
		user.setTitleBefore(limit(m.get(TITLE_BEFORE),40));
		user.setFirstName(limit(m.get(FIRST_NAME),64));
		user.setLastName(limit(m.get(LAST_NAME),64));
		user.setTitleAfter(limit(m.get(TITLE_AFTER),40));
		return user;
	}

	/**
	 * Try to parse rawName to keys: "titleBefore" "firstName" "lastName" "titleAfter"
	 *
	 * If rawName is null or empty, return map with empty values of all keys.
	 *
	 * Parsing procedure:
	 * 1] prepare array of parts by replacing all characters "," and "_" by spaces
	 * 2] change all sequence of inivisible characters (space, tabulator etc.) to one space
	 * 3] one by one try to parsing parts from array
	 *  - A] try to find all titleBefore parts
	 *  - B] try to find one firstName part
	 *  - C] try to find all lastName parts
	 *  - D] if the rest is not lastName so save it to the title after
	 *
	 * Example of parsing rawName:
	 * 1] rawName = "Mgr. et Mgr.    Petr_Jiri R. Sojka, Ph.D., CSc."
	 * 2] convert all ',' and '_' to spaces: rawName = "Mgr. et Mgr.    Petr Jiri R. Sojka  Ph.D.  CSc."
	 * 3] convert more than 1 invisible char to 1 space: rawName = "Mgr. et Mgr. Petr Jiri R. Sojka Ph.D. CSc."
	 * 4] parse string to array of parts by space: ArrayOfParts= ["Mgr.","et","Mgr.","Petr","Jiri","R.","Sojka","Ph.D.","CSc."]
	 * 5] first fill everything what can be in title before: titleBefore="Mgr. et Mgr."
	 * 6] then fill everything what can be in first name (maximum 1 part): firstName="Petr"
	 * 7] then fill everything what can be in last name: lastName="Jiri R. Sojka"
	 * 8] everything else put to the title after: titleAfter="Ph.D. CSc."
	 * 9] put these variables to map like key=value, for ex.: Map["titleBefore"="Mgr. et Mgr.",firstName="Petr", ... ] and return this map
	 *
	 * @param rawName
	 * @return map string to string where are 4 keys (titleBefore,titleAfter,firstName and lastName) with their values (value can be null)
	 */
	public static Map<String, String> parseCommonName(String rawName) {
		//prepare variables and map
		Map<String, String> parsedName = new HashMap<String, String>();
		String titleBefore = "";
		String firstName = "";
		String lastName = "";
		String titleAfter = "";

		//if raw name is null or empty, skip this part and only return map with null values for keys
		if(rawName!=null && !rawName.isEmpty()) {
			// all characters ',' replace by ' ' for rawName
			rawName = rawName.replaceAll(",", " ").trim();
			// all characters '_' replace by ' ' for rawName
			rawName = rawName.replaceAll("_", " ").trim();
			// replace all inivisible chars in row for
			rawName = rawName.replaceAll("\\s+", " ").trim();

			//split parts by space
			String[] nameParts = rawName.split(" ");

			//if length of nameParts is 1, save it to the lastName
			if(nameParts.length == 1) {
				lastName = nameParts[0];
			//if length of nameParts is more than 1, try to choose which part belong to which value
			} else {
				//variables for states
				boolean titleBeforeDone = false;
				boolean firstNameDone = false;
				boolean lastNameDone = false;

				//for every part try to get which one it is
				for(int i=0;i<nameParts.length;i++) {
					String part = nameParts[i];
					//trim this value (remove spaces before and after string)
					part = part.trim();

					//if titleBeforeDone is false, this string can be title before
					if(!titleBeforeDone) {
						Matcher titleBeforeMatcher = titleBeforePattern.matcher(part);
						//if title before matches
						if(titleBeforeMatcher.matches()) {
							//add space if this title is not first title before
							if(titleBefore.isEmpty()) titleBefore+= part;
							else titleBefore+= " " + part;
							//go on next part
							continue;
						} else {
							//this is not title before, so end part of title before and go next
							titleBeforeDone = true;
						}
					}

					//if firstNameDone is false, this string can be first name
					if(!firstNameDone) {
						Matcher firstNameMatcher = firstNamePattern.matcher(part);
						//if first name matches
						if(firstNameMatcher.matches()) {
							//first name can be only one
							firstName = part;
							//go on next part
							firstNameDone = true;
							continue;
						}
						//if this is not firstName skip firstName because only first word after titleBefore can be firstName
						firstNameDone = true;
					}

					//if lastNameDone is false, this string can be lastName
					if(!lastNameDone) {
						Matcher lastNameMatcher = lastNamePattern.matcher(part);
						//if last name matches
						if(lastNameMatcher.matches()) {
							//add space if this name is not first last name
							if(lastName.isEmpty()) lastName+= part;
							else lastName+= " " + part;
							//go on next part
							continue;
						//if last name not matches
						} else {
							//because last name can't be empty, save this part to lastName even if not matches
							if(lastName.isEmpty()) {
								lastName = part;
								lastNameDone = true;
								//go on next part
								continue;
							} else {
								//if there is already something in lastName, go on title after
								lastNameDone = true;
							}
						}
					}

					//rest of parts if lastName exists go to the title after
					if(lastNameDone) {
						//add space if this is not first title after
						if(titleAfter.isEmpty()) titleAfter+= part;
						else titleAfter+= " " + part;
					}
				}
			}
		}

		//empty string means null, add variables to map
		if (titleBefore.isEmpty()) titleBefore = null;
		parsedName.put(TITLE_BEFORE, titleBefore);
		if (firstName.isEmpty()) firstName = null;
		parsedName.put(FIRST_NAME, firstName);
		if (lastName.isEmpty()) lastName = null;
		parsedName.put(LAST_NAME, lastName);
		if (titleAfter.isEmpty()) titleAfter = null;
		parsedName.put(TITLE_AFTER, titleAfter);

		return parsedName;
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
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

	public static Attribute copyAttributeToViAttributeWithoutValue(Attribute copyFrom, Attribute copyTo) throws InternalErrorException {
		copyTo.setValueCreatedAt(copyFrom.getValueCreatedAt());
		copyTo.setValueCreatedBy(copyFrom.getValueCreatedBy());
		copyTo.setValueModifiedAt(copyFrom.getValueModifiedAt());
		copyTo.setValueModifiedBy(copyFrom.getValueModifiedBy());
		return copyTo;
	}

	public static Attribute copyAttributeToVirtualAttributeWithValue(Attribute copyFrom, Attribute copyTo) throws InternalErrorException {
		copyTo.setValue(copyFrom.getValue());
		copyTo.setValueCreatedAt(copyFrom.getValueCreatedAt());
		copyTo.setValueCreatedBy(copyFrom.getValueCreatedBy());
		copyTo.setValueModifiedAt(copyFrom.getValueModifiedAt());
		copyTo.setValueModifiedBy(copyFrom.getValueModifiedBy());
		return copyTo;
	}

	/**
	 * Method generates strings by pattern.
	 * The pattern is string with square brackets, e.g. "a[1-3]b". Then the content of the brackets
	 * is distributed, so the list is [a1b, a2b, a3c].
	 * Multibrackets are aslo allowed. For example "a[00-01]b[90-91]c" generates [a00b90c, a00b91c, a01b90c, a01b91c].
	 *
	 * @param pattern
	 * @return list of all generated strings
	 */
	public static List<String> generateStringsByPattern(String pattern) throws WrongPatternException {
		List<String> result = new ArrayList<String>();

		// get chars between the brackets
		List<String> values = new ArrayList<String>(Arrays.asList(pattern.split("\\[[^\\]]*\\]")));
		// get content of the brackets
		List<String> generators = new ArrayList<String>();
		Pattern generatorPattern = Pattern.compile("\\[([^\\]]*)\\]");
		Matcher m = generatorPattern.matcher(pattern);
		while (m.find()) {
			generators.add(m.group(1));
		}

		// if values strings contain square brackets, wrong syntax, abort
		for (String value: values) {
			if (value.contains("]") || (value.contains("["))) {
				throw new WrongPatternException("The pattern \"" + pattern + "\" has a wrong syntax. Too much closing brackets.");
			}
		}

		// if generators strings contain square brackets, wrong syntax, abort
		for (String generator: generators) {
			if (generator.contains("]") || (generator.contains("["))) {
				throw new WrongPatternException("The pattern \"" + pattern + "\" has a wrong syntax. Too much opening brackets.");
			}
		}

		// list, that contains list for each generator, with already generated numbers
		List<List<String>> listOfGenerated = new ArrayList<List<String>>();

		Pattern rangePattern = Pattern.compile("^(\\d+)-(\\d+)$");
		for (String range: generators) {
			m = rangePattern.matcher(range);
			if (m.find()) {
				String start = m.group(1);
				String end = m.group(2);
				int startNumber;
				int endNumber;
				try {
					startNumber = Integer.parseInt(start);
					endNumber = Integer.parseInt(end);
				} catch (NumberFormatException ex) {
					throw new WrongPatternException("The pattern \"" + pattern + "\" has a wrong syntax. Wrong format of the range.");
				}

				// if end is before start -> abort
				if (startNumber > endNumber) {
					throw new WrongPatternException("The pattern \"" + pattern + "\" has a wrong syntax. Start number has to be lower than end number.");

				}

				// find out, how many zeros are before start number
				int zerosInStart = 0;
				int counter = 0;
				while ( (start.charAt(counter) == '0') && (counter < start.length()-1) ) {
					zerosInStart++;
					counter++;
				}

				String zeros = start.substring(0, zerosInStart);
				int oldNumberOfDigits = String.valueOf(startNumber).length();

				// list of already generated numbers
				List<String> generated = new ArrayList<String>();
				while (endNumber >= startNumber) {
					// keep right number of zeros before number
					if (String.valueOf(startNumber).length() == oldNumberOfDigits +1) {
						if (!zeros.isEmpty()) zeros = zeros.substring(1);
					}
					generated.add(zeros + startNumber);
					oldNumberOfDigits = String.valueOf(startNumber).length();
					startNumber++;
				}

				listOfGenerated.add(generated);

			} else {
				// range is not in the format number-number -> abort
				throw new WrongPatternException("The pattern \"" + pattern + "\" has a wrong syntax. The format numer-number not found.");
			}
		}

		// add values among the generated numbers as one item lists
		List<List<String>> listOfGeneratorsAndValues = new ArrayList<List<String>>();
		int index = 0;

		for (List<String> list : listOfGenerated) {
			if (index < values.size()) {
				List<String> listWithValue = new ArrayList<>();
				listWithValue.add(values.get(index));
				listOfGeneratorsAndValues.add(listWithValue);
				index++;
			}
			listOfGeneratorsAndValues.add(list);
		}

		// complete list with remaining values
		for (int i = index; i < values.size(); i++) {
			List<String> listWithValue = new ArrayList<>();
			listWithValue.add(values.get(i));
			listOfGeneratorsAndValues.add(listWithValue);
		}

		// generate all posibilities
		return getCombinationsOfLists(listOfGeneratorsAndValues);
	}

	/**
	 * Method generates all combinations of joining of strings.
	 * It respects given order of lists.
	 * Example: input: [[a,b],[c,d]], output: [ac,ad,bc,bd]
	 * @param lists list of lists, which will be joined
	 * @return all joined strings
	 */
	private static List<String> getCombinationsOfLists(List<List<String>> lists) {
		if (lists.isEmpty()) {
			// this should not happen
			return new ArrayList<>();
		}
		if (lists.size() == 1) {
			return lists.get(0);
		}
		List<String> result = new ArrayList<String>();

		List<String> list = lists.remove(0);
		// get recursively all posibilities without first list
		List<String> posibilities = getCombinationsOfLists(lists);

		// join all strings from first list with the others
		for (String item: list) {
			if (posibilities.isEmpty()) {
				result.add(item);
			} else {
				for (String itemToConcat : posibilities) {
					result.add(item + itemToConcat);
				}
			}
		}
		return result;
	}

	/**
	 * Return encrypted version of input in UTF-8 by HmacSHA256
	 *
	 * @param input input to encrypt
	 * @return encrypted value
	 */
	public static String getMessageAuthenticationCode(String input) {

		if (input == null)
			throw new NullPointerException("input must not be null");
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(BeansUtils.getCoreConfig().getMailchangeSecretKey().getBytes("UTF-8"),"HmacSHA256"));
			byte[] macbytes = mac.doFinal(input.getBytes("UTF-8"));
			return new BigInteger(macbytes).toString(Character.MAX_RADIX);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Send validation email related to requested change of users preferred email.
	 *
	 * @param user user to change preferred email for
	 * @param url base URL of running perun instance passed from RPC
	 * @param email new email address to send notification to
	 * @param changeId ID of change request in DB
	 * @throws InternalErrorException
	 */
	public static void sendValidationEmail(User user, String url, String email, int changeId) throws InternalErrorException {

		// create mail sender
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("localhost");

		// create message
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setFrom(BeansUtils.getCoreConfig().getMailchangeBackupFrom());

		String instanceName = BeansUtils.getCoreConfig().getInstanceName();

		message.setSubject("["+instanceName+"] New email address verification");

		// get validation link params
		String i = Integer.toString(changeId, Character.MAX_RADIX);
		String m = Utils.getMessageAuthenticationCode(i);

		try {

			// !! There is a hard-requirement for Perun instance
			// to host GUI on same server as RPC like: "serverUrl/gui/"

			URL urlObject = new URL(url);

			// use default if unknown rpc path
			String path = "/gui/";

			if (urlObject.getPath().contains("/krb/")) {
				path = "/krb/gui/";
			} else if (urlObject.getPath().contains("/fed/")) {
				path = "/fed/gui/";
			} else if (urlObject.getPath().contains("/cert/")) {
				path = "/cert/gui/";
			}

			StringBuilder link = new StringBuilder();

			link.append(urlObject.getProtocol());
			link.append("://");
			link.append(urlObject.getHost());
			link.append(path);
			link.append("?i=");
			link.append(URLEncoder.encode(i, "UTF-8"));
			link.append("&m=");
			link.append(URLEncoder.encode(m, "UTF-8"));
			link.append("&u=" + user.getId());

			// Build message
			String text = "Dear "+user.getDisplayName()+",\n\nWe've received request to change your preferred email address to: "+email+"."+
				"\n\nTo confirm this change please use link below:\n\n"+link+"\n\n" +
				"Message is automatically generated." +
				"\n----------------------------------------------------------------" +
				"\nPerun - Identity & Access Management System";

			message.setText(text);

			mailSender.send(message);

		} catch (UnsupportedEncodingException ex) {
			throw new InternalErrorException("Unable to encode validation URL for mail change.", ex);
		} catch (MalformedURLException ex) {
			throw new InternalErrorException("Not valid URL of running Perun instance.", ex);
		}

	}

	/**
	 * Sends email with link to non-authz password reset GUI where user
	 * can reset forgotten password
	 *
	 * @param user user to send notification for
	 * @param email user's email to send notification to
	 * @param namespace namespace to reset password in
	 * @param url base URL of Perun instance
	 * @param id ID of pwd reset request
	 * @param messageTemplate message of the email
	 * @param subject subject of the email
	 * @throws InternalErrorException
	 */
	public static void sendPasswordResetEmail(User user, String email, String namespace, String url, int id, String messageTemplate, String subject) throws InternalErrorException {

		// create mail sender
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("localhost");

		// create message
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setFrom(BeansUtils.getCoreConfig().getMailchangeBackupFrom());

		String instanceName = BeansUtils.getCoreConfig().getInstanceName();

		if (subject == null) {
			message.setSubject("[" + instanceName + "] Password reset in namespace: " + namespace);
		} else {
			subject = subject.replace("{namespace}", namespace);
			subject = subject.replace("{instanceName}", instanceName);
			message.setSubject(subject);
		}

		// get validation link params
		String i = cipherInput(String.valueOf(user.getId()), false);
		String m = cipherInput(String.valueOf(id), false);

		try {

			URL urlObject = new URL(url);

			StringBuilder link = new StringBuilder();

			link.append(urlObject.getProtocol());
			link.append("://");
			link.append(urlObject.getHost());
			// reset link uses non-authz
			link.append("/non/pwd-reset/");
			link.append("?i=");
			link.append(URLEncoder.encode(i, "UTF-8"));
			link.append("&m=");
			link.append(URLEncoder.encode(m, "UTF-8"));

			//validity formatting
			String validity = Integer.toString(BeansUtils.getCoreConfig().getPwdresetValidationWindow());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR, Integer.parseInt(validity));
			String validityFormatted = sdf.format(calendar.getTime());

			// Build message en
			String textEn = "Dear " + user.getDisplayName() + ",\n\nWe've received request to reset your password in namespace \"" + namespace + "\"." +
					"\n\nPlease visit the link below, where you can set new password:\n\n" + link + "\n\n" +
					"Link is valid till " + validityFormatted + "\n\n" +
					"Message is automatically generated." +
					"\n----------------------------------------------------------------" +
					"\nPerun - Identity & Access Management System";


			if (messageTemplate == null) {
				message.setText(textEn);
			} else {
				messageTemplate = messageTemplate.replace("{link}", link);
				messageTemplate = messageTemplate.replace("{displayName}", user.getDisplayName());
				messageTemplate = messageTemplate.replace("{namespace}", namespace);
				messageTemplate = messageTemplate.replace("{validity}", validityFormatted);
				message.setText(messageTemplate);
			}

			mailSender.send(message);

		} catch (UnsupportedEncodingException ex) {
			throw new InternalErrorException("Unable to encode URL for password reset.", ex);
		} catch (MalformedURLException ex) {
			throw new InternalErrorException("Not valid URL of running Perun instance.", ex);
		}

	}

	/**
	 * Sends email to user confirming his password was changed.
	 *
	 * @param user user to send notification for
	 * @param email user's email to send notification to
	 * @param namespace namespace the password was re-set
	 * @throws InternalErrorException
	 */
	public static void sendPasswordResetConfirmationEmail(User user, String email, String namespace) throws InternalErrorException {

		// create mail sender
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("localhost");

		// create message
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setFrom(BeansUtils.getCoreConfig().getMailchangeBackupFrom());

		String instanceName = BeansUtils.getCoreConfig().getInstanceName();

		message.setSubject("["+instanceName+"] Password reset in namespace: "+namespace);

		// get validation link params
		String i = cipherInput(String.valueOf(user.getId()), false);
		String m = cipherInput(namespace, false);

			// Build message
			String text = "Dear "+user.getDisplayName()+",\n\nyour password in namespace \""+namespace+"\" was successfully reset."+
					"\n\nThis message is automatically sent to all your email addresses registered in "+instanceName+" in order to prevent malicious password reset without your knowledge.\n\n" +
					"If you didn't request / perform password reset, please notify your administrators and support at "+BeansUtils.getCoreConfig().getMailchangeBackupFrom()+" to resolve this security issue.\n\n" +
					"Message is automatically generated." +
					"\n----------------------------------------------------------------" +
					"\nPerun - Identity & Access Management System";

			message.setText(text);

			mailSender.send(message);

	}

	/**
	 * Return en/decrypted version of input using AES/CBC/PKCS5PADDING cipher.
	 * Perun's internal secretKey and initVector are used (you can configure them in
	 * perun.properties file).
	 *
	 * @param plainText text to en/decrypt
	 * @param decrypt TRUE = decrypt input / FALSE = encrypt input
	 * @return en/decrypted text
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException if anything fails
	 */
	public static String cipherInput(String plainText, boolean decrypt) throws InternalErrorException {

		try {

			String encryptionKey = BeansUtils.getCoreConfig().getPwdresetSecretKey();
			String initVector = BeansUtils.getCoreConfig().getPwdresetInitVector();

			Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			SecretKeySpec k = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
			c.init((decrypt) ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE, k, new IvParameterSpec(initVector.getBytes("UTF-8")));

			if (decrypt) {

				byte[] bytes = Base64.decodeBase64(plainText.getBytes("UTF-8"));
				return new String(c.doFinal(bytes), "UTF-8");

			} else {

				byte[] bytes = Base64.encodeBase64(c.doFinal(plainText.getBytes("UTF-8")));
				return new String(bytes, "UTF-8");

			}

		} catch (Exception ex) {

			throw new InternalErrorException("Error when encrypting message", ex);

		}

	}

	/**
	 * Checks whether the destination is not null and is of the right type.
	 *
	 * @param destination destination to check
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException if destination is null
	 * @throws cz.metacentrum.perun.core.api.exceptions.WrongPatternException if destination is not of the right type
	 */
	public static void checkDestinationType(Destination destination) throws InternalErrorException, WrongPatternException  {
		if (destination == null) {
			throw new InternalErrorException("Destination is null.");
		}
		String destinationType = destination.getType();
		if ((!destinationType.equals(Destination.DESTINATIONHOSTTYPE)
				&& (!destinationType.equals(Destination.DESTINATIONEMAILTYPE))
				&& (!destinationType.equals(Destination.DESTINATIONSEMAILTYPE))
				&& (!destinationType.equals(Destination.DESTINATIONURLTYPE))
				&& (!destinationType.equals(Destination.DESTINATIONUSERHOSTTYPE))
				&& (!destinationType.equals(Destination.DESTINATIONUSERHOSTPORTTYPE))
				&& (!destinationType.equals(Destination.DESTINATIONSERVICESPECIFICTYPE)))) {
			throw new WrongPatternException("Destination type " + destinationType + " is not supported.");
		}
	}

	/**
	 * Sends SMS to the phone number of a user with the given message.
	 * The phone number is taken from the user attribute urn:perun:user:attribute-def:def:phone.
	 *
	 * @param sess session
	 * @param user receiver of the message
	 * @param message sms message to send
	 * @throws InternalErrorException when the attribute value cannot be found or is broken
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException when the actor has not right to get the attribute
	 * @throws cz.metacentrum.perun.core.api.exceptions.UserNotExistsException when given user does not exist
	 */
	public static void sendSMS(PerunSession sess, User user, String message) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		if (user == null) {
			throw new cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException("user is null");
		}
		if (message == null) {
			throw new cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException("message is null");
		}
		String telNumber;
		try {
			telNumber = (String) sess.getPerun().getAttributesManager().getAttribute(sess, user, userPhoneAttribute).getValue();
		} catch (AttributeNotExistsException ex ) {
			log.error("Sendig SMS with text \"{}\" to user {} failed: cannot get tel. number.", message, user );
			throw new InternalErrorException("The attribute " + userPhoneAttribute + " has not been found.", ex);
		} catch (WrongAttributeAssignmentException ex) {
			log.error("Sendig SMS with text \"{}\" to user {} failed: cannot get tel. number.", message, user );
			throw new InternalErrorException("The attribute " + userPhoneAttribute + " has not been found in user attributes.", ex);
		}
		sendSMS(telNumber, message);
	}

	/**
	 * Sends SMS to the phone number of a member with the given message.
	 * The phone number is taken from the user attribute urn:perun:member:attribute-def:def:phone.
	 *
	 * @param sess session
	 * @param member receiver of the message
	 * @param message sms message to send
	 * @throws InternalErrorException when the attribute value cannot be found or is broken
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException when the actor has not right to get the attribute
	 * @throws cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException when given member does not exist
	 */
	public static void sendSMS(PerunSession sess, Member member, String message) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		String telNumber;
		try {
			telNumber = (String) sess.getPerun().getAttributesManager().getAttribute(sess, member, memberPhoneAttribute).getValue();
		} catch (AttributeNotExistsException ex) {
			log.error("Sendig SMS with text \"{}\" to member {} failed: cannot get tel. number.", message, member );
			throw new InternalErrorException("The attribute " + memberPhoneAttribute + " has not been found.", ex);
		} catch (WrongAttributeAssignmentException ex) {
			log.error("Sendig SMS with text \"{}\" to member {} failed: cannot get tel. number.", message, member );
			throw new InternalErrorException("The attribute " + memberPhoneAttribute + " has not been found in user attributes.", ex);
		}
		sendSMS(telNumber, message);
	}

	/**
	 * Sends SMS to the phone number with the given message.
	 * The sending provides external program for sending sms.
	 * Its path is saved in the perun property perun.sms.program.
	 *
	 * @param telNumber phone number of the receiver
	 * @param message sms message to send
	 * @throws InternalErrorException when there is something wrong with external program
	 * @throws IllegalArgumentException when the phone or message has a wrong format
	 */
	public static void sendSMS(String telNumber, String message) throws InternalErrorException {
		log.debug("Sending SMS with text \"{}\" to tel. number {}.", message, telNumber);

		try {
			// create properties list
			List<String> processProperties = new ArrayList<>();
			// pass the location of external program for sending sms
			processProperties.add(BeansUtils.getCoreConfig().getSmsProgram());
			// pass program options
			processProperties.add("-p");
			processProperties.add(telNumber);
			processProperties.add("-m");
			processProperties.add(message);
			// execute
			ProcessBuilder pb = new ProcessBuilder(processProperties);
			Process process;
			process = pb.start();
			int exitValue;
			try {
				exitValue = process.waitFor();
			} catch (InterruptedException ex) {
				String errMsg = "The external process for sending sms was interrupted.";
				log.error("Sending SMS with text \"{}\" to tel. number {} failed.", message, telNumber);
				throw new InternalErrorException(errMsg, ex);
			}

			// handle response
			if (exitValue == 0) {
				// successful
				log.debug("SMS with text \"{}\" to tel. number {} successfully sent.", message, telNumber);
			} else if ((exitValue == 1) || (exitValue == 2)) {
				// users fault
				String errMsg = getStringFromInputStream(process.getErrorStream());
				log.error("Sending SMS with text \"{}\" to tel. number {} failed.", message, telNumber);
				throw new cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException(errMsg);
			} else if (exitValue > 2) {
				// internal fault
				String errMsg = getStringFromInputStream(process.getErrorStream());
				log.error("Sending SMS with text \"{}\" to tel. number {} failed.", message, telNumber);
				throw new InternalErrorException(errMsg);
			}

		} catch (IOException ex) {
			log.warn("Sending SMS with text \"{}\" to tel. number {} failed.", message, telNumber);
			throw new InternalErrorException("Cannot access the sms external application.", ex);
		}

	}

	/**
	 * Get BigDecimal number like '1024' in Bytes and create better readable
	 * String with metric value like '1K' where K means KiloBytes.
	 *
	 * Use M,G,T,P,E like multipliers of 1024.
	 *
	 * If quota is not dividable by 1024 use B (Bytes) without dividing.
	 *
	 * @param quota in big natural number
	 * @return string with number and metric
	 */
	public static String bigDecimalBytesToReadableStringWithMetric(BigDecimal quota) throws InternalErrorException {
		if(quota == null) throw new InternalErrorException("Quota in BigDecimal can't be null if we want to convert it to number with metric.");
		//Prepare variable for result
		String stringWithMetric;
		//Try to divide quota to get result module 1024^x = 0 where X is in [K-0,M-1,G-2,T-3,P-4,E-5]
		//If module is bigger than 0, try x-1
		if(!quota.divide(BigDecimal.valueOf(ModulesUtilsBlImpl.E)).stripTrailingZeros().toPlainString().contains(".")) {
			//divide by 1024^5
			stringWithMetric = quota.divide(BigDecimal.valueOf(ModulesUtilsBlImpl.E)).stripTrailingZeros().toPlainString() + "E";
		} else if(!quota.divide(BigDecimal.valueOf(ModulesUtilsBlImpl.P)).stripTrailingZeros().toPlainString().contains(".")) {
			//divide by 1024^4
			stringWithMetric = quota.divide(BigDecimal.valueOf(ModulesUtilsBlImpl.P)).stripTrailingZeros().toPlainString() + "P";
		} else if(!quota.divide(BigDecimal.valueOf(ModulesUtilsBlImpl.T)).stripTrailingZeros().toPlainString().contains(".")) {
			//divide by 1024^3
			stringWithMetric = quota.divide(BigDecimal.valueOf(ModulesUtilsBlImpl.T)).stripTrailingZeros().toPlainString() + "T";
		} else if(!quota.divide(BigDecimal.valueOf(ModulesUtilsBlImpl.G)).stripTrailingZeros().toPlainString().contains(".")) {
			//divide by 1024^2
			stringWithMetric = quota.divide(BigDecimal.valueOf(ModulesUtilsBlImpl.G)).stripTrailingZeros().toPlainString() + "G";
		} else if(!quota.divide(BigDecimal.valueOf(ModulesUtilsBlImpl.M)).stripTrailingZeros().toPlainString().contains(".")) {
			//divide by 1024^1
			stringWithMetric = quota.divide(BigDecimal.valueOf(ModulesUtilsBlImpl.M)).stripTrailingZeros().toPlainString() + "M";
		} else {
			//can't be diveded by 1024^x where x>0 so let it be in the format like it already is, convert it to BigInteger without fractional part
			stringWithMetric = quota.toBigInteger().toString() + "K";
		}
		//return result format with metric
		return stringWithMetric;
	}

	private static String getStringFromInputStream(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder out = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			out.append(line);
		}
		return out.toString();
	}

	/**
	 * IMPORTANT: this method not convert utf to ascii, just try to convert some problematic
	 * chars to UTF and others change to '?'!!!
	 *
	 * @param s
	 * @return converted string from ascii to something near utf
	 */
	public synchronized static String utftoasci(String s){
		final StringBuffer sb = new StringBuffer( s.length() * 2 );

		final StringCharacterIterator iterator = new StringCharacterIterator( s );

		char ch = iterator.current();

		while( ch != StringCharacterIterator.DONE ){
			if(Character.getNumericValue(ch)>=0){
				sb.append( ch );
			}else{
				boolean f=false;
				if(Character.toString(ch).equals("Ê")){sb.append("E");f=true;}
				if(Character.toString(ch).equals("È")){sb.append("E");f=true;}
				if(Character.toString(ch).equals("ë")){sb.append("e");f=true;}
				if(Character.toString(ch).equals("é")){sb.append("e");f=true;}
				if(Character.toString(ch).equals("è")){sb.append("e");f=true;}
				if(Character.toString(ch).equals("Â")){sb.append("A");f=true;}
				if(Character.toString(ch).equals("ä")){sb.append("a");f=true;}
				if(Character.toString(ch).equals("ß")){sb.append("ss");f=true;}
				if(Character.toString(ch).equals("Ç")){sb.append("C");f=true;}
				if(Character.toString(ch).equals("Ö")){sb.append("O");f=true;}
				if(Character.toString(ch).equals("º")){sb.append("");f=true;}
				if(Character.toString(ch).equals("ª")){sb.append("");f=true;}
				if(Character.toString(ch).equals("º")){sb.append("");f=true;}
				if(Character.toString(ch).equals("Ñ")){sb.append("N");f=true;}
				if(Character.toString(ch).equals("É")){sb.append("E");f=true;}
				if(Character.toString(ch).equals("Ä")){sb.append("A");f=true;}
				if(Character.toString(ch).equals("Å")){sb.append("A");f=true;}
				if(Character.toString(ch).equals("Ü")){sb.append("U");f=true;}
				if(Character.toString(ch).equals("ö")){sb.append("o");f=true;}
				if(Character.toString(ch).equals("ü")){sb.append("u");f=true;}
				if(Character.toString(ch).equals("á")){sb.append("a");f=true;}
				if(Character.toString(ch).equals("Ó")){sb.append("O");f=true;}
				if(Character.toString(ch).equals("ě")){sb.append("e");f=true;}
				if(Character.toString(ch).equals("Ě")){sb.append("E");f=true;}
				if(Character.toString(ch).equals("š")){sb.append("s");f=true;}
				if(Character.toString(ch).equals("Š")){sb.append("S");f=true;}
				if(Character.toString(ch).equals("č")){sb.append("c");f=true;}
				if(Character.toString(ch).equals("Č")){sb.append("C");f=true;}
				if(Character.toString(ch).equals("ř")){sb.append("r");f=true;}
				if(Character.toString(ch).equals("Ř")){sb.append("R");f=true;}
				if(Character.toString(ch).equals("ž")){sb.append("z");f=true;}
				if(Character.toString(ch).equals("Ž")){sb.append("Z");f=true;}
				if(Character.toString(ch).equals("ý")){sb.append("y");f=true;}
				if(Character.toString(ch).equals("Ý")){sb.append("Y");f=true;}
				if(Character.toString(ch).equals("í")){sb.append("i");f=true;}
				if(Character.toString(ch).equals("Í")){sb.append("I");f=true;}
				if(Character.toString(ch).equals("ó")){sb.append("o");f=true;}
				if(Character.toString(ch).equals("ú")){sb.append("u");f=true;}
				if(Character.toString(ch).equals("Ú")){sb.append("u");f=true;}
				if(Character.toString(ch).equals("ů")){sb.append("u");f=true;}
				if(Character.toString(ch).equals("Ů")){sb.append("U");f=true;}
				if(Character.toString(ch).equals("Ň")){sb.append("N");f=true;}
				if(Character.toString(ch).equals("ň")){sb.append("n");f=true;}
				if(Character.toString(ch).equals("Ť")){sb.append("T");f=true;}
				if(Character.toString(ch).equals("ť")){sb.append("t");f=true;}
				if(Character.toString(ch).equals(" ")){sb.append(" ");f=true;}

				if(!f){
					sb.append("?");
				}
			}
			ch = iterator.next();
		}
		return sb.toString();
	}

	/**
	 * Convert input string (expected UTF-8) to ASCII if possible.
	 * Any non-ASCII character is replaced by replacement parameter.
	 *
	 * @param input String to convert from UTF-8 to ASCII.
	 * @param replacement Replacement character used for all non-ASCII chars in input.
	 * @return converted string from ascii to something near utf
	 */
	public synchronized static String toASCII(String input, Character replacement) {

		String normalizedOutput = "";

		// take unicode characters one by one and normalize them
		for ( int i=0; i<input.length(); i++ ) {
			char c = input.charAt(i);
			// normalize a single unicode character, then remove every non-ascii symbol (like accents)
			String normalizedChar = Normalizer.normalize(String.valueOf(c) , Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");

			if ( ! normalizedChar.isEmpty() ) {
				// if there is a valid ascii representation, use it
				normalizedOutput += normalizedChar;
			} else {
				// otherwise replace character with an "replacement"
				normalizedOutput += replacement;
			}
		}
		return normalizedOutput;

	}

	/**
	 * Determine if attribute is large (can contain value over 4kb).
	 *
	 * @param sess perun session
	 * @param attribute attribute to be checked
	 * @return true if the attribute is large
	 */
	public static boolean isLargeAttribute(PerunSession sess, AttributeDefinition attribute) {
		return (attribute.getType().equals(LinkedHashMap.class.getName()) ||
				attribute.getType().equals(BeansUtils.largeStringClassName) ||
				attribute.getType().equals(BeansUtils.largeArrayListClassName));
	}

	/**
	 * Extends given calendar by given period.
	 *
	 * @param calendar calendar to be extended
	 * @param period period used to extend calendar
	 * @throws InternalErrorException when the period has wrong format,
	 * allowed format is given by regex "\\+([0-9]+)([dmy]?)"
	 */
	public static void extendCalendarByPeriod(Calendar calendar, String period) throws InternalErrorException {
		// By default do not add nothing
		int amount = 0;
		int field;

		// We will add days/months/years
		Pattern p = Pattern.compile("\\+([0-9]+)([dmy]?)");
		Matcher m = p.matcher(period);
		if (m.matches()) {
			String countString = m.group(1);
			amount = Integer.valueOf(countString);

			String dmyString = m.group(2);
			if (dmyString.equals("d")) {
				field = Calendar.DAY_OF_YEAR;
			} else if (dmyString.equals("m")) {
				field = Calendar.MONTH;
			} else if (dmyString.equals("y")) {
				field = Calendar.YEAR;
			} else {
				throw new InternalErrorException("Wrong format of period. Period: " + period);
			}
		} else {
			throw new InternalErrorException("Wrong format of period. Period: " + period);
		}
		// Add days/months/years
		calendar.add(field, amount);
	}

	/**
	 * Extends given calendar by values from given matcher.
	 * @param calendar calendar to be extended
	 * @param matcher matcher with day and month values
	 * @return True if the extension is next year, false otherwise.
	 */
	public static boolean extendCalendarByStaticDate(Calendar calendar, Matcher matcher) {

		int day = Integer.valueOf(matcher.group(1));
		int month = Integer.valueOf(matcher.group(2));

		// Get current year
		int year = calendar.get(Calendar.YEAR);

		// We must detect if the extension date is in current year or in a next year
		boolean extensionInNextYear;
		Calendar extensionCalendar = Calendar.getInstance();
		extensionCalendar.set(year, month-1, day);
		Calendar today = Calendar.getInstance();

		// check if extension is next year
		extensionInNextYear = extensionCalendar.before(today);

		// Set the date to which the membership should be extended, can be changed if there was grace period, see next part of the code
		calendar.set(year, month-1, day); // month is 0-based
		if (extensionInNextYear) {
			calendar.add(Calendar.YEAR, 1);
		}

		return extensionInNextYear;
	}

	/**
	 * Extends grace period calendar by values from given matcher.
	 * @param gracePeriodCalendar grace period calendar
	 * @param matcher matcher
	 * @param extensionCalendar extension calendar
	 * @param extensionInNextYear is extension next year
	 * @return pair of field(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH) and amount
	 * @throws InternalErrorException when given matcher contains invalid data
	 */
	public static Pair<Integer, Integer> extendGracePeriodCalendar(Calendar gracePeriodCalendar, Matcher matcher, Calendar extensionCalendar, boolean extensionInNextYear) throws InternalErrorException {
		if (!matcher.matches()) {
			return null;
		}
		String countString = matcher.group(1);
		int amount = Integer.valueOf(countString);

		// Set the gracePeriodCalendar to the extension date
		int year = extensionCalendar.get(Calendar.YEAR);
		int month = extensionCalendar.get(Calendar.MONTH);
		int day = extensionCalendar.get(Calendar.DAY_OF_MONTH);
		gracePeriodCalendar.set(year, month, day);
		if (extensionInNextYear) {
			gracePeriodCalendar.add(Calendar.YEAR, 1);
		}

		int field;
		String dmyString = matcher.group(2);
		switch (dmyString) {
			case "d":
				field = Calendar.DAY_OF_YEAR;
				break;
			case "m":
				field = Calendar.MONTH;
				break;
			case "y":
				field = Calendar.YEAR;
				break;
			default:
				throw new InternalErrorException("Wrong format of gracePeriod.");
		}
		// subtracts period definition, e.g. 3m
		gracePeriodCalendar.add(field, -amount);

		return new Pair<>(field, amount);
	}
}
