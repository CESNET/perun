package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunPolicy;
import cz.metacentrum.perun.core.api.RoleManagementRules;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The purpose of the PerunRolesLoader is to load perun roles and other policies from the perun-roles.yml configuration file.
 *
 * Production configuration file is located in /etc/perun/perun-roles.yml
 * Configuration file which is used during the build is located in perun-base/src/test/resources/perun-roles.yml
 */
public class PerunRolesLoader {

	private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
	private static final Logger log = LoggerFactory.getLogger(PerunRolesLoader.class);

	private Resource configurationPath;
	private Resource secondaryConfigurationPath;

	/**
	 * Load perun roles from the configuration file to the database.
	 *
	 * @param jdbc connection to database
	 */
	public void loadPerunRoles(JdbcPerunTemplate jdbc) {
		try {
			Set<String> roles = new HashSet<>(loadPerunRolesFromResource(configurationPath));
			if (secondaryConfigurationPath != null) {
				Set<String> secondaryRoles = new HashSet<>(loadPerunRolesFromResource(secondaryConfigurationPath));
				roles.addAll(secondaryRoles);
			}

			List<String> databaseRoles = new ArrayList<>(jdbc.query("select name from roles", new SingleColumnRowMapper<>(String.class)));
			databaseRoles.forEach(dbRole -> {
				if (!roles.contains(dbRole.toUpperCase()))
					log.debug("Role {} exists in the database but it is missing in the configuration files", dbRole);
			});

			// Check if all roles defined in class Role exists in the DB
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
		} catch(RuntimeException e) {
			throw new InternalErrorException("One of the roles configuration file has invalid syntax. Configuration files: " +
					configurationPath.getFilename() +
					(secondaryConfigurationPath == null ? "not defined" : secondaryConfigurationPath.getFilename()), e);
		}
	}

	/**
	 * Load policies from the configuration file as list of PerunPolicies
	 *
	 * @return list of PerunPolicies
	 */
	public Set<PerunPolicy> loadPerunPolicies() {
		Set<PerunPolicy> policies = new HashSet<>();

		try {
			JsonNode rootNode;
			if (secondaryConfigurationPath != null) {
				rootNode = loadConfigurationFile(secondaryConfigurationPath);
				policies.addAll(loadPoliciesFromJsonNode(rootNode));
			}
			rootNode = loadConfigurationFile(configurationPath);
			policies.addAll(loadPoliciesFromJsonNode(rootNode));

		} catch(RuntimeException e) {
			throw new InternalErrorException("One of the roles configuration file has invalid syntax. Configuration files: " +
					configurationPath.getFilename() +
					(secondaryConfigurationPath == null ? "not defined" : secondaryConfigurationPath.getFilename()), e);
		}

		return policies;
	}

	/**
	 * Load role management rules from the configuration file as map
	 * with RoleManagementRules' identification as key and RoleManagementRules as value.
	 *
	 * @return RoleManagementRules in a map.
	 */
	public Map<String, RoleManagementRules> loadPerunRolesManagement() {
		Map<String, RoleManagementRules> rolesManagementRules = new HashMap<>();

		try {
			JsonNode rootNode = loadConfigurationFile(configurationPath);
			loadPerunRolesManagementFromJsonNode(rootNode)
					.forEach(rule -> rolesManagementRules.put(rule.getRoleName(), rule));

			if (secondaryConfigurationPath != null) {
				rootNode = loadConfigurationFile(secondaryConfigurationPath);
				loadPerunRolesManagementFromJsonNode(rootNode)
						.forEach(rule -> rolesManagementRules.put(rule.getRoleName(), rule));
			}
		} catch(RuntimeException e) {
			throw new InternalErrorException("One of the roles configuration file has invalid syntax. Configuration files: " +
					configurationPath.getFilename() +
					(secondaryConfigurationPath == null ? "not defined" : secondaryConfigurationPath.getFilename()), e);
		}

		return rolesManagementRules;
	}

