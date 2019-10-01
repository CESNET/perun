package cz.metacentrum.perun.core.api;


import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Object for representation of database version.
 *
 * @author Simona Kruppova 410315
 * @author Oliver Mrazik 410035
 */
public class DBVersion implements Comparable<DBVersion>{
	private String version;
	private List<String> commands;

	public DBVersion(String version) {
		Pattern versionPattern = Pattern.compile("^[1-9][0-9]*[.][0-9]+[.][0-9]+");
		if(!versionPattern.matcher(version).matches()){
			throw new IllegalArgumentException("Version format is invalid.");
		}
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[version='")
				.append(version).append("', commands='").append(commands).append("']").toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DBVersion dbVersion = (DBVersion) o;

		if (!version.equals(dbVersion.version)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return version.hashCode();
	}

	@Override
	public int compareTo(DBVersion version) {
		if(version == null) {
			throw new InternalErrorException(new NullPointerException("DBVersion version"));
		}

		String[] thisSplitVersion = this.version.split("\\.");
		String[] splitVersion = version.getVersion().split("\\.");

		//each version should have three numbers split by dots
		for (int n = 0; n < 3; n++) {
			if(Integer.parseInt(thisSplitVersion[n]) > Integer.parseInt(splitVersion[n])) return 1;
			if(Integer.parseInt(thisSplitVersion[n]) < Integer.parseInt(splitVersion[n])) return -1;
		}
		return 0;
	}
}