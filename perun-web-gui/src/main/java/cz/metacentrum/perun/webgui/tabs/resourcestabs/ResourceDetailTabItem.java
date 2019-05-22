package cz.metacentrum.perun.webgui.tabs.resourcestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.RichResource;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilityDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilityResourcesTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoResourcesTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;

import java.util.Map;

/**
 * Provides page with resource management and details
 * used by VO ADMINISTRATORS
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ResourceDetailTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading resource");

	// data
	private RichResource resource;
	private int resourceId;
	private TabPanelForTabItems tabPanel;

	private int facilityId = 0; // if non zero, tab was opened from facility admin section

	/**
	 * Creates a tab instance
	 * @param resource resource to get details for
	 * @param facilityId (optional) if tab was opened from facility manager section
	 */
	public ResourceDetailTabItem(RichResource resource, int facilityId){
		this.resource = resource;
		this.resourceId = resource.getId();
		this.tabPanel = new TabPanelForTabItems(this);
		this.facilityId = facilityId;
	}

	/**
	 * Creates a tab instance
	 * @param resourceId resource to get details for
	 */
	public ResourceDetailTabItem(int resourceId, int fid){
		this.resourceId = resourceId;
		new GetEntityById(PerunEntity.RICH_RESOURCE, resourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				resource = jso.cast();
			}
		}).retrieveData();
		this.tabPanel = new TabPanelForTabItems(this);
		this.facilityId = fid;
	}

	public boolean isPrepared(){
		return !(resource == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(resource.getName()));

		// main widget panel
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		AbsolutePanel dp = new AbsolutePanel();
		//dp.setStyleName("decoration");
		final FlexTable menu = new FlexTable();
		menu.setCellSpacing(5);

		// MAIN INFO PANEL

		// resource info
		menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.serverGroupIcon()));
		Label resourceName = new Label();
		resourceName.setText(Utils.getStrippedStringWithEllipsis(resource.getName(), 40));
		resourceName.setStyleName("now-managing");
		resourceName.setTitle(resource.getName());
		menu.setWidget(0, 1, resourceName);

		menu.setHTML(0, 2, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, 2, "25px");

		int column = 3;

		final JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				new GetEntityById(PerunEntity.RICH_RESOURCE, resourceId, new JsonCallbackEvents(){
					public void onFinished(JavaScriptObject jso){
						resource = jso.cast();
						open();
						draw();
					}
				}).retrieveData();
			}
		};

		CustomButton change = new CustomButton("",ButtonTranslation.INSTANCE.editResourceDetails(), SmallIcons.INSTANCE.applicationFormEditIcon());
		if (!session.isVoAdmin(resource.getVoId()) && !session.isFacilityAdmin(resource.getFacilityId())) change.setEnabled(false);
		change.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				// prepare confirm content
				Resource r = resource.cast();
				session.getTabManager().addTabToCurrentTab(new EditResourceDetailsTabItem(r, events));
			}
		});
		menu.setWidget(0, column, change);

		column++;
		menu.setHTML(0, column, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, column, "25px");

		column++;

		if (JsonUtils.isExtendedInfoVisible()) {
			menu.setHTML(0, column, "<strong>ID:</strong><br/><span class=\"inputFormInlineComment\">"+resource.getId()+"</span>");
			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;
		}

		if (facilityId > 0) {
			// facility admin view
			menu.setHTML(0, column, "<strong>VO:</strong><br/><span class=\"inputFormInlineComment\">"+resource.getVoId()+" / "+SafeHtmlUtils.fromString(resource.getVo().getShortName()).asString()+"</span>");
			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;
		}

		menu.setHTML(0, column, "<strong>Description:</strong><br/><span class=\"inputFormInlineComment\">"+ SafeHtmlUtils.fromString((resource.getDescription() != null) ? resource.getDescription() : "").asString()+"&nbsp;</span>");

		if (session.isFacilityAdmin(resource.getFacilityId())) {

			column++;

			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;

			Anchor a = new Anchor("View facility details >>");
			a.setStyleName("pointer");
			a.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					session.getTabManager().addTab(new FacilityDetailTabItem(resource.getFacility()));
				}
			});
			menu.setWidget(0, column, a);

		}

		dp.add(menu);
		vp.add(dp);
		vp.setCellHeight(dp, "30px");

		// TAB PANEL WITH INNER TABS
		tabPanel.clear();

		Resource r = resource.cast();
		tabPanel.add(new ResourceAssignedGroupsTabItem(r), "Assigned groups");
		tabPanel.add(new ResourceAssignedServicesTabItem(r), "Assigned services");
		tabPanel.add(new ResourceSettingsTabItem(r, null), "Service settings");
		tabPanel.add(new ResourceGroupSettingsTabItem(r), "Group settings");
		tabPanel.add(new ResourceMemberSettingsTabItem(resource), "Member settings");
		if (session.isVoAdmin(r.getVoId()) || session.isVoObserver(r.getVoId())) {
			tabPanel.add(new ResourceTagsTabItem(r), "Tags");
		}

		// Resize must be called after page fully displays
		Scheduler.get().scheduleDeferred(new Command() {
			@Override
			public void execute() {
				tabPanel.finishAdding();
			}
		});

		vp.add(tabPanel);
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
		return SmallIcons.INSTANCE.serverGroupIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1039;
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
		ResourceDetailTabItem other = (ResourceDetailTabItem) obj;
		if (resourceId != other.resourceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		if (facilityId == 0) {
			// opened from VO section
			session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
			session.getUiElements().getBreadcrumbs().setLocation(resource.getVo(), "Resources", VosTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+ VoResourcesTabItem.URL+"?id="+resource.getVoId());
			session.setActiveVo(resource.getVo());
		} else {
			// opened from facility section
			session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
			session.getUiElements().getBreadcrumbs().setLocation(resource.getFacility(), "Resources", FacilitiesTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+ FacilityResourcesTabItem.URL+"?id="+resource.getFacilityId());
			session.setActiveFacility(resource.getFacility());
		}

	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(resource.getVoId()) || session.isVoObserver(resource.getVoId()) || session.isFacilityAdmin(resource.getFacilityId())) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "detail";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return ResourcesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + resourceId + "&fid=" + facilityId;
	}

	static public ResourceDetailTabItem load(Map<String, String> parameters) {
		int id = Integer.parseInt(parameters.get("id"));
		int fid = Integer.parseInt(parameters.get("fid"));
		return new ResourceDetailTabItem(id, fid);
	}

	static public ResourceDetailTabItem load(RichResource resource, int fid) {
		return new ResourceDetailTabItem(resource, fid);
	}

}
