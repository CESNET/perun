package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunPolicy;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.RoleManagementRules;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.rt.RolesConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * The purpose of the PerunRolesLoader is to load perun roles and other policies from the perun-roles.yml configuration
 * file.
 * <p>
 * Production configuration file is located in /etc/perun/perun-roles.yml Configuration file which is used during the
 * build is located in perun-base/src/test/resources/perun-roles.yml
 */
public class PerunRolesLoader {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static final Logger LOG = LoggerFactory.getLogger(PerunRolesLoader.class);

  private Resource configurationPath;
  private Resource secondaryConfigurationPath;

  /**
   * Loads an array of strings from json node.
   *
   * @param node json array node
   * @param subject subject being parsed, used in exception message in case of wrong format
   * @param parsedProperty property being converted, used in exception message in case of wrong format
   * @return
   */
  private List<String> createListFromJsonNode(JsonNode node, String subject, String parsedProperty) {
    List<String> resultList = new ArrayList<>();

    if (node == null) {
      return resultList;
    }

    if (!node.isArray()) {
      throw new RolesConfigurationException("Expected " + parsedProperty + " for " + subject +
          " to be a correct YAML array");
    }

    Iterator<JsonNode> nodeArray = node.elements();
    while (nodeArray.hasNext()) {
      String value = nodeArray.next().asText();
      if (value == null || value.isEmpty()) {
        throw new RolesConfigurationException("Expected " + parsedProperty + " for " + subject +
            " to be a correct YAML array with string items");
      }
      resultList.add(value);
    }

    return resultList;
  }

  /**
   * Create a list of maps from json listNode
   *
   * @param listNode json list node
   * @param allowedKeys the strings which can be used as keys of the maps (e.g. role names), no constraint if null
   * @param subject subject being parsed, used in exception message in case of wrong format
   * @param parsedProperty property being converted, used in exception message in case of wrong format
   * @return the node converted to a map
   */
  private List<Map<String, String>> createListOfMapsFromJsonNode(JsonNode listNode, List<String> allowedKeys,
                                                                 String subject, String parsedProperty) {
    if (!listNode.isArray()) {
      throw new RolesConfigurationException("Expected " + parsedProperty + " for " + subject +
          " to be a correct YAML array");
    }
    List<Map<String, String>> rules = new ArrayList<>();

    // iterate list of maps
    for (JsonNode node : listNode) {
      Map<String, String> innerRoleMap = createMapFromJsonNode(node, allowedKeys, subject, parsedProperty);
      rules.add(innerRoleMap);
    }

    return rules;
  }

  /**
   * Convert json node to a map. Expects string from allowedKeys as keys if allowedKeys not null.
   * The subject and parsedProperty parameters are used in case of parsing error to specify the place of the error.
   *
   * @param node json node
   * @param allowedKeys the strings which can be used as keys of the map (e.g. role names), no constraint if null
   * @param subject subject being parsed, used in exception message in case of wrong format
   * @param parsedProperty property being converted, used in exception message in case of wrong format
   * @return the node converted to a map
   */
  private Map<String, String> createMapFromJsonNode(JsonNode node, List<String> allowedKeys,
                                                    String subject, String parsedProperty) {
    if (!node.isObject()) {
      throw new RolesConfigurationException("Could not parse " + parsedProperty + " for " + subject +
          " error reading: " + node.asText());
    }
    Map<String, String> resultMap = new HashMap<>();

    Iterator<String> nodeArrayKeys = node.fieldNames();
    while (nodeArrayKeys.hasNext()) {
      String key = nodeArrayKeys.next();
      JsonNode valueNode = node.get(key);
      String value = valueNode.isNull() ? null : valueNode.textValue();
      resultMap.put(key, value);
    }
    if (allowedKeys != null) {
      checkThatRolesExist(resultMap.keySet(), allowedKeys, subject, parsedProperty);
    }

    return resultMap;
  }

  public Resource getSecondaryConfigurationPath() {
    return secondaryConfigurationPath;
  }

  private JsonNode loadConfigurationFile(Resource resource) {

    JsonNode rootNode;
    try (InputStream is = resource.getInputStream()) {
      rootNode = OBJECT_MAPPER.readTree(is);
    } catch (FileNotFoundException e) {
      throw new InternalErrorException("Configuration file not found for perun roles. It should be in: " + resource, e);
    } catch (IOException e) {
      throw new InternalErrorException("IO exception was thrown during the processing of the file: " + resource, e);
    }

    return rootNode;
  }

