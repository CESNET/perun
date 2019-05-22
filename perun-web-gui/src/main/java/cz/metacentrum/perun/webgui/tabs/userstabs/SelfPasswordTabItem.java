package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
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
import cz.metacentrum.perun.webgui.json.usersManager.ChangePassword;
import cz.metacentrum.perun.webgui.json.usersManager.CreatePassword;
import cz.metacentrum.perun.webgui.json.usersManager.GenerateAccount;
import cz.metacentrum.perun.webgui.json.usersManager.SetLogin;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedPasswordBox;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.HashMap;
import java.util.Map;

/**
 * Tab with user's password settings
 *
 * USE ONLY AS INNER TAB !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SelfPasswordTabItem implements TabItem, TabItemWithUrl{

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	/**
	 * Password actions - CREATE,DELETE,CHANGE
	 */
	public enum Actions {
		CREATE,
		DELETE,
		CHANGE
	};

	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("Loading user");

	private User user;
	private int userId;
	private String namespace = "Not selected";
	private String login = "";
	private Actions action = Actions.CHANGE;

	/**
	 * Creates a tab instance
	 *
	 * @param namespace namespace for login
	 * @param login login
	 * @param action action with password
	 */
	public SelfPasswordTabItem(String namespace, String login, Actions action){
		this.user = session.getActiveUser();
		this.userId = session.getActiveUser().getId();
		this.namespace = namespace;
		this.login = login;
		this.action = action;
	}

	/**
	 * Creates a tab instance
	 *
	 * @param user user to set password for
	 * @param namespace namespace for login
	 * @param login login
	 * @param action action with password
	 */
	public SelfPasswordTabItem(User user, String namespace, String login, Actions action){
		this.user = user;
		this.userId = user.getId();
		this.namespace = namespace;
		this.login = login;
		this.action = action;
	}

	/**
	 * Creates a tab instance
	 *
	 * @param userId ID of user to set password for
	 * @param namespace namespace for login
	 * @param login login
	 * @param action action with password
	 */
	public SelfPasswordTabItem(int userId, String namespace, String login, Actions action){
		this.userId = userId;
		this.namespace = namespace;
		this.login = login;
		this.action = action;
		new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				user = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return (user != null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		String actionText = "Change";
		if (action.equals(Actions.CREATE)) {
			actionText = "Create";
		} else if (action.equals(Actions.DELETE)) {
			actionText = "Delete";
		}

		// set tab name
		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()) + ": " + actionText + " password");

		// main panel
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();

		// widgets
		final Label namespaceLabel = new Label();
		namespaceLabel.setText(namespace);
		final Label loginLabel = new Label();
		loginLabel.setText(login);
		final ExtendedPasswordBox newPass = new ExtendedPasswordBox();
		final ExtendedPasswordBox confPass = new ExtendedPasswordBox();
		final ExtendedPasswordBox oldPass = new ExtendedPasswordBox();

		final CustomButton changeButton = new CustomButton("Change password", "Changes your password in selected namespace", SmallIcons.INSTANCE.keyIcon());
		final CustomButton createButton = new CustomButton("Create password", "Creates your password in selected namespace", SmallIcons.INSTANCE.keyAddIcon());
		final CustomButton deleteButton = new CustomButton("Delete password", "Deletes your password in selected namespace", SmallIcons.INSTANCE.keyDeleteIcon());
		final TabItem tab = this;

		final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {

				if (newPass.getTextBox().getValue().trim().equals("")) {
					newPass.setError("Password can't be empty!");
					return false;
				} else if (!newPass.getTextBox().getValue().equals(confPass.getTextBox().getValue())) {
					newPass.setError("Password in both textboxes must be the same!");
					return false;
				} else {
					newPass.setOk();
					confPass.setOk();
					return true;
				}
			}
		};

		final ExtendedTextBox.TextBoxValidator validator2 = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {

				if (confPass.getTextBox().getValue().trim().equals("")) {
					confPass.setError("Password can't be empty!");
					return false;
				} else if (!confPass.getTextBox().getValue().equals(newPass.getTextBox().getValue())) {
					confPass.setError("Password in both textboxes must be the same!");
					return false;
				} else {
					confPass.setOk();
					newPass.setOk();
					return true;
				}
			}
		};

		final ExtendedTextBox.TextBoxValidator oldValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {

				if (oldPass.getTextBox().getValue().trim().equals("")) {
					oldPass.setError("Password can't be empty!");
					return false;
				} else {
					oldPass.setOk();
					return true;
				}
			}
		};

		newPass.setValidator(validator);
		confPass.setValidator(validator2);
		oldPass.setValidator(oldValidator);

		// save changes
		changeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				if ("mu".equals(namespace) && !JsonUtils.checkParseInt(login)) {
					UiElements.generateAlert("Operation not supported",
							"Password change/reset is not supported for non-numeric logins (UČO).");
				}

				if (session.isPerunAdmin() || user.isServiceUser()) {
					if (!validator.validateTextBox() && !validator2.validateTextBox()) return;
					ChangePassword changepw = new ChangePassword(JsonCallbackEvents.closeTabDisableButtonEvents(changeButton, tab), false);
					changepw.changePassword(user, namespace, oldPass.getTextBox().getValue(), newPass.getTextBox().getValue());
				} else {
					if (!validator.validateTextBox() && !validator2.validateTextBox() && !oldValidator.validateTextBox()) return;
					ChangePassword changepw = new ChangePassword(JsonCallbackEvents.closeTabDisableButtonEvents(changeButton, tab), true);
					changepw.changePassword(user, namespace, oldPass.getTextBox().getValue(), newPass.getTextBox().getValue());
				}

			}
		});

		if (user.isServiceUser()) {
			// for service users it's reset since they don't provide old password
			changeButton.setText("Reset password…");
		}

		createButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent clickEvent) {

				if (validator.validateTextBox() && validator2.validateTextBox()) {

					if ("mu".equals(namespace)) {

						final GenerateAccount generateAccount = new GenerateAccount(JsonCallbackEvents.disableButtonEvents(createButton, new JsonCallbackEvents() {

							@Override
							public void onFinished(JavaScriptObject jso) {

								BasicOverlayType basic = jso.cast();
								final String login = basic.getCustomProperty("urn:perun:user:attribute-def:def:login-namespace:mu");

								SetLogin setLogin = new SetLogin(JsonCallbackEvents.disableButtonEvents(createButton, new JsonCallbackEvents() {
									@Override
									public void onFinished(JavaScriptObject jso) {

										UiElements.generateInfo("Assigned login", "You were assigned with login <b>" + login + "</b> in namespace MU.");

										// VALIDATE PASSWORD - SET EXT SOURCES
										CreatePassword req = new CreatePassword(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab));
										req.validateAndSetUserExtSources(user.getId(), login, namespace);

									}
								}));
								setLogin.setLogin(user.getId(), "mu", login);

							}

						}));

						final Map<String, String> params = new HashMap<String, String>();
						GetEntityById get = new GetEntityById(PerunEntity.RICH_USER_WITH_ATTRS, user.getId(), JsonCallbackEvents.disableButtonEvents(createButton, new JsonCallbackEvents() {
							@Override
							public void onFinished(JavaScriptObject jso) {
								User usr = jso.cast();

								params.put("urn:perun:user:attribute-def:core:firstName", usr.getFirstName());
								params.put("urn:perun:user:attribute-def:core:lastName", usr.getLastName());
								params.put("urn:perun:member:attribute-def:def:mail", usr.getAttribute("urn:perun:user:attribute-def:def:preferredMail").getValue());
								generateAccount.generateAccount(namespace, newPass.getTextBox().getValue(), params);

							}
						}));
						get.retrieveData();

					} else {

						// NORMAL PWD LOGIC
						CreatePassword create = new CreatePassword(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab));
						create.createPassword(userId, login, namespace, newPass.getTextBox().getValue());

					}
				}
			}
		});

		deleteButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent clickEvent) {
				UiElements.generateAlert("Not yet implemented", "Not yet implemented");
			}
		});

		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");

		// change layout
		if (action.equals(Actions.CHANGE)) {

			int row = 0;

			layout.setHTML(row, 0, "Namespace:");
			layout.setWidget(row, 1, namespaceLabel);
			row++;
			layout.setHTML(row, 0, "Login:");
			layout.setWidget(row, 1, loginLabel);
			row++;

			// perun admin doesn't need to know old password
			// if service user, we can always change password without knowing old
			// mu namespace can change without knowing old
			if (!session.isPerunAdmin()) {
				if (!user.isServiceUser()) {
					layout.setHTML(row, 0, "Old password: ");
					layout.setWidget(row, 1, oldPass);
					row++;
				}
			}
			layout.setHTML(row, 0, "New password:");
			layout.setWidget(row, 1, newPass);
			row++;
			layout.setHTML(row, 0, "Retype new pass:");
			layout.setWidget(row, 1, confPass);

			menu.addWidget(changeButton);
			vp.add(layout);

		} else if (action.equals(Actions.CREATE)) {

			layout.setHTML(0, 0, "Namespace:");
			layout.setWidget(0, 1, namespaceLabel);

			if ("mu".equals(namespace)) {
				loginLabel.setText("Will be generated...");
				loginLabel.addStyleName("inputFormInlineComment");
			}

			layout.setHTML(1, 0, "Login:");
			layout.setWidget(1, 1, loginLabel);
			layout.setHTML(2, 0, "New password:");
			layout.setWidget(2, 1, newPass);
			layout.setHTML(3, 0, "Retype new pass:");
			layout.setWidget(3, 1, confPass);

			final CustomButton skip = new CustomButton("Skip", "Will set random/empty password", SmallIcons.INSTANCE.arrowRightIcon());
			skip.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {

					if ("mu".equals(namespace)) {

						final GenerateAccount generateAccount = new GenerateAccount(JsonCallbackEvents.disableButtonEvents(createButton, new JsonCallbackEvents() {

							@Override
							public void onFinished(JavaScriptObject jso) {

								BasicOverlayType basic = jso.cast();
								final String login = basic.getCustomProperty("urn:perun:user:attribute-def:def:login-namespace:mu");

								SetLogin setLogin = new SetLogin(JsonCallbackEvents.disableButtonEvents(createButton, new JsonCallbackEvents() {
									@Override
									public void onFinished(JavaScriptObject jso) {

										UiElements.generateInfo("Assigned login", "You were assigned with login <b>" + login + "</b> in namespace MU.");

										// VALIDATE PASSWORD - SET EXT SOURCES
										CreatePassword req = new CreatePassword(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab));
										req.validateAndSetUserExtSources(user.getId(), login, namespace);

									}
								}));
								setLogin.setLogin(user.getId(), "mu", login);

							}

						}));

						final Map<String, String> params = new HashMap<String, String>();
						GetEntityById get = new GetEntityById(PerunEntity.RICH_USER_WITH_ATTRS, user.getId(), JsonCallbackEvents.disableButtonEvents(createButton, new JsonCallbackEvents() {
							@Override
							public void onFinished(JavaScriptObject jso) {
								User usr = jso.cast();

								params.put("urn:perun:user:attribute-def:core:firstName", usr.getFirstName());
								params.put("urn:perun:user:attribute-def:core:lastName", usr.getLastName());
								params.put("urn:perun:member:attribute-def:def:mail", usr.getAttribute("urn:perun:user:attribute-def:def:preferredMail").getValue());
								generateAccount.generateAccount(namespace, newPass.getTextBox().getValue(), params);

							}
						}));
						get.retrieveData();

					} else {

						CreatePassword create = new CreatePassword(JsonCallbackEvents.closeTabDisableButtonEvents(skip, tab));
						create.createRandomPassword(userId, login, namespace);

					}
				}
			});
			menu.addWidget(skip);

			menu.addWidget(createButton);
			vp.add(layout);

		} else if (action.equals(Actions.DELETE)) {

			layout.setHTML(0, 0, "Namespace:");
			layout.setWidget(0, 1, namespaceLabel);
			layout.setHTML(1, 0, "Login:");
			layout.setWidget(1, 1, loginLabel);

			menu.addWidget(deleteButton);
			vp.add(layout);

		}

		for (int i=0; i<layout.getRowCount(); i++) {
			layout.getFlexCellFormatter().setStyleName(i, 0, "itemName");
		}

		int row = layout.getRowCount();
		layout.setHTML(row, 0, "Please <b>avoid using accented characters</b>. It might not be supported by all backend components and services.");
		layout.getFlexCellFormatter().setColSpan(row, 0, 2);
		layout.getCellFormatter().setStyleName(row, 0,"inputFormInlineComment");
		layout.setWidth("400px");

		if (!action.equals(Actions.CREATE)) {
			menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					session.getTabManager().closeTab(tab, isRefreshParentOnClose());
				}
			}));
		}

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
		return SmallIcons.INSTANCE.keyIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 1237;
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
		if (this.userId != ((SelfPasswordTabItem)obj).userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.USER);
	}

	public boolean isAuthorized() {

		if (session.isSelf(userId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "pass";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		// if same user as in session
		if(userId == session.getUser().getId()){
			return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?ns=" + namespace + "&l=" + login + "&a=" + action;
		}

		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId + "&ns=" + namespace + "&l=" + login+ "&a=" + action;
	}


	static public SelfPasswordTabItem load(Map<String, String> parameters)
	{
		int uid;
		if(parameters.containsKey("id"))
		{
			uid = Integer.parseInt(parameters.get("id"));
		}else{
			uid = PerunWebSession.getInstance().getUser().getId();
		}
		String namespace = String.valueOf(parameters.get("ns"));
		String login = String.valueOf(parameters.get("l"));
		String action = String.valueOf(parameters.get("a"));
		Actions act = Actions.valueOf(action);

		return new SelfPasswordTabItem(uid, namespace, login, act);
	}

}
