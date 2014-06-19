package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public class DatabaseManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	Pattern versionPatter = Pattern.compile("^[1-9][0-9]*[.][1-9][0-9]*[.][1-9][0-9]*");
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void getCurrentDBVersion() throws Exception {
		System.out.println("DatabaseManager.getCurrentDBVersion");
		String dbVersion = perun.getDatabaseManager().getCurrentDatabaseVersion(sess);
		Matcher versionMatcher = versionPatter.matcher(dbVersion);
		assertTrue("DbVersion must match to something like '1.0.0'", versionMatcher.matches());
	}
	
	@Test
	public void getDatabaseDriverInformation() throws Exception {
		System.out.println("DatabaseManager.getDatabaseDriverInformation");
		String driverInfo = perun.getDatabaseManager().getDatabaseDriverInformation(sess);
		assertTrue("DB driver info can't be empty", !driverInfo.isEmpty());
	}
	
	@Test
	public void getDatabaseInformation() throws Exception {
		System.out.println("DatabaseManager.getDatabaseDriverInformation");
		String dbInfo = perun.getDatabaseManager().getDatabaseInformation(sess);
		assertTrue("DB info can't be empty", !dbInfo.isEmpty());
	}
}
