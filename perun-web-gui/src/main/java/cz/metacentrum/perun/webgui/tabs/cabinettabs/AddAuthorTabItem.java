package cz.metacentrum.perun.webgui.tabs.cabinettabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.cabinetManager.CreateAuthorship;
import cz.metacentrum.perun.webgui.json.cabinetManager.FindNewAuthors;
import cz.metacentrum.perun.webgui.model.Author;
import cz.metacentrum.perun.webgui.model.Publication;
import cz.metacentrum.perun.webgui.tabs.CabinetTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab for adding author to publication
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddAuthorTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Add author");
	private String searchString = "";
	private FindNewAuthors users;
	private Publication publication;
	private JsonCallbackEvents events;
	private int publicationId;

	private HTML alreadyAddedAuthors = new HTML("");

	/**
	 * Creates a tab instance
	 *
	 * @param publication
	 */
	public AddAuthorTabItem(Publication publication){
		this.publication = publication;
		this.publicationId = publication.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param publication
	 * @param events
	 */
	public AddAuthorTabItem(Publication publication, JsonCallbackEvents events){
		this.publication = publication;
		this.publicationId = publication.getId();
		this.events = events;
	}

	/**
	 * Creates a tab instance
	 *
	 * @param publicationId
	 */
	public AddAuthorTabItem(int publicationId){
		this.publicationId = publicationId;
		new GetEntityById(PerunEntity.PUBLICATION, publicationId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				publication = jso.cast();
			}
		}).retrieveData();
	}


	public boolean isPrepared() {
		return !(publication == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {
		// trigger refresh of sub-tab via event
		if (events != null) events.onFinished(null);
	}

	public Widget draw() {


		titleWidget.setText("Add author");

		this.users = new FindNewAuthors("");

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// PUB-INFO
		TabMenu info = new TabMenu();
		info.addWidget(new HTML("<strong>FULL&nbsp;CITE: </strong>"+publication.getMain()));
		firstTabPanel.add(info);

		// HORIZONTAL MENU
		TabMenu tabMenu = new TabMenu();

		// get the table
		final CellTable<Author> table;
		if (session.isPerunAdmin()) {
			table = users.getEmptyTable(new FieldUpdater<Author, String>() {
				public void update(int index, Author object, String value) {
					session.getTabManager().addTab(new UserDetailTabItem(object.getId()));
				}
			});
		} else {
			table = users.getEmptyTable();
		}

		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, "Add selected user(s) as author(s) of publication: " + publication.getTitle());
		tabMenu.addWidget(addButton);

		addButton.setEnabled(false);
		JsonUtils.addTableManagedButton(users, table, addButton);

		final TabItem tab = this;

		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ArrayList<Author> list = users.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					// proceed
					for (int i=0; i<list.size(); i++ ) {
						final String name = list.get(i).getDisplayName();
						// add name events
						JsonCallbackEvents authorshipEvents = new JsonCallbackEvents(){
							public void onFinished(JavaScriptObject jso){
								updateAlreadyAdded(name);
							}
						};
						// merge with refresh?
						if (i == list.size()-1 && events != null) {
							authorshipEvents = JsonCallbackEvents.mergeEvents(authorshipEvents, events);
						}
						// call
						CreateAuthorship request = new CreateAuthorship(JsonCallbackEvents.disableButtonEvents(addButton, authorshipEvents));
						request.createAuthorship(publicationId, list.get(i).getId());
						if (i == list.size()-1) {
							users.clearTableSelectedSet();
						}
					}
				}
			}
		});

		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		tabMenu.addSearchWidget(new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				users.searchFor(text);
				searchString = text;
			}
		}, ButtonTranslation.INSTANCE.searchUsers());

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add menu to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");

		// add already added
		firstTabPanel.add(alreadyAddedAuthors);
		firstTabPanel.setCellHeight(alreadyAddedAuthors, "30px");

		// add table to the main panel
		firstTabPanel.add(sp);

		// do not resize like perun table to prevent wrong width in inner tab
		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

		this.contentWidget.setWidget(firstTabPanel);

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

	protected void updateAlreadyAdded(String newlyAdded)
	{
		String text = alreadyAddedAuthors.getHTML();
		if(text.length() == 0){
			text += "<strong>Added:</strong> ";
		}else{
			text += ", ";
		}

		text += SafeHtmlUtils.fromString(newlyAdded).asString();
		alreadyAddedAuthors.setHTML(text);
	}

	/**
	 * Starts the search for users
	 */
	protected void startSearching(String text){
		users.searchFor(text);
	}


	@Override
	public int hashCode() {
		final int prime = 571;
		int result = 1;
		result = prime * result + publicationId;
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
		AddAuthorTabItem other = (AddAuthorTabItem) obj;
		if (publicationId != other.publicationId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {

	}

	public boolean isAuthorized() {
		return true;
	}

	public final static String URL = "add-auth-to-pbl";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return CabinetTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?publication=" + publicationId;
	}

	static public AddAuthorTabItem load(Map<String, String> parameters)
	{
		int pubId = Integer.parseInt(parameters.get("publication"));
		return new AddAuthorTabItem(pubId);
	}


}
