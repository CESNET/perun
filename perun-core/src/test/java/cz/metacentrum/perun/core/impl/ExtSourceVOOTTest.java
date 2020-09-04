package cz.metacentrum.perun.core.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Metodej Klang
 */
public class ExtSourceVOOTTest {

	@Spy
	private static ExtSourceVOOT extSourceVOOT;

	@Before
	public void setUp() throws Exception {
		extSourceVOOT = new ExtSourceVOOT();

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getUsersSubjectsTest() throws Exception {
		System.out.println("getUsersSubjectsTest");

		// mock connection
		HttpURLConnection http = mock(HttpURLConnection.class);
		doReturn(http).when(extSourceVOOT).createConnection("uriMembership");
		String input = "{\"entry\":[{\"id\":\"1\"}]}";
		InputStream is = new ByteArrayInputStream(input.getBytes());
		doReturn(is).when(http).getInputStream();

		HttpURLConnection http2 = mock(HttpURLConnection.class);
		doReturn(http2).when(extSourceVOOT).createConnection("uriMembers/1");
		String input2 = "{\"entry\":[{\"id\":\"xmorales@1\",\"login\":xmorales@miles.sp,\"emails\":[{\"value\":miles@morales.sp}]}]}";
		InputStream is2 = new ByteArrayInputStream(input2.getBytes());
		doReturn(is2).when(http2).getInputStream();

		// define needed attributes
		Map<String, String> mapOfAttributes = new HashMap<>();
		mapOfAttributes.put("usersQuery", "id=xmorales@1");
		mapOfAttributes.put("uriMembership", "uriMembership");
		mapOfAttributes.put("uriMembers", "uriMembers");
		mapOfAttributes.put("vootMapping", "login={login},\nemail={email}");
		doReturn(mapOfAttributes).when(extSourceVOOT).getAttributes();

		// create expected subject to get
		List<Map<String, String>> expectedSubjects = new ArrayList<>();
		Map<String, String> subject = new HashMap<>();
		subject.put("login", "xmorales");
		subject.put("email", "miles@morales.sp");
		expectedSubjects.add(subject);

		// test the method
		List<Map<String, String>> actualSubjects = extSourceVOOT.getUsersSubjects();
		assertEquals("subjects should be same", expectedSubjects, actualSubjects);
	}
}
