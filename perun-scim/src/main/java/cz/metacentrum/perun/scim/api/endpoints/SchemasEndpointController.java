package cz.metacentrum.perun.scim.api.endpoints;

import static cz.metacentrum.perun.scim.api.SCIMDefaults.URN_GROUP;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.URN_SCHEMA;
import static cz.metacentrum.perun.scim.api.SCIMDefaults.URN_USER;

import cz.metacentrum.perun.scim.api.entities.ListResponseSCIM;
import cz.metacentrum.perun.scim.api.entities.SchemaSCIM;
import cz.metacentrum.perun.scim.api.exceptions.SCIMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Endpoint controller, that returns schema of all resources.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
public class SchemasEndpointController {

	public Response getSchemas() throws SCIMException {
		try {
			ListResponseSCIM result = new ListResponseSCIM();
			result.setResources(getAllSchemas());
			result.setSchemas(URN_SCHEMA);
			int numberOfSchemas = result.getResources().size();
			result.setTotalResults(new Long(numberOfSchemas));

			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(mapper.writeValueAsString(result)).build();
		} catch (IOException ex) {
			throw new SCIMException("Cannot convert schemas to json string", ex);
		}
	}

	private List<SchemaSCIM> getAllSchemas() {
		List<SchemaSCIM> schemas = new ArrayList<>();
		schemas.add(getUserSchema());
		schemas.add(getGroupSchema());
		return schemas;
	}

	private SchemaSCIM getUserSchema() {
		SchemaSCIM userSchema = new SchemaSCIM();
		userSchema.setId(URN_USER);
		userSchema.setName("User");
		userSchema.setDescription("User Account");

		return userSchema;
	}

	private SchemaSCIM getGroupSchema() {
		SchemaSCIM groupSchema = new SchemaSCIM();
		groupSchema.setId(URN_GROUP);
		groupSchema.setName("Group");
		groupSchema.setDescription("Group");

		return groupSchema;
	}
}