  /**
   * Load policies from the configuration file as list of PerunPolicies
   *
   * @param roles the list of role names used to check the correctness of rules
   * @return list of PerunPolicies
   */
  public Set<PerunPolicy> loadPerunPolicies(List<String> roles) {
    Set<PerunPolicy> policies = new HashSet<>();

    try {
      JsonNode rootNode;
      if (secondaryConfigurationPath != null) {
        rootNode = loadConfigurationFile(secondaryConfigurationPath);
        policies.addAll(loadPoliciesFromJsonNode(rootNode, roles));
      }
      rootNode = loadConfigurationFile(configurationPath);
      policies.addAll(loadPoliciesFromJsonNode(rootNode, roles));

    } catch (RuntimeException e) {
      throw new RolesConfigurationException("One of the roles configuration file has invalid syntax." +
          " Configuration files: " + configurationPath.getFilename() +
          (secondaryConfigurationPath == null ? " secondary file not defined" :
              ", " + secondaryConfigurationPath.getFilename()), e);
    }

    return policies;
  }

  /**
   * Load perun roles from the configuration file to the database.
   *
   * @param jdbc connection to database
   * @return up-to-date list of role names
   */
  public List<String> loadPerunRoles(JdbcPerunTemplate jdbc) {
    try {
      Set<String> roles = new HashSet<>(loadPerunRolesFromResource(configurationPath));
      if (secondaryConfigurationPath != null) {
        Set<String> secondaryRoles = new HashSet<>(loadPerunRolesFromResource(secondaryConfigurationPath));
        roles.addAll(secondaryRoles);
      }

      List<String> databaseRoles =
          new ArrayList<>(jdbc.query("select name from roles", new SingleColumnRowMapper<>(String.class)));
      databaseRoles.forEach(dbRole -> {
        if (!roles.contains(dbRole.toUpperCase())) {
          LOG.debug("Role {} exists in the database but it is missing in the configuration files", dbRole);
        }
      });

      // Check if all roles defined in the configuration file exist in the DB as well
      for (String role : roles) {
        if (!databaseRoles.contains(role.toLowerCase())) {
          //Skip creating not existing roles for read only Perun
          if (BeansUtils.isPerunReadOnly()) {
            throw new InternalErrorException("One of default roles not exists in DB - " + role);
          } else {
            int newId = Utils.getNewId(jdbc, "roles_id_seq");
            jdbc.update("insert into roles (id, name) values (?,?)", newId, role.toLowerCase());
          }
        }
      }
      List<String> allRoles = new ArrayList<>(
          jdbc.query("select name from roles", new SingleColumnRowMapper<>(String.class)));

      // check if all roles are listed in the Role class
      if (!new HashSet<>(allRoles).equals(new HashSet<>(Role.rolesAsList()))) {
        LOG.warn("The roles listed in the Role class do not correspond to those in the database. " +
            "Please check and update the list of roles in the class.");
      }
      return allRoles;
    } catch (RuntimeException e) {
      throw new InternalErrorException("One of the roles configuration file has invalid syntax. Configuration files: " +
                                       configurationPath.getFilename() +
                                       (secondaryConfigurationPath == null ? "not defined" :
                                           secondaryConfigurationPath.getFilename()), e);
    }
  }

  private List<String> loadPerunRolesFromResource(Resource resource) {
    JsonNode primaryConfigRootNode = loadConfigurationFile(resource);

    JsonNode rolesNode = primaryConfigRootNode.get("perun_roles");

    return OBJECT_MAPPER.convertValue(rolesNode, new TypeReference<List<String>>() {
    });
  }

