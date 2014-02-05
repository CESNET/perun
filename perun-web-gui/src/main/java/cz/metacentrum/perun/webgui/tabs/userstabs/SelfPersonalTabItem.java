package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetListOfAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.usersManager.RequestPreferredEmailChange;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab with user's personal settings (personal info, contacts)
 *
 * @author Pavel Zlamal <256627&mail.muni.cz>
 * @version $Id: $
 */
public class SelfPersonalTabItem implements TabItem {

    PerunWebSession session = PerunWebSession.getInstance();

    private SimplePanel contentWidget = new SimplePanel();
    private Label titleWidget = new Label("Loading user");

    private User user;
    private int userId;
    private ArrayList<Attribute> userAttrs = new ArrayList<Attribute>();

    /**
     * Creates a tab instance
     */
    public SelfPersonalTabItem(){
        this.user = session.getUser();
        this.userId = user.getId();
    }

    /**
     * Creates a tab instance with custom user
     * @param user
     */
    public SelfPersonalTabItem(User user){
        this.user = user;
        this.userId = user.getId();
    }

    /**
     * Creates a tab instance with custom user
     * @param userId
     */
    public SelfPersonalTabItem(int userId) {
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

        // content
        VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%","100%");

        FlexTable layout = new FlexTable();
        vp.add(layout);

        layout.setStyleName("perun-table");
        vp.setStyleName("perun-tableScrollPanel");
        session.getUiElements().resizeSmallTabPanel(vp, 350, this);

        FlexTable quickHeader = new FlexTable();
        quickHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.directionIcon()));
        quickHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Quick links</p>");

        FlexTable prefHeader = new FlexTable();
        prefHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.settingToolsIcon()));
        prefHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Global settings</p>");

        layout.setWidget(0, 0, quickHeader);
        layout.setWidget(0, 1, prefHeader);

        layout.getFlexCellFormatter().setWidth(0, 0, "50%");

        // widgets
        final ExtendedTextBox preferredEmail = new ExtendedTextBox();
        preferredEmail.getTextBox().setWidth("300px");
        preferredEmail.setWidth("300px");

        preferredEmail.setValidator(new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (preferredEmail.getTextBox().getText().trim().isEmpty()) {
                    preferredEmail.setError("Preferred email address can't be empty.");
                    return false;
                } else if (!JsonUtils.isValidEmail(preferredEmail.getTextBox().getText().trim())) {
                    preferredEmail.setError("Not valid email address format.");
                    return false;
                } else {
                    preferredEmail.setOk();
                    return true;
                }
            }
        });

        final ListBox preferredLanguage = new ListBox();

        preferredLanguage.addItem("Not selected", "");
        preferredLanguage.addItem("Czech", "cs");
        preferredLanguage.addItem("English", "en");

        final ListBox timezone = new ListBox();
        timezone.addItem("Not set", "null");
        for (String zone : Utils.getTimezones()){
            timezone.addItem(zone, zone);
        }

        // content
        final FlexTable contactTable = new FlexTable();
        contactTable.setStyleName("inputFormFlexTableDark");

        contactTable.setHTML(1, 0, "Preferred&nbsp;mail:");
        contactTable.setWidget(1, 1, preferredEmail);
        contactTable.setHTML(2, 0, "Preferred&nbsp;language:");
        contactTable.setWidget(2, 1, preferredLanguage);
        contactTable.setHTML(3, 0, "Timezone:");
        contactTable.setWidget(3, 1, timezone);

        for (int i=1; i<contactTable.getRowCount(); i++) {
            contactTable.getFlexCellFormatter().addStyleName(i, 0, "itemName");
        }

        // SET SAVE CLICK HANDLER

        final CustomButton save = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes in contact info");
        //TabMenu menu = new TabMenu();
        //menu.addWidget(save);

        contactTable.setWidget(0, 0, save);

        save.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {

                ArrayList<Attribute> toSend = new ArrayList<Attribute>(); // will be set
                ArrayList<Attribute> toRemove = new ArrayList<Attribute>(); // will be removed

                for (Attribute a : userAttrs) {

                    String oldValue = a.getValue();
                    String newValue = "";

                    if (a.getFriendlyName().equalsIgnoreCase("preferredLanguage")) {
                        newValue = preferredLanguage.getValue(preferredLanguage.getSelectedIndex());
                    } else if (a.getFriendlyName().equalsIgnoreCase("timezone")) {
                        newValue = timezone.getValue(timezone.getSelectedIndex());
                    } else if (a.getFriendlyName().equalsIgnoreCase("preferredMail")) {
                        newValue = preferredEmail.getTextBox().getValue().trim();
                    } else {
                        continue; // other than contact attributes must be skipped
                    }

                    if (oldValue.equals(newValue) || (oldValue.equalsIgnoreCase("null") && ("").equalsIgnoreCase(newValue))) {
                        // if both values are the same or both are "empty"
                        continue; // skip this cycle
                    } else {
                        if (newValue.equalsIgnoreCase("")) {
                            toRemove.add(a); // value was cleared
                            // preferred email can't be ever removed from here
                        } else {
                            if (a.getFriendlyName().equalsIgnoreCase("preferredMail")) {
                                RequestPreferredEmailChange call = new RequestPreferredEmailChange();
                                call.requestChange(user, newValue);
                            } else {
                                a.setValue(newValue); // set value
                                toSend.add(a); // value was changed / added
                            }
                        }
                    }
                }

                // ids
                Map<String, Integer> ids = new HashMap<String, Integer>();
                ids.put("user", userId);

                // requests
                SetAttributes request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(save));
                RemoveAttributes removeRequest = new RemoveAttributes();
                // send if not empty
                if (!toRemove.isEmpty()) {
                    removeRequest.removeAttributes(ids, toRemove);
                }
                if (!toSend.isEmpty()) {
                    request.setAttributes(ids, toSend);
                }
            }
        });

        // GET USER ATTRIBUTES BY NAME

        GetListOfAttributes attrsCall = new GetListOfAttributes(new JsonCallbackEvents(){
            @Override
            public void onFinished(JavaScriptObject jso) {
                ArrayList<Attribute> list = JsonUtils.jsoAsList(jso);
                userAttrs = list;

                contactTable.setWidget(1, 1, preferredEmail);
                contactTable.setWidget(2, 1, preferredLanguage);
                contactTable.setWidget(3, 1, timezone);

                for (final Attribute a : list) {

                    if (a.getValue().equalsIgnoreCase("null")) {
                        continue; // skip null attributes
                    }

                    if (a.getFriendlyName().equalsIgnoreCase("preferredLanguage")) {
                        if (a.getValue().equals("cs")) {
                            preferredLanguage.setSelectedIndex(1);
                        } else if (a.getValue().equals("en")) {
                            preferredLanguage.setSelectedIndex(2);
                        }
                    } else if (a.getFriendlyName().equalsIgnoreCase("preferredMail")) {
                        preferredEmail.getTextBox().setText(a.getValue());
                    } else if (a.getFriendlyName().equalsIgnoreCase("timezone")) {
                        for (int i=0; i<timezone.getItemCount(); i++) {
                            if (timezone.getValue(i).equals(a.getValue())) {
                                timezone.setSelectedIndex(i);
                            }
                        }
                    }
                }
            }
            @Override
            public void onLoadingStart() {
                contactTable.setWidget(1, 1, new AjaxLoaderImage(true));
                contactTable.setWidget(2, 1, new AjaxLoaderImage(true));
                contactTable.setWidget(3, 1, new AjaxLoaderImage(true));
            }
            @Override
            public void onError(PerunError error) {
                contactTable.setWidget(1, 1, new AjaxLoaderImage(true).loadingError(error));
                contactTable.setWidget(2, 1, new AjaxLoaderImage(true).loadingError(error));
                contactTable.setWidget(3, 1, new AjaxLoaderImage(true).loadingError(error));
            }
        });
        // list of wanted attributes
        ArrayList<String> list = new ArrayList<String>();
        list.add("urn:perun:user:attribute-def:def:preferredLanguage");
        list.add("urn:perun:user:attribute-def:def:preferredMail");
        list.add("urn:perun:user:attribute-def:def:timezone");

        Map<String,Integer> ids = new HashMap<String,Integer>();
        ids.put("user", userId);
        attrsCall.getListOfAttributes(ids, list);

        layout.setWidget(1, 1, contactTable);

        HTML text = new HTML("<p><strong>VO preferences</strong><p class=\"inputFormInlineComment\">View VO / Group membership, change contact information, applications");
        layout.setWidget(1, 0, text);

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
        return SmallIcons.INSTANCE.userGrayIcon();
    }

    @Override
    public int hashCode() {
        final int prime = 11;
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
        if (this.userId != ((SelfPersonalTabItem)obj).userId)
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