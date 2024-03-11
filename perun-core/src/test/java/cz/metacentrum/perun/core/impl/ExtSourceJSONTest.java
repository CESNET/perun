package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.GroupsManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

/**
 * @author Johana Supikova <supikova@ics.muni.cz>
 */
public class ExtSourceJSONTest {

	@Spy
	private static ExtSourceJSON extSourceJson;

	@Before
	public void setUp() throws Exception {
		extSourceJson = new ExtSourceJSON();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getGroupMembersJsonTest() throws Exception {
		System.out.println("getGroupMembersJsonTest");

		File temp = File.createTempFile("temp",".json");
		temp.deleteOnExit();

		// define needed attributes
		Map<String, String> mapOfAttributes = new HashMap<>();
		mapOfAttributes.put(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME, "$.members.*");
		mapOfAttributes.put("file", temp.getAbsolutePath());
		mapOfAttributes.put("jsonMapping", "login={urn:perun:user:attribute-def:virt:login-namespace:mu},\n" +
				"additionalues_1=https://oidc.muni.cz/oidc/|cz.metacentrum.perun.core.impl.ExtSourceIdp|{urn:perun:user:attribute-def:virt:login-namespace:mu}|2,\n");
		doReturn(mapOfAttributes).when(extSourceJson).getAttributes();

		String json = "{\"members\":{\"uuid_1\":{\"urn:perun:user:attribute-def:virt:login-namespace:mu\":\"123456\"},\"uuid_2\":{\"urn:perun:user:attribute-def:virt:login-namespace:mu\":\"987654\"}}}";

		// fill in the file
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
			bw.write(json);
		}

		List<Map<String, String>> expectedSubjects = new ArrayList<>();
		Map<String, String> mapOfSubject = new HashMap<>();
		mapOfSubject.put("additionalues_1", "https://oidc.muni.cz/oidc/|cz.metacentrum.perun.core.impl.ExtSourceIdp|123456|2");
		mapOfSubject.put("login", "123456");
		expectedSubjects.add(mapOfSubject);
		mapOfSubject = new HashMap<>();
		mapOfSubject.put("additionalues_1", "https://oidc.muni.cz/oidc/|cz.metacentrum.perun.core.impl.ExtSourceIdp|987654|2");
		mapOfSubject.put("login", "987654");
		expectedSubjects.add(mapOfSubject);

		List<Map<String, String>> actualSubjects = extSourceJson.getGroupSubjects(mapOfAttributes);
		assertEquals("subjects should be the same", expectedSubjects, actualSubjects);
	}
}
