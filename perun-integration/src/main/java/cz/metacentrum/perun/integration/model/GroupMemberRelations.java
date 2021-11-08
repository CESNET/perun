package cz.metacentrum.perun.integration.model;

import java.util.Set;

public record GroupMemberRelations(
	Integer groupId,
	Set<MemberWithAttributes> memberWithAttributes
) { }
