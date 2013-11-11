package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
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
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.groupsManager.GetMemberGroups;
import cz.metacentrum.perun.webgui.json.membersManager.GetMemberByUser;
import cz.metacentrum.perun.webgui.json.usersManager.GetUserExtSources;
import cz.metacentrum.perun.webgui.json.usersManager.GetVosWhereUserIsMember;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.PerunAttributeTableWidget;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Page with user's details for user
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class SelfDetailTabItem implements TabItem, TabItemWithUrl {

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
    private Label titleWidget = new Label("Loading user");

    private User user;
    private int userId = 0;
    private int vosIndex = 0;
    private int uesIndex = 0;
    private int lastTabId = 0;

    // members and their attributes lists
    private Map<VirtualOrganization, Member> members = new HashMap<VirtualOrganization, Member>();
    private Map<Member, ArrayList<Attribute>> memAttrs = new HashMap<Member, ArrayList<Attribute>>();

    // members and their attributes lists
    private Map<VirtualOrganization, Member> voMembers = new HashMap<VirtualOrganization, Member>();
    private Map<Member, ArrayList<Attribute>> voMemAttrs = new HashMap<Member, ArrayList<Attribute>>();

    // store-place for user attributes
    private ArrayList<Attribute> userAttrs = new ArrayList<Attribute>();
    private ArrayList<Attribute> userLoginAttrs = new ArrayList<Attribute>();

    // tab panel
    TabLayoutPanel tabPanel;

    /**
     * Creates a tab instance
     */
    public SelfDetailTabItem(){
        this.user = session.getUser();
        this.userId = user.getId();
    }

    /**
     * Creates a tab instance with custom user
     * @param user
     */
    public SelfDetailTabItem(User user){
        this.user = user;
        this.userId = user.getId();
    }

    /**
     * Creates a tab instance with custom user
     * @param userId
     */
    public SelfDetailTabItem(int userId) {
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

        userAttrs.clear();
        userLoginAttrs.clear();

        this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim())+": My profile");

        // panel
        tabPanel = new TabLayoutPanel(33, Unit.PX);
        tabPanel.addStyleName("smallTabPanel");
        tabPanel.setWidth("100%");

        final TabItem tab = this;
        // common selection actions
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            public void onSelection(SelectionEvent<Integer> event) {
                UiElements.runResizeCommands(tab);
                setLastTabId(event.getSelectedItem());
            }
        });

        session.getUiElements().resizeSmallTabPanel(tabPanel, 100, this);

        final SimplePanel sp0 = new SimplePanel(); // personal data
        tabPanel.add(sp0, "User overview");
        sp0.add(loadPersonalInfo());

        final ArrayList<VirtualOrganization> listOfVos = new ArrayList<VirtualOrganization>();

        // get user VOs
        GetVosWhereUserIsMember vos = new GetVosWhereUserIsMember(user.getId(), new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                // sort & add
                ArrayList<VirtualOrganization> vosList = JsonUtils.jsoAsList(jso);
                vosList = new TableSorter<VirtualOrganization>().sortByName(vosList);
                listOfVos.addAll(vosList);

                // if not empty
                if (!listOfVos.isEmpty()) {
                    for (int i=0; i<listOfVos.size(); i++) {
                        // create empty tab for VO
                        final SimplePanel voContent = new SimplePanel();
                        tabPanel.add(voContent, Utils.getStrippedStringWithEllipsis(listOfVos.get(i).getName()));

                        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
                            public void onSelection(SelectionEvent<Integer> event) {
                                // if this tab is selected
                                if (event.getSelectedItem() == tabPanel.getWidgetIndex(voContent)) {
                                    // is child of widget null? = not loaded?
                                    if (((SimplePanel)tabPanel.getWidget(event.getSelectedItem())).getWidget() == null) {
                                        // load position -1 because of personal info tab
                                        voContent.add(loadVoContent(listOfVos.get(event.getSelectedItem()-1)));
                                    }
                                }
                            }
                        });
                    }
                }

                tabPanel.selectTab(getLastTabId(), true);  // select and trigger onSelect event

            }
        });
        vos.retrieveData();

        this.contentWidget.setWidget(tabPanel);

        return getWidget();

    }

    /**
     * Loads personal info - user detail and user attributes
     *
     * @return return tab content
     */
    private Widget loadPersonalInfo() {

        // CONTENT PANEL
        ScrollPanel scroll = new ScrollPanel();
        final VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setStyleName("perun-table");
        contentPanel.setSpacing(5); // spacing
        scroll.setWidget(contentPanel);
        scroll.setStyleName("perun-tableScrollPanel");
        session.getUiElements().resizeSmallTabPanel(scroll, 350, this);
        scroll.setWidth("100%");

        // PERSONAL INFO
        contentPanel.add(getWidgetPersonalInfo());

        // VOS
        contentPanel.add(getWidgetVos());

        // logins / certificates
        contentPanel.add(getWidgetLoginsCertificates());

        // CONTACT INFO
        contentPanel.add(getWidgetContactInfo());

        // TODO unified user logins a certificates into first callback




        // EXTERNAL ORIGIN
        contentPanel.add(getWidgetExtSources());


        return scroll;

    }

    /**
     * Returns the widget with personal information
     * @return
     */
    private Widget getWidgetPersonalInfo() {
        DisclosurePanel dp = new DisclosurePanel();
        dp.setWidth("100%");
        dp.setOpen(true);

        // header
        FlexTable personalHeader = new FlexTable();
        personalHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.userGrayIcon()));
        personalHeader.setWidget(0, 1, new HTML("<h3>User details</h3>"));
        personalHeader.setTitle("Click to show/hide User details");
        dp.setHeader(personalHeader);

        // content
        final FlexTable layout = new FlexTable();
        layout.setStyleName("userDetailTable");

        // Add some standard form options
        layout.setHTML(0, 0, "<strong>User&nbsp;ID:</strong>");
        layout.setHTML(0, 1, String.valueOf(user.getId()));
        layout.setHTML(1, 0, "<strong>Full&nbsp;name:</strong>");
        layout.setHTML(1, 1, user.getFullNameWithTitles());
        layout.setHTML(2, 0, "<strong>User&nbsp;type:</strong>");
        if (user.isServiceUser()) {
            layout.setHTML(2, 1, "Service");
        } else {
            layout.setHTML(2, 1, "Person");
        }

        dp.setContent(layout);
        return dp;
    }

    /**
     * Returns the widget with VOs information
     * @return
     */
    private Widget getWidgetVos()
    {
        final DisclosurePanel vosDp = new DisclosurePanel();
        vosDp.setWidth("100%");
        vosDp.setOpen(true);

        // header
        FlexTable vosHeader = new FlexTable();
        vosHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.buildingIcon()));
        vosHeader.setWidget(0, 1, new HTML("<h3>Virtual organizations</h3>"));
        vosHeader.setTitle("Click to show/hide VO details");
        vosDp.setHeader(vosHeader);

        // set content
        final FlexTable vosTable = new FlexTable();
        vosTable.addStyleName("userDetailTable");
        vosDp.setContent(vosTable);

        // draw content
        JsonCallbackEvents vosEvent = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                // get
                ArrayList<VirtualOrganization> vos = JsonUtils.jsoAsList(jso);
                // check
                if (vos.isEmpty()) {
                    vosDp.setContent(new HTML("You are not member of any Virtual Organization."));
                }
                // sort
                vos = new TableSorter<VirtualOrganization>().sortByName(vos);

                vosTable.setHTML(0, 0, "<strong>Vo name</strong>");
                vosTable.setHTML(0, 1, "<strong>Member ID</strong>");
                vosTable.setHTML(0, 2, "<strong>Membership status</strong>");
                vosTable.setHTML(0, 3, "<strong>Membership expiration</strong>");

                for (final VirtualOrganization vo : vos){

                    members.put(vo, null);

                    GetMemberByUser call = new GetMemberByUser(vo.getId(), user.getId(), new JsonCallbackEvents(){
                        public void onFinished(JavaScriptObject jso) {

                            final Member mem = jso.cast();
                            members.put(vo, mem);

                            GetListOfAttributes attrsCall = new GetListOfAttributes(new JsonCallbackEvents(){
                                public void onFinished(JavaScriptObject jso) {
                                    ArrayList<Attribute> list = JsonUtils.jsoAsList(jso);
                                    memAttrs.put(mem, list);
                                    vosIndex++;

                                    if (session.isPerunAdmin()) {
                                        Hyperlink link = new Hyperlink();
                                        link.setText(vo.getName());
                                        link.setTargetHistoryToken(session.getTabManager().getLinkForTab(new MemberDetailTabItem(mem.getId(), 0)));
                                        vosTable.setWidget(vosIndex, 0, link);
                                    } else {
                                        vosTable.setHTML(vosIndex, 0, vo.getName());
                                    }
                                    vosTable.setHTML(vosIndex, 1, String.valueOf(mem.getId()));
                                    vosTable.setHTML(vosIndex, 2, getImage(mem) + " " + mem.getStatus());
                                    vosTable.setHTML(vosIndex, 3, "&nbsp;");
                                    for (Attribute a : list) {
                                        if (a.getFriendlyName().equalsIgnoreCase("membershipExpiration")) {
                                            if (!a.getValue().equalsIgnoreCase("null")) {
                                                vosTable.setHTML(vosIndex, 3, a.getValue());
                                            }
                                            break;
                                        }
                                    }

                                }
                                public void onError(PerunError error) {

                                    vosIndex++;

                                    vosTable.setHTML(vosIndex, 0, vo.getName());
                                    vosTable.setHTML(vosIndex, 1, String.valueOf(mem.getId()));
                                    vosTable.setHTML(vosIndex, 2, getImage(mem)+" "+mem.getStatus());
                                    vosTable.setHTML(vosIndex, 3, "Error while loading.");

                                }
                            });
                            // retrieve only needed attributes
                            Map<String, Integer> ids = new HashMap<String, Integer>();
                            ids.put("member", mem.getId());
                            attrsCall.getListOfAttributes(ids, "urn:perun:member:attribute-def:def:membershipExpiration");
                        }
                        @Override
                        public void onError(PerunError error) {
                            vosIndex++;
                            vosTable.setHTML(vosIndex, 0, vo.getName());
                            vosTable.setHTML(vosIndex, 1, "Error while loading.");
                            vosTable.setHTML(vosIndex, 2, "Error while loading.");
                            vosTable.setHTML(vosIndex, 3, "Error while loading.");
                        }
                    });
                    call.retrieveData();

                }
            }
            public void onError(PerunError error) {
                vosTable.setHTML(0, 0, "Error while loading.");
            }
        };
        GetVosWhereUserIsMember vos = new GetVosWhereUserIsMember(user.getId(), vosEvent);
        vos.retrieveData();

        return vosDp;
    }

    /**
     * Returns the widget with contact information
     * @return
     */
    private Widget getWidgetContactInfo()
    {
        // widgets
        final TextBox preferredEmail = new TextBox();
        preferredEmail.setWidth("350px");
        preferredEmail.setEnabled(false); // disable until change via link will work in core
        final ListBox preferredLanguage = new ListBox();
        final TextBox organization = new TextBox();
        organization.setWidth("350px");
        organization.setEnabled(false); // disable changing info from IDP
        final TextArea address = new TextArea();
        address.setWidth("350px");
        address.setHeight("50px");
        final TextBox phone = new TextBox();
        phone.setWidth("350px");
        final TextBox workplace = new TextBox();
        workplace.setWidth("350px");
        final TextBox research = new TextBox();
        research.setWidth("350px");

        preferredLanguage.addItem("Not selected", "");
        preferredLanguage.addItem("Czech", "cs");
        preferredLanguage.addItem("English", "en");

        DisclosurePanel contactDp = new DisclosurePanel();
        contactDp.setWidth("100%");
        contactDp.setOpen(true);

        // header
        FlexTable contactHeader = new FlexTable();
        contactHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.vcardIcon()));
        contactHeader.setWidget(0, 1, new HTML("<h3>Contact</h3>"));
        contactHeader.setTitle("Click to show/hide user's contact info");
        contactDp.setHeader(contactHeader);

        // content
        final FlexTable contactTable = new FlexTable();
        contactTable.setStyleName("userDetailTable");

        // menu
        TabMenu menu = new TabMenu();
        //contentPanel.add(menu.getWidget());
        //contentPanel.setCellHeight(menu.getWidget(), "50px");
        final CustomButton save = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes in contact info");
        menu.addWidget(save);
        // content
        VerticalPanel vp = new VerticalPanel();
        vp.add(menu);
        vp.setCellHeight(menu, "30px");
        vp.add(contactTable);

        contactDp.setContent(vp);
        //contentPanel.add(contactDp);

        contactTable.setHTML(0, 0, "<strong>Organization:</strong>");
        contactTable.setWidget(0, 1, organization.asWidget());
        contactTable.setHTML(1, 0, "<strong>Workplace:</strong>");
        contactTable.setWidget(1, 1, workplace.asWidget());
        contactTable.setHTML(2, 0, "<strong>Research&nbsp;group:</strong>");
        contactTable.setWidget(2, 1, research.asWidget());
        contactTable.setHTML(3, 0, "<strong>Address:</strong>");
        contactTable.setWidget(3, 1, address.asWidget());
        contactTable.setHTML(4, 0, "<strong>Phone:</strong>");
        contactTable.setWidget(4, 1, phone.asWidget());

        contactTable.setHTML(5, 0, "<strong>Preferred&nbsp;mail:</strong>");
        contactTable.setWidget(5, 1, preferredEmail.asWidget());
        contactTable.setHTML(6, 0, "<strong>Preferred&nbsp;language:</strong>");
        contactTable.setWidget(6, 1, preferredLanguage.asWidget());

        // SET SAVE CLICK HANDLER

        save.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {

                ArrayList<Attribute> toSend = new ArrayList<Attribute>(); // will be set
                ArrayList<Attribute> toRemove = new ArrayList<Attribute>(); // will be removed

                for (Attribute a : userAttrs) {

                    String oldValue = a.getValue();
                    String newValue = "";

                    if (a.getFriendlyName().equalsIgnoreCase("preferredLanguage")) {
                        newValue = preferredLanguage.getValue(preferredLanguage.getSelectedIndex());
                    } else if (a.getFriendlyName().equalsIgnoreCase("preferredMail")) {
                        newValue = preferredEmail.getText();
                    } else if (a.getFriendlyName().equalsIgnoreCase("organization")) {
                        newValue = organization.getText();
                    }  else if (a.getFriendlyName().equalsIgnoreCase("workplace")) {
                        newValue = workplace.getText();
                    } else if (a.getFriendlyName().equalsIgnoreCase("address")) {
                        newValue = address.getText();
                    }  else if (a.getFriendlyName().equalsIgnoreCase("researchGroup")) {
                        newValue = research.getText();
                    }  else if (a.getFriendlyName().equalsIgnoreCase("phone")) {
                        newValue = phone.getText();
                    } else {
                        continue; // other than contact attributes must be skipped
                    }

                    if (oldValue.equalsIgnoreCase(newValue) || (oldValue.equalsIgnoreCase("null") && newValue.equalsIgnoreCase(""))) {
                        // if both values are the same or both are "empty"
                        continue; // skip this cycle
                    } else {
                        if (newValue.equalsIgnoreCase("")) {
                            toRemove.add(a); // value was cleared
                        } else {
                            a.setValue(newValue); // set value
                            toSend.add(a); // value was changed / added
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
            public void onFinished(JavaScriptObject jso) {
                ArrayList<Attribute> list = JsonUtils.jsoAsList(jso);
                userAttrs = list;

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
                        preferredEmail.setText(a.getValue());
                    } else if (a.getFriendlyName().equalsIgnoreCase("organization")) {
                        organization.setText(a.getValue());
                    } else if (a.getFriendlyName().equalsIgnoreCase("workplace")) {
                        workplace.setText(a.getValue());
                    } else if (a.getFriendlyName().equalsIgnoreCase("address")) {
                        address.setText(a.getValue());
                    } else if (a.getFriendlyName().equalsIgnoreCase("phone")) {
                        phone.setText(a.getValue());
                    } else if (a.getFriendlyName().equalsIgnoreCase("researchGroup")) {
                        research.setText(a.getValue());
                    }
                }
            }
        });
        // list of wanted attributes
        ArrayList<String> list = new ArrayList<String>();
        list.add("urn:perun:user:attribute-def:def:preferredLanguage");
        list.add("urn:perun:user:attribute-def:def:preferredMail");
        list.add("urn:perun:user:attribute-def:def:organization");
        list.add("urn:perun:user:attribute-def:def:workplace");
        list.add("urn:perun:user:attribute-def:opt:researchGroup");
        list.add("urn:perun:user:attribute-def:def:address");
        list.add("urn:perun:user:attribute-def:def:phone");
        Map<String,Integer> ids = new HashMap<String,Integer>();
        ids.put("user", userId);
        attrsCall.getListOfAttributes(ids, list);


        return contactDp;
    }

    /**
     * Returns the widget with ext sources
     * @return
     */
    private Widget getWidgetExtSources()
    {
        DisclosurePanel originDp = new DisclosurePanel();
        originDp.setWidth("100%");
        originDp.setOpen(true);

        // header
        FlexTable originHeader = new FlexTable();
        originHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.vcardIcon()));
        originHeader.setWidget(0, 1, new HTML("<h3>External identities</h3>"));
        originHeader.setTitle("Click to show/hide user's external identities");
        originDp.setHeader(originHeader);

        // set content
        final FlexTable originTable = new FlexTable();
        originTable.setStyleName("userDetailTable");
        originDp.setContent(originTable);

        final GetUserExtSources extSources = new GetUserExtSources(user.getId(), new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {

                ArrayList<UserExtSource> ues = JsonUtils.jsoAsList(jso);

                if (ues.isEmpty()) {
                    originTable.setHTML(0, 0, "No external identity found.");
                    return;
                } else {
                    originTable.setHTML(0, 0, "<strong>Origin</strong>");
                    originTable.setHTML(0, 1, "<strong>Identity</strong>");
                    //		originTable.setHTML(0, 2, "<strong>Level&nbsp;of&nbsp;assurance</strong>");
                    for (UserExtSource es : ues) {
                        // show only Federation identities
                        if (es.getExtSource().getType().equalsIgnoreCase("cz.metacentrum.perun.core.impl.ExtSourceIdp")) {
                            uesIndex++;
                            originTable.setHTML(uesIndex, 0, es.getExtSource().getName());
                            originTable.setHTML(uesIndex, 1, es.getLogin());
                            //				originTable.setHTML(uesIndex, 2, String.valueOf(es.getLevelOfAfiliation()));
                        }
                    }
                }
            }
            public void onError(PerunError error) {
                originTable.setHTML(0, 0, "Error while loading.");
            }
        });
        extSources.retrieveData();
        return originDp;
    }

    /**
     * Returns the widget with logins and certificates
     * @return
     */
    private Widget getWidgetLoginsCertificates()
    {
        // USER LOGINS / CERTIFICATES / SSH KEYS
        final DisclosurePanel attrsDp = new DisclosurePanel();
        attrsDp.setWidth("100%");
        attrsDp.setOpen(true);

        // header
        FlexTable attrsHeader = new FlexTable();
        attrsHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.keyIcon()));
        attrsHeader.setWidget(0, 1, new HTML("<h3>Authentications</h3>"));
        attrsHeader.setTitle("Click to show/hide user's logins & certificates");
        attrsDp.setHeader(attrsHeader);
        attrsDp.setContent(new AjaxLoaderImage());

        // ids used for attribute handling
        Map<String, Integer> ids = new HashMap<String, Integer>();
        ids.put("user", userId);

        // attribute table
        final PerunAttributeTableWidget table = new PerunAttributeTableWidget(ids);
        table.setDescriptionShown(true);

        // load data
        GetListOfAttributes attributes = new GetListOfAttributes(new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {

                userLoginAttrs.addAll(JsonUtils.<Attribute>jsoAsList(jso));

                userLoginAttrs = new TableSorter<Attribute>().sortByAttrNameTranslation(userLoginAttrs);
                for (Attribute a : userLoginAttrs) {
					/*
					if (a.getBaseFriendlyName().equalsIgnoreCase("login-namespace")) {
						if (a.getValueAsObject() != null) {
							// add only non-empty logins
							table.add(a);													
						}
					} else */ if (a.getFriendlyName().equalsIgnoreCase("userCertificates")) {
                        table.add(a);
                    } else if (a.getFriendlyName().equalsIgnoreCase("sshPublicKey")) {
                        table.add(a);
                    }  else if (a.getFriendlyName().equalsIgnoreCase("kerberosAdminPrincipal")) {
                        table.add(a);
                    }   else if (a.getFriendlyName().equalsIgnoreCase("sshPublicAdminKey")) {
                        table.add(a);
                    }
                }
                // build attr table
                table.build();

                // content
                VerticalPanel vpContent = new VerticalPanel();
                vpContent.add(table.asWidget());
                attrsDp.setContent(vpContent);

                FlexTable innerTable = new FlexTable();
                innerTable.setCellPadding(10);
                vpContent.add(innerTable);
                int rowCount = 0;

                for (final Attribute a : userLoginAttrs) {
                    if (a.getBaseFriendlyName().equalsIgnoreCase("login-namespace")) {
                        if (a.getValueAsObject() != null) {
                            // name
                            innerTable.setHTML(rowCount, 0, "<strong>"+a.getDisplayName()+"</strong>");
                            // value
                            innerTable.setHTML(rowCount, 1, a.getValue());
                            // change password
                            if ("einfra".equalsIgnoreCase(a.getFriendlyNameParameter()) ||
                                    "einfra-services".equalsIgnoreCase(a.getFriendlyNameParameter()) ||
                                    "egi-ui".equalsIgnoreCase(a.getFriendlyNameParameter()) ||
                                    "sitola".equalsIgnoreCase(a.getFriendlyNameParameter())) {
                                CustomButton cb = new CustomButton("Change password", SmallIcons.INSTANCE.keyIcon(), new ClickHandler(){
                                    public void onClick(ClickEvent event) {
                                        session.getTabManager().addTabToCurrentTab(new SelfPasswordTabItem(a.getFriendlyNameParameter(), a.getValue(), SelfPasswordTabItem.Actions.CHANGE));
                                    }
                                });
                                innerTable.setWidget(rowCount, 2, cb);
                            }
                            rowCount++;
                        }
                    }
                }
            }
            public void onError(PerunError error) {
                ((AjaxLoaderImage)attrsDp.getContent()).loadingError(error);
            }
        });

        ArrayList<String> list = new ArrayList<String>();
        list.add("urn:perun:user:attribute-def:def:userCertificates");
        list.add("urn:perun:user:attribute-def:def:kerberosAdminPrincipal");
        list.add("urn:perun:user:attribute-def:def:sshPublicKey");
        list.add("urn:perun:user:attribute-def:def:sshPublicAdminKey");
        list.add("urn:perun:user:attribute-def:def:login-namespace:mu");
        list.add("urn:perun:user:attribute-def:def:login-namespace:einfra");
        list.add("urn:perun:user:attribute-def:def:login-namespace:sitola");
        list.add("urn:perun:user:attribute-def:def:login-namespace:egi-ui");
        list.add("urn:perun:user:attribute-def:def:login-namespace:cesnet");
        list.add("urn:perun:user:attribute-def:def:login-namespace:meta");
        list.add("urn:perun:user:attribute-def:def:login-namespace:einfra-services");
        list.add("urn:perun:user:attribute-def:def:login-namespace:shongo");
        attributes.getListOfAttributes(ids, list);

        return attrsDp;
    }

    /**
     * Loads members details for each Vo
     *
     * @param vo one of user's VOs
     * @return widget with members details content
     */
    private Widget loadVoContent(final VirtualOrganization vo) {

        // CONTENT PANEL
        ScrollPanel scroll = new ScrollPanel();
        final VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setStyleName("perun-table");
        contentPanel.setSpacing(5); // spacing
        scroll.setWidget(contentPanel);
        scroll.setStyleName("perun-tableScrollPanel");
        session.getUiElements().resizeSmallTabPanel(scroll, 350, this);
        scroll.setWidth("100%");

        // VO DETAIL (LINKS)

        FlexTable voHeader = new FlexTable();
        voHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.buildingIcon()));
        voHeader.setWidget(0, 1, new HTML("<h3>"+vo.getName()+"</h3>"));
        contentPanel.add(voHeader);

        final FlexTable voTable = new FlexTable();
        contentPanel.add(voTable);

        // MEMBERSHIP

        DisclosurePanel membership = new DisclosurePanel();
        membership.setWidth("100%");
        membership.setOpen(true);

        // header
        FlexTable membershipHeader = new FlexTable();
        membershipHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.userGreenIcon()));
        membershipHeader.setWidget(0, 1, new HTML("<h3>Membership status</h3>"));
        membershipHeader.setTitle("Click to show/hide user's membership status for VO: " + vo.getName());
        membership.setHeader(membershipHeader);

        // content
        final FlexTable membershipTable = new FlexTable();
        membershipTable.setStyleName("userDetailTable");
        membership.setContent(membershipTable);
        contentPanel.add(membership);

        membershipTable.setHTML(0, 0, "<strong>Member&nbsp;ID:</strong>");
        membershipTable.setHTML(0, 1, "");
        membershipTable.setHTML(1, 0, "<strong>Membership&nbsp;status:</strong>");
        membershipTable.setHTML(1, 1, "");
        membershipTable.setHTML(2, 0, "<strong>Membership&nbsp;expiration:</strong>");
        membershipTable.setHTML(2, 1, "");

        // GROUPS

        final DisclosurePanel groups = new DisclosurePanel();
        groups.setWidth("100%");
        groups.setOpen(true);

        // header
        FlexTable groupsHeader = new FlexTable();
        groupsHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.groupIcon()));
        groupsHeader.setWidget(0, 1, new HTML("<h3>Member of Groups</h3>"));
        groupsHeader.setTitle("Click to show/hide members Groups in VO: " + vo.getName());
        groups.setHeader(groupsHeader);

        // content
        final FlexTable groupsTable = new FlexTable();
        groupsTable.setStyleName("userDetailTable");
        groups.setContent(groupsTable);
        contentPanel.add(groups);


        // SETTINGS
        // header
        final DisclosurePanel settings = new DisclosurePanel();
        settings.setWidth("100%");
        settings.setOpen(true);

        FlexTable settingsHeader = new FlexTable();
        settingsHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.settingToolsIcon()));
        settingsHeader.setWidget(0, 1, new HTML("<h3>Settings</h3>"));
        settingsHeader.setTitle("Change settings of resources in: " + vo.getName());
        settings.setHeader(settingsHeader);

        VerticalPanel settingsContents = new VerticalPanel();
        settings.setContent(settingsContents);
        settingsContents.add(new CustomButton("Quota and shell settings for resources", SmallIcons.INSTANCE.settingToolsIcon(), new ClickHandler() {

            public void onClick(ClickEvent event) {
                session.getTabManager().addTab(new SelfSettingsTabItem(user, vo));
            }
        }));

        // content
        contentPanel.add(settings);

        // CALLBACK

        GetMemberByUser call = new GetMemberByUser(vo.getId(), user.getId(), new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                // set member
                final Member mem = jso.cast();
                voMembers.put(vo, mem);

                // GET ATTIBUTES

                GetListOfAttributes attrsCall = new GetListOfAttributes(new JsonCallbackEvents(){
                    public void onFinished(JavaScriptObject jso) {
                        ArrayList<Attribute> list = JsonUtils.jsoAsList(jso);
                        voMemAttrs.put(mem, list);

                        membershipTable.setHTML(0, 1, String.valueOf(mem.getId()));
                        membershipTable.setHTML(1, 1, getImage(mem) + " " + mem.getStatus());
                        for (final Attribute a : list) {
                            if (a.getValue().equalsIgnoreCase("null")) {
                                continue; // skip null attributes
                            }
                            if (a.getFriendlyName().equalsIgnoreCase("membershipExpiration")) {
                                if (!a.getValue().equalsIgnoreCase("null")) {
                                    membershipTable.setHTML(2, 1, a.getValue());
                                }
                            }
                        }
                    }
                });
                // list of wanted attributes
                ArrayList<String> list = new ArrayList<String>();
                list.add("urn:perun:member:attribute-def:def:membershipExpiration");
                Map<String,Integer> ids = new HashMap<String,Integer>();
                ids.put("member", mem.getId());
                attrsCall.getListOfAttributes(ids, list);

                // GET MEMBER GROUPS

                GetMemberGroups groupsCall = new GetMemberGroups(voMembers.get(vo).getId(), new JsonCallbackEvents(){
                    public void onError(PerunError error) {
                        groups.setContent(new AjaxLoaderImage().loadingError(error));
                    }
                    public void onFinished(JavaScriptObject jso) {
                        ArrayList<Group> list = JsonUtils.jsoAsList(jso);
                        if (list.isEmpty() || list == null) {
                            groups.setContent(new HTML("You are not member of any group."));
                            return;
                        }
                        groupsTable.setHTML(0, 0, "<strong>Name</strong>");
                        groupsTable.setHTML(0, 1, "<strong>Description</strong>");
                        for (int i=0; i<list.size(); i++){
                            groupsTable.setHTML(i+1, 0, list.get(i).getName());
                            groupsTable.setHTML(i+1, 1, list.get(i).getDescription());
                        }
                    }
                });
                groupsCall.retrieveData();
            }
        });
        call.retrieveData();

        // GET VO ATTRS

        GetAttributes voAttrsCall = new GetAttributes(new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                ArrayList<Attribute> attrs = JsonUtils.jsoAsList(jso);
                int i = 0; // skip header
                for (Attribute a : attrs) {
                    if (a.getFriendlyName().equalsIgnoreCase("userManualsLink")) {
                        voTable.setHTML(i, 0, "<strong>User's&nbsp;manuals:</strong>");
                        Anchor link = new Anchor(a.getValue(), a.getValue());
                        link.getElement().setPropertyString("target", "_blank");
                        voTable.setWidget(i, 1, link);
                        i++;
                    } else if (a.getFriendlyName().equalsIgnoreCase("dashboardLink")) {
                        voTable.setHTML(i, 0, "<strong>Dashboard:</strong>");
                        Anchor link = new Anchor(a.getValue(), a.getValue());
                        link.getElement().setPropertyString("target", "_blank");
                        voTable.setWidget(i, 1, link);
                        i++;
                    }
                }
            }
            public void onError(PerunError error) {
                voTable.setHTML(0, 1, "Error while loading");
            }
        });
        voAttrsCall.getVoAttributes(vo.getId());
        voAttrsCall.retrieveData();

        return scroll;

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

    private int getLastTabId(){
        return lastTabId;
    }

    private void setLastTabId(int id) {
        lastTabId = id;
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
        final int prime = 31;
        int result = 432;
        result = prime * result * userId;
        return result;
    }

    /**
     * @param obj
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (this.userId != ((SelfDetailTabItem)obj).userId)
            return false;

        return true;
    }

    public boolean multipleInstancesEnabled() {
        return false;
    }

    public void open()
    {
        session.setActiveUser(user);
        session.getUiElements().getMenu().openMenu(MainMenu.USER);
        session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, "My profile", getUrlWithParameters());
    }

    public boolean isAuthorized() {

        if (session.isSelf(userId)) {
            return true;
        } else {
            return false;
        }

    }

    public final static String URL = "info";

    public String getUrl()
    {
        return URL;
    }

    public String getUrlWithParameters()
    {
        return  UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId;
    }

    static public SelfDetailTabItem load(Map<String, String> parameters) {

        if (parameters.containsKey("id")) {
            int uid = Integer.parseInt(parameters.get("id"));
            if (uid != 0) {
                return new SelfDetailTabItem(uid);
            }
        }
        return new SelfDetailTabItem();
    }

}