package cz.metacentrum.perun.webgui.tabs.resourcestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroupsWithHierarchy;
import cz.metacentrum.perun.webgui.json.resourcesManager.AssignGroupToResources;
import cz.metacentrum.perun.webgui.json.resourcesManager.AssignGroupsToResource;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.RichResource;
import cz.metacentrum.perun.webgui.tabs.ResourcesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupResourceRequiredAttributesTabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupsResourceRequiredAttributesTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Provides Tab that allows to configure groups and their members before assigning to resource
 *
 * !!! USE ONLY AS INNER TAB !!!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ManageGroupsBeforeAssigning implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Configure groups / members");

	//data
	private int resourceId;
	private Resource resource;
	private ArrayList<Group> groupsToAssign = new ArrayList<Group>();
	private ArrayList<Group> allGroups = new ArrayList<Group>();

	/**
	 * @param resource ID of resource to have groups assigned
	 * @param groupsToAssign list of groups to be configured and assigned
	 */
	public ManageGroupsBeforeAssigning(Resource resource, ArrayList<Group> groupsToAssign){
		this.resource = resource;
		this.resourceId = resource.getId();
		this.groupsToAssign = groupsToAssign;

		new GetAllGroupsWithHierarchy(resource.getVoId(), new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				allGroups = JsonUtils.jsoAsList(jso);
			}
		}).retrieveData();

	}

	/**
	 * @param resourceId ID of resource to have groups assigned
	 * @param grpToAssign list of groups to be configured and assigned
	 */
	protected ManageGroupsBeforeAssigning(int resourceId, ArrayList<Group> grpToAssign) {
		this.resourceId = resourceId;
		this.groupsToAssign = grpToAssign;
		new GetEntityById(PerunEntity.RESOURCE, resourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				resource = jso.cast();
				new GetAllGroupsWithHierarchy(resource.getVoId(), new JsonCallbackEvents(){
					public void onFinished(JavaScriptObject jso) {
						allGroups = JsonUtils.jsoAsList(jso);
					}
				}).retrieveData();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){

		if (resource == null || allGroups == null) { return false; }
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

		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();

		final ListBoxWithObjects<Group> groupsDropDown = new ListBoxWithObjects<Group>();
		for (Group grp : groupsToAssign) {
			groupsDropDown.addItem(grp);
		}

		final ListBoxWithObjects<Group> subgroupsDropDown = new ListBoxWithObjects<Group>();
		subgroupsDropDown.addNotSelectedOption();

		final CustomButton finishAssigningButton = TabMenu.getPredefinedButton(ButtonType.FINISH, ButtonTranslation.INSTANCE.finishGroupAssigning());

		final JsonCallbackEvents closeTabEvents = JsonCallbackEvents.closeTabDisableButtonEvents(finishAssigningButton, this);
		final JsonCallbackEvents disableButtonEvents = JsonCallbackEvents.disableButtonEvents(finishAssigningButton);

		finishAssigningButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// assign all
				if (groupsDropDown.getSelectedIndex() == 0) {
					AssignGroupsToResource request = new AssignGroupsToResource(closeTabEvents);
					request.assignGroupsToResource(groupsToAssign, resource);
				} else {
					// assign selected
					if (groupsDropDown.getSelectedObject() == null) {
						Window.alert("No group selected");
						return;
					}
					JsonCallbackEvents localEvents = new JsonCallbackEvents() {
						public void onFinished(JavaScriptObject jso) {
							closeTabEvents.onFinished(null); // call previus events
							groupsDropDown.removeSelectedItem(); // removed finished group
							if (groupsDropDown.isEmpty()) {
								// if no group left - close windows
								closeTabEvents.onFinished(null);
							}
						}
					};
					AssignGroupToResources request = new AssignGroupToResources(JsonCallbackEvents.disableButtonEvents(finishAssigningButton, localEvents));
					request.assignGroupToResources(groupsDropDown.getSelectedObject(), JsonUtils.<RichResource>toList(resource));
				}
			}
		});

		menu.addWidget(finishAssigningButton);
		menu.addWidget(new HTML("<strong>Selected group: </strong>"));
		menu.addWidget(groupsDropDown);
		// TODO - get subgroups correctly too
		//menu.addWidget(new HTML("<strong>Configure sub-groups: </strong>"));
		//menu.addWidget(subgroupsDropDown);

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		groupsDropDown.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event) {
				if (vp.getWidgetCount() == 2) { vp.remove(1); } // removes previous table
				if (groupsDropDown.getSelectedIndex() > 0) {
					/*
					// fill subgroups listbox with groups
					subgroupsDropDown.removeAllOption();
					subgroupsDropDown.removeNotSelectedOption();
					subgroupsDropDown.clear();
					subgroupsDropDown.addNotSelectedOption();
					for (Group grp : findSubgroups(groupsDropDown.getSelectedObject())){
					subgroupsDropDown.addItem(grp);
					}
					subgroupsDropDown.addAllOption();
					*/
					// some group selected
					TabItem ti = new GroupResourceRequiredAttributesTabItem(resourceId, groupsDropDown.getSelectedObject().getId());
					ti.draw();
					Widget w = ti.getWidget();
					vp.add(w);
					vp.setCellHeight(w, "100%");

				} else {
					/*
					//clear listbox
					subgroupsDropDown.removeAllOption();
					subgroupsDropDown.removeNotSelectedOption();
					subgroupsDropDown.clear();
					subgroupsDropDown.addNotSelectedOption();
					subgroupsDropDown.addItem("All");
					*/
					// all groups tab
					TabItem ti = new GroupsResourceRequiredAttributesTabItem(resourceId, groupsToAssign);
					ti.draw();
					Widget w = ti.getWidget();
					vp.add(w);
					vp.setCellHeight(w, "100%");
				}
			}
		});

		/*

			 subgroupsDropDown.addChangeHandler(new ChangeHandler(){
			 public void onChange(ChangeEvent event) {
			 if (vp.getWidgetCount() == 2) { vp.remove(1); } // removes previous table
			 if (subgroupsDropDown.getSelectedIndex() > 1){
// sub-group selected
TabItem ti = new GroupResourceRequiredAttributesTabItem(session, resourceId, subgroupsDropDown.getSelectedObject().getId());
ti.draw();
Widget w = ti.getWidget();
vp.add(w);
vp.setCellHeight(w, "100%");
} else if (subgroupsDropDown.getSelectedIndex() == 1){
// all selected
if (groupsDropDown.getSelectedIndex() > 0) {
// all subgroups of selected group
TabItem ti = new GroupsResourceRequiredAttributesTabItem(session, resourceId, subgroupsDropDown.getAllObjects());
ti.draw();
Widget w = ti.getWidget();
vp.add(w);
vp.setCellHeight(w, "100%");
} else {
		// all groups and subgroups
		TabItem ti = new GroupsResourceRequiredAttributesTabItem(session, resourceId, allGroups);
		ti.draw();
		Widget w = ti.getWidget();
		vp.add(w);
		vp.setCellHeight(w, "100%");
}
} else {
		// parent-group selected / subgroup not selected
		TabItem ti = new GroupResourceRequiredAttributesTabItem(session, resourceId, groupsDropDown.getSelectedObject().getId());
		ti.draw();
		Widget w = ti.getWidget();
		vp.add(w);
		vp.setCellHeight(w, "100%");
}
			 }
			 });

*/

