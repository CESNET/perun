package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Host with list of all its attributes
 *
 * @author Metodej Klang <metodej.klang@gmail.com>
 */
public class EnrichedHost {

  private Host host;
  private List<Attribute> hostAttributes;

  public EnrichedHost() {
  }

  public EnrichedHost(Host host, List<Attribute> hostAttributes) {
    this.host = host;
    this.hostAttributes = hostAttributes;
  }

  public Host getHost() {
    return host;
  }

  public void setHost(Host host) {
    this.host = host;
  }

  public List<Attribute> getHostAttributes() {
    return hostAttributes;
  }

  public void setHostAttributes(List<Attribute> hostAttributes) {
    this.hostAttributes = hostAttributes;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();

    return str.append("EnrichedHost:[host='").append(host.toString()).append("', hostAttributes='")
        .append(hostAttributes).append("']").toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnrichedHost that = (EnrichedHost) o;
    return Objects.equals(host, that.getHost());
  }

  @Override
  public int hashCode() {
    return Objects.hash(host);
  }
}
