package cz.metacentrum.perun.integration.model;

import cz.metacentrum.perun.core.api.Attribute;

import java.util.Objects;
import java.util.Set;

public record MemberWithAttributes(
	Integer memberId,
	Set<Attribute> memberGroupAttributes
) {
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MemberWithAttributes that = (MemberWithAttributes) o;
		return Objects.equals(memberId, that.memberId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(memberId);
	}
}
