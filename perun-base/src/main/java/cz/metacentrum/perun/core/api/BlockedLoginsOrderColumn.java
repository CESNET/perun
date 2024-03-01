package cz.metacentrum.perun.core.api;

import java.util.function.Function;

/**
 * Class representing columns, that can be used to sort paginated blocked logins.
 * <p>
 * For each such column, this instances also contain sql parts that are specific for them.
 */
public enum BlockedLoginsOrderColumn {
  LOGIN("", "", query -> "login " + query.getOrder().getSqlValue()),
  NAMESPACE("", "", query -> "namespace " + query.getOrder().getSqlValue());

  private final Function<BlockedLoginsPageQuery, String> orderBySqlFunction;
  private final String selectSql;
  private final String joinSql;

  BlockedLoginsOrderColumn(String selectSql, String joinSql, Function<BlockedLoginsPageQuery, String> sqlFunction) {
    this.selectSql = selectSql;
    this.joinSql = joinSql;
    this.orderBySqlFunction = sqlFunction;
  }

  private static String getLangSql(BlockedLoginsPageQuery query) {
    return "";
    // TODO add support for other languages
  }

  public String getSqlOrderBy(BlockedLoginsPageQuery query) {
    return this.orderBySqlFunction.apply(query);
  }

  public String getSqlSelect() {
    return this.selectSql;
  }

  public String getSqlJoin() {
    return this.joinSql;
  }
}
