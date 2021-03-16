package cz.metacentrum.perun.core.api;

import java.util.function.Function;

/**
 * Class representing columns, that can be used to sort paginated members.
 *
 * For each such column, this instances also contain sql parts that are specific for them.
 * This class can be extended, in the future, if for example, we would like to sort by some attributes.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public enum MembersOrderColumn {
	NAME(
			", users.first_name, users.last_name ",
			" LEFT JOIN users on members.user_id = users.id ",
			query -> "users.last_name " + getLangSql(query) + query.getOrder().getSqlValue() + ", " +
	                 "users.first_name " + getLangSql(query) + query.getOrder().getSqlValue()
	),

	ID("", "", query -> "members.id " + query.getOrder().getSqlValue());

	private final Function<MembersPageQuery, String> orderBySqlFunction;
	private final String selectSql;
	private final String joinSql;

	MembersOrderColumn(String selectSql, String joinSql, Function<MembersPageQuery, String> sqlFunction) {
		this.selectSql = selectSql;
		this.joinSql = joinSql;
		this.orderBySqlFunction = sqlFunction;
	}

	public String getSqlOrderBy(MembersPageQuery query) {
		return this.orderBySqlFunction.apply(query);
	}

	public String getSqlSelect() {
		return this.selectSql;
	}

	public String getSqlJoin() {
		return this.joinSql;
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
}
