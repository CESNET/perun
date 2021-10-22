package cz.metacentrum.perun.integration.daoImpl;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.integration.dao.IntegrationManagerImplApi;
import cz.metacentrum.perun.integration.model.GroupMembers;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class IntegrationManagerImpl implements IntegrationManagerImplApi {

	private JdbcPerunTemplate jdbc;

	private static final ResultSetExtractor<List<GroupMembers>> GROUP_MEMBERS_EXTRACTOR = (resultSet) -> {
		Map<Integer, Set<Integer>> membersByGroups = new HashMap<>();

		while(resultSet.next()) {
			int groupId = resultSet.getInt("group_id");
			int memberId = resultSet.getInt("member_id");

			if (!membersByGroups.containsKey(groupId)) {
				membersByGroups.put(groupId, new HashSet<>());
			}

			membersByGroups.get(groupId).add(memberId);
		}

		return membersByGroups.entrySet().stream()
			.map((entry) -> new GroupMembers(entry.getKey(), entry.getValue()))
			.collect(toList());
	};

	@Override
	public List<GroupMembers> getGroupMemberRelations(PerunSession sess) {
		return jdbc.query("SELECT member_id, group_id FROM groups_members",	GROUP_MEMBERS_EXTRACTOR);
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbc = new JdbcPerunTemplate(dataSource);
	}
}
