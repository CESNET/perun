package cz.metacentrum.perun.scim.api.endpoints;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.scim.AbstractSCIMTest;
import cz.metacentrum.perun.scim.api.entities.GroupSCIM;
import cz.metacentrum.perun.scim.api.entities.MemberSCIM;
import cz.metacentrum.perun.scim.api.exceptions.SCIMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static cz.metacentrum.perun.scim.api.SCIMDefaults.URN_GROUP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Testing class for handling group resources from Perun in SCIM protocol format.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 11.10.2016
 */
public class GroupResourceEndpointControllerTest extends AbstractSCIMTest {

	private GroupResourceEndpointController controller;
	private ObjectMapper mapper;

	@Before
	public void setUp() throws SCIMException, IOException, Exception {
		controller = new GroupResourceEndpointController(session);
		mapper = new ObjectMapper();
	}

	@Test
	public void testGetGroup() throws Exception {
		GroupSCIM group = prepareExpectedResult();
		String expectedJson = mapper.writeValueAsString(group);
		Response expectedResponse = Response.ok(expectedJson).build();

		Response result = controller.getGroup(group.getId().toString());

		if (result.getStatus() != 200) {
			fail();
		}

		assertEquals("expected group should equal with result obtained from SCIM REST API",
				expectedResponse.getEntity(), result.getEntity());
	}

	private GroupSCIM prepareExpectedResult() throws Exception {

		Vo vo1 = createVo(1, "vo1", "vo1");
		Vo vo2 = createVo(2, "vo2", "vo2");

		Group group1 = createGroup(vo1, "group1", "group1 in vo1");
		Group group2 = createGroup(vo2, "group2", "group2 in vo2");

		GroupSCIM group = new GroupSCIM();
		group.setId(new Long(group2.getId()));
		group.setDisplayName(group2.getName());
		List<String> schemas = new ArrayList<>();
		schemas.add(URN_GROUP);
		group.setSchemas(schemas);
		List<MemberSCIM> scimMembers = new ArrayList();
		group.setMembers(scimMembers);

		return group;
	}
}
