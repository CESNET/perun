package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.QuotaNotInAllowedLimitException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Michal Stava <stavamichal@gmail.com>
 *
 * ModuleUtils interface.
 * There are methods for help with modules.
 *
 */
public interface ModulesUtilsBl {

	/**
	 * If attribute "def_facility_unixGroup_namespace" is "null" return false.
	 * If value of attribute "def_facility_unixGroup_namespace" is not same like "namespace", return false.
	 * Else return true.
	 *
	 * Facility, sess and namespace can't be null, otherwise throw InternalErrorException
	 *
	 * @param sess
	 * @param facility
	 * @param namespace
	 * @return
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	boolean isNamespaceEqualsToFacilityUnixGroupNameNamespace(PerunSessionImpl sess, Facility facility, String namespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method get if the resource has the same attribute "attr" with the same namespace and same or different values
	 *
	 * If return 0 then there exists for the resource the same attribute with the same value
	 * if return more than 0 then there exists for the resource the same attribute with different value
	 * if return less than 0 then there not exists for the resource the same attribute
	 *
	 * @param sess
	 * @param resource
	 * @param attr any resource attribute with namespace which will be use for comparing
	 * @return
	 * @throws InternalErrorException if something is not correct
	 * @throws WrongAttributeAssignmentException if attribute name is not RESOURCE attribute
	 */
	int haveTheSameAttributeWithTheSameNamespace(PerunSessionImpl sess, Resource resource, Attribute attr) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * This method get if the group has the same attribute "attr" with the same namespace and same or different values
	 *
	 * If return 0 then there exists for the group the same attribute with the same value
	 * if return more than 0 then there exists for the group the same attribute with different value
	 * if return less than 0 then there not exists for the group the same attribute
	 *
	 * @param sess
	 * @param group
	 * @param attr any group attribute with namespace which will be use for comparing
	 * @return
	 * @throws InternalErrorException if something is not correct
	 * @throws WrongAttributeAssignmentException if attribute name is not GROUP attribute
	 */
	int haveTheSameAttributeWithTheSameNamespace(PerunSessionImpl sess, Group group, Attribute attr) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * This method is looking for exactly one commonGID for all objects in list.
	 * If commonGID in parameter is not null, it checks that all objects in list have this one set as gid.
	 *
	 * If list of groups is empty, return always commonGID from parameter (it can be null).
	 * If there are more than one different commonGIDs, throw ConsistencyErrorException
	 *
	 * @param sess
	 * @param resourcesWithSameGroupNameInSameNamespace
	 * @param nameOfAttribute
	 * @param commonGID if any common gid already exists (for example from Resources) use it to compare, null in other case
	 * @return common GID, if no exists return null
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Integer getCommonGIDOfResourcesWithSameNameInSameNamespace(PerunSessionImpl sess, List<Resource> resourcesWithSameGroupNameInSameNamespace, String nameOfAttribute, Integer commonGID) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Check if list of gids in arguments is free in the namespace
	 *
	 * @param sess
	 * @param attribute list of unixGIDs-namespace attribute with value
	 * @param user handler of atribute
	 *
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException if minGid or maxGid is null
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeValueException
	 */
	void checkIfListOfGIDIsWithinRange(PerunSessionImpl sess,User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongAttributeValueException;

	/**
	 * This method is looking for exactly one commonGID for all objects in list.
	 * If commonGID in parameter is not null, it checks that all objects in list have this one set as gid.
	 *
	 * If list of groups is empty, return always commonGID from parameter (it can be null).
	 * If there are more than one different commonGIDs, throw ConsistencyErrorException
	 *
	 * @param sess
	 * @param groupsWithSameGroupNameInSameNamespace
	 * @param nameOfAttribute
	 * @param commonGID if any common gid already exists (for example from Resources) use it to compare, null in other case
	 * @return common GID, if no exists return null
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Integer getCommonGIDOfGroupsWithSameNameInSameNamespace(PerunSessionImpl sess, List<Group> groupsWithSameGroupNameInSameNamespace, String nameOfAttribute, Integer commonGID) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get free gid for resource or group.
	 *
	 * @param sess
	 * @param attribute group or resource unixGID-namespace attribute
	 * @return if 0 there probably isn't maxGID or minGID, if null there is no free gid, other less or more than 0 gid
	 *
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	Integer getFreeGID(PerunSessionImpl sess, Attribute attribute) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Check if gid in arguments is free in the namespace
	 *
	 * @param sess
	 * @param attribute group or resource unixGID-namespace attribute with value
	 *
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException if minGid or maxGid is null
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeValueException
	 */
	void checkIfGIDIsWithinRange(PerunSessionImpl sess, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongAttributeValueException;

	/**
	 * Return true if i have right on any of groups or resources to WRITE the attribute
	 *
	 * @param sess
	 * @param groups lists of groups to search
	 * @param resources lists of resources to search
	 * @param groupAttribute AttributeDefinition for testing Write privileges for groups
	 * @param resourceAttribute AttributeDefinition for testing Write privileges for resource
	 *
	 * @return true if such group or resource exists, false if not
	 * @throws InternalErrorException if something is not correct or attribute is null
	 */
	boolean haveRightToWriteAttributeInAnyGroupOrResource(PerunSessionImpl sess, List<Group> groups, List<Resource> resources, AttributeDefinition groupAttribute, AttributeDefinition resourceAttribute) throws InternalErrorException;

	/**
	 * Take list of groupGID attributes and return list of the same GID attributes only for resource (with the same original value)
	 *
	 * @param sess
	 * @param groupGIDs list of attributes type of Group UnixGID
	 * @return list of attribute type of Resource UnixGID with same values like in original list
	 * @throws InternalErrorException if something is not correct or attribute is null
	 * @throws AttributeNotExistsException
	 */
	List<Attribute> getListOfResourceGIDsFromListOfGroupGIDs(PerunSessionImpl sess, List<Attribute> groupGIDs) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Take list of resourceGID attributes and return list of the same GID attributes only for group (with the same original value)
	 *
	 * @param sess
	 * @param resourceGIDs list of attributes type of Resource UnixGID
	 * @return list of attribute type of Group UnixGID with same values like in original list
	 * @throws InternalErrorException if something is not correct or attribute is null
	 * @throws AttributeNotExistsException
	 */
	List<Attribute> getListOfGroupGIDsFromListOfResourceGIDs(PerunSessionImpl sess, List<Attribute> resourceGIDs) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get list of facilities and namespace of group or resource attribute unixGroupName-namespace and
	 * if any facility has unixGroupName-namespace with same value like this namespace of unixGroupNameNamespace attribute
	 * then get unixGID-namespace of this facility and save it to the hashSet of these namespaces.
	 *
	 * @param sess
	 * @param facilities list of facilities
	 * @param unixGroupNameNamespace unixGroupName-namespace attribute
	 * @return list of namespaces
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongReferenceAttributeValueException
	 */
	Set<String> getSetOfGIDNamespacesWhereFacilitiesHasTheSameGroupNameNamespace(PerunSessionImpl sess, List<Facility> facilities, Attribute unixGroupNameNamespace) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Get list of facilities and namespace of group or resource attribute unixGID-namespace and
	 * if any facility has unixGID-namespace with same value like this namespace of unixGIDNamespace attribute
	 * then get unixGroupName-namespace of this facility and save it to the hashSet of these namespaces.
	 *
	 * @param sess
	 * @param facilities list of facilities
	 * @param unixGroupNameNamespace unixGroupName-namespace attribute
	 * @return list of namespaces
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongReferenceAttributeValueException
	 */
	Set<String> getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace(PerunSessionImpl sess, List<Facility> facilities, Attribute unixGroupNameNamespace) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of groupName attribute is not reserved String.
	 * If not, its ok.
	 * If yes, throw WrongAttributeValueException.
	 * If attribute is null, then it's ok.
	 * For reserved unix group names this method firstly tries to read perun-namespaces.properties file.
	 * If there is no property in this file, it reads the default hardcoded values.
	 *
	 * @param groupNameAttribute unixGroupName-namespace
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 */
	void checkReservedUnixGroupNames(Attribute groupNameAttribute) throws InternalErrorException, WrongAttributeValueException;

	/**
	 * Check if value of login attribute is unpermitted.
	 * If not, its ok.
	 * If yes, throw WrongAttributeValueException.
	 * If attribute is null, then it's ok.
	 * For unpermitted user logins this method firstly tries to read perun-namespaces.properties file.
	 * If there is no property in this file, it reads the default hardcoded values.
	 *
	 * @param loginAttribute login-namespace
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 */
	void checkUnpermittedUserLogins(Attribute loginAttribute) throws InternalErrorException, WrongAttributeValueException;

	/**
	 * Get value of attribute A_F_Def_unixGroupName-Namespace
	 * If this value is null, throw WrongReferenceAttributeValueException
	 *
	 * @param sess
	 * @param resource
	 * @return namespace if is not null
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException if value of unixGroupName-namespace attribute is null
	 */
	Attribute getUnixGroupNameNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource) throws InternalErrorException, WrongReferenceAttributeValueException;

	/**
	 * Get value of attribute A_F_Def_googleGroupName-Namespace
	 * If this value is null, throw WrongReferenceAttributeValueException
	 *
	 * @param sess
	 * @param resource
	 * @return namespace if is not null
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException if value of googleGroupName-namespace attribute is null
	 */
	Attribute getGoogleGroupNameNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource) throws InternalErrorException, WrongReferenceAttributeValueException;