  /**
   * Load role management rules from the configuration file as map with RoleManagementRules' identification as key and
   * RoleManagementRules as value.
   *
   * @param availableRoleNames available role names in perun to check the role names correctness when parsing the file
   * @return RoleManagementRules in a map.
   */
  public Map<String, RoleManagementRules> loadPerunRolesManagement(List<String> availableRoleNames) {
    Map<String, RoleManagementRules> rolesManagementRules = new HashMap<>();

    try {
      JsonNode rootNode = loadConfigurationFile(configurationPath);
      loadPerunRolesManagementFromJsonNode(rootNode, availableRoleNames).forEach(
          rule -> rolesManagementRules.put(rule.getRoleName(), rule));

      if (secondaryConfigurationPath != null) {
        rootNode = loadConfigurationFile(secondaryConfigurationPath);
        loadPerunRolesManagementFromJsonNode(rootNode, availableRoleNames).forEach(
            rule -> rolesManagementRules.put(rule.getRoleName(), rule));
      }
    } catch (RuntimeException e) {
      throw new RolesConfigurationException(
          "One of the roles configuration file has invalid syntax. Configuration files: " +
                                       configurationPath.getFilename() +
                                       (secondaryConfigurationPath == null ? "not defined" :
                                           secondaryConfigurationPath.getFilename()), e);
    }

    return rolesManagementRules;
  }

  /**
   * Parses role management rules from a loaded file to a set of RoleManagementRules. Throws exception
   * if the rules are not in the expected format (list instead of map, unknown role...).
   *
   * @param rootNode the root json node of the file
   * @param availableRoleNames available role names in perun to check the correctness when parsing the rules
   * @return the loaded role management rules
   */
  private Set<RoleManagementRules> loadPerunRolesManagementFromJsonNode(JsonNode rootNode,
                                                                        List<String> availableRoleNames) {
    Set<RoleManagementRules> rules = new HashSet<>();
    //Fetch all policies from the configuration file
    JsonNode rolesNodes = rootNode.get("perun_roles_management");

    // For each role node construct RoleManagementRules and add it to the map
    Iterator<String> roleNames = rolesNodes.fieldNames();
    while (roleNames.hasNext()) {
      String roleName = roleNames.next();
      JsonNode roleNode = rolesNodes.get(roleName);

      JsonNode primaryObjectNode = roleNode.get("primary_object");
      String primaryObject = primaryObjectNode.isNull() ? null : primaryObjectNode.textValue();

      List<Map<String, String>> privilegedRolesToManage =
          createListOfMapsFromJsonNode(roleNode.get("privileged_roles_to_manage"), availableRoleNames,
          roleName + " management rules", "privileged_roles_to_manage");

      List<Map<String, String>> privilegedRolesToRead =
          createListOfMapsFromJsonNode(roleNode.get("privileged_roles_to_read"), availableRoleNames,
              roleName + " management rules", "privileged_roles_to_read");

      Map<String, String> entitiesToManage = createMapFromJsonNode(
          roleNode.get("entities_to_manage"), null, roleName + " management rules", "entities_to_manage");

      Map<String, String> objectsToAssign = createMapFromJsonNode(
          roleNode.get("assign_to_objects"), null, roleName + " management rules", "assign_to_objects");

      List<Map<String, String>> assignmentCheck =
          createListOfMapsFromJsonNode(roleNode.get("assignment_check"), List.of("MFA"),
              roleName + " management rules", "assignment_check");

      List<String> associatedReadRoles = createListFromJsonNode(
          roleNode.get("associated_read_roles"), roleName + " management rules", "associated_read_roles");

      boolean assignableToAttribute = roleNode.get("assignable_to_attributes").asBoolean();

      boolean skipMFA = roleNode.get("skip_mfa") != null && roleNode.get("skip_mfa").asBoolean();

      boolean mfaCriticalRole =
          roleNode.get("mfa_critical_role") != null && roleNode.get("mfa_critical_role").asBoolean();

      JsonNode displayNameNode = roleNode.get("display_name");
      String displayName = displayNameNode.isNull() ? null : displayNameNode.textValue();

      List<String> receiveNotifications = createListFromJsonNode(
          roleNode.get("receive_notifications"), roleName + " management rules", "receive_notifications");

      rules.add(new RoleManagementRules(roleName, primaryObject, privilegedRolesToManage, privilegedRolesToRead,
          entitiesToManage, objectsToAssign, assignmentCheck, associatedReadRoles, assignableToAttribute, skipMFA,
          mfaCriticalRole, displayName, receiveNotifications));
    }

    return rules;
  }

