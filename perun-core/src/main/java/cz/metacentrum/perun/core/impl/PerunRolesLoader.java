package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The purpose of the PerunRolesLoader is to load perun roles and policies from the perun-roles.yml configuration file.
 *
 * Production configuration file is located in /etc/perun/perun-roles.yml
 * Configuration file which is used during the build is located in perun-base/src/test/resources/perun-roles.yml
 */
public class PerunRolesLoader {

	private static final Logger log = LoggerFactory.getLogger(PerunBasicDataSource.class);

	private Resource configurationPath;

	public void loadPerunRoles(JdbcPerunTemplate jdbc) {
		if (BeansUtils.isPerunReadOnly()) log.debug("Loading authzresolver manager init in readOnly version.");

		List<String> roles;
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		try (InputStream is = configurationPath.getInputStream()) {
			JsonNode rolesNode = mapper.readTree(is).get("perun_roles");
			roles = new ObjectMapper().convertValue(rolesNode, new TypeReference<List<String>>() {});
		} catch (FileNotFoundException e) {
			throw new InternalErrorException("Configuration file not found for perun roles. It should be in: " + configurationPath, e);
		} catch (IOException e) {
			throw new InternalErrorException("IO exception was thrown during the processing of the file: " + configurationPath, e);
		}

		// Check if all roles defined in class Role exists in the DB
		for (String role : roles) {
			try {
				if (0 == jdbc.queryForInt("select count(*) from roles where name=?", role.toLowerCase())) {
					//Skip creating not existing roles for read only Perun
					if (BeansUtils.isPerunReadOnly()) {
						throw new InternalErrorException("One of default roles not exists in DB - " + role);
					} else {
						int newId = Utils.getNewId(jdbc, "roles_id_seq");
						jdbc.update("insert into roles (id, name) values (?,?)", newId, role.toLowerCase());
					}
				}
			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	public void setConfigurationPath(Resource configurationPath) {
		this.configurationPath = configurationPath;
	}
}
