package cz.metacentrum.perun.core.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.DiacriticNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MaxSizeExceededException;
import cz.metacentrum.perun.core.api.exceptions.MinSizeExceededException;
import cz.metacentrum.perun.core.api.exceptions.NumberNotInRangeException;
import cz.metacentrum.perun.core.api.exceptions.NumbersNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.SpaceNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.SpecialCharsNotAllowedException;
import java.util.LinkedHashSet;
/**
 * Utilities.
 */
public class Utils {

	private final static Logger log = LoggerFactory.getLogger(Utils.class);	
	private final static Pattern patternForCommonNameParsing = Pattern.compile("(([\\w]*. )*)([\\p{L}-']+) ([\\p{L}-']+)[, ]*(.*)");
	public final static String configurationsLocations = "/etc/perunv3/";
	
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
	 * Gets particular property from perun.properties file.
	 * 
	 * @param propertyName name of the property
	 * @return value of the property
	 */
	public static String getPropertyFromConfiguration(String propertyName) throws InternalErrorException {
		log.trace("Entering getPropertyFromConfiguration: propertyName='" +  propertyName + "'");
		notNull(propertyName, "propertyName");
		
		// Load properties file with configuration
		Properties properties = new Properties();
		try {
			// Get the path to the perun.properties file
		  BufferedInputStream is = new BufferedInputStream(new FileInputStream(Utils.configurationsLocations + "perun.properties"));
			properties.load(is);
			is.close();

			String property = properties.getProperty(propertyName);
			if (property == null) {
				throw new InternalErrorException("Property " + propertyName + " cannot be found in the configuration file");
			}
			return property;
		} catch (FileNotFoundException e) {
			throw new InternalErrorException("Cannot find perun.properties file", e);
		} catch (IOException e) {
			throw new InternalErrorException("Cannot read perun.properties file", e);
		}
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
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(Utils.configurationsLocations + propertyFile));
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
    String dbType = getPropertyFromConfiguration("perun.db.type");
    
    String query = "";
    if (dbType.equals("oracle")) {
      query = "select " + sequenceName + ".nextval from dual";
    } else if (dbType.equals("postgresql")) {
      query = "select nextval('" + sequenceName + "')";
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
   * Parse raw string which contains name of the user. 
   * Parsed name is split into the field titleBefore, firstName, lastName and titleAfter. 
   * All is stored in the hashMap.
   * 
   * @param rawName
   * @return HashMap contains entries with keys titleBefore, firstName, lastName and titleAfter.
   * @throws InternalErrorException if some exception has been thrown in procc
   */
  public static Map<String, String> parseCommonName(String rawName) throws InternalErrorException {  
    Map<String, String> parsedName = new HashMap<String, String>();
    String titleBefore = "";
    String firstName = "";
    String lastName = "";
    String titleAfter = "";

    // If rawName contains only one word then use it only for lastName
    try {
        if (rawName.matches("^[\\s]*[\\S]+[\\s]*$")) {
          lastName = rawName.trim();
        } else {
          Matcher matcher = patternForCommonNameParsing.matcher(rawName);
          matcher.find();           

          titleBefore = matcher.group(1).trim();
          firstName = matcher.group(3);
          lastName = matcher.group(4);
          titleAfter = matcher.group(5);
        }
    } catch (Exception ex) {
        throw new InternalErrorException("Problem with parsing of rawName ='" + rawName + "'",ex);
    }
    
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
     * are distributed, so the list is [a1b, a2b, a3c].
     * Multibrackets are aslo allowed. For example "a[00-01]b[90-91]c" generates [a00b90c, a00b91c, a01b90c, a01b91c].
     * 
     * @param pattern
     * @return list of all generated strings
     */
    public static List<String> generateStringsByPattern(String pattern) {
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
                result.add(pattern);
                return result;
            }
        }
        
        // if generators strings contain square brackets, wrong syntax, abort
        for (String generator: generators) {
            if (generator.contains("]") || (generator.contains("["))) {
                result.add(pattern);
                return result;
            }
        }
        
        // list, that contains list for each generator, with already generated numbers
        List<List<String>> listOfGenerated = new ArrayList<List<String>>();
        
        Pattern rangePattern = Pattern.compile("(\\d*)-(\\d*)");
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
                    // wrong format of the range -> skip this range (brackets)
                    continue;
                }
                
                // find out, how many zeros are before start number
                int zerosInStart = 0;
                int counter = 0;
                while ( (start.charAt(counter) == '0') && (counter < start.length()-1) ) {
                    zerosInStart++;
                    counter++;
                }
                
                String zeros = new String(new char[zerosInStart]).replace("\0", "0"); // replicate zeros
                int oldNumberOfDigits = numberOfDigits(startNumber);
                
                // list of already generated numbers
                List<String> generated = new ArrayList<String>();
                while (endNumber >= startNumber) {
                    // keep right number of zeros before number
                    if (numberOfDigits(startNumber) == oldNumberOfDigits +1) {
                        if (!zeros.isEmpty()) zeros = zeros.substring(1);
                    }
                    generated.add(zeros + startNumber);
                    oldNumberOfDigits = numberOfDigits(startNumber);
                    startNumber++;
                }
                
                listOfGenerated.add(generated);
                
            } else {
                // range is not in the format number-number -> abort
                result.add(pattern);
                return result;
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
        return allPosibilities(listOfGeneratorsAndValues);
    }
    
    /**
     * Method counts number of digits in the given integer.
     * @param number
     * @return count of digits
     */
    private static int numberOfDigits(int number) {
        if (number == 0) {
            return 1;
        }
        
        int digits = 0;
        while (number != 0) {
            digits++;
            number = number / 10;
        }
        return digits;
    }
    
    /**
     * Method generates all posibilities of joining of strings.
     * Example: input: [[a,b],[c,d]], output: [ac,ad,bc,bd] 
     * @param lists list of lists, which will be joined
     * @return all joined strings
     */
    private static List<String> allPosibilities(List<List<String>> lists) {
        if (lists.size() == 0) {
            // this should not happen
            return new ArrayList<>();
        }
        if (lists.size() == 1) {
            return lists.get(0);
        }
        List<String> result = new ArrayList<String>();
        
        List<String> list = lists.remove(0);
        // get recursively all posibilities without first list
        List<String> posibilities = allPosibilities(lists);
        
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
}
