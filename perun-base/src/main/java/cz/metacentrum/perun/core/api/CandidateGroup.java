package cz.metacentrum.perun.core.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Group obtained from an extSource with the login and login of its parent in the external source.
 * It can be then created as a group of a virtual organization in Perun.
 *
 * @author Peter Balcirak peter.balcirak@gmail.com
 * @date 8/30/17.
 */
public class CandidateGroup extends Auditable {

  private final Map<String, String> additionalAttributes = new HashMap<>();
  private ExtSource extSource;
  private String login;
  private String parentGroupLogin;
  private Group group;

  public CandidateGroup() {
    this.group = new Group();
  }

  public ExtSource getExtSource() {
    return extSource;
  }

  public void setExtSource(ExtSource extSource) {
    this.extSource = extSource;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getParentGroupLogin() {
    return parentGroupLogin;
  }

  public void setParentGroupLogin(String parentGroupLogin) {
    this.parentGroupLogin = parentGroupLogin;
  }

  public Group asGroup() {
    return group;
  }

  public Map<String, String> getAdditionalAttributes() {
    return Collections.unmodifiableMap(additionalAttributes);
  }

  public void addAdditionalAttribute(String urn, String value) {
    this.additionalAttributes.put(urn, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    CandidateGroup that = (CandidateGroup) o;
    return Objects.equals(getExtSource(), that.getExtSource()) &&
        Objects.equals(getLogin(), that.getLogin()) &&
        Objects.equals(getParentGroupLogin(), that.getParentGroupLogin()) &&
        Objects.equals(asGroup(), that.asGroup());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getExtSource(), getLogin(), getParentGroupLogin(), asGroup());
  }

  @Override
  public String toString() {
    return "CandidateGroup{" +
        "extSource=" + extSource +
        ", login='" + login + +'\'' +
        ", parentGroupLogin='" + parentGroupLogin + '\'' +
        ", group=" + group +
        ", additionalAttributes=" + additionalAttributes +
        '}';
  }

  @Override
  public String serializeToString() {
    StringBuilder str = new StringBuilder();

    return str.append(this.getClass().getSimpleName())
        .append(":[extSource=<")
        .append(getExtSource() == null ? "\\0" : getExtSource().serializeToString())
        .append(">, login=<")
        .append(getLogin())
        .append(">, parentGroupLogin=<")
        .append(getParentGroupLogin())
        .append(">, group=<")
        .append(asGroup() == null ? "\\0" : asGroup().serializeToString())
        .append(">, additionalAttributes=<")
        .append(BeansUtils.serializeMapToString(additionalAttributes))
        .append(">]").toString();
  }
}
