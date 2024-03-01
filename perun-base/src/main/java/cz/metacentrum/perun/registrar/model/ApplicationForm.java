package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;

/**
 * Application form of a VO. Use {@link cz.metacentrum.perun.registrar.RegistrarManager#getFormItems} for items.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class ApplicationForm {

  private final List<String> moduleClassNames = new ArrayList<>();
  private int id;
  private Vo vo;
  private Group group;
  private boolean automaticApproval;
  private boolean automaticApprovalExtension;
  private boolean automaticApprovalEmbedded;

  public ApplicationForm() {
  }

  public void addModuleClassName(String moduleClassName) {
    if (StringUtils.hasText(moduleClassName)) {
      this.moduleClassNames.add(moduleClassName);
    }
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
    ApplicationForm other = (ApplicationForm) obj;
    if (id != other.id) {
      return false;
    }
    return true;
  }

  /**
   * Return bean name as PerunBean does.
   *
   * @return Class simple name (beanName)
   */
  public String getBeanName() {
    return this.getClass().getSimpleName();
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<String> getModuleClassNames() {
    return new ArrayList<>(moduleClassNames);
  }

  public void setModuleClassNames(List<String> moduleClassNames) {
    this.moduleClassNames.clear();
    for (String moduleClassName : moduleClassNames) {
      if (StringUtils.hasText(moduleClassName)) {
        this.moduleClassNames.add(moduleClassName);
      }
    }
  }

  public Vo getVo() {
    return vo;
  }

  public void setVo(Vo vo) {
    this.vo = vo;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  public boolean isAutomaticApproval() {
    return automaticApproval;
  }

  public void setAutomaticApproval(boolean automaticApproval) {
    this.automaticApproval = automaticApproval;
  }

  public boolean isAutomaticApprovalEmbedded() {
    return automaticApprovalEmbedded;
  }

  public void setAutomaticApprovalEmbedded(boolean automaticApprovalEmbedded) {
    this.automaticApprovalEmbedded = automaticApprovalEmbedded;
  }

  public boolean isAutomaticApprovalExtension() {
    return automaticApprovalExtension;
  }

  public void setAutomaticApprovalExtension(boolean automaticApproval) {
    this.automaticApprovalExtension = automaticApproval;
  }

  public void removeModuleClassName(String moduleClassName) {
    if (StringUtils.hasText(moduleClassName)) {
      this.moduleClassNames.remove(moduleClassName);
    }
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + ":[" + "id='" + getId() + '\'' + ", vo='" + getVo() + '\'' + ", group='" +
           getGroup() + '\'' + ", automaticApproval='" + isAutomaticApproval() + '\'' +
           ", automaticApprovalExtension='" + isAutomaticApprovalExtension() + '\'' + ", moduleClassNames='" +
           getModuleClassNames() + '\'' + "]";
  }

}
