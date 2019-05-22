package cz.metacentrum.perun.webgui.tabs.vostabs;

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
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;

import java.util.Map;

/**
 * View VO details
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class VoDetailTabItem implements TabItem, TabItemWithUrl{

	/**
	 * VO
	 */
	private VirtualOrganization vo;

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

	private int voId;
	private TabPanelForTabItems tabPanel;

	/**
	 * Creates a new view VO class
	 *
	 * @param vo
	 */
	public VoDetailTabItem(VirtualOrganization vo){
		this.vo = vo;
		this.voId = vo.getId();
		tabPanel = new TabPanelForTabItems(this);
	}

	/**
	 * Creates a new view VO class
	 *
	 * @param voId
	 */
	public VoDetailTabItem(int voId){
		this.voId = voId;
		tabPanel = new TabPanelForTabItems(this);
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
	}

	public boolean isPrepared(){
		return !(vo == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName()));

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// The table
		AbsolutePanel dp = new AbsolutePanel();
		//dp.setStyleName("decoration");
		final FlexTable menu = new FlexTable();
		menu.setCellSpacing(5);

		// Add VO information
		menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.buildingIcon()));
		Label voName = new Label();
		voName.setText(Utils.getStrippedStringWithEllipsis(vo.getName(), 40));
		voName.setStyleName("now-managing");
		voName.setTitle(vo.getName());
		menu.setWidget(0, 1, voName);

		menu.setHTML(0, 2, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, 2, "25px");

		int column = 3;

		if (session.isVoAdmin(voId)) {

			final JsonCallbackEvents events = new JsonCallbackEvents() {
				@Override
				public void onFinished(JavaScriptObject jso) {
					// set VO and redraw tab
					vo = jso.cast();
					open();
					draw();
				}
			};

			CustomButton change = new CustomButton("", "Edit VO name", SmallIcons.INSTANCE.applicationFormEditIcon());
			change.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					session.getTabManager().addTabToCurrentTab(new EditVoDetailsTabItem(vo, events));
				}
			});
			menu.setWidget(0, column, change);

			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;

		}

		if (JsonUtils.isExtendedInfoVisible()) {
			menu.setHTML(0, column, "<strong>ID:</strong><br/><span class=\"inputFormInlineComment\">"+vo.getId()+"</span>");
			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;

			menu.setHTML(0, column, "<strong>Short&nbsp;name:</strong><br/><span class=\"inputFormInlineComment\">"+ SafeHtmlUtils.fromString((vo.getShortName() != null) ? vo.getShortName() : "").asString()+"</span>");

		}

		/*
		CustomButton cb = new CustomButton("", "Refresh page content", SmallIcons.INSTANCE.updateIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				tabPanel.getSelectedTabItem().draw();
			}
		});
		dp.add(cb);
		cb.getElement().setAttribute("style", "position: absolute; right: 50px; top: 5px;");



		final JsonCallbackEvents events = new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				// set VO and redraw tab
				vo = jso.cast();
				open();
				draw();
			}
		};

		CustomButton change = new CustomButton("", "Edit VO name", SmallIcons.INSTANCE.applicationFormEditIcon());
		change.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new EditVoDetailsTabItem(vo, events));
			}
		});

		if (!session.isVoAdmin(voId)) change.setEnabled(false);
		dp.add(change);
		change.getElement().setAttribute("style", "position: absolute; right: 5px; top: 5px;");
		*/

		dp.add(menu);
		vp.add(dp);
		vp.setCellHeight(dp, "30px");

		tabPanel.clear();

		tabPanel.add(new VoOverviewTabItem(vo), "Overview");
		tabPanel.add(new VoMembersTabItem(vo), "Members");
		tabPanel.add(new VoGroupsTabItem(vo), "Groups");
		tabPanel.add(new VoResourcesTabItem(vo), "Resources");
		tabPanel.add(new VoApplicationsTabItem(vo), "Applications");
		tabPanel.add(new VoApplicationFormSettingsTabItem(vo), "Application form");
		tabPanel.add(new VoSettingsTabItem(vo), "Settings");
		tabPanel.add(new VoManagersTabItem(vo), "Managers");
		tabPanel.add(new VoExtSourcesTabItem(vo), "External sources");
		//tabPanel.add(new VoFacilitiesPropagationsTabItem(session, vo), "Facilities states");

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
		return SmallIcons.INSTANCE.buildingIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1307;
		int result = 1;
		result = prime * result + voId;
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
		VoDetailTabItem other = (VoDetailTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(vo, "", "");
		if(vo != null){
			session.setActiveVo(vo);
		} else {
			session.setActiveVoId(voId);
		}
	}

	public boolean isAuthorized() {
		if (session.isVoAdmin(voId) || session.isVoObserver(voId)) {
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
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + voId;
	}

	static public VoDetailTabItem load(Map<String, String> parameters) {
		int voId = Integer.parseInt(parameters.get("id"));
		return new VoDetailTabItem(voId);
	}

}
