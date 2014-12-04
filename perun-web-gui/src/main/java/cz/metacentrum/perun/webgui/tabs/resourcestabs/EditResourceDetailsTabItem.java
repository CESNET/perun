package cz.metacentrum.perun.webgui.tabs.resourcestabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.resourcesManager.UpdateResource;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * !! USE ONLY AS INNER TAB !!!
 * Edit resource details tab
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class EditResourceDetailsTabItem implements TabItem {

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
	private Resource resource;
	private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;
	private JsonCallbackEvents events;

	/**
	 * Creates a tab instance
	 *
	 * @param res
	 * @param event
	 */
	public EditResourceDetailsTabItem(Resource res, JsonCallbackEvents event){
		this.resource = res;
		this.events = event;
	}

	public boolean isPrepared(){
		return (resource != null);
	}

	public Widget draw() {

		titleWidget = new Label("Edit resource");

		VerticalPanel vp = new VerticalPanel();

		// textboxes which set the class data when updated
		final ExtendedTextBox nameTextBox = new ExtendedTextBox();
		nameTextBox.getTextBox().setText(resource.getName());

		final TextBox descriptionTextBox = new TextBox();
		descriptionTextBox.setText(resource.getDescription());

		final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (nameTextBox.getTextBox().getText().trim().isEmpty()) {
					nameTextBox.setError("Name can't be empty.");
					return false;
				} else {
					nameTextBox.setOk();
					return true;
				}
			}
		};

		nameTextBox.setValidator(validator);

		// prepares layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// close tab events
		final TabItem tab = this;

		TabMenu menu = new TabMenu();

		// send button
		final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, buttonTranslation.saveResourceDetails());
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!validator.validateTextBox()) return;
				Resource r = JsonUtils.clone(resource).cast();
				r.setName(nameTextBox.getTextBox().getText().trim());
				r.setDescription(descriptionTextBox.getText().trim());
				UpdateResource request = new UpdateResource(JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab, events));
				request.updateResource(r);
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
		layout.setHTML(0, 0, "Name:");
		layout.setWidget(0, 1, nameTextBox);
		layout.setHTML(1, 0, "Description:");
		layout.setWidget(1, 1, descriptionTextBox);

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
		final int prime = 1031;
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
		EditResourceDetailsTabItem other = (EditResourceDetailsTabItem) obj;
		if (resource != other.resource)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isVoAdmin(resource.getVoId()) || session.isFacilityAdmin(resource.getFacilityId())) {
			return true;
		} else {
			return false;
		}

	}

}
