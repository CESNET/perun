package cz.metacentrum.perun.core.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Metodej Klang
 */
public class ExtSourceSqlTest {

	@Spy
	private static ExtSourceSql extSourceSql;

	@Before
	public void setUp() throws Exception {
		extSourceSql = new ExtSourceSql();

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getUsersSubjectsTest() throws Exception {
		System.out.println("getUsersSubjectsTest");

		// define needed attributes
		String usersQuery = "usersQuery";
		Map<String, String> mapOfAttributes = new HashMap<>();
		mapOfAttributes.put("usersQuery", usersQuery);
		mapOfAttributes.put("url", "some.url.com");
		doReturn(mapOfAttributes).when(extSourceSql).getAttributes();

		// mock data got from database
		doNothing().when(extSourceSql).createConnection();
		PreparedStatement preparedStatement = mock(PreparedStatement.class);
		doReturn(preparedStatement).when(extSourceSql).getPreparedStatement(usersQuery, null, 0);
		ResultSet resultSet = mock(ResultSet.class, RETURNS_DEEP_STUBS);
		doReturn(resultSet).when(preparedStatement).executeQuery();
		doReturn(true, false).when(resultSet).next();
		doReturn("josef").when(resultSet).getString("firstName");
		doReturn("xjosef").when(resultSet).getString("login");

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
		List<Map<String, String>> actualSubjects = extSourceSql.getUsersSubjects();
		assertEquals("subjects should be same", expectedSubjects, actualSubjects);
	}
}
