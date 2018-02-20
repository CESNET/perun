package cz.metacentrum.perun.webgui.tabs.securitytabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.SecurityTeam;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;

import java.util.Map;

/**
 * View SecurityTeam details
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SecurityTeamDetailTabItem implements TabItem, TabItemWithUrl{

	/**
	 * SecTeam
	 */
	private SecurityTeam securityTeam;

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
	private Label titleWidget = new Label("Loading vo");

	private int securityTeamId;
	private TabPanelForTabItems tabPanel;

	/**
	 * Creates a new view SecurityTeam class
	 *
	 * @param securityTeam
	 */
	public SecurityTeamDetailTabItem(SecurityTeam securityTeam){
		this.securityTeam = securityTeam;
		this.securityTeamId = securityTeam.getId();
		tabPanel = new TabPanelForTabItems(this);
	}

	/**
	 * Creates a new view VO class
	 *
	 * @param securityTeamId
	 */
	public SecurityTeamDetailTabItem(int securityTeamId){
		this.securityTeamId = securityTeamId;
		tabPanel = new TabPanelForTabItems(this);
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				securityTeam = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.SECURITY_TEAM, securityTeamId, events).retrieveData();
	}

	public boolean isPrepared(){
		return !(securityTeam == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(securityTeam.getName()));

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// The table
		AbsolutePanel dp = new AbsolutePanel();
		//dp.setStyleName("decoration");
		final FlexTable menu = new FlexTable();
		menu.setCellSpacing(5);

		// Add VO information
		menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.userPoliceEnglandIcon()));
		Label voName = new Label();
		voName.setText(Utils.getStrippedStringWithEllipsis(securityTeam.getName(), 40));
		voName.setStyleName("now-managing");
		voName.setTitle(securityTeam.getName());
		menu.setWidget(0, 1, voName);

		menu.setHTML(0, 2, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, 2, "25px");

		int column = 3;

		final JsonCallbackEvents events = new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				// set VO and redraw tab
				securityTeam = jso.cast();
				open();
				draw();
			}
		};

		CustomButton change = new CustomButton("", "Edit SecurityTeam details", SmallIcons.INSTANCE.applicationFormEditIcon());
		change.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new EditSecurityTeamDetailsTabItem(securityTeam, events));
			}
		});
		menu.setWidget(0, column, change);
		column++;

		menu.setHTML(0, column, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, column, "25px");
		column++;

		if (JsonUtils.isExtendedInfoVisible()) {
			menu.setHTML(0, column, "<strong>ID:</strong><br/><span class=\"inputFormInlineComment\">"+securityTeam.getId()+"</span>");
			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;
		}

		menu.setHTML(0, column, "<strong>Description:</strong><br/><span class=\"inputFormInlineComment\">"+ SafeHtmlUtils.fromString((securityTeam.getDescription() != null) ? securityTeam.getDescription() : "").asString()+"</span>");

		dp.add(menu);
		vp.add(dp);
		vp.setCellHeight(dp, "30px");

		tabPanel.clear();

		tabPanel.add(new SecurityTeamMembersTabItem(securityTeam), "Members");
		tabPanel.add(new SecurityTeamBlacklistTabItem(securityTeam), "Blacklist");

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
		return SmallIcons.INSTANCE.userPoliceEnglandIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1307;
		int result = 1;
		result = prime * result + securityTeamId;
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
		SecurityTeamDetailTabItem other = (SecurityTeamDetailTabItem) obj;
		if (securityTeamId != other.securityTeamId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.SECURITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(securityTeam, "", "");
		if(securityTeam != null){
			session.setActiveSecurityTeam(securityTeam);
		} else {
			session.setActiveSecurityTeamId(securityTeamId);
		}
	}

	public boolean isAuthorized() {
		if (session.isSecurityAdmin(securityTeamId)) {
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
		return SecurityTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + securityTeamId;
	}

	static public SecurityTeamDetailTabItem load(Map<String, String> parameters) {
		int teamId = Integer.parseInt(parameters.get("id"));
		return new SecurityTeamDetailTabItem(teamId);
	}

}
