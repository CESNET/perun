package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetResourceRequiredAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeValueCell;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides widget for attributes management for list of groups on specified resource
 * This is used when assigning more groups to resource
 *
 * !!! USE ONLY AS PART OF INNER TAB !!!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GroupsResourceRequiredAttributesTabItem implements TabItem {

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
	private Label titleWidget = new Label("Settings of groups");


	/**
	 * Resource ID to set
	 */
	private int resourceId = 0;

	/**
	 * Groups list
	 */
	private ArrayList<Group> groups;

	/**
	 * Column id to switch when extended info visible
	 */
	private int columnId = 3;

	/**
	 * Creates a tab instance
	 *
	 * @param resourceId ID of resource to get services from
	 * @param groups groups
	 */
	public GroupsResourceRequiredAttributesTabItem(int resourceId, ArrayList<Group> groups){
		this.groups = groups;
		this.resourceId = resourceId;
	}

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

		if (groups == null || groups.get(0) == null) {
			Window.alert("No groups in list.");
		}

		if (JsonUtils.isExtendedInfoVisible()) {
			columnId = 3;
		} else {
			columnId = 2;
		}

		final VerticalPanel mainTab = new VerticalPanel();
		mainTab.setSize("100%","100%");

		TabMenu menu = new TabMenu();

		mainTab.add(new HTML("<hr size=\"2px\" />"));

		// set attributes type to group_resource
		final Map<String, Integer> ids = new HashMap<String, Integer>();
		ids.put("resourceToGetServicesFrom", resourceId);
		ids.put("group", groups.get(0).getId());

		// gets all required group attributes for specified group and resource
		final GetResourceRequiredAttributesV2 reqAttrs = new GetResourceRequiredAttributesV2(ids);
		final CellTable<Attribute> reqAttrsTable = reqAttrs.getTable();

		// remove value column
		reqAttrsTable.removeColumn(columnId);

		// create own value column
		Column<Attribute, Attribute> valueColumn = JsonUtils.addColumn(
				new PerunAttributeValueCell(), "Value",
				new JsonUtils.GetValue<Attribute, Attribute>() {
					public Attribute getValue(Attribute attribute) {
						attribute.setValueAsJso(null); // set empty value
						return attribute;
					}
				}, new FieldUpdater<Attribute, Attribute>() {

					public void update(int index, Attribute object,
						Attribute value) {

						object = value;
						reqAttrsTable.getSelectionModel().setSelected(object, object.isAttributeValid());

					}
				});

		// add to table
		reqAttrsTable.insertColumn(columnId, valueColumn, "Value");

		// get all required group_resource attributes too
		ids.put("resource", resourceId);
		reqAttrs.setIds(ids);
		reqAttrs.retrieveData();

		reqAttrsTable.addStyleName("perun-table");
		final ScrollPanel sp = new ScrollPanel(reqAttrsTable);
		sp.addStyleName("perun-tableScrollPanel");

		CustomButton saveChangesButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes(), new ClickHandler() {
			public void onClick(ClickEvent event) {

				ArrayList<Attribute> list = reqAttrs.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {

					ArrayList<Attribute> groupList = new ArrayList<Attribute>();
					ArrayList<Attribute> groupResourceList = new ArrayList<Attribute>();
					SetAttributes request = new SetAttributes();

					// ask to be sure
					// TODO - use new confirm dialog
					if (!Window.confirm("Same values for selected attributes will be set to all groups you are about to assign." +
							"\n\nDo you want to continue ?")) {
						return;
							}
					// get different attributes
					for (Attribute attr : list) {
						if (attr.getNamespace().contains("urn:perun:group:")) {
							groupList.add(attr);
						} else if (attr.getNamespace().contains("urn:perun:group_resource:")) {
							groupResourceList.add(attr);
						}
					}
					if (!(groupList.isEmpty())) {
						ids.clear();
						for (int i = 0; i < groups.size(); i++) {
							ids.put("group", groups.get(i).getId());
							request.setAttributes(ids, groupList);
						}
					}
					if (!(groupResourceList.isEmpty())) {
						ids.clear();
						ids.put("resource", resourceId);
						for (int i = 0; i < groups.size(); i++) {
							ids.put("group", groups.get(i).getId());
							request.setAttributes(ids, groupResourceList);
						}
					}
					reqAttrs.clearTableSelectedSet();
				}
			}
		});

		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addWidget(saveChangesButton);
		if (!session.isGroupAdmin() && !session.isVoAdmin()) saveChangesButton.setEnabled(false);

		// table content
		session.getUiElements().resizePerunTable(sp, 350, this);

		mainTab.add(menu);
		mainTab.add(sp);
		mainTab.setCellHeight(sp, "100%");
		mainTab.setCellHeight(menu, "30px");

		this.contentWidget.setWidget(mainTab);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.attributesDisplayIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 853;
		int result = 1;
		result = prime * result * resourceId;
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

		GroupsResourceRequiredAttributesTabItem o = (GroupsResourceRequiredAttributesTabItem) obj;

		if (resourceId != o.resourceId){
			return false;
		}

		if (!groups.equals(o.groups)){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin() || session.isVoObserver()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "gps-res-req-attrs";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?groups=" + groups.toString() + "&res=" + resourceId;
	}

}
