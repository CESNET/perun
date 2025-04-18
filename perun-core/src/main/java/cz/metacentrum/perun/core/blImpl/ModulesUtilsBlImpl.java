package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeAction;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.QuotaNotInAllowedLimitException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ModulesUtilsBlImpl implements ModulesUtilsBl {

  public static final String A_E_NAMESPACE_GID_RANGES =
      AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-GIDRanges";
  public static final String A_G_UNIX_GID_NAMESPACE = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace";
  public static final String A_G_UNIX_GROUP_NAME_NAMESPACE =
      AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace";
  public static final String A_R_UNIX_GID_NAMESPACE = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace";
  public static final String A_R_UNIX_GROUP_NAME_NAMESPACE =
      AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace";
  public static final String A_F_UNIX_GID_NAMESPACE = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace";
  public static final String A_F_UNIX_GROUP_NAME_NAMESPACE =
      AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace";
  public static final String A_F_GOOGLE_GROUPS_DOMAIN = AttributesManager.NS_FACILITY_ATTR_DEF + ":googleGroupsDomain";
  //Often used patterns
  public static final Pattern QUOTA_WITH_METRICS_PATTERN =
      Pattern.compile("^([0-9]+([.][0-9]+)?[KMGTPE]?):([0-9]+([.][0-9]+)?[KMGTPE]?)$");
  public static final Pattern QUOTA_WITHOUT_METRICS_PATTERN = Pattern.compile("^([0-9]+)(:)([0-9]+)$");
  public static final Pattern QUOTA_PATH_PATTERN = Pattern.compile("^[-a-zA-Z.0-9_/:=,]+$");
  public static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+([.][0-9]+)?");
  public static final Pattern LETTER_PATTERN = Pattern.compile("[A-Z]");
  public static final Pattern FQDN_PATTERN = Pattern.compile("^((?!-)[a-zA-Z0-9-]{1,63}(?<!-)\\.)+[a-zA-Z]{2,63}\\.?$");
  //previous regex ^/[-a-zA-Z0-9_/]*$"
  public static final Pattern SHELL_PATTERN = Pattern.compile("^(/[-_a-zA-Z0-9]+)+$");
  public static final List<String> RESERVED_NAMES_FOR_UNIX_GROUPS =
      Arrays.asList("root", "daemon", "tty", "bin", "sys", "sudo", "nogroup", "hadoop", "hdfs", "mapred", "yarn",
          "hsqldb", "derby", "jetty", "hbase", "zookeeper", "users", "oozie", "hive");
  public static final List<String> UNPERMITTED_NAMES_FOR_USER_LOGINS =
      Arrays.asList("arraysvcs", "at", "backup", "bin", "daemon", "Debian-exim", "flexlm", "ftp", "games", "gdm",
          "glite", "gnats", "haldaemon", "identd", "irc", "libuuid", "list", "lp", "mail", "man", "messagebus", "news",
          "nobody", "ntp", "openslp", "pcp", "polkituser", "postfix", "proxy", "pulse", "puppet", "root", "saned",
          "smmsp", "smmta", "sshd", "statd", "suse-ncc", "sync", "sys", "uucp", "uuidd", "www-data", "wwwrun", "zenssh",
          "tomcat6", "tomcat7", "tomcat8", "tomcat", "nn", "dn", "rm", "nm", "sn", "jn", "jhs", "http", "yarn", "hdfs",
          "mapred", "hadoop", "hsqldb", "derby", "jetty", "hbase", "zookeeper", "hive", "hue", "oozie", "httpfs");
  //Definition of K = KB, M = MB etc.
  public static final long M = 1024;
  public static final long G = M * 1024;
  public static final long T = G * 1024;
  public static final long P = T * 1024;
  public static final long E = P * 1024;
  static final Logger LOG = LoggerFactory.getLogger(ServicesManagerBlImpl.class);
  private static final String A_E_USED_GIDS = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":usedGids";
  Map<String, String> perunNamespaces = null;
  private PerunBl perunBl;

  public ModulesUtilsBlImpl() {
  }

  /**
   * Normalize string for purpose of generating safe login value.
   *
   * @return normalized string
   */
  public static String normalizeStringForLogin(String toBeNormalized) {

    if (toBeNormalized == null || toBeNormalized.trim().isEmpty()) {
      return null;
    }

    toBeNormalized = toBeNormalized.toLowerCase();
    toBeNormalized = java.text.Normalizer.normalize(toBeNormalized, java.text.Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    toBeNormalized = toBeNormalized.replaceAll("[^a-zA-Z]+", "");

    // unable to fill login for users without name or with partial name
    if (toBeNormalized.isEmpty()) {
      return null;
    }

    return toBeNormalized;

  }

  /**
   * Returns pair of number (BigDecimal) and unit (String) from given string. Returns default value Pair<0, "G"> if
   * parsing fails. E.g.: "5T" -> Pair<5, "T">
   *
   * @param attributeValue string to parse
   * @return pair of number and unit
   */
  public static Pair<BigDecimal, String> getNumberAndUnitFromString(String attributeValue) {
    String numberString = "0";
    String unit = "G";

    if (attributeValue != null) {
      Matcher numberMatcher = NUMBER_PATTERN.matcher(attributeValue);
      Matcher letterMatcher = LETTER_PATTERN.matcher(attributeValue);
      numberMatcher.find();
      letterMatcher.find();
      try {
        numberString = attributeValue.substring(numberMatcher.start(), numberMatcher.end());
      } catch (IllegalStateException ex) {
        LOG.debug("No number could be parsed from given string.", ex);
      }
      try {
        unit = attributeValue.substring(letterMatcher.start(), letterMatcher.end());
      } catch (IllegalStateException ex) {
        LOG.debug("No unit could be parsed from given string.", ex);
      }
    }

    BigDecimal number = new BigDecimal(numberString.replace(',', '.'));

    return new Pair<>(number, unit);
  }

  /**
   * Extracts expiration of the given certificates.
   *
   * @param certificates as a map where the key is a DN and the value is a certificate
   * @return map where the key is certificate DN and the value is a certificate expiration
   */
  public static Map<String, String> retrieveCertificatesExpiration(Map<String, String> certificates) {
    Utils.notNull(certificates, "certificates");
    Map<String, String> resultMap = new LinkedHashMap<>();
    certificates.forEach((key, value) -> resultMap.put(key, getCertificateExpiration(value)));
    return resultMap;
  }

  /**
   * Retrieve expiration of the given certificate
   *
   * @param certificate in PEM format from which the expiration will be retrieved
   * @return Certificate expiration as String
   */
  private static String getCertificateExpiration(String certificate) {
    Utils.notNull(certificate, "certificate");

    String certWithoutBegin = certificate.replaceFirst("-----BEGIN CERTIFICATE-----", "");
    String rawCert = certWithoutBegin.replaceFirst("-----END CERTIFICATE-----", "");

    try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(rawCert.getBytes()))) {
      CertificateFactory certFact = CertificateFactory.getInstance("X.509");
      X509Certificate cert = (X509Certificate) certFact.generateCertificate(bis);
      DateFormat dateFormat = DateFormat.getDateInstance();
      return dateFormat.format(cert.getNotAfter());
    } catch (IllegalArgumentException e) {
      throw new ConsistencyErrorException("Certificate is not in base64 format!", e);
    } catch (IOException | CertificateException e) {
      throw new InternalError(e);
    }
  }

  @Override
  public Map<Integer, Integer> checkAndConvertIDRanges(Attribute idRangesAttribute)
      throws WrongAttributeValueException {
    //Prepare structure for better working with GID Ranges
    Map<Integer, Integer> convertedRanges = new HashMap<>();

    //For null attribute throw an exception
    if (idRangesAttribute == null) {
      throw new InternalErrorException("Can't get value from null attribute!");
    }

    Map<String, String> idRanges = idRangesAttribute.valueAsMap();

    //Return empty map if there is empty input of gidRanges in method parameters
    if (idRanges == null || idRanges.isEmpty()) {
      return convertedRanges;
    }

    //Check every range if it is in correct format and it is valid range
    for (String minimumOfRange : idRanges.keySet()) {
      //Check not null
      if (minimumOfRange == null || minimumOfRange.isEmpty()) {
        throw new WrongAttributeValueException(idRangesAttribute, "Minimum in one of id ranges is empty!");
      }
      String maximumOfRange = idRanges.get(minimumOfRange);
      if (maximumOfRange == null || maximumOfRange.isEmpty()) {
        throw new WrongAttributeValueException(idRangesAttribute, "Maximum in one of id ranges is empty!");
      }

      //Transfer string to numbers
      Integer minimum;
      Integer maximum;

      try {
        minimum = Integer.valueOf(minimumOfRange);
        maximum = Integer.valueOf(maximumOfRange);
      } catch (NumberFormatException ex) {
        throw new WrongAttributeValueException(idRangesAttribute,
            "Min or max value of some range is not correct number format.");
      }

      //Check if min value from range is bigger than 0
      if (minimum < 1) {
        throw new WrongAttributeValueException(idRangesAttribute, "Minimum of one of id ranges is less than 0.");
      }

      //Check if it is correct range
      if (minimum > maximum) {
        throw new WrongAttributeValueException(idRangesAttribute,
            "One of id ranges is not correct range. Minimum of this range is bigger then it's maximum.");
      }

      //Put this valid range to the map of correct gid ranges
      convertedRanges.put(minimum, maximum);
    }

    //Check gid ranges overlaps (there should be no overlaps)
    Integer lastMaxValue = 0;
    for (Integer minValue : convertedRanges.keySet().stream().sorted().collect(Collectors.toList())) {
      if (minValue <= lastMaxValue) {
        throw new WrongAttributeValueException(idRangesAttribute, "There is an overlap between two id ranges.");
      }
      lastMaxValue = convertedRanges.get(minValue);
    }

    return convertedRanges;
  }

  @Override
  public Map<String, Pair<BigDecimal, BigDecimal>> checkAndTransferQuotas(Attribute quotasAttribute,
                                                                          PerunBean firstPlaceholder,
                                                                          PerunBean secondPlaceholder,
                                                                          boolean withMetrics)
      throws WrongAttributeValueException {
    //firstPlaceholder can't be null
    if (firstPlaceholder == null) {
      throw new InternalErrorException("Missing first mandatory placeHolder (PerunBean).");
    }
    //If quotas attribute is null or it's value is empty, return empty hash map
    if (quotasAttribute == null || quotasAttribute.getValue() == null) {
      return new LinkedHashMap<>();
    }

    //Prepare result container and value of attribute
    Map<String, Pair<BigDecimal, BigDecimal>> transferedQuotas = new LinkedHashMap<>();
    Map<String, String> defaultQuotasMap = quotasAttribute.valueAsMap();

    //List to test if all paths are unique (/var/log and /var/log/ are the same so these two paths are not unique)
    List<String> uniquePaths = new ArrayList<>();
    for (String path : defaultQuotasMap.keySet()) {
      //null is not correct path for volume on File System
      if (path == null || path.isEmpty()) {
        throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
            "The path of some volume where quota should be set is null.");
      }

      //testing if path is unique
      String canonicalPath = Paths.get(path).normalize().toString();
      //path should not end on '/' (problem with some systems as GPFS)
      if (!canonicalPath.equals("/") && canonicalPath.endsWith("/")) {
        canonicalPath = canonicalPath.substring(0, canonicalPath.length() - 1);
      }
      if (!QUOTA_PATH_PATTERN.matcher(canonicalPath).matches()) {
        throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
            "Path '" + path + "' is not correct form.");
      }

      if (uniquePaths.contains(canonicalPath)) {
        throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
            "Paths are not unique, there are two same paths: " + path);
      } else {
        uniquePaths.add(canonicalPath);
      }

      String quota = defaultQuotasMap.get(path);
      //quota can't be null, if exists in attribute, must be set in some way
      if (quota == null) {
        throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
            "The quota of some volume where quota should be set is null.");
      }

      //check format of quota parameter (for data with metrics, for count of files without metrics)
      Matcher quotaMatcher;
      if (withMetrics) {
        quotaMatcher = ModulesUtilsBlImpl.QUOTA_WITH_METRICS_PATTERN.matcher(quota);
        if (!quotaMatcher.matches()) {
          throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
              "Format of quota in quotas attribute is not correct.");
        }
      } else {
        quotaMatcher = ModulesUtilsBlImpl.QUOTA_WITHOUT_METRICS_PATTERN.matcher(quota);
        if (!quotaMatcher.matches()) {
          throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
              "Format of quota in quotas attribute is not correct.");
        }
      }

      //Parse quotas to variables
      String softQuota = quotaMatcher.group(1);
      String hardQuota = quotaMatcher.group(3);

      //Parse number pattern and letter pattern from whole quotas

      //SoftQuotaNumber
      BigDecimal softQuotaAfterTransfer;
      BigDecimal hardQuotaAfterTransfer;
      //special behavior with metrics
      if (withMetrics) {
        Matcher numberMatcher = NUMBER_PATTERN.matcher(softQuota);
        if (!numberMatcher.find()) {
          throw new ConsistencyErrorException(
              "Matcher can't find number in softQuota '" + softQuota + "' in attribute " + quotasAttribute);
        }
        String softQuotaNumber = numberMatcher.group();

        //SoftQuotaLetter
        String softQuotaLetter;
        Matcher letterMatcher = LETTER_PATTERN.matcher(softQuota);
        //in this case no letter means default and default is G
        if (!letterMatcher.find()) {
          softQuotaLetter = "G";
        } else {
          softQuotaLetter = letterMatcher.group();
        }

        //HardQuotaNumber
        numberMatcher = NUMBER_PATTERN.matcher(hardQuota);
        if (!numberMatcher.find()) {
          throw new ConsistencyErrorException(
              "Matcher can't find number in hardQuota '" + hardQuota + "' in attribute " + quotasAttribute);
        }
        String hardQuotaNumber = numberMatcher.group();

        //HardQuotaLetter
        String hardQuotaLetter;
        letterMatcher = LETTER_PATTERN.matcher(hardQuota);
        //in this case no letter means default and default is G
        if (!letterMatcher.find()) {
          hardQuotaLetter = "G";
        } else {
          hardQuotaLetter = letterMatcher.group();
        }

        //Prepare whole big decimal numbers
        softQuotaAfterTransfer = new BigDecimal(softQuotaNumber);
        hardQuotaAfterTransfer = new BigDecimal(hardQuotaNumber);

        //multiplying for softQuota
        switch (softQuotaLetter) {
          case "K":
            break; //K is basic metric, no need to multiply it
          case "G":
            softQuotaAfterTransfer = softQuotaAfterTransfer.multiply(BigDecimal.valueOf(G));
            break;
          case "M":
            softQuotaAfterTransfer = softQuotaAfterTransfer.multiply(BigDecimal.valueOf(M));
            break;
          case "T":
            softQuotaAfterTransfer = softQuotaAfterTransfer.multiply(BigDecimal.valueOf(T));
            break;
          case "P":
            softQuotaAfterTransfer = softQuotaAfterTransfer.multiply(BigDecimal.valueOf(P));
            break;
          case "E":
            softQuotaAfterTransfer = softQuotaAfterTransfer.multiply(BigDecimal.valueOf(E));
            break;
          default:
            throw new ConsistencyErrorException(
                "There is not allowed character in soft quota letter '" + softQuotaLetter + "'.");
        }

        //multiplying for softQuota
        switch (hardQuotaLetter) {
          case "K":
            break; //K is basic metric, no need to multiply it
          case "G":
            hardQuotaAfterTransfer = hardQuotaAfterTransfer.multiply(BigDecimal.valueOf(G));
            break;
          case "M":
            hardQuotaAfterTransfer = hardQuotaAfterTransfer.multiply(BigDecimal.valueOf(M));
            break;
          case "T":
            hardQuotaAfterTransfer = hardQuotaAfterTransfer.multiply(BigDecimal.valueOf(T));
            break;
          case "P":
            hardQuotaAfterTransfer = hardQuotaAfterTransfer.multiply(BigDecimal.valueOf(P));
            break;
          case "E":
            hardQuotaAfterTransfer = hardQuotaAfterTransfer.multiply(BigDecimal.valueOf(E));
            break;
          default:
            throw new ConsistencyErrorException(
                "There is not allowed character in hard quota letter '" + hardQuotaLetter + "'.");
        }
        //easy way without metrics
      } else {
        softQuotaAfterTransfer = new BigDecimal(softQuota);
        hardQuotaAfterTransfer = new BigDecimal(hardQuota);
      }

      //test comparing softQuota and hardQuota (softQuota must be less or equals than hardQuota, 0 means unlimited)
      //1] if softQuota is unlimited, but hardQuota not = exception
      if (softQuotaAfterTransfer.compareTo(BigDecimal.valueOf(0)) == 0 &&
          hardQuotaAfterTransfer.compareTo(BigDecimal.valueOf(0)) != 0) {
        throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
            "SoftQuota is set to unlimited (0) but hardQuota is limited to '" + hardQuota + "'.");
        //2] if hardQuota is not unlimited but still it is less then softQuota = exception
      } else if (hardQuotaAfterTransfer.compareTo(BigDecimal.valueOf(0)) != 0 &&
                 hardQuotaAfterTransfer.compareTo(softQuotaAfterTransfer) < 0) {
        throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
            "One of quotas is not correct. HardQuota '" + hardQuota + "' is less then softQuota '" + softQuota + "'.");
      }
      //other cases are ok

      transferedQuotas.put(canonicalPath, new Pair<>(softQuotaAfterTransfer, hardQuotaAfterTransfer));
    }

    return transferedQuotas;
  }

  @Override
  public Pair<Integer, Integer> checkAndTransferBucketQuota(Attribute quotasAttribute,
                                                            PerunBean firstPlaceholder,
                                                            PerunBean secondPlaceholder)
      throws WrongAttributeValueException {
    String quota = quotasAttribute.valueAsString();
    //quota can't be null, if exists in attribute, must be set in some way
    if (quota == null) {
      throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
          "The quota should be set but is null.");
    }
    Matcher quotaMatcher;
    quotaMatcher = ModulesUtilsBlImpl.QUOTA_WITHOUT_METRICS_PATTERN.matcher(quota);
    if (!quotaMatcher.matches()) {
      throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
          "Wrong format of bucket quota. Expected format 'softQuota:hardQuota'. Example: '100:200'");
    }
    //Parse quotas to variables
    String softQuota = quotaMatcher.group(1);
    String hardQuota = quotaMatcher.group(3);

    Integer softQuotaAfterTransfer = Integer.parseInt(softQuota);
    Integer hardQuotaAfterTransfer = Integer.parseInt(hardQuota);

    //test comparing softQuota and hardQuota (softQuota must be less or equals than hardQuota, 0 means unlimited)
    //1] if softQuota is unlimited, but hardQuota not = exception
    if (softQuotaAfterTransfer.compareTo(0) == 0 &&
        hardQuotaAfterTransfer.compareTo(0) != 0) {
      throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
          "SoftQuota is set to unlimited (0) but hardQuota is limited to '" + hardQuota + "'.");
      //2] if hardQuota is not unlimited but still it is less then softQuota = exception
    } else if (hardQuotaAfterTransfer.compareTo(0) != 0 &&
               hardQuotaAfterTransfer.compareTo(softQuotaAfterTransfer) < 0) {
      throw new WrongAttributeValueException(quotasAttribute, firstPlaceholder, secondPlaceholder,
          "One of quotas is not correct. HardQuota '" + hardQuota + "' is less then softQuota '" + softQuota + "'.");
    }
    //other cases are ok
    return new Pair<>(softQuotaAfterTransfer, hardQuotaAfterTransfer);
  }

  @Override
  public void checkAttributeRegex(Attribute attribute, Pattern defaultRegex) throws WrongAttributeValueException {
    if (attribute == null || attribute.getValue() == null) {
      throw new InternalErrorException("Attribute or it's value is null.");
    }
    String attributeValue = (String) attribute.getValue();
    checkPerunNamespacesMap();

    String regex = perunNamespaces.get(attribute.getFriendlyName() + ":regex");
    if (regex != null) {
      //Check if regex is valid
      try {
        Pattern.compile(regex);
      } catch (PatternSyntaxException e) {
        LOG.error("Regex pattern \"" + regex + "\" from \"" + attribute.getFriendlyName() + ":regex\"" +
                  " property of perun-namespaces.properties file is invalid.");
        throw new InternalErrorException(
            "Regex pattern \"" + regex + "\" from \"" + attribute.getFriendlyName() + ":regex\"" +
            " property of perun-namespaces.properties file is invalid.");
      }
      if (!attributeValue.matches(regex)) {
        throw new WrongAttributeValueException(attribute,
            "Wrong format. Regex: \"" + regex + "\" expected for this attribute:");
      }
    } else {
      //Regex property not found in our attribute map, so use the default hardcoded regex
      if (!defaultRegex.matcher(attributeValue).matches()) {
        throw new WrongAttributeValueException(attribute,
            "Wrong format. Regex: \"" + defaultRegex + "\" expected for this attribute:");
      }
    }
  }

  @Override
  public void checkAttributeValueIsIncludedOrSubgroupId(PerunSessionImpl sess, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }

    // subgroups
    Set<Integer> includedAndSubgroupsIds =
        sess.getPerunBl().getGroupsManagerBl().getAllSubGroups(sess, group).stream().map(Group::getId)
            .collect(Collectors.toSet());

    // directly included groups
    includedAndSubgroupsIds.addAll(
        sess.getPerunBl().getGroupsManagerBl().getGroupUnions(sess, group, false).stream().map(PerunBean::getId)
            .collect(Collectors.toSet()));

    for (String groupId : attribute.valueAsList()) {
      if (!includedAndSubgroupsIds.contains(Integer.valueOf(groupId))) {
        throw new WrongReferenceAttributeValueException(
            "Id: " + groupId + " is not id of any subgroup or included group of group " + group);
      }
    }
  }

  @Override
  public void checkFormatOfShell(String shell, Attribute attribute) throws WrongAttributeValueException {
    Matcher match = SHELL_PATTERN.matcher(shell);
    if (!match.matches()) {
      throw new WrongAttributeValueException(attribute, "Bad shell attribute format " + shell);
    }
  }

  @Override
  public void checkIfGIDIsWithinRange(PerunSessionImpl sess, Attribute attribute)
      throws WrongAttributeAssignmentException, AttributeNotExistsException, WrongAttributeValueException,
      WrongReferenceAttributeValueException {
    Utils.notNull(attribute, "attribute");
    Integer gid = null;
    if (attribute.getValue() != null) {
      gid = (Integer) attribute.getValue();
    }

    if (gid == null) {
      throw new WrongAttributeValueException(attribute, "Gid with null value is not allowed.");
    }

    String gidNamespace = attribute.getFriendlyNameParameter();

    Attribute gidRangesAttribute =
        sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_NAMESPACE_GID_RANGES);
    Map<Integer, Integer> gidRanges = checkAndConvertIDRanges(gidRangesAttribute);

    //Gid is not in range, throw exception
    if (!isGIDWithinRanges(gidRanges, gid)) {
      throw new WrongReferenceAttributeValueException(attribute, gidRangesAttribute, null, null, gidNamespace, null,
          "GID number is not in allowed ranges " + gidRanges + " for namespace " + gidNamespace);
    }
  }

  @Override
  public void checkIfListOfGIDIsWithinRange(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeAssignmentException, AttributeNotExistsException, WrongAttributeValueException {
    Utils.notNull(attribute, "attribute");
    List<String> gidsToCheck = attribute.valueAsList();
    if (gidsToCheck != null) {
      String gidNamespace = attribute.getFriendlyNameParameter();
      Attribute gidRangesAttribute =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_NAMESPACE_GID_RANGES);
      Map<Integer, Integer> gidRanges = checkAndConvertIDRanges(gidRangesAttribute);

      for (String gidToCheck : gidsToCheck) {
        try {
          Integer gid = Integer.valueOf(gidToCheck);

          if (!isGIDWithinRanges(gidRanges, gid)) {
            throw new WrongAttributeValueException(attribute,
                "GID number is not in allowed ranges " + gidRanges + " for namespace " + gidNamespace);
          }
        } catch (NumberFormatException ex) {
          throw new WrongAttributeValueException(attribute, user, "attribute is not a number", ex);
        }
      }
    }
  }

  @Override
  public void checkIfQuotasIsInLimit(Map<String, Pair<BigDecimal, BigDecimal>> quotaToCheck,
                                     Map<String, Pair<BigDecimal, BigDecimal>> limitQuota) {
    if (quotaToCheck == null) {
      throw new InternalErrorException("Quota to check can't be null.");
    }
    if (limitQuota == null) {
      throw new InternalErrorException("Limit quota can't be null.");
    }

    //If there is no value to check, then everything is in limit (we don't need to limit anything)
    if (quotaToCheck.isEmpty()) {
      return;
    }

    //test every record of quotaToCheck against record in limitQuota
    for (String volumeToCheck : quotaToCheck.keySet()) {
      if (!limitQuota.containsKey(volumeToCheck)) {
        throw new QuotaNotInAllowedLimitException(quotaToCheck, limitQuota,
            "Volume " + volumeToCheck + " is missing in limitQuota.");
      }

      Pair<BigDecimal, BigDecimal> volumeToCheckQuotas = quotaToCheck.get(volumeToCheck);
      Pair<BigDecimal, BigDecimal> volumeToCheckLimitQuotas = limitQuota.get(volumeToCheck);

      //Check limit of softQuota, zero limit means unlimited so no need for testing
      if (volumeToCheckLimitQuotas.getLeft().compareTo(BigDecimal.ZERO) != 0) {
        if (volumeToCheckQuotas.getLeft().compareTo(BigDecimal.ZERO) == 0 ||
            volumeToCheckQuotas.getLeft().compareTo(volumeToCheckLimitQuotas.getLeft()) > 0) {
          throw new QuotaNotInAllowedLimitException(quotaToCheck, limitQuota,
              "SoftQuota of volume " + volumeToCheck + " is bigger than limit.");
        }
      }

      //Check limit of hardQuota, zero limit means unlimited so no need for testing
      if (volumeToCheckLimitQuotas.getRight().compareTo(BigDecimal.ZERO) != 0) {
        if (volumeToCheckQuotas.getRight().compareTo(BigDecimal.ZERO) == 0 ||
            volumeToCheckQuotas.getRight().compareTo(volumeToCheckLimitQuotas.getRight()) > 0) {
          throw new QuotaNotInAllowedLimitException(quotaToCheck, limitQuota,
              "HardQuota of volume " + volumeToCheck + " is bigger than limit.");
        }
      }
    }
  }

  @Override
  public void checkLoginNamespaceRegex(String namespace, String login, Pattern defaultRegex)
      throws InvalidLoginException {
    Utils.notNull(namespace, "namespace to check login syntax");
    Utils.notNull(login, "login to check syntax for");

    checkPerunNamespacesMap();

    String regex = perunNamespaces.get("login-namespace:" + namespace + ":regex");
    if (regex != null) {
      //Check if regex is valid
      try {
        Pattern.compile(regex);
      } catch (PatternSyntaxException e) {
        LOG.error(
            "Regex pattern \"{}\" from \"login-namespace:{}:regex\" property of perun-namespaces.properties file is " +
            "invalid.", regex, namespace);
        throw new InternalErrorException("Regex pattern \"" + regex + "\" from \"login-namespace:" + namespace +
                                         ":regex\" property of perun-namespaces.properties file is invalid.");
      }
      // check syntax or if its between exceptions
      if (!login.matches(regex) && !isLoginExceptionallyAllowed(namespace, login)) {
        LOG.warn("Login '{}' in {} namespace doesn't match regex: {}", login, namespace, regex);
        throw new InvalidLoginException("Login doesn't matches expected regex: \"" + regex + "\"");
      }
    } else {
      // Regex property not found in our attribute map, so use the default hardcoded regex
      // check syntax or if its between exceptions
      if (!defaultRegex.matcher(login).matches() && !isLoginExceptionallyAllowed(namespace, login)) {
        LOG.warn("Login '{}' in {} namespace doesn't match regex: {}", login, namespace, regex);
        throw new InvalidLoginException("Login doesn't matches expected regex: \"" + defaultRegex + "\"");
      }
    }
  }

  /**
   * Internal protected method. Checks this.perunNamespaces map, which is always initialized as null. If null, it tries
   * to load the configuration into this map from a perun-namespaces.properties file. If the file does not exist, it
   * creates an empty HashMap, so it's not null anymore.
   */
  protected void checkPerunNamespacesMap() {
    if (perunNamespaces == null) {
      try {
        perunNamespaces = BeansUtils.getAllPropertiesFromCustomConfiguration("perun-namespaces.properties");
      } catch (InternalErrorException e) {
        perunNamespaces = new HashMap<>();
      }
    }
  }

  @Override
  public void checkReservedUnixGroupNames(Attribute groupNameAttribute) throws WrongAttributeValueException {
    if (groupNameAttribute == null) {
      return;
    }
    checkPerunNamespacesMap();

    String reservedNames = perunNamespaces.get(groupNameAttribute.getFriendlyName() + ":reservedNames");
    if (reservedNames != null) {
      List<String> reservedNamesList = Arrays.asList(reservedNames.split("\\s*,\\s*"));
      if (reservedNamesList.contains(groupNameAttribute.valueAsString())) {
        throw new WrongAttributeValueException(groupNameAttribute, "This groupName is reserved.");
      }
    } else {
      //Property not found in our attribute map, so we will use the default hardcoded values instead
      if (RESERVED_NAMES_FOR_UNIX_GROUPS.contains(groupNameAttribute.valueAsString())) {
        throw new WrongAttributeValueException(groupNameAttribute, "This groupName is reserved.");
      }
    }
  }

  @Override
  public Map<String, Pair<BigDecimal, BigDecimal>> countUserFacilityQuotas(
      List<Map<String, Pair<BigDecimal, BigDecimal>>> allUserQuotas) {
    Map<String, Pair<BigDecimal, BigDecimal>> resultTransferredQuotas = new LinkedHashMap<>();
    //for every transfered map of merged quotas count one result transfered map
    for (Map<String, Pair<BigDecimal, BigDecimal>> mapValue : allUserQuotas) {
      //for every path in one transfered map
      for (String pathKey : mapValue.keySet()) {
        //if path not exists in result map, add it with it's values
        if (!resultTransferredQuotas.containsKey(pathKey)) {
          resultTransferredQuotas.put(pathKey, mapValue.get(pathKey));
          //if path already exists in result map, sum their quotas together
        } else {
          Pair<BigDecimal, BigDecimal> quotasValue1 = resultTransferredQuotas.get(pathKey);
          Pair<BigDecimal, BigDecimal> quotasValue2 = mapValue.get(pathKey);
          //for soft quota (left part of pair)
          BigDecimal softQuota = BigDecimal.ZERO;
          if (quotasValue1.getLeft().compareTo(BigDecimal.ZERO) != 0 &&
              quotasValue2.getLeft().compareTo(BigDecimal.ZERO) != 0) {
            softQuota = quotasValue1.getLeft().add(quotasValue2.getLeft());
          }
          //for hard quota (right part of pair)
          BigDecimal hardQuota = BigDecimal.ZERO;
          if (quotasValue1.getRight().compareTo(BigDecimal.ZERO) != 0 &&
              quotasValue2.getRight().compareTo(BigDecimal.ZERO) != 0) {
            hardQuota = quotasValue1.getRight().add(quotasValue2.getRight());
          }
          //create new pair of summed numbers
          Pair<BigDecimal, BigDecimal> finalQuotasValue = new Pair<>(softQuota, hardQuota);
          //add new summed pair to the result map
          resultTransferredQuotas.put(pathKey, finalQuotasValue);
        }
      }
    }
    //return result map
    return resultTransferredQuotas;
  }

  public List<Group> findCollisionGroupsWithSamgeGroupGid(PerunSessionImpl sess, Resource resource, String namespace)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    Utils.notNull(sess, "perunSessionImpl");
    Utils.notNull(resource, "resource");
    Utils.notNull(namespace, "namespace");
    Attribute resourceUnixGid =
        getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_UNIX_GID_NAMESPACE + ":" + namespace);
    Attribute copyGroupUnixGid = new Attribute(
        getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_G_UNIX_GID_NAMESPACE + ":" + namespace));
    copyGroupUnixGid.setValue(resourceUnixGid.getValue());
    return getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, copyGroupUnixGid);
  }

  public List<Group> findCollisionGroupsWithSamgeGroupGid(PerunSessionImpl sess, Group group, String namespace)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    Utils.notNull(sess, "perunSessionImpl");
    Utils.notNull(group, "group");
    Utils.notNull(namespace, "namespace");
    Attribute groupUnixGid =
        getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_UNIX_GID_NAMESPACE + ":" + namespace);
    List<Group> groupsWithSameUnixGid = getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupUnixGid);
    groupsWithSameUnixGid.remove(group);
    return groupsWithSameUnixGid;
  }

  public List<Group> findCollisionGroupsWithSamgeGroupName(PerunSessionImpl sess, Resource resource, String namespace)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    Utils.notNull(sess, "perunSessionImpl");
    Utils.notNull(resource, "resource");
    Utils.notNull(namespace, "namespace");
    Attribute resourceUnixGroupName = getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, resource, A_R_UNIX_GROUP_NAME_NAMESPACE + ":" + namespace);
    Attribute copyGroupUnixGroupName = new Attribute(getPerunBl().getAttributesManagerBl()
        .getAttributeDefinition(sess, A_G_UNIX_GROUP_NAME_NAMESPACE + ":" + namespace));
    copyGroupUnixGroupName.setValue(resourceUnixGroupName.getValue());
    return getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, copyGroupUnixGroupName);

  }

  public List<Group> findCollisionGroupsWithSamgeGroupName(PerunSessionImpl sess, Group group, String namespace)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    Utils.notNull(sess, "perunSessionImpl");
    Utils.notNull(group, "group");
    Utils.notNull(namespace, "namespace");
    Attribute groupUnixGroupName =
        getPerunBl().getAttributesManagerBl().getAttribute(sess, group,
            A_G_UNIX_GROUP_NAME_NAMESPACE + ":" + namespace);
    List<Group> groupsWithsameGroupName =
        getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, groupUnixGroupName);
    groupsWithsameGroupName.remove(group);
    return groupsWithsameGroupName;
  }

  public List<Resource> findCollisionResourcesWithSameGid(PerunSessionImpl sess, Resource resource, String namespace)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    Utils.notNull(sess, "perunSessionImpl");
    Utils.notNull(resource, "resource");
    Utils.notNull(namespace, "namespace");
    Attribute resourceUnixGid =
        getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_UNIX_GID_NAMESPACE + ":" + namespace);
    List<Resource> resourcesWithSameGid =
        getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, resourceUnixGid);
    resourcesWithSameGid.remove(resource);
    return resourcesWithSameGid;
  }

  public List<Resource> findCollisionResourcesWithSameGid(PerunSessionImpl sess, Group group, String namespace)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    Utils.notNull(sess, "perunSessionImpl");
    Utils.notNull(group, "group");
    Utils.notNull(namespace, "namespace");
    Attribute groupUnixGid =
        getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_UNIX_GID_NAMESPACE + ":" + namespace);
    Attribute copyResourceUnixGid = new Attribute(
        getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, A_R_UNIX_GID_NAMESPACE + ":" + namespace));
    copyResourceUnixGid.setValue(groupUnixGid.getValue());
    return getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, copyResourceUnixGid);
  }

  public List<Resource> findCollisionResourcesWithSameGroupName(PerunSessionImpl sess, Resource resource,
                                                                String namespace)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    Utils.notNull(sess, "perunSessionImpl");
    Utils.notNull(resource, "resource");
    Attribute resourceUnixGroupName = getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, resource, A_R_UNIX_GROUP_NAME_NAMESPACE + ":" + namespace);
    List<Resource> resourcesWithSameUnixGroupName =
        getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, resourceUnixGroupName);
    resourcesWithSameUnixGroupName.remove(resource);
    return resourcesWithSameUnixGroupName;
  }

  public List<Resource> findCollisionResourcesWithSameGroupName(PerunSessionImpl sess, Group group, String namespace)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    Utils.notNull(sess, "perunSessionImpl");
    Utils.notNull(group, "group");
    Utils.notNull(namespace, "namespace");
    Attribute groupUnixGroupName =
        getPerunBl().getAttributesManagerBl().getAttribute(sess, group,
            A_G_UNIX_GROUP_NAME_NAMESPACE + ":" + namespace);
    Attribute copyResourceUnixGroupName = new Attribute(getPerunBl().getAttributesManagerBl()
        .getAttributeDefinition(sess, A_R_UNIX_GROUP_NAME_NAMESPACE + ":" + namespace));
    copyResourceUnixGroupName.setValue(groupUnixGroupName.getValue());
    return getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, copyResourceUnixGroupName);
  }

  @Override
  public Integer getCommonGIDOfGroupsWithSameNameInSameNamespace(PerunSessionImpl sess,
                                                                 List<Group> groupsWithSameGroupNameInSameNamespace,
                                                                 String gidNamespace, Integer commonGID)
      throws WrongAttributeAssignmentException {
    //If there are no groups, return commonGID from param (it can be null)
    if (groupsWithSameGroupNameInSameNamespace == null || groupsWithSameGroupNameInSameNamespace.isEmpty()) {
      return commonGID;
    }
    Utils.notNull(gidNamespace, "gidNamespace");

    Group commonGIDGroup = null;  //only for more verbose exception messages
    for (Group g : groupsWithSameGroupNameInSameNamespace) {
      try {
        Attribute attr = sess.getPerunBl().getAttributesManagerBl()
            .getAttribute(sess, g, A_G_UNIX_GID_NAMESPACE + ":" + gidNamespace);
        if (attr.getValue() != null) {
          if (commonGID == null) {
            commonGIDGroup = g;
            commonGID = (Integer) attr.getValue();
          } else {
            if (!commonGID.equals(attr.getValue())) {
              throw new ConsistencyErrorException(
                  "There are at least 1 groups/resources with same GroupName in same namespace but with different GID" +
                  " in same namespaces. Conflict found: " + g + "(gid=" + attr.getValue() + ") and " + commonGIDGroup +
                  "(gid=" + commonGID + ")");
            }
          }
        }
      } catch (AttributeNotExistsException ex) {
        throw new ConsistencyErrorException(ex);
      }
    }

    return commonGID;
  }

  @Override
  public Integer getCommonGIDOfResourcesWithSameNameInSameNamespace(PerunSessionImpl sess,
                                                              List<Resource> resourcesWithSameGroupNameInSameNamespace,
                                                              String gidNamespace, Integer commonGID)
      throws WrongAttributeAssignmentException {
    //If there are no resources, return commonGID from param (it can be null)
    if (resourcesWithSameGroupNameInSameNamespace == null || resourcesWithSameGroupNameInSameNamespace.isEmpty()) {
      return commonGID;
    }
    Utils.notNull(gidNamespace, "gidNamespace");

    Resource commonGIDResource = null;   //only for more verbose exception messages
    for (Resource r : resourcesWithSameGroupNameInSameNamespace) {
      try {
        Attribute attr = sess.getPerunBl().getAttributesManagerBl()
            .getAttribute(sess, r, A_R_UNIX_GID_NAMESPACE + ":" + gidNamespace);
        if (attr.getValue() != null) {
          if (commonGID == null) {
            commonGIDResource = r;
            commonGID = (Integer) attr.getValue();
          } else {
            if (!commonGID.equals(attr.getValue())) {
              throw new ConsistencyErrorException(
                  "There are at least 1 groups/resources with same GroupName in same namespace but with different GID" +
                  " in same namespaces. Conflict found: " + r + "(gid=" + attr.getValue() + ") and " +
                  commonGIDResource + "(gid=" + commonGID + ")");
            }
          }
        }
      } catch (AttributeNotExistsException ex) {
        throw new ConsistencyErrorException(ex);
      }
    }

    return commonGID;
  }

  @Override
  public Integer getFreeGID(PerunSessionImpl sess, Attribute attribute)
      throws AttributeNotExistsException, WrongAttributeAssignmentException {
    Utils.notNull(attribute, "attribute");
    String gidNamespace = attribute.getFriendlyNameParameter();

    Attribute gidRangesAttribute =
        sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_NAMESPACE_GID_RANGES);
    if (gidRangesAttribute.getValue() == null) {
      return 0;
    }
    Map<Integer, Integer> gidRanges;
    try {
      gidRanges = checkAndConvertIDRanges(gidRangesAttribute);
    } catch (WrongAttributeValueException ex) {
      throw new InternalErrorException(
          "Value in GID ranges attribute where we are looking for free gid is not in correct format " +
          gidRangesAttribute, ex);
    }
    if (gidRanges.isEmpty()) {
      return 0;
    }
    List<Integer> allMinimums = gidRanges.keySet().stream().sorted().collect(Collectors.toList());

    List<Integer> allGids = new ArrayList<>();
    Attribute usedGids = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, gidNamespace, A_E_USED_GIDS);

    //return the minimum from all ranges
    if (usedGids.getValue() == null) {
      return allMinimums.get(0);
    } else {
      Map<String, String> usedGidsValue = usedGids.valueAsMap();
      Set<String> keys = usedGidsValue.keySet();

      for (String key : keys) {
        allGids.add(Integer.parseInt(usedGidsValue.get(key)));
      }
    }

    for (Integer minimum : allMinimums) {
      Integer maximum = gidRanges.get(minimum);
      for (int i = minimum; i <= maximum; i++) {
        if (!allGids.contains(i)) {
          return i;
        }
      }
    }

    return null;
  }

  @Override
  public Attribute getGoogleGroupNameNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource)
      throws WrongReferenceAttributeValueException {
    Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
    try {
      Attribute googleGroupNameNamespaceAttribute =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_F_GOOGLE_GROUPS_DOMAIN);
      if (googleGroupNameNamespaceAttribute.getValue() == null) {
        throw new WrongReferenceAttributeValueException(googleGroupNameNamespaceAttribute);
      }
      return googleGroupNameNamespaceAttribute;
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Attribute> getListOfGroupGIDsFromListOfResourceGIDs(PerunSessionImpl sess, List<Attribute> resourceGIDs)
      throws AttributeNotExistsException {
    List<Attribute> groupGIDs = new ArrayList<>();
    if (resourceGIDs == null || resourceGIDs.isEmpty()) {
      return groupGIDs;
    }

    for (Attribute a : resourceGIDs) {
      Attribute groupGID = new Attribute(sess.getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(sess, A_G_UNIX_GID_NAMESPACE + ":" + a.getFriendlyNameParameter()));
      groupGID.setValue(a.getValue());
      groupGIDs.add(groupGID);
    }

    return groupGIDs;
  }

  @Override
  public List<Attribute> getListOfResourceGIDsFromListOfGroupGIDs(PerunSessionImpl sess, List<Attribute> groupGIDs)
      throws AttributeNotExistsException {
    List<Attribute> resourceGIDs = new ArrayList<>();
    if (groupGIDs == null || groupGIDs.isEmpty()) {
      return resourceGIDs;
    }

    for (Attribute a : groupGIDs) {
      Attribute resourceGID = new Attribute(sess.getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(sess, A_R_UNIX_GID_NAMESPACE + ":" + a.getFriendlyNameParameter()));
      resourceGID.setValue(a.getValue());
      resourceGIDs.add(resourceGID);
    }

    return resourceGIDs;
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  @Override
  public boolean getSendRightFromAttributes(PerunSessionImpl sess, Member member, Group group, String booleanAttribute,
                                            String listAttribute) {
    try {
      Attribute sendAs = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, member, group, booleanAttribute);
      if (sendAs.getValue() != null && sendAs.valueAsBoolean()) {
        return true;
      }

      Attribute sendAsGroups = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, listAttribute);
      if (sendAsGroups.getValue() == null) {
        return false;
      }
      List<String> subgroups = sendAsGroups.valueAsList();

      for (String groupId : subgroups) {
        Group subgroup = sess.getPerunBl().getGroupsManagerBl().getGroupById(sess, Integer.parseInt(groupId));
        if (sess.getPerunBl().getGroupsManagerBl().isGroupMember(sess, subgroup, member)) {
          return true;
        }
      }
      return false;

    } catch (MemberGroupMismatchException | GroupNotExistsException e) {
      throw new InternalErrorException(e);
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      throw new ConsistencyErrorException(e);
    }
  }

  @Override
  public Set<String> getSetOfGIDNamespacesWhereFacilitiesHasTheSameGroupNameNamespace(PerunSessionImpl sess,
                                                                                      List<Facility> facilities,
                                                                                      Attribute unixGroupNameNamespace)
      throws WrongAttributeAssignmentException {
    Set<String> gidNamespaces = new HashSet<>();
    if (facilities == null || facilities.isEmpty()) {
      return gidNamespaces;
    }
    Utils.notNull(facilities, "facilities");

    for (Facility f : facilities) {
      Attribute facilityGroupNameNamespace;
      try {
        facilityGroupNameNamespace =
            sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, f, A_F_UNIX_GROUP_NAME_NAMESPACE);
        if (facilityGroupNameNamespace.getValue() != null) {
          //if they are same, save GID-namespace from this facility to hashSet
          if (unixGroupNameNamespace.getFriendlyNameParameter().equals(facilityGroupNameNamespace.getValue())) {
            Attribute facilityGIDNamespace =
                sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, f, A_F_UNIX_GID_NAMESPACE);
            //If facilityGIDNamespace exists and is not null, save to the hashSet of gidNamespaces
            if (facilityGIDNamespace.getValue() != null) {
              gidNamespaces.add((String) facilityGIDNamespace.getValue());
            }
          }
        }
      } catch (AttributeNotExistsException ex) {
        throw new ConsistencyErrorException(ex);
      }
    }

    return gidNamespaces;
  }

  @Override
  public Set<String> getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace(PerunSessionImpl sess,
                                                                                      List<Facility> facilities,
                                                                                      Attribute unixGIDNamespace)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
    Set<String> groupNameNamespaces = new HashSet<>();
    if (facilities == null || facilities.isEmpty()) {
      return groupNameNamespaces;
    }
    Utils.notNull(unixGIDNamespace, "unixGIDNamespace");

    for (Facility f : facilities) {
      Attribute facilityGIDNamespace;
      try {
        facilityGIDNamespace = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, f, A_F_UNIX_GID_NAMESPACE);
        if (facilityGIDNamespace.getValue() != null) {
          //if they are same, save GroupName-namespace from this facility to hashSet
          if (unixGIDNamespace.getFriendlyNameParameter().equals(facilityGIDNamespace.getValue())) {
            Attribute facilityGroupNameNamespace =
                sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, f, A_F_UNIX_GROUP_NAME_NAMESPACE);
            //If facilityGroupNameNamespace exists and is not null, save to the hashSet of gidNamespaces
            if (facilityGroupNameNamespace.getValue() != null) {
              groupNameNamespaces.add((String) facilityGroupNameNamespace.getValue());
            } else {
              throw new WrongReferenceAttributeValueException(unixGIDNamespace, facilityGroupNameNamespace,
                  "Facility has gidNamespace set, but groupNameNamespace not set.");
            }
          }
        }
      } catch (AttributeNotExistsException ex) {
        throw new ConsistencyErrorException(ex);
      }
    }

    return groupNameNamespaces;
  }

  @Override
  public Attribute getUnixGIDNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource)
      throws WrongReferenceAttributeValueException {
    Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
    try {
      Attribute unixGIDNamespaceAttribute =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_F_UNIX_GID_NAMESPACE);
      if (unixGIDNamespaceAttribute.getValue() == null) {
        throw new WrongReferenceAttributeValueException(unixGIDNamespaceAttribute);
      }
      return unixGIDNamespaceAttribute;
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Attribute getUnixGroupNameNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource)
      throws WrongReferenceAttributeValueException {
    Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
    try {
      Attribute unixGroupNameNamespaceAttribute =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_F_UNIX_GROUP_NAME_NAMESPACE);
      if (unixGroupNameNamespaceAttribute.getValue() == null) {
        throw new WrongReferenceAttributeValueException(unixGroupNameNamespaceAttribute);
      }
      return unixGroupNameNamespaceAttribute;
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public User getUserByLoginInNamespace(PerunSession sess, String login, String namespace) {
    String attrName = AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + namespace;
    List<User> usersByLoginInNamespace =
        getPerunBl().getUsersManagerBl().getUsersByAttributeValue(sess, attrName, login);

    if (usersByLoginInNamespace.isEmpty()) {
      return null;
    } else if (usersByLoginInNamespace.size() == 1) {
      return usersByLoginInNamespace.get(0);
    } else {
      throw new ConsistencyErrorException("There is more than 1 login '" + login + "' in namespace " + namespace + ".");
    }
  }

  @Override
  public User getUserFromMessage(PerunSessionImpl sess, String message) {
    List<PerunBean> perunBeans = AuditParser.parseLog(message);

    User user = null;
    UserExtSource userExtSource = null;
    Member member = null;

    for (PerunBean perunBean : perunBeans) {
      if (perunBean instanceof User) {
        user = (User) perunBean;
        break;
      } else if (perunBean instanceof UserExtSource && userExtSource == null) {
        userExtSource = (UserExtSource) perunBean;
      } else if (perunBean instanceof Member && member == null) {
        member = (Member) perunBean;
      }
    }

    //if we don't have object user, try to parse user id from userExtSource (-1 means no userId was defined)
    if (user == null && userExtSource != null && userExtSource.getUserId() != -1) {
      try {
        user = getPerunBl().getUsersManagerBl().getUserById(sess, userExtSource.getUserId());
      } catch (UserNotExistsException ex) {
        LOG.warn("User from UserExtSource {} doesn't exist in Perun. This occurred while parsing message: {}.",
            userExtSource, message);
        return null;
      }
    } else if (user == null && member != null) {
      try {
        user = getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
      } catch (UserNotExistsException ex) {
        LOG.warn("User from Member {} doesn't exist in Perun. This occurred while parsing message: {}.", member,
            message);
        return null;
      }
    }

    return user;
  }

  public boolean hasAccessToWriteToAttributeForAnyGroup(PerunSessionImpl sess, AttributeDefinition attrDef,
                                                        List<Group> groups) {
    Utils.notNull(sess, "perunSessionImpl");
    Utils.notNull(attrDef, "attributeDefinition");
    if (groups == null || groups.isEmpty()) {
      return false;
    }
    for (Group g : groups) {
      if (AuthzResolver.isAuthorizedForAttribute(sess, AttributeAction.WRITE, attrDef, g, true)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasAccessToWriteToAttributeForAnyResource(PerunSessionImpl sess, AttributeDefinition attrDef,
                                                           List<Resource> resources) {
    Utils.notNull(sess, "perunSessionImpl");
    Utils.notNull(attrDef, "attributeDefinition");
    if (resources == null || resources.isEmpty()) {
      return false;
    }
    for (Resource r : resources) {
      if (AuthzResolver.isAuthorizedForAttribute(sess, AttributeAction.WRITE, attrDef, r, true)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean haveRightToWriteAttributeInAnyGroupOrResource(PerunSessionImpl sess, List<Group> groups,
                                                               List<Resource> resources,
                                                               AttributeDefinition groupAttribute,
                                                               AttributeDefinition resourceAttribute) {
    if (groups != null && !groups.isEmpty() && groupAttribute != null) {
      for (Group g : groups) {
        if (AuthzResolver.isAuthorizedForAttribute(sess, AttributeAction.WRITE, groupAttribute, g, false)) {
          return true;
        }
      }
    }

    if (resources != null && !resources.isEmpty() && resourceAttribute != null) {
      for (Resource r : resources) {
        if (AuthzResolver.isAuthorizedForAttribute(sess, AttributeAction.WRITE, resourceAttribute, r, false)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public int haveTheSameAttributeWithTheSameNamespace(PerunSessionImpl sess, Resource resource, Attribute attr)
      throws WrongAttributeAssignmentException {
    Utils.notNull(resource, "resource");
    Utils.notNull(attr, "attr");

    String attributeNamespace = attr.getFriendlyNameParameter();
    if (attributeNamespace == null || attributeNamespace.isEmpty()) {
      throw new InternalErrorException("Attribute has no namespace, this method can't be use.");
    }

    try {
      Attribute testingAttribute =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, attr.getName());
      if (testingAttribute.getValue() == null) {
        return -1;
      } else {
        if (!testingAttribute.getValue().equals(attr.getValue())) {
          return 1;
        }
      }
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    }
    return 0;
  }

  @Override
  public int haveTheSameAttributeWithTheSameNamespace(PerunSessionImpl sess, Group group, Attribute attr)
      throws WrongAttributeAssignmentException {
    Utils.notNull(group, "group");
    Utils.notNull(attr, "attr");

    String attributeNamespace = attr.getFriendlyNameParameter();
    if (attributeNamespace == null || attributeNamespace.isEmpty()) {
      throw new InternalErrorException("Attribute has no namespace, this method can't be use.");
    }

    try {
      Attribute testingAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, attr.getName());
      if (testingAttribute.getValue() == null) {
        return -1;
      } else {
        if (!testingAttribute.getValue().equals(attr.getValue())) {
          return 1;
        }
      }
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    }
    return 0;
  }

  @Override
  public boolean isFQDNValid(PerunSessionImpl sess, String fqdn) {
    if (fqdn == null) {
      return false;
    }
    Matcher fqdnMatcher = FQDN_PATTERN.matcher(fqdn);
    return fqdnMatcher.find();
  }

  /**
   * Return true if gid is valid in ranges defined by Map of ranges, false otherwise. Keys in gidRanges map are minimums
   * of one range and values are maximums. If minimum is same as maximum, such range has only one element.
   *
   * @param gidRanges map of gid ranges (keys = minimums, values = maximums)
   * @param gid       gid which need to be checked if it is in ranges
   * @return
   */
  private boolean isGIDWithinRanges(Map<Integer, Integer> gidRanges, Integer gid) {
    if (gid == null) {
      return false;
    }
    if (gidRanges == null || gidRanges.isEmpty()) {
      return false;
    }

    //Test all valid ranges
    for (Integer minimum : gidRanges.keySet()) {
      Integer maximum = gidRanges.get(minimum);
      //Gid is in range, it is ok
      if (gid >= minimum && gid <= maximum) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean isGroupUnixGIDNamespaceFillable(PerunSessionImpl sess, Group group, Attribute groupUnixGIDNamespace)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    Utils.notNull(group, "group");
    Utils.notNull(groupUnixGIDNamespace, "groupUnixGIDNamespace");

    //Get All Facilities from group
    Set<Facility> facilitiesOfGroup = new HashSet<>();
    List<Resource> resourcesOfGroup = sess.getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
    for (Resource r : resourcesOfGroup) {
      facilitiesOfGroup.add(sess.getPerunBl().getResourcesManagerBl().getFacility(sess, r));
    }

    //Prepare list of gid namespaces of all facilities which have the same groupName namespace like this
    // unixGroupName namespace
    Set<String> groupNameNamespaces =
        this.getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace(sess, new ArrayList<>(facilitiesOfGroup),
            groupUnixGIDNamespace);

    if (!groupNameNamespaces.isEmpty()) {
      for (String s : groupNameNamespaces) {
        try {
          Attribute groupNameNamespace = sess.getPerunBl().getAttributesManagerBl()
              .getAttribute(sess, group, A_G_UNIX_GROUP_NAME_NAMESPACE + ":" + s);
          if (groupNameNamespace.getValue() != null) {
            return true;
          }
        } catch (AttributeNotExistsException ex) {
          throw new ConsistencyErrorException(ex);
        }
      }
    }
    return false;
  }

  @Override
  public boolean isLoginExceptionallyAllowed(String namespace, String login) {
    Utils.notNull(namespace, "namespace to check allowed exceptions for login in");
    if (login == null) {
      return false;
    }

    checkPerunNamespacesMap();

    String exceptionNames = perunNamespaces.get("login-namespace:" + namespace + ":allowedExceptions");
    if (exceptionNames != null) {
      List<String> exceptionNamesList = Arrays.asList(exceptionNames.split("\\s*,\\s*"));
      return exceptionNamesList.contains(login);
    }
    return false;
  }

  @Override
  public boolean isNameOfEmailValid(PerunSessionImpl sess, String email) {
    if (email == null) {
      return false;
    }

    Matcher emailMatcher = Utils.EMAIL_PATTERN.matcher(email);
    return emailMatcher.find();
  }

  @Override
  public boolean isNamespaceEqualsToFacilityUnixGroupNameNamespace(PerunSessionImpl sess, Facility facility,
                                                                   String namespace)
      throws AttributeNotExistsException, WrongAttributeAssignmentException {
    Utils.notNull(facility, "facility");
    Utils.notNull(namespace, "namespace");
    Utils.notNull(sess, "perunSessionImpl");
    Attribute facilityNamespaceAttr = sess.getPerunBl().getAttributesManagerBl()
        .getAttribute(sess, facility, A_F_UNIX_GROUP_NAME_NAMESPACE + ":" + namespace);
    if (facilityNamespaceAttr.getValue() == null) {
      return false;
    }
    if (!namespace.equals(facilityNamespaceAttr.getValue())) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isNamespaceIDGenerationDisabled(PerunSessionImpl sess, String namespace) {
    boolean disableIDGeneration = false;
    Attribute disableIDGenAttr;
    try {
      disableIDGenAttr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess,
          namespace, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":disableIDGeneration");
      disableIDGeneration = disableIDGenAttr.getValue() != null ? disableIDGenAttr.valueAsBoolean() : false;
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      LOG.debug("Not disabling ID generation for  {}", namespace, e);
      // ignore for backwards compatibility
    }
    return disableIDGeneration;
  }

  @Override
  public boolean isUserLoginPermitted(String namespace, String login) {
    Utils.notNull(namespace, "namespace to check unpermited logins in");
    if (login == null) {
      return true;
    }

    checkPerunNamespacesMap();

    String prohibitedNames = perunNamespaces.get("login-namespace:" + namespace + ":reservedNames");
    if (prohibitedNames != null) {
      List<String> prohibitedNamesList = Arrays.asList(prohibitedNames.split("\\s*,\\s*"));
      return !prohibitedNamesList.contains(login) || isLoginExceptionallyAllowed(namespace, login);
    } else {
      //Property not found in our attribute map, so we will use the default hardcoded values instead
      return !UNPERMITTED_NAMES_FOR_USER_LOGINS.contains(login) || isLoginExceptionallyAllowed(namespace, login);
    }
  }

  @Override
  public Map<String, Pair<BigDecimal, BigDecimal>> mergeMemberAndResourceTransferredQuotas(
      Map<String, Pair<BigDecimal, BigDecimal>> resourceQuotas,
      Map<String, Pair<BigDecimal, BigDecimal>> memberResourceQuotas,
      Map<String, Pair<BigDecimal, BigDecimal>> quotasOverride) {
    Map<String, Pair<BigDecimal, BigDecimal>> mergedTransferedQuotas = new LinkedHashMap<>();

    //first go through member-resource quotas values
    for (String path : memberResourceQuotas.keySet()) {
      //override has the highest priority
      if (quotasOverride.containsKey(path)) {
        mergedTransferedQuotas.put(path, quotasOverride.get(path));
      } else {
        //if override not exists, take the original value
        mergedTransferedQuotas.put(path, memberResourceQuotas.get(path));
      }
    }

    //save unique values from resource quotas (not exists in member-resource quotas)
    for (String path : resourceQuotas.keySet()) {
      //skip already saved values, they are not unique
      if (mergedTransferedQuotas.containsKey(path)) {
        continue;
      }

      //take override if exists
      if (quotasOverride.containsKey(path)) {
        mergedTransferedQuotas.put(path, quotasOverride.get(path));
      } else {
        mergedTransferedQuotas.put(path, resourceQuotas.get(path));
      }
    }

    return mergedTransferedQuotas;
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

  @Override
  public Map<String, String> transferQuotasBackToAttributeValue(
      Map<String, Pair<BigDecimal, BigDecimal>> transferedQuotasMap, boolean withMetrics) {
    Map<String, String> attributeQuotasValue = new LinkedHashMap<>();
    //if null or empty, return empty attribute value map for quotas
    if (transferedQuotasMap == null || transferedQuotasMap.isEmpty()) {
      return attributeQuotasValue;
    }

    //every path with quotas transfer step by step
    for (String path : transferedQuotasMap.keySet()) {
      Pair<BigDecimal, BigDecimal> quotas = transferedQuotasMap.get(path);
      BigDecimal softQuotaBD = quotas.getLeft();
      BigDecimal hardQuotaBD = quotas.getRight();

      //Divide decimal till it is still natural number
      //Soft Quota
      String softQuota = "0";
      //Zero means unlimited, stay the same
      if (softQuotaBD.compareTo(BigDecimal.ZERO) != 0) {
        if (withMetrics) {
          softQuota = Utils.bigDecimalBytesToReadableStringWithMetric(softQuotaBD);
        } else {
          softQuota = softQuotaBD.toPlainString();
        }
      }
      //Hard Quota
      String hardQuota = "0";
      //Zero means unlimited, stay the same
      if (hardQuotaBD.compareTo(BigDecimal.ZERO) != 0) {
        if (withMetrics) {
          hardQuota = Utils.bigDecimalBytesToReadableStringWithMetric(hardQuotaBD);
        } else {
          hardQuota = hardQuotaBD.toPlainString();
        }
      }

      //add softQuota and hardQuota to result (50T:60T)
      attributeQuotasValue.put(path, softQuota + ":" + hardQuota);
    }
    return attributeQuotasValue;
  }

  /**
   * Shared logic for purpose of login generation
   */
  public static class LoginGenerator {

    /**
     * Generate login for user using his name and joining function
     *
     * @param user     User to get data from
     * @param function Function to join fist/lastName to login
     * @return generated login
     */
    public String generateLogin(User user, LoginGeneratorFunction function) {

      String firstName = user.getFirstName();
      String lastName = user.getLastName();

      // get only first part of first name and remove spec. chars
      if (firstName != null && !firstName.isEmpty()) {
        firstName = ModulesUtilsBlImpl.normalizeStringForLogin(firstName.split(" ")[0]);
      }

      // get only last part of last name and remove spec. chars
      if (lastName != null && !lastName.isEmpty()) {
        List<String> names = Arrays.asList(lastName.split(" "));
        lastName = names.get(names.size() - 1);
        lastName = ModulesUtilsBlImpl.normalizeStringForLogin(lastName.split(" ")[0]);
      }

      return function.generateLogin(firstName, lastName);

    }

    /**
     * Define joining function for anonymous classes
     */
    public interface LoginGeneratorFunction {

      /**
       * Generate login for user using his name. Implementation must handle empty/null input on both fields.
       *
       * @param firstName
       * @param lastName
       * @return generated login
       */
      String generateLogin(String firstName, String lastName);

    }

  }


}
