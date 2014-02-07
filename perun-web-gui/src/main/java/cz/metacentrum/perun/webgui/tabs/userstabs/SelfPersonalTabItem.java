package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.storage.client.Storage;
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
import cz.metacentrum.perun.webgui.model.*;
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
 */
public class SelfPersonalTabItem implements TabItem {

    PerunWebSession session = PerunWebSession.getInstance();

    private SimplePanel contentWidget = new SimplePanel();
    private Label titleWidget = new Label("Loading user");

    private User user;
    private int userId;
    private ArrayList<Attribute> userAttrs = new ArrayList<Attribute>();

    private int vosIndex = 0;

    // members and their attributes lists
    private Map<VirtualOrganization, Member> members = new HashMap<VirtualOrganization, Member>();
    private Map<Member, ArrayList<Attribute>> memAttrs = new HashMap<Member, ArrayList<Attribute>>();

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
        final ScrollPanel sp = new ScrollPanel();
        sp.setSize("100%", "100%");
        sp.setStyleName("perun-tableScrollPanel");
        session.getUiElements().resizeSmallTabPanel(sp, 350, this);

        HorizontalPanel horizontalSplitter = new HorizontalPanel();
        horizontalSplitter.setStyleName("perun-table");
        horizontalSplitter.setSize("100%", "100%");
        final VerticalPanel leftPanel = new VerticalPanel();
        final VerticalPanel rightPanel = new VerticalPanel();

        horizontalSplitter.add(leftPanel);
        horizontalSplitter.add(rightPanel);
        horizontalSplitter.setCellWidth(leftPanel, "50%");
        horizontalSplitter.setCellWidth(rightPanel, "50%");

        sp.setWidget(horizontalSplitter);

        FlexTable quickHeader = new FlexTable();
        quickHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.directionIcon()));
        quickHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Quick links</p>");

        FlexTable prefHeader = new FlexTable();
        prefHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.settingToolsIcon()));
        prefHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Global settings</p>");

        leftPanel.add(quickHeader);
        rightPanel.add(prefHeader);

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
        final FlexTable settingsTable = new FlexTable();
        settingsTable.setStyleName("inputFormFlexTableDark");

        settingsTable.setHTML(1, 0, "Preferred&nbsp;mail:");
        settingsTable.setWidget(1, 1, preferredEmail);
        settingsTable.getFlexCellFormatter().setRowSpan(1, 0, 2);
        settingsTable.setHTML(2, 0, "");

        settingsTable.setHTML(3, 0, "Preferred&nbsp;language:");
        settingsTable.setWidget(3, 1, preferredLanguage);
        settingsTable.setHTML(4, 0, "Timezone:");
        settingsTable.setWidget(4, 1, timezone);

        for (int i=1; i<settingsTable.getRowCount(); i++) {
            if (i != 2) {
                settingsTable.getFlexCellFormatter().addStyleName(i, 0, "itemName");
            }
        }

        // SET SAVE CLICK HANDLER

        final CustomButton save = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes in contact info");
        //TabMenu menu = new TabMenu();
        //menu.addWidget(save);

        settingsTable.setWidget(0, 0, save);

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
                                final String val = newValue;
                                RequestPreferredEmailChange call = new RequestPreferredEmailChange(new JsonCallbackEvents(){
                                    @Override
                                    public void onFinished(JavaScriptObject jso) {
                                        try {
                                            Storage storage = Storage.getLocalStorageIfSupported();
                                            if (storage != null) {
                                                storage.setItem("urn:perun:gui:user:mailchange", val);
                                            }
                                        } catch (Exception ex) {
                                            // storage is blocked but supported
                                        }
                                    }
                                });
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

                settingsTable.setWidget(1, 1, preferredEmail);
                settingsTable.setWidget(3, 1, preferredLanguage);
                settingsTable.setWidget(4, 1, timezone);

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

                        preferredEmail.getTextBox().addKeyUpHandler(new KeyUpHandler() {
                            @Override
                            public void onKeyUp(KeyUpEvent event) {
                                if (!a.getValue().equals(preferredEmail.getTextBox().getText().trim())) {
                                    settingsTable.setHTML(2, 0, "No changes in preferred mail are saved, until new email address is validated. After saving new value, please check your inbox for validation mail.");
                                    settingsTable.getFlexCellFormatter().setStyleName(2, 0, "inputFormInlineComment");
                                } else {
                                    settingsTable.setHTML(2, 0, "");
                                }
                            }
                        });

                        try {
                            Storage storage = Storage.getLocalStorageIfSupported();
                            if (storage != null) {

                                // we must also write so we could get an error if storage is supported but size set to 0.
                                storage.setItem("urn:perun:gui:preferences:localStorageCheck", "checked");

                                String value = storage.getItem("urn:perun:gui:user:mailchange");
                                if (value != null && !value.isEmpty()) {
                                    if (value.equals(a.getValue())) {
                                        settingsTable.setHTML(2, 0, "Preferred mail change is pending for: "+value+". Please check your inbox for validation mail.");
                                        settingsTable.getFlexCellFormatter().setStyleName(2, 0, "inputFormInlineComment serverResponseLabelError");
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            // storage is blocked but supported
                        }

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
                settingsTable.setWidget(1, 1, new AjaxLoaderImage(true));
                settingsTable.setWidget(3, 1, new AjaxLoaderImage(true));
                settingsTable.setWidget(4, 1, new AjaxLoaderImage(true));
            }
            @Override
            public void onError(PerunError error) {
                settingsTable.setWidget(1, 1, new AjaxLoaderImage(true).loadingError(error));
                settingsTable.setWidget(3, 1, new AjaxLoaderImage(true).loadingError(error));
                settingsTable.setWidget(4, 1, new AjaxLoaderImage(true).loadingError(error));
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


        FlexTable quickLinks = new FlexTable();
        quickHeader.setStyleName("inputFormFlexTable");

        quickLinks.setHTML(0, 0, "Add login");
        quickLinks.setHTML(1, 0, "Change password");
        quickLinks.setHTML(2, 0, "Reset password");
        quickLinks.setHTML(3, 0, "Update titles in name");
        quickLinks.setHTML(4, 0, "Manage service identities");
        quickLinks.setHTML(5, 0, "View applications");
        quickLinks.setHTML(6, 0, "Add certificate");
        quickLinks.setHTML(7, 0, "Add SSH key");
        quickLinks.setHTML(8, 0, "Change shell on VO resources");
        quickLinks.setHTML(9, 0, "Request data/files quota change");
        quickLinks.setHTML(10, 0, "Manage mailing lists");

        rightPanel.add(settingsTable);
        leftPanel.add(quickLinks);

        this.contentWidget.setWidget(sp);
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

    /**
     * Returns image for vo membership status of member
     *
     * @param mem
     * @return image
     */
    private Image getImage(Member mem){

        ImageResource ir = null;

        // member status
        if(mem.getStatus().equalsIgnoreCase("VALID")){
            ir = SmallIcons.INSTANCE.acceptIcon();
        } else if (mem.getStatus().equalsIgnoreCase("INVALID")){
            ir = SmallIcons.INSTANCE.flagRedIcon();
        } else if (mem.getStatus().equalsIgnoreCase("SUSPENDED")){
            ir = SmallIcons.INSTANCE.stopIcon();
        } else if (mem.getStatus().equalsIgnoreCase("EXPIRED")){
            ir = SmallIcons.INSTANCE.flagYellowIcon();
        } else if (mem.getStatus().equalsIgnoreCase("DISABLED")){
            ir = SmallIcons.INSTANCE.binClosedIcon();
        }

        return new Image(ir);

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