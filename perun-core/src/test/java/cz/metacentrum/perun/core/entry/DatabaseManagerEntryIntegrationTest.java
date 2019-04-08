package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Integration tests of DatabaseManager.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class DatabaseManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private final String DATABASE_MANAGER = "DatabaseManager";
	final Pattern versionPatter = Pattern.compile("^[1-9][0-9]*[.][1-9][0-9]*[.][1-9][0-9]*");
	
	@Before
	public void setUp() {
	}

	@Test
	public void getCurrentDBVersion() throws Exception {
		System.out.println(DATABASE_MANAGER+".getCurrentDBVersion");
		String dbVersion = perun.getDatabaseManager().getCurrentDatabaseVersion(sess);
		Matcher versionMatcher = versionPatter.matcher(dbVersion);
		assertTrue("DBVersion must match to something like '1.0.0'", versionMatcher.matches());
	}
	
	@Test
	public void getDatabaseDriverInformation() throws Exception {
		System.out.println(DATABASE_MANAGER+".getDatabaseDriverInformation");
		String driverInfo = perun.getDatabaseManager().getDatabaseDriverInformation(sess);
		assertTrue("DB driver info can't be empty", !driverInfo.isEmpty());
	}
	
	@Test
	public void getDatabaseInformation() throws Exception {
		System.out.println(DATABASE_MANAGER+".getDatabaseDriverInformation");
		String dbInfo = perun.getDatabaseManager().getDatabaseInformation(sess);
		assertTrue("DB info can't be empty", !dbInfo.isEmpty());
	}

}
