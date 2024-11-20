package cz.metacentrum.perun.core.api;

import java.util.Objects;

public class PersonalDataChangeConfig {
  private boolean enableLinkedName;
  private boolean enableCustomName;
  private boolean customNameRequiresApprove;
  private boolean enableLinkedOrganization;
  private boolean enableCustomOrganization;
  private boolean customOrganizationRequiresApprove;
  private boolean enableLinkedEmail;
  private boolean enableCustomEmail;
  private boolean customEmailRequiresVerification;

  public PersonalDataChangeConfig() {}

  public boolean isEnableLinkedName() {
    return enableLinkedName;
  }

  public void setEnableLinkedName(boolean enableLinkedName) {
    this.enableLinkedName = enableLinkedName;
  }

  public boolean isEnableCustomName() {
    return enableCustomName;
  }

  public void setEnableCustomName(boolean enableCustomName) {
    this.enableCustomName = enableCustomName;
  }

  public boolean isCustomNameRequiresApprove() {
    return customNameRequiresApprove;
  }

  public void setCustomNameRequiresApprove(boolean customNameRequiresApprove) {
    this.customNameRequiresApprove = customNameRequiresApprove;
  }

  public boolean isEnableLinkedOrganization() {
    return enableLinkedOrganization;
  }

  public void setEnableLinkedOrganization(boolean enableLinkedOrganization) {
    this.enableLinkedOrganization = enableLinkedOrganization;
  }

  public boolean isEnableCustomOrganization() {
    return enableCustomOrganization;
  }

  public void setEnableCustomOrganization(boolean enableCustomOrganization) {
    this.enableCustomOrganization = enableCustomOrganization;
  }

  public boolean isCustomOrganizationRequiresApprove() {
    return customOrganizationRequiresApprove;
  }

  public void setCustomOrganizationRequiresApprove(boolean customOrganizationRequiresApprove) {
    this.customOrganizationRequiresApprove = customOrganizationRequiresApprove;
  }

  public boolean isEnableLinkedEmail() {
    return enableLinkedEmail;
  }

  public void setEnableLinkedEmail(boolean enableLinkedEmail) {
    this.enableLinkedEmail = enableLinkedEmail;
  }

  public boolean isEnableCustomEmail() {
    return enableCustomEmail;
  }

  public void setEnableCustomEmail(boolean enableCustomEmail) {
    this.enableCustomEmail = enableCustomEmail;
  }

  public boolean isCustomEmailRequiresVerification() {
    return customEmailRequiresVerification;
  }

  public void setCustomEmailRequiresVerification(boolean customEmailRequiresVerification) {
    this.customEmailRequiresVerification = customEmailRequiresVerification;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PersonalDataChangeConfig that = (PersonalDataChangeConfig) o;
    return enableLinkedName == that.enableLinkedName &&
                   enableCustomName == that.enableCustomName &&
                   customNameRequiresApprove == that.customNameRequiresApprove &&
                   enableLinkedOrganization == that.enableLinkedOrganization &&
                   enableCustomOrganization == that.enableCustomOrganization &&
                   customOrganizationRequiresApprove == that.customOrganizationRequiresApprove &&
                   enableLinkedEmail == that.enableLinkedEmail &&
                   enableCustomEmail == that.enableCustomEmail &&
                   customEmailRequiresVerification == that.customEmailRequiresVerification;
  }

  @Override
  public int hashCode() {
    return Objects.hash(enableLinkedName, enableCustomName, customNameRequiresApprove, enableLinkedOrganization,
            enableCustomOrganization, customOrganizationRequiresApprove, enableLinkedEmail, enableCustomEmail,
            customEmailRequiresVerification);
  }

  @Override
  public String toString() {
    return "PersonalDataChangeConfig{" +
                   "enableLinkedName=" + enableLinkedName +
                   ", enableCustomName=" + enableCustomName +
                   ", customNameRequiresApprove=" + customNameRequiresApprove +
                   ", enableLinkedOrganization=" + enableLinkedOrganization +
                   ", enableCustomOrganization=" + enableCustomOrganization +
                   ", customOrganizationRequiresApprove=" + customOrganizationRequiresApprove +
                   ", enableLinkedEmail=" + enableLinkedEmail +
                   ", enableCustomEmail=" + enableCustomEmail +
                   ", customEmailRequiresVerification=" + customEmailRequiresVerification +
                   '}';
  }
}
