package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.servicesManager.UpdateService;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * !! USE ONLY AS INNER TAB !!!
 * Edit Service details tab
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class EditServiceDetailsTabItem implements TabItem {

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
	private Label titleWidget = new Label("Edit: ");

	/**
	 * Data
	 */
	private Service service;
	private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;
	private JsonCallbackEvents events;

	/**
	 * Creates a tab instance
	 *
	 * @param service
	 * @param event
	 */
	public EditServiceDetailsTabItem(Service service, JsonCallbackEvents event){
		this.service = service;
		this.events = event;
	}

	public boolean isPrepared(){
		return (service != null);
	}

	public Widget draw() {

		titleWidget = new Label("Edit service");

		VerticalPanel vp = new VerticalPanel();

		final ExtendedTextBox serviceName = new ExtendedTextBox();
		final ExtendedTextBox serviceDescription = new ExtendedTextBox();
		final ExtendedTextBox scriptPath = new ExtendedTextBox();
		final CheckBox enabled = new CheckBox();
		final ExtendedTextBox delay = new ExtendedTextBox();
		final ExtendedTextBox recurrence = new ExtendedTextBox();

		serviceName.getTextBox().setText(service.getName());
		serviceDescription.getTextBox().setText(service.getDescription());
		scriptPath.getTextBox().setText(service.getScriptPath());
		enabled.setValue(service.isEnabled());
		delay.getTextBox().setText(String.valueOf(service.getDelay()));
		recurrence.getTextBox().setText(String.valueOf(service.getRecurrence()));

		final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (serviceName.getTextBox().getText().trim().isEmpty()) {
					serviceName.setError("Name can't be empty");
					return false;
				} else if (!serviceName.getTextBox().getText().trim().matches(Utils.SERVICE_NAME_MATCHER)) {
					serviceName.setError("Name can contain only letters, numbers and underscore.");
					return false;
				} else {
					serviceName.setOk();
					// fill script path on service name change
					scriptPath.getTextBox().setValue("./"+serviceName.getTextBox().getText().trim().toLowerCase().replaceAll(Utils.SERVICE_NAME_TO_SCRIP_PATH_MATCHER,"_"));
					return true;
				}
			}
		};
		serviceName.setValidator(validator);

		enabled.setText("Enabled / Disabled");

		final ExtendedTextBox.TextBoxValidator delayValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (!JsonUtils.checkParseInt(delay.getTextBox().getText().trim())) {
					delay.setError("Delay must be a number (time in minutes) !");
					return false;
				} else {
					delay.setOk();
					return true;
				}
			}
		};
		delay.setValidator(delayValidator);

		final ExtendedTextBox.TextBoxValidator recurrenceValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (!JsonUtils.checkParseInt(delay.getTextBox().getText().trim())) {
					recurrence.setError("Recurrence must be a number!");
					return false;
				} else {
					recurrence.setOk();
					return true;
				}
			}
		};
		recurrence.setValidator(recurrenceValidator);

		final ExtendedTextBox.TextBoxValidator scriptValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (scriptPath.getTextBox().getText().trim().isEmpty()) {
					scriptPath.setError("Script path can't be empty !");
					return false;
				} else {
					scriptPath.setOk();
					return true;
				}
			}
		};
		scriptPath.setValidator(scriptValidator);

		// prepares layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// close tab events
		final TabItem tab = this;

		TabMenu menu = new TabMenu();

		// send button
		final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, buttonTranslation.saveServiceDetails());
		saveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (validator.validateTextBox() && delayValidator.validateTextBox() && scriptValidator.validateTextBox() && recurrenceValidator.validateTextBox()) {
					Service serv = JsonUtils.clone(service).cast();
					serv.setName(serviceName.getTextBox().getText().trim());
					String desc = serviceDescription.getTextBox().getText().trim();
					if (desc.isEmpty()) desc = null;
					serv.setDescription(desc);
					serv.setDelay(Integer.parseInt(delay.getTextBox().getText().trim()));
					serv.setRecurrence(Integer.parseInt(recurrence.getTextBox().getText().trim()));
					serv.setEnabled(enabled.getValue());
					serv.setScriptPath(scriptPath.getTextBox().getText().trim());
					UpdateService request = new UpdateService(JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab, events));
					request.updateService(serv);
				}
			}
		});

		// cancel button
		final CustomButton cancelButton = TabMenu.getPredefinedButton(ButtonType.CANCEL, "");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, false);
			}
		});

		// Add some standard form options
		// fill form
		layout.setHTML(0, 0, "Name:");
		layout.setHTML(1, 0, "Description:");
		layout.setHTML(2, 0, "Status:");
		layout.setHTML(3, 0, "Delay:");
		layout.setHTML(4, 0, "Recurrence:");
		layout.setHTML(5, 0, "Script path:");

		layout.setWidget(0, 1, serviceName);
		layout.setWidget(1, 1, serviceDescription);
		layout.setWidget(2, 1, enabled);
		layout.setWidget(3, 1, delay);
		layout.setWidget(4, 1, recurrence);
		layout.setWidget(5, 1, scriptPath);

		for (int i=0; i<layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		menu.addWidget(saveButton);
		menu.addWidget(cancelButton);

		vp.add(layout);
		vp.add(menu);
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.applicationFormEditIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 593441861;
		int result = 1;
		result = prime * result + 6786786;
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
		EditServiceDetailsTabItem other = (EditServiceDetailsTabItem) obj;
		if (service != other.service)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

}
