package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Metodej Klang
 */
public class ExtSourceREMSTest extends AbstractPerunIntegrationTest {

	@Spy
	private static ExtSourceREMS extSourceREMS;

	private static PerunBlImpl perunBl;

	@Before
	public void setUp() throws Exception {
		extSourceREMS = new ExtSourceREMS();
		perunBl = mock(PerunBlImpl.class, RETURNS_DEEP_STUBS);

		MockitoAnnotations.initMocks(this);
		ExtSourceREMS.setPerunBlImpl(perunBl);
	}

	@Test
	public void getUsersSubjectsTest() throws Exception {
		System.out.println("getUsersSubjectsTest");

		// define needed attributes
		String usersQuery = "usersQuery";
		Map<String, String> mapOfAttributes = new HashMap<>();
		mapOfAttributes.put("usersQuery", usersQuery);
		mapOfAttributes.put("url", "some.url.com");
		doReturn(mapOfAttributes).when(extSourceREMS).getAttributes();

		// mock data got from database
		doNothing().when(extSourceREMS).createConnection();
		PreparedStatement preparedStatement = mock(PreparedStatement.class);
		doReturn(preparedStatement).when(extSourceREMS).getPreparedStatement(usersQuery, null, 0);
		ResultSet resultSet = mock(ResultSet.class, RETURNS_DEEP_STUBS);
		doReturn(resultSet).when(preparedStatement).executeQuery();
		doReturn(true, false).when(resultSet).next();
		doReturn("josef").when(resultSet).getString("firstName");
		doReturn("xjosef").when(resultSet).getString("login");
		User user = new User();
		user.setFirstName("josef");
		when(perunBl.getUsersManagerBl().getUsersByExtSourceTypeAndLogin(any(), anyString(), eq("xjosef"))).thenReturn(Collections.singletonList(user));

		// create expected subject to get
		List<Map<String, String>> expectedSubjects = new ArrayList<>();
		Map<String, String> subject = new HashMap<>();
		subject.put("firstName", "josef");
		subject.put("login", "xjosef");
		subject.put("lastName", null);
		subject.put("titleBefore", null);
		subject.put("titleAfter", null);
		subject.put("middleName", null);
		expectedSubjects.add(subject);

		// test the method
		List<Map<String, String>> actualSubjects = extSourceREMS.getUsersSubjects();
		assertEquals("subjects should be same", expectedSubjects, actualSubjects);
	}
}
