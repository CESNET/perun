package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Candidate used in group synchronization.
 *
 * @author Metodej Klang
 */
public class CandidateSync extends User {

  private RichUserExtSource richUserExtSource;
  private List<RichUserExtSource> additionalRichUserExtSources;
  private Map<String, String> attributes;
  private String expectedSyncGroupStatus;

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
    this.setExpectedSyncGroupStatus(candidate.getExpectedSyncGroupStatus());
    setAttributes(candidate.getAttributes());
    setRichUserExtSource(new RichUserExtSource(candidate.getUserExtSource(), new ArrayList<>()));
    if (candidate.getAdditionalUserExtSources() != null) {
      setAdditionalRichUserExtSources(candidate.getAdditionalUserExtSources().stream().map(extSource ->
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

  public String getExpectedSyncGroupStatus() {
    return expectedSyncGroupStatus;
  }

  public void setExpectedSyncGroupStatus(String expectedSyncGroupStatus) {
    this.expectedSyncGroupStatus = expectedSyncGroupStatus;
  }

  public List<RichUserExtSource> getUserExtSources() {
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((attributes == null) ? 0 : attributes.hashCode());
    result = prime * result
        + ((getUserExtSources() == null) ? 0 : getUserExtSources().hashCode());
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
    if (getUserExtSources() == null) {
      return other.getUserExtSources() == null;
    } else {
      return getUserExtSources().equals(other.getUserExtSources());
    }
  }
}
