package cz.metacentrum.perun.webgui.client.passwordresetresources;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetLogins;
import cz.metacentrum.perun.webgui.json.usersManager.ChangeNonAuthzPassword;
import cz.metacentrum.perun.webgui.json.usersManager.ChangePassword;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;

public class PasswordResetFormPage {

	/**
	 * Main body contents
	 */
	private SimplePanel bodyContents = new SimplePanel();
	private PerunWebSession session = PerunWebSession.getInstance();
	private ArrayList<Attribute> logins;
	private String namespace = "";

	public PasswordResetFormPage() {}

	/**
	 * Returns page content
	 *
	 * @return page content
	 */
	public Widget getContent() {

		bodyContents.clear();

		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("50%", "50%");
		vp.getElement().setAttribute("style", "margin: auto; position: relative; top: 50px;");

		bodyContents.setWidget(vp);

		// if using backup pwd-reset option, draw different content
		if (Location.getParameterMap().keySet().contains("m") &&
				Location.getParameterMap().keySet().contains("i") &&
				session.getRpcUrl().startsWith("/non/rpc")) {

			return drawNonAuthzPasswordReset(vp);

		}

		if (Location.getParameter("login-namespace") != null && !Location.getParameter("login-namespace").isEmpty()) {
			namespace = Location.getParameter("login-namespace");
		} else {
			namespace = "";
		}

		final Label headerLabel = new Label();

		final ExtendedPasswordBox passBox = new ExtendedPasswordBox();
		final ExtendedPasswordBox secondPassBox = new ExtendedPasswordBox();

		final ExtendedTextBox.TextBoxValidator validator;
		final ExtendedTextBox.TextBoxValidator validator2;

		final CustomButton resetPass = new CustomButton("Reset password", "Reset password in namespace: "+namespace, SmallIcons.INSTANCE.keyIcon());

		validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (passBox.getTextBox().getValue().trim().isEmpty()) {
					passBox.setError("Password can't be empty !");
					return false;
				} else if (!passBox.getTextBox().getValue().trim().equals(secondPassBox.getTextBox().getValue().trim())) {
					passBox.setError("Password in both textboxes must be the same !");
					return false;
				} else {
					passBox.setOk();
					secondPassBox.setOk();
					return true;
				}
			}
		};

		validator2 = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (secondPassBox.getTextBox().getValue().trim().isEmpty()) {
					secondPassBox.setError("Password can't be empty !");
					return false;
				} else if (!secondPassBox.getTextBox().getValue().trim().equals(passBox.getTextBox().getValue().trim())) {
					secondPassBox.setError("Password in both textboxes must be the same !");
					return false;
				} else {
					secondPassBox.setOk();
					passBox.setOk();
					return true;
				}
			}
		};

		passBox.setValidator(validator);
		secondPassBox.setValidator(validator2);

		FlexTable ft = new FlexTable();
		ft.setSize("300px", "100px");
		ft.addStyleName("inputFormFlexTable");
		ft.getElement().setAttribute("style", "margin: auto;");

		ft.setHTML(1, 0, "New password:");
		ft.getFlexCellFormatter().setStyleName(1, 0, "itemName");
		ft.setWidget(1, 1, passBox);

		ft.setHTML(2, 0, "Retype new password:");
		ft.getFlexCellFormatter().setStyleName(2, 0, "itemName");
		ft.setWidget(2, 1, secondPassBox);

		final FlexTable header = new FlexTable();
		header.setWidget(0, 0, new AjaxLoaderImage());
		header.setWidget(0, 1, headerLabel);

		GetLogins loginsCall = new GetLogins(session.getUser().getId(), new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {

				bodyContents.clear();
				FlexTable ft = new FlexTable();
				ft.setSize("100%", "300px");
				ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>Error occurred when getting your login from Perun. Please, reload page to retry.</h2><p>"+error.getErrorId()+": "+error.getErrorInfo()+"</p>");
				ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
				ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
				bodyContents.setWidget(ft);

			}
		@Override
		public void onFinished(JavaScriptObject jso) {
			header.setWidget(0, 0, new Image(LargeIcons.INSTANCE.keyIcon()));
			logins = JsonUtils.jsoAsList(jso);
			if (logins != null && !logins.isEmpty()) {
				for (Attribute a : logins) {
					// if have login in namespace
					if (a.getFriendlyNameParameter().equals(namespace)) {
						boolean found = false;
						for (String name : Utils.getSupportedPasswordNamespaces()) {
							if (a.getFriendlyNameParameter().equalsIgnoreCase(name)) {
								found = true;
							}
						}
						if (found) {
							// HAVE LOGIN AND SUPPORTED
							headerLabel.setText("Password reset for "+a.getValue()+"@"+namespace);
							return;
						} else {
							// NOT SUPPORTED
							bodyContents.clear();
							FlexTable ft = new FlexTable();
							ft.setSize("100%", "300px");
							ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>Password reset in selected namespace is not supported !</h2>");
							ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
							ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
							bodyContents.setWidget(ft);
							return;
						}
					}
				}
			}
			// DO NOT HAVE LOGIN IN NAMESPACE
			bodyContents.clear();
			FlexTable ft = new FlexTable();
			ft.setSize("100%", "300px");

			if (namespace != null && !namespace.isEmpty()) {
				ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>You don't have login in selected namespace !</h2>");
			} else {
				ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>You must specify login-namespace in URL !</h2>");
			}
			ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
			ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
			bodyContents.setWidget(ft);

		}
		});

		loginsCall.retrieveData();

		resetPass.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				ChangePassword changepw = new ChangePassword(JsonCallbackEvents.disableButtonEvents(resetPass, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						bodyContents.clear();
						FlexTable ft = new FlexTable();
						ft.setSize("100%", "300px");
						ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.acceptIcon())+"<h2>Password successfully changed!</h2>");
						ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
						ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
						ft.setHTML(1, 0, "<h2>New password will work on all resources in approx. 15 minutes after reset.</h2>");
						ft.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
						ft.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_MIDDLE);
						bodyContents.setWidget(ft);
					}
				}), false);

				if (validator.validateTextBox() && validator2.validateTextBox()) {
					changepw.changePassword(session.getUser(), namespace, "", passBox.getTextBox().getText().trim());
				}

			}
		});

		headerLabel.setText("Password reset for ");
		headerLabel.setStyleName("now-managing");

		vp.add(header);

		vp.add(ft);

		TabMenu menu = new TabMenu();
		menu.addWidget(resetPass);

		vp.add(menu);
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		return bodyContents;

	}

	public Widget drawNonAuthzPasswordReset(VerticalPanel vp) {

		final String i = Location.getParameter("i");
		final String m = Location.getParameter("m");

		final ExtendedPasswordBox passBox = new ExtendedPasswordBox();
		final ExtendedPasswordBox secondPassBox = new ExtendedPasswordBox();

		final ExtendedTextBox.TextBoxValidator validator;
		final ExtendedTextBox.TextBoxValidator validator2;

		final CustomButton resetPass = new CustomButton("Reset password", "Reset password", SmallIcons.INSTANCE.keyIcon());

		validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (passBox.getTextBox().getValue().trim().isEmpty()) {
					passBox.setError("Password can't be empty !");
					return false;
				} else if (!passBox.getTextBox().getValue().trim().equals(secondPassBox.getTextBox().getValue().trim())) {
					passBox.setError("Password in both textboxes must be the same !");
					return false;
				} else {
					passBox.setOk();
					secondPassBox.setOk();
					return true;
				}
			}
		};

		validator2 = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (secondPassBox.getTextBox().getValue().trim().isEmpty()) {
					secondPassBox.setError("Password can't be empty !");
					return false;
				} else if (!secondPassBox.getTextBox().getValue().trim().equals(passBox.getTextBox().getValue().trim())) {
					secondPassBox.setError("Password in both textboxes must be the same !");
					return false;
				} else {
					secondPassBox.setOk();
					passBox.setOk();
					return true;
				}
			}
		};

		passBox.setValidator(validator);
		secondPassBox.setValidator(validator2);

		FlexTable ft = new FlexTable();
		ft.setSize("300px", "100px");
		ft.addStyleName("inputFormFlexTable");
		ft.getElement().setAttribute("style", "margin: auto;");

		ft.getFlexCellFormatter().setColSpan(0, 0, 2);
		ft.setHTML(0, 0, "Password reset");
		ft.getFlexCellFormatter().setStyleName(0, 0, "now-managing");
		ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);

		ft.setHTML(1, 0, "New password:");
		ft.getFlexCellFormatter().setStyleName(1, 0, "itemName");
		ft.setWidget(1, 1, passBox);

		ft.setHTML(2, 0, "Retype new password:");
		ft.getFlexCellFormatter().setStyleName(2, 0, "itemName");
		ft.setWidget(2, 1, secondPassBox);

		resetPass.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				ChangeNonAuthzPassword changepw = new ChangeNonAuthzPassword(JsonCallbackEvents.disableButtonEvents(resetPass, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						bodyContents.clear();
						FlexTable ft = new FlexTable();
						ft.setSize("100%", "300px");
						ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.acceptIcon())+"<h2>Password successfully changed!</h2>");
						ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
						ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
						ft.setHTML(1, 0, "<h2>New password will work on all resources in approx. 15 minutes after reset.</h2>");
						ft.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
						ft.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_MIDDLE);
						bodyContents.setWidget(ft);
					}
				}));

				if (validator.validateTextBox() && validator2.validateTextBox()) {
					changepw.changeNonAuthzPassword(i, m, passBox.getTextBox().getText().trim());
				}

			}
		});

		vp.add(ft);

		TabMenu menu = new TabMenu();
		menu.addWidget(resetPass);

		vp.add(menu);
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		return bodyContents;

	}

}
