package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.servicesManager.CreateCompleteService;
import cz.metacentrum.perun.webgui.json.servicesManager.CreateService;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Tab with create service form
 *
 * ! USE AS INNER TAB ONLY !
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateServiceTabItem implements TabItem {

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
	private Label titleWidget = new Label("Create service");

	private static final String DEFAULT_DELAY = "10";

	/**
	 * Tab with create service form
	 */
	public CreateServiceTabItem(){}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		final ExtendedTextBox serviceName = new ExtendedTextBox();
		final ExtendedTextBox scriptPath = new ExtendedTextBox();
		final CheckBox enabled = new CheckBox();
		final ExtendedTextBox delay = new ExtendedTextBox();

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
		enabled.setValue(true);

		delay.getTextBox().setText(DEFAULT_DELAY);

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
		FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// close tab events
		final TabItem tab = this;

		TabMenu menu = new TabMenu();

		// fill form
		layout.setHTML(0, 0, "Name:");
		layout.setHTML(1, 0, "Status:");
		layout.setHTML(2, 0, "Delay:");
		layout.setHTML(3, 0, "Script path:");

		layout.setWidget(0, 1, serviceName);
		layout.setWidget(1, 1, enabled);
		layout.setWidget(2, 1, delay);
		layout.setWidget(3, 1, scriptPath);

		for (int i=0; i<layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		// create button
		final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createService());
		createButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (validator.validateTextBox() && delayValidator.validateTextBox() && scriptValidator.validateTextBox()) {
					CreateCompleteService request = new CreateCompleteService(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab));
					request.createCompleteService(serviceName.getTextBox().getText().trim(), scriptPath.getTextBox().getText().trim(), Integer.parseInt(delay.getTextBox().getText().trim()), enabled.getValue());
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

		menu.addWidget(createButton);
		menu.addWidget(cancelButton);

		vp.add(layout);
		vp.add(menu);
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		// add tabs to the main panel
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
		return SmallIcons.INSTANCE.trafficLightsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1063;
		int result = 31;
		result = prime * result;
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

	public void open()
	{

	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

}
