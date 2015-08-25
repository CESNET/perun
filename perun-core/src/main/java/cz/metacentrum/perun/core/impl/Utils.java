package cz.metacentrum.perun.core.impl;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpStatus;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

/**
 * Utilities.
 */
public class Utils {

	private final static Logger log = LoggerFactory.getLogger(Utils.class);
	public final static String configurationsLocations = "/etc/perun/";
	private static Properties properties;
	public static final Pattern emailPattern = Pattern.compile("^[-_A-Za-z0-9+']+(\\.[-_A-Za-z0-9+']+)*@[-A-Za-z0-9]+(\\.[-A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	public static final Pattern hostPattern = Pattern.compile("^[^/]*(//)?([^/]*).*");
	
	private static final Pattern titleBeforePattern = Pattern.compile("^([\\p{L}]+[.])|(et)$");
	private static final Pattern firstNamePattern = Pattern.compile("^[\\p{L}-']+$");
	private static final Pattern lastNamePattern = Pattern.compile("^([\\p{L}-']+)|([\\p{L}][.])$");
	
	/**
	 * Replaces dangerous characters.
	 * Replaces : with - and spaces with _.
	 *
	 * @param str string to be normalized
	 * @return normalized string
	 */
	public static String normalizeString(String str) {
		log.trace("Entering normalizeString: str='" +  str + "'");
		return str.replace(':', '-').replace(' ', '_');
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
		log.trace("Entering join: objs='" +  objs + "', separator='" +  separator + "'");
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
	 * Define min length of some entity name.
	 *
	 * @param name name of entity
	 * @param minLength
	 * @throws MinSizeExceededException
	 */
	public static void checkMinLength(String name, int minLength) throws MinSizeExceededException{

		if(name.length()<minLength) throw new MinSizeExceededException("Length of name is too short! MinLength=" + minLength + ", ActualLength=" + name.length());
	}

	/**
	 * Define max length of some entity name.
	 *
	 * @param name name of entity
	 * @param maxLength
	 * @throws
	 */
	public static void checkMaxLength(String name, int maxLength) throws MaxSizeExceededException{

		if(name.length()<maxLength) throw new MaxSizeExceededException("Length of name is too long! MaxLength=" + maxLength + ", ActualLength=" + name.length());
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
	public static int getNewId(Object jdbc, String sequenceName) throws InternalErrorException {
		String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");

		String url = "";
	
		// try to deduce database type from jdbc connection metadata
		try {
			if (jdbc instanceof JdbcTemplate) {
				DataSource ds = ((JdbcTemplate)jdbc).getDataSource();
				if(ds instanceof BasicDataSource)
				url = ((BasicDataSource)ds).getUrl();
				// c.close();
			}
		} catch (Exception e) {
		}
		if(url.matches("hsqldb")) {
			dbType = "hsqldb";
		} else if(url.matches("oracle")) {
			dbType = "oracle";
		} else if(url.matches("postgresql")) {
			dbType = "postgresql";
		}	

		String query = "";
		if (dbType.equals("oracle")) {
			query = "select " + sequenceName + ".nextval from dual";
		} else if (dbType.equals("postgresql")) {
			query = "select nextval('" + sequenceName + "')";
 		} else if (dbType.equals("hsqldb")) {
 			query = "call next value for " + sequenceName + ";";
 		} else {
			throw new InternalErrorException("Unsupported DB type");
		}

		// Decide which type of the JdbcTemplate is provided
		try {
			if (jdbc instanceof SimpleJdbcTemplate) {
				return ((SimpleJdbcTemplate) jdbc).queryForInt(query);
			} else if (jdbc instanceof JdbcTemplate) {
				return ((JdbcTemplate) jdbc).queryForInt(query);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		// Shouldn't ever happened
		throw new InternalErrorException("Unsupported DB type");
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
		parsedName.put("titleBefore", titleBefore);
		if (firstName.isEmpty()) firstName = null;
		parsedName.put("firstName", firstName);
		if (lastName.isEmpty()) lastName = null;
		parsedName.put("lastName", lastName);
		if (titleAfter.isEmpty()) titleAfter = null;
		parsedName.put("titleAfter", titleAfter);

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
			mac.init(new SecretKeySpec(BeansUtils.getPropertyFromConfiguration("perun.mailchange.secretKey").getBytes("UTF-8"),"HmacSHA256"));
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
		message.setFrom(BeansUtils.getPropertyFromConfiguration("perun.mailchange.backupFrom"));
		message.setSubject("[Perun] New email address verification");

		// get validation link params
		String i = Integer.toString(changeId, Character.MAX_RADIX);
		String m = Utils.getMessageAuthenticationCode(i);

		try {

			// !! There is a hard-requirement for Perun instance
			// to host GUI on same server as RPC like: "serverUrl/gui/"

			URL urlObject = new URL(url);

			// use default if unknown rpc path
			String path = "/gui/";

			if (urlObject.getPath().contains("/krb/rpc/")) {
				path = "/krb/gui/";
			} else if (urlObject.getPath().contains("/fed/rpc/")) {
				path = "/fed/gui/";
			} else if (urlObject.getPath().contains("/cert/rpc/")) {
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
				"\nPerun - User and Resource Management System";

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
	 * @throws InternalErrorException
	 */
	public static void sendPasswordResetEmail(User user, String email, String namespace, String url, int id) throws InternalErrorException {

		// create mail sender
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("localhost");

		// create message
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setFrom(BeansUtils.getPropertyFromConfiguration("perun.mailchange.backupFrom"));
		message.setSubject("[Perun] Password reset in namespace: "+namespace);

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

			// Build message
			String text = "Dear "+user.getDisplayName()+",\n\nWe've received request to reset your password in namespace \""+namespace+"\"."+
					"\n\nPlease visit the link below, where you can set new password:\n\n"+link+"\n\n" +
					"Message is automatically generated." +
					"\n----------------------------------------------------------------" +
					"\nPerun - User and Resource Management System";

			message.setText(text);

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
		message.setFrom(BeansUtils.getPropertyFromConfiguration("perun.mailchange.backupFrom"));
		message.setSubject("[Perun] Password reset in namespace: "+namespace);

		// get validation link params
		String i = cipherInput(String.valueOf(user.getId()), false);
		String m = cipherInput(namespace, false);

			// Build message
			String text = "Dear "+user.getDisplayName()+",\n\nyour password in namespace \""+namespace+"\" was successfully reset."+
					"\n\nThis message is automatically sent to all your email addresses registered in Perun in order to prevent malicious password reset without your knowledge.\n\n" +
					"If you didn't request / perform password reset, please notify your VO administrator and support at "+BeansUtils.getPropertyFromConfiguration("perun.mailchange.backupFrom")+" to resolve this security issue.\n\n" +
					"Message is automatically generated." +
					"\n----------------------------------------------------------------" +
					"\nPerun - User and Resource Management System";

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

			String encryptionKey = BeansUtils.getPropertyFromConfiguration("perun.pwdreset.secretKey");
			String initVector = BeansUtils.getPropertyFromConfiguration("perun.pwdreset.initVector");

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
	 * @param perun perun
	 * @param sess session
	 * @param user receiver of the message
	 * @param message sms message to send
	 * @throws InternalErrorException when 
	 */
	public static void sendSMS(PerunBl perun, PerunSession sess, User user, String message) throws InternalErrorException {
		if (user == null) {
			throw new cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException("sendSMS: user is null");
		}
		if (message == null) {
			throw new cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException("sendSMS: message is null");
		}
		String phoneAttributeName = "urn:perun:user:attribute-def:def:phone";
		String telNumber;
	    try {
		telNumber = (String) perun.getAttributesManagerBl().getAttribute(sess, user, phoneAttributeName).getValue();
	    } catch (AttributeNotExistsException ex ) {
		log.debug("Sendig SMS with text \"" + message + "\" to user " + user + "failed: cannot get tel. number." );
		throw new InternalErrorException("The attribute " + phoneAttributeName + " has not been found.", ex);
	    } catch (WrongAttributeAssignmentException ex) {
		log.debug("Sendig SMS with text \"" + message + "\" to user " + user + "failed: cannot get tel. number." );
		throw new InternalErrorException("The attribute " + phoneAttributeName + " has not been found in user attributes.", ex);
	    }
		sendSMS(telNumber, message);
	}
	
	/**
	 * Sends SMS to the phone number of a member with the given message.
	 * The phone number is taken from the user attribute urn:perun:member:attribute-def:def:phone.
	 * 
	 * @param perun perun
	 * @param sess session
	 * @param member receiver of the message
	 * @param message sms message to send
	 * @throws InternalErrorException when 
	 */
	public static void sendSMS(PerunBl perun, PerunSession sess, Member member, String message) throws InternalErrorException {
		String phoneAttributeName = "urn:perun:member:attribute-def:def:phone";
		String telNumber;
	    try {
		telNumber = (String) perun.getAttributesManagerBl().getAttribute(sess, member, phoneAttributeName).getValue();
	    } catch (WrongAttributeAssignmentException ex) {
		log.debug("Sendig SMS with text \"" + message + "\" to member " + member + "failed: cannot get tel. number." );
		throw new InternalErrorException("The attribute " + phoneAttributeName + " has not been found in user attributes.", ex);
	    } catch (AttributeNotExistsException ex) {
		log.debug("Sendig SMS with text \"" + message + "\" to member " + member + "failed: cannot get tel. number." );
		throw new InternalErrorException("The attribute " + phoneAttributeName + " has not been found.", ex);
	    }
		sendSMS(telNumber, message);
	}
	
	/**
	 * Sends SMS to the phone number with the given message.
	 * 
	 * @param telNumber phone number of the receiver
	 * @param message sms message to send
	 * @throws InternalErrorException 
	 */
	public static void sendSMS(String telNumber, String message) throws InternalErrorException {
		log.debug("Sending SMS with text \"" + message + "\" to tel. number " + telNumber + ".");
		
		// URL identificator
		String url = "https://sms.cesnet.cz/sendSMS";
		
		// Prepare post request
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setParameter(HttpMethodParams.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
		
		PostMethod post = new PostMethod(url);
		post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf8");
		post.addRequestHeader("Connection", "Close");
		
		NameValuePair[] data = {
			new NameValuePair("phoneNumber", telNumber),
			new NameValuePair("message", message)
		};
		post.setRequestBody(data);
		
		try {
			// Fire the post request
			int statusCode = httpClient.executeMethod(post);
			
			// check http status code
			if (statusCode != HttpStatus.SC_OK) {
				log.debug("Sending SMS with text \"" + message + "\" to tel. number " + telNumber + "failed: " + post.getStatusLine());
				throw new InternalErrorException("Sending SMS failed: Communication error with server " + url + " error status line: " + post.getStatusLine());
			}
			
			// check response code
			String response = post.getResponseBodyAsString();
			String[] responseArray = response.split("\n");
			
			int responseCode = Integer.parseInt(responseArray[0]);
			String responseMsg = responseArray[1];
			
			if (responseCode == 0) {
				// Successful
				log.debug("SMS with text \"" + message + "\" succssesfully sent to tel. number " + telNumber + " .");
			} else {
				// Unsuccessful
				log.debug("Sending SMS with text \"" + message + "\" to tel. number " + telNumber + "failed: " + responseMsg);
				if (statusCode > -4) {
					// internal problems
					throw new InternalErrorException("SMS was not sent. Error code " + statusCode + ": " + responseMsg);
				} else {
					// invalid user inputs
					throw new cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException("SMS was not sent. Error code " + statusCode + ": " + responseMsg);
					
				}
			}
			
		} catch (IOException ex) {
			log.debug("Sending SMS with text \"" + message + "\" to tel. number " + telNumber + " failed: Communication error with server " + url);
			throw new InternalErrorException("Sending SMS failed: Communication error with server " + url, ex);
		}		
	}
}
