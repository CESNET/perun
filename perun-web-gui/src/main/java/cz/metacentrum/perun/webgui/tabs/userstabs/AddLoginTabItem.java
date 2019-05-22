package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetLogins;
import cz.metacentrum.perun.webgui.json.usersManager.IsLoginAvailable;
import cz.metacentrum.perun.webgui.json.usersManager.SetLogin;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Tab for setting new login for user in selected namespace
 *
 * USE ONLY AS INNER TAB !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddLoginTabItem implements TabItem {

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Loading user");

	private int userId;
	private User user;
	private ArrayList<Attribute> usersLogins; // must be null by default

	/**
	 * Creates a tab instance
	 *
	 * @param list list of "login" type attributes which user already has
	 */
	public AddLoginTabItem(ArrayList<Attribute> list){
		this.user = session.getActiveUser();
		this.userId = session.getActiveUser().getId();
		if (list != null) {
			this.usersLogins = list;
		} else {
			GetLogins request = new GetLogins(userId, new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					usersLogins = JsonUtils.jsoAsList(jso);
				}
			});
			request.retrieveData();
		}

	}

	/**
	 * Creates a tab instance
	 *
	 * @param list list of "login" type attributes which user already has
	 */
	public AddLoginTabItem(User user, ArrayList<Attribute> list){
		this.user = user;
		this.userId = user.getId();
		if (list != null) {
			this.usersLogins = list;
		} else {
			GetLogins request = new GetLogins(userId, new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					usersLogins = JsonUtils.jsoAsList(jso);
				}
			});
			request.retrieveData();
		}

	}

	public boolean isPrepared(){
		return (user != null && usersLogins != null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget.setText("Add login");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		final ExtendedTextBox userLogin = new ExtendedTextBox();
		final ListBox namespace = new ListBox();
		final CustomButton createLogin = TabMenu.getPredefinedButton(ButtonType.ADD, "Add login in selected namespace");
		final Label notice = new Label("Your login will be automatically generated.");
		notice.setVisible(false);

		// offer only available namespaces.
		ArrayList<String> logins = new ArrayList<String>();
		for (Attribute a : usersLogins) {
			logins.add(a.getFriendlyNameParameter());
		}
		for (String s : Utils.getSupportedPasswordNamespaces()) {
			if (!logins.contains(s)) {
				namespace.addItem(s.toUpperCase(), s);
			}
		}

		final ExtendedTextBox.TextBoxValidator loginValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (userLogin.getTextBox().getValue().trim().isEmpty()) {
					userLogin.setError("Login can't be empty!");
					return false;
				}
				RegExp regExp = RegExp.compile(Utils.LOGIN_VALUE_MATCHER);
				boolean match = regExp.test(userLogin.getTextBox().getValue().trim());
				if (!match) {
					userLogin.setError("Invalid format!");
					return false;
				}
				if (userLogin.isProcessing() || userLogin.isHardError()) {
					return false;
				}
				userLogin.setOk();
				return true;
			}
		};
		userLogin.setValidator(loginValidator);

		final FlexTable layout = new FlexTable();
		layout.addStyleName("inputFormFlexTable");

		layout.setHTML(0, 0, "Namespace:");
		layout.setHTML(1, 0, "Login:");

		for (int i=0; i<layout.getRowCount(); i++) {
			layout.getFlexCellFormatter().addStyleName(i, 0, "itemName");
		}

		layout.setWidget(0, 1, namespace);
		layout.setWidget(1, 1, userLogin);

		layout.getFlexCellFormatter().setColSpan(2, 0, 2);
		layout.setWidget(2, 0, notice);
		layout.getFlexCellFormatter().addStyleName(2, 0, "inputFormInlineComment");

		TabMenu menu = new TabMenu();
		menu.addWidget(createLogin);

		final TabItem tab = this;
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		// user can't add new login
		if (namespace.getItemCount() == 0) {
			vp.add(new HTML("<p><strong>You already have login in all supported namespaces!</strong></p>"));
			createLogin.setEnabled(false);
		} else {
			// user can add new login
			vp.add(layout);

			// check login availability
			userLogin.getTextBox().addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent keyUpEvent) {
					if (keyUpEvent.isDownArrow() || keyUpEvent.isUpArrow() || keyUpEvent.isLeftArrow() || keyUpEvent.isRightArrow()) {
						// do not trigger when no text input
						return;
					}
					final String value = userLogin.getTextBox().getValue().trim();
					// trigger new validation on checked input or if previously was hard error
					if ((!value.isEmpty() && RegExp.compile(Utils.LOGIN_VALUE_MATCHER).test(value)) || userLogin.isHardError()) {
						new IsLoginAvailable(namespace.getValue(namespace.getSelectedIndex()), userLogin.getTextBox().getValue().trim(), new JsonCallbackEvents(){
							@Override
							public void onFinished(JavaScriptObject jso) {
								if (value.equals(userLogin.getTextBox().getValue().trim())) {
									BasicOverlayType bo = jso.cast();
									userLogin.setProcessing(false);
									if (!bo.getBoolean()) {
										userLogin.setHardError("Login is already in use!");
									} else {
										userLogin.removeHardError();
										loginValidator.validateTextBox();
									}
								}
							}
						@Override
						public void onLoadingStart(){
							if (value.equals(userLogin.getTextBox().getValue().trim())) {
								userLogin.removeHardError();
								userLogin.setProcessing(true);
							}
						}
						@Override
						public void onError(PerunError error) {
							if (value.equals(userLogin.getTextBox().getValue().trim())) {
								userLogin.setProcessing(false);
								userLogin.setHardError("Unable to check if login is available!");
							}
						}
						}).retrieveData();
					}
				}
			});

			namespace.addChangeHandler(new ChangeHandler() {
				public void onChange(ChangeEvent changeEvent) {

					if (namespace.getSelectedValue().equals("mu")) {

						userLogin.getTextBox().setValue("");
						userLogin.removeHardError();
						userLogin.setOk();
						userLogin.getTextBox().setEnabled(false);
						notice.setVisible(true);

					} else {

						userLogin.getTextBox().setEnabled(true);
						notice.setVisible(false);

						final String value = userLogin.getTextBox().getValue().trim();
						// trigger new validation on checked input or if previously was hard error
						if ((!value.isEmpty() && RegExp.compile(Utils.LOGIN_VALUE_MATCHER).test(value)) || userLogin.isHardError()) {
							new IsLoginAvailable(namespace.getValue(namespace.getSelectedIndex()), userLogin.getTextBox().getValue().trim(), new JsonCallbackEvents() {
								@Override
								public void onFinished(JavaScriptObject jso) {
									if (value.equals(userLogin.getTextBox().getValue().trim())) {
										BasicOverlayType bo = jso.cast();
										userLogin.setProcessing(false);
										if (!bo.getBoolean()) {
											userLogin.setError("Login is already in use!");
										} else {
											userLogin.removeHardError();
											loginValidator.validateTextBox();
										}
									}
								}
								@Override
								public void onLoadingStart() {
									if (value.equals(userLogin.getTextBox().getValue().trim())) {
										userLogin.removeHardError();
										userLogin.setProcessing(true);
									}
								}
								@Override
								public void onError(PerunError error) {
									if (value.equals(userLogin.getTextBox().getValue().trim())) {
										userLogin.setProcessing(false);
										userLogin.setHardError("Error while loading.");
									}
								}
							}).retrieveData();
						}

					}
				}
			});

			createLogin.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {

					if (namespace.getSelectedValue().equals("mu")) {

						session.getTabManager().addTabToCurrentTab(new SelfPasswordTabItem(user, namespace.getValue(namespace.getSelectedIndex()), userLogin.getTextBox().getValue().trim(), SelfPasswordTabItem.Actions.CREATE));

					} else {

						if (!loginValidator.validateTextBox()) return;

						SetLogin request = new SetLogin(JsonCallbackEvents.disableButtonEvents(createLogin, new JsonCallbackEvents() {
							@Override
							public void onFinished(JavaScriptObject jso) {
								session.getTabManager().addTabToCurrentTab(new SelfPasswordTabItem(user, namespace.getValue(namespace.getSelectedIndex()), userLogin.getTextBox().getValue().trim(), SelfPasswordTabItem.Actions.CREATE));
							}
						}));
						request.setLogin(user, namespace.getValue(namespace.getSelectedIndex()), userLogin.getTextBox().getValue().trim());
					}

				}
			});

		}

		if (namespace.getSelectedValue().equals("mu")) {

			userLogin.getTextBox().setValue("");
			userLogin.removeHardError();
			userLogin.setOk();
			userLogin.getTextBox().setEnabled(false);
			notice.setVisible(true);

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
		final int prime = 1171;
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
		if (this.userId != ((AddLoginTabItem)obj).userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {}

	public boolean isAuthorized() {

		if (session.isSelf(userId)) {
			return true;
		} else {
			return false;
		}

	}

}
