package cz.metacentrum.perun.webgui.client.passwordresetresources;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetLogins;
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

        if (Location.getParameter("login-namespace") != null && !Location.getParameter("login-namespace").isEmpty()) {
            namespace = Location.getParameter("login-namespace");
        } else {
            namespace = "";
        }

        final ListBox namespaces = new ListBox();
        final Label headerLabel = new Label();
        final Label loginLabel = new Label();

        final ExtendedPasswordBox passBox = new ExtendedPasswordBox();
        final ExtendedPasswordBox secondPassBox = new ExtendedPasswordBox();

        final ExtendedTextBox.TextBoxValidator validator;
        final ExtendedTextBox.TextBoxValidator validator2;

        final CustomButton resetPass = new CustomButton("Reset password", "Reset password in selected namespace", SmallIcons.INSTANCE.keyIcon());

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

        ft.setHTML(1, 0, "Namespace:");
        ft.getFlexCellFormatter().setStyleName(1, 0, "itemName");
        ft.setWidget(1, 1, namespaces);

        ft.setHTML(2, 0, "Login:");
        ft.getFlexCellFormatter().setStyleName(2, 0, "itemName");
        ft.setWidget(2, 1, loginLabel);

        ft.setHTML(3, 0, "New password:");
        ft.getFlexCellFormatter().setStyleName(3, 0, "itemName");
        ft.setWidget(3, 1, passBox);

        ft.setHTML(4, 0, "Retype new password:");
        ft.getFlexCellFormatter().setStyleName(4, 0, "itemName");
        ft.setWidget(4, 1, secondPassBox);

        GetLogins loginsCall = new GetLogins(session.getUser().getId(), new JsonCallbackEvents(){
            @Override
            public void onLoadingStart() {
                namespaces.clear();
                namespaces.addItem("Loading...");
            }
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
                namespaces.clear();
                logins = JsonUtils.jsoAsList(jso);
                if (logins != null && !logins.isEmpty()) {
                    namespaces.addItem("Not selected");
                    for (String name : Utils.getSupportedPasswordNamespaces()) {
                        boolean found = false;
                        for (Attribute a : logins) {
                            if (a.getFriendlyNameParameter().equalsIgnoreCase(name)) {
                                found = true;
                            }
                        }
                        if (found) {
                            namespaces.addItem(name.toUpperCase(), name);
                        }
                    }
                    if (namespaces.getItemCount() <= 1) {

                        bodyContents.clear();
                        FlexTable ft = new FlexTable();
                        ft.setSize("100%", "300px");
                        ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>You don't have login in any of namespaces supported for password reset !</h2>");
                        ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
                        ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
                        bodyContents.setWidget(ft);

                    }

                    if (namespace != null && !namespace.equals("")) {
                        // select namespace from URL
                        for (int i=0; i<namespaces.getItemCount(); i++) {
                            if (namespaces.getValue(i).equals(namespace)) {
                                namespaces.setSelectedIndex(i);
                                for (Attribute a : logins) {
                                    if (a.getFriendlyNameParameter().equals(namespace)) {
                                        loginLabel.setText(a.getValue());
                                    }
                                }
                                break;
                            }
                        }
                    }

                }
            }
        });
        if (logins == null) {
            loginsCall.retrieveData();
        } else {
            if (logins != null && !logins.isEmpty()) {
                namespaces.addItem("Not selected");
                for (String name : Utils.getSupportedPasswordNamespaces()) {
                    boolean found = false;
                    for (Attribute a : logins) {
                        if (a.getFriendlyNameParameter().equalsIgnoreCase(name)) {
                            found = true;
                        }
                    }
                    if (found) {
                        namespaces.addItem(name.toUpperCase(), name);
                    }
                }
            }
        }

        namespaces.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (namespaces.getSelectedIndex() > 0) {
                    namespace = namespaces.getValue(namespaces.getSelectedIndex());
                    for (Attribute a : logins) {
                        if (a.getFriendlyNameParameter().equals(namespace)) {
                            loginLabel.setText(a.getValue());
                        }
                    }
                } else {
                    namespace = "";
                    loginLabel.setText("");
                }
            }
        });

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

                if (validator.validateTextBox() && validator2.validateTextBox() && namespaces.getSelectedIndex()>0) {
                    changepw.changePassword(session.getUser(), namespace, "", passBox.getTextBox().getText().trim());
                }

            }
        });

        FlexTable header = new FlexTable();
        header.setWidget(0, 0, new Image(LargeIcons.INSTANCE.keyIcon()));
        header.setWidget(0, 1, headerLabel);

        headerLabel.setText("Password reset");
        headerLabel.setStyleName("now-managing");

        vp.add(header);

        vp.add(ft);

        TabMenu menu = new TabMenu();
        menu.addWidget(resetPass);

        vp.add(menu);
        vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

        return bodyContents;

    }

}