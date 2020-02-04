package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.DBVersion;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.DatabaseManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Database manager can work with database version and upgraded state of perun DB.
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public class DatabaseManagerImpl implements DatabaseManagerImplApi {
	final static Logger log = LoggerFactory.getLogger(DatabaseManagerImpl.class);
	private static JdbcPerunTemplate jdbc;

	public static final String VERSION_PROPERTY = "DATABASE VERSION";
	public static final String PERFORMANCE_PROPERTY = "LAST PERFORMANCE TEST TIMESTAMP";

	public DatabaseManagerImpl(DataSource perunPool) {
		jdbc = new JdbcPerunTemplate(perunPool);
		jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	@Override
	public String getCurrentDatabaseVersion() throws InternalErrorException {
		try {
			return jdbc.queryForObject("select value from configurations where property=?", String.class, VERSION_PROPERTY);
		} catch(EmptyResultDataAccessException ex) {
			throw new ConsistencyErrorException(ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public String getDatabaseDriverInformation() throws InternalErrorException {
		if (jdbc.getDataSource() == null) return "Data source is NULL.";
		try (Connection con = jdbc.getDataSource().getConnection()) {
			String driverVersion = con.getMetaData().getDriverVersion();
			String driverName = con.getMetaData().getDriverName();
			return driverName + "-" + driverVersion;
		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public String getDatabaseInformation() throws InternalErrorException {
		if (jdbc.getDataSource() == null) return "Data source is NULL.";
		try (Connection con = jdbc.getDataSource().getConnection()) {
			String dbName = con.getMetaData().getDatabaseProductName();
			String dbVersion = con.getMetaData().getDatabaseProductVersion();
			return dbName + "-" + dbVersion;
		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public String getCodeDatabaseVersion(List<DBVersion> dbVersions, String currentDBVersion) {
		if(dbVersions.isEmpty()) return currentDBVersion;
		return dbVersions.get(0).getVersion();
	}

	@Override
	public void updateDatabaseVersion(List<DBVersion> dbVersions) throws InternalErrorException {
		Collections.reverse(dbVersions);

		for (DBVersion v : dbVersions) {
			log.debug("Executing update commands of version " + v.getVersion());
			List<String> successfulCommands = new ArrayList<>();
			for (String c : v.getCommands()) {
				try {
					jdbc.execute(c);
					log.debug("Command executed: " + c);
					successfulCommands.add(c);
				} catch(EmptyResultDataAccessException ex) {
					log.error("Update unsuccessful. All versions before " + v.getVersion() + " were successfully executed. " +
							"Error executing command in version " + v.getVersion() + ": " + c, ex);
					log.error("Successful commands from " + v.getVersion() + ": " + successfulCommands);
					throw new ConsistencyErrorException("Update unsuccessful. Error executing command in version " + v.getVersion() + ": " + c, ex);
				} catch(RuntimeException ex) {
					log.error("Update unsuccessful. All versions before " + v.getVersion() + " were successfully executed. " +
							"Error executing command in version " + v.getVersion() + ": " + c, ex);
					log.error("Successful commands from " + v.getVersion() + ": " + successfulCommands);
					throw new InternalErrorException("Update unsuccessful. Error executing command in version " + v.getVersion() + ": " + c, ex);
				}
			}
		}
	}

	@Override
	public List<DBVersion> getChangelogVersions(String currentDBVersion, String fileName) throws InternalErrorException {

		Pattern versionPattern = Pattern.compile("^[1-9][0-9]*[.][0-9]+[.][0-9]+");
		Pattern commentPattern = Pattern.compile("^--.*");

		List<DBVersion> versions = new ArrayList<>();
		boolean versionFound = false;
		Resource resource = new ClassPathResource(fileName);

		try(BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {

			String line = br.readLine();
			while (line != null){
				line = line.trim();

				// ignore empty lines and comments at the start of the file and between versions
				if(line.isEmpty() || commentPattern.matcher(line).matches()) {
					line = br.readLine();
					continue;
				}

				if(versionPattern.matcher(line).matches()){

					//saving version
					DBVersion version = new DBVersion(line);

					//comparing two last version numbers
					if(!versions.isEmpty()) {
						DBVersion previousVersion = versions.get(versions.size() - 1);
						if(version.compareTo(previousVersion) >= 0) {
							throw new InternalErrorException("Version numbers in changelog file are not increasing properly. " +
									"Version " + previousVersion.getVersion() + " should be higher than " + version.getVersion());
						}
					}

					//the current db version was found, all new versions and their commands were saved to versions list
					if(line.equals(currentDBVersion)){
						versionFound = true;
						break;
					}

					List<String> commands = new ArrayList<>();

					while((line = br.readLine()) != null) {
						line = line.trim();

						// empty line means end of current version
						if(line.isEmpty()) {
							break;
						}
						commands.add(line);
					}

					//saving version commands
					version.setCommands(commands);

					versions.add(version);
				} else {
					throw new InternalErrorException("Version does not match the pattern required in " + fileName);
				}
			}
			if(!versionFound) {
				throw new InternalErrorException("Version " + currentDBVersion + " not found in " + fileName);
			}
		} catch(IOException e) {
			throw new InternalErrorException("Error reading " + fileName, e);
		}

		return versions;
	}

	@Override
	public long getTimeOfQueryPerformance() {
		//Begin of query
		long startTime = System.nanoTime();

		try {
			jdbc.update("update configurations set value=" + Compatibility.getSysdate() + " where property=?", PERFORMANCE_PROPERTY);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		//End of query
		long endTime = System.nanoTime();

		return endTime-startTime;
	}

	@Override
	public void createProperty(String property) {
		try {
			jdbc.update("insert into configurations (property, value) values (?,?)", property, "N/A");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean propertyExists(String property) {
		Utils.notNull(property, "property");

		try {
			return 1 == jdbc.queryForInt("select count(1) from configurations where property=?", property);
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public JdbcPerunTemplate getJdbcPerunTemplate() {
		return jdbc;
	}

}
