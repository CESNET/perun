package cz.metacentrum.perun.registrar.model;

import java.util.function.Function;

/**
 * Class representing columns, that can be used to sort paginated applications.
 * <p>
 * For each such column, this instances also contain sql parts that are specific for them. This class can be extended,
 * in the future, if for example, we would like to sort by some attributes.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public enum ApplicationsOrderColumn {
  ID("", "", query -> "id " + query.getOrder().getSqlValue()),
  DATE_CREATED("", "", query -> "app_created_at " + query.getOrder().getSqlValue()),
  TYPE("", "", query -> "apptype " + query.getOrder().getSqlValue()),
  STATE("", "", query -> "state " + query.getOrder().getSqlValue()),
  SUBMITTER("", "", query -> "app_created_by " + query.getOrder().getSqlValue()),
  GROUP_NAME("", "", query -> "group_name " + query.getOrder().getSqlValue()),
  MODIFIED_BY("", "", query -> "app_modified_by " + query.getOrder().getSqlValue());

  private final Function<ApplicationsPageQuery, String> orderBySqlFunction;
  private final String selectSql;
  private final String joinSql;

  ApplicationsOrderColumn(String selectSql, String joinSql, Function<ApplicationsPageQuery, String> sqlFunction) {
    this.selectSql = selectSql;
    this.joinSql = joinSql;
    this.orderBySqlFunction = sqlFunction;
  }

  private static String getLangSql(ApplicationsPageQuery query) {
    return "";

  }

  public String getSqlJoin() {
    return this.joinSql;
  }

  public String getSqlOrderBy(ApplicationsPageQuery query) {
    return this.orderBySqlFunction.apply(query);
  }

  public String getSqlSelect() {
    return this.selectSql;
  }
}
