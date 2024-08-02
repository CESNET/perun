package cz.metacentrum.perun.core.api;

public interface OrderColumn {
  public String getSqlJoin();

  public String getSqlOrderBy(PageQuery query);

  public String getSqlSelect();

  public default String getSqlGroupBy() {
    return "";
  }
}
