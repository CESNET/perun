package cz.metacentrum.perun.integration.daoImpl;

import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.integration.dao.IntegrationManagerDao;
import cz.metacentrum.perun.integration.model.GroupMemberRelation;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

public class IntegrationManagerDaoImpl implements IntegrationManagerDao {

  private static final ResultSetExtractor<List<GroupMemberRelation>> GROUP_MEMBERS_EXTRACTOR = (resultSet) -> {
    List<GroupMemberRelation> relations = new ArrayList<>();

    while (resultSet.next()) {
      var groupId = resultSet.getInt("group_id");
      var memberId = resultSet.getInt("member_id");
      var userId = resultSet.getInt("user_id");
      var sourceGroupId = resultSet.getInt("source_group_id");
      var groupName = resultSet.getString("group_name");
      var parentGroupId = resultSet.getInt("parent_group_id");
      var memberGroupStatus = MemberGroupStatus.getMemberGroupStatus(resultSet.getInt("source_group_status"));
      var type = MembershipType.getMembershipType(resultSet.getInt("membership_type"));

      relations.add(
          new GroupMemberRelation(groupId, memberId, userId, sourceGroupId, groupName, parentGroupId, memberGroupStatus,
              type));
    }

    return relations;
  };
  private JdbcPerunTemplate jdbc;

  @Override
  public List<GroupMemberRelation> getGroupMemberRelations(PerunSession sess) {
    return jdbc.query(
        "SELECT gm.member_id AS member_id, gm.group_id AS group_id, gm.source_group_id AS source_group_id, " +
        "gm.source_group_status AS source_group_status, gm.membership_type AS membership_type, " +
        "g.name AS group_name, g.parent_group_id AS parent_group_id, m.user_id AS user_id " +
        " FROM groups_members gm JOIN groups g ON gm.group_id = g.id JOIN members m ON gm.member_id = m.id",
        GROUP_MEMBERS_EXTRACTOR);
  }

  public void setDataSource(DataSource dataSource) {
    this.jdbc = new JdbcPerunTemplate(dataSource);
  }
}
