package cz.metacentrum.perun.core.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ext source implementation for LDAP.
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @author Jan Zverina <zverina@cesnet.cz>
 */
public class ExtSourceLdap extends ExtSource implements ExtSourceApi {

	protected Map<String, String> mapping;

	protected final static Logger log = LoggerFactory.getLogger(ExtSourceLdap.class);

	protected DirContext dirContext = null;
	protected String filteredQuery = null;

	protected DirContext getContext() throws InternalErrorException {
		if (dirContext == null) {
			initContext();
		}
		return dirContext;
	}

	protected static PerunBlImpl perunBl;

	// filled by spring (perun-core.xml)
	public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
		perunBl = perun;
		return perun;
	}

	public List<Map<String,String>> findSubjectsLogins(String searchString) throws InternalErrorException {
		return findSubjectsLogins(searchString, 0);
	}

	public List<Map<String,String>> findSubjectsLogins(String searchString, int maxResults) throws InternalErrorException {
		// Prepare searchQuery
		// attributes.get("query") contains query template, e.g. (uid=?), ? will be replaced by the searchString
		String query = (String) getAttributes().get("query");
		if (query == null) {
			throw new InternalErrorException("query attributes is required");
		}
		query = query.replaceAll("\\?", searchString);

		String base = (String) getAttributes().get("base");
		if (base == null) {
			throw new InternalErrorException("base attributes is required");
		}
		return this.querySource(query, base, maxResults);
	}

	public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException {
		// Prepare searchQuery
		// attributes.get("loginQuery") contains query template, e.g. (uid=?), ? will be replaced by the login
		String query = (String) getAttributes().get("loginQuery");
		if (query == null) {
			throw new InternalErrorException("loginQuery attributes is required");
		}
		query = query.replaceAll("\\?", login);

		String base = (String) getAttributes().get("base");
		if (base == null) {
			throw new InternalErrorException("base attributes is required");
		}

		List<Map<String, String>> subjects = this.querySource(query, base, 0);

		if (subjects.size() > 1) {
			throw new SubjectNotExistsException("There are more than one results for the login: " + login);
		}

		if (subjects.size() == 0) {
			throw new SubjectNotExistsException(login);
		}

		return subjects.get(0);
	}

	public String getGroupSubjects(PerunSession sess, Group group, String status, List<Map<String, String>> subjects) throws InternalErrorException {
		NamingEnumeration<SearchResult> results = null;
		// String for detection, which type of synchronization this will be (full or modified)
		String typeOfSynchronization = GroupsManager.GROUP_SYNC_STATUS_FULL;
		// Check if we need to save new value of LDAP modify timestamp of group
		boolean saveCheck = false;

		// Get all group attributes and store them to map (info like query, time interval etc.)
		List<cz.metacentrum.perun.core.api.Attribute> groupAttributes = perunBl.getAttributesManagerBl().getAttributes(sess, group);
		Map<String, String> groupAttributesMap = new HashMap<>();
		for (cz.metacentrum.perun.core.api.Attribute attr: groupAttributes) {
			String value = BeansUtils.attributeValueToString(attr);
			String name = attr.getName();
			groupAttributesMap.put(name, value);
		}

		// Get the LDAP group name
		String ldapGroupName = groupAttributesMap.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
		// Get optional filter for members filtering
		String filter = groupAttributesMap.get(GroupsManager.GROUPMEMBERSFILTER_ATTRNAME);
		// If attribute filter not exists, use optional default filter from extSource definition
		if(filter == null) filter = filteredQuery;
		// Get modifyTimestamp of Group
		String groupModifyTimestamp = groupAttributesMap.get(GroupsManager.GROUPCHANGEDETECTION_ATTRNAME);

		String newModifyTimestamp = null;
		try {
			log.info("LDAP External Source: search for modifyTimestamp of ldap group: [{}]", ldapGroupName);

			// If modifyTimestamp attribute exists, compare it with the one from last synchronization cycle
			if (getAttributes().containsKey("timestampAttribute")) {
				// Get name of timestamp attribute
				String modifyTimestampName = getAttributes().get("timestampAttribute");
				// Get format of modifyTimestamp from definition of extSource
				String modifyTimestampFormat = getAttributes().get("timestampFormat");
				if (modifyTimestampFormat == null) {
					throw new InternalErrorException("LDAP: Format of modifyTimestamp is not defined. Declare timestampFormat in definition of extSource.");
				}

				// Get the modify timestamp attribute of group
				Attributes attrsModifyTimestamp = getContext().getAttributes(ldapGroupName, new String[] {modifyTimestampName});
				Attribute modifyTimestampAttribute = attrsModifyTimestamp.get(modifyTimestampName);

				// Get string from modify timestamp attribute gained from external source
				if (modifyTimestampAttribute != null) {
					newModifyTimestamp = (String) modifyTimestampAttribute.get();

					// If obtained modifyTimestamp is equal to the stored one, get list of modified members from the extSource
					if (newModifyTimestamp != null && newModifyTimestamp.equals(groupModifyTimestamp)) {

						// If its Lightweight synchronization, we don't need to gain data from extSource in this case
						if (status.equals(GroupsManager.GROUP_SYNC_STATUS_LIGHTWEIGHT)) {
							return GroupsManager.GROUP_SYNC_STATUS_MODIFIED;
						}

						// If start of last successful synchronization is stored in attribute, get modified members from this date
						String startOfLastSuccessSync = groupAttributesMap.get(GroupsManager.GROUPSTARTOFLASTSUCCESSSYNC_ATTRNAME);
						if (startOfLastSuccessSync != null) {
							try {
								Date startDate = BeansUtils.getDateFormatter().parse(startOfLastSuccessSync);
								// Create string with filter to detect modified records (e.g. (modifyTimestamp>=20170414220836Z))
								SimpleDateFormat sdf = new SimpleDateFormat(modifyTimestampFormat);
								String dateForFilter = sdf.format(startDate);
								String additionToFilter = "(" + modifyTimestampName + ">=" + dateForFilter + ")";
								if (filter == null) {
									filter = additionToFilter;
								} else {
									String tmp = "(&" + additionToFilter + filter + ")";
									filter = tmp;
								}
								log.debug("LDAP: Filter is {}", filter);

								typeOfSynchronization = GroupsManager.GROUP_SYNC_STATUS_MODIFIED;
							} catch (ParseException e) {
								// Should not happen
								log.error("LDAP: Attribute " + GroupsManager.GROUPSTARTOFLASTSUCCESSSYNC_ATTRNAME + " has bad format: {}", startOfLastSuccessSync);
								throw new InternalErrorException("Error in parsing of date with start of last success sync: " + startOfLastSuccessSync, e);
							}
						}
					} else {
						// Set check of saving new value of modify timestamp to true
						saveCheck = true;
					}
				} else {
					throw new InternalErrorException("Attribute with timestamp of group was not obtained from external source.");
				}
			}
			// Gain subjects from extSource
			queryForGroupSubjectsExtSource(ldapGroupName, filter, subjects);

		} catch (NamingException e) {
			log.error("LDAP exception during modifyTimestamp query '{}'", ldapGroupName);
			throw new InternalErrorException("Entry '" + ldapGroupName + "' was not found in LDAP.", e);
		} finally {
			try {
				if (results != null) { results.close(); }
			} catch (Exception e) {
				log.error("LDAP exception during closing result, while running query for group '{}'", ldapGroupName);
				throw new InternalErrorException(e);
			}
		}

		if (saveCheck) {
			log.info("Saving new LDAP Group modify timestamp {}", newModifyTimestamp);
			// Save new LDAP modify timestamp to group
			try {
				AttributeDefinition attributeDefinition = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, GroupsManager.GROUPCHANGEDETECTION_ATTRNAME);
				cz.metacentrum.perun.core.api.Attribute timestamp = new cz.metacentrum.perun.core.api.Attribute(attributeDefinition);
				timestamp.setValue(newModifyTimestamp);
				perunBl.getAttributesManagerBl().setAttributeInNestedTransaction(sess, group, timestamp);
			} catch (WrongAttributeValueException | WrongAttributeAssignmentException | WrongReferenceAttributeValueException | AttributeNotExistsException e) {
				throw new InternalErrorException("There is a problem with saving of new modify timestamp of group. New modify timestamp is " + newModifyTimestamp + ".");
			}
		}

		return typeOfSynchronization;
	}

	private void queryForGroupSubjectsExtSource(String ldapGroupName, String filter, List<Map<String, String>> subjects) throws InternalErrorException {
		List<String> ldapGroupSubjects = new ArrayList<String>();

		try {
			log.trace("LDAP External Source: searching for group subjects [{}]", ldapGroupName);

			String attrName;
			if (getAttributes().containsKey("memberAttribute")) {
				attrName = getAttributes().get("memberAttribute");
			} else {
				// Default value
				attrName = "uniqueMember";
			}

			List<String> retAttrs = new ArrayList<String>();
			retAttrs.add(attrName);
			String[] retAttrsArray = retAttrs.toArray(new String[retAttrs.size()]);
			Attributes attrs = getContext().getAttributes(ldapGroupName, retAttrsArray);

			Attribute ldapAttribute = null;
			// Get the list of returned groups, should be only one
			if (attrs.get(attrName) != null) {
				// Get the attribute which holds group subjects
				ldapAttribute = attrs.get(attrName);
			}
			if (ldapAttribute != null) {
				// Get the DNs of the subjects
				for (int i=0; i < ldapAttribute.size(); i++) {
					String ldapSubjectDN = (String) ldapAttribute.get(i);
					ldapGroupSubjects.add(ldapSubjectDN);
					log.trace("LDAP External Source: found group subject [{}].", ldapSubjectDN);
				}
			}

			// Now query LDAP again and search for each subject
			for (String ldapSubjectName : ldapGroupSubjects) {
				subjects.addAll(this.querySource(filter, ldapSubjectName, 0));
			}
		} catch (NamingException e) {
			log.error("LDAP exception during running query '{}'", ldapGroupName);
			throw new InternalErrorException("Entry '"+ldapGroupName+"' was not found in LDAP." , e);
		}
	}

	protected void initContext() throws InternalErrorException {
		// Load mapping between LDAP attributes and Perun attributes
		Hashtable<String,String> env = new Hashtable<String,String>();

		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		if (getAttributes().containsKey("referral")) {
			env.put(Context.REFERRAL, getAttributes().get("referral"));
		}
		if (getAttributes().containsKey("url")) {
			env.put(Context.PROVIDER_URL, getAttributes().get("url"));
		} else {
			throw new InternalErrorException("url attributes is required");
		}
		if (getAttributes().containsKey("user")) {
			env.put(Context.SECURITY_PRINCIPAL, getAttributes().get("user"));
		}
		if (getAttributes().containsKey("password")) {
			env.put(Context.SECURITY_CREDENTIALS, getAttributes().get("password"));
		}

		if (getAttributes().containsKey("filteredQuery")) {
			filteredQuery = getAttributes().get("filteredQuery");
		}

		try {
			// ldapMapping contains entries like: firstName={givenName},lastName={sn},email={mail}
			if (getAttributes().get("ldapMapping") == null) {
				throw new InternalErrorException("ldapMapping attributes is required");
			}
			String ldapMapping[] = (getAttributes().get("ldapMapping")).trim().split(",\n");
			mapping = new HashMap<>();
			for (String entry: ldapMapping) {
				String values[] = entry.trim().split("=", 2);
				mapping.put(values[0].trim(), values[1].trim());
			}

			this.dirContext = new InitialDirContext(env);
		} catch (NamingException e) {
			log.error("LDAP exception during creating the context.");
			throw new InternalErrorException(e);
		}
	}

	protected Map<String,String> getSubjectAttributes(Attributes attributes) throws InternalErrorException {
		Pattern pattern = Pattern.compile("\\{([^\\}])*\\}");
		Map<String, String> map = new HashMap<String, String>();

		for (String key: mapping.keySet()) {
			// Get attribute value and substitute all {} in the string
			Matcher matcher = pattern.matcher(mapping.get(key));
			String value = mapping.get(key);

			// Find all matches
			while (matcher.find()) {
				// Get the matching string
				String ldapAttributeNameRaw = matcher.group();
				String ldapAttributeName = ldapAttributeNameRaw.replaceAll("\\{([^\\}]*)\\}", "$1"); // ldapAttributeNameRaw is encapsulate with {}, so remove it
				// Replace {ldapAttrName} with the value
				value = value.replace(ldapAttributeNameRaw, getLdapAttributeValue(attributes, ldapAttributeName));
				log.trace("ExtSourceLDAP: Retrieved value {} of attribute {} for {} and storing into the key {}.", new Object[]{value, ldapAttributeName, ldapAttributeNameRaw, key});
			}

			map.put(key, value);
		}

		return map;
	}

	protected String getLdapAttributeValue(Attributes attributes, String ldapAttrNameRaw)  throws InternalErrorException {
		String ldapAttrName;
		String rule = null;
		Matcher matcher = null;
		String attrValue = "";

		// Check if the ldapAttrName contains regex
		if (ldapAttrNameRaw.contains("|")) {
			int splitter = ldapAttrNameRaw.indexOf('|');
			ldapAttrName = ldapAttrNameRaw.substring(0,splitter);
			rule = ldapAttrNameRaw.substring(splitter+1, ldapAttrNameRaw.length());
		} else {
			ldapAttrName = ldapAttrNameRaw;
		}

		// Check if the ldapAttrName contains specification of the value index
		int attributeValueIndex = -1;
		if (ldapAttrNameRaw.contains("[")) {
			Pattern indexPattern = Pattern.compile("^(.*)\\[([0-9]+)\\]$");
			Matcher indexMatcher = indexPattern.matcher(ldapAttrNameRaw);
			if (indexMatcher.find()) {
				ldapAttrName = indexMatcher.group(1);
				attributeValueIndex = Integer.parseInt(indexMatcher.group(2));
			} else {
				throw new InternalErrorException("Wrong attribute name format for attribute: " + ldapAttrNameRaw + ", it should be name[0-9+]");
			}
		}

		// Mapping to the LDAP attribute
		Attribute attr = attributes.get(ldapAttrName);
		if (attr != null) {
			// There could be more than one value in the attribute. Separator is defined in the AttributesManagerImpl
			for (int i = 0; i < attr.size(); i++) {
				if (attributeValueIndex != -1 && attributeValueIndex != i) {
					// We want only value on concrete index, so skip the other ones
					continue;
				}

				String tmpAttrValue = "";
				try {
					if(attr.get() instanceof byte[]) {
						// It can be byte array with cert or binary file
						char[] encodedValue = Base64Coder.encode((byte[]) attr.get());
						tmpAttrValue = new String(encodedValue);
					} else {
						tmpAttrValue = (String) attr.get(i);
					}
				} catch (NamingException e) {
					throw new InternalErrorException(e);
				}

				if (rule != null) {
					if(rule.contains("#")) {
						// Rules are in place, so apply them
						String regex = rule.substring(0, rule.indexOf('#'));
						String replacement = rule.substring(rule.indexOf('#')+1);
						tmpAttrValue = tmpAttrValue.replaceAll(regex, replacement);
					//DEPRECATED way
					} else {
						// Rules are in place, so apply them
						Pattern pattern = Pattern.compile(rule);
						matcher = pattern.matcher(tmpAttrValue);
						// Get the first group which matched
						if (matcher.matches()) {
							tmpAttrValue = matcher.group(1);
						}
					}
				}
				if (i == 0 || attributeValueIndex != -1) {
					// Do not add delimiter before first entry or if the particular index has been requested
					attrValue += tmpAttrValue;
				} else {
					attrValue += AttributesManagerImpl.LIST_DELIMITER + tmpAttrValue;
				}
			}

			if (attrValue.isEmpty()) {
				return "";
			} else {
				return attrValue;
			}
		} else {
			return "";
		}
	}

	/**
	 * Query LDAP using query in defined base. Results can be limited to the maxResults.
	 *
	 * @param query
	 * @param base
	 * @param maxResults
	 * @return List of Map of the LDAP attribute names and theirs values
	 * @throws InternalErrorException
	 */
	protected List<Map<String,String>> querySource(String query, String base, int maxResults) throws InternalErrorException {

		NamingEnumeration<SearchResult> results = null;
		List<Map<String, String>> subjects = new ArrayList<Map<String, String>>();

		try {
			// If query is null, then we are finding object by the base
			if (query == null) {
				log.trace("search base [{}]", base);
				Attributes ldapAttributes = getContext().getAttributes(base);
				if (ldapAttributes.size() > 0) {
					Map<String, String> attributes = this.getSubjectAttributes(ldapAttributes);
					if (!attributes.isEmpty()) {
						subjects.add(attributes);
					}
				}
			} else {
				log.trace("search string [{}]", query);

				SearchControls controls = new SearchControls();
				controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				// Set timeout to 5s
				controls.setTimeLimit(5000);
				if (maxResults > 0) {
					controls.setCountLimit(maxResults);
				}

				if (base == null) base = "";

				results = getContext().search(base, query, controls);
				while (results.hasMore()) {
					SearchResult searchResult = (SearchResult) results.next();
					Attributes attributes = searchResult.getAttributes();
					Map<String,String> subjectAttributes = this.getSubjectAttributes(attributes);
					if (!subjectAttributes.isEmpty()) {
						subjects.add(subjectAttributes);
					}
				}
			}

			return subjects;

		} catch (NamingException e) {
			log.error("LDAP exception during running query '{}'", query);
			throw new InternalErrorException("LDAP exception during running query: "+query+".", e);
		} finally {
			try {
				if (results != null) { results.close(); }
			} catch (Exception e) {
				log.error("LDAP exception during closing result, while running query '{}'", query);
				throw new InternalErrorException(e);
			}
		}
	}

	public void close() throws InternalErrorException {
		if (this.dirContext != null) {
			try {
				this.dirContext.close();
				this.dirContext = null;
			} catch (NamingException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	@Override
	public List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		return findSubjects(searchString, 0);
	}

	@Override
	public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		// We can call original implementation, since LDAP always return whole entry and not just login
		return findSubjectsLogins(searchString, maxResults);
	}

	protected Map<String,String> getAttributes() throws InternalErrorException {
		return perunBl.getExtSourcesManagerBl().getAttributes(this);
	}

}
