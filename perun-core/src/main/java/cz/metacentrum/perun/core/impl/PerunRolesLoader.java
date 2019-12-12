package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunPolicy;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The purpose of the PerunRolesLoader is to load perun roles and policies from the perun-roles.yml configuration file.
 *
 * Production configuration file is located in /etc/perun/perun-roles.yml
 * Configuration file which is used during the build is located in perun-base/src/test/resources/perun-roles.yml
 */
public class PerunRolesLoader {

	private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

	private Resource configurationPath;

	public void loadPerunRoles(JdbcPerunTemplate jdbc) {
		try {
			JsonNode rootNode = loadConfigurationFile();

			JsonNode rolesNode = rootNode.get("perun_roles");
			List<String> roles = objectMapper.convertValue(rolesNode, new TypeReference<List<String>>() {});

			// Check if all roles defined in class Role exists in the DB
			for (String role : roles) {
				if (0 == jdbc.queryForInt("select count(*) from roles where name=?", role.toLowerCase())) {
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
			throw new InternalErrorException("The configuration file " + configurationPath.getFilename() + " has invalid syntax.", e);
		}
	}

	/**
	 * Load policies from the configuration file as list of PerunPolicies
	 *
	 * @return list of PerunPolicies
	 */
	public List<PerunPolicy> loadPerunPolicies() {
		List<PerunPolicy> policies = new ArrayList<>();

		try {
			JsonNode rootNode = loadConfigurationFile();
			//Fetch all policies from the configuration file
			JsonNode policiesNode = rootNode.get("perun_policies");

			// For each policy node construct PerunPolicy and add it to the list
			Iterator<String> policyNames = policiesNode.fieldNames();
			while (policyNames.hasNext()) {
				String policyName = policyNames.next();
				JsonNode policyNode = policiesNode.get(policyName);
				List<Map<String, String>> perunRoles = new ArrayList<>();
				JsonNode perunRolesNode = policyNode.get("policy_roles");

				//Field policy_roles is saved as List of maps in the for loop
				for (JsonNode perunRoleNode : perunRolesNode) {
					Map<String, String> innerRoleMap = new HashMap<>();
					Iterator<String> roleArrayKeys = perunRoleNode.fieldNames();
					while (roleArrayKeys.hasNext()) {
						String role = roleArrayKeys.next();
						JsonNode roleObjectNode = perunRoleNode.get(role);
						String object = roleObjectNode.isNull() ? null : roleObjectNode.textValue();
						innerRoleMap.put(role, object);
					}
					perunRoles.add(innerRoleMap);
				}

				//Field include_policies is saved as List of Strings.
				List<String> includePolicies = new ArrayList<>(objectMapper.convertValue(policyNode.get("include_policies"), new TypeReference<List<String>>() {
				}));

				policies.add(new PerunPolicy(policyName, perunRoles, includePolicies));
			}
		} catch(RuntimeException e) {
			throw new InternalErrorException("The configuration file " + configurationPath.getFilename() + " has invalid syntax.", e);
		}

		return policies;
	}

	private JsonNode loadConfigurationFile() {

		JsonNode rootNode;
		try (InputStream is = configurationPath.getInputStream()) {
			rootNode = objectMapper.readTree(is);
		} catch (FileNotFoundException e) {
			throw new InternalErrorException("Configuration file not found for perun roles. It should be in: " + configurationPath, e);
		} catch (IOException e) {
			throw new InternalErrorException("IO exception was thrown during the processing of the file: " + configurationPath, e);
		}

		return rootNode;
	}

	public void setConfigurationPath(Resource configurationPath) {
		this.configurationPath = configurationPath;
	}
}
