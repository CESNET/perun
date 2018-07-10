package cz.metacentrum.perun.core.impl;

import static org.junit.Assert.assertEquals;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.DBVersion;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * @author Simona Kruppova 410315
 * @author Oliver Mrazik 410035
 */
public class DatabaseManagerImplIntegrationTest extends AbstractPerunIntegrationTest{

	private DatabaseManagerBl dbManager;

	@Before
	public void setUp() throws Exception {
		dbManager = perun.getDatabaseManagerBl();
		System.out.println("DB information: "+dbManager.getDatabaseInformation());
	}

	@Test(expected=InternalErrorException.class)
	public void getChangelogVersionsOrder() throws Exception {
		dbManager.getChangelogVersions("2.2.4", "changelogPatternTests/versionOrderTestFile.txt");
	}

	@Test(expected=InternalErrorException.class)
	public void getChangelogVersionsTwoSame() throws Exception {
		dbManager.getChangelogVersions("2.2.4", "changelogPatternTests/versionTwoSameTestFile.txt");
	}

	@Test(expected=InternalErrorException.class)
	public void getChangelogVersionsEmptyFile() throws Exception {
		dbManager.getChangelogVersions("", "changelogPatternTests/emptyTestFile.txt");
	}

	@Test(expected=InternalErrorException.class)
	public void getChangelogVersionsDepth() throws Exception {
		dbManager.getChangelogVersions("", "changelogPatternTests/vPatternTest-depth.txt");
	}

	@Test(expected=InternalErrorException.class)
	public void getChangelogVersionsPeriod() throws Exception {
		dbManager.getChangelogVersions("", "changelogPatternTests/vPatternTest-period.txt");
	}

	@Test(expected=InternalErrorException.class)
	public void getChangelogVersionsSpace() throws Exception {
		dbManager.getChangelogVersions("", "changelogPatternTests/vPatternTest-space.txt");
	}

	@Test(expected=InternalErrorException.class)
	public void getChangelogVersionsZero() throws Exception {
		dbManager.getChangelogVersions("", "changelogPatternTests/vPatternTest-zero.txt");
	}

	@Test
	public void getChangelogVersions() throws Exception {
		List<DBVersion> versions = dbManager.getChangelogVersions("2.2.4", "changelogPatternTests/correctTestFile.txt");
		assertEquals("It should load 2 new database versions from a file", 2, versions.size());
	}
}
