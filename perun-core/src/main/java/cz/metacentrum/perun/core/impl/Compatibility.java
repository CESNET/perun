package cz.metacentrum.perun.core.impl;

import java.sql.Timestamp;

/**
 * This class provide support for compatibility issues.
 * For example for covering differences between oracle and postgres DB.
 * Currently supports only postgresql.
 */
public class Compatibility {

  static String getSequenceNextval(String sequenceName) {
    return "nextval('" + sequenceName + "')";
  }

  static String getLockTable(String tableName) {
    return "LOCK TABLE " + tableName + " IN EXCLUSIVE MODE";
  }

  public static String getStructureForInClause() {
    return " = ANY(?) ";
  }

  public static String getTrue() {
    return "TRUE";
  }

  public static String getFalse() {
    return "FALSE";
  }

  public static String getSysdate() {
    return "statement_timestamp()";
  }

  static Object getDate(long dateInMiliseconds) {
    return new Timestamp(dateInMiliseconds);
  }

  static String castToVarchar() {
    return "::varchar(128)";
  }

  public static String castToInteger() {
    return "::integer";
  }

  static String getAsAlias(String aliasName) {
    return "as " + aliasName;
  }

  static String getRowNumberOver() {
    return ",row_number() over (ORDER BY id DESC) as rownumber";
  }

  static String orderByBinary(String columnName) {
    return columnName + " USING ~<~";
  }

  static String convertToAscii(String columnName) {
    return "unaccent(" + columnName + ")";
  }

  public static String toDate(String value, String format) {
    return "to_timestamp(" + value + ", " + format + ")";
  }

}
