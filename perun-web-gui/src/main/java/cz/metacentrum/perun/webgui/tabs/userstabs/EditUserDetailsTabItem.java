package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.usersManager.UpdateNameTitles;
import cz.metacentrum.perun.webgui.json.usersManager.UpdateUser;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * !! USE ONLY AS INNER TAB !!!
 * Edit user details tab
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class EditUserDetailsTabItem implements TabItem {

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
	private User user;
	private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;
	private JsonCallbackEvents events;

	/**
	 * Creates a tab instance
	 *
	 * @param u
	 * @param event
	 */
	public EditUserDetailsTabItem(User u, JsonCallbackEvents event){
		this.user = u;
		this.events = event;
	}

	public boolean isPrepared(){
		return (user != null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget = new Label("Edit user");

		VerticalPanel vp = new VerticalPanel();

		final TextBox beforeName = new TextBox();
		final TextBox afterName = new TextBox();
		final TextBox firstName = new TextBox();
		final TextBox middleName = new TextBox();
		final TextBox lastName = new TextBox();

		// prepares layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// set values from user
		beforeName.setText(user.getTitleBefore());
		afterName.setText(user.getTitleAfter());
		firstName.setText(user.getFirstName());
		lastName.setText(user.getLastName());
		middleName.setText(user.getMiddleName());

		// service users can have only first and last name (first is fixed as "(Service)"
		if (user.isServiceUser()) {
			beforeName.setEnabled(false);
			afterName.setEnabled(false);
			firstName.setEnabled(false);
			middleName.setEnabled(false);
		}

		// close tab events
		final TabItem tab = this;

		TabMenu menu = new TabMenu();

		// send button
		final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, buttonTranslation.saveResourceDetails());

		if (session.isPerunAdmin()) {

			saveButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					User u = JsonUtils.clone(user).cast();
					u.setFirstName(firstName.getText().trim());
					u.setMiddleName(middleName.getText().trim());
					u.setLastName(lastName.getText().trim());
					u.setTitleBefore(beforeName.getText().trim());
					u.setTitleAfter(afterName.getText().trim());
					UpdateUser request = new UpdateUser(JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab, events));
					request.updateUser(u);
				}
			});

		} else {

			saveButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					User u = JsonUtils.clone(user).cast();
					u.setTitleBefore(beforeName.getText().trim());
					u.setTitleAfter(afterName.getText().trim());
					UpdateNameTitles request = new UpdateNameTitles(JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab, events));
					request.updateUserTitles(u);
				}
			});

		}

		// cancel button
		final CustomButton cancelButton = TabMenu.getPredefinedButton(ButtonType.CANCEL, "");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		});

		// Add some standard form options
		layout.setHTML(0, 0, "Title before name:");
		layout.setWidget(0, 1, beforeName);
		layout.setHTML(1, 0, "Title after name:");
		layout.setWidget(1, 1, afterName);

		if (session.isPerunAdmin()) {

			layout.setHTML(2, 0, "First name:");
			layout.setWidget(2, 1, firstName);
			layout.setHTML(3, 0, "Middle name:");
			layout.setWidget(3, 1, middleName);
			layout.setHTML(4, 0, "Last name:");
			layout.setWidget(4, 1, lastName);

		}

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
		final int prime = 1193;
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
		EditUserDetailsTabItem other = (EditUserDetailsTabItem) obj;
		if (user != other.user)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
	}

	public boolean isAuthorized() {

		if (session.isSelf(user.getId())) {
			return true;
		} else {
			return false;
		}

	}

}
