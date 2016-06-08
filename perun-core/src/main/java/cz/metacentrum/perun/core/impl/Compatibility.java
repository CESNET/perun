package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * This class provide support for compatibility issues.
 * For example for covering differences between oracle and postgree DB.
 */
public class Compatibility {

	private final static Logger log = LoggerFactory.getLogger(Compatibility.class);

	public static boolean isMergeSupported() throws InternalErrorException {
		String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
		return dbType.equals("oracle");
	}

	public static boolean isOracle() throws InternalErrorException {
		String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
		return dbType.equals("oracle");
	}

	public static boolean isPostgreSql() throws InternalErrorException {
		String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
		return dbType.equals("postgresql");
	}

	public static boolean isHSQLDB() throws InternalErrorException {
		String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
		return dbType.equals("hsqldb");
	}

	public static String getSysdate() throws InternalErrorException {
		String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
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

	public static Object getDate(long dateInMiliseconds) throws InternalErrorException {
		String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
		if (dbType.equals("oracle")) {
			return new Date(dateInMiliseconds);
		} else if (dbType.equals("postgresql")) {
			return new Timestamp(dateInMiliseconds);
		} else if (dbType.equals("hsqldb")) {
			return new Timestamp(dateInMiliseconds);
		} else {
			throw new InternalErrorException("unknown DB type");
		}
	}

	public static String castToVarchar() {

		try {
			String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
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
			String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
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
			String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
			if (dbType.equals("oracle")) {
				return " "+aliasName;
			} else if (dbType.equals("postgresql")) {
				return "as "+aliasName;
			} else {
				return " "+aliasName;
			}
		} catch (InternalErrorException ex) {
			return "";
		}

	}

	public static String getRowNumberOver() {
		try {
			String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
			if (dbType.equals("hsqldb")) {
				return ",row_number() over () as rownumber";
			} else {
				return ",row_number() over (ORDER BY id DESC) as rownumber";
			}
		} catch (InternalErrorException e) {
			return ",row_number() over (ORDER BY id DESC) as rownumber";
		}
	}

	public static String orderByBinary(String columnName) {

		try {
			String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
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
			String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
			if (dbType.equals("oracle")) {
				// convert column type to VARCHAR2 from (N)VARCHAR2 and modify encoding from UTF to US7ASCII
				return "to_char(convert("+columnName+", 'US7ASCII', 'UTF8'))"; // DESTINATION / SOURCE
			} else if (dbType.equals("postgresql")) {
				return "unaccent("+columnName+")";
			} else if (dbType.equals("hsqldb")){
				return "translate("+columnName+", 'ÁÇÉÍÓÚÀÈÌÒÙÚÂÊÎÔÛÃÕËÜŮŘřáçéíóúàèìòùâêîôûãõëüů', 'ACEIOUUAEIOUAEIOUAOEUURraceiouaeiouaeiouaoeuu')";
			} else {
				return "unaccent("+columnName+")";
			}
		} catch (InternalErrorException ex) {
			return "unaccent("+columnName+")";
		}

	}
	
	public static String toDate(String value, String format) {
		try {
			String dbType = BeansUtils.getPropertyFromConfiguration("perun.db.type");
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
