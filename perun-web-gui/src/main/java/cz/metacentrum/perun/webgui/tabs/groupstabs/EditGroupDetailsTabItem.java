package cz.metacentrum.perun.webgui.tabs.groupstabs;

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
import cz.metacentrum.perun.webgui.json.groupsManager.UpdateGroup;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * !! USE ONLY AS INNER TAB !!!
 * Edit group details tab
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class EditGroupDetailsTabItem implements TabItem {

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
	private Group group;
	private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;
	private JsonCallbackEvents events;

	/**
	 * Creates a tab instance
	 *
	 * @param group
	 * @param event
	 */
	public EditGroupDetailsTabItem(Group group, JsonCallbackEvents event){
		this.group = group;
		this.events = event;
	}

	public boolean isPrepared(){
		return (group != null);
	}

	public Widget draw() {

		titleWidget = new Label("Edit group");

		VerticalPanel vp = new VerticalPanel();

		// textboxes which set the class data when updated
		final ExtendedTextBox descriptionTextBox = new ExtendedTextBox();
		descriptionTextBox.getTextBox().setText(group.getDescription());

		final ExtendedTextBox nameTextBox = new ExtendedTextBox();
		nameTextBox.getTextBox().setText(group.getShortName());

		// disable name change for core groups
		nameTextBox.getTextBox().setEnabled(!group.isCoreGroup());

		// prepares layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// close tab events
		final TabItem tab = this;

		TabMenu menu = new TabMenu();

		// send button
		final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, buttonTranslation.saveGroupDetails());

		final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (nameTextBox.getTextBox().getText().trim().isEmpty()) {
					nameTextBox.setError("Name can't be empty.");
				} else if (!nameTextBox.getTextBox().getText().trim().matches(Utils.GROUP_SHORT_NAME_MATCHER)) {
					nameTextBox.setError("Name can contain only a-z, A-Z, numbers, spaces, dots, '_' and '-'.");
				} else {
					nameTextBox.setOk();
					return true;
				}
				return false;
			}
		};
		nameTextBox.setValidator(validator);

		saveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (!validator.validateTextBox()) return;

				Group g = JsonUtils.clone(group).cast();

				g.setDescription(descriptionTextBox.getTextBox().getText().trim());
				String value = nameTextBox.getTextBox().getText().trim();
				g.setShortName(value);
				int index = g.getName().lastIndexOf(":");
				if (index > 0) {
					// short name append to base name
					String baseName = g.getName().substring(0, index);
					baseName += ":"+g.getShortName();
					g.setName(baseName);
				} else {
					// short name is whole name
					g.setName(value);
				};
				UpdateGroup request = new UpdateGroup(JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab, events));
				request.updateGroup(g);

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
		final int prime = 1381;
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
		EditGroupDetailsTabItem other = (EditGroupDetailsTabItem) obj;
		if (group != other.group)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId()) || session.isGroupAdmin(group.getId())) {
			return true;
		} else {
			return false;
		}

	}

}