	/**
	 * Get value of attribute A_F_Def_unixGID-Namespace
	 * If this value is null, throw WrongReferenceAttributeValueException
	 *
	 * @param sess
	 * @param resource
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 */
	Attribute getUnixGIDNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource) throws InternalErrorException, WrongReferenceAttributeValueException;

	/**
	 * This method return true if there exists some Facility (get from assigned resources) where is
	 * facility_unixGID-namespace attribute with value same like group_unixGID-namespace namespace and
	 * if the group has unixGroupName-namespace with notNull value in the same namespace like value of attribute
	 * facility_unixGroupName-namespace.
	 * Return false if not.
	 *
	 * @param sess
	 * @param group the group
	 * @param groupUnixGIDNamespace attribute of the group
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	boolean isGroupUnixGIDNamespaceFillable(PerunSessionImpl sess, Group group, Attribute groupUnixGIDNamespace) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * Check if shell has the right format.
	 * Use regex ^(/[-_a-zA-Z0-9]+)+$
	 *
	 * @param shell value of shell
	 * @param attribute attribute which need to test shell (needed for right exception)
	 * @throws WrongAttributeValueException if shell has bad format
	 */
	void checkFormatOfShell(String shell, Attribute attribute) throws WrongAttributeValueException;

	/**
	 * Checks name of an email by standard pattern and returns true, if it is valid.
	 *
	 * @param sess
	 * @param email name of the email
	 *
	 * @return true if the name of email is valid
	 */
	boolean isNameOfEmailValid(PerunSessionImpl sess, String email);

	/**
	 * Check if value of attribute (friendlyName) suits regex in perun-namespaces.properties file.
	 * If yes, nothing happens.
	 * If no, WrongAttributeValueException is thrown.
	 * If there is no property record in the properties file, defaultRegex is used instead (if not null).
	 *
	 * @param attribute
	 * @param defaultRegex Default regex to be used if regex is not found in the configuration file.
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 */
	void checkAttributeRegex(Attribute attribute, String defaultRegex) throws InternalErrorException, WrongAttributeValueException;

	/**
	 * Check if quotaToCheck is in limit of limitQuota.
	 * That means that every key of quotaToCheck map must exist in limitQuota and if such key exists, softQuota (left value)
	 * of quotaToCheck map need to be lower or same as softQuota in limitQuota of the same key and the same must be in effect
	 * for hardQuota (right value) of both maps.
	 *
	 * It uses transferred quotas so it can be used for files and data same way. 0 means unlimited. If no quota is allowed,
	 * the value for volume shouldn't be in limit quota at all.
	 *
	 * Example of possible limitations:
	 * quotaToCheck -> ( '/var/log/something' => '10000:50000', '/sys/something' => '0:0', '/tmp/something' => '0:0' )
	 * quotaToLimit -> ( '/var/log/something' => '10000:50000', '/sys/something' => '50:0', '/cache/something' => '0:0' )
	 * ---------------
	 * '/var/log/something' => '10000:50000' -- this value is correct, exists in limit quota and both quotas are in limit
	 * '/sys/something' => '0:0' -- this is not correct, 0 means unlimited quota and we have limit 50 for softQuota (not in limit)
	 * '/tmp/something' => '0:0' -- this value is not correct, because this path is not set in limit quota at all
	 * '/cache/something' => '0:0' -- there is no problem, that limit quota has some limited values which are not in quotasToCheck
	 *
	 * @param quotaToCheck map of volumes (as keys) and pairs of soft quota (left value) and hard quota (right value) for this volume
	 *                     we want to check this map against the limit one
	 * @param limitQuota map of volumes (as keys) and pairs of soft quota (left value) and hard quota (right value) for this volume
	 *                   we want to use this map as limit one
	 *
	 * @throws QuotaNotInAllowedLimitException throw this exception, if check quota is not in limit of limit quota
	 * @throws InternalErrorException if any of inputs is in unexpected format
	 */
	void checkIfQuotasIsInLimit(Map<String, Pair<BigDecimal, BigDecimal>> quotaToCheck, Map<String, Pair<BigDecimal, BigDecimal>> limitQuota) throws QuotaNotInAllowedLimitException, InternalErrorException;

	/**
	 * Check if value in quotas attribute are in the right format.
	 * Also transfer and return data in suitable container.
	 *
	 * Example of correct quotas with metrics: key=/path/to/volume , value=50T:0
	 * Example of correct quotas without metrics: key=/path/to/volume , value=1000:2000
	 *
	 * Example of suitable format: key=/path/to/volume, softQuota=50000000000000, hradQuota=0
	 *
	 * Left part of value is softQuota, right part after delimeter ':' is hardQuota.
	 * SoftQuota must be less or equals to hardQuota. '0' means unlimited.
	 *
	 * @param quotasAttribute attribute with paths and quotas (Map<String, String>) (data or files quotas)
	 * @param firstPlaceholder first attribute placeholder (can't be null, mandatory)
	 * @param secondPlaceholder second attribute placeholder (can be null if not exists)
	 * @param withMetrics true if metrics are used, false if not
	 *
	 * @return map with path in key and pair with <softQuota, hardQuota> in big decimal
	 *
	 * @throws InternalErrorException if first mandatory placeholder is null
	 * @throws WrongAttributeValueException if something is wrong in format of attribute
	 */
	Map<String, Pair<BigDecimal, BigDecimal>> checkAndTransferQuotas(Attribute quotasAttribute, PerunBean firstPlaceholder, PerunBean secondPlaceholder, boolean withMetrics) throws InternalErrorException, WrongAttributeValueException;

	/**
	 * Reverse method for checkAndTransferQuotas method.
	 * Take transfered map and create again not transfered map.
	 * From path=/path/to/ , softQuota=50000, hardQuota=0
	 * To path=/path/to/ , value=50M:0
	 * (Do not check again!)
	 *
	 * @param transferedQuotasMap
	 * @param withMetrics if true, then use metrics, if not, do not convert data to metrics
	 *
	 * @return not transfered map for saving to attribute value
	 * @throws InternalErrorException
	 */
	Map<String, String> transferQuotasBackToAttributeValue(Map<String, Pair<BigDecimal, BigDecimal>> transferedQuotasMap, boolean withMetrics) throws InternalErrorException;

	/**
	 * Merge resource default quotas and member-resource specific quotas together. Use override if exists instead.
	 * Paths are always unique, quotas are merged. (soft together and hard together)
	 *
	 * Together means by priority:
	 * - override has the highest priority but is it used only if path exists in resource or resource-member quotas
	 * - member-resource quotas has the second highest priority if override not exists
	 * - resource quotas are used only if contain unique path (path not exists as member-resource or as override)
	 *
	 * @param resourceQuotas transferred map with default resource quotas
	 * @param memberResourceQuotas transferred map with member-resource specific quotas
	 * @param quotasOverride transfered map with all manual overrides of quotas
	 *
	 * @return merged quotas transferred map
	 */
	Map<String,Pair<BigDecimal, BigDecimal>> mergeMemberAndResourceTransferredQuotas(Map<String, Pair<BigDecimal, BigDecimal>> resourceQuotas, Map<String, Pair<BigDecimal, BigDecimal>> memberResourceQuotas, Map<String, Pair<BigDecimal, BigDecimal>> quotasOverride);

	/**
	 * Count all quotas for user.
	 * Every record in list is merged quotas map with value from resource attribute and resource-member attribute where user has allowed member.
	 *
	 * Quotas for same paths are sum together. If value is '0' then result is also '0', because 0 means unlimited.
	 *
	 * Example: /path/to/volume 30G:50G , /path/to/volume 40G:0 => /path/to/volume 70G:0
	 *
	 * @param allUserQuotas list
	 *
	 * @return counted user facility quotas
	 */
	Map<String, Pair<BigDecimal, BigDecimal>> countUserFacilityQuotas(List<Map<String, Pair<BigDecimal, BigDecimal>>> allUserQuotas);

	/**
	 * Checks fully qualified domain name and returns true, if it is valid.
	 *
	 * @param sess
	 * @param fqdn fully qualified domain name
	 *
	 * @return true if the fqdn is valid
	 */
	boolean isFQDNValid(PerunSessionImpl sess, String fqdn);

	/**
	 * Get object User from Perun audit message.
	 * Try to find it by different objects in this order: User, UserExtSource, Member.
	 * Always return first occurrence of User using objects above:
	 * - if user has been found, return it (do not look for another user)
	 * - if no user has been found, try to find UserExtSource and get user from it
	 * - if no UserExtSource has been found, try to find Member and get user from it
	 * - if there is no such object, return null
	 *
	 * @param message audit message in machine format (with characters '<' as brackets)
	 *
	 * @return user if found or null if not found
	 * @throws InternalErrorException
	 */
	User getUserFromMessage(PerunSessionImpl sess, String message) throws InternalErrorException;

	/**
	 * Take attribute with gidRanges value (map of strings) and check if all records of this value are valid ranges.
	 * Valid range is from minimum to maximum where minimum must be less or equal to maximum. If minimum and maximum
	 * are equal, the interval has exactly one element. If all ranges are valid, it also checks if there is any
	 * overlap between ranges. If yes, it throws an error.
	 *
	 * If every check is ok, it will return map of integer values where records are ranges, in keys are minimums of
	 * these ranges, in values are maximum of these ranges and there are no overlaps between any two ranges in map.
	 *
	 * Attribute in parameter of this method can't be null but can have null value which returns empty map.
	 *
	 * If there are empty or null elements (value or key) in map it will throw an exception.
	 * If any of minimums and maximums is not a number (convertible to Java Integer) it will throw an exception.
	 * If any of minimums is less than 1 it also throw an exception.
	 * If one of ranges is not correct range (minimum is not less or equal to maximum) it will throw an exception.
	 * If there are any overlaps between two or more ranges, it will throw an exception - ex. 100-102 and 101-103.
	 *
	 * Example of valid format of range:
	 * key='100', value='1000' - range from 100 to 1000 included
	 * key='1', value ='1' - range with exactly one gid with number "1"
	 *
	 * @param gidRangesAttribute attribute with gid ranges value (map of ranges as strings)
	 *
	 * @return map of valid ranges without overlaps
	 *
	 * @throws InternalErrorException if attribute in parameter of method is null
	 * @throws WrongAttributeValueException if value of attribute in parameter does not contain valid ranges without overlaps
	 */
	Map<Integer, Integer> checkAndConvertGIDRanges(Attribute gidRangesAttribute) throws InternalErrorException, WrongAttributeValueException;
}
