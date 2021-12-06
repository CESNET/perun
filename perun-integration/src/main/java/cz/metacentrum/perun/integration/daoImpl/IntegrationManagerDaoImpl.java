package cz.metacentrum.perun.integration.daoImpl;

import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.integration.dao.IntegrationManagerDao;
import cz.metacentrum.perun.integration.model.GroupMemberRelation;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class IntegrationManagerDaoImpl implements IntegrationManagerDao {

	private JdbcPerunTemplate jdbc;

	private static final ResultSetExtractor<List<GroupMemberRelation>> GROUP_MEMBERS_EXTRACTOR = (resultSet) -> {
		List<GroupMemberRelation> relations = new ArrayList<>();

		while(resultSet.next()) {
			var groupId = resultSet.getInt("group_id");
			var memberId = resultSet.getInt("member_id");
			var sourceGroupId = resultSet.getInt("source_group_id");
			var memberGroupStatus = MemberGroupStatus.getMemberGroupStatus(resultSet.getInt("source_group_status"));
			var type = MembershipType.getMembershipType(resultSet.getInt("membership_type"));

			relations.add(new GroupMemberRelation(groupId, memberId, sourceGroupId, memberGroupStatus, type));
		}

		return relations;
	};

	@Override
	public List<GroupMemberRelation> getGroupMemberRelations(PerunSession sess) {
		return jdbc.query("SELECT member_id, group_id, source_group_id, source_group_status, membership_type" +
			              " FROM groups_members", GROUP_MEMBERS_EXTRACTOR);
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbc = new JdbcPerunTemplate(dataSource);
	}
}
