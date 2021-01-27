package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * This class provide support for compatibility issues.
 * For example for covering differences between oracle and postgres DB.
 */
public class Compatibility {

	public static boolean isOracle() {
		return "oracle".equals(getDbType());
	}

	public static boolean isPostgreSql() {
		return "postgresql".equals(getDbType());
	}

	static String getSequenceNextval(String sequenceName) {
		switch (getDbType()) {
			case "oracle":
				return sequenceName + ".nextval";
			case "postgresql":
				return "nextval('" + sequenceName + "')";
			default:
				throw new InternalErrorException("Unsupported DB type");
		}
	}

	static String getLockTable(String tableName) {
		switch (getDbType()) {
			case "oracle":
			case "postgresql":
				return "LOCK TABLE "+tableName+" IN EXCLUSIVE MODE";
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	public static String getStructureForInClause() {
		switch (getDbType()) {
			case "oracle":
				return " IN (SELECT * FROM TABLE(?)) ";
			case "postgresql":
				return " = ANY(?) ";
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	public static String getTrue() {
		switch (getDbType()) {
			case "oracle":
				return "'1'";
			case "postgresql":
				return "TRUE";
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	public static String getFalse() {
		switch (getDbType()) {
			case "oracle":
				return "'0'";
			case "postgresql":
				return "FALSE";
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	public static String getSysdate() {
		switch (getDbType()) {
			case "oracle":
				return "sysdate";
			case "postgresql":
				return "statement_timestamp()";
			default:
				throw new InternalErrorException("unknown DB type");
		}
	}

	static Object getDate(long dateInMiliseconds) {
		switch (getDbType()) {
			case "oracle":
				return new Date(dateInMiliseconds);
			case "postgresql":
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

	public static String castToInteger() {
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
		return ",row_number() over (ORDER BY id DESC) as rownumber";
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
