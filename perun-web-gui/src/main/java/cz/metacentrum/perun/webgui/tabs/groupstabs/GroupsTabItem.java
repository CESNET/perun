package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllRichGroups;
import cz.metacentrum.perun.webgui.json.vosManager.GetVos;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichGroup;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab, which displays Groups where user is Admin
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class GroupsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Groups");

	private int voId = 0;
	private VirtualOrganization vo = null;

	/**
	 * Creates a tab instance
	 *
	 * @param vo vo to show groups for (if null, you can select VO from list)
	 */
	public GroupsTabItem(VirtualOrganization vo){
		this.vo = vo;
		if (vo != null) {
			this.voId = vo.getId();
		}
	}

	/**
	 * Creates a tab instance
	 *
	 * @param voId vo to show groups for (if 0, you can select VO from list)
	 */
	public GroupsTabItem(int voId){
		this.voId = voId;
		if (voId != 0) {
			JsonCallbackEvents events = new JsonCallbackEvents(){
				public void onFinished(JavaScriptObject jso) {
					vo = jso.cast();
				}
			};
			new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
		}
	}

	public boolean isPrepared(){
		// if vo selected and loaded or no vo selected
		return ((voId != 0 && vo != null) || (voId == 0 && vo == null));
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");
		// Horizontal menu
		TabMenu menu = new TabMenu();

		menu.addWidget(UiElements.getRefreshButton(this));

		//call
		ArrayList<String> attrNames = new ArrayList<>();
		attrNames.add("urn:perun:group:attribute-def:def:synchronizationEnabled");
		attrNames.add("urn:perun:group:attribute-def:def:synchronizationInterval");
		attrNames.add("urn:perun:group:attribute-def:def:lastSynchronizationState");
		attrNames.add("urn:perun:group:attribute-def:def:lastSuccessSynchronizationTimestamp");
		attrNames.add("urn:perun:group:attribute-def:def:lastSynchronizationTimestamp");
		attrNames.add("urn:perun:group:attribute-def:def:authoritativeGroup");
		attrNames.add("urn:perun:group:attribute-def:def:groupSynchronizationTimes");
		attrNames.add("urn:perun:group:attribute-def:def:startOfLastSuccessfulSynchronization");
		final GetAllRichGroups groups = new GetAllRichGroups(voId, attrNames);
		groups.setCheckable(false);

		// listbox
		final ListBoxWithObjects<VirtualOrganization> vos = new ListBoxWithObjects<VirtualOrganization>();
		menu.addWidget(new HTML("<strong>Selected VO: </strong>"));
		menu.addWidget(vos);
		menu.addFilterWidget(new ExtendedSuggestBox(groups.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				groups.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterGroup());
		menu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
		menu.addWidget(new HTML("<strong>Select VO to see groups that you can manage.</strong>"));

		final TabItem tab = this;

		// groups table
		CellTable<RichGroup> table = groups.getEmptyTable(new FieldUpdater<RichGroup, String>() {
			public void update(int index, RichGroup group, String value) {
				session.getTabManager().addTab(new GroupDetailTabItem(group));
				// close group selection tab when group is selected
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		});

		// listbox change => load vo's groups
		vos.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				groups.setVoId(vos.getSelectedObject().getId());
				groups.clearTable();
				groups.retrieveData();
			}
		});

		// initial fill listbox and trigger groups loading
		GetVos vosCall = new GetVos(new JsonCallbackEvents(){
			public void onLoadingStart(){
				vos.clear();
				vos.addItem("Loading...");
			}
			public void onFinished(JavaScriptObject jso) {
				vos.clear();
				ArrayList<VirtualOrganization> returnedVos = JsonUtils.jsoAsList(jso);
				returnedVos = new TableSorter<VirtualOrganization>().sortByName(returnedVos);
				if (returnedVos == null || returnedVos.isEmpty()){
					vos.addItem("No VO available");
					return;
				}
				// put and set selected
				for (int i = 0; i<returnedVos.size(); i++) {
					vos.addItem(returnedVos.get(i));
					if (voId == returnedVos.get(i).getId()) {
						vos.setSelected(returnedVos.get(i), true);
					}
				}
				// trigger loading of groups for selected VO
				groups.setVoId(vos.getSelectedObject().getId());
				groups.clearTable();
				groups.retrieveData();
			}
			public void onError(PerunError error){
				vos.clear();
				vos.addItem("Error while loading");
			};
		});
		vosCall.retrieveData();

		// set width to 100%
		table.setWidth("100%");

		// add menu and table to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, this);

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
		return SmallIcons.INSTANCE.groupIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1427;
		int result = 1;
		result = prime * result + 341;
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
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.GROUP_ADMIN, "Select group", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isGroupAdmin() || session.isVoAdmin() || session.isVoObserver()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "list";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl()+"?vo="+voId;
	}

	static public GroupsTabItem load(Map<String, String> parameters) {
		int id = Integer.parseInt(parameters.get("vo"));
		return new GroupsTabItem(id);
	}

}
