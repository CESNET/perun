package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichUserExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.DiacriticNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidDestinationException;
import cz.metacentrum.perun.core.api.exceptions.InvalidGroupNameException;
import cz.metacentrum.perun.core.api.exceptions.InvalidHostnameException;
import cz.metacentrum.perun.core.api.exceptions.MaxSizeExceededException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MinSizeExceededException;
import cz.metacentrum.perun.core.api.exceptions.NumberNotInRangeException;
import cz.metacentrum.perun.core.api.exceptions.NumbersNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.ParseUserNameException;
import cz.metacentrum.perun.core.api.exceptions.ParserException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.SpaceNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.SpecialCharsNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static cz.metacentrum.perun.core.impl.PerunAppsConfig.getBrandContainingDomain;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.ALERT_TIME_FORMATTER;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.DEFAULT_IDENTITY_ADDED_PREF_MAIL;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.DEFAULT_IDENTITY_ADDED_PREF_MAIL_SUBJECT;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.DEFAULT_IDENTITY_ADDED_UES_MAIL;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.DEFAULT_IDENTITY_ADDED_UES_MAIL_SUBJECT;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.DEFAULT_IDENTITY_REMOVED_PREF_MAIL;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.DEFAULT_IDENTITY_REMOVED_PREF_MAIL_SUBJECT;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.DEFAULT_IDENTITY_REMOVED_UES_MAIL;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.DEFAULT_IDENTITY_REMOVED_UES_MAIL_SUBJECT;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.KEY_EN;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.LOGIN_PLACEHOLDER;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.ORG_PLACEHOLDER;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.ORG_UNKNOWN_TEXT;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.TIME_PLACEHOLDER;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.UES_ADDED_PREFERRED_MAIL;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.UES_ADDED_PREFERRED_MAIL_SUBJECT;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.UES_ADDED_UES_MAIL;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.UES_ADDED_UES_MAIL_SUBJECT;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.UES_REMOVED_PREF_MAIL;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.UES_REMOVED_PREF_MAIL_SUBJECT;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.UES_REMOVED_UES_MAIL;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_identityAlertsTemplates.UES_REMOVED_UES_MAIL_SUBJECT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
	public static final Pattern ucoEmailPattern = Pattern.compile("^[0-9]+@muni\\.cz$");

	private static final Pattern titleBeforePattern = Pattern.compile("^(([\\p{L}]+[.])|(et))$");
	private static final Pattern firstNamePattern = Pattern.compile("^[\\p{L}-']+$");
	private static final Pattern lastNamePattern = Pattern.compile("^(([\\p{L}-']+)|([\\p{L}][.]))$");

	public static final Pattern hostPattern = Pattern.compile("^(?!:\\/\\/)(?=.{1,255}$)((.{1,63}\\.){1,127}(?![0-9]*$)[a-z0-9-]+\\.?)$|^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
	public static final Pattern urlPattern = Pattern.compile("[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");
	public static final Pattern userAtHostPattern = Pattern.compile("^[a-z_]([a-z0-9_-]{0,31}|[a-z0-9_-]{0,30}\\$)@(?:(?!:\\/\\/)(?=.{1,255}$)((.{1,63}\\.){1,127}(?![0-9]*$)[a-z0-9-]+\\.?)$|(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$)");
	public static final Pattern userAtHostPortPattern = Pattern.compile("^[a-z_]([a-z0-9_-]{0,31}|[a-z0-9_-]{0,30}\\$)@(?:(?!:\\/\\/)(?=.{1,255}$)((.{1,63}\\.){1,127}(?![0-9]*$)[a-z0-9-]+\\.?)|(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}):[0-9]+");
	public static final Pattern serviceSpecificPattern = Pattern.compile("^(?!-)[a-zA-Z0-9-_.:/]*$");

	private static final String userPhoneAttribute = "urn:perun:user:attribute-def:def:phone";
	private static final String memberPhoneAttribute = "urn:perun:member:attribute-def:def:phone";

	private static final String A_U_MAIL = AttributesManager.NS_USER_ATTR_DEF + ":preferredMail";
	private static final String A_UES_MAIL = AttributesManager.NS_UES_ATTR_DEF + ":mail";
	private static final String A_E_IDENTITY_ALERTS = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":identityAlertsTemplates";
	private static final String A_UES_ORGANIZATION = AttributesManager.NS_UES_ATTR_DEF + ":o";

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
		Set<T> set = new HashSet<>(all.size());
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
	 * Returns additionalUserExtSources from the subject. It's used for synchronization from different ExtSources. subjectFromExtSource was obtained from the ExtSource.
	 * This additional userExtSource has 3 required parts: name of extSource, type of extSource, login of extsource with optional ues attributes and their values.
	 * And 1 optional part: LoA.
	 * Expected format of additional userExtSource is: extSourceName|extSourceType|extLogin;uesAttribute1=value1,value2;uesAttribute2=value1|LoA
	 *
	 * @param sess perun session
	 * @param subjectFromExtSource map with the subject
	 * @return List<RichUserExtSource> all additional ExtSources (and possibly their attributes) from the subject, returned list will never contain null value
	 */
	public static List<RichUserExtSource> extractAdditionalUserExtSources(PerunSession sess, Map<String, String> subjectFromExtSource) {
		List<RichUserExtSource> additionalUserExtSources = new ArrayList<>();
		for (String attrName : subjectFromExtSource.keySet()) {
			if(attrName != null &&
				subjectFromExtSource.get(attrName) != null &&
				attrName.startsWith(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING)) {
				String login = subjectFromExtSource.get("login");

				// Entry contains extSourceName|extSourceType|extLogin;uesAttribute=value1,value2[|LoA]
				String[] userExtSourceRaw =  subjectFromExtSource.get(attrName).split("\\|");
				log.debug("Processing additionalUserExtSource {}",  subjectFromExtSource.get(attrName));

				// Check if the array has at least 3 parts, this is protection against outOfBoundException
				if(userExtSourceRaw.length < 3) {
					throw new InternalErrorException("There is a missing mandatory part of additional user extSource value when processing it - '" + attrName + "'");
				}

				try {
					// Get additional ues
					UserExtSource additionalUserExtSource = parseAdditionalUserExtSource(sess, userExtSourceRaw);

					// Get ues attributes
					List<Attribute> uesAttributes = parseUESAttributes(sess, login, userExtSourceRaw[2]);

					// Create richUserExtSource from ues and its attributes
					RichUserExtSource richUserExtSource = new RichUserExtSource(additionalUserExtSource, uesAttributes);

					// Add additional rich user extSource
					additionalUserExtSources.add(richUserExtSource);
				} catch (ParserException e) {
					log.error("User with login {} has invalid additional userExtSource defined {}.", login, userExtSourceRaw);
				}
			}
		}
		return additionalUserExtSources;
	}

	/**
	 * Returns loa of addtional ues, if not stated, returns 0. If integer cannot be parsed from input, ParserException is thrown.
	 * Used in extractAdditionalUserExtSources to get ues LoA.
	 *
	 * @param login login of subject
	 * @param userExtSourceRaw array containing LoA
	 * @return int LoA
	 */
	private static int parseAdditionalUESLoa(String[] userExtSourceRaw) {
		int additionalExtLoa = 0;
		// Loa is not mandatory argument
		if (userExtSourceRaw.length>3 && userExtSourceRaw[3] != null) {
			try {
				additionalExtLoa = Integer.parseInt(userExtSourceRaw[3]);
			} catch (NumberFormatException e) {
				throw new ParserException("Subject has wrong LoA '" + userExtSourceRaw[3] + "'.", e, "LoA");
			}
		}
		return additionalExtLoa;
	}

	/**
	 * Returns additional user ext source either found in Perun or creates new. Parameter userExtSourceRaw is array of
	 * Strings containing name, type and extLogin. If any of the required parts is empty, ParserException is thrown.
	 * Used in extractAdditionalUserExtSources to get ues.
	 *
	 * @param sess perun session
	 * @param userExtSourceRaw array of strings containing all parts of ues
	 * @return UserExtSource additional ues
	 */
	private static UserExtSource parseAdditionalUserExtSource(PerunSession sess, String[] userExtSourceRaw) {
		// Get extLogin from 3rd part of userExtSourceRaw as well as ues attributes, so it needs to be parsed from it
		String extLogin = userExtSourceRaw[2].split(";")[0];

		// Check whether any of the required parts of ues are not empty
		if (userExtSourceRaw[0].isEmpty() || userExtSourceRaw[1].isEmpty() || extLogin.isEmpty()) {
			throw new ParserException("Some of the required parts of userExtSource are empty.");
		}

		ExtSource additionalExtSource;
		try {
			// Try to get extSource, with full extSource object (containg ID)
			additionalExtSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, userExtSourceRaw[0]);
		} catch (ExtSourceNotExistsException e) {
			try {
				// Create new one if not exists
				additionalExtSource = new ExtSource(userExtSourceRaw[0], userExtSourceRaw[1]);
				additionalExtSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().createExtSource(sess, additionalExtSource, null);
			} catch (ExtSourceExistsException e1) {
				throw new ConsistencyErrorException("Creating existing extSource: " + userExtSourceRaw[0]);
			}
		}

		// Get optional LoA (0 if not stated)
		int loa = parseAdditionalUESLoa(userExtSourceRaw);

		return new UserExtSource(additionalExtSource, loa, extLogin);
	}

	/**
	 * Parses attributes of additional ues from String which is in format: extLogin;uesAttributeName1=value1,value2;uesAttributeName2=value3.
	 * These attributes are returned with their values. If value is missing or attribute is not found, that part is skipped and
	 * message is logged.
	 * Used in extractAdditionalUserExtSources to get ues attributes.
	 *
	 * @param sess perun session
	 * @param login login of subject
	 * @param additionalExtLoginAndAttribute array containing extLogin and uesAttributes
	 * @return List<Attribute> attributes of additional ues
	 */
	private static List<Attribute> parseUESAttributes(PerunSession sess, String login, String additionalExtLoginAndAttribute) {
		List<Attribute> attributes = new ArrayList<>();

		String[] loginAndAttributes = additionalExtLoginAndAttribute.split(";");

		// Check wheter there are any ues attributes
		if (loginAndAttributes.length > 1) {
			List<String> uesAttributesWithValues =  new LinkedList<>();
			// Get all attributes without extLogin into a list
			uesAttributesWithValues.addAll(Arrays.asList(loginAndAttributes));
			uesAttributesWithValues.remove(0);

			for (String uesAttributeWithValue : uesAttributesWithValues) {
				String[] uesAttribute = uesAttributeWithValue.split("=");

				// Check that the attribute has both required parts (name and value)
				if (uesAttribute.length != 2) {
					log.error("User with login {} has invalid attribute for userExtSource defined as {}.", login, uesAttribute);
					continue;
				}

				try {
					// Create new attribute and add it to be returned
					Attribute attribute = new Attribute(sess.getPerun().getAttributesManager().getAttributeDefinition(sess, uesAttribute[0]), parseAttributeValuesDuringUESExtraction(uesAttribute[1]));
					attributes.add(attribute);
				} catch (AttributeNotExistsException e) {
					log.error("User with login {} has invalid attribute for userExtSource defined as {}.", login, uesAttribute);
				}
			}
		}
		return attributes;
	}

	/**
	 * Splits attribute value by "," and returns list of string values or single string if no "," occurres.
	 * Used in extractAdditionalUserExtSources to get ues attribute values.
	 *
	 * @param attributeValue unsplitted attribute value
	 * @return splitted attribute value to list of string or single string
	 */
	private static Object parseAttributeValuesDuringUESExtraction(String attributeValue) {
		String[] separatedValue = attributeValue.split(",");
		if (separatedValue.length == 1) {
			return attributeValue;
		} else {
			return Arrays.asList(separatedValue);
		}
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
	public static final RowMapper<Integer> ID_MAPPER = (resultSet, i) -> resultSet.getInt("id");

	/**
	 * String row mapper
	 */
	public static final RowMapper<String> STRING_MAPPER = (resultSet, i) -> resultSet.getString("value");

	// FIXME prijde odstranit
	public static void checkPerunSession(PerunSession sess) {
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
	public static void notNull(Object e, String name) {
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
	public static void checkMinLength(String propertyName, String actualValue, int minLength) {
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
	public static void checkMaxLength(String propertyName, String actualValue, int maxLength) {
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
	public static void checkWithoutDiacritic(String name) {

		if(!Normalizer.isNormalized(name, Form.NFKD))throw new DiacriticNotAllowedException("Name of the entity is not in the normalized form NFKD (diacritic not allowed)!");

	}

	/**
	 * Define, if some entity contain a special symbol
	 * Special symbol is everything except - numbers, letters and space
	 *
	 * @param name name of entity
	 * @throws SpecialCharsNotAllowedException
	 */
	public static void checkWithoutSpecialChars(String name) {

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
	public static void checkWithoutSpecialChars(String name, String allowedSpecialChars) {

		if(!name.matches("^([0-9 \\p{L}" + allowedSpecialChars + "])*$")) throw new SpecialCharsNotAllowedException("The special chars (except " + allowedSpecialChars + ") in the name of entity are not allowed!");
	}

	/**
	 * Define, if some entity contain a number
	 *
	 * @param name
	 * @throws NumbersNotAllowedException
	 */
	public static void checkWithoutNumbers(String name) {

		if(!name.matches("^([^0-9])*$")) throw new NumbersNotAllowedException("The numbers in the name of entity are not allowed!");
	}

	/**
	 * Define, if some entity contain a space
	 *
	 * @param name
	 * @throws SpaceNotAllowedException
	 */
	public static void checkWithoutSpaces(String name) {

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
	public static void checkRangeOfNumbers(int number, int lowestValue, int highestValue) {

		if(number<lowestValue || number>highestValue) throw new NumberNotInRangeException("Number is not in range, Lowest="+lowestValue+" < Number="+number+" < Highest="+highestValue);
	}

	/**
	 * Gets the next number from the sequence.
	 *
	 * @param jdbc
	 * @param sequenceName
	 * @return new ID
	 * @throws InternalErrorException
	 */
	public static int getNewId(JdbcTemplate jdbc, String sequenceName) {

		String query = "select nextval('" + sequenceName + "')";

		try {
			Integer i = jdbc.queryForObject(query, Integer.class);
			if (i == null) {
				throw new InternalErrorException("New ID should not be null.");
			}
			return i;
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
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	private static String limit(String s,int limit) {
		if(s==null) return null;
		return s.length() > limit ? s.substring(0, limit) : s;
	}

	public static User createUserFromNameMap(Map<String, String> name, boolean allowEmptyFirstName) {
		User user = new User();
		if (name.get(LAST_NAME) == null || name.get(LAST_NAME).isEmpty()) {
			throw new InternalErrorException("Last name is either empty or null when creating user");
		}
		if (!allowEmptyFirstName && (name.get(FIRST_NAME) == null || name.get(FIRST_NAME).isEmpty())) {
			throw new InternalErrorException("First name is either empty or null when creating user");
		}
		user.setTitleBefore(limit(name.get(TITLE_BEFORE),40));
		if (name.get(FIRST_NAME) != null) {
			user.setFirstName(limit(name.get(FIRST_NAME),64));
		}
		user.setLastName(limit(name.get(LAST_NAME),64));
		user.setTitleAfter(limit(name.get(TITLE_AFTER),40));
		return user;
	}

	public static User createUserFromNameMap(Map<String, String> name) {
		return createUserFromNameMap(name, false);
	}

	/**
	 * Creates a new instance of User with names initialized from parsed rawName.
	 * Imposes limit on leghts of fields.
	 * @see #parseCommonName(String)
	 * @param rawName raw name
	 * @param fullNameRequired if true, throw exception if firstName or lastName is missing, do not throw exception otherwise
	 * @return user
	 */
	public static User parseUserFromCommonName(String rawName, boolean fullNameRequired) {
		Map<String, String> m = parseCommonName(rawName, fullNameRequired);
		return createUserFromNameMap(m);
	}

	/**
	 * @see Utils.parseCommonName(String rawName, boolean fullNameRequired) - where fullNameRequired is false
	 */
	public static Map<String, String> parseCommonName(String rawName) {
		try {
			return Utils.parseCommonName(rawName, false);
		} catch (ParseUserNameException ex) {
			throw new InternalErrorException("Unexpected behavior while parsing user name without full name requirement.");
		}
	}

	/**
	 * Try to parse rawName to keys: "titleBefore" "firstName" "lastName" "titleAfter"
	 *
	 * If rawName is null or empty, return map with empty values of all keys.
	 *
	 * Parsing procedure:
	 * 1] prepare list of parts by replacing all characters "," and "_" by spaces
	 * 2] change all sequence of invisible characters (space, tabulator etc.) to one space
	 * 3] one by one try to parsing parts from the list
	 *  - A] try to find all titleBefore parts
	 *  - B] try to find one firstName part
	 *  - C] try to find all lastName parts
	 *  - D] if the rest is not lastName so save it to the title after
	 *
	 * Example of parsing rawName:
	 * 1] rawName = "Mgr. et Mgr.    Petr_Jiri R. Sojka, Ph.D., CSc."
	 * 2] convert all ',' and '_' to spaces: rawName = "Mgr. et Mgr.    Petr Jiri R. Sojka  Ph.D.  CSc."
	 * 3] convert more than 1 invisible char to 1 space: rawName = "Mgr. et Mgr. Petr Jiri R. Sojka Ph.D. CSc."
	 * 4] parse string to list of parts by space: ListOfParts= ["Mgr.","et","Mgr.","Petr","Jiri","R.","Sojka","Ph.D.","CSc."]
	 * 5] first fill everything what can be in title before: titleBefore="Mgr. et Mgr."
	 * 6] then fill everything what can be in first name (maximum 1 part): firstName="Petr"
	 * 7] then fill everything what can be in last name: lastName="Jiri R. Sojka"
	 * 8] everything else put to the title after: titleAfter="Ph.D. CSc."
	 * 9] put these variables to map like key=value, for ex.: Map[titleBefore="Mgr. et Mgr.",firstName="Petr", ... ] and return this map
	 *
	 * @param rawName name to parse
	 * @param fullNameRequired if true, throw exception if firstName or lastName is missing, do not throw exception otherwise
	 * @return map string to string where are 4 keys (titleBefore,titleAfter,firstName and lastName) with their values (value can be null)
	 * @throws ParseUserNameException when method was unable to parse both first name and last name from the rawName
	 */
	public static Map<String, String> parseCommonName(String rawName, boolean fullNameRequired) {
		// prepare variables and result map
		Map<String, String> parsedName = new HashMap<>();
		String titleBefore = "";
		String firstName = "";
		String lastName = "";
		String titleAfter = "";

		if (rawName != null && !rawName.isEmpty()) {
			// replace all ',' and '_' characters for ' ' for rawName
			rawName = rawName.replaceAll("[,_]", " ");
			// replace all invisible chars in row for ' '
			rawName = rawName.replaceAll("\\s+", " ").trim();

			// split parts by space
			List<String> nameParts = new ArrayList<>(Arrays.asList(rawName.split(" ")));

			// if length of nameParts is 1, save it to the lastName
			if(nameParts.size() == 1) {
				lastName = nameParts.get(0);
				// if length of nameParts is more than 1, try to choose which part belong to which value
			} else {
				// join title before name to single string with ' ' as delimiter
				titleBefore = parsePartOfName(nameParts, new StringJoiner(" "), titleBeforePattern);

				// get first name as a next name part if pattern matches and nameParts are not empty
				if (!nameParts.isEmpty()) firstName = parsePartOfName(nameParts, new StringJoiner(" "), firstNamePattern);

				// join last names to single string with ' ' as delimiter
				if (!nameParts.isEmpty()) lastName = parsePartOfName(nameParts, new StringJoiner(" "), lastNamePattern);

				// if any nameParts are left join them to one string with ' ' as delimiter and assume they are titles after name
				if (!nameParts.isEmpty()) {
					StringJoiner titleAfterBuilder = new StringJoiner(" ");
					for (String namePart : nameParts) {
						titleAfterBuilder.add(namePart);
					}
					titleAfter = titleAfterBuilder.toString();
				}
			}
		}

		// add variables to map, empty string means null
		if (titleBefore.isEmpty()) titleBefore = null;
		parsedName.put(TITLE_BEFORE, titleBefore);
		if (firstName.isEmpty()) firstName = null;
		parsedName.put(FIRST_NAME, firstName);
		if (lastName.isEmpty()) lastName = null;
		parsedName.put(LAST_NAME, lastName);
		if (titleAfter.isEmpty()) titleAfter = null;
		parsedName.put(TITLE_AFTER, titleAfter);

		if(fullNameRequired) {
			if (parsedName.get(FIRST_NAME) == null)
				throw new ParseUserNameException("Unable to parse first name from text.", rawName);
			if (parsedName.get(LAST_NAME) == null)
				throw new ParseUserNameException("Unable to parse last name from text.", rawName);
		}

		return parsedName;
	}

	private static String parsePartOfName(List<String> nameParts, StringJoiner result, Pattern pattern) {
		Matcher matcher = pattern.matcher(nameParts.get(0));

		// if the matcher does not match continue to the next part of the name
		if (!matcher.matches()) return result.toString();

		result.add(nameParts.get(0));
		nameParts.remove(0);
		// when nameParts are depleted or firstName was found there is no reason to continue the recursion
		if (nameParts.isEmpty() || pattern.equals(firstNamePattern)) return result.toString();

		// continue the recursion to find the next part
		return parsePartOfName(nameParts, result, pattern);
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
		List<Class<?>> classes = new ArrayList<>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					assert !file.getName().contains(".");
					classes.addAll(findClasses(file, packageName + "." + file.getName()));
				} else if (file.getName().endsWith(".class")) {
					classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
				}
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
		Map<String, String> attrNew = new HashMap<>(map);
		Set<String> keys = new HashSet<>(attrNew.keySet());
		for(String s: keys) {
			attrNew.put("<" + BeansUtils.createEscaping(s) + ">", "<" + BeansUtils.createEscaping(attrNew.get(s)) + ">");
			attrNew.remove(s);
		}
		return attrNew.toString();
	}

	public static Attribute copyAttributeToViAttributeWithoutValue(Attribute copyFrom, Attribute copyTo) {
		copyTo.setValueCreatedAt(copyFrom.getValueCreatedAt());
		copyTo.setValueCreatedBy(copyFrom.getValueCreatedBy());
		copyTo.setValueModifiedAt(copyFrom.getValueModifiedAt());
		copyTo.setValueModifiedBy(copyFrom.getValueModifiedBy());
		return copyTo;
	}

	public static Attribute copyAttributeToVirtualAttributeWithValue(Attribute copyFrom, Attribute copyTo) {
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
		List<String> result = new ArrayList<>();

		// get chars between the brackets
		List<String> values = new ArrayList<>(Arrays.asList(pattern.split("\\[[^]]*]")));
		// get content of the brackets
		List<String> generators = new ArrayList<>();
		Pattern generatorPattern = Pattern.compile("\\[([^]]*)]");
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
		List<List<String>> listOfGenerated = new ArrayList<>();

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
				List<String> generated = new ArrayList<>();
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
		List<List<String>> listOfGeneratorsAndValues = new ArrayList<>();
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
		List<String> result = new ArrayList<>();

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
			mac.init(new SecretKeySpec(BeansUtils.getCoreConfig().getMailchangeSecretKey().getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
			byte[] macbytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
			return new BigInteger(macbytes).toString(Character.MAX_RADIX);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Send validation email related to requested change of users preferred email.
	 * @param user user to change preferred email for
	 * @param url base URL of running perun instance passed from RPC
	 * @param email new email address to send notification to
	 * @param changeUuid UUID of change request in DB
	 * @param subject Template subject or null
	 * @param content Template message or null
	 * @param customUrlPath custom path to be used in generated URL
	 * @param idp Authentication to be added as query parameter in generated URL
	 */
	public static void sendValidationEmail(User user, String url, String email, UUID changeUuid, String subject, String content, String customUrlPath, String idp) {
		String instanceName = BeansUtils.getCoreConfig().getInstanceName();

		// use default if unknown rpc path
		String linkLocation = "/gui/";
		try {
			URL urlObject = new URL(url);
			String domain = urlObject.getProtocol()+"://"+urlObject.getHost();
			PerunAppsConfig.Brand brand = getBrandContainingDomain(domain);

			// no brand contains specified domain
			if (brand == null) {
				throw new InternalErrorException("Not valid URL of running Perun instance: " + url + ".");
			}

			//if there is custom path, use it, if not, try to use specific path
			if (customUrlPath != null && !customUrlPath.isEmpty()) {
				linkLocation = customUrlPath;
			} else if (!brand.getOldGuiDomain().equals(domain)) { //did not come from old GUI, do not add anything
				linkLocation = "";
			} else if (urlObject.getPath().contains("/krb/")) {
				linkLocation = "/krb/gui/";
			} else if (urlObject.getPath().contains("/fed/")) {
				linkLocation = "/fed/gui/";
			} else if (urlObject.getPath().contains("/ceitec/")) {
				// to support ceitec proxy, since it gets more fed attributes than IDM SP.
				linkLocation = "/ceitec/gui/";
			} else if (urlObject.getPath().contains("/cert/")) {
				linkLocation = "/cert/gui/";
			}
		} catch (MalformedURLException ex) {
			throw new InternalErrorException("Not valid URL of running Perun instance.", ex);
		}
		String validationLink = prepareValidationLinkForEmailChange(url, linkLocation, changeUuid, user, idp);

		String defaultSubject = "["+instanceName+"] New email address verification";
		String defaultBody = "Dear "+user.getDisplayName()+",\n\nWe've received request to change your preferred email address to: "+email+"."+
			"\n\nTo confirm this change please use link below:\n\n"+validationLink+"\n\n" +
			"Message is automatically generated." +
			"\n----------------------------------------------------------------" +
			"\nPerun - Identity & Access Management System";

		Map<String, String> subjectParametersToReplace = new HashMap<>();
		subjectParametersToReplace.put("{instanceName}", instanceName);
		subject = prepareSubjectOfEmail(subject, defaultSubject, subjectParametersToReplace);

		Map<String, String> bodyParametersToReplace = new HashMap<>();
		bodyParametersToReplace.put("{link}", validationLink);
		content = prepareBodyOfEmail(content, defaultBody, bodyParametersToReplace);

		sendEmail(subject, content, email);
	}

	/**
	 * Sends email with link to non-authz account activation where user can activate his account by setting a password.
	 *  @param user user to send notification for
	 * @param email user's email to send notification to
	 * @param namespace namespace to reset password in
	 * @param url base URL of Perun instance
	 * @param uuid UUID of account activation request
	 * @param messageTemplate message of the email (use default if null)
	 * @param subject subject of the email (use default if null)
	 * @param validityTo time till link is valid
	 */
	public static void sendAccountActivationEmail(User user, String email, String namespace, String url, UUID uuid, String messageTemplate, String subject, LocalDateTime validityTo) {
		String instanceName = BeansUtils.getCoreConfig().getInstanceName();

		String validationLink = prepareValidationLinkForPasswordResetAndAccountActivation(url, "/non/pwd-reset/", uuid, user, namespace, true);
		String validityToString = prepareValidityTo(validityTo);

		String defaultSubject = "[" + instanceName + "] Account activation in namespace: " + namespace;
		String defaultBody = "Dear " + user.getDisplayName() + ",\n\nWe've received request to activate your account in namespace \"" + namespace + "\"." +
			"\n\nPlease visit the link below, where you can activate your account:\n\n" + validationLink + "\n\n" +
			"Link is valid till " + validityToString + "\n\n" +
			"Message is automatically generated." +
			"\n----------------------------------------------------------------" +
			"\nPerun - Identity & Access Management System";

		Map<String, String> subjectParametersToReplace = new HashMap<>();
		subjectParametersToReplace.put("{instanceName}", instanceName);
		subjectParametersToReplace.put("{namespace}", namespace);
		subject = prepareSubjectOfEmail(subject, defaultSubject, subjectParametersToReplace);

		Map<String, String> bodyParametersToReplace = new HashMap<>();
		bodyParametersToReplace.put("{displayName}", user.getDisplayName());
		bodyParametersToReplace.put("{namespace}", namespace);
		bodyParametersToReplace.put("{validity}", validityToString);
		// allow enforcing per-language links
		if (messageTemplate != null && messageTemplate.contains("{link-")) {
			Pattern pattern = Pattern.compile("\\{link-[^}]+}");
			Matcher matcher = pattern.matcher(messageTemplate);
			while (matcher.find()) {
				// whole "{link-something}"
				String toSubstitute = matcher.group(0);
				String langLink = validationLink;

				Pattern namespacePattern = Pattern.compile("-(.*?)}");
				Matcher m2 = namespacePattern.matcher(toSubstitute);
				if (m2.find()) {
					// only language "cs", "en",...
					String lang = m2.group(1);
					langLink = langLink + "&locale=" + lang;
				}
				bodyParametersToReplace.put(toSubstitute, langLink);
			}
		} else {
			bodyParametersToReplace.put("{link}", validationLink);
		}
		messageTemplate = prepareBodyOfEmail(messageTemplate, defaultBody, bodyParametersToReplace);

		sendEmail(subject, messageTemplate, email);
	}

	/**
	 * Sends email with link to non-authz password reset GUI where user
	 * can reset forgotten password
	 *
	 * @param user user to send notification for
	 * @param email user's email to send notification to
	 * @param namespace namespace to reset password in
	 * @param url base URL of Perun instance
	 * @param uuid UUID of pwd reset request
	 * @param messageTemplate message of the email
	 * @param subject subject of the email
	 * @param validityTo time till link is valid
	 * @throws InternalErrorException
	 */
	public static void sendPasswordResetEmail(User user, String email, String namespace, String url, UUID uuid, String messageTemplate, String subject, LocalDateTime validityTo) {
		String instanceName = BeansUtils.getCoreConfig().getInstanceName();

		String validationLink = prepareValidationLinkForPasswordResetAndAccountActivation(url, "/non/pwd-reset/", uuid, user, namespace, false);
		String validityToString = prepareValidityTo(validityTo);

		String defaultSubject = "[" + instanceName + "] Password reset in namespace: " + namespace;
		String defaultBody = "Dear " + user.getDisplayName() + ",\n\nWe've received request to reset your password in namespace \"" + namespace + "\"." +
			"\n\nPlease visit the link below, where you can set new password:\n\n" + validationLink + "\n\n" +
			"Link is valid till " + validityToString + "\n\n" +
			"Message is automatically generated." +
			"\n----------------------------------------------------------------" +
			"\nPerun - Identity & Access Management System";

		Map<String, String> subjectParametersToReplace = new HashMap<>();
		subjectParametersToReplace.put("{instanceName}", instanceName);
		subjectParametersToReplace.put("{namespace}", namespace);
		subject = prepareSubjectOfEmail(subject, defaultSubject, subjectParametersToReplace);

		Map<String, String> bodyParametersToReplace = new HashMap<>();
		bodyParametersToReplace.put("{displayName}", user.getDisplayName());
		bodyParametersToReplace.put("{namespace}", namespace);
		bodyParametersToReplace.put("{validity}", validityToString);
		// allow enforcing per-language links
		if (messageTemplate != null && messageTemplate.contains("{link-")) {
			Pattern pattern = Pattern.compile("\\{link-[^}]+}");
			Matcher matcher = pattern.matcher(messageTemplate);
			while (matcher.find()) {
				// whole "{link-something}"
				String toSubstitute = matcher.group(0);
				String langLink = validationLink;

				Pattern namespacePattern = Pattern.compile("-(.*?)}");
				Matcher m2 = namespacePattern.matcher(toSubstitute);
				if (m2.find()) {
					// only language "cs", "en",...
					String lang = m2.group(1);
					langLink = langLink + "&locale=" + lang;
				}
				bodyParametersToReplace.put(toSubstitute, langLink);
			}
		} else {
			bodyParametersToReplace.put("{link}", validationLink);
		}
		messageTemplate = prepareBodyOfEmail(messageTemplate, defaultBody, bodyParametersToReplace);

		sendEmail(subject, messageTemplate, email);
	}

	/**
	 * Sends email to user confirming his password was set and the account was activated.
	 *
	 * @param user user to send notification for
	 * @param email user's email to send notification to
	 * @param namespace namespace where the account was activated
	 * @param login login of user
	 * @param subject Subject from template or null
	 * @param content Message from template or null
	 */
	public static void sendAccountActivationConfirmationEmail(User user, String email, String namespace, String login, String subject, String content) {
		String instanceName = BeansUtils.getCoreConfig().getInstanceName();

		String defaultSubject = "["+instanceName+"] Account activation in namespace: "+namespace;
		String defaultText = "Dear "+user.getDisplayName()+",\n\nyour account in namespace \""+namespace+"\" was successfully activated."+
			"\n\nThis message is automatically sent to all your email addresses registered in "+instanceName+" in order to prevent malicious account activation without your knowledge.\n\n" +
			"If you didn't request / perform account activation, please notify your administrators and support at "+BeansUtils.getCoreConfig().getMailchangeBackupFrom()+" to resolve this security issue.\n\n" +
			"Message is automatically generated." +
			"\n----------------------------------------------------------------" +
			"\nPerun - Identity & Access Management System";

		Map<String, String> subjectParametersToReplace = new HashMap<>();
		subjectParametersToReplace.put("{instanceName}", instanceName);
		subjectParametersToReplace.put("{namespace}", namespace);
		subject = prepareSubjectOfEmail(subject, defaultSubject, subjectParametersToReplace);

		Map<String, String> bodyParametersToReplace = new HashMap<>();
		bodyParametersToReplace.put("{displayName}", user.getDisplayName());
		bodyParametersToReplace.put("{namespace}", namespace);
		bodyParametersToReplace.put("{login}", login);
		bodyParametersToReplace.put("{instanceName}", instanceName);
		content = prepareBodyOfEmail(content, defaultText, bodyParametersToReplace);

		sendEmail(subject, content, email);
	}

	/**
	 * Sends email to user confirming his password was changed.
	 *
	 * @param user user to send notification for
	 * @param email user's email to send notification to
	 * @param namespace namespace the password was re-set
	 * @param login login of user
	 * @param subject Subject from template or null
	 * @param content Message from template or null
	 */
	public static void sendPasswordResetConfirmationEmail(User user, String email, String namespace, String login, String subject, String content) {
		String instanceName = BeansUtils.getCoreConfig().getInstanceName();

		String defaultSubject = "["+instanceName+"] Password reset in namespace: "+namespace;
		String defaultText = "Dear "+user.getDisplayName()+",\n\nyour password in namespace \""+namespace+"\" was successfully reset."+
			"\n\nThis message is automatically sent to all your email addresses registered in "+instanceName+" in order to prevent malicious password reset without your knowledge.\n\n" +
			"If you didn't request / perform password reset, please notify your administrators and support at "+BeansUtils.getCoreConfig().getMailchangeBackupFrom()+" to resolve this security issue.\n\n" +
			"Message is automatically generated." +
			"\n----------------------------------------------------------------" +
			"\nPerun - Identity & Access Management System";

		Map<String, String> subjectParametersToReplace = new HashMap<>();
		subjectParametersToReplace.put("{instanceName}", instanceName);
		subjectParametersToReplace.put("{namespace}", namespace);
		subject = prepareSubjectOfEmail(subject, defaultSubject, subjectParametersToReplace);

		Map<String, String> bodyParametersToReplace = new HashMap<>();
		bodyParametersToReplace.put("{displayName}", user.getDisplayName());
		bodyParametersToReplace.put("{namespace}", namespace);
		bodyParametersToReplace.put("{login}", login);
		bodyParametersToReplace.put("{instanceName}", instanceName);
		content = prepareBodyOfEmail(content, defaultText, bodyParametersToReplace);

		sendEmail(subject, content, email);
	}

	/**
	 * Prepare subject of email for password reset or account activation.
	 *
	 * If optionalSubject is defined, replace some variables in it and use it as is. If not, use the default
	 * subject instead.
	 *
	 * @param optionalSubject optional subject, can be null, if so use default subject instead
	 * @param defaultSubject default subject, need to be not null
	 * @param parametersToReplace keys are name of parameters and values are replacements for the parameter
	 *
	 * @return subject to be used for sending an email to user
	 */
	private static String prepareSubjectOfEmail(String optionalSubject, String defaultSubject, Map<String, String> parametersToReplace) {
		notNull(defaultSubject, "defaultSubject");

		//use optional subject if not empty
		if(optionalSubject != null && !optionalSubject.isEmpty()) {
			//replace all optional parameters
			if(parametersToReplace != null) {
				for(String parameterToReplace : parametersToReplace.keySet()) {
					optionalSubject = optionalSubject.replace(parameterToReplace, parametersToReplace.get(parameterToReplace));
				}
			}
			return optionalSubject;
		}

		return defaultSubject;
	}

	/**
	 * Prepare text of email for password reset or account activation.
	 *
	 * If optionalBody is defined, replace some variables in it and use it as is. If not, use the default
	 * subject instead.
	 *
	 * @param optionalBody optional body, can be null, if so use default subject instead
	 * @param defaultBody default body, need to be not null
	 * @param parametersToReplace keys are name of parameters and values are replacements for the parameter
	 *
	 * @return body to be used for sending an email to user
	 */
	private static String prepareBodyOfEmail(String optionalBody, String defaultBody, Map<String, String> parametersToReplace) {
		notNull(defaultBody, "defaultBody");

		//use optional body if not empty
		if(optionalBody != null && !optionalBody.isEmpty()) {
			//replace all optional parameters
			if(parametersToReplace != null) {
				for(String parameterToReplace : parametersToReplace.keySet()) {
					optionalBody = optionalBody.replace(parameterToReplace, parametersToReplace.get(parameterToReplace));
				}
			}
			return optionalBody;
		}

		return defaultBody;
	}

	/**
	 * Send message with specific body and subject to the email.
	 *
	 * @param subjectOfEmail specific subject of mail (need to be not null)
	 * @param bodyOfEmail specific text of body of mail (need to be not null)
	 * @param email email to send message to (need to be not null)
	 */
	private static void sendEmail(String subjectOfEmail, String bodyOfEmail, String email) {
		notNull(subjectOfEmail, "subject");
		notNull(bodyOfEmail, "body");
		notNull(email, "email");

		// create mail sender
		JavaMailSender mailSender = BeansUtils.getDefaultMailSender();

		// create message
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setFrom(BeansUtils.getCoreConfig().getMailchangeBackupFrom());

		// set subject and body
		message.setSubject(subjectOfEmail);
		message.setText(bodyOfEmail);

		// send email
		try {
			mailSender.send(message);
		} catch (MailException ex) {
			log.error("Unable to send email to '" + email + "'.", ex);
			throw new InternalErrorException("Unable to send email.", ex);
		}
	}

	/**
	 * Prepare validity time of validation link.
	 *
	 * @param validityTo
	 * @return validity as formatted string
	 */
	private static String prepareValidityTo(LocalDateTime validityTo) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return dtf.format(validityTo);
	}

	/**
	 * Prepare validation link for password reset or account activation.
	 *
	 * @param url base URL of Perun instance
	 * @param linkLocation location of validation link under specific Perun instance (for example '/non/pwd-reset/')
	 * @param uuid UUID of request
	 * @param user user to who link will be send
	 * @param namespace namespace to processing request for
	 * @param activation if result link should work as activation
	 *
	 * @return link of validation as String
	 */
	private static String prepareValidationLinkForPasswordResetAndAccountActivation(String url, String linkLocation, UUID uuid, User user, String namespace, boolean activation) {
		notNull(user, "user");
		notNull(url, "url");
		notNull(linkLocation, "linkLocation");

		StringBuilder link = new StringBuilder();

		try {
			URL urlObject = new URL(url);

			link.append(urlObject.getProtocol());
			link.append("://");
			link.append(urlObject.getHost());
			link.append(linkLocation);
			link.append("?token=");
			link.append(URLEncoder.encode(uuid.toString(), StandardCharsets.UTF_8));
			link.append("&login-namespace=");
			link.append(URLEncoder.encode(namespace, StandardCharsets.UTF_8));
			if(activation) {
				link.append("&activation=true");
			}
		} catch (MalformedURLException ex) {
			throw new InternalErrorException("Not valid URL of running Perun instance.", ex);
		}

		return link.toString();
	}

	/**
	 * Prepare validation link for email change
	 *
	 * @param url base URL of Perun instance
	 * @param linkLocation location of validation link under specific Perun instance (for example '/non/pwd-reset/')
	 * @param changeUuid UUID of request
	 * @param user user to who link will be send
	 * @param idp authentication method for query parameter
	 *
	 * @return link of validation as String
	 */
	private static String prepareValidationLinkForEmailChange(String url, String linkLocation, UUID changeUuid, User user, String idp) {
		notNull(user, "user");
		notNull(url, "url");
		notNull(linkLocation, "linkLocation");

		StringBuilder link = new StringBuilder();

		try {
			URL urlObject = new URL(url);

			link.append(urlObject.getProtocol());
			link.append("://");
			link.append(urlObject.getHost());
			link.append(linkLocation);
			link.append("?token=");
			link.append(URLEncoder.encode(changeUuid.toString(), StandardCharsets.UTF_8));
			link.append("&u=" + user.getId());
			if (isNotBlank(idp)) {
				link.append("&idpFilter=");
				link.append(URLEncoder.encode(idp, StandardCharsets.UTF_8));
			}
		} catch (MalformedURLException ex) {
			throw new InternalErrorException("Not valid URL of running Perun instance.", ex);
		}

		return link.toString();
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
	public static String cipherInput(String plainText, boolean decrypt) {

		try {

			String encryptionKey = BeansUtils.getCoreConfig().getPwdresetSecretKey();
			String initVector = BeansUtils.getCoreConfig().getPwdresetInitVector();

			Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			SecretKeySpec k = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
			c.init((decrypt) ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE, k, new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8)));

			if (decrypt) {

				byte[] bytes = Base64.decodeBase64(plainText.getBytes(StandardCharsets.UTF_8));
				return new String(c.doFinal(bytes), StandardCharsets.UTF_8);

			} else {

				byte[] bytes = Base64.encodeBase64(c.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
				return new String(bytes, StandardCharsets.UTF_8);

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
	public static void checkDestinationType(Destination destination) throws WrongPatternException  {
		if (destination == null) {
			throw new InternalErrorException("Destination is null.");
		}
		String destinationType = destination.getType();
		if ((!Objects.equals(destinationType, Destination.DESTINATIONHOSTTYPE)
				&& (!Objects.equals(destinationType, Destination.DESTINATIONEMAILTYPE))
				&& (!Objects.equals(destinationType, Destination.DESTINATIONSEMAILTYPE))
				&& (!Objects.equals(destinationType, Destination.DESTINATIONURLTYPE))
				&& (!Objects.equals(destinationType, Destination.DESTINATIONUSERHOSTTYPE))
				&& (!Objects.equals(destinationType, Destination.DESTINATIONUSERHOSTPORTTYPE))
				&& (!Objects.equals(destinationType, Destination.DESTINATIONSERVICESPECIFICTYPE))
				&& (!Objects.equals(destinationType, Destination.DESTINATIONWINDOWS))
				&& (!Objects.equals(destinationType, Destination.DESTINATIONWINDOWSPROXY)))) {
			throw new WrongPatternException("Destination type " + destinationType + " is not supported.");
		}
	}

	/**
	 * Checks whether the destinations name has correct syntax.
	 *
	 * @param destination destination to check
	 * @throws cz.metacentrum.perun.core.api.exceptions.InvalidDestinationException if destination has invalid value in given destination type.
	 */
	public static void checkDestination(Destination destination) throws InvalidDestinationException {
		if (destination == null) {
			throw new InternalErrorException("Destination is null.");
		}

		Matcher matcher = null;
		String destinationType = destination.getType();
		if (destinationType.equals(Destination.DESTINATIONHOSTTYPE) || destinationType.equals(Destination.DESTINATIONWINDOWSPROXY))
			matcher = hostPattern.matcher(destination.getDestination());
		if (destinationType.equals(Destination.DESTINATIONEMAILTYPE) || destinationType.equals(Destination.DESTINATIONSEMAILTYPE))
			matcher = emailPattern.matcher(destination.getDestination());
		if (destinationType.equals(Destination.DESTINATIONURLTYPE))
			matcher = urlPattern.matcher(destination.getDestination());
		if (destinationType.equals(Destination.DESTINATIONUSERHOSTTYPE) || destinationType.equals(Destination.DESTINATIONWINDOWS))
			matcher = userAtHostPattern.matcher(destination.getDestination());
		if (destinationType.equals(Destination.DESTINATIONUSERHOSTPORTTYPE))
			matcher = userAtHostPortPattern.matcher(destination.getDestination());
		if (destinationType.equals(Destination.DESTINATIONSERVICESPECIFICTYPE))
			matcher = serviceSpecificPattern.matcher(destination.getDestination());

		// it should not happen because destination type is checked earlier
		if (matcher == null)
			throw new InvalidDestinationException("Destination type " + destinationType + " is not supported.");

		if (!matcher.matches())
			throw new InvalidDestinationException("Wrong syntax of destination " + destination.getDestination());

	}

	/**
	 * Checks whether the hostname has correct syntax.
	 *
	 * @param host host to check
	 * @throws cz.metacentrum.perun.core.api.exceptions.InvalidHostnameException if host has invalid hostname
	 */
	public static void checkHostname(Host host) throws InvalidHostnameException {
		if (host == null) {
			throw new InternalErrorException("Host is null.");
		}

		Matcher matcher = hostPattern.matcher(host.getHostname());

		if (!matcher.matches())
			throw new InvalidHostnameException("Wrong syntax of hostname " + host.getHostname());
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
	public static void sendSMS(PerunSession sess, User user, String message) throws PrivilegeException, UserNotExistsException {
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
	public static void sendSMS(PerunSession sess, Member member, String message) throws PrivilegeException, MemberNotExistsException {
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
	public static void sendSMS(String telNumber, String message) {
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
	public static String bigDecimalBytesToReadableStringWithMetric(BigDecimal quota) {
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
	 * Extends given date by given period.
	 *
	 * @param localDate date to be extended
	 * @param period period used to extend date
	 * @throws InternalErrorException when the period has wrong format,
	 * allowed format is given by regex "\\+([0-9]+)([dmy]?)"
	 */
	public static LocalDate extendDateByPeriod(LocalDate localDate, String period) {
		// We will add days/months/years
		Pattern p = Pattern.compile("\\+([0-9]+)([dmy]?)");
		Matcher m = p.matcher(period);
		if (m.matches()) {
			String countString = m.group(1);
			int amount = Integer.valueOf(countString);

			String dmyString = m.group(2);
			switch (dmyString) {
				case "d":
					return localDate.plusDays(amount);
				case "m":
					return localDate.plusMonths(amount);
				case "y":
					return localDate.plusYears(amount);
				default:
					throw new InternalErrorException("Wrong format of period. Period: " + period);
			}
		} else {
			throw new InternalErrorException("Wrong format of period. Period: " + period);
		}
	}

	/**
	 * Shortens the given date by the given period.
	 *
	 * @param localDate date to be shortened
	 * @param period string of format ([0-9]+)([dmy])
	 * @return shortened date
	 */
	public static LocalDate shortenDateByPeriod(LocalDate localDate, String period) {
		Pattern p = Pattern.compile("([0-9]+)([dmy])");
		Matcher m = p.matcher(period);
		if (m.matches()) {
			String countString = m.group(1);
			int amount = Integer.parseInt(countString);

			String dmyString = m.group(2);
			switch (dmyString) {
				case "d":
					return localDate.minusDays(amount);
				case "m":
					return localDate.minusMonths(amount);
				case "y":
					return localDate.minusYears(amount);
				default:
					throw new InternalErrorException("Wrong format of period. Period: " + period);
			}
		} else {
			throw new InternalErrorException("Wrong format of period. Period: " + period);
		}
	}

	/**
	 * Returns closest future LocalDate based on values given by matcher.
	 * If returned value should fall to 29. 2. of non-leap year, the date is extended to 28. 2. instead.
	 *
	 * @param matcher matcher with day and month values
	 * @return Extended date.
	 */
	public static LocalDate getClosestExpirationFromStaticDate(Matcher matcher) {
		int day = Integer.parseInt(matcher.group(1));
		int month = Integer.parseInt(matcher.group(2));

		// We must detect if the extension date is in current year or in a next year (we use year 2000 in comparison because it is a leap year)
		LocalDate extensionDate = LocalDate.of(2000, month, day);

		// check if extension is next year
		// in case of static date being today's date, we want to extend to next year (that's why we use the negation later)
		boolean extensionInThisYear = LocalDate.of(2000, LocalDate.now().getMonth(), LocalDate.now().getDayOfMonth()).isBefore(extensionDate);

		// Get current year
		int year = LocalDate.now().getYear();
		if (!extensionInThisYear) {
			// Add year to get next year
			year++;
		}

		// Set the date to which the membership should be extended, can be changed if there was grace period, see next part of the code
		if (day == 29 && month == 2 && !LocalDate.of(year, 1,1).isLeapYear()) {
			// If extended date is 29. 2. of non-leap year, the date is set to 28. 2.
			extensionDate = LocalDate.of(year, 2, 28);
		} else {
			extensionDate = LocalDate.of(year, month, day);
		}

		return extensionDate;
	}

	/**
	 * Prepares grace period date by values from given matcher.
	 * @param matcher matcher
	 * @return pair of field(ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS) and amount
	 * @throws InternalErrorException when given matcher contains invalid data
	 * @throws IllegalArgumentException when matcher does not match gracePeriod format
	 */
	public static Pair<Integer, TemporalUnit> prepareGracePeriodDate(Matcher matcher) {
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Wrong format of gracePeriod.");
		}
		String countString = matcher.group(1);
		int amount = Integer.valueOf(countString);

		TemporalUnit field;
		String dmyString = matcher.group(2);
		switch (dmyString) {
			case "d":
				field = ChronoUnit.DAYS;
				break;
			case "m":
				field = ChronoUnit.MONTHS;
				break;
			case "y":
				field = ChronoUnit.YEARS;
				break;
			default:
				throw new InternalErrorException("Wrong format of gracePeriod.");
		}

		return new Pair<>(amount, field);
	}

	/**
	 * We need to escape some special characters for LDAP filtering.
	 * We need to escape these characters: '\\', '*', '(', ')', '\000'
	 *
	 * @param searchString search string which need to be escaped properly
	 * @return properly escaped search string
	 */
	public static String escapeStringForLDAP(String searchString) {
		if(searchString == null) return "";
		return searchString.replace("\\", "\\5C").replace("*", "\\2A").replace("(", "\\28").replace(")", "\\29").replace("\000", "\\00");
	}

	/**
	 */
	public static void validateFullGroupName(String name) throws InvalidGroupNameException {
		String primaryRegex = GroupsManager.GROUP_FULL_NAME_REGEXP;
		validateGroupName(name, primaryRegex);

		String secondaryRegex = BeansUtils.getCoreConfig().getGroupFullNameSecondaryRegex();
		if (secondaryRegex != null && !secondaryRegex.isEmpty()) {
			validateGroupName(name, secondaryRegex);
		}
	}


	/**
	 * Validates group name.
	 *
	 * To check the group name, this method uses two regexes. A default one, hardcoded in
	 * the GroupsManager, and a secondary optional. The secondary regex can be default as
	 * a core property named groupNameSecondaryRegex.
	 *
	 * @param name name to be validated
	 * @throws InvalidGroupNameException if the name is invalid
	 */
	public static void validateGroupName(String name) throws InvalidGroupNameException {
		String primaryRegex = GroupsManager.GROUP_SHORT_NAME_REGEXP;
		validateGroupName(name, primaryRegex);

		String secondaryRegex = BeansUtils.getCoreConfig().getGroupNameSecondaryRegex();
		if (secondaryRegex != null && !secondaryRegex.isEmpty()) {
			validateGroupName(name, secondaryRegex);
		}
	}

	/**
	 * Validates given group name against a given regex.
	 *
	 * @param name group name
	 * @param regex regex to be used
	 * @throws InvalidGroupNameException if the name is invalid
	 */
	public static void validateGroupName(String name, String regex) throws InvalidGroupNameException {
		try {
			if (!name.matches(regex)) {
				throw new InvalidGroupNameException("Wrong group name, group name must matches " + regex);
			}
		} catch (PatternSyntaxException e) {
			throw new InternalErrorException("Invalid group name regex defined: " + regex, e);
		}
	}

	/**
	 * Returns search query to search by user name (exact match) based on databased in use.
	 *
	 * @return search query
	 */
	public static String prepareUserSearchQueryExactMatch() {
		return " replace(lower("+Compatibility.convertToAscii("COALESCE(users.first_name,'') || COALESCE(users.middle_name,'') || COALESCE(users.last_name,'')")+"), ' ', '')=replace(lower(" + Compatibility.convertToAscii(":nameString") + "), ' ', '')";
	}

	/**
	 * Returns search query to search by user name (similar match) based on databased in use.
	 *
	 * @return search query
	 */
	public static String prepareUserSearchQuerySimilarMatch() {
		return " strpos(replace(lower(" + Compatibility.convertToAscii("COALESCE(users.first_name,'') || COALESCE(users.middle_name,'') || COALESCE(users.last_name,'')") + "), ' ', ''),replace(lower(" + Compatibility.convertToAscii(":nameString") + "), ' ', '')) > 0 or " +
			" strpos(replace(lower(" + Compatibility.convertToAscii("COALESCE(users.last_name,'') || COALESCE(users.first_name,'') || COALESCE(users.middle_name,'')") + "), ' ', ''),replace(lower(" + Compatibility.convertToAscii(":nameString") + "), ' ', '')) > 0 ";
	}

	/**
	 * Takes attributes from CoreConfig used in users or members search and divides them into correct categories based
	 * on attribute type (member, user, userExtSource).
	 *
	 * Then returns map: memberAttributes -> list of member attributes
	 *                   userAttributes -> list of user attributes
	 *                   uesAttributes -> list of userExtSource attributes
	 *
	 * @return map of attributes
	 */
	public static Map<String, List<String>> getDividedAttributes() {
		Map<String, List<String>> result = new HashMap<>();

		List<String> allAttributes = BeansUtils.getCoreConfig().getAttributesToSearchUsersAndMembersBy();
		List<String> memberAttributes = new ArrayList<>();
		List<String> userAttributes = new ArrayList<>();
		List<String> uesAttributes = new ArrayList<>();
		for (String attribute : allAttributes) {
			if (attribute.startsWith(AttributesManager.NS_MEMBER_ATTR)) {
				memberAttributes.add(attribute);
			} else if (attribute.startsWith(AttributesManager.NS_USER_ATTR)) {
				userAttributes.add(attribute);
			} else if (attribute.startsWith(AttributesManager.NS_UES_ATTR)) {
				uesAttributes.add(attribute);
			}
		}

		result.put("memberAttributes", memberAttributes);
		result.put("userAttributes", userAttributes);
		result.put("uesAttributes", uesAttributes);

		return result;
	}

	/**
	 * Prepares query to search users or members by attributes if received attribute list is not empty.
	 *
	 * Returns map: memberAttributesQueryString -> pair of parts of query to search by member attributes
	 * 	            userAttributesQueryString -> pair of parts of query to search by user attributes
	 * 	            uesAttributesQueryString -> pair of parts of query to search by userExtSource attributes
	 *
	 * @param memberAttributes member attributes
	 * @param userAttributes user attributes
	 * @param uesAttributes userExtSource attributes
	 *
	 * @return map of parts of query
	 */
	public static Map<String, Pair<String, String>> getAttributesQuery(List<String> memberAttributes, List<String> userAttributes, List<String> uesAttributes) {
		Map<String, Pair<String, String>> result = new HashMap<>();

		Pair<String, String> memberAttributesQueryString = new Pair<>("", "");
		if (!memberAttributes.isEmpty()) {
			memberAttributesQueryString.put(" left join member_attr_values mav on members.id=mav.member_id and mav.attr_id in (select id from attr_names where attr_name in (:memberAttributes))", " lower(mav.attr_value)=lower(:searchString) or " );
		}
		result.put("memberAttributesQuery", memberAttributesQueryString);

		Pair<String, String> userAttributesQueryString = new Pair<>("", "");
		if (!userAttributes.isEmpty()) {
			userAttributesQueryString.put(" left join user_attr_values uav on users.id=uav.user_id and uav.attr_id in (select id from attr_names where attr_name in (:userAttributes))" , " lower(uav.attr_value)=lower(:searchString) or ");
		}
		result.put("userAttributesQuery", userAttributesQueryString);

		Pair<String, String> uesAttributesQueryString = new Pair<>("", "");
		if (!uesAttributes.isEmpty()) {
			uesAttributesQueryString.put(" left join user_ext_source_attr_values uesav on uesav.user_ext_source_id=ues.id and uesav.attr_id in (select id from attr_names where attr_name in (:uesAttributes))", " lower(uesav.attr_value)=lower(:searchString) or ");
		}
		result.put("uesAttributesQuery", uesAttributesQueryString);

		return result;
	}

	/**
	 * Returns MapSqlParameterSource with values added to search members or users.
	 *
	 * @param searchString string used to search by
	 * @param attributesToSearchBy map of attributes used to search for users or members
	 *
	 * @return filled MapSqlParameterSource
	 */
	public static MapSqlParameterSource getMapSqlParameterSourceToSearchUsersOrMembers(String searchString, Map<String, List<String>> attributesToSearchBy) {
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		namedParams.addValue("searchString", searchString);
		namedParams.addValue("nameString", searchString);
		namedParams.addValue("memberAttributes", attributesToSearchBy.get("memberAttributes"));
		namedParams.addValue("userAttributes", attributesToSearchBy.get("userAttributes"));
		namedParams.addValue("uesAttributes", attributesToSearchBy.get("uesAttributes"));

		return namedParams;
	}

	/**
	 * Sends all alerts about an identity being added. Sends an email to the preferred email of
	 * the user. Also, if the identity has an email and it is different that the preferred email,
	 * another different email is send to this email.
	 *
	 * @param sess session
	 * @param newUes removed userExtSource
	 */
	public static void sendIdentityAddedAlerts(PerunSession sess, UserExtSource newUes) {
		String userEmail = getUserPreferredEmail(sess);
		if (userEmail == null) {
			log.error("Cannot send identity added alert because the user has no preferred mail. User: {}",
					sess.getPerunPrincipal().getUser());
		} else {
			sendIdentityAddedAlertToPreferredMail(sess, newUes, userEmail);
		}
		String uesMail = getUESEmail(sess, newUes);
		if (uesMail != null && !uesMail.equals(userEmail)) {
			sendIdentityAddedAlertToNewIdentityMail(sess, newUes, uesMail);
		}
	}

	/**
	 * Sends all alerts about an identity being removed. Sends an email to the preferred email of
	 * the user. Also, if the identity has an email and it is different that the preferred email,
	 * another different email is send to this email.
	 *
	 * @param sess session
	 * @param removedUes removed userExtSource
	 * @param uesAttrs attributes of the removed userExtSource
	 */
	public static void sendIdentityRemovedAlerts(PerunSession sess, UserExtSource removedUes, List<Attribute> uesAttrs) {
		String userEmail = getUserPreferredEmail(sess);
		if (userEmail == null) {
			log.error("Cannot send identity removed alert because the user has no preferred mail. User: {}",
					sess.getPerunPrincipal().getUser());
		} else {
			sendIdentityRemovedAlertToPreferredMail(sess, removedUes, uesAttrs, userEmail);
		}
		Optional<Attribute> mailAttr = uesAttrs.stream()
				.filter(attr -> attr.getFriendlyName().equals("mail"))
				.filter(attr -> attr.getValue() != null)
				.findFirst();
		if (mailAttr.isPresent()) {
			String uesMail = mailAttr.get().valueAsString();
			if (!uesMail.equals(userEmail)) {
				sendIdentityRemovedAlertToUesMail(sess, removedUes, uesAttrs, uesMail);
			}
		}
	}

	/**
	 * Sends alert to the mail of the removed identity.
	 *
	 * @param sess session
	 * @param removedUes removed userExtSource
	 * @param uesAttrs attributes of the removed userExtSource
	 * @param uesMail email, where the message is send
	 */
	private static void sendIdentityRemovedAlertToUesMail(PerunSession sess, UserExtSource removedUes,
	                                                      List<Attribute> uesAttrs, String uesMail) {
		sendIdentityRemovedAlert(sess, removedUes, uesAttrs, uesMail,
				UES_REMOVED_UES_MAIL, DEFAULT_IDENTITY_REMOVED_UES_MAIL,
				UES_REMOVED_UES_MAIL_SUBJECT, DEFAULT_IDENTITY_REMOVED_UES_MAIL_SUBJECT);
	}

	/**
	 * Sends alert to the preferred mail of the user whose identity has bee removed.
	 *
	 * @param sess session
	 * @param removedUes removed userExtSource
	 * @param uesAttrs attributes of the removed userExtSource
	 * @param userEmail email, where the message is send
	 */
	private static void sendIdentityRemovedAlertToPreferredMail(PerunSession sess, UserExtSource removedUes,
	                                                            List<Attribute> uesAttrs, String userEmail) {
		sendIdentityRemovedAlert(sess, removedUes, uesAttrs, userEmail,
				UES_REMOVED_PREF_MAIL, DEFAULT_IDENTITY_REMOVED_PREF_MAIL,
				UES_REMOVED_PREF_MAIL_SUBJECT, DEFAULT_IDENTITY_REMOVED_PREF_MAIL_SUBJECT);
	}

	/**
	 * Sends alert to the preferred mail of the user of the newly added identity.
	 *
	 * @param sess session
	 * @param newUes new userExtSource
	 * @param userEmail email, where the message is send
	 */
	private static void sendIdentityAddedAlertToPreferredMail(PerunSession sess, UserExtSource newUes, String userEmail) {
		sendIdentityAddedAlert(sess, newUes, userEmail,
				UES_ADDED_PREFERRED_MAIL, DEFAULT_IDENTITY_ADDED_PREF_MAIL,
				UES_ADDED_PREFERRED_MAIL_SUBJECT, DEFAULT_IDENTITY_ADDED_PREF_MAIL_SUBJECT);
	}

	/**
	 * Sends alert to the email of the new given userExtSource.
	 *
	 * @param sess session
	 * @param newUes new userExtSource
	 * @param uesMail email of the newUserExtSource
	 */
	private static void sendIdentityAddedAlertToNewIdentityMail(PerunSession sess, UserExtSource newUes, String uesMail) {
		sendIdentityAddedAlert(sess, newUes, uesMail,
				UES_ADDED_UES_MAIL, DEFAULT_IDENTITY_ADDED_UES_MAIL,
				UES_ADDED_UES_MAIL_SUBJECT, DEFAULT_IDENTITY_ADDED_UES_MAIL_SUBJECT);
	}

	/**
	 * Sends alert about the given ues being removed.
	 *
	 * @param sess session
	 * @param removedUes the removed ues
	 * @param uesAttrs attributes of the removed ues
	 * @param email email, where the alert is send
	 * @param templateName name of the template for the message
	 * @param defaultTemplate default template for the message
	 * @param subjectName name of the subject template
	 * @param defaultSubject default template for the subject
	 */
	private static void sendIdentityRemovedAlert(PerunSession sess, UserExtSource removedUes, List<Attribute> uesAttrs,
	                                             String email, String templateName, String defaultTemplate,
	                                             String subjectName, String defaultSubject) {
		String message = getAlertTemplate(sess, templateName, defaultTemplate);
		message = message.replace(LOGIN_PLACEHOLDER, removedUes.getLogin());
		message = insertOrganization(message, uesAttrs);
		message = insertCurrentTime(message);

		String subject = getAlertTemplate(sess, subjectName, defaultSubject);

		Utils.sendEmail(subject, message, email);
	}

	/**
	 * Sends alert about the given ues being added.
	 *
	 * @param sess session
	 * @param newUes the added ues
	 * @param email email, where the alert is send
	 * @param templateName name of the template for the message
	 * @param defaultTemplate default template for the message
	 * @param subjectName name of the subject template
	 * @param defaultSubject default template for the subject
	 */
	private static void sendIdentityAddedAlert(PerunSession sess, UserExtSource newUes, String email, String templateName,
	                                           String defaultTemplate, String subjectName, String defaultSubject) {
		String message = getAlertTemplate(sess, templateName, defaultTemplate);
		message = message.replace(LOGIN_PLACEHOLDER, newUes.getLogin());
		message = insertOrganization(sess, message, newUes);
		message = insertCurrentTime(message);

		String subject = getAlertTemplate(sess, subjectName, defaultSubject);

		Utils.sendEmail(subject, message, email);
	}

	/**
	 * Into the given message, replace all '{organization}' placeholders with the value of the
	 * organization attribute, that is found for the given userUxtSource. If the ues has no organization
	 * attribute filled, the '<unknown>' string is used.
	 *
	 * @param sess session
	 * @param message message, where the placeholder will be replaced
	 * @param newUes userExtSource for which the organization is found
	 * @return message where the '{organization}' placeholders are replaced
	 */
	private static String insertOrganization(PerunSession sess, String message, UserExtSource newUes) {
		String organization = ORG_UNKNOWN_TEXT;
		try {
			Attribute orgAttr = ((PerunBl)sess.getPerun()).getAttributesManagerBl().getAttribute(sess, newUes, A_UES_ORGANIZATION);
			if (orgAttr.getValue() != null) {
				organization = orgAttr.valueAsString();
			}
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		} catch (AttributeNotExistsException e) {
			log.error("Cannot add organization info, because the attribute does not exist.");
		}
		return message.replace(ORG_PLACEHOLDER, organization);
	}

	/**
	 * In the given message, replaces all '{organization}' placeholders with the value of the
	 * organization attribute, that is given. If there is no such attribute, the '<unknown>'
	 * is used.
	 *
	 * @param message message, where the placeholders will be replaced
	 * @param uesAttributes attributes with the organization attribute
	 * @return message where the '{organization}' placeholders are replaced
	 */
	private static String insertOrganization(String message, List<Attribute> uesAttributes) {
		String organization = uesAttributes.stream()
				.filter(attr -> attr.getFriendlyName().equals("o"))
				.filter(attr -> attr.getValue() != null)
				.map(Attribute::valueAsString)
				.findFirst()
				.orElse(ORG_UNKNOWN_TEXT);

		return message.replace(ORG_PLACEHOLDER, organization);
	}

	/**
	 * In the given message, replaces all '{time}' placeholders with the value of the
	 * current date and time in format MM/dd/yyyy - HH:mm:ss Z.
	 *
	 * @param message message, where the placeholders will be replaced.
	 * @return message with inserted date and time info
	 */
	private static String insertCurrentTime(String message) {
		String currentDateTime = ALERT_TIME_FORMATTER.format(ZonedDateTime.now());
		return message.replace(TIME_PLACEHOLDER, currentDateTime);
	}

	/**
	 * Returns alert template for the given templateName. The templates should be stored in
	 * entityless:def:identityAlertsTemplates attribute, under the 'en' key.
	 * If the templateName is not present or has an empty value, the default value is used.
	 *
	 * @param sess session
	 * @param templateName key of the template
	 * @param defaultValue default value that will be used if needed
	 * @return template for the given templateName, or the default value
	 */
	private static String getAlertTemplate(PerunSession sess, String templateName, String defaultValue) {
		String value = defaultValue;
		try {
			Attribute attr = ((PerunBl)sess.getPerun()).getAttributesManagerBl().getAttribute(sess, KEY_EN, A_E_IDENTITY_ALERTS);
			if (attr.getValue() != null) {
				String attrValue = attr.valueAsMap().get(templateName);
				if (isNotBlank(attrValue)) {
					value = attrValue;
				}
			}
		} catch (AttributeNotExistsException e) {
			log.warn("Identity alerts attribute not found, using default templates.");
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
		return value;
	}

	/**
	 * Returns the mail for the given userExtSource.
	 *
	 * @param sess session
	 * @param ues userExtSource
	 * @return email of the userExtSource or null, if not present
	 */
	private static String getUESEmail(PerunSession sess, UserExtSource ues) {
		try {
			return ((PerunBl)sess.getPerun()).getAttributesManagerBl()
					.getAttribute(sess, ues, A_UES_MAIL)
					.valueAsString();
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}


	/**
	 * Returns preferred mail of the user from the principal.
	 *
	 * @param sess session
	 * @return preferred mail of the user from the principal
	 */
	private static String getUserPreferredEmail(PerunSession sess) {
		try {
			return ((PerunBl)sess.getPerun()).getAttributesManagerBl()
					.getAttribute(sess, sess.getPerunPrincipal().getUser(), A_U_MAIL)
					.valueAsString();
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

}
