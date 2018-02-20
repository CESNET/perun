package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetListOfAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.GetLogins;
import cz.metacentrum.perun.webgui.json.usersManager.GetUserExtSources;
import cz.metacentrum.perun.webgui.json.usersManager.RemoveUserExtSource;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.model.UserExtSource;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.PerunAttributeTableWidget;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab with user's authentications settings (logins / passwords / certificates)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SelfAuthenticationsTabItem implements TabItem, TabItemWithUrl {

	PerunWebSession session = PerunWebSession.getInstance();

	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Loading user");

	private User user;
	private int userId;

	/**
	 * Creates a tab instance
	 */
	public SelfAuthenticationsTabItem(){
		this.user = session.getActiveUser();
		this.userId = user.getId();
	}

	/**
	 * Creates a tab instance with custom user
	 * @param user
	 */
	public SelfAuthenticationsTabItem(User user){
		this.user = user;
		this.userId = user.getId();
	}

	/**
	 * Creates a tab instance with custom user
	 * @param userId
	 */
	public SelfAuthenticationsTabItem(int userId) {
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

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim())+": Authentication");

		final String notSet = "<i>N/A</i>";

		// content
		ScrollPanel vp = new ScrollPanel();
		vp.setSize("100%","100%");

		final VerticalPanel innerVp = new VerticalPanel();
		innerVp.setSize("100%", "100%");

		final TabMenu menu = new TabMenu();
		innerVp.add(menu);
		innerVp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		final FlexTable layout = new FlexTable();
		layout.setSize("100%","100%");
		vp.add(innerVp);
		innerVp.add(layout);

		layout.setStyleName("perun-table");
		vp.setStyleName("perun-tableScrollPanel");
		session.getUiElements().resizeSmallTabPanel(vp, 350, this);

		FlexTable loginsHeader = new FlexTable();
		loginsHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.keyIcon()));
		loginsHeader.setHTML(0, 1, "<p>Logins");
		loginsHeader.getFlexCellFormatter().setStyleName(0, 1, "subsection-heading");
		layout.setWidget(0, 0, loginsHeader);

		FlexTable certHeader = new FlexTable();
		certHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.sslCertificatesIcon()));
		certHeader.setHTML(0, 1, "<p>Certificates");
		certHeader.getFlexCellFormatter().setStyleName(0, 1, "subsection-heading");
		layout.setWidget(3, 0, certHeader);

		layout.setHTML(4, 0, "To <strong>add certificate</strong> please visit <a href=\""+ Utils.getIdentityConsolidatorLink(false)+"\" target=\"_blank\">identity consolidator &gt;&gt;</a> and select \"Using personal certificate\" option.<br />&nbsp;");

		FlexTable sshHeader = new FlexTable();
		sshHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.serverKeyIcon()));
		sshHeader.setHTML(0, 1, "<p>Kerberos & SSH keys");
		sshHeader.getFlexCellFormatter().setStyleName(0, 1, "subsection-heading");
		layout.setWidget(6, 0, sshHeader);

		// login table
		final FlexTable loginsTable = new FlexTable();
		loginsTable.setStyleName("inputFormFlexTableDark");

		final GetLogins logins = new GetLogins(userId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {

				final ArrayList<Attribute> list = JsonUtils.jsoAsList(jso);
				layout.setWidget(1, 0, loginsTable);

				int row = 0;

				if (user.isServiceUser() || user.isSponsoredUser()) {
					CustomButton addLogin = TabMenu.getPredefinedButton(ButtonType.ADD, "Add new login (only for supported namespaces)");
					addLogin.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							session.getTabManager().addTabToCurrentTab(new AddLoginTabItem(user, list));
						}
					});
					loginsTable.setWidget(row, 0, addLogin);
					row++;
				}

				if (list != null && !list.isEmpty()) {
					for (final Attribute a : list) {
						loginsTable.setHTML(row, 0, "Login in "+a.getFriendlyNameParameter().toUpperCase()+":");
						loginsTable.getFlexCellFormatter().setWidth(row, 0, "150px");
						loginsTable.getFlexCellFormatter().addStyleName(row, 0, "itemName");
						loginsTable.setHTML(row, 1, SafeHtmlUtils.fromString((a.getValue() != null) ? a.getValue() : "").asString());
						loginsTable.getFlexCellFormatter().setWidth(row, 1, "150px");
						// change password if possible
						if (Utils.getSupportedPasswordNamespaces().contains(a.getFriendlyNameParameter())) {
							FlexTable fw = new FlexTable();
							fw.addStyleName("padding-vertical");

							CustomButton cb = new CustomButton("Change password…", SmallIcons.INSTANCE.keyIcon(), new ClickHandler(){
								public void onClick(ClickEvent event) {
									session.getTabManager().addTabToCurrentTab(new SelfPasswordTabItem(user, a.getFriendlyNameParameter(), a.getValue(), SelfPasswordTabItem.Actions.CHANGE));
								}
							});
							CustomButton cb2 = new CustomButton("Reset password…", SmallIcons.INSTANCE.keyIcon(), new ClickHandler(){
								public void onClick(ClickEvent event) {
									// OPEN PASSWORD RESET APPLICATION ON SAME SERVER
									Window.open("" + Utils.getPasswordResetLink(a.getFriendlyNameParameter()), "_blank", "");
								}
							});
							fw.setWidget(0, 0, cb);
							if (!user.isServiceUser()) {
								fw.setWidget(0, 1, cb2);
							} else {
								cb.setText("Reset password…");
							}

							loginsTable.setWidget(row, 2, fw);
						}
						row++;
					}
				} else {
					loginsTable.setHTML(row, 0, "You don't have any login in supported namespaces.");
					loginsTable.getFlexCellFormatter().setStyleName(0, 0, "inputFormInlineComment");
				}

			}
		@Override
		public void onLoadingStart() {
			layout.setWidget(1, 0, new AjaxLoaderImage().loadingStart());
		}
		@Override
		public void onError(PerunError error) {
			layout.setHTML(1, 0, "Error while loading logins.");
			layout.getFlexCellFormatter().setStyleName(1, 0, "serverResponseLabelError");
		}
		});
		logins.retrieveData();

		// certificates table
		final FlexTable certTable = new FlexTable();
		certTable.addStyleName("inputFormFlexTableDark");
		certTable.setHTML(0, 0, "Certificates: ");
		certTable .getFlexCellFormatter().addStyleName(0, 0, "itemName");
		certTable .getFlexCellFormatter().setWidth(0, 0, "150px");
		layout.setWidget(5, 0, certTable);

		final GetUserExtSources ueses = new GetUserExtSources(userId);
		ueses.setEvents(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				ArrayList<UserExtSource> list = JsonUtils.jsoAsList(jso);
				if (list != null && !list.isEmpty()) {
					boolean found = false;
					FlexTable tab = new FlexTable();
					int i = 0; // rowcounter
					for (final UserExtSource a : list) {
						if (a.getExtSource().getType().equals("cz.metacentrum.perun.core.impl.ExtSourceX509")) {
							found = true;
							tab.setHTML(i++, 0, "<strong>"+SafeHtmlUtils.fromString((a.getLogin() != null) ? a.getLogin() : "").asString()+"</strong>");
							tab.setHTML(i++, 0, "Issuer: " + SafeHtmlUtils.fromString((a.getExtSource().getName() != null) ? a.getExtSource().getName() : "").asString());
							if (!a.isPersistent()) {
								CustomButton removeButton = new CustomButton("Remove", SmallIcons.INSTANCE.deleteIcon(), new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										RemoveUserExtSource remove = new RemoveUserExtSource(new JsonCallbackEvents() {
											@Override
											public void onFinished(JavaScriptObject jso) {
												// reload whole tab
												ueses.retrieveData();
											}
										});
										remove.removeUserExtSource(userId, a.getId());
									}
								});
								// add button to table
								tab.getFlexCellFormatter().setRowSpan(i - 2, 1, 2);
								tab.setWidget(i - 2, 1, removeButton);
							}
						}
					}
					if (found) {
						certTable.setWidget(0, 1, tab);
					} else {
						certTable.setHTML(0, 1, notSet);
					}
				} else {
					certTable.setHTML(0, 1, notSet);
				}
			}
		@Override
		public void onError(PerunError error) {
			certTable.setHTML(0, 1, "Error while loading certificates. Refresh page to retry.");
			certTable.getFlexCellFormatter().setStyleName(0, 1, "serverResponseLabelError");
		}
		@Override
		public void onLoadingStart() {
			certTable.setWidget(0, 1, new Image(AjaxLoaderImage.SMALL_IMAGE_URL));
		}
		});
		ueses.retrieveData();

		// Kerberos and SSH table

		Map<String, Integer> ids = new HashMap<>();
		ids.put("user", userId);

		final PerunAttributeTableWidget table = new PerunAttributeTableWidget(ids);
		table.setDark(true);
		table.setDescriptionShown(true);

		final GetListOfAttributes attrs2 = new GetListOfAttributes(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				table.add(JsonUtils.<Attribute>jsoAsList(jso));
				layout.setWidget(7, 0, table);
			}
		@Override
		public void onError(PerunError error) {
			layout.setHTML(7, 0, "Error while loading Kerberos and SSH settings. Refresh page to retry.");
			layout.getFlexCellFormatter().setStyleName(7, 0, "serverResponseLabelError");
		}
		@Override
		public void onLoadingStart() {
			layout.setWidget(7, 0, new AjaxLoaderImage().loadingStart());
		}
		});
		ArrayList<String> list2 = new ArrayList<String>();
		list2.add("urn:perun:user:attribute-def:def:kerberosAdminPrincipal");
		list2.add("urn:perun:user:attribute-def:def:sshPublicAdminKey");
		list2.add("urn:perun:user:attribute-def:def:sshPublicKey");
		attrs2.getListOfAttributes(ids, list2);

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
		return SmallIcons.INSTANCE.keyIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1559;
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
		if (this.userId != ((SelfAuthenticationsTabItem)obj).userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.setActiveUser(user);
		session.getUiElements().getMenu().openMenu(MainMenu.USER);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()), UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId, "Authentication", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isSelf(userId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "authz";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return  UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId;
	}

	static public SelfAuthenticationsTabItem load(Map<String, String> parameters) {

		if (parameters.containsKey("id")) {
			int uid = Integer.parseInt(parameters.get("id"));
			if (uid != 0) {
				return new SelfAuthenticationsTabItem(uid);
			}
		}
		return new SelfAuthenticationsTabItem();
	}

}
