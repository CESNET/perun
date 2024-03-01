package cz.metacentrum.perun.core.api;

import java.util.function.Function;

/**
 * Class representing columns, that can be used to sort paginated users.
 * <p>
 * For each such column, this instances also contain sql parts that are specific for them.
 * This class can be extended, in the future, if for example, we would like to sort by some attributes.
 *
 * @author Metodej Klang
 */
public enum UsersOrderColumn {
  NAME(
      ", users.first_name, users.last_name ",
      "",
      query -> "users.last_name " + getLangSql(query) + query.getOrder().getSqlValue() + ", " +
          "users.first_name " + getLangSql(query) + query.getOrder().getSqlValue()
  ),

  ID("", "", query -> "users.id " + query.getOrder().getSqlValue());

  private final Function<UsersPageQuery, String> orderBySqlFunction;
  private final String selectSql;
  private final String joinSql;

  UsersOrderColumn(String selectSql, String joinSql, Function<UsersPageQuery, String> sqlFunction) {
    this.selectSql = selectSql;
    this.joinSql = joinSql;
    this.orderBySqlFunction = sqlFunction;
  }

  private static String getLangSql(UsersPageQuery query) {
    return "";
    // TODO add support for other languages
  }

  public String getSqlOrderBy(UsersPageQuery query) {
    return this.orderBySqlFunction.apply(query);
  }

  public String getSqlSelect() {
    return this.selectSql;
  }

  public String getSqlJoin() {
    return this.joinSql;
  }
}
