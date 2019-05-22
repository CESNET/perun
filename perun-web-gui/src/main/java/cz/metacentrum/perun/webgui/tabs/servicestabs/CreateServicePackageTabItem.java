package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.servicesManager.CreateServicePackage;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Tab which create service package
 *
 * ! USE AS INNER TAB ONLY !
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateServicePackageTabItem implements TabItem {

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
	private Label titleWidget = new Label("Create service package");

	/**
	 * Tab with create service form
	 */
	public CreateServicePackageTabItem(){}

	public boolean isPrepared(){
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

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		final ExtendedTextBox packageName = new ExtendedTextBox();
		final ExtendedTextBox packageDescription = new ExtendedTextBox();

		final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (packageName.getTextBox().getText().trim().isEmpty()) {
					packageName.setError("Name can't be empty");
					return false;
				} else {
					packageName.setOk();
					return true;
				}
			}
		};
		packageName.setValidator(validator);

		final ExtendedTextBox.TextBoxValidator validator2 = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (packageDescription.getTextBox().getText().trim().isEmpty()) {
					packageDescription.setError("Description can't be empty");
					return false;
				} else {
					packageDescription.setOk();
					return true;
				}
			}
		};
		packageDescription.setValidator(validator);


		// prepares layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// close tab events
		final TabItem tab = this;

		TabMenu menu = new TabMenu();

		// fill form
		layout.setHTML(0, 0, "Name:");
		layout.setWidget(0, 1, packageName);
		layout.setHTML(1, 0, "Description:");
		layout.setWidget(1, 1, packageDescription);

		for (int i=0; i<layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		// create button
		final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createServicePackage());
		createButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (validator.validateTextBox() && validator2.validateTextBox()) {
					CreateServicePackage request = new CreateServicePackage(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab));
					request.createServicePackage(packageName.getTextBox().getText().trim(), packageDescription.getTextBox().getText().trim());
				}
			}
		});

		// cancel button
		final CustomButton cancelButton = TabMenu.getPredefinedButton(ButtonType.CANCEL, "");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
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
		return SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1061;
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
