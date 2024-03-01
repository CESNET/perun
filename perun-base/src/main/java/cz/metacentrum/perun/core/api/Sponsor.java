package cz.metacentrum.perun.core.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.metacentrum.perun.rpc.stdserializers.LocalDateSerializer;
import java.time.LocalDate;
import java.util.List;

/**
 * Class representing a Sponsor for some specific member. This object contains information
 * about the validity of this sponsorship for the specific member. It also contains additional
 * information about the sponsor so it can be obtained in one API call.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class Sponsor {
  private final User user;
  @JsonSerialize(using = LocalDateSerializer.class)
  private LocalDate validityTo;
  private boolean active;
  private List<UserExtSource> userExtSources;
  private List<Attribute> userAttributes;

  public Sponsor(User user) {
    this.user = user;
  }

  public Sponsor(RichUser user) {
    this.user = user;
    this.userAttributes = user.getUserAttributes();
    this.userExtSources = user.getUserExtSources();
  }

  public LocalDate getValidityTo() {
    return validityTo;
  }

  public void setValidityTo(LocalDate validityTo) {
    this.validityTo = validityTo;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public List<UserExtSource> getUserExtSources() {
    return userExtSources;
  }

  public void setUserExtSources(List<UserExtSource> userExtSources) {
    this.userExtSources = userExtSources;
  }

  public List<Attribute> getUserAttributes() {
    return userAttributes;
  }

  public void setUserAttributes(List<Attribute> userAttributes) {
    this.userAttributes = userAttributes;
  }

  public User getUser() {
    return user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Sponsor sponsor = (Sponsor) o;

    return user != null ? user.equals(sponsor.user) : sponsor.user == null;
  }

  @Override
  public int hashCode() {
    return user != null ? user.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Sponsor[" +
        "user=" + user +
        ", validityTo=" + validityTo +
        ", active=" + active +
        ", userExtSources=" + userExtSources +
        ", userAttributes=" + userAttributes +
        ']';
  }
}
