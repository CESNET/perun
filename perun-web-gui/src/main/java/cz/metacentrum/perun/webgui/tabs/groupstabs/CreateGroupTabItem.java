package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.CreateGroup;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;

/**
 * Tab which allows to create a group
 * !!! USE AS INNER TAB ONLY !!!
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateGroupTabItem implements TabItem {

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
	private Label titleWidget = new Label("Create group");


	/**
	 * Entity ID to set
	 */
	private int groupId = 0;
	private int voId = 0;

	/**
	 * Entity type
	 */
	private PerunEntity entity;

	/**
	 * Creates a tab instance
	 *
	 * @param vo
	 */
	public CreateGroupTabItem(VirtualOrganization vo){
		this.entity = PerunEntity.VIRTUAL_ORGANIZATION;
		this.voId = vo.getId();
	}

	/**
	 * Creates a tab instance for group
	 *
	 * @param group
	 */
	public CreateGroupTabItem(Group group){
		this.entity = PerunEntity.GROUP;
		this.groupId = group.getId();
		this.voId = group.getVoId();
	}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();

		// used for closing
		final TabItem tab = this;

		// form inputs
		final ExtendedTextBox groupNameTextBox = new ExtendedTextBox();
		final TextBox groupDescriptionTextBox  = new TextBox();
		final ListBoxWithObjects<Group> vosGroups = new ListBoxWithObjects<Group>();
		vosGroups.setVisible(false);
		final CheckBox asSubGroup = new CheckBox("", false);
		TabMenu menu = new TabMenu();
		final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, "");
		final CustomButton cancelButton = TabMenu.getPredefinedButton(ButtonType.CANCEL, "");
		final HTML parentGroupText = new HTML("Parent group:");
		parentGroupText.setVisible(false);

		final GetAllGroups groupsCall = new GetAllGroups(voId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				vosGroups.clear();
				ArrayList<Group> retGroups = JsonUtils.jsoAsList(jso);
				retGroups = new TableSorter<Group>().sortByName(retGroups);
				for (Group g : retGroups) {
					if (!g.isCoreGroup()) {
						// SKIP CORE GROUPS !!
						vosGroups.addItem(g);
						if (g.getId() == groupId) {
							// select default if passed to tab
							vosGroups.setSelected(g, true);
						}
					}
				}
				if (vosGroups.getAllObjects().isEmpty()) {
					vosGroups.addItem("No groups found");
				} else {
					createButton.setEnabled(true);
				}
				// call finished when user changed his mind
				if (!asSubGroup.getValue()) {
					createButton.setEnabled(true);
				}
			}
			public void onLoadingStart(){
				vosGroups.clear();
				vosGroups.addItem("Loading...");
				createButton.setEnabled(false);
			}
			public void onError(PerunError error) {
				vosGroups.clear();
				vosGroups.addItem("Error while loading");
				if (!asSubGroup.getValue()) {
					createButton.setEnabled(true);
				}
			}
		});

		// set title
		if (PerunEntity.GROUP.equals(entity)) {
			this.titleWidget.setText("Create sub-group");
			asSubGroup.setValue(true);
			createButton.setTitle(ButtonTranslation.INSTANCE.createSubGroup());
			parentGroupText.setVisible(true);
			vosGroups.setVisible(true);
			groupsCall.retrieveData();
		} else {
			this.titleWidget.setText("Create group");
			createButton.setTitle(ButtonTranslation.INSTANCE.createGroup());
		}

		asSubGroup.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
				if (booleanValueChangeEvent.getValue() == true) {
					// set title
					titleWidget.setText("Create sub-group");
					vosGroups.setVisible(true);
					parentGroupText.setVisible(true);
					groupsCall.retrieveData();
					createButton.setTitle(ButtonTranslation.INSTANCE.createSubGroup());
				} else {
					titleWidget.setText("Create group");
					vosGroups.setVisible(false);
					parentGroupText.setVisible(false);
					createButton.setTitle(ButtonTranslation.INSTANCE.createGroup());
					createButton.setEnabled(true);
				}
			}
		});

		// layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (groupNameTextBox.getTextBox().getText().trim().isEmpty()) {
					groupNameTextBox.setError("Name can't be empty.");
				} else if (!groupNameTextBox.getTextBox().getText().trim().matches(Utils.GROUP_SHORT_NAME_MATCHER)) {
					groupNameTextBox.setError("Name can contain only a-z, A-Z, numbers, spaces, dots, '_' and '-'.");
				} else {
					groupNameTextBox.setOk();
					return true;
				}
				return false;
			}
		};
		groupNameTextBox.setValidator(validator);

		// send button
		createButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				if (!validator.validateTextBox()) return;

				// creates a new request
				CreateGroup cg = new CreateGroup(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab));
				if (asSubGroup.getValue()) {
					if (vosGroups.getSelectedObject() != null) {
						cg.createGroupInGroup(vosGroups.getSelectedObject().getId(), groupNameTextBox.getTextBox().getText().trim(), groupDescriptionTextBox.getText().trim());
					} else {
						UiElements.generateInfo("No parent group selected", "You checked create this group as sub-group, but no parent group is selected. Please select parent group.");
					}
				} else {
					cg.createGroupInVo(voId, groupNameTextBox.getTextBox().getText().trim(), groupDescriptionTextBox.getText().trim());
				}
			}
		});
		// cancel button
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, false);
			}
		});



		// Add some standard form options
		layout.setHTML(0, 0, "Name:");
		layout.setWidget(0, 1, groupNameTextBox);
		layout.setHTML(1, 0, "Description:");
		layout.setWidget(1, 1, groupDescriptionTextBox);
		layout.setHTML(2, 0, "As sub-group:");
		layout.setWidget(2, 1, asSubGroup);
		layout.setWidget(3, 0, parentGroupText);
		layout.setWidget(3, 1, vosGroups);

		for (int i=0; i<layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		// button align
		menu.addWidget(createButton);
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
		return SmallIcons.INSTANCE.addIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 811;
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

		CreateGroupTabItem create = (CreateGroupTabItem) obj;
		if (entity != create.entity){
			return false;
		}
		if (voId != create.voId){
			return false;
		}
		if ((voId == create.voId) && groupId != create.groupId) {
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		// no open for inner tab
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}
	}

}
