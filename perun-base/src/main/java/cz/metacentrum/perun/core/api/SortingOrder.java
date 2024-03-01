package cz.metacentrum.perun.core.api;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public enum SortingOrder {
  ASCENDING("ASC"),
  DESCENDING("DESC");

  private final String sqlValue;

  SortingOrder(String sqlValue) {
    this.sqlValue = sqlValue;
  }

  public String getSqlValue() {
    return sqlValue;
  }
}
