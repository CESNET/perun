package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * This class provide support for compatibility issues.
 * For example for covering differences between oracle and postgree DB.
 */
public class Compatibility {

	public static boolean isOracle() {
		return "oracle".equals(getDbType());
	}

	public static boolean isPostgreSql() {
		return "postgresql".equals(getDbType());
	}

	public static boolean isHSQLDB() {
		return "hsqldb".equals(getDbType());
	}

	static String getSequenceNextval(String sequenceName) throws InternalErrorException {
		switch (getDbType()) {
			case "oracle":
				return sequenceName + ".nextval";
			case "postgresql":
				return "nextval('" + sequenceName + "')";
			case "hsqldb":
				return "next value for " + sequenceName;
			default:
				throw new InternalErrorException("Unsupported DB type");
		}
	}

	static String getLockTable(String tableName) throws InternalErrorException {
		switch (getDbType()) {
			case "oracle":
			case "postgresql":
				return "LOCK TABLE "+tableName+" IN EXCLUSIVE MODE";
			case "hsqldb":
				return "LOCK TABLE "+tableName+" WRITE";
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	public static String getStructureForInClause() throws InternalErrorException {
		switch (getDbType()) {
			case "oracle":
				return " IN (SELECT * FROM TABLE(?)) ";
			case "postgresql":
				return " = ANY(?) ";
			case "hsqldb":
				return "  IN ( UNNEST(?) ) ";
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	public static String getTrue() throws InternalErrorException {
		switch (getDbType()) {
			case "oracle":
				return "'1'";
			case "postgresql":
				return "TRUE";
			case "hsqldb":
				return "TRUE";
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	public static String getFalse() throws InternalErrorException {
		switch (getDbType()) {
			case "oracle":
				return "'0'";
			case "postgresql":
				return "FALSE";
			case "hsqldb":
				return "FALSE";
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	public static String getSysdate() throws InternalErrorException {
		switch (getDbType()) {
			case "oracle":
				return "sysdate";
			case "postgresql":
				return "statement_timestamp()";
			case "hsqldb":
				return "current_date";
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	static Object getDate(long dateInMiliseconds) throws InternalErrorException {
		switch (getDbType()) {
			case "oracle":
				return new Date(dateInMiliseconds);
			case "postgresql":
				return new Timestamp(dateInMiliseconds);
			case "hsqldb":
				return new Timestamp(dateInMiliseconds);
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	static String castToVarchar() {
		switch (getDbType()) {
			case "oracle":
				return "";
			case "postgresql":
				return "::varchar(128)";
			default:
				return "";
		}
	}

	static String castToInteger() {
		switch (getDbType()) {
			case "oracle":
				return "";
			case "postgresql":
				return "::integer";
			default:
				return "";
		}
	}

	private static String getDbType() {
		return BeansUtils.getCoreConfig().getDbType();
	}

	static String getAsAlias(String aliasName) {
		String dbType = getDbType();
		switch (dbType) {
			case "oracle":
				return " " + aliasName;
			case "postgresql":
				return "as " + aliasName;
			default:
				return " " + aliasName;
		}
	}

	static String getRowNumberOver() {
		if ("hsqldb".equals(getDbType())) {
			return ",row_number() over () as rownumber";
		} else {
			return ",row_number() over (ORDER BY id DESC) as rownumber";
		}
	}

	static String orderByBinary(String columnName) {
		switch (getDbType()) {
			case "oracle":
				return "NLSSORT(" + columnName + ",'NLS_SORT=BINARY_AI')";
			case "postgresql":
				return columnName + " USING ~<~";
			default:
				return columnName;
		}
	}

	static String convertToAscii(String columnName) {
		switch (getDbType()) {
			case "oracle":
				// convert column type to VARCHAR2 from (N)VARCHAR2 and modify encoding from UTF to US7ASCII
				return "to_char(convert(" + columnName + ", 'US7ASCII', 'UTF8'))"; // DESTINATION / SOURCE

			case "postgresql":
				return "unaccent(" + columnName + ")";
			case "hsqldb":
				return "translate(" + columnName + ", 'ÁÇÉÍÓÚÀÈÌÒÙÚÂÊÎÔÛÃÕËÜŮŘřáçéíóúàèìòùâêîôûãõëüů', 'ACEIOUUAEIOUAEIOUAOEUURraceiouaeiouaeiouaoeuu')";
			default:
				return "unaccent(" + columnName + ")";
		}
	}

	public static String toDate(String value, String format) {
		switch (getDbType()) {
			case "oracle":
				return "to_date(" + value + ", " + format + ")";
			case "postgresql":
				return "to_timestamp(" + value + ", " + format + ")";
			default:
				return "to_date(" + value + ", " + format + ")";
		}
	}

}
