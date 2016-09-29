package cz.metacentrum.perun.webgui.client.applicationresources;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.*;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.applicationresources.FormInputStatusWidget.Status;
import cz.metacentrum.perun.webgui.client.localization.ApplicationMessages;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.usersManager.IsLoginAvailable;
import cz.metacentrum.perun.webgui.model.ApplicationFormItem;
import cz.metacentrum.perun.webgui.model.ApplicationFormItemWithPrefilledValue;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.ItemTexts;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.*;

/**
 * Creates GWT widgets from the ApplicationFormItems
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RegistrarFormItemGenerator {

	/**
	 * Namespace position
	 */
	private static final int PERUN_ATTRIBUTE_LOGIN_NAMESPACE_POSITION = 49;

	/**
	 * Variable width calculated for all TextBox inputs
	 */
	private static final int WIDTH_PER_CHAR = 10;
	private static final int MIN_WIDTH = 250;
	private static final int MAX_WIDTH = 500;

	/**
	 * Max. length params for input fields common (text box) and text area.
	 */
	private static final int TEXT_BOX_MAX_LENGTH = 512;
	private static final int TEXT_AREA_MAX_LENGTH = 3999;

	private int RADIO_COUNTER = 0;

	/**
	 * Item with type definition
	 */
	private ApplicationFormItem item;

	/**
	 * Whether to show label (input), or not (button)
	 */
	private boolean showLabel = true;

	/**
	 * The input widget itself
	 */
	private Widget widget;

	/**
	 * String value
	 */
	private String strValue = "";

	/**
	 * prefilled value
	 */
	private String prefilledValue = "";

	/**
	 * String value wrapper
	 */
	private ValueBoxBase<String> strValueBox;

	/**
	 * LOA
	 */
	private String assuranceLevel = "";

	/**
	 * Whether to show it (not hidden)
	 */
	private boolean visible = true;

	/**
	 * Input checker
	 */
	private FormInputChecker inputChecker;


	/**
	 * Status cell
	 */
	private SimplePanel statusCellWrapper = new SimplePanel();

	private FormValidator validationTrigger;


	private boolean visibleOnlyToAdmin = false;

	/**
	 * Counter for nameing textbox classes
	 */
	private static int counter = 0;

	/**
	 * Default locale
	 */
	private String locale = "en";

	/**
	 * Create new item with prefilled value and assurance level
	 *
	 * @param withValue
	 * @param locale
	 */
	public RegistrarFormItemGenerator(ApplicationFormItemWithPrefilledValue withValue, String locale) {
		this(withValue.getFormItem(), withValue.getValue(), locale);
		this.assuranceLevel = withValue.getAssuranceLevel();
	}

	/**
	 * Widget generator instance
	 * @param item
	 * @param locale
	 */
	public RegistrarFormItemGenerator(ApplicationFormItem item, String locale) {
		this(item, "", locale);
	}

	/**
	 * Widget generator instance
	 * @param item
	 * @param strValue
	 * @param locale
	 */
	public RegistrarFormItemGenerator(final ApplicationFormItem item, String strValue, String locale) {

		if(strValue == null || strValue.equals("null")) {
			strValue = "";
		}

		this.strValue = strValue.trim();
		this.prefilledValue = strValue.trim();  // store original value
		this.item = item;
		if ((!Utils.getNativeLanguage().isEmpty() && locale.equalsIgnoreCase(Utils.getNativeLanguage().get(0))) || locale.equalsIgnoreCase("en")) {
			// set locale if correct
			this.locale = locale;
		}

		// fix value for mails from federation
		if ("mail".equalsIgnoreCase(item.getFederationAttribute())) {
			// multiple emails can be returned from RPC, they are separated by semi-colon
			if (strValue != null && !strValue.isEmpty()) {
				// split mails and use first one
				String[] emails = strValue.split(";");
				strValue = emails[0];
				this.strValue = emails[0].trim();
			}
		}

		widget = generateWidget();

		if(inputChecker == null){
			inputChecker = getDefaultInputChecker();
		}

		// when changed, check
		whenChangedCheck();
	}

	private FormInputChecker getDefaultInputChecker() {
		// default input checker - if item required and box empty -> not valid & check regex
		return new FormInputChecker() {

			private boolean valid = true;

			public boolean isValid(boolean forceNewValidation) {

				// if hidden, true
				if(!isVisible()){
					return true;
				}

				if(!forceNewValidation){
					return valid;
				}

				// missing?
				valid = (!(item.isRequired() && getValue().equals("")));
				if(!valid && !item.getType().equalsIgnoreCase("FROM_FEDERATION_SHOW")){
					// from_federation_show can be valid when empty
					statusCellWrapper.setWidget(new FormInputStatusWidget(ApplicationMessages.INSTANCE.missingValue(), Status.ERROR));
					return false;
				}

				// length
				valid = checkLength();
				if(!valid){
					return false;
				}

				// is regex?
				valid = checkValueRegex();
				return valid;
			}

			public boolean isValidating() {
				return false;
			}

			public boolean useDefaultOkMessage() {
				return true;
			}
		};
	}


	protected boolean checkValueRegex(){
		if(item.getRegex() != null && !("".equals(item.getRegex()))){

			// Compile and use regular expression
			RegExp regExp = RegExp.compile(item.getRegex());
			MatchResult matcher = regExp.exec(strValueBox.getValue());
			boolean matchFound = (matcher != null); // equivalent to regExp.test(inputStr);

			if(!matchFound){

				String errorMessage = ApplicationMessages.INSTANCE.incorrectFormat();

				// does a custom message exist?
				ItemTexts it = item.getItemTexts(locale);
				if(it != null){
					if(it.getErrorMessage() != null && !it.getErrorMessage().equals("")){
						errorMessage = it.getErrorMessage();
					}
				}

				statusCellWrapper.setWidget(new FormInputStatusWidget(errorMessage, Status.ERROR));
				return false;
			}
		}
		return true;
	}

	protected boolean checkLength() {

		boolean tooLong = false;
		String value = getValue();
		if (value != null) {
			if ("TEXTAREA".equalsIgnoreCase(item.getType())) {
				if (value.length() > TEXT_AREA_MAX_LENGTH) {
					tooLong = true;
				}
			} else {
				if (value.length() > TEXT_BOX_MAX_LENGTH) {
					tooLong = true;
				}
			}
		}
		if(tooLong){
			String errorMessage = ApplicationMessages.INSTANCE.inputTextIsTooLong();

			statusCellWrapper.setWidget(new FormInputStatusWidget(errorMessage, Status.ERROR));
		}

		return !tooLong;

	}


	private void whenChangedCheck() {

		if(strValueBox == null) return;

		if ("USERNAME".equalsIgnoreCase(item.getType())) {

			// Check USERNAME only when leaving text box

			strValueBox.addBlurHandler(new BlurHandler() {
				public void onBlur(BlurEvent event) {

					// check validity
					if(inputChecker.isValid(true)){
						// is valid AND value not empty
						if(!strValueBox.getText().equals(""))
						{
							// default OK?
							if(inputChecker.useDefaultOkMessage())
							{
								statusCellWrapper.setWidget(new FormInputStatusWidget("OK", Status.OK));
							}
						}else{
							// input empty - clear;
							statusCellWrapper.clear();
						}
					}

					// update
					if(validationTrigger == null) return;
					validationTrigger.triggerValidation();

				}
			});

			strValueBox.addKeyUpHandler(new KeyUpHandler() {
				public void onKeyUp(KeyUpEvent event) {
					DomEvent.fireNativeEvent(Document.get().createBlurEvent(), strValueBox);
				}
			});

		} else {

			// Check OTHER when typing in text box

			strValueBox.addKeyUpHandler(new KeyUpHandler() {
				public void onKeyUp(KeyUpEvent event) {
					DomEvent.fireNativeEvent(Document.get().createChangeEvent(), strValueBox);
				}
			});

			// is triggered manually by onBlur + onPaste
			strValueBox.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					// check validity
					if (inputChecker.isValid(true)) {
						// is valid AND value not empty
						if (!strValueBox.getText().equals("")) {
							// default OK?
							if (inputChecker.useDefaultOkMessage()) {
								statusCellWrapper.setWidget(new FormInputStatusWidget("OK", Status.OK));
							}
						} else {
							// input empty - clear;
							statusCellWrapper.clear();
						}
					}

					// update
					if (validationTrigger == null) return;
					validationTrigger.triggerValidation();
				}
			});

			strValueBox.addBlurHandler(new BlurHandler() {
				@Override
				public void onBlur(BlurEvent event) {
					// check validity
					if (inputChecker.isValid(true)) {
						// is valid AND value not empty
						if (!strValueBox.getText().equals("")) {
							// default OK?
							if (inputChecker.useDefaultOkMessage()) {
								statusCellWrapper.setWidget(new FormInputStatusWidget("OK", Status.OK));
							}
						} else {
							// input empty - clear;
							statusCellWrapper.clear();
						}
					}

					// update
					if (validationTrigger == null) return;
					validationTrigger.triggerValidation();
				}
			});

		}
	}



	/**
	 * Returns the generated widget
	 * @return
	 */
	public Widget getWidget()
	{
		return widget;
	}

	public FormInputChecker getInputChecker()
	{
		return this.inputChecker;
	}

	/**
	 * Generates the widget according to the "type"
	 *
	 * @return
	 */
	private Widget generateWidget(){

		if(item.getType().equals("TEXTFIELD")){
			return generateTextBox();
		}

		if(item.getType().equals("TEXTAREA")){
			return generateTextArea();
		}

		if(item.getType().equals("SELECTIONBOX")){
			return generateListBox();
		}

		if(item.getType().equals("CHECKBOX")){
			return generateCheckBox();
		}

		if(item.getType().equals("COMBOBOX")){
			return generateComboBox();
		}

		if(item.getType().equals("PASSWORD")){
			return generatePasswordTextBox();
		}

		if(item.getType().equals("VALIDATED_EMAIL")){
			return generateEmailTextBox();
		}

		if(item.getType().equals("SUBMIT_BUTTON") || item.getType().equals("AUTO_SUBMIT_BUTTON")){
			showLabel = false;
			return generateButton();
		}

		if(item.getType().equals("HTML_COMMENT")){
			showLabel = false;
			return generateHtmlComment();
		}

		if(item.getType().equals("HEADING")){
			showLabel = false;
			return generateHtmlComment();
		}

		if(item.getType().equals("TIMEZONE")){
			return generateTimezoneListBox();
		}

		if(item.getType().equals("FROM_FEDERATION_HIDDEN")){
			this.visibleOnlyToAdmin = true;
			this.visible = false;
			return generateHidden();
		}

		if(item.getType().equals("FROM_FEDERATION_SHOW")){
			this.visible = true;
			return generateReadonlyTextBox();
		}

		if(item.getType().equals("USERNAME")){
			this.visible = true;
			return generateUsernameBox();
		}

		if(item.getType().equals("RADIO")){
			this.visible = true;
			return generateRadioBox();
		}

		this.visible = false;


		return new HTML(item.getType() + " is not supported");

	}

	/**
	 * Generates the readonly textbox
	 * @return
	 */
	private Widget generateReadonlyTextBox() {
		TextBox tbox = new ExtendedTextBox();
		tbox.setMaxLength(TEXT_BOX_MAX_LENGTH);
		strValueBox = tbox;
		strValueBox.setValue(strValue);

		setVariableWidth(tbox);

		//tbox.setReadOnly(true);
		//don't want to trigger on click action
		tbox.setEnabled(false);
		return tbox;
	}

	/**
	 * Generates the hidden widget
	 * @return
	 */
	private Widget generateHidden() {
		TextBox tbox = new ExtendedTextBox();
		tbox.setMaxLength(TEXT_BOX_MAX_LENGTH);
		strValueBox = tbox;
		strValueBox.setValue(strValue);
		return new HTML("<em>hidden</em>");
	}

	/**
	 * Generates the email text box
	 * @return
	 */
	private Widget generateEmailTextBox() {
		final TextBox box = new ExtendedTextBox();
		box.setMaxLength(TEXT_BOX_MAX_LENGTH);
		strValueBox = box;

		box.getElement().setClassName("apptextbox"+counter++);
		setCutCopyPasteHandler("apptextbox"+counter);

		// multiple emails can be returned from RPC, they are separated by semi-colon
		if (strValue != null && !strValue.isEmpty()) {
			// split mails and use first one
			String[] emails = strValue.split(";");
			strValue = emails[0];
		}
		box.setText(strValue);

		setVariableWidth(box);

		inputChecker = new FormInputChecker() {

			private boolean valid = true;

			public boolean isValid(boolean forceNewValidation) {

				if(!forceNewValidation){
					return valid;
				}

				// missing?
				valid = (!(item.isRequired() && strValueBox.getValue().equals("")));
				if(!valid){
					statusCellWrapper.setWidget(new FormInputStatusWidget(ApplicationMessages.INSTANCE.missingValue(), Status.ERROR));
					return false;
				}

				// check
				valid = JsonUtils.isValidEmail(strValueBox.getValue());
				if(!valid){
					statusCellWrapper.setWidget(new FormInputStatusWidget(ApplicationMessages.INSTANCE.incorrectEmailFormat(), Status.ERROR));
					if ("".equalsIgnoreCase(strValueBox.getValue()) && !item.isRequired()) {
						// if not 'mail' valid, but empty and not required
						valid = true;
					} else {
						return false;
					}
				}

				// length
				valid = checkLength();
				if(!valid){
					return false;
				}

				return valid;
			}

			public boolean isValidating() {
				return false;
			}

			public boolean useDefaultOkMessage() {
				return true;
			}
		};

		return box;
	}

	/**
	 * Generates the HTML comment
	 * @return
	 */
	private Widget generateHtmlComment() {
		HTML html = new HTML(getLabelOrShortname());
		return html;
	}

	/**
	 * Generates the button
	 * @return
	 */
	private Widget generateButton() {
		CustomButton button = new CustomButton(getLabelOrShortname(), SmallIcons.INSTANCE.acceptIcon());
		return button;
	}


	/**
	 * Generates the password box
	 * @return
	 */
	private Widget generatePasswordTextBox() {

		final ExtendedPasswordTextBox pwdbox1 = new ExtendedPasswordTextBox();
		final ExtendedPasswordTextBox pwdbox2 = new ExtendedPasswordTextBox();
		pwdbox1.setMaxLength(TEXT_BOX_MAX_LENGTH);
		pwdbox2.setMaxLength(TEXT_BOX_MAX_LENGTH);

		pwdbox1.getElement().setClassName("apptextbox"+counter++);
		setCutCopyPasteHandler("apptextbox"+counter);
		pwdbox2.getElement().setClassName("apptextbox"+counter++);
		setCutCopyPasteHandler("apptextbox"+counter);

		strValueBox = pwdbox1;
		pwdbox1.setWidth(MIN_WIDTH+"px");
		pwdbox2.setWidth(MIN_WIDTH+"px");

		inputChecker = new FormInputChecker() {

			private boolean valid = true;

			public boolean isValid(boolean forceNewValidation) {

				// if not new, don't force
				if(!forceNewValidation) return valid;

				// Password can never be empty !! even if not "required" by app form config !!
				valid = (!(pwdbox1.getValue().equals("") && pwdbox2.getValue().equals("")));
				if(!valid){
					statusCellWrapper.setWidget(new FormInputStatusWidget(ApplicationMessages.INSTANCE.missingValue(), Status.ERROR));
					return false;
				}

				// length
				valid = checkLength();
				if(!valid){
					return false;
				}

				// check regex
				valid = checkValueRegex();
				if(!valid){
					return false;
				}

				// password same
				if(!pwdbox1.getText().equals(pwdbox2.getText())){
					statusCellWrapper.setWidget(new FormInputStatusWidget( ApplicationMessages.INSTANCE.passwordsDontMatch(), Status.ERROR));
					valid = false;
					return false;
				}
				// true by default if not changed by checks().
				return valid;

			}

			public boolean isValidating() {
				return false;
			}

			public boolean useDefaultOkMessage() {
				return true;
			}
		};

		// We must manually add handlers to each box
		// reference by strValueBox is not working with manually triggered actions

		pwdbox1.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				DomEvent.fireNativeEvent(Document.get().createChangeEvent(), pwdbox1);
			}
		});

		// is triggered manually by onBlur + onPaste
		pwdbox1.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				// check validity
				if (inputChecker.isValid(true)) {
					// is valid AND value not empty
					if (!pwdbox1.getText().equals("")) {
						// default OK?
						if (inputChecker.useDefaultOkMessage()) {
							statusCellWrapper.setWidget(new FormInputStatusWidget("OK", Status.OK));
						}
					} else {
						// input empty - clear;
						statusCellWrapper.clear();
					}
				}

				// update
				if (validationTrigger == null) return;
				validationTrigger.triggerValidation();
			}
		});

		pwdbox1.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				// check validity
				if (inputChecker.isValid(true)) {
					// is valid AND value not empty
					if (!pwdbox1.getText().equals("")) {
						// default OK?
						if (inputChecker.useDefaultOkMessage()) {
							statusCellWrapper.setWidget(new FormInputStatusWidget("OK", Status.OK));
						}
					} else {
						// input empty - clear;
						statusCellWrapper.clear();
					}
				}

				// update
				if (validationTrigger == null) return;
				validationTrigger.triggerValidation();
			}
		});

		pwdbox2.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				DomEvent.fireNativeEvent(Document.get().createChangeEvent(), pwdbox2);
			}
		});

		// is triggered manually by onBlur + onPaste
		pwdbox2.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				// check validity
				if (inputChecker.isValid(true)) {
					// is valid AND value not empty
					if (!pwdbox2.getText().equals("")) {
						// default OK?
						if (inputChecker.useDefaultOkMessage()) {
							statusCellWrapper.setWidget(new FormInputStatusWidget("OK", Status.OK));
						}
					} else {
						// input empty - clear;
						statusCellWrapper.clear();
					}
				}

				// update
				if (validationTrigger == null) return;
				validationTrigger.triggerValidation();
			}
		});

		pwdbox2.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				// check validity
				if (inputChecker.isValid(true)) {
					// is valid AND value not empty
					if (!pwdbox2.getText().equals("")) {
						// default OK?
						if (inputChecker.useDefaultOkMessage()) {
							statusCellWrapper.setWidget(new FormInputStatusWidget("OK", Status.OK));
						}
					} else {
						// input empty - clear;
						statusCellWrapper.clear();
					}
				}

				// update
				if (validationTrigger == null) return;
				validationTrigger.triggerValidation();
			}
		});

		FlexTable ft = new FlexTable();
		ft.setStyleName("appFormPasswordTable");
		ft.setCellPadding(0);
		ft.setCellSpacing(0);
		ft.setWidget(0, 0, pwdbox1);
		ft.setWidget(1, 0, pwdbox2);
		ft.setWidth("100%");
		return ft;

	}

	/**
	 * Generates the listbox
	 * @return
	 */
	private Widget generateListBox() {

		final ListBox lbox = new ListBox();
		strValueBox = new ExtendedTextBox();

		// parse options
		String options = getOptions();

		Map<String,String> boxContents = parseSelectionBox(options);

		ArrayList<String> keyList = JsonUtils.setToList(boxContents.keySet());
		//Collections.sort(keyList);

		int i = 0;
		for(String key : keyList){
			boolean selected = strValue.equals(key);
			lbox.addItem(boxContents.get(key), key);
			lbox.setItemSelected(i, selected);
			i++;
		}

		// when changed, update value
		lbox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				String value = lbox.getValue(lbox.getSelectedIndex());
				// fire change event to check for correct input
				strValueBox.setValue(value, true);
			}
		});

		if (lbox.getItemCount() != 0) {
			// set default value
			strValueBox.setText(lbox.getValue(lbox.getSelectedIndex()));
		}

		return lbox;
	}

	/**
	 * Generates the listbox with Timezone selection.
	 * @return
	 */
	private Widget generateTimezoneListBox() {

		final ListBox lbox = new ListBox();
		strValueBox = new ExtendedTextBox();

		// add timezone option
		lbox.addItem("Not selected", "");
		int i = 1;
		for(String key : Utils.getTimezones()){
			boolean selected = strValue.equals(key);
			lbox.addItem(key, key);
			lbox.setItemSelected(i, selected);
			i++;
		}

		// when changed, update value
		lbox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				String value = lbox.getValue(lbox.getSelectedIndex());
				// fire change event to check for correct input
				strValueBox.setValue(value, true);
			}
		});

		if (lbox.getItemCount() != 0) {
			// set default value
			strValueBox.setText(lbox.getValue(lbox.getSelectedIndex()));
		}

		return lbox;
	}

	/**
	 * Generates the checkboxes widget
	 * @return
	 */
	private Widget generateCheckBox() {

		FlexTable ft = new FlexTable();
		ft.setStyleName("appFormCheckBoxTable");
		// parse options
		String options = getOptions();
		Map<String,String> boxContents = parseSelectionBox(options);
		final Map<CheckBox, String> boxValueMap = new HashMap<CheckBox, String>();

		int i = 0;

		ArrayList<String> keyList = JsonUtils.setToList(boxContents.keySet());
		//Collections.sort(keyList);

		for(String key : keyList){

			final CheckBox checkbox = new CheckBox(boxContents.get(key));
			// pre-fill
			for (String s : prefilledValue.split("\\|")) {
				if (key.trim().equals(s.trim())) {
					checkbox.setValue(true);
				}
			}
			boxValueMap.put(checkbox, key);

			checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {

					// rebuild value
					strValue = "";
					for(Map.Entry<CheckBox, String> innerEntry : boxValueMap.entrySet()) {
						if (innerEntry.getKey().getValue()) {
							// put in selected values
							strValue += innerEntry.getValue()+"|";
						}
					}
					if (strValue.length() > 1) {
						strValue = strValue.substring(0, strValue.length()-1);
					}

					inputChecker.isValid(true);

					// update
					if (validationTrigger == null) return;
					validationTrigger.triggerValidation();

				}
			});

			inputChecker = new FormInputChecker() {

				private boolean valid = true;

				@Override
				public boolean isValid(boolean forceNewValidation) {

					// if not new, don't force
					if(!forceNewValidation) return valid;

					if (item.isRequired() && strValue.isEmpty()) {
						statusCellWrapper.setWidget(new FormInputStatusWidget(ApplicationMessages.INSTANCE.missingValue(), Status.ERROR));
						valid = false;
						return valid;
					} else {
						// if not required any value is good
						statusCellWrapper.setWidget(new FormInputStatusWidget("OK", Status.OK));
						valid = true;
						return true;
					}

				}

				@Override
				public boolean isValidating() {
					return false;
				}

				@Override
				public boolean useDefaultOkMessage() {
					return true;
				}
			};

			// fill widget
			ft.setWidget(i, 0, checkbox);
			i++;

		}

		return ft;

	}

	/**
	 * Generates the radiobox widget
	 * @return
	 */
	private Widget generateRadioBox() {

		FlexTable ft = new FlexTable();
		ft.setStyleName("appFormCheckBoxTable");
		// parse options
		String options = getOptions();
		Map<String,String> boxContents = parseSelectionBox(options);
		final Map<RadioButton, String> boxValueMap = new HashMap<RadioButton, String>();

		int i = 0;

		ArrayList<String> keyList = JsonUtils.setToList(boxContents.keySet());

		RADIO_COUNTER++;

		for(String key : keyList){

			final RadioButton radioBox = new RadioButton("form-radio-"+RADIO_COUNTER);

			radioBox.setText(boxContents.get(key));

			// pre-fill
			for (String s : prefilledValue.split("\\|")) {
				if (key.trim().equals(s.trim())) {
					radioBox.setValue(true);
				}
			}
			boxValueMap.put(radioBox, key);

			radioBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {

					// rebuild value
					strValue = "";
					for(Map.Entry<RadioButton, String> innerEntry : boxValueMap.entrySet()) {
						if (innerEntry.getKey().getValue()) {
							// put in selected values
							strValue += innerEntry.getValue()+"|";
						}
					}
					if (strValue.length() > 1) {
						strValue = strValue.substring(0, strValue.length()-1);
					}

					inputChecker.isValid(true);

					// update
					if (validationTrigger == null) return;
					validationTrigger.triggerValidation();

				}
			});

			inputChecker = new FormInputChecker() {

				private boolean valid = true;

				@Override
				public boolean isValid(boolean forceNewValidation) {

					// if not new, don't force
					if(!forceNewValidation) return valid;

					if (item.isRequired() && strValue.isEmpty()) {
						statusCellWrapper.setWidget(new FormInputStatusWidget(ApplicationMessages.INSTANCE.missingValue(), Status.ERROR));
						valid = false;
						return valid;
					} else {
						// if not required any value is good
						statusCellWrapper.setWidget(new FormInputStatusWidget("OK", Status.OK));
						valid = true;
						return true;
					}

				}

				@Override
				public boolean isValidating() {
					return false;
				}

				@Override
				public boolean useDefaultOkMessage() {
					return true;
				}
			};

			// fill widget
			ft.setWidget(i, 0, radioBox);
			i++;

		}

		Anchor clear = new Anchor("Clear selection");
		clear.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				for (RadioButton radioButton : boxValueMap.keySet()) {
					radioButton.setValue(false);
				}
			}
		});

		ft.setWidget(i, 0, clear);

		return ft;

	}

	/**
	 * Generates the combobox
	 * @return
	 */
	private Widget generateComboBox() {

		final ListBox lbox = new ListBox();
		final TextBox textBox = new ExtendedTextBox();
		textBox.setMaxLength(TEXT_BOX_MAX_LENGTH);
		textBox.getElement().setClassName("apptextbox"+counter++);
		setCutCopyPasteHandler("apptextbox"+counter);
		boolean anyValueSelected = false;
		textBox.setWidth(MIN_WIDTH+"px");
		strValueBox = textBox;
		inputChecker = getDefaultInputChecker();
		textBox.setVisible(false);

		// parse options
		String options = getOptions();

		Map<String,String> boxContents = parseSelectionBox(options);
		ArrayList<String> keyList = JsonUtils.setToList(boxContents.keySet());
		//Collections.sort(keyList);

		int i = 0;
		for(String key : keyList){

			boolean selected = strValue.equals(key);

			lbox.addItem(boxContents.get(key), key);
			lbox.setItemSelected(i, selected);
			if(selected == true){
				anyValueSelected = true;
			}
			i++;
		}
		// add " - other value - " with previous value as default
		if(strValue == null){
			strValue = "";
		}
		lbox.addItem("--- other value ---", strValue);
		final int otherValueIndex = i;
		if(!anyValueSelected && !strValue.equals("")){
			lbox.setItemSelected(otherValueIndex, true);
			textBox.setVisible(true);
		}


		// when changed, update value
		lbox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {

				String value = lbox.getValue(lbox.getSelectedIndex());
				strValueBox.setValue(value, true);

				// if other value selected, set textbox visible
				textBox.setVisible(lbox.getSelectedIndex() == otherValueIndex);

				if(lbox.getSelectedIndex() == otherValueIndex)
				{
					textBox.setFocus(true);
					textBox.selectAll();
				}

				// validation
				if(inputChecker.isValid(true)){
					// is valid AND value not empty
					if(!strValueBox.getText().equals("") || !item.isRequired())
					{
						// default OK?
						if(inputChecker.useDefaultOkMessage())
						{
							statusCellWrapper.setWidget(new FormInputStatusWidget("OK", Status.OK));
						}
					}else{
						// input empty - clear;
						statusCellWrapper.clear();
					}
				}
			}
		});
		// set default value
		strValueBox.setText(lbox.getValue(lbox.getSelectedIndex()));


		// container
		FlexTable ft = new FlexTable();
		ft.setStyleName("appFormComboBoxTable");
		FlexCellFormatter ftf = ft.getFlexCellFormatter();
		ft.setWidget(0, 0, lbox);
		ft.setWidget(1, 0, textBox);
		ftf.setWidth(1, 0, MIN_WIDTH + "px");


		return ft;
	}

	/**
	 * Generates the textbox
	 * @return
	 */
	private Widget generateTextBox() {
		TextBox tbox = new ExtendedTextBox();
		tbox.setMaxLength(TEXT_BOX_MAX_LENGTH);
		strValueBox = tbox;
		tbox.setText(strValue);

		tbox.getElement().setClassName("apptextbox"+counter++);
		setCutCopyPasteHandler("apptextbox"+counter);

		setVariableWidth(tbox);

		return tbox;
	}


	/**
	 * Generates the username textbox
	 * @return
	 */
	private Widget generateUsernameBox() {

		final TextBox tbox = new ExtendedTextBox();
		tbox.setMaxLength(TEXT_BOX_MAX_LENGTH);
		strValueBox = tbox;
		tbox.setText(strValue);

		tbox.getElement().setClassName("apptextbox"+counter++);
		setCutCopyPasteHandler("apptextbox"+counter);

		setVariableWidth(tbox);

		// if username not empty - disable
		if (!tbox.getValue().equalsIgnoreCase("")) {
			tbox.setEnabled(false);
			// do not check prefilled logins
			inputChecker = getDefaultInputChecker();
		} else {

			// get namespace
			if (item.getPerunDestinationAttribute() != null && !item.getPerunDestinationAttribute().isEmpty()) {

				final String loginNamespace = item.getPerunDestinationAttribute().substring(PERUN_ATTRIBUTE_LOGIN_NAMESPACE_POSITION);

				// check if login is new
				inputChecker = new FormInputChecker() {

					private boolean validating = false;

					private boolean valid = true;

					private Map<String, Boolean> validMap = new HashMap<String, Boolean>();

					public boolean isValid(boolean forceNewValidation) {

						// if not new, don't force
						if(!forceNewValidation) return valid;

						final String str = tbox.getValue();

						// missing?
						valid = (!(item.isRequired() && str.equals("")));
						if(!valid){

							statusCellWrapper.setWidget(new FormInputStatusWidget(ApplicationMessages.INSTANCE.missingValue(), Status.ERROR));
							return false;
						}

						// length
						valid = checkLength();
						if(!valid){
							return false;
						}

						valid = checkValueRegex();
						// regex check
						if(!valid){
							return false;
						}

						// force check for base REGEX used in login attribute module
						RegExp regExp = RegExp.compile(Utils.LOGIN_VALUE_MATCHER);
						boolean match = regExp.test(str);
						if (!match) return false;

						// has already checked it?
						if(validMap.containsKey(str)){
							valid = validMap.get(str);
							if(valid){

								statusCellWrapper.setWidget(new FormInputStatusWidget(ApplicationMessages.INSTANCE.usernameAvailable(), Status.OK));
							} else {

								statusCellWrapper.setWidget(new FormInputStatusWidget(ApplicationMessages.INSTANCE.usernameNotAvailable(), Status.ERROR));
							}
							return valid;
						}

						// check login
						validating = true;

						statusCellWrapper.setWidget(new FormInputStatusWidget(ApplicationMessages.INSTANCE.validating(), Status.LOADING));

						// check login
						new IsLoginAvailable(loginNamespace, str, new JsonCallbackEvents(){
							@Override
							public void onFinished(JavaScriptObject jso){

								// store result for the requested login
								BasicOverlayType bo = jso.cast();
								validMap.put(str, bo.getBoolean());

								if(!str.equals(strValueBox.getValue())){
									// value changed before request finished, don't update the valid value
									return;
								}

								valid = bo.getBoolean();

								validating = false;

								if(valid){

									statusCellWrapper.setWidget(new FormInputStatusWidget( ApplicationMessages.INSTANCE.usernameAvailable(), Status.OK));
								} else {

									statusCellWrapper.setWidget(new FormInputStatusWidget( ApplicationMessages.INSTANCE.usernameNotAvailable(), Status.ERROR));
								}
								validationTrigger.triggerValidation();
							}
						}).retrieveData();


						// check login end

						valid = false;
						return false;
					}

					public boolean isValidating() {
						return validating;
					}

					public boolean useDefaultOkMessage() {
						return false;
					}
				};

			} else {

				// Username has no "login-namespace"
				// prevent such malformed applications from submission
				tbox.setEnabled(false);
				tbox.setValue("Wrong form item configuration !!");
				inputChecker = new FormInputChecker() {
					@Override
					public boolean isValid(boolean forceNewValidation) {
						return false;
					}

					@Override
					public boolean isValidating() {
						return false;
					}

					@Override
					public boolean useDefaultOkMessage() {
						return false;
					}
				};

			}

		}

		return tbox;
	}

	/**
	 * Generates the textarea
	 * @return
	 */
	private Widget generateTextArea() {

		TextArea tarea = new ExtendedTextArea();

		strValueBox = tarea;
		tarea.setSize("300px", "150px");
		tarea.setText(strValue);
		// Set maximum length of the text area
		tarea.getElement().setAttribute("maxlength", String.valueOf(TEXT_AREA_MAX_LENGTH));

		tarea.getElement().setClassName("apptextbox"+counter++);
		setCutCopyPasteHandler("apptextbox"+counter);

		return tarea;
	}

	/**
	 * Returns the label or shortname for current locale
	 * @return
	 */
	public String getLabelOrShortname() {
		String label = item.getItemTexts(locale).getLabel();
		if(label == null || label.length() == 0){
			label = item.getShortname();
		}
		return label;
	}

	/**
	 * Returns the options for current locale
	 * @return
	 */
	private String getOptions() {
		String options = item.getItemTexts(locale).getOptions();
		return options;
	}

	/**
	 * Whether to show label
	 * @return
	 */
	public boolean isLabelShown()
	{
		return showLabel;
	}


	/**
	 * Returns the value inserted
	 *
	 * @return
	 */
	public String getValue() {

		if(strValueBox == null) return strValue.trim();

		return strValueBox.getValue().trim();

	}

	/**
	 * Returns the form item
	 *
	 * @return
	 */
	public ApplicationFormItem getFormItem()
	{
		return item;
	}

	/**
	 * Return the input value status
	 *
	 * @return
	 */
	public Widget getStatusWidget(){
		return this.statusCellWrapper;
	}

	/**
	 * Parses the "options" into MAP
	 *
	 * Standard HTML selection box, options are in for each locale in ItemTexts.label separated by | with values separated by #.
	 * Thus a language selection box would have for English locale the label <code>cs#Czech|en#English</code>.
	 *
	 * @param options
	 * @return
	 */
	static public Map<String, String> parseSelectionBox(String options){

		Map<String, String> map = new HashMap<String, String>();

		if(options == null || options.length() == 0){
			return map;
		}

		String[] keyValue = options.split("\\|");

		for(int i = 0; i < keyValue.length; i++){

			String kv = keyValue[i];

			String[] split = kv.split("#", 2);

			if(split.length != 2){
				continue;
			}

			String key = split[0];
			String value = split[1];
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Serializes MAP into "options"
	 *
	 * Standard HTML selection box, options are in for each locale in ItemTexts.label separated by | with values separated by #.
	 * Thus a language selection box would have for English locale the label <code>cs#Czech|en#English</code>.
	 *
	 * @param map
	 * @return
	 */
	static public String serializeSelectionBox(Map<String, String> map){

		String serialized = "";

		for(Map.Entry<String, String> entry : map.entrySet()){

			if(serialized != ""){
				serialized += "|";
			}

			serialized += entry.getKey() + "#" + entry.getValue();
		}
		return serialized;
	}

	/**
	 * Generates form item from given values
	 *
	 * @param shortname
	 * @param type
	 * @return
	 */
	public static ApplicationFormItem generateFormItem(String shortname, String type) {

		JSONObject jsonObj = new JSONObject();

		jsonObj.put("id", new JSONNumber(0));
		jsonObj.put("shortname", new JSONString(shortname));
		jsonObj.put("type", new JSONString(type));
		jsonObj.put("regex", new JSONString(""));
		jsonObj.put("federationAttribute", new JSONString(""));
		jsonObj.put("perunDestinationAttribute", new JSONString(""));
		jsonObj.put("applicationTypes", new JSONArray());
		jsonObj.put("required", JSONBoolean.getInstance(false));
		jsonObj.put("i18n", new JSONArray());
		jsonObj.put("ordnum", new JSONNumber(-1));

		// convert to ApplicationFormItem
		ApplicationFormItem formItem = jsonObj.getJavaScriptObject().cast();

		return formItem;
	}

	/**
	 * Whether is the input visible to regular user
	 * @return
	 */
	public boolean isVisible() {
		return this.visible ;
	}

	/**
	 * Returns the assurance level
	 * @return
	 */
	public String getAssuranceLevel()
	{
		return this.assuranceLevel;
	}


	public void addValidationTrigger(FormValidator formValidator) {
		this.validationTrigger = formValidator;
	}

	/**
	 * Returns original prefilled value of this item
	 * @return
	 */
	public String getPrefilledValue() {
		return this.prefilledValue.trim();
	}

	/**
	 * Whether is the item visible only to the administrator
	 * @return
	 */
	public boolean isVisibleOnlyToAdmin()
	{
		return visibleOnlyToAdmin;
	}

	/**
	 * Set variable width for TextBox like widgets
	 *
	 * @param box to set width to
	 */
	private void setVariableWidth(TextBox box) {

		if (box.getText().length() != 0) {
			if (box.getText().length()*WIDTH_PER_CHAR < MIN_WIDTH) {
				box.setWidth(MIN_WIDTH+"px");
			} else if (box.getText().length()*WIDTH_PER_CHAR > MIN_WIDTH && box.getText().length()*WIDTH_PER_CHAR <= MAX_WIDTH) {
				box.setWidth(box.getText().length()*WIDTH_PER_CHAR+"px");
			} else {
				box.setWidth(MAX_WIDTH+"px");
			}
		} else {
			box.setWidth(MIN_WIDTH+"px");
		}

	}

	private class ExtendedTextBox extends TextBox {

		public ExtendedTextBox() {
			super();
			sinkEvents(Event.ONPASTE);
		}

		@Override
		public void onBrowserEvent(Event event) {
			super.onBrowserEvent(event);
			switch (DOM.eventGetType(event)) {
				case Event.ONPASTE:
					Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
						@Override
						public void execute() {
							ValueChangeEvent.fire(ExtendedTextBox.this, getText());
						}
					});
					break;
			}
		}
	}

	private class ExtendedPasswordTextBox extends PasswordTextBox {

		public ExtendedPasswordTextBox() {
			super();
			sinkEvents(Event.ONPASTE);
		}

		@Override
		public void onBrowserEvent(Event event) {
			super.onBrowserEvent(event);
			switch (DOM.eventGetType(event)) {
				case Event.ONPASTE:
					Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
						@Override
						public void execute() {
							ValueChangeEvent.fire(ExtendedPasswordTextBox.this, getText());
						}
					});
					break;
			}
		}
	}

	private class ExtendedTextArea extends TextArea {

		public ExtendedTextArea() {
			super();
			sinkEvents(Event.ONPASTE);
		}

		@Override
		public void onBrowserEvent(Event event) {
			super.onBrowserEvent(event);
			switch (DOM.eventGetType(event)) {
				case Event.ONPASTE:
					Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
						@Override
						public void execute() {
							ValueChangeEvent.fire(ExtendedTextArea.this, getText());
						}
					});
					break;
			}
		}
	}

	/**
	 * Set copy & cut & paste javascript handlers to textbox by class
	 *
	 * @param id class of textbox to assign handlers to
	 */
	private final native void setCutCopyPasteHandler(String id) /*-{
		$wnd.jQuery.ready(function() {
			$wnd.jQuery('#'+id).bind('cut', function(e) {
				$wnd.jQuery('#'+id).onkeyup()
			});
			$wnd.jQuery('#'+id).bind('copy', function(e) {
				$wnd.jQuery('#'+id).onkeyup()
			});
			$wnd.jQuery('#'+id).bind('paste', function(e) {
				$wnd.jQuery('#'+id).onkeyup()
			});
		});
	}-*/;


}