  /**
   * Parses the policies from a loaded file to a set of PerunPolicy. Throws exception if the policies are not in the
   * expected format (list instead of map, unknown role...).
   *
   * @param rootNode the root json node of the file
   * @param availableRoleNames available role names in perun to check the correctness when parsing the policies
   * @return the loaded policies
   */
  private Set<PerunPolicy> loadPoliciesFromJsonNode(JsonNode rootNode, List<String> availableRoleNames) {
    Set<PerunPolicy> policies = new HashSet<>();

    //Fetch all policies from the configuration file
    JsonNode policiesNode = rootNode.get("perun_policies");
    if (policiesNode == null) {
      throw new RolesConfigurationException("Expected a perun_policies field in the roles specification file");
    }

    // For each policy node construct PerunPolicy and add it to the list
    Iterator<String> policyNames = policiesNode.fieldNames();
    while (policyNames.hasNext()) {
      String policyName = policyNames.next();
      JsonNode policyNode = policiesNode.get(policyName);
      List<Map<String, String>> perunRoles = new ArrayList<>();

      JsonNode perunRolesNode = policyNode.get("policy_roles");
      if (!perunRolesNode.isArray()) {
        throw new RolesConfigurationException("Could not parse policy " + policyName +
            ". The field policy_roles must be a correct yaml array");
      }

      //Field policy_roles is saved as List of maps in the for loop
      for (JsonNode perunRoleNode : perunRolesNode) {
        Map<String, String> innerRoleMap = createMapFromJsonNode(perunRoleNode, availableRoleNames,
            policyName, "policy_roles");

        perunRoles.add(innerRoleMap);
      }

      //Field include_policies is saved as List of Strings.
      List<String> includePolicies;
      try {
        includePolicies = new ArrayList<>(
            OBJECT_MAPPER.convertValue(policyNode.get("include_policies"), new TypeReference<List<String>>() {
            }));
      } catch (Exception e) {
        throw new RolesConfigurationException("Could not parse include_policies field for " + policyName +
            ". The field should be a correct yaml array of strings.");
      }

      List<Map<String, String>> mfaRules = new ArrayList<>();
      JsonNode perunMFARulesNode = policyNode.get("mfa_rules");
      if (perunMFARulesNode != null) {
        if (!perunMFARulesNode.isArray()) {
          throw new RolesConfigurationException("Could not parse MFA rules for " + policyName +
              ". The field mfa_rules must be a correct yaml array");
        }

        //Field mfa_roles is saved as List of maps in the for loop
        for (JsonNode perunMfaNode : perunMFARulesNode) {
          // All MFA rules need to have MFA as key
          Map<String, String> innerMFAMap = createMapFromJsonNode(perunMfaNode, List.of("MFA"),
              policyName, "mfa_rules");

          mfaRules.add(innerMFAMap);
        }
      }

      policies.add(new PerunPolicy(policyName, perunRoles, includePolicies, mfaRules));
    }

    return policies;
  }

  public void setConfigurationPath(Resource configurationPath) {
    this.configurationPath = configurationPath;
  }

  public void setSecondaryConfigurationPath(Resource secondaryConfigurationPath) {
    this.secondaryConfigurationPath = secondaryConfigurationPath;
  }

  /**
   * Check that the provided list of keys is included in the provided allowed keys case insensitively
   *
   * @param keysToCheck the strings to check
   * @param allowedKeys the allowed values (e.g. available role names)
   * @param subject subject being parsed, used in exception message in case of wrong format
   * @param parsedProperty property being converted, used in exception message in case of wrong format
   */
  private void checkThatRolesExist(Collection<String> keysToCheck, List<String> allowedKeys,
                                   String subject, String parsedProperty) {
    keysToCheck = keysToCheck.stream().map(String::toLowerCase).collect(Collectors.toList());
    List<String> availableRoleNamesLowercase = allowedKeys.stream().map(String::toLowerCase).toList();

    if (!new HashSet<>(availableRoleNamesLowercase).containsAll(keysToCheck)) {
      Optional<String> missingRole = keysToCheck
          .stream()
          .filter(e -> !availableRoleNamesLowercase.contains(e))
          .findFirst();
      throw new RolesConfigurationException(
          parsedProperty + " of " + subject + " includes non existing role " + missingRole.get());
    }
  }
}
