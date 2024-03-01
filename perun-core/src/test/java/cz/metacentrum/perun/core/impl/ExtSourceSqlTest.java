package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
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
public class ExtSourceSqlTest extends AbstractPerunIntegrationTest {

  @Spy
  private static ExtSourceSql extSourceSql;

  private static PerunBlImpl perunBl;

  @Before
  public void setUp() throws Exception {
    extSourceSql = new ExtSourceSql();

    perunBl = mock(PerunBlImpl.class, RETURNS_DEEP_STUBS);

    MockitoAnnotations.initMocks(this);
    extSourceSql.setPerunBl(perunBl);
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
    Connection con = mock(Connection.class);
    DataSource dataSource = mock(DataSource.class);
    doReturn(dataSource).when(extSourceSql).getDataSource();
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

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("firstName", "josef");
    subject.put("login", "xjosef");
    expectedSubjects.add(subject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceSql.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }
}
