package cz.metacentrum.perun.webgui.tabs.cabinettabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.cabinetManager.CreateThanks;
import cz.metacentrum.perun.webgui.json.ownersManager.GetOwners;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.model.Publication;
import cz.metacentrum.perun.webgui.tabs.CabinetTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Tab for adding new Thanks
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateThanksTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Add acknowledgement");

	// data
	private int publicationId;
	private Publication publication;
	private JsonCallbackEvents events;

	private HTML alreadyAddedOwners = new HTML("");

	/**
	 * Creates a tab instance
	 *
	 * @param publicationId id
	 */
	public CreateThanksTabItem(int publicationId){
		this.publicationId = publicationId;
		new GetEntityById(PerunEntity.PUBLICATION, publicationId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				publication = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param publication
	 */
	public CreateThanksTabItem(Publication publication){
		this.publication = publication;
		this.publicationId = publication.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param publication
	 * @param extEvents
	 */
	public CreateThanksTabItem(Publication publication, JsonCallbackEvents extEvents){
		this.publication = publication;
		this.publicationId = publication.getId();
		this.events = extEvents;
	}

	public boolean isPrepared(){
		return !(publication == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// MAIN PANEL
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// CALLBACK
		final GetOwners owners = new GetOwners();
		// FIXME - maybe make it configurable in a future
		List<String> names = Arrays.asList("MetaCentrum", "CERIT-SC", "ELIXIR");
		owners.setFilterByNames(names);

		// MENU
		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		// add button
		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, "Add acknowledgement for selected owner(s)");

		final TabItem tab = this;

		// click handler
		addButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				ArrayList<Owner> list = owners.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					for (int i=0; i<list.size(); i++) {
						final String name = list.get(i).getName();
						// add name events
						JsonCallbackEvents thanksEvents = new JsonCallbackEvents(){
							public void onFinished(JavaScriptObject jso){
								updateAlreadyAdded(name);
							}
						};
						// merge with refresh?
						if (i == list.size()-1 && events != null) {
							thanksEvents = JsonCallbackEvents.mergeEvents(thanksEvents, events);
						}
						CreateThanks request = new CreateThanks(publicationId, JsonCallbackEvents.disableButtonEvents(addButton, thanksEvents));
						request.createThanks(list.get(i).getId());
						if (i == list.size()-1) {
							owners.clearTableSelectedSet();
						}
					}
				}
			}
		});

		menu.addWidget(addButton);
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// trigger refresh of sub-tab via event
				events.onFinished(null);
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		// add already added
		vp.add(alreadyAddedOwners);
		vp.setCellHeight(alreadyAddedOwners, "30px");

		// TABLE
		owners.setFilterByType("administrative"); // show only administrative contacts
		CellTable<Owner> table = owners.getTable();
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel();
		sp.add(table);
		sp.addStyleName("perun-tableScrollPanel");
		vp.add(sp);
		// resize small tab panel to correct size on screen
		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

		addButton.setEnabled(false);
		JsonUtils.addTableManagedButton(owners, table, addButton);

		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	protected void updateAlreadyAdded(String newlyAdded)
	{
		String text = alreadyAddedOwners.getHTML();
		if(text.length() == 0){
			text += "<strong>Added:</strong> ";
		}else{
			text += ", ";
		}

		text += SafeHtmlUtils.fromString(newlyAdded).asString();
		alreadyAddedOwners.setHTML(text);
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
		final int prime = 607;
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
		CreateThanksTabItem other = (CreateThanksTabItem)obj;
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

		if (session.isSelf()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "create-thanks";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return CabinetTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?pubId=" + publicationId;
	}

	static public CreateThanksTabItem load(Map<String, String> parameters)
	{
		int publicationId = Integer.parseInt(parameters.get("pubId"));
		return new CreateThanksTabItem(publicationId);
	}

}