	private Set<RoleManagementRules> loadPerunRolesManagementFromJsonNode(JsonNode rootNode) {
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
			List<Map<String, String>> privilegedRolesToManage = createListOfMapsFromJsonNode(roleNode.get("privileged_roles_to_manage"));
			List<Map<String, String>> privilegedRolesToRead = createListOfMapsFromJsonNode(roleNode.get("privileged_roles_to_read"));
			Map<String, String> entitiesToManage = createMapFromJsonNode(roleNode.get("entities_to_manage"));
			Map<String, String> objectsToAssign = createMapFromJsonNode(roleNode.get("assign_to_objects"));
			List<Map<String, String>> assignmentCheck = createListOfMapsFromJsonNode(roleNode.get("assignment_check"));
			List<String> associatedReadRoles = createListFromJsonNode(roleNode.get("associated_read_roles"));
			boolean assignableToAttribute = roleNode.get("assignable_to_attributes").asBoolean();
			boolean systemRole = roleNode.get("system_role") != null && roleNode.get("system_role").asBoolean();

			rules.add(new RoleManagementRules(roleName, primaryObject, privilegedRolesToManage, privilegedRolesToRead, entitiesToManage, objectsToAssign, assignmentCheck, associatedReadRoles, assignableToAttribute, systemRole));
		}

		return rules;
	}

	private List<Map<String, String>> createListOfMapsFromJsonNode(JsonNode listNode) {
		List<Map<String, String>> rules = new ArrayList<>();

		// iterate list of maps
		for (JsonNode node : listNode) {
			Map<String, String> innerRoleMap = createMapFromJsonNode(node);
			rules.add(innerRoleMap);
		}

		return rules;
	}

	private Map<String, String> createMapFromJsonNode(JsonNode node) {
		Map<String, String> resultMap = new HashMap<>();

		Iterator<String> nodeArrayKeys = node.fieldNames();
		while (nodeArrayKeys.hasNext()) {
			String key = nodeArrayKeys.next();
			JsonNode valueNode = node.get(key);
			String value = valueNode.isNull() ? null : valueNode.textValue();
			resultMap.put(key, value);
		}

		return resultMap;
	}

	private List<String> createListFromJsonNode(JsonNode node) {
		List<String> resultList = new ArrayList<>();

		Iterator<JsonNode> nodeArray = node.elements();
		while (nodeArray.hasNext()) {
			String value = nodeArray.next().asText();
			resultList.add(value);
		}

		return resultList;
	}

	private JsonNode loadConfigurationFile(Resource resource) {

		JsonNode rootNode;
		try (InputStream is = resource.getInputStream()) {
			rootNode = objectMapper.readTree(is);
		} catch (FileNotFoundException e) {
			throw new InternalErrorException("Configuration file not found for perun roles. It should be in: " + resource, e);
		} catch (IOException e) {
			throw new InternalErrorException("IO exception was thrown during the processing of the file: " + resource, e);
		}

		return rootNode;
	}

	private List<String> loadPerunRolesFromResource(Resource resource) {
		JsonNode primaryConfigRootNode = loadConfigurationFile(resource);

		JsonNode rolesNode = primaryConfigRootNode.get("perun_roles");

		return objectMapper.convertValue(rolesNode, new TypeReference<List<String>>() {});
	}

	private Set<PerunPolicy> loadPoliciesFromJsonNode(JsonNode rootNode) {
		Set<PerunPolicy> policies = new HashSet<>();

		//Fetch all policies from the configuration file
		JsonNode policiesNode = rootNode.get("perun_policies");

		// For each policy node construct PerunPolicy and add it to the list
		Iterator<String> policyNames = policiesNode.fieldNames();
		while(policyNames.hasNext()) {
			String policyName = policyNames.next();
			JsonNode policyNode = policiesNode.get(policyName);
			List<Map<String, String>> perunRoles = new ArrayList<>();
			JsonNode perunRolesNode = policyNode.get("policy_roles");

			//Field policy_roles is saved as List of maps in the for loop
			for (JsonNode perunRoleNode : perunRolesNode) {
				Map<String, String> innerRoleMap = createMapFromJsonNode(perunRoleNode);
				perunRoles.add(innerRoleMap);
			}

			//Field include_policies is saved as List of Strings.
			List<String> includePolicies = new ArrayList<>(objectMapper.convertValue(policyNode.get("include_policies"), new TypeReference<List<String>>() {
			}));

			List<Map<String,String>> mfaRules = new ArrayList<>();
			JsonNode perunMFARulesNode = policyNode.get("mfa_rules");
			if (perunMFARulesNode != null) {
				//Field mfa_roles is saved as List of maps in the for loop
				for (JsonNode perunMfaNode : perunMFARulesNode) {
					Map<String, String> innerMFAMap = createMapFromJsonNode(perunMfaNode);
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

	public Resource getSecondaryConfigurationPath() {
		return secondaryConfigurationPath;
	}

	public void setSecondaryConfigurationPath(Resource secondaryConfigurationPath) {
		this.secondaryConfigurationPath = secondaryConfigurationPath;
	}
}
