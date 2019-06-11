package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.BeansUtils;
import java.util.*;

import cz.metacentrum.perun.registrar.model.Application.AppType;

/**
 * Item of a form for an application to a VO.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class ApplicationFormItem {

	//data fields
	private int id;
	private String shortname;
	private boolean required = false;
	private Type type = Type.TEXTFIELD;
	private String federationAttribute;
	private String perunSourceAttribute;
	private String perunDestinationAttribute;
	private String regex;
	private List<AppType> applicationTypes = Arrays.asList(AppType.INITIAL,AppType.EXTENSION);

	private Integer ordnum;

	/**
	 * Field for GUI purpose - tells updateFromItems()
	 * to delete this item instead of update
	 *
	 * default is FALSE = keep/update item
	 * TRUE = delete item
	 */
	private boolean forDelete = false;

	public static final Locale EN = new Locale("en");
	public static final Locale CS = getNativeLanguage();

	/**
	 * Return code of native language defined in config file.
	 * Return NULL if no native language set.
	 *
	 * @return String representation of native language
	 */
	public static Locale getNativeLanguage() {
		try {
			String loc = BeansUtils.getCoreConfig().getNativeLanguage().split(",")[0];
			if (loc != null && loc.trim().isEmpty()) {
				return null;
			}
			return new Locale(loc);
		} catch (Exception ex) {
			return null;
		}
	}

	public ApplicationFormItem(int id, String shortname, boolean required, Type type, String federationAttribute, String perunSourceAttribute, String perunDestinationAttribute, String regex) {
		this.id = id;
		this.shortname = shortname;
		this.required = required;
		this.type = type;
		this.federationAttribute = federationAttribute;
		this.perunSourceAttribute = perunSourceAttribute;
		this.perunDestinationAttribute = perunDestinationAttribute;
		this.regex = regex;
	}



	public ApplicationFormItem(int id, String shortname, boolean required,
	                           Type type, String federationAttribute,
	                           String perunSourceAttribute, String perunDestinationAttribute, String regex,
	                           List<AppType> applicationTypes, Integer ordnum, boolean forDelete,
	                           Map<Locale, ItemTexts> i18n) {
		this(id, shortname, required, type, federationAttribute, perunSourceAttribute, perunDestinationAttribute, regex);
		this.applicationTypes = applicationTypes;
		this.ordnum = ordnum;
		this.forDelete = forDelete;
		this.i18n = i18n;
	}

	public String getPerunSourceAttribute() {
		return perunSourceAttribute;
	}

	public void setPerunSourceAttribute(String perunSourceAttribute) {
		this.perunSourceAttribute = perunSourceAttribute;
	}

	public String getPerunDestinationAttribute() {
		return perunDestinationAttribute;
	}

	public void setPerunDestinationAttribute(String perunDestinationAttribute) {
		this.perunDestinationAttribute = perunDestinationAttribute;
	}

	public List<AppType> getApplicationTypes() {
		return applicationTypes;
	}

	public void setApplicationTypes(List<AppType> applicationTypes) {
		this.applicationTypes = applicationTypes;
	}

	public Integer getOrdnum() {
		return ordnum;
	}

	public void setOrdnum(Integer ordnum) {
		this.ordnum = ordnum;
	}

	public boolean isForDelete() {
		return forDelete;
	}

	public void setForDelete(boolean forDelete) {
		this.forDelete = forDelete;
	}

	/**
	 * Enumeration for types of application form items. For example text fields, checkboxes and so on.
	 */
	public static enum Type {
		/**
		 * For inserting arbitrary HTML text into the form.
		 */
		HTML_COMMENT,
		/**
		 * For specifying a label for the submit button.
		 */
		SUBMIT_BUTTON,
		/**
		 * For specifying a label for the submit button. When validation of form is OK, then it's
		 * automatically submitted. When validation fails, user have to fix it and submit form manually.
		 */
		AUTO_SUBMIT_BUTTON,
		/**
		 * For read-only fields with values taken from identity federation.
		 */
		FROM_FEDERATION_SHOW,
		/**
		 * For hidden fields with values taken from identity federation.
		 */
		FROM_FEDERATION_HIDDEN,
		/**
		 * For password, which needs to be typed twice.
		 */
		PASSWORD,
		/**
		 * For an email address that must be validated by sending an email with a URL.
		 */
		VALIDATED_EMAIL,
		/**
		 * Standard HTML text field.
		 */
		TEXTFIELD,
		/**
		 * Standard HTML text area.
		 */
		TEXTAREA,
		/**
		 * Standard HTML checkbox, multiple values are allowed.
		 */
		CHECKBOX,
		/**
		 * Standard HTML radio button.
		 */
		RADIO,
		/**
		 * Standard HTML selection box, options are in for each locale in ItemTexts.label separated by | with values separated by #.
		 * Thus a language selection box would have for English locale the label <code>cs#Czech|en#English</code>.
		 */
		SELECTIONBOX,
		/**
		 * A widget that is a combination of a drop-down list and a single-line editable textbox,
		 * allowing the user to either type a value directly into the control or choose from a list of existing options.
		 * See <a href="http://en.wikipedia.org/wiki/Combobox">Combobox</a> for description.<p>
		 * The options are defined in the same way as for SELECTIONBOX.
		 *
		 */
		COMBOBOX,
		/**
		 * Special type for specifying username. It needs to be treated separately from ordinary TEXTFIELD,
		 * because initial applications by users who are already members of a VO need to display read-only.
		 */
		USERNAME,
		/**
		 * Special item defining Heading of application form.
		 */
		HEADING,
		/**
		 * Special item with pre-defined Timezone selection. Value is autoselected in GUI using Google's API
		 */
		TIMEZONE
	}

	public static class ItemTexts {
		private Locale locale;
		private String label;
		private String options; //also used for options for checkboxes, radiobuttons and selections
		private String help;
		private String errorMessage;

		public ItemTexts() {
		}

		public ItemTexts(Locale locale) {
			this.locale = locale;
		}

		public ItemTexts(Locale locale,String label, String options, String help, String errorMessage) {
			this.locale = locale;
			this.label = label;
			this.options = options;
			this.help = help;
			this.errorMessage = errorMessage;
		}

		public Locale getLocale() {
			return locale;
		}

		public void setLocale(Locale locale) {
			this.locale= locale ;
		}

		/**
		 *
		 * @return label(s) for the given locale. For multiple option in radiobutton or selectionbox, values are separated by | and prefixed with values sepearted by #,
		 * i.e. <code>cs#Czech|en#English</code>
		 */
		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getOptions() {
			return options;
		}

		public void setOptions(String options) {
			this.options = options;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		public String getHelp() {
			return help;
		}

		public void setHelp(String help) {
			this.help = help;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ":[" +
					"locale=" + locale +
					", label='" + label + '\'' +
					", options='" + options + '\'' +
					", help='" + help + '\'' +
					", errorMessage='" + errorMessage + '\'' +
					']';
		}
	}

	private Map<Locale,ItemTexts> i18n = new HashMap<Locale, ItemTexts>(); {
		// create with locale property set
		i18n.put(EN,new ItemTexts(EN));
		if (CS != null) {
			i18n.put(CS,new ItemTexts(CS));
		}
	}

	public ApplicationFormItem() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getFederationAttribute() {
		return federationAttribute;
	}

	public void setFederationAttribute(String federationAttribute) {
		this.federationAttribute = federationAttribute;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public Map<Locale, ItemTexts> getI18n() {
		return i18n;
	}

	public ItemTexts getTexts(Locale locale) {
		ItemTexts texts = i18n.get(locale);
		if(texts==null) {
			texts = new ItemTexts();
			i18n.put(locale,texts);
		}
		return texts;
	}

	public void setI18n(Map<Locale, ItemTexts> i18n) {
		this.i18n = i18n;
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
				"id='" + getId() + '\'' +
				", shortname='" + getShortname() + '\'' +
				", ordnum='" + getOrdnum() + '\'' +
				", required='" + isRequired() + '\'' +
				", type='" + getType().toString() + '\'' +
				", federationAttribute='" + getFederationAttribute() + '\'' +
				", regex='" + getRegex() + '\'' +
				", i18n='" + getI18n().toString() + '\'' +
				"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApplicationFormItem other = (ApplicationFormItem) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
