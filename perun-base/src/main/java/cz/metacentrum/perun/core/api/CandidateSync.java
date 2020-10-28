package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Candidate used in group synchronization.
 * @author Metodej Klang
 */
public class CandidateSync extends User {

	private List<RichUserExtSource> richUserExtSources;
	private Map<String, String> attributes;

	public CandidateSync() {
	}

	public CandidateSync(Candidate candidate) {
		this.setFirstName(candidate.getFirstName());
		this.setLastName(candidate.getLastName());
		this.setMiddleName(candidate.getMiddleName());
		this.setTitleAfter(candidate.getTitleAfter());
		this.setTitleBefore(candidate.getTitleBefore());
		this.setServiceUser(candidate.isServiceUser());
		this.setSponsoredUser(candidate.isSponsoredUser());
		setAttributes(candidate.getAttributes());
		setRichUserExtSources(candidate.getUserExtSources().stream().map(extSource ->
			new RichUserExtSource(extSource, new ArrayList<>())).collect(Collectors.toList()));
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public List<RichUserExtSource> getRichUserExtSources() {
		return richUserExtSources;
	}

	public void setRichUserExtSources(List<RichUserExtSource> richUserExtSources) {
		this.richUserExtSources = richUserExtSources;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result
			+ ((richUserExtSources == null) ? 0 : richUserExtSources.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CandidateSync other = (CandidateSync) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		if (richUserExtSources == null) {
			return other.richUserExtSources == null;
		} else return richUserExtSources.equals(other.richUserExtSources);
	}
}
