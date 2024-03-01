package cz.metacentrum.perun.core.api;

public class BlockedLogin {

  private int id;
  private String login;
  private String namespace;

  public BlockedLogin(String login, String namespace) {
    this.login = login;
    this.namespace = namespace;
  }

  public BlockedLogin(int id, String login, String namespace) {
    this.id = id;
    this.login = login;
    this.namespace = namespace;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    final BlockedLogin that = (BlockedLogin) obj;
    if (!this.login.equals(that.getLogin())) {
      return false;
    }

    if ((this.namespace == null) ? (that.namespace != null) : !this.namespace.equals(that.namespace)) {
      return false;
    }

    return true;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @Override
  public int hashCode() {
    int result = this.login.hashCode();
    result = 31 * result + (this.namespace != null ? this.namespace.hashCode() : 0);
    return result;
  }
}
