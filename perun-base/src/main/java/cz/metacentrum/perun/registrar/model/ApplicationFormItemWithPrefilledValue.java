package cz.metacentrum.perun.registrar.model;

/**
 * Application form item with a value prefilled.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class ApplicationFormItemWithPrefilledValue {

  private ApplicationFormItem formItem;
  private String prefilledValue;
  private String assuranceLevel;
  private boolean generated = false;

  public ApplicationFormItemWithPrefilledValue(ApplicationFormItem formItem, String prefilledValue) {
    this.formItem = formItem;
    this.prefilledValue = prefilledValue;
  }

  public String getAssuranceLevel() {
    return assuranceLevel;
  }

  public void setAssuranceLevel(String assuranceLevel) {
    this.assuranceLevel = assuranceLevel;
  }

  /**
   * Return bean name as PerunBean does.
   *
   * @return Class simple name (beanName)
   */
  public String getBeanName() {
    return this.getClass().getSimpleName();
  }

  public ApplicationFormItem getFormItem() {
    return formItem;
  }

  public void setFormItem(ApplicationFormItem formItem) {
    this.formItem = formItem;
  }

  public String getPrefilledValue() {
    return prefilledValue;
  }

  public void setPrefilledValue(String prefilledValue) {
    this.prefilledValue = prefilledValue;
  }

  public boolean isGenerated() {
    return generated;
  }

  public void setGenerated(boolean generated) {
    this.generated = generated;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + ":[" + "formItem='" + getFormItem().toString() + '\'' +
           ", prefilledValue='" + getPrefilledValue() + '\'' + ", assuranceLevel='" + getAssuranceLevel() + '\'' +
           ", generated='" + isGenerated() + '\'' + ']';
  }

}
