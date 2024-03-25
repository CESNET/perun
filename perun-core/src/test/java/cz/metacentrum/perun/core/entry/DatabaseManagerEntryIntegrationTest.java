package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests of DatabaseManager.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class DatabaseManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

  final Pattern versionPatter = Pattern.compile("^[1-9][0-9]*[.][1-9][0-9]*[.][1-9][0-9]*");
  private final String DATABASE_MANAGER = "DatabaseManager";

  @Test
  public void createProperty() throws Exception {
    System.out.println(DATABASE_MANAGER + ".createProperty");
    String property = "TEST";
    perun.getDatabaseManagerBl().createProperty(property);
    assertTrue(perun.getDatabaseManagerBl().propertyExists(property));
  }

  @Test(expected = InternalErrorException.class)
  public void createTwiceTheSameProperty() throws Exception {
    System.out.println(DATABASE_MANAGER + ".createTwiceTheSameProperty");
    String property = "TEST";
    perun.getDatabaseManagerBl().createProperty(property);
    perun.getDatabaseManagerBl().createProperty(property);
  }

  @Test
  public void getCurrentDBVersion() throws Exception {
    System.out.println(DATABASE_MANAGER + ".getCurrentDBVersion");
    String dbVersion = perun.getDatabaseManager().getCurrentDatabaseVersion(sess);
    Matcher versionMatcher = versionPatter.matcher(dbVersion);
    assertTrue("DBVersion must match to something like '1.0.0'", versionMatcher.matches());
  }

  @Test
  public void getDatabaseDriverInformation() throws Exception {
    System.out.println(DATABASE_MANAGER + ".getDatabaseDriverInformation");
    String driverInfo = perun.getDatabaseManager().getDatabaseDriverInformation(sess);
    assertTrue("DB driver info can't be empty", !driverInfo.isEmpty());
  }

  @Test
  public void getDatabaseInformation() throws Exception {
    System.out.println(DATABASE_MANAGER + ".getDatabaseDriverInformation");
    String dbInfo = perun.getDatabaseManager().getDatabaseInformation(sess);
    assertTrue("DB info can't be empty", !dbInfo.isEmpty());
  }

  @Test
  public void getTimeOfQueryPerformance() throws Exception {
    System.out.println(DATABASE_MANAGER + ".getTimeOfQueryPerformance");
    assertTrue(perun.getDatabaseManager().getTimeOfQueryPerformance(sess) > 0);
  }

  @Test
  public void propertyExists() throws Exception {
    System.out.println(DATABASE_MANAGER + ".propertyExists");
    String property = "TEST";
    assertFalse(perun.getDatabaseManagerBl().propertyExists(property));
  }

  @Before
  public void setUp() {
  }
}
