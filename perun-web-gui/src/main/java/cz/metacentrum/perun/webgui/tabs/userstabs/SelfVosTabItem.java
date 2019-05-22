package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.GetListOfAttributes;
import cz.metacentrum.perun.webgui.json.groupsManager.GetMemberGroups;
import cz.metacentrum.perun.webgui.json.membersManager.GetMemberByUser;
import cz.metacentrum.perun.webgui.json.membersManager.SetStatus;
import cz.metacentrum.perun.webgui.json.usersManager.GetVosWhereUserIsMember;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab with user's VO preferences
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SelfVosTabItem implements TabItem, TabItemWithUrl {

	PerunWebSession session = PerunWebSession.getInstance();

	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Loading user");

	private TabPanelForTabItems tabPanel;

	private User user;
	private int userId;
	private int selectedVoId = 0;

	/**
	 * Creates a tab instance
	 */
	public SelfVosTabItem(){
		this.user = session.getUser();
		this.userId = user.getId();
	}

	/**
	 * Creates a tab instance with custom user
	 * @param user
	 */
	public SelfVosTabItem(User user){
		this.user = user;
		this.userId = user.getId();
	}

	/**
	 * Creates a tab instance with custom user
	 * @param userId
	 */
	public SelfVosTabItem(int userId) {
		this.userId = userId;
		new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				user = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return !(user == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim())+": VO settings");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		final TabMenu menu = new TabMenu();
		final ScrollPanel sp = new ScrollPanel();
		sp.setSize("100%", "100%");
		sp.setStyleName("perun-tableScrollPanel");
		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

		final ListBoxWithObjects<VirtualOrganization> vosListbox = new ListBoxWithObjects<VirtualOrganization>();

		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addWidget(new HTML("<strong>Selected VO:</strong>"));
		menu.addWidget(vosListbox);

		vp.add(menu);
		vp.setCellHeight(menu, "50px");
		vp.setCellVerticalAlignment(menu, HasVerticalAlignment.ALIGN_MIDDLE);
		vp.add(new HTML("<hr size=\"1\" color=\"#ccc\">"));
		vp.add(sp);
		vp.setCellHeight(sp, "100%");

		final GetVosWhereUserIsMember whereMember = new GetVosWhereUserIsMember(userId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				ArrayList<VirtualOrganization> vos = JsonUtils.jsoAsList(jso);
				vos = new TableSorter<VirtualOrganization>().sortByName(vos);
				vosListbox.clear();
				if (vos != null && !vos.isEmpty()) {
					for (VirtualOrganization vo : vos) {
						vosListbox.addItem(vo);
						if (vo.getId() == selectedVoId) {
							vosListbox.setSelected(vo, true);
						}
					}
					vosListbox.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							selectedVoId = vosListbox.getSelectedObject().getId();
							sp.setWidget(displayVoSubtab(vosListbox.getSelectedObject()));
						}
					});
					sp.setWidget(displayVoSubtab(vosListbox.getSelectedObject()));
				} else {
					vosListbox.addItem("No VO available");
					sp.setWidget(new HTML(new Image(LargeIcons.INSTANCE.errorIcon()) + "<h2>You are not member of any VO.</h2>"));
				}
			}
		@Override
		public void onLoadingStart() {
			vosListbox.clear();
			vosListbox.addItem("Loading...");
		}
		@Override
		public void onError(PerunError error) {
			vosListbox.clear();
			vosListbox.addItem("Error while loading");
			sp.clear();
		}
		});
		whereMember.retrieveData();

		this.contentWidget.setWidget(vp);
		return getWidget();

	}

	/**
	 * Internal method which renders subtab for each (selected) VO
	 *
	 * @param vo VO to render preferences for
	 * @return subtab widget
	 */
	private Widget displayVoSubtab(final VirtualOrganization vo) {

		// do the layout
		HorizontalPanel horizontalSplitter = new HorizontalPanel();
		horizontalSplitter.setSize("100%", "100%");
		final VerticalPanel leftPanel = new VerticalPanel();
		final VerticalPanel rightPanel = new VerticalPanel();

		horizontalSplitter.add(leftPanel);
		horizontalSplitter.add(rightPanel);
		horizontalSplitter.setCellWidth(leftPanel, "50%");
		horizontalSplitter.setCellWidth(rightPanel, "50%");

		// VO overview

		FlexTable voHeader = new FlexTable();
		voHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.buildingIcon()));
		voHeader.setHTML(0, 1, "<p class=\"subsection-heading\">"+ SafeHtmlUtils.fromString((vo.getName() != null) ? vo.getName() : "").asString()+"</p>");

		final FlexTable voOverview = new FlexTable();
		voOverview.setStyleName("inputFormFlexTableDark");

		leftPanel.add(voHeader);
		leftPanel.add(voOverview);

		GetAttributes voAttrsCall = new GetAttributes(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				ArrayList<Attribute> attrs = JsonUtils.jsoAsList(jso);
				int i = 0;
				for (Attribute a : attrs) {
					if (a.getFriendlyName().equalsIgnoreCase("userManualsLink")) {
						voOverview.setHTML(i, 0, "User's manuals:");
						String val = SafeHtmlUtils.fromString((a.getValue() != null) ? a.getValue() : "").asString();
						Anchor link = new Anchor(val, val);
						link.getElement().setPropertyString("target", "_blank");
						voOverview.setWidget(i, 1, link);
						i++;
					} else if (a.getFriendlyName().equalsIgnoreCase("dashboardLink")) {
						voOverview.setHTML(i, 0, "Dashboard:");
						String val = SafeHtmlUtils.fromString((a.getValue() != null) ? a.getValue() : "").asString();
						Anchor link = new Anchor(val, val);
						link.getElement().setPropertyString("target", "_blank");
						voOverview.setWidget(i, 1, link);
						i++;
					} else if (a.getFriendlyName().equalsIgnoreCase("contactEmail")) {
						voOverview.setHTML(i, 0, "VO contact:");
						voOverview.setHTML(i, 1, SafeHtmlUtils.fromString((a.getValue() != null) ? a.getValue() : "").asString());
						i++;
					}
				}
				// no rows in selected result
				if (i < 1) {
					voOverview.setHTML(i, 0, "VO doesn't provide any details or contacts about itself.");
					voOverview.getFlexCellFormatter().setStyleName(i, 0, "inputFormInlineComment");
				} else {
					for (int n=0; n<voOverview.getRowCount(); n++) {
						voOverview.getFlexCellFormatter().setStyleName(n, 0, "itemName");
						voOverview.getFlexCellFormatter().setWidth(n, 0, "200px");
					}
				}
			}
		@Override
		public void onError(PerunError error) {
			voOverview.setHTML(0, 0, "Error while loading");
		}
		@Override
		public void onLoadingStart() {
			voOverview.setWidget(0, 0, new AjaxLoaderImage());
		}

		});
		voAttrsCall.getVoAttributes(vo.getId());
		voAttrsCall.retrieveData();

		// CONTACT INFO

		FlexTable contactHeader = new FlexTable();
		contactHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.vcardIcon()));
		contactHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Contact</p>");

		final PerunAttributeTableWidget contactTable = new PerunAttributeTableWidget();
		contactTable.setDark(true);
		contactTable.setDisplaySaveButton(false);

		leftPanel.add(contactHeader);

		final GetListOfAttributes attributes = new GetListOfAttributes(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				contactTable.add(new TableSorter<Attribute>().sortByAttrNameTranslation(JsonUtils.<Attribute>jsoAsList(jso)));
				leftPanel.add(contactTable.getSaveButton());
				leftPanel.setCellHeight(contactTable.getSaveButton(), "50px");
				leftPanel.add(contactTable);
			}
		});
		final ArrayList<String> list = new ArrayList<String>();
		list.add("urn:perun:member:attribute-def:def:organization");
		list.add("urn:perun:member:attribute-def:def:workplace");
		list.add("urn:perun:member:attribute-def:opt:researchGroup");
		list.add("urn:perun:member:attribute-def:def:mail");
		list.add("urn:perun:member:attribute-def:def:phone");
		list.add("urn:perun:member:attribute-def:def:address");

		GetMemberByUser mem = new GetMemberByUser(vo.getId(), userId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				Member m = jso.cast();
				HashMap<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("member", m.getId());
				contactTable.setIds(ids);
				attributes.getListOfAttributes(ids, list);
			}
		});
		mem.retrieveData();

		// MEMBERSHIP STATE

		FlexTable membershipHeader = new FlexTable();
		membershipHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.userGreenIcon()));
		membershipHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Membership</p>");

		// Membership table
		final FlexTable memberLayout = new FlexTable();
		memberLayout.setStyleName("inputFormFlexTableDark");

		rightPanel.add(membershipHeader);
		rightPanel.add(memberLayout);

		GetMemberByUser call = new GetMemberByUser(vo.getId(), userId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {

				final Member m = jso.cast();

				memberLayout.setHTML(0, 0, "Status:");
				memberLayout.setHTML(1, 0, "Expiration:");

				memberLayout.getFlexCellFormatter().setStyleName(0, 0, "itemName");
				memberLayout.getFlexCellFormatter().setStyleName(1, 0, "itemName");

				// fill inner layout
				PerunStatusWidget<Member> statusWidget;
				if (session.isVoAdmin(vo.getId())) {
					JsonCallbackEvents event = new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso) {
							// REFRESH PARENT TAB
							draw();
						}
					};
					statusWidget = new PerunStatusWidget<Member>(m, user.getFullName(), event);
				} else {
					statusWidget = new PerunStatusWidget<Member>(m, user.getFullName(), null);
				}

				memberLayout.setWidget(0, 1, statusWidget);

				HashMap<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("member", m.getId());

				GetListOfAttributes attrCall = new GetListOfAttributes(new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						ArrayList<Attribute> la = JsonUtils.jsoAsList(jso);
						if (la != null && !la.isEmpty()) {
							for (Attribute a : la) {
								if ("urn:perun:member:attribute-def:def:membershipExpiration".equals(a.getName())) {
									RichMember rm = m.cast();
									// store value into richmember
									rm.setAttribute(a);
									memberLayout.setWidget(1, 1, new MembershipExpirationWidget(rm));
								}
							}
						}
					}
				});
				ArrayList<String> ls = new ArrayList<String>();
				ls.add("urn:perun:member:attribute-def:def:membershipExpiration");
				attrCall.getListOfAttributes(ids, ls);

			}
		@Override
		public void onLoadingStart(){
			memberLayout.setWidget(0, 0, new AjaxLoaderImage());
		}
		@Override
		public void onError(PerunError error){
			memberLayout.setWidget(0, 0, new AjaxLoaderImage().loadingError(error));
		}
		});
		call.retrieveData();

		// RESOURCES SETTINGS

		FlexTable resourcesSettingsHeader = new FlexTable();
		resourcesSettingsHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.settingToolsIcon()));
		resourcesSettingsHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Resources settings</p>");

		FlexTable resourcesSettingsTable = new FlexTable();
		resourcesSettingsTable.setStyleName("inputFormFlexTable");

		Anchor a = new Anchor();
		a.setText("Go to Resources settings page >>");
		a.setStyleName("pointer");
		a.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (tabPanel != null) {
					// resource settings tab is next to this one
					SelfResourcesSettingsTabItem tab = ((SelfResourcesSettingsTabItem)tabPanel.getTabItem(tabPanel.getSelectedIndex() + 1));
					tab.setVo(vo);
					tab.draw();
					tabPanel.selectTab(tabPanel.getSelectedIndex()+1);
				} else {
					session.getTabManager().addTab(new SelfResourcesSettingsTabItem(user, vo), true);
				}
			}
		});

		resourcesSettingsTable.setHTML(0, 0, "Manage VO resources specific settings like: shell, data/files quotas, mailing list exclusions");
		resourcesSettingsTable.getFlexCellFormatter().setStyleName(0, 0, "inputFormInlineComment");
		resourcesSettingsTable.setWidget(1, 0, a);

		rightPanel.add(resourcesSettingsHeader);
		rightPanel.add(resourcesSettingsTable);

		// GROUPS

		FlexTable groupsHeader = new FlexTable();
		groupsHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.groupIcon()));
		groupsHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Groups</p>");

		final FlexTable groupsTable = new FlexTable();
		groupsTable.setStyleName("inputFormFlexTable");

		rightPanel.add(groupsHeader);
		rightPanel.add(groupsTable);

		GetMemberByUser memCall = new GetMemberByUser(vo.getId(), userId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				Member m = jso.cast();
				GetMemberGroups groupsCall = new GetMemberGroups(m.getId(), new JsonCallbackEvents(){
					@Override
					public void onError(PerunError error) {
						groupsTable.setWidget(0, 0, new AjaxLoaderImage().loadingError(error));
					}
				@Override
				public void onFinished(JavaScriptObject jso) {
					ArrayList<Group> list = JsonUtils.jsoAsList(jso);
					if (list.isEmpty() || list == null) {
						groupsTable.setHTML(0, 0, "You aren't member of any group in this VO.");
						return;
					}
					groupsTable.addStyleName("userDetailTable");
					groupsTable.setHTML(0, 0, "<strong>Name</strong>");
					groupsTable.setHTML(0, 1, "<strong>Description</strong>");
					for (int i=0; i<list.size(); i++){
						groupsTable.setHTML(i+1, 0, SafeHtmlUtils.fromString(list.get(i).getName()).asString());
						groupsTable.setHTML(i+1, 1, SafeHtmlUtils.fromString(list.get(i).getDescription()).asString());
					}
				}
				});
				groupsCall.retrieveData();
			}
		@Override
		public void onError(PerunError error) {
			groupsTable.setWidget(0, 0, new AjaxLoaderImage().loadingError(error));
		}
		@Override
		public void onLoadingStart() {
			groupsTable.setWidget(0, 0, new AjaxLoaderImage().loadingStart());
		}
		});
		memCall.retrieveData();

		return horizontalSplitter;

	}

	public void setParentPanel(TabPanelForTabItems panel) {
		this.tabPanel = panel;
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
		final int prime = 1571;
		int result = 432;
		result = prime * result * userId;
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
		if (this.userId != ((SelfVosTabItem)obj).userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.setActiveUser(user);
		session.getUiElements().getMenu().openMenu(MainMenu.USER);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()), UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId, "VO settings", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isSelf(userId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "vos";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return  UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId;
	}

	static public SelfVosTabItem load(Map<String, String> parameters) {

		if (parameters.containsKey("id")) {
			int uid = Integer.parseInt(parameters.get("id"));
			if (uid != 0) {
				return new SelfVosTabItem(uid);
			}
		}
		return new SelfVosTabItem();
	}

}
