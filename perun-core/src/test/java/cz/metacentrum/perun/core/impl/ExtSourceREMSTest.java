package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
		extSourceREMS.setPerunBl(perunBl);
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
		Connection con = mock(Connection.class);
		DataSource dataSource = mock(DataSource.class);
		doReturn(dataSource).when(extSourceREMS).getDataSource();
		doReturn(con).when(dataSource).getConnection();
		PreparedStatement preparedStatement = mock(PreparedStatement.class);
		doReturn(preparedStatement).when(con).prepareStatement(mapOfAttributes.get("usersQuery"));
		ResultSet resultSet = mock(ResultSet.class, RETURNS_DEEP_STUBS);
		doReturn(resultSet).when(preparedStatement).executeQuery();
		ResultSetMetaData metaData = mock(ResultSetMetaData.class);
		doReturn(2).when(metaData).getColumnCount();
		doReturn("firstName").when(metaData).getColumnLabel(1);
		doReturn("login").when(metaData).getColumnLabel(2);
		doReturn(metaData).when(resultSet).getMetaData();
		doReturn(true, false).when(resultSet).next();
		doReturn("josef").when(resultSet).getString(1);
		doReturn("xjosef").when(resultSet).getString(2);
		User user = new User();
		user.setFirstName("josef");
		when(perunBl.getUsersManagerBl().getUsersByExtSourceTypeAndLogin(any(), anyString(), eq("xjosef"))).thenReturn(Collections.singletonList(user));

		// create expected subject to get
		List<Map<String, String>> expectedSubjects = new ArrayList<>();
		Map<String, String> subject = new HashMap<>();
		subject.put("firstName", "josef");
		subject.put("login", "xjosef");
		expectedSubjects.add(subject);

		// test the method
		List<Map<String, String>> actualSubjects = extSourceREMS.getUsersSubjects();
		assertEquals("subjects should be same", expectedSubjects, actualSubjects);
	}
}
