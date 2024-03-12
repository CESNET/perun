package cz.metacentrum.perun.core.api;

import java.util.function.Function;

/**
 * Class representing columns, that can be used to sort paginated groups.
 * <p>
 * For each such column, this instances also contain sql parts that are specific for them. This class can be extended,
 * in the future, if for example, we would like to sort by some attributes.
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public enum GroupsOrderColumn {
  ID("", "", query -> "groups_id " + query.getOrder().getSqlValue()),
  NAME("", "", query -> "groups_name " + query.getOrder().getSqlValue()),
  DESCRIPTION("", "", query -> "groups_dsc " + query.getOrder().getSqlValue());

  private final Function<GroupsPageQuery, String> orderBySqlFunction;
  private final String selectSql;
  private final String joinSql;

  GroupsOrderColumn(String selectSql, String joinSql, Function<GroupsPageQuery, String> sqlFunction) {
    this.selectSql = selectSql;
    this.joinSql = joinSql;
    this.orderBySqlFunction = sqlFunction;
  }

  private static String getLangSql(GroupsPageQuery query) {
    return "";
    // TODO add support for other languages
  }

  public String getSqlJoin() {
    return this.joinSql;
  }

  public String getSqlOrderBy(GroupsPageQuery query) {
    return this.orderBySqlFunction.apply(query);
  }

  public String getSqlSelect() {
    return this.selectSql;
  }
}
