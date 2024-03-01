package cz.metacentrum.perun.scim.api.endpoints;

import static cz.metacentrum.perun.scim.api.SCIMDefaults.URN_USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.scim.AbstractSCIMTest;
import cz.metacentrum.perun.scim.api.entities.UserSCIM;
import cz.metacentrum.perun.scim.api.exceptions.SCIMException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing class for handling user resources from Perun in SCIM protocol format.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 11.10.2016
 */
public class UserResourceEndpointControllerTest extends AbstractSCIMTest {

  private UserResourceEndpointController controller;
  private ObjectMapper mapper;

  private UserSCIM prepareExpectedResult() throws Exception {
    User user1 = createUser(1, "firstName1", "lastName1");
    Long userId = new Long(user1.getId());

    UserSCIM user = new UserSCIM();
    user.setId(userId);
    user.setName(user1.getDisplayName());
    user.setDisplayName(user1.getDisplayName());
    user.setUserName(userId.toString());
    List<String> schemas = new ArrayList<>();
    schemas.add(URN_USER);
    user.setSchemas(schemas);

    return user;
  }

  @Before
  public void setUp() throws SCIMException, IOException, Exception {
    controller = new UserResourceEndpointController(session);
    mapper = new ObjectMapper();
  }

  @Test
  public void testGetUser() throws Exception {
    UserSCIM user = prepareExpectedResult();
    String expectedJson = mapper.writeValueAsString(user);
    Response expectedResponse = Response.ok(expectedJson).build();

    Response result = controller.getUser(user.getId().toString());

    if (result.getStatus() != 200) {
      fail();
    }
    assertEquals("expected user should equal with result obtained from SCIM REST API", expectedResponse.getEntity(),
        result.getEntity());
  }
}
