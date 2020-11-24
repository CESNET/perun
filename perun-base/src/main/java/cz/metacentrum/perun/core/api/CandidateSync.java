package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Candidate used in group synchronization.
 * @author Metodej Klang
 */
public class CandidateSync extends User {

	private RichUserExtSource richUserExtSource;
	private List<RichUserExtSource> additionalRichUserExtSources;
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
		setRichUserExtSource(new RichUserExtSource(candidate.getUserExtSource(), new ArrayList<>()));
		if (candidate.getAdditionalUserExtSources() != null) {
			setAdditionalRichUserExtSources(candidate.getAdditionalUserExtSources().stream().map(extSource ->
				new RichUserExtSource(extSource, new ArrayList<>())).collect(Collectors.toList()));
		}
	}

	public CandidateSync(User user, UserExtSource userExtSource, Map<String, String> attributes, List<UserExtSource> additionalUserExtSources) {
		if(user != null) {
			this.setFirstName(user.getFirstName());
			this.setLastName(user.getLastName());
			this.setMiddleName(user.getMiddleName());
			this.setTitleAfter(user.getTitleAfter());
			this.setTitleBefore(user.getTitleBefore());
			this.setServiceUser(user.isServiceUser());
			this.setSponsoredUser(user.isSponsoredUser());
		}
		this.richUserExtSource = new RichUserExtSource(userExtSource, new ArrayList<>());
		this.attributes = attributes;
		if (additionalUserExtSources != null) {
			setAdditionalRichUserExtSources(additionalUserExtSources.stream().map(extSource ->
				new RichUserExtSource(extSource, new ArrayList<>())).collect(Collectors.toList()));
		}
	}

	public RichUserExtSource getRichUserExtSource() {
		return richUserExtSource;
	}

	public void setRichUserExtSource(RichUserExtSource richUserExtSource) {
		this.richUserExtSource = richUserExtSource;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public List<RichUserExtSource> getAdditionalRichUserExtSources() {
		return additionalRichUserExtSources;
	}

	public void setAdditionalRichUserExtSources(List<RichUserExtSource> additionalRichUserExtSources) {
		this.additionalRichUserExtSources = additionalRichUserExtSources;
	}

	public List<RichUserExtSource> getRichUserExtSources() {
		List<RichUserExtSource> userExtSources = new ArrayList<>();
		if (this.richUserExtSource != null) {
			userExtSources.add(this.richUserExtSource);
		}
		if (this.additionalRichUserExtSources != null) {
			userExtSources.addAll(this.additionalRichUserExtSources);
		}
		return Collections.unmodifiableList(userExtSources);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		Map<String, String> attrNew = null;
		if(attributes != null) attrNew = new HashMap<>(attributes);
		if(attrNew != null) {
			Set<String> keys = new HashSet<>(attrNew.keySet());
			for(String attrName : keys) {
				attrNew.put('\'' + attrName + '\'', '\'' + attrNew.get(attrName) + '\'');
				attrNew.remove(attrName);
			}
		}
		return str.append(getClass().getSimpleName()).append(":[userExtSource='").append(richUserExtSource)
			.append("', attributes='").append(attrNew).append("', additionalUserExtSources='").append(additionalRichUserExtSources)
			.append("']").toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result
			+ ((getRichUserExtSources() == null) ? 0 : getRichUserExtSources().hashCode());
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
		if (getRichUserExtSources() == null) {
			return other.getRichUserExtSources() == null;
		} else return getRichUserExtSources().equals(other.getRichUserExtSources());
	}
}
