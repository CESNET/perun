package cz.metacentrum.perun.core.impl;

import java.util.ArrayList;
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

import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;

/**
 * Ext source implementation for LDAP.
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Pavel Zlámal <zlamal@cesnet.cz>
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

	private static PerunBlImpl perunBl;

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

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		return getGroupSubjects(attributes, null);
	}

	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes, List<String> logins) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		if(logins == null) throw new ExtSourceUnsupportedOperationException("Not supported to get subjects for this extSource by list of logins.");

		NamingEnumeration<SearchResult> results = null;

		List<String> ldapGroupSubjects = new ArrayList<String>();

		// Get the LDAP group name
		String ldapGroupName = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

		try {
			log.trace("LDAP External Source: searching for group subjects [{}]", ldapGroupName);

			String attrName;
			if (getAttributes().containsKey("memberAttribute")) {
				attrName = (String) getAttributes().get("memberAttribute");
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

			List<Map<String, String>> subjects = new ArrayList<Map<String, String>>();

			// Now query LDAP again and search for each subject
			for (String ldapSubjectName : ldapGroupSubjects) {
				subjects.addAll(this.querySource(filteredQuery, ldapSubjectName, 0));
			}

			return subjects;

		} catch (NamingException e) {
			log.error("LDAP exception during running query '{}'", ldapGroupName);
			throw new InternalErrorException("Entry '"+ldapGroupName+"' was not found in LDAP." , e);
		} finally {
			try {
				if (results != null) { results.close(); }
			} catch (Exception e) {
				log.error("LDAP exception during closing result, while running query '{}'", ldapGroupName);
				throw new InternalErrorException(e);
			}
		}
	}

	protected void initContext() throws InternalErrorException {
		// Load mapping between LDAP attributes and Perun attributes
		Hashtable<String,String> env = new Hashtable<String,String>();

		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		if (getAttributes().containsKey("referral")) {
			env.put(Context.REFERRAL, (String) getAttributes().get("referral"));
		}
		if (getAttributes().containsKey("url")) {
			env.put(Context.PROVIDER_URL, (String) getAttributes().get("url"));
		} else {
			throw new InternalErrorException("url attributes is required");
		}
		if (getAttributes().containsKey("user")) {
			env.put(Context.SECURITY_PRINCIPAL, (String) getAttributes().get("user"));
		}
		if (getAttributes().containsKey("password")) {
			env.put(Context.SECURITY_CREDENTIALS, (String) getAttributes().get("password"));
		}

		if (getAttributes().containsKey("filteredQuery")) {
			filteredQuery = (String) getAttributes().get("filteredQuery");
		}

		try {
			// ldapMapping contains entries like: firstName={givenName},lastName={sn},email={mail}
			if (getAttributes().get("ldapMapping") == null) {
				throw new InternalErrorException("ldapMapping attributes is required");
			}
			String ldapMapping[] = ((String) getAttributes().get("ldapMapping")).trim().split(",");
			mapping = new HashMap<String, String>();
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
		Pattern pattern = Pattern.compile("\\{([^\\}])*\\}");;
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
		Pattern pattern = null;
		String attrValue = "";;

		// Check if the ldapAttrName contains regex
		if (ldapAttrNameRaw.contains("|")) {
			int splitter = ldapAttrNameRaw.indexOf('|');
			ldapAttrName = ldapAttrNameRaw.substring(0,splitter);
			rule = ldapAttrNameRaw.substring(splitter+1, ldapAttrNameRaw.length());
			pattern =  Pattern.compile(rule);
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
					if (((String) attr.get()).startsWith(": ")) {
						// Base64 encoded attribute starts with ": "
						tmpAttrValue = String.valueOf(Base64Coder.decodeString((String) attr.get(i)));
					} else {
						tmpAttrValue = (String) attr.get(i);
					}
				} catch (NamingException e) {
					throw new InternalErrorException(e);
				}

				if (rule != null) {
					// Rules are in place, so apply them
					matcher = pattern.matcher(tmpAttrValue);
					// Get the first group which matched
					if (matcher.matches()) {
						tmpAttrValue = matcher.group(1);
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
				// TODO jmena atributu spise prijimiat pres vstupni parametr metody
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

			log.trace("Returning [{}] subjects", subjects.size());
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

	protected Map<String,String> getAttributes() {
		return perunBl.getExtSourcesManagerBl().getAttributes(this);
	}

}
