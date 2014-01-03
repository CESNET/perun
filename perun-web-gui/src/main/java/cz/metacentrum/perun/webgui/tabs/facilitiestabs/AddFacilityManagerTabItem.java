package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

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
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Tab for adding new facility administrator
 * !! USE AS INNER TAB ONLY !!
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class AddFacilityManagerTabItem implements TabItem {

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
	private Label titleWidget = new Label("Loading facility");
	
	/**
	 * Search string
	 */
	private String searchString = "";
	private FindCompleteRichUsers users;
	
	// data
	private int facilityId;
	private Facility facility;
	
	/**
	 * Creates a tab instance
	 *
     * @param facility Facility
     */
	public AddFacilityManagerTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param facilityId ID of facility
     */
	public AddFacilityManagerTabItem(int facilityId){
		this.facilityId = facilityId;
		new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				facility = jso.cast();			
			}
		}).retrieveData();
	}
	
	public boolean isPrepared(){
		return !(facility == null);
	}
	
	public Widget draw() {
		
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+" ("+facility.getType()+"): add manager");
		
		this.users = new FindCompleteRichUsers("", null);

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu tabMenu = new TabMenu();

		// get the table
		final CellTable<User> table = users.getTable();

        final TabItem tab = this;
		
		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedManagersToFacility());
		addButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				ArrayList<User> list = users.getTableSelectedList();
                if (UiElements.cantSaveEmptyListDialogBox(list)){
                    // proceed
                    for (int i=0; i<list.size(); i++) {
                        if (i == list.size() - 1) {
                            AddAdmin request = new AddAdmin(PerunEntity.FACILITY, JsonCallbackEvents.closeTabDisableButtonEvents(addButton, tab));
                            request.addAdmin(facilityId, list.get(i).getId());
                        } else {
                            AddAdmin request = new AddAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(addButton));
                            request.addAdmin(facilityId, list.get(i).getId());
                        }
                    }
                }
			}
		});
        tabMenu.addWidget(addButton);

        tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

		// search textbox
		TextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				startSearching(text);
				searchString = text;
			}
		}, ButtonTranslation.INSTANCE.searchUsers());

		// if some text has been searched before
		if(!searchString.equals(""))
		{
			searchBox.setText(searchString);
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

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add menu and the table to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");
		firstTabPanel.add(sp);

        addButton.setEnabled(false);
        JsonUtils.addTableManagedButton(users, table, addButton);

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
		result = prime * result + facilityId;
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
        AddFacilityManagerTabItem other = (AddFacilityManagerTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}
	
	public void open()
	{
	}

	public boolean isAuthorized() {

		if (session.isFacilityAdmin(facilityId)) {
			return true; 
		} else {
			return false;
		}

	}
	
}