package cz.metacentrum.perun.core.api;

import java.util.function.Function;

/**
 * Class representing columns, that can be used to sort paginated members.
 * <p>
 * For each such column, this instances also contain sql parts that are specific for them. This class can be extended,
 * in the future, if for example, we would like to sort by some attributes.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public enum MembersOrderColumn implements OrderColumn {
  NAME(", users.first_name, users.last_name ", "", ", users.last_name, users.first_name",
      query -> "users.last_name " + getLangSql(query) + query.getOrder().getSqlValue() + ", " + "users.first_name " +
               getLangSql(query) + query.getOrder().getSqlValue()),

  ID("", "", query -> "members.id " + query.getOrder().getSqlValue()),
  STATUS("", "", "", query -> "members.status " + query.getOrder().getSqlValue()), GROUP_STATUS("", "",
      ", groups_members.group_id, groups_members.source_group_id, groups_members.membership_type," +
      " groups_members.source_group_status",
      query -> "groups_members.source_group_status " + query.getOrder().getSqlValue()),

  // 1. user preferred mail, 2. member mail
  EMAIL(", usrvals.attr_value, memvals.attr_value ",
      " left join " + "(select attr_value, member_id, attr_id from member_attr_values) as memvals " +
      "on members.id=memvals.member_id and memvals.attr_id=" +
      "(select id from attr_names where attr_name='urn:perun:member:attribute-def:def:mail') " + " left join " +
      "(select attr_value, user_id, attr_id from user_attr_values) as usrvals " +
      "on members.user_id=usrvals.user_id and usrvals.attr_id=" +
      "(select id from attr_names where attr_name='urn:perun:user:attribute-def:def:preferredMail') ",
      ", usrvals.attr_value, memvals.attr_value ",
      query -> "usrvals.attr_value " + query.getOrder().getSqlValue() + ", " + "memvals.attr_value " +
               query.getOrder().getSqlValue()),

  // 1. member organization, 2. user organization (from IdP)
  ORGANIZATION(", usrvals.attr_value, memvals.attr_value ",
      " left join " + "(select attr_value, member_id, attr_id from member_attr_values) as memvals " +
      "on members.id=memvals.member_id and memvals.attr_id=" +
      "(select id from attr_names where attr_name='urn:perun:member:attribute-def:def:organization') " + " left join " +
      "(select attr_value, user_id, attr_id from user_attr_values) as usrvals " +
      "on members.user_id=usrvals.user_id and usrvals.attr_id=" +
      "(select id from attr_names where attr_name='urn:perun:user:attribute-def:def:organization') ",
      ", usrvals.attr_value, memvals.attr_value ",
      query -> "memvals.attr_value " + query.getOrder().getSqlValue() + ", " + "usrvals.attr_value " +
               query.getOrder().getSqlValue());

  private final Function<MembersPageQuery, String> orderBySqlFunction;
  private final String selectSql;
  private final String joinSql;
  private final String groupbySql;

  MembersOrderColumn(String selectSql, String joinSql, String groupbySql,
                     Function<MembersPageQuery, String> sqlFunction) {
    this.selectSql = selectSql;
    this.joinSql = joinSql;
    this.groupbySql = groupbySql;
    this.orderBySqlFunction = sqlFunction;
  }

  MembersOrderColumn(String selectSql, String joinSql, Function<MembersPageQuery, String> sqlFunction) {
    this.selectSql = selectSql;
    this.joinSql = joinSql;
    this.groupbySql = "";
    this.orderBySqlFunction = sqlFunction;
  }

  private static String getLangSql(MembersPageQuery query) {
    return "";
    // TODO add support for other languages
    // to make this work, Czech collation has to be created in DB - create COLLATION cs_CZ (locale="cs_CZ.UTF-8");
    // However, this COLLATION can be only created, if the OS has this language installed. Therefore, we must install
    // this language in the test container and also, we need to make sure in is available at the production.

    // We also need to figure out, when to use the Czech collation. Maybe we can create a Vo attribute, that could
    // be set for this purposes, or we can create a new property in the Perun configuration.
    //
    // " collate \"cs_CZ\" "
  }

  public String getSqlGroupBy() {
    return this.groupbySql;
  }

  public String getSqlJoin() {
    return this.joinSql;
  }

  public String getSqlOrderBy(PageQuery query) {
    return this.orderBySqlFunction.apply((MembersPageQuery) query);
  }

  public String getSqlSelect() {
    return this.selectSql;
  }
}