TabItem ti = new GroupResourceRequiredAttributesTabItem(resourceId, groupsDropDown.getSelectedObject().getId());
ti.draw();
Widget w = ti.getWidget();
vp.add(w);
vp.setCellHeight(w, "100%");

groupsDropDown.addAllOption();

/*
// if some group selected
if (groupsDropDown.getSelectedObject() != null){
// fill subgroups listbox with groups
subgroupsDropDown.addNotSelectedOption();
for (Group grp : findSubgroups(groupsDropDown.getSelectedObject())){
subgroupsDropDown.addItem(grp);
}
subgroupsDropDown.addAllOption();
}
*/
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
	return SmallIcons.INSTANCE.groupGoIcon();
}

@Override
public int hashCode() {
	final int prime = 1031;
	int result = 1;
	result = prime * result + resourceId;
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
	ManageGroupsBeforeAssigning other = (ManageGroupsBeforeAssigning) obj;
	if (resourceId != other.resourceId)
		return false;
	return true;
}

public boolean multipleInstancesEnabled() {
	return false;
}

/**
 * Finds subgroups for selected group and fills the listbox with subgroups.
 *
 * @return list of all subgroups
 */
private ArrayList<Group> findSubgroups(Group group){

	ArrayList<Group> subgroups = new ArrayList<Group>();
	for (int i = allGroups.indexOf(group)+1; i<allGroups.size(); i++){
		if (allGroups.get(i).getIndent() >= group.getIndent()+1) {
			// if subgroup anywhere in this tree
			subgroups.add(allGroups.get(i));
		} else {
			// if same level group or subgroup of some other parent - break
			break;
		}
	}
	return subgroups;
}

public void open()
{
	session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
}

public boolean isAuthorized() {

	if (session.isVoAdmin(resource.getVoId())) {
		return true;
	} else {
		return false;
	}

}

static public String URL = "manage-before-assigning";

public String getUrl() {
	return URL;
}

public String getUrlWithParameters() {
	return ResourcesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?resId=" + resourceId + "&" + UrlMapper.getUrlFromList("to-assign", groupsToAssign)+"&length="+groupsToAssign.size();
}

}
