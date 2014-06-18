package cz.metacentrum.perun.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * This class provide support for compatibility issues.
 * For example for covering differences between oracle and postgree DB.
 */
public class Compatibility {

	private final static Logger log = LoggerFactory.getLogger(Compatibility.class);

	public static boolean isMergeSupported() throws InternalErrorException {
		String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
		return dbType.equals("oracle");
	}

	public static boolean isOracle() throws InternalErrorException {
		String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
		return dbType.equals("oracle");
	}

	public static boolean isPostgreSql() throws InternalErrorException {
		String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
		return dbType.equals("postgresql");
	}

	public static String getSysdate() throws InternalErrorException {
		String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
		if (dbType.equals("oracle")) {
			return "sysdate";
		} else if (dbType.equals("postgresql")) {
			return "'now'";
		} else if (dbType.equals("hsqldb")) {
			return "current_date";
		} else {
			throw new InternalErrorException("unknown DB type");
		}
	}

	public static String getWithClause() {

		try {
			String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
			if (dbType.equals("oracle")) {
				return "with";
			} else if (dbType.equals("postgresql")) {
				return "with recursive";
			} else if (dbType.equals("hsqldb")) {
				return "with recursive";
			} else {
				return "with";
			}
		} catch (InternalErrorException ex) {
			return "with";
		}

	}

	public static String castToVarchar() {

		try {
			String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
			if (dbType.equals("oracle")) {
				return "";
			} else if (dbType.equals("postgresql")) {
				return "::varchar(128)";
			} else {
				return "";
			}
		} catch (InternalErrorException ex) {
			return "";
		}

	}

	public static String castToInteger() {

		try {
			String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
			if (dbType.equals("oracle")) {
				return "";
			} else if (dbType.equals("postgresql")) {
				return "::integer";
			} else {
				return "";
			}
		} catch (InternalErrorException ex) {
			return "";
		}

	}

	public static String getAsAlias(String aliasName) {

		try {
			String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
			if (dbType.equals("oracle")) {
				return "";
			} else if (dbType.equals("postgresql")) {
				return "as "+aliasName;
			} else {
				return "";
			}
		} catch (InternalErrorException ex) {
			return "";
		}

	}

	public static String orderByBinary(String columnName) {

		try {
			String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
			if (dbType.equals("oracle")) {
				return "NLSSORT("+columnName+",'NLS_SORT=BINARY_AI')";
			} else if (dbType.equals("postgresql")) {
				return columnName+" USING ~<~";
			} else {
				return columnName;
			}
		} catch (InternalErrorException ex) {
			return columnName;
		}

	}

	public static String convertToAscii(String columnName) {

		try {
			String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
			if (dbType.equals("oracle")) {
				return "convert("+columnName+", 'US7ASCII', 'UTF8')"; // DESTINATION / SOURCE
			} else if (dbType.equals("postgresql")) {
				return "unaccent("+columnName+")";   // SOURCE  / DESTINATION
			} else {
				return "unaccent("+columnName+")";
			}
		} catch (InternalErrorException ex) {
			return "unaccent("+columnName+")";
		}

	}
	
	public static String toDate(String value, String format) {
		try {
			String dbType = Utils.getPropertyFromConfiguration("perun.db.type");
			if (dbType.equals("oracle")) {
				return "to_date("+value + ", " + format +")";
			} else if (dbType.equals("postgresql")) {
				return "to_timestamp("+value + ", " + format +")";
			} else {
				return "to_date("+value + ", " + format +")";
			}
		} catch (InternalErrorException ex) {
			return "to_date("+value + ", " + format +")";
		}
			
	}
	
}
