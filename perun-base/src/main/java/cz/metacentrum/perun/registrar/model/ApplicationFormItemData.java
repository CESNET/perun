package cz.metacentrum.perun.registrar.model;

/**
 * Application form data as submitted by a user.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class ApplicationFormItemData {

	private Integer id;
	private ApplicationFormItem formItem;
	private String shortname;
	private String value;
	private String assuranceLevel;
	private String prefilledValue = "";

	public ApplicationFormItemData() {
	}

	public ApplicationFormItemData(ApplicationFormItem formItem, String shortname, String value, String assuranceLevel) {
		this.formItem = formItem;
		this.shortname = shortname;
		this.value = value;
		this.assuranceLevel = assuranceLevel;
	}

	public ApplicationFormItemData(Integer id, ApplicationFormItem formItem, String shortname, String value, String assuranceLevel) {
		this.id = id;
		this.formItem = formItem;
		this.shortname = shortname;
		this.value = value;
		this.assuranceLevel = assuranceLevel;
	}

	public ApplicationFormItemData(ApplicationFormItem formItem, String shortname, String value, String prefilled, String assuranceLevel) {
		this(formItem, shortname, value, assuranceLevel);
		this.prefilledValue = prefilled;
	}

	public ApplicationFormItemData(Integer id, ApplicationFormItem formItem, String shortname, String value, String prefilled, String assuranceLevel) {
		this(id, formItem, shortname, value, assuranceLevel);
		this.prefilledValue = prefilled;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ApplicationFormItem getFormItem() {
		return formItem;
	}

	public void setFormItem(ApplicationFormItem formItem) {
		this.formItem = formItem;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getAssuranceLevel() {
		return assuranceLevel;
	}

	public void setAssuranceLevel(String assuranceLevel) {
		this.assuranceLevel = assuranceLevel;
	}

	public String getPrefilledValue() {
		return prefilledValue;
	}

	public void setPrefilledValue(String prefilledValue) {
		this.prefilledValue = prefilledValue;
	}

	/**
	 * Return bean name as PerunBean does.
	 *
	 * @return Class simple name (beanName)
	 */
	public String getBeanName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+":[" +
			"formItem='" + getFormItem().toString() +  '\'' +
			", shortname='" + getShortname() +  '\'' +
			", value='" + getValue() +  '\'' +
			", prefilledValue='" + getPrefilledValue() +  '\'' +
			", assuranceLevel='"+ getAssuranceLevel() +  '\''
			+"]";
	}

}
