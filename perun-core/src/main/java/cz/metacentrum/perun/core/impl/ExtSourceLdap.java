package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ext source implementation for LDAP.
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ExtSourceLdap extends ExtSourceImpl implements ExtSourceApi {

  protected static final Logger LOG = LoggerFactory.getLogger(ExtSourceLdap.class);
  protected Map<String, String> mapping;
  protected DirContext dirContext = null;
  protected String filteredQuery = null;

  @Override
  public void close() {
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
  public List<Map<String, String>> findSubjects(String searchString) {
    // We can call original implementation, since LDAP always return whole entry and not just login
    return findSubjectsLogins(searchString);
  }

  @Override
  public List<Map<String, String>> findSubjectsLogins(String searchString) {
    // Prepare searchQuery
    // attributes.get("query") contains query template, e.g. (uid=?), ? will be replaced by the searchString
    String query = getAttributes().get("query");
    if (query == null) {
      throw new InternalErrorException("query attributes is required");
    }
    query = query.replace("?", Utils.escapeStringForLDAP(searchString));

    String base = getAttributes().get("base");
    if (base == null) {
      throw new InternalErrorException("base attributes is required");
    }
    return this.querySource(query, base);
  }

  protected DirContext getContext() {
    if (dirContext == null) {
      initContext();
    }
    return dirContext;
  }

  @Override
  public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) {

    List<String> ldapGroupSubjects = new ArrayList<>();

    // Get the LDAP group name
    String ldapGroupName = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
    // Get optional filter for members filtering
    String filter = attributes.get(GroupsManager.GROUPMEMBERSFILTER_ATTRNAME);

    try {
      LOG.trace("LDAP External Source: searching for group subjects [{}]", ldapGroupName);

      String attrName;
      // Default value
      attrName = getAttributes().getOrDefault("memberAttribute", "uniqueMember");
      List<String> retAttrs = new ArrayList<>();
      retAttrs.add(attrName);

      String[] retAttrsArray = retAttrs.toArray(new String[0]);
      Attributes attrs = getContext().getAttributes(ldapGroupName, retAttrsArray);

      Attribute ldapAttribute = null;
      // Get the list of returned groups, should be only one
      if (attrs.get(attrName) != null) {
        // Get the attribute which holds group subjects
        ldapAttribute = attrs.get(attrName);
      }

      if (ldapAttribute != null) {
        // Get the DNs of the subjects
        for (int i = 0; i < ldapAttribute.size(); i++) {
          String ldapSubjectDN = (String) ldapAttribute.get(i);
          ldapGroupSubjects.add(ldapSubjectDN);
          LOG.trace("LDAP External Source: found group subject [{}].", ldapSubjectDN);
        }
      }

      List<Map<String, String>> subjects = new ArrayList<>();

      // If attribute filter not exists, use optional default filter from extSource definition
      if (filter == null) {
        filter = filteredQuery;
      }

      // Now query LDAP again and search for each subject
      for (String ldapSubjectName : ldapGroupSubjects) {
        subjects.addAll(this.querySource(filter, ldapSubjectName));
      }

      return subjects;

    } catch (NamingException e) {
      LOG.error("LDAP exception during running query '{}'", ldapGroupName);
      throw new InternalErrorException("Entry '" + ldapGroupName + "' was not found in LDAP.", e);
    }
  }

  protected String getLdapAttributeValue(Attributes attributes, String ldapAttrNameRaw) {
    String ldapAttrName;
    String rule = null;
    Matcher matcher;
    String attrValue = "";

    // Check if the ldapAttrName contains regex
    if (ldapAttrNameRaw.contains("|")) {
      int splitter = ldapAttrNameRaw.indexOf('|');
      ldapAttrName = ldapAttrNameRaw.substring(0, splitter);
      rule = ldapAttrNameRaw.substring(splitter + 1);
    } else {
      ldapAttrName = ldapAttrNameRaw;
    }

    // Check if the ldapAttrName contains specification of the value index
    int attributeValueIndex = -1;
    if (ldapAttrNameRaw.contains("[")) {
      Pattern indexPattern = Pattern.compile("^(.*)\\[([0-9]+)]$");
      Matcher indexMatcher = indexPattern.matcher(ldapAttrNameRaw);
      if (indexMatcher.find()) {
        ldapAttrName = indexMatcher.group(1);
        attributeValueIndex = Integer.parseInt(indexMatcher.group(2));
      } else {
        throw new InternalErrorException(
            "Wrong attribute name format for attribute: " + ldapAttrNameRaw + ", it should be name[0-9+]");
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

        String tmpAttrValue;
        try {
          if (attr.get() instanceof byte[]) {
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
          if (rule.contains("#")) {
            // Rules are in place, so apply them
            String regex = rule.substring(0, rule.indexOf('#'));
            String replacement = rule.substring(rule.indexOf('#') + 1);
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

  protected Map<String, String> getSubjectAttributes(Attributes attributes) {
    Pattern pattern = Pattern.compile("\\{([^}])*}");
    Map<String, String> map = new HashMap<>();

    for (String key : mapping.keySet()) {
      // Get attribute value and substitute all {} in the string
      Matcher matcher = pattern.matcher(mapping.get(key));
      String value = mapping.get(key);

      // Find all matches
      while (matcher.find()) {
        // Get the matching string
        String ldapAttributeNameRaw = matcher.group();
        String ldapAttributeName = ldapAttributeNameRaw.replaceAll("\\{([^}]*)}",
            "$1"); // ldapAttributeNameRaw is encapsulate with {}, so remove it
        // Replace {ldapAttrName} with the value
        value = value.replace(ldapAttributeNameRaw, getLdapAttributeValue(attributes, ldapAttributeName));
        LOG.trace("ExtSourceLDAP: Retrieved value {} of attribute {} for {} and storing into the key {}.", value,
            ldapAttributeName, ldapAttributeNameRaw, key);
      }

      map.put(key, value);
    }

    return map;
  }

  @Override
  public Map<String, String> getSubjectByLogin(String login) throws SubjectNotExistsException {
    // Prepare searchQuery
    // attributes.get("loginQuery") contains query template, e.g. (uid=?), ? will be replaced by the login
    String query = getAttributes().get("loginQuery");
    if (query == null) {
      throw new InternalErrorException("loginQuery attributes is required");
    }
    query = query.replace("?", Utils.escapeStringForLDAP(login));

    String base = getAttributes().get("base");
    if (base == null) {
      throw new InternalErrorException("base attributes is required");
    }

    List<Map<String, String>> subjects = this.querySource(query, base);

    if (subjects.size() > 1) {
      throw new SubjectNotExistsException("There are more than one results for the login: " + login);
    }

    if (subjects.size() == 0) {
      throw new SubjectNotExistsException(login);
    }

    return subjects.get(0);
  }

  @Override
  public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes)
      throws ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }

  @Override
  public List<Map<String, String>> getUsersSubjects() {
    // if usersQuery is null, there is no filter and method returns all users subjects
    String filter = getAttributes().get(UsersManager.USERS_QUERY);

    String base = getAttributes().get("base");
    if (base == null) {
      throw new InternalErrorException("base attributes is required");
    }

    return querySource(filter, base);
  }

  protected void initContext() {
    // Load mapping between LDAP attributes and Perun attributes
    Hashtable<String, String> env = new Hashtable<>();

    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
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
      String[] ldapMapping = getAttributes().get("ldapMapping").trim().split(",\n");
      mapping = new HashMap<>();
      for (String entry : ldapMapping) {
        String[] values = entry.trim().split("=", 2);
        mapping.put(values[0].trim(), values[1].trim());
      }

      this.dirContext = new InitialDirContext(env);
    } catch (NamingException e) {
      LOG.error("LDAP exception during creating the context.");
      throw new InternalErrorException(e);
    }
  }

  /**
   * Query LDAP using query in defined base.
   *
   * @param query
   * @param base
   * @return List of Map of the LDAP attribute names and theirs values
   * @throws InternalErrorException
   */
  protected List<Map<String, String>> querySource(String query, String base) {

    NamingEnumeration<SearchResult> results = null;
    List<Map<String, String>> subjects = new ArrayList<>();

    try {
      // If query is null, then we are finding object by the base
      if (query == null) {
        LOG.trace("search base [{}]", base);
        // TODO jmena atributu spise prijimiat pres vstupni parametr metody
        Attributes ldapAttributes = getContext().getAttributes(base);
        if (ldapAttributes.size() > 0) {
          Map<String, String> attributes = this.getSubjectAttributes(ldapAttributes);
          if (!attributes.isEmpty()) {
            subjects.add(attributes);
          }
        }
      } else {
        LOG.trace("search string [{}]", query);

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // Set timeout to 5s
        controls.setTimeLimit(5000);

        if (base == null) {
          base = "";
        }

        results = getContext().search(base, query, controls);
        while (results.hasMore()) {
          SearchResult searchResult = results.next();
          Attributes attributes = searchResult.getAttributes();
          Map<String, String> subjectAttributes = this.getSubjectAttributes(attributes);
          if (!subjectAttributes.isEmpty()) {
            subjects.add(subjectAttributes);
          }
        }
      }

      LOG.trace("Returning [{}] subjects", subjects.size());
      return subjects;

    } catch (NamingException e) {
      LOG.error("LDAP exception during running query '{}'", query);
      throw new InternalErrorException("LDAP exception during running query: " + query + ".", e);
    } finally {
      try {
        if (results != null) {
          results.close();
        }
      } catch (Exception e) {
        LOG.error("LDAP exception during closing result, while running query '{}'", query);
        throw new InternalErrorException(e);
      }
    }
  }
}
