package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.OrderColumn;
import cz.metacentrum.perun.core.api.PageQuery;
import java.util.function.Function;

/**
 * Class representing columns, that can be used to sort paginated invitations.
 * <p>
 * For each such column, this instances also contain sql parts that are specific for them.
 */
public enum InvitationsOrderColumn implements OrderColumn {
  ID("", "", query -> "invitations.id " + query.getOrder().getSqlValue()),
  STATUS("", "", query -> "invitations.status " + query.getOrder().getSqlValue()),
  EXPIRATION("", "", query -> "invitations.expiration " + query.getOrder().getSqlValue()),
  RECEIVER_NAME("", "", query -> "invitations.receiver_name " + query.getOrder().getSqlValue()),
  RECEIVER_EMAIL("", "", query -> "invitations.receiver_email " + query.getOrder().getSqlValue()),
  SENDER_NAME("", "",
      query -> "users.last_name " + getLangSql(query) + query.getOrder().getSqlValue() + ", " + "users.first_name " +
                   getLangSql(query) + query.getOrder().getSqlValue());

  private final Function<InvitationsPageQuery, String> orderBySqlFunction;
  private final String selectSql;
  private final String joinSql;

  InvitationsOrderColumn(String selectSql, String joinSql, Function<InvitationsPageQuery, String> sqlFunction) {
    this.selectSql = selectSql;
    this.joinSql = joinSql;
    this.orderBySqlFunction = sqlFunction;
  }

  private static String getLangSql(InvitationsPageQuery query) {
    return "";
    // TODO add support for other languages
  }

  public String getSqlJoin() {
    return this.joinSql;
  }

  public String getSqlOrderBy(PageQuery query) {
    return this.orderBySqlFunction.apply((InvitationsPageQuery) query);
  }

  public String getSqlSelect() {
    return this.selectSql;
  }
}
