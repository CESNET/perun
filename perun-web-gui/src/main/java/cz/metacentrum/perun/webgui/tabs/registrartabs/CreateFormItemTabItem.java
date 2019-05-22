package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.applicationresources.RegistrarFormItemGenerator;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.ApplicationFormItem;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * View ApplicationFormItem
 * !! USE IN INNER TAB ONLY !!
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateFormItemTabItem implements TabItem {

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("Add form item");

	/**
	 * List with inputs
	 */
	private ArrayList<ApplicationFormItem> sourceList;

	private JsonCallbackEvents events;

	public static final Map<String, String> inputTypes;
	static {
		Map<String, String> aMap = new HashMap<>();
		aMap.put("TEXTFIELD", "Input text field");
		aMap.put("TEXTAREA", "Input text multi-line field");
		aMap.put("SELECTIONBOX", "Single value selection from list");
		aMap.put("COMBOBOX", "Single value selection with opt. custom value");
		aMap.put("CHECKBOX", "Checkbox (multiple selection)");
		aMap.put("RADIO", "Radio (single selection)");
		aMap.put("USERNAME", "Input text field for username");
		aMap.put("PASSWORD", "Input text field for password");
		aMap.put("VALIDATED_EMAIL", "Input text field for email");
		aMap.put("SUBMIT_BUTTON", "Custom submit button");
		aMap.put("AUTO_SUBMIT_BUTTON", "Submit button with auto-submit");
		aMap.put("HTML_COMMENT", "Custom HTML text");
		aMap.put("HEADING", "Header");
		aMap.put("TIMEZONE", "Selection of timezone");
		aMap.put("FROM_FEDERATION_HIDDEN", "Hidden input text pre-filled from external source");
		aMap.put("FROM_FEDERATION_SHOW", "Input text field pre-filled from external source");
		inputTypes = Collections.unmodifiableMap(aMap);
	}

	/**
	 * Cropping length in select box after which item to add item
	 */
	static private final int CROP_LABEL_LENGTH = 25;

	/**
	 * Creates a tab instance
	 */
	public CreateFormItemTabItem(ArrayList<ApplicationFormItem> sourceList, JsonCallbackEvents events) {
		this.sourceList = sourceList;
		this.events = events;
	}

	public boolean isPrepared() {
		return true;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// vertical panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("425px", "100%");

		// flex table
		final FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// select widget short name
		final ExtendedTextBox shortNameTextBox = new ExtendedTextBox();
		shortNameTextBox.setWidth("200px");

		final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (shortNameTextBox.getTextBox().getText().trim().isEmpty()) {
					shortNameTextBox.setError("Short name can't be empty.");
					return false;
				} else {
					shortNameTextBox.setOk();
					return true;
				}
			}
		};
		shortNameTextBox.setValidator(validator);

		// select widget type
		final ListBox typeListBox = new ListBox();
		for (String type : inputTypes.keySet()) {
			typeListBox.addItem(inputTypes.get(type), type);
		}

		// insert after
		final ListBox insertAfterListBox = new ListBox();
		insertAfterListBox.addItem(" - insert to the beginning - ", 0 + "");
		for (int i = 0; i < sourceList.size(); i++) {
			ApplicationFormItem item = sourceList.get(i);
			RegistrarFormItemGenerator gen = new RegistrarFormItemGenerator(item, ""); // with default en locale
			String label = gen.getFormItem().getShortname();

			// crop length
			if (label.length() > CROP_LABEL_LENGTH) {
				label = label.substring(0, CROP_LABEL_LENGTH);
			}

			// add to box
			insertAfterListBox.addItem(label, (i + 1) + "");
		}

		layout.setHTML(0, 0, "Short name:");
		layout.setWidget(0, 1, shortNameTextBox);
		layout.setHTML(1, 0, "Input widget:");
		layout.setWidget(1, 1, typeListBox);
		layout.setHTML(2, 0, "Insert after:");
		layout.setWidget(2, 1, insertAfterListBox);

		for (int i = 0; i < layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		layout.setHTML(3,0,"");
		cellFormatter.setColSpan(3,0,2);
		cellFormatter.setStyleName(3,0, "inputFormInlineComment");

		typeListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {

				String type = typeListBox.getValue(typeListBox.getSelectedIndex());

				if (type.equals("TEXTFIELD")) {
					layout.setHTML(3,0, "Editable text field useful to gather short text input, e.g. name, phone.");
				} else if (type.equals("TEXTAREA")) {
					layout.setHTML(3,0, "Editable text area useful to gather longer text input with linebreaks, e.g. comments, SSH key");
				} else if (type.equals("SELECTIONBOX")) {
					layout.setHTML(3,0, "Simple selection box with defined custom values that user can choose.");
				} else if (type.equals("COMBOBOX")) {
					layout.setHTML(3,0, "Selection box with defined custom values and one special option: \"--custom value--\", which allows users to input own text (as simple text field).");
				} else if (type.equals("CHECKBOX")) {
					layout.setHTML(3,0,"List of defined custom options with checkboxes. Selected values are gathered as comma separated string.");
				} else if (type.equals("RADIO")) {
					layout.setHTML(3,0,"List of defined custom options with radio buttons. Single selection model. Value is stored as string.");
				} else if (type.equals("USERNAME")) {
					layout.setHTML(3,0,"Special text field to gather user`s login. It checks login availability on user input.");
				} else if (type.equals("PASSWORD")) {
					layout.setHTML(3,0,"Two password fields to gather user`s new password. Input is never displayed. User must type same password in both fields.");
				} else if (type.equals("VALIDATED_EMAIL")) {
					layout.setHTML(3,0,"Special text field to gather and verify user`s email address. Input is checked on email address format. If user enters new value, then validation email is sent. Application then can't be approved unless provided email address is validated.");
				} else if (type.equals("SUBMIT_BUTTON")) {
					layout.setHTML(3,0,"Button used to submit the form with custom label. All other form items are checked on valid input before submission. If it fails, form is not sent.");
				} else if (type.equals("AUTO_SUBMIT_BUTTON")) {
					layout.setHTML(3,0,"Button used to auto-submit the form with custom label. All other form items are checked on valid input before submission. If validation fail (at least once) user must submit form manually. If it's OK, then form is automatically submitted.");
				} else if (type.equals("HTML_COMMENT")) {
					layout.setHTML(3,0,"Item is used to display custom HTML content anywhere on form. Useful for explanation descriptions, dividing parts of form etc.");
				} else if (type.equals("HEADING")) {
					layout.setHTML(3,0,"Item is used to display customizable heading of form. Can have any HTML content.");
				} else if (type.equals("TIMEZONE")) {
					layout.setHTML(3,0,"Selection box with pre-defined values of UTC timezones.");
				} else if (type.equals("FROM_FEDERATION_HIDDEN")) {
					layout.setHTML(3,0,"Non-editable and hidden form item. Form is submitted even on invalid input ! Useful to automatically gather information provided by AUTH mechanism (IdP federation, certificate).");
				} else if (type.equals("FROM_FEDERATION_SHOW")) {
					layout.setHTML(3,0,"Non-editable and visible form item. Form is submitted even on invalid input ! Useful to automatically gather information provided by AUTH mechanism (IdP federation, certificate).");
				} else {
					layout.setHTML(3,0,"");
				}

			}
		});
		layout.setHTML(3,0, "Editable text field useful to gather short text input, e.g. name, phone.");

		TabMenu menu = new TabMenu();

		// create button
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createFormItem(), new ClickHandler() {

			public void onClick(ClickEvent event) {

				if (validator.validateTextBox()) {

					int positionToAdd = Integer.parseInt(insertAfterListBox.getValue(insertAfterListBox.getSelectedIndex()));
					String type = typeListBox.getValue(typeListBox.getSelectedIndex());
					String shortName = shortNameTextBox.getTextBox().getText().trim();
					createItem(shortName, type, positionToAdd);

				}

			}
		}));

		final TabItem tab = this;
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		vp.add(layout);
		vp.add(menu);
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	/**
	 * Creates the item
	 *
	 * @param shortname
	 * @param type
	 * @param positionToAdd
	 */
	protected void createItem(String shortname, String type, int positionToAdd) {

		ApplicationFormItem item = RegistrarFormItemGenerator.generateFormItem(shortname, type);

		// set both app types checked for new item
		JSONArray array = new JSONArray();
		array.set(0, new JSONString("INITIAL"));
		array.set(1, new JSONString("EXTENSION"));
		item.setApplicationTypes(array.getJavaScriptObject());

		// set also position
		item.setOrdnum(positionToAdd);
		sourceList.add(positionToAdd, item);

		session.getTabManager().addTabToCurrentTab(new EditFormItemTabItem(item, events));

		events.onFinished(item);

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.addIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 983;
		int result = 1;
		result = prime * result + 672;
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

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin() || session.isGroupAdmin()) {
			return true;
		} else {
			return false;
		}

	}
}
