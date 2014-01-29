package cz.metacentrum.perun.webgui.tabs.vostabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.authzResolver.AddAdmin;
import cz.metacentrum.perun.webgui.json.usersManager.FindCompleteRichUsers;
import cz.metacentrum.perun.webgui.json.usersManager.FindUsersByIdsNotInRpc;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * !! USE AS INNER TAB ONLY !!
 *
 * Provides page with add admin to VO form
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class AddVoManagerTabItem implements TabItem {

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
	private Label titleWidget = new Label("Add VO manager");
	
	/**
	 * Entity ID to set
	 */
	private int voId = 0;
	private VirtualOrganization vo;
    private FindCompleteRichUsers users;
    private boolean somebodyAdded = false;

	private String searchString = "";
	
	final SimplePanel pageWidget = new SimplePanel();
	
	
	/**
	 * Creates a tab instance
	 *
     * @param voId ID of VO to add admin into
     */
	public AddVoManagerTabItem(int voId){
		this.voId = voId;
        JsonCallbackEvents events = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                vo = jso.cast();
            }
        };
        new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param vo VO to add admin into
     */
	public AddVoManagerTabItem(VirtualOrganization vo){
		this.voId = vo.getId();
		this.vo = vo;
	}
	
	
	public boolean isPrepared(){
		return vo != null;
	}
	
	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName())+": add manager");

        final CustomButton searchButton = new CustomButton("Search", ButtonTranslation.INSTANCE.searchUsers(), SmallIcons.INSTANCE.findIcon());

        this.users = new FindCompleteRichUsers("", null, JsonCallbackEvents.disableButtonEvents(searchButton));

        // MAIN TAB PANEL
        VerticalPanel firstTabPanel = new VerticalPanel();
        firstTabPanel.setSize("100%", "100%");

        // HORIZONTAL MENU
        TabMenu tabMenu = new TabMenu();

        // get the table
        final CellTable<User> table;
        if (session.isPerunAdmin()) {
            table = users.getTable(new FieldUpdater<User, String>() {
                public void update(int i, User user, String s) {
                    session.getTabManager().addTab(new UserDetailTabItem(user));
                }
            });
        } else {
            table = users.getTable();
        }

        // already added
        final SimplePanel alreadyAdded = new SimplePanel();
        alreadyAdded.setStyleName("alreadyAdded");
        alreadyAdded.setWidget(new HTML("<strong>Already added: </strong>"));
        alreadyAdded.setVisible(false);

        final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedManagersToVo());
        final TabItem tab = this;

        // search textbox
        final ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
            @Override
            public void searchFor(String text) {
                startSearching(text);
                searchString = text;
            }
        }, searchButton);

        tabMenu.addWidget(addButton);

        tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, somebodyAdded);
            }
        }));

        addButton.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                final ArrayList<User> list = users.getTableSelectedList();
                if (UiElements.cantSaveEmptyListDialogBox(list)){
                    for (int i=0; i<list.size(); i++) {
                        // FIXME - Should have only one callback to core
                        final int n = i;
                        AddAdmin request = new AddAdmin(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents(){
                            @Override
                            public void onFinished(JavaScriptObject jso) {
                                // put names to already added
                                alreadyAdded.setVisible(true);
                                alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML() + list.get(n).getFullName() + ", ");
                                // unselect added person
                                users.getSelectionModel().setSelected(list.get(n), false);
                                // clear search
                                searchBox.getTextBox().setText("");
                                somebodyAdded = true;
                            }
                        }));
                        request.addVoAdmin(vo, list.get(i));
                    }
                }
            }
        });

        // if some text has been searched before
        if(!searchString.equals(""))
        {
            searchBox.getTextBox().setText(searchString);
            startSearching(searchString);
        }

        /*
        Button b1 = tabMenu.addButton("List all", SmallIcons.INSTANCE.userGrayIcon(), new ClickHandler(){
            public void onClick(ClickEvent event) {
                GetCompleteRichUsers callback = new GetCompleteRichUsers(new JsonCallbackEvents(){
                    public void onLoadingStart() {
                        table.setEmptyTableWidget(new AjaxLoaderImage().loadingStart());
                    }
                    public void onFinished(JavaScriptObject jso) {
                        users.clearTable();
                        JsArray<User> usrs = JsonUtils.<User>jsoAsArray(jso);
                        for (int i=0; i<usrs.length(); i++){
                            users.addToTable(usrs.get(i));
                        }
                        users.sortTable();
                    }
                });
                callback.retrieveData();
            }
        });
        b1.setTitle("List of all users in Perun");
        */

        addButton.setEnabled(false);
        JsonUtils.addTableManagedButton(users, table, addButton);

        // add a class to the table and wrap it into scroll panel
        table.addStyleName("perun-table");
        ScrollPanel sp = new ScrollPanel(table);
        sp.addStyleName("perun-tableScrollPanel");

        // add menu and the table to the main panel
        firstTabPanel.add(tabMenu);
        firstTabPanel.setCellHeight(tabMenu, "30px");
        firstTabPanel.add(alreadyAdded);
        firstTabPanel.add(sp);

        session.getUiElements().resizePerunTable(sp, 350, this);

        this.contentWidget.setWidget(firstTabPanel);

        return getWidget();

    }

    /**
     * Starts the search for users
     */
    protected void startSearching(String text){

        users.clearTable();

        // IS searched string IDs?
        if (JsonUtils.isStringWithIds(text)) {

            FindUsersByIdsNotInRpc req = new FindUsersByIdsNotInRpc(new JsonCallbackEvents(){

                public void onFinished(JavaScriptObject jso){

                    ArrayList<User> usersList = JsonUtils.jsoAsList(jso);
                    for(User u : usersList)
                    {
                        users.addToTable(u);
                    }
                }

            }, text);

            req.retrieveData();
            return;
        }


        users.searchFor(text);
    }


    private void setPageWidget(Widget w)
	{
		this.pageWidget.setWidget(w);

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
		result = prime * result + 6786786;
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

        AddVoManagerTabItem create = (AddVoManagerTabItem) obj;
		if (voId != create.voId){
			return false;
		}
		
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}
	
	public void open() {
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId)) {
			return true; 
		} else {
			return false;
		}

	}

}