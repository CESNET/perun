package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.usersManager.GetSpecificUsersByUser;
import cz.metacentrum.perun.webgui.json.usersManager.GetUsersBySpecificUser;
import cz.metacentrum.perun.webgui.json.usersManager.RemoveSpecificUserOwner;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab for managing service identities for users.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SelfServiceUsersTabItem implements TabItem, TabItemWithUrl {

	private PerunWebSession session = PerunWebSession.getInstance();
	private User user;
	private int userId;

	private Label titleWidget = new Label("Loading user");
	private SimplePanel contentWidget = new SimplePanel();

	public SelfServiceUsersTabItem(User user) {
		this.user = user;
		this.userId = user.getId();
	}

	public SelfServiceUsersTabItem(int userId) {
		this.userId = userId;
		new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				user = jso.cast();
			}
		}).retrieveData();
	}

	@Override
	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		if (user.isServiceUser()) {

			// SERVICE TYPE user
			this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim())+": Associated users");

			// request
			final GetUsersBySpecificUser request = new GetUsersBySpecificUser(userId);
			final JsonCallbackEvents refreshEvents = JsonCallbackEvents.refreshTableEvents(request);

			// menu
			TabMenu menu = new TabMenu();
			vp.add(menu);
			vp.setCellHeight(menu, "30px");

			menu.addWidget(UiElements.getRefreshButton(this));

			// buttons
			menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Add new user to service identity: "+user.getLastName(), new ClickHandler() {
				public void onClick(ClickEvent clickEvent) {
					session.getTabManager().addTabToCurrentTab(new ConnectServiceIdentityTabItem(user), true);
				}
			}));

			final CustomButton removeUserButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove user from service identity "+user.getLastName());
			menu.addWidget(removeUserButton);
			removeUserButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent clickEvent) {

					final ArrayList<User> list = request.getTableSelectedList();
					final ArrayList<User> fullList = request.getList();

					if (fullList.size() == list.size()) {

						UiElements.generateAlert("Remove warning", "<strong><span class=\"serverResponseLabelError\">If you remove all users from service identity you won't be able to use it in the future.</br></br>Please consider keeping at least one user, e.g. add someone else before you remove yourself.</span></strong><p><strong>Do you wish to continue anyway ?</strong>", new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {

								UiElements.showDeleteConfirm(list, "Following users will be removed from service identity and they will lose all access to it. Only users associated with service identity can add other users again. If you remove all users connected to the service identity, you won't be able to use it in future!", new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										for (int i = 0; i < list.size(); i++) {
											// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
											RemoveSpecificUserOwner req;
											if (i == list.size() - 1) {
												req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton, refreshEvents));
											} else {
												req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton));
											}
											req.removeServiceUser(list.get(i), user);

											// TODO - consider fixing authz in session ?

										}
									}
								});

							}
						});

					} else {

						boolean containsMyself = false;
						for (User user : list) {
							if (user.getId() == session.getUser().getId()) {
								containsMyself = true;
							}
						}

						if (containsMyself) {

							// if not selected myself, continue same way
							UiElements.showDeleteConfirm(list, "<p style=\"color:red;\"><b>You are about to remove yourself from service identity! If you do that, you won't be able to add yourself again. Only users associated with service identity can add other users again.</b></p>" +
									"<p>Following users will be removed from service identity and they will lose any access to it.</p>", new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									for (int i = 0; i < list.size(); i++) {
										// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
										RemoveSpecificUserOwner req;
										if (i == list.size() - 1) {
											req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton, refreshEvents));
										} else {
											req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton));
										}
										req.removeServiceUser(list.get(i), user);

										// TODO - consider fixing authz in session ?

									}
								}
							});

						} else {

							// if not selected myself, continue same way
							UiElements.showDeleteConfirm(list, "Following users will be removed from service identity and they will lose any access to it. Only users associated with service identity can add other users again.", new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									for (int i = 0; i < list.size(); i++) {
										// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
										RemoveSpecificUserOwner req;
										if (i == list.size() - 1) {
											req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton, refreshEvents));
										} else {
											req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton));
										}
										req.removeServiceUser(list.get(i), user);

										// TODO - consider fixing authz in session ?

									}
								}
							});

						}

					}

				}
			});

			menu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
			menu.addWidget(new HTML("<strong>Click on yourself to view your profile (switch context).</strong>"));

			// table
			CellTable<User> table = request.getTable(new FieldUpdater<User, String>() {
				public void update(int i, User user, String s) {
					if (session.isSelf(user.getId())) {
						session.getTabManager().addTab(new SelfDetailTabItem(user));
					} else {
						UiElements.generateAlert("You are not authorized", "You are not authorized to view personal details of user "+ SafeHtmlUtils.fromString(user.getFullNameWithTitles()).asString()+".");
					}
				}
			});

			removeUserButton.setEnabled(false);
			JsonUtils.addTableManagedButton(request, table, removeUserButton);

			table.addStyleName("perun-table");
			table.setWidth("100%");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			vp.add(sp);

			session.getUiElements().resizePerunTable(sp, 350, this);

		} else {

			// PERSON TYPE user
			this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim())+": Service identities");

			// request
			final GetSpecificUsersByUser request = new GetSpecificUsersByUser(userId);
			final JsonCallbackEvents refreshEvents = JsonCallbackEvents.refreshTableEvents(request);
			request.setHideSponsored(true);

			// menu
			TabMenu menu = new TabMenu();
			vp.add(menu);
			vp.setCellHeight(menu, "30px");
			menu.addWidget(UiElements.getRefreshButton(this));

			final CustomButton removeUserButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove service identity from "+user.getFullName());
			menu.addWidget(removeUserButton);
			removeUserButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					final ArrayList<User> list = request.getTableSelectedList();
					UiElements.showDeleteConfirm(list, "Following service identities will be removed from you and you will lose any access to them. Only users associated with service identity can add you again.", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
							for (int i=0; i<list.size(); i++ ) {
								RemoveSpecificUserOwner req;
								// if last, refresh
								if(i == list.size() - 1) {
									req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton, refreshEvents));
								} else {
									req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton));
								}
								req.removeServiceUser(user, list.get(i));

								// TODO - consider fixing authz in session ?
							}
						}
					});
				}
			});

			menu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
			menu.addWidget(new HTML("<strong>Click on service identity to view it's profile (switch context).</strong>"));

			// table
			CellTable<User> table = request.getTable(new FieldUpdater<User, String>() {
				public void update(int i, User user, String s) {
					session.getTabManager().addTab(new SelfDetailTabItem(user));
				}
			});

			removeUserButton.setEnabled(false);
			JsonUtils.addTableManagedButton(request, table, removeUserButton);

			table.addStyleName("perun-table");
			table.setWidth("100%");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			vp.add(sp);

			session.getUiElements().resizePerunTable(sp, 350, this);

		}

		contentWidget.setWidget(vp);

		return getWidget();
	}

	@Override
	public Widget getWidget() {
		return contentWidget;
	}

	@Override
	public Widget getTitle() {
		return titleWidget;
	}

	@Override
	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.userRedIcon();
	}

	@Override
	public boolean multipleInstancesEnabled() {
		return false;
	}

	@Override
	public void open() {
		session.setActiveUser(user);
		session.getUiElements().getMenu().openMenu(MainMenu.USER);
		if (!user.isServiceUser()) {
			session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()), UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId, "Service identities", getUrlWithParameters());
		} else {
			session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()), UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId, "Associated users", getUrlWithParameters());
		}
	}

	@Override
	public boolean isAuthorized() {
		if (session.isSelf(userId)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isPrepared() {
		return (user != null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	@Override
	public int hashCode() {
		final int prime = 1259;
		int result = 432;
		result = prime * result;
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
		if (this.userId != ((SelfServiceUsersTabItem)obj).userId)
			return false;

		return true;
	}

	public final static String URL = "service-identities";

	@Override
	public String getUrl() {
		return URL;
	}

	@Override
	public String getUrlWithParameters() {
		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id="+userId;
	}

	static public SelfServiceUsersTabItem load(Map<String, String> parameters) {
		int uid = Integer.parseInt(parameters.get("id"));
		return new SelfServiceUsersTabItem(uid);
	}

}
