package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Role;

@Deprecated
public class AuthzRole {
  private Role role;
  private PerunBean complementaryObject;

  public AuthzRole(Role role, PerunBean complemenetaryObject) {
    this.role = role;
    this.complementaryObject = complemenetaryObject;
  }

  public AuthzRole(Role role) {
    this.role = role;
    this.complementaryObject = null;
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
    AuthzRole other = (AuthzRole) obj;
    if (complementaryObject == null) {
      if (other.complementaryObject != null) {
        return false;
      }
    } else if (!complementaryObject.equals(other.complementaryObject)) {
      return false;
    }
    if (role != other.role) {
      return false;
    }
    return true;
  }

  public PerunBean getComplementaryObject() {
    return complementaryObject;
  }

  public Role getRole() {
    return role;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((complementaryObject == null) ? 0 : complementaryObject.hashCode());
    result = prime * result + ((role == null) ? 0 : role.hashCode());
    return result;
  }

  public void setComplementaryObject(PerunBean complementaryObject) {
    this.complementaryObject = complementaryObject;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public String toString() {
    return getClass().getSimpleName() + "role='" + ((role == null) ? "null" : role) + "', " + "complementaryObject='" +
           ((complementaryObject == null) ? "null" : complementaryObject + "']");
  }
}
