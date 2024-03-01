package cz.metacentrum.perun.integration.model;

import cz.metacentrum.perun.core.api.Attribute;
import java.util.List;
import java.util.Map;

public record GroupMemberData(
    List<GroupMemberRelation> relations,
    Map<Integer, Map<Integer, List<Attribute>>> groupMemberAttributes
) {
}
