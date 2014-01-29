package cz.metacentrum.perun.core.api;



/**
 * Basic class. All beans (entitties) in Perun must extends this class.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */

public abstract class PerunBean {
  private int id;

  public PerunBean() {
  }

  public PerunBean(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  /** Returns bean name like VO, Member, Resource,... 
   */
  public String getBeanName() {
    return this.getClass().getSimpleName();
  }


  @Override
  public String toString() {
    return this.getClass().getSimpleName()+":[" +
      "id='" + id + '\'' +
      ']';
  }


  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + this.id;
    hash = 53 * hash + this.getBeanName().hashCode();
    return hash;
  }

  public String serializeToString() {
      return this.getClass().getSimpleName()+":[" +
      "id=<" + id + ">" +
      ']'; 
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PerunBean other = (PerunBean) obj;

    if (!this.getBeanName().equals(other.getBeanName())) {
      return false;
    }

    if (this.id != other.id) {
      return false;
    }
    return true;
  }
}
