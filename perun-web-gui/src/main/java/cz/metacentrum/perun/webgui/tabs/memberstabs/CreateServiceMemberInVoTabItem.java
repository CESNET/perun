package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.applicationresources.FormInputStatusWidget;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.membersManager.CreateServiceMember;
import cz.metacentrum.perun.webgui.json.membersManager.ValidateMemberAsync;
import cz.metacentrum.perun.webgui.json.usersManager.CreatePassword;
import cz.metacentrum.perun.webgui.json.usersManager.FindUsers;
import cz.metacentrum.perun.webgui.json.usersManager.IsLoginAvailable;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.MembersTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.Map;

/**
 * Create service member in VO.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateServiceMemberInVoTabItem implements TabItem, TabItemWithUrl {

    /**
     * vo id
     */
    private int voId;
    private VirtualOrganization vo;

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
    private Label titleWidget = new Label("Loading VO");

    private String searchString = "";

    /**
     * Constructor
     *
     * @param voId ID of VO into which member should be added
     */
    public CreateServiceMemberInVoTabItem(int voId){
        this.voId = voId;
        JsonCallbackEvents events = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso)
            {
                vo = jso.cast();
            }
        };
        GetEntityById callback = new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events);
        callback.retrieveData();
    }

    public boolean isPrepared(){
        return !(vo == null);
    }

    /**
     * Constructor
     *
     * @param vo ID of VO into which member should be added
     */
    public CreateServiceMemberInVoTabItem(VirtualOrganization vo){
        this.vo = vo;
        this.voId = vo.getId();
    }

    public Widget draw() {

        titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName()) + ": create service member");

        final TabItem tab = this;

        // draw the main tab
        final VerticalPanel mainTab = new VerticalPanel();
        mainTab.setSize("100%", "100%");

        final ExtendedTextBox serviceUserName = new ExtendedTextBox();
        final ExtendedTextBox serviceUserEmail = new ExtendedTextBox();
        final ExtendedTextBox serviceUserLogin = new ExtendedTextBox();
        final ExtendedPasswordBox serviceUserPassword = new ExtendedPasswordBox();
        final ExtendedPasswordBox serviceUserPassword2 = new ExtendedPasswordBox();
        final ListBox namespace = new ListBox();

        serviceUserPassword.getTextBox().setWidth("200px");
        serviceUserPassword2.getTextBox().setWidth("200px");

        final ExtendedTextBox.TextBoxValidator nameValidator = new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (serviceUserName.getTextBox().getValue().trim().isEmpty()) {
                    serviceUserName.setError("Name can't be empty!");
                    return false;
                }
                serviceUserName.setOk();
                return true;
            }
        };
        serviceUserName.setValidator(nameValidator);

        final ExtendedTextBox.TextBoxValidator loginValidator = new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (serviceUserLogin.getTextBox().getValue().trim().isEmpty()) {
                    serviceUserLogin.setError("Login can't be empty!");
                    return false;
                }
                RegExp regExp = RegExp.compile(Utils.LOGIN_VALUE_MATCHER);
                boolean match = regExp.test(serviceUserLogin.getTextBox().getValue().trim());
                if (!match) {
                    serviceUserLogin.setError("Invalid format!");
                    return false;
                }
                if (serviceUserLogin.isProcessing() || serviceUserLogin.isHardError()) {
                    // keep original message
                    return false;
                }
                serviceUserLogin.setOk();
                return true;
            }
        };
        serviceUserLogin.setValidator(loginValidator);


        final ExtendedTextBox.TextBoxValidator emailValidator = new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (!JsonUtils.isValidEmail(serviceUserEmail.getTextBox().getValue().trim())) {
                    serviceUserEmail.setError("Wrong email format!");
                    return false;
                }
                serviceUserEmail.setOk();
                return true;
            }
        };
        serviceUserEmail.setValidator(emailValidator);

        final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (serviceUserPassword.getTextBox().getValue().trim().isEmpty()) {
                    serviceUserPassword.setError("Password can't be empty !");
                    return false;
                } else if (!serviceUserPassword.getTextBox().getValue().trim().equals(serviceUserPassword2.getTextBox().getValue().trim())) {
                    serviceUserPassword.setError("Password in both textboxes must be the same !");
                    return false;
                } else {
                    serviceUserPassword.setOk();
                    serviceUserPassword2.setOk();
                    return true;
                }
            }
        };
        serviceUserPassword.setValidator(validator);

        final ExtendedTextBox.TextBoxValidator validator2 = new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (serviceUserPassword2.getTextBox().getValue().trim().isEmpty()) {
                    serviceUserPassword2.setError("Password can't be empty !");
                    return false;
                } else if (!serviceUserPassword2.getTextBox().getValue().trim().equals(serviceUserPassword.getTextBox().getValue().trim())) {
                    serviceUserPassword2.setError("Password in both textboxes must be the same !");
                    return false;
                } else {
                    serviceUserPassword2.setOk();
                    serviceUserPassword.setOk();
                    return true;
                }
            }
        };
        serviceUserPassword2.setValidator(validator2);

        for (String name : Utils.getSupportedPasswordNamespaces()) {
            namespace.addItem(name.toUpperCase(), name);
        }

        final FlexTable layout = new FlexTable();
        layout.setCellPadding(5);
        layout.setHTML(0, 0, "<h3>1. Create service identity</h3>");
        layout.getFlexCellFormatter().setColSpan(0, 0, 3);
        layout.setHTML(1, 0, "<strong>Member's name: </strong>");
        layout.setWidget(1, 1, serviceUserName);
        layout.setHTML(2, 0, "<strong>Member's email: </strong>");
        layout.setWidget(2, 1, serviceUserEmail);
        layout.setHTML(3, 0, "<strong>Namespace: </strong>");
        layout.setWidget(3, 1, namespace);
        layout.setHTML(4, 0, "<strong>Login: </strong>");
        layout.setWidget(4, 1, serviceUserLogin);

        final FlexTable firstTabLayout = new FlexTable();
        firstTabLayout.setSize("100%", "100%");
        firstTabLayout.setVisible(false);

        final AddRemoveItemsTable<User> itemsTable = new AddRemoveItemsTable<User>(true);

        final VerticalPanel secondTabPanel = new VerticalPanel();
        secondTabPanel.setSize("100%", "100%");
        secondTabPanel.setVisible(false);

        mainTab.add(layout);
        mainTab.add(firstTabLayout);
        mainTab.add(secondTabPanel);

        layout.getFlexCellFormatter().setHorizontalAlignment(5, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        final CustomButton cb = new CustomButton("Continue", SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {

                // check
                if (!loginValidator.validateTextBox()) return;
                if (!nameValidator.validateTextBox()) return;
                if (!emailValidator.validateTextBox()) return;

                // change to lager tab
                session.getTabManager().changeStyleOfInnerTab(true);

                // first tab panel
                firstTabLayout.setHTML(0, 0, "<h3>2. Associate real users</h3>");
                firstTabLayout.getFlexCellFormatter().setColSpan(0, 0, 2);

                layout.setVisible(false);
                firstTabLayout.setVisible(true);

                final FindUsers callback = new FindUsers();

                // HORIZONTAL MENU
                TabMenu tabMenu = new TabMenu();

                // get the table
                final CellTable<User> table = callback.getTable();

                // search textbox
                ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
                    @Override
                    public void searchFor(String text) {
                        callback.searchFor(text);
                        searchString = text;
                    }
                }, ButtonTranslation.INSTANCE.searchUsers());

                final CustomButton cb = new CustomButton("Continue", SmallIcons.INSTANCE.arrowRightIcon());
                cb.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent clickEvent) {

                        // check
                        if (itemsTable.getList().isEmpty()) {
                            new Confirm("No user associated",new HTML("You must associate at least one real user to service member."), true).show();
                            return;
                        }

                        // create member + user
                        CreateServiceMember request = new CreateServiceMember(JsonCallbackEvents.disableButtonEvents(cb, new JsonCallbackEvents(){
                            public void onFinished(JavaScriptObject jso){

                                final Member member = jso.cast();

                                // change to small tab
                                session.getTabManager().changeStyleOfInnerTab(false);

                                secondTabPanel.add(new HTML("<h3>3.Set password for: "+serviceUserLogin.getTextBox().getValue().trim()+"</h3>"));

                                final CustomButton button = new CustomButton("Set password", SmallIcons.INSTANCE.keyIcon());
                                button.addClickHandler(new ClickHandler() {
                                    public void onClick(ClickEvent clickEvent) {

                                        if (!validator.validateTextBox() && !validator2.validateTextBox()) {
                                            return;
                                        }

                                        // create password which sets also user ext sources
                                        CreatePassword req = new CreatePassword(JsonCallbackEvents.disableButtonEvents(button, new JsonCallbackEvents(){
                                            public void onFinished(JavaScriptObject jso) {
                                                // validate member when all kerberos logins are set
                                                ValidateMemberAsync req2 = new ValidateMemberAsync(JsonCallbackEvents.closeTabDisableButtonEvents(button, tab));
                                                req2.validateMemberAsync(member);
                                            }
                                        }));
                                        req.createPassword(member.getUserId(), serviceUserLogin.getTextBox().getValue().trim(), namespace.getValue(namespace.getSelectedIndex()), serviceUserPassword.getTextBox().getValue().trim());

                                    }
                                });

                                final CustomButton skipButton = new CustomButton("Skip", SmallIcons.INSTANCE.arrowRightIcon());
                                skipButton.addClickHandler(new ClickHandler() {
                                    public void onClick(ClickEvent clickEvent) {
                                        CreatePassword req = new CreatePassword(JsonCallbackEvents.disableButtonEvents(skipButton, new JsonCallbackEvents(){
                                            public void onFinished(JavaScriptObject jso) {
                                                // validate member when all kerberos logins are set
                                                ValidateMemberAsync req2 = new ValidateMemberAsync(JsonCallbackEvents.closeTabDisableButtonEvents(skipButton, tab));
                                                req2.validateMemberAsync(member);
                                            }
                                        }));
                                        // set empty password for service member if "skipped"
                                        req.createRandomPassword(member.getUserId(), serviceUserLogin.getTextBox().getValue().trim(), namespace.getValue(namespace.getSelectedIndex()));
                                    }
                                });

                                FlexTable ft = new FlexTable();
                                ft.setStyleName("inputFormFlexTable");
                                ft.setWidth("400px");
                                ft.setHTML(0, 0, "Password:");
                                ft.setWidget(0, 1, serviceUserPassword);
                                ft.getFlexCellFormatter().setStyleName(0, 0, "itemName");
                                ft.setHTML(1, 0, "Re-type password:");
                                ft.setWidget(1, 1, serviceUserPassword2);
                                ft.getFlexCellFormatter().setStyleName(1, 0, "itemName");

                                ft.setWidget(2, 1, skipButton);
                                ft.getFlexCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
                                ft.setWidget(3, 1, button);
                                ft.getFlexCellFormatter().setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_RIGHT);

                                secondTabPanel.add(ft);

                                firstTabLayout.setVisible(false); // hide 2nd panel
                                secondTabPanel.setVisible(true); // show 3rd panel

                            };
                        }));

                        request.createMember(voId, serviceUserName.getTextBox().getValue().trim(), serviceUserEmail.getTextBox().getValue().trim(), itemsTable.getList(), namespace.getValue(namespace.getSelectedIndex()), serviceUserLogin.getTextBox().getValue().trim());

                    }
                });
                cb.setEnabled(false);

                CustomButton button = TabMenu.getPredefinedButton(ButtonType.ADD, "Add selected users to service member");
                button.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent clickEvent) {
                        ArrayList<User> list = callback.getTableSelectedList();
                        if (UiElements.cantSaveEmptyListDialogBox(list)) {
                            itemsTable.addItems(list);
                            cb.setEnabled(true);
                            callback.clearTableSelectedSet();
                        }
                    }
                });
                button.setEnabled(false);
                tabMenu.addWidget(button);
                JsonUtils.addTableManagedButton(callback, table, button);

                // add finish button to menu
                tabMenu.addWidget(cb);

                // if some text has been searched before
                if(!searchString.equals(""))
                {
                    searchBox.getTextBox().setText(searchString);
                    callback.searchFor(searchString);
                }

                final ScrollPanel sp = new ScrollPanel(table);
                table.addStyleName("perun-table");
                sp.addStyleName("perun-tableScrollPanel");

                session.getUiElements().resizeSmallTabPanel(sp, 350, tab);

                firstTabLayout.setWidget(1, 0,tabMenu);
                firstTabLayout.setWidget(2, 0, sp);
                firstTabLayout.setHTML(1, 1,"<h3>To be associated:</h3>");
                firstTabLayout.setWidget(2, 1, itemsTable);
                firstTabLayout.getFlexCellFormatter().setWidth(2, 0, "75%");
                firstTabLayout.getFlexCellFormatter().setWidth(2, 1, "25%");
                firstTabLayout.getFlexCellFormatter().setVerticalAlignment(2, 1, HasVerticalAlignment.ALIGN_TOP);
                firstTabLayout.getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);

                // actions when added items or removed items
                itemsTable.setEvents(new AddRemoveItemsTable.HandleItemsAction() {
                    @Override
                    public void onAdd() {
                        cb.setEnabled(true);
                    }
                    @Override
                    public void onRemove() {
                        if (itemsTable.getList().isEmpty()) {
                            cb.setEnabled(false);
                        }
                    }
                });

            }
        });

        // check login availability
        serviceUserLogin.getTextBox().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                if (keyUpEvent.isDownArrow() || keyUpEvent.isUpArrow() || keyUpEvent.isLeftArrow() || keyUpEvent.isRightArrow()) {
                    // do not trigger when no text input
                    return;
                }
                final String login = serviceUserLogin.getTextBox().getValue().trim();
                final String loginNamespace = namespace.getValue(namespace.getSelectedIndex());
                // trigger new validation on checked input or if previously was hard error
                if ((!login.isEmpty() && RegExp.compile(Utils.LOGIN_VALUE_MATCHER).test(login)) || serviceUserLogin.isHardError()) {
                    new IsLoginAvailable(loginNamespace, login, new JsonCallbackEvents(){
                        @Override
                        public void onFinished(JavaScriptObject jso) {
                            // UPDATE RESULT ONLY IF CONTENT OF LOGIN BOX IS SAME AS ON CALLBACK START
                            if (serviceUserLogin.getTextBox().getValue().trim().equals(login)) {
                                BasicOverlayType bo = jso.cast();
                                serviceUserLogin.setProcessing(false);
                                if (!bo.getBoolean()) {
                                    serviceUserLogin.setHardError("Login is already in use!");
                                } else {
                                    serviceUserLogin.removeHardError();
                                    loginValidator.validateTextBox();
                                }
                            }
                        }
                        @Override
                        public void onLoadingStart(){
                            if (serviceUserLogin.getTextBox().getValue().trim().equals(login)) {
                                serviceUserLogin.removeHardError();
                                serviceUserLogin.setProcessing(true);
                            }
                        }
                        @Override
                        public void onError(PerunError error) {
                            if (serviceUserLogin.getTextBox().getValue().trim().equals(login)) {
                                serviceUserLogin.setProcessing(false);
                                serviceUserLogin.setHardError("Unable to check if login is available!");
                            }
                        }
                    }).retrieveData();
                }
            }
        });

        namespace.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                final String login = serviceUserLogin.getTextBox().getValue().trim();
                final String loginNamespace = namespace.getValue(namespace.getSelectedIndex());
                if ((!login.isEmpty() && RegExp.compile(Utils.LOGIN_VALUE_MATCHER).test(login)) || serviceUserLogin.isHardError()) {
                    new IsLoginAvailable(loginNamespace, login, new JsonCallbackEvents(){
                        @Override
                        public void onFinished(JavaScriptObject jso) {
                            // UPDATE RESULT ONLY IF CONTENT OF LOGIN BOX IS SAME AS ON CALLBACK START
                            if (serviceUserLogin.getTextBox().getValue().trim().equals(login)) {
                                serviceUserLogin.setProcessing(false);
                                BasicOverlayType bo = jso.cast();
                                if (!bo.getBoolean()) {
                                    serviceUserLogin.setHardError("Login is already in use!");
                                } else {
                                    serviceUserLogin.removeHardError();
                                    loginValidator.validateTextBox();
                                }
                            }
                        }
                        @Override
                        public void onLoadingStart(){
                            if (serviceUserLogin.getTextBox().getValue().trim().equals(login)) {
                                serviceUserLogin.removeHardError();
                                serviceUserLogin.setProcessing(true);
                                loginValidator.validateTextBox();
                            }
                        }
                        @Override
                        public void onError(PerunError error) {
                            if (serviceUserLogin.getTextBox().getValue().trim().equals(login)) {
                                serviceUserLogin.setProcessing(false);
                                serviceUserLogin.setHardError("Unable to check if login is available!");
                            }
                        }
                    }).retrieveData();
                }
            }
        });

        layout.setWidget(5, 0, cb);
        layout.getFlexCellFormatter().setColSpan(5, 0, 2);

        this.contentWidget.setWidget(mainTab);

        return getWidget();
    }

    public Widget getWidget() {
        return this.contentWidget;
    }

    public Widget getTitle() {
        return this.titleWidget;
    }

    public ImageResource getIcon() {
        return SmallIcons.INSTANCE.addIcon();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + voId;
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
        CreateServiceMemberInVoTabItem other = (CreateServiceMemberInVoTabItem) obj;
        if (voId != other.voId)
            return false;
        return true;
    }

    public boolean multipleInstancesEnabled() {
        return false;
    }

    public void open() {
        session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
        if(vo != null){
            session.setActiveVo(vo);
            return;
        }
        session.setActiveVoId(voId);
    }

    public boolean isAuthorized() {

        if (session.isVoAdmin(voId)) {
            return true;
        } else {
            return false;
        }

    }

    public final static String URL = "add-service-member";

    public String getUrl()
    {
        return URL;
    }

    public String getUrlWithParameters() {
        return MembersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?vo=" + voId;
    }

    static public CreateServiceMemberInVoTabItem load(Map<String, String> parameters) {
        int gid = Integer.parseInt(parameters.get("vo"));
        return new CreateServiceMemberInVoTabItem(gid);
    }

}