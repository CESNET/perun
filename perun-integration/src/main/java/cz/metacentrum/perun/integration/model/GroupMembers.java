package cz.metacentrum.perun.integration.model;

import java.util.Set;

public record GroupMembers(
	Integer groupId,
	Set<Integer> memberIds
) { }
