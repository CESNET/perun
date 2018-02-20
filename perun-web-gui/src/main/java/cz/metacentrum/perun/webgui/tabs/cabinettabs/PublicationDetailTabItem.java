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
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.cabinetManager.*;
import cz.metacentrum.perun.webgui.model.Author;
import cz.metacentrum.perun.webgui.model.Category;
import cz.metacentrum.perun.webgui.model.Publication;
import cz.metacentrum.perun.webgui.model.Thanks;
import cz.metacentrum.perun.webgui.tabs.CabinetTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Tab which shows publication's details.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class PublicationDetailTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading publication");

	//data
	private Publication publication;
	private int publicationId;
	private boolean fromSelf = false; // accessed from perun admin by default

	/**
	 * Creates a tab instance
	 *
	 * @param pub publication
	 */
	public PublicationDetailTabItem(Publication pub){
		this.publication = pub;
		this.publicationId = pub.getId();
	}

	/**
	 * Creates a tab instance
	 * @param pub publication
	 * @param fromSelf TRUE if accessed from user section / FALSE otherwise
	 */
	public PublicationDetailTabItem(Publication pub, boolean fromSelf){
		this.publication = pub;
		this.publicationId = pub.getId();
		this.fromSelf = fromSelf;
	}

	/**
	 * Creates a tab instance
	 * @param publicationId publication
	 * @param fromSelf TRUE if accessed from user section / FALSE otherwise
	 */
	public PublicationDetailTabItem(int publicationId, boolean fromSelf){
		this.publicationId = publicationId;
		this.fromSelf = fromSelf;
		GetEntityById call = new GetEntityById(PerunEntity.PUBLICATION, publicationId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				publication = jso.cast();
			}
		});
		// do not use cache this time because of update publ. method  !!
		call.retrieveData();
	}

	public boolean isPrepared(){
		return !(publication == null);
	}

	public Widget draw() {

		// show only part of title
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(publication.getTitle()));

		// MAIN PANEL
		ScrollPanel sp = new ScrollPanel();
		sp.addStyleName("perun-tableScrollPanel");

		VerticalPanel vp = new VerticalPanel();
		vp.addStyleName("perun-table");
		sp.add(vp);

		// resize perun table to correct size on screen
		session.getUiElements().resizePerunTable(sp, 350, this);

		// content
		final FlexTable ft = new FlexTable();
		ft.setStyleName("inputFormFlexTable");

		if (publication.getLocked() == false) {

			ft.setHTML(1, 0, "Id / Origin:");
			ft.setHTML(2, 0, "Title:");
			ft.setHTML(3, 0, "Year:");
			ft.setHTML(4, 0, "Category:");
			ft.setHTML(5, 0, "Rank:");
			ft.setHTML(6, 0, "ISBN / ISSN:");
			ft.setHTML(7, 0, "DOI:");
			ft.setHTML(8, 0, "Full cite:");
			ft.setHTML(9, 0, "Created by:");
			ft.setHTML(10, 0, "Created date:");

			for (int i=0; i<ft.getRowCount(); i++) {
				ft.getFlexCellFormatter().setStyleName(i, 0, "itemName");
			}
			ft.getFlexCellFormatter().setWidth(1, 0, "100px");

			final ListBoxWithObjects<Category> listbox = new ListBoxWithObjects<Category>();
			// fill listbox
			JsonCallbackEvents events = new JsonCallbackEvents(){
				public void onFinished(JavaScriptObject jso) {
					for (Category cat : JsonUtils.<Category>jsoAsList(jso)){
						listbox.addItem(cat);
						// if right, selected
						if (publication.getCategoryId() == cat.getId()) {
							listbox.setSelected(cat, true);
						}
					}
				}
			};
			GetCategories categories = new GetCategories(events);
			categories.retrieveData();

			final TextBox rank = new TextBox();
			rank.setWidth("30px");
			rank.setMaxLength(4);
			rank.setText(String.valueOf(publication.getRank()));

			final TextBox title = new TextBox();
			title.setMaxLength(1024);
			title.setText(publication.getTitle());
			title.setWidth("500px");
			final TextBox year = new TextBox();
			year.setText(String.valueOf(publication.getYear()));
			year.setMaxLength(4);
			year.setWidth("30px");
			final TextBox isbn = new TextBox();
			isbn.setText(publication.getIsbn());
			isbn.setMaxLength(32);
			final TextBox doi = new TextBox();
			doi.setText(publication.getDoi());
			doi.setMaxLength(256);
			final TextArea main = new TextArea();
			main.setText(publication.getMain());
			main.setSize("500px", "70px");
			// set max length
			main.getElement().setAttribute("maxlength", "4000");

			ft.setHTML(1, 1, publication.getId()+" / <Strong>Ext. Id: </strong>"+publication.getExternalId()+" <Strong>System: </strong>"+ SafeHtmlUtils.fromString(publication.getPublicationSystemName()).asString());
			ft.setWidget(2, 1, title);
			ft.setWidget(3, 1, year);
			ft.setWidget(4, 1, listbox);
			if (session.isPerunAdmin()) {
				// only perunadmin can change rank
				ft.setWidget(5, 1, rank);
			} else {
				ft.setHTML(5, 1, SafeHtmlUtils.fromString(String.valueOf(publication.getRank()) +"").asString());
			}
			ft.setWidget(6, 1, isbn);
			ft.setWidget(7, 1, doi);
			ft.setWidget(8, 1, main);
			ft.setHTML(9, 1, SafeHtmlUtils.fromString((publication.getCreatedBy() != null) ? publication.getCreatedBy() : "").asString());
			ft.setHTML(10, 1, SafeHtmlUtils.fromString((String.valueOf(publication.getCreatedDate()) != null) ? String.valueOf(publication.getCreatedDate()) : "").asString());

			// update button

			final CustomButton change = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes in publication details");
			change.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {

					Publication pub = JsonUtils.clone(publication).cast();

					if (!JsonUtils.checkParseInt(year.getText())){
						JsonUtils.cantParseIntConfirm("YEAR", year.getText());
					} else {
						pub.setYear(Integer.parseInt(year.getText()));
					}
					if (session.isPerunAdmin()) {
						pub.setRank(Double.parseDouble(rank.getText()));
					}
					pub.setCategoryId(listbox.getSelectedObject().getId());
					pub.setTitle(title.getText());
					pub.setMain(main.getText());
					pub.setIsbn(isbn.getText());
					pub.setDoi(doi.getText());

					UpdatePublication upCall = new UpdatePublication(JsonCallbackEvents.disableButtonEvents(change, new JsonCallbackEvents(){
						public void onFinished(JavaScriptObject jso) {
							// refresh page content
							Publication p = jso.cast();
							publication = p;
							draw();
						}
					}));
					upCall.updatePublication(pub);

				}
			});

			ft.setWidget(0, 0, change);

		} else {

			ft.getFlexCellFormatter().setColSpan(0, 0, 2);
			ft.setWidget(0, 0, new HTML(new Image(SmallIcons.INSTANCE.lockIcon())+" <strong>Publication is locked. Ask administrator to perform any changes for you at meta@cesnet.cz.</strong>"));

			ft.setHTML(1, 0, "Id / Origin:");
			ft.setHTML(2, 0, "Title:");
			ft.setHTML(3, 0, "Year:");
			ft.setHTML(4, 0, "Category:");
			ft.setHTML(5, 0, "Rank:");
			ft.setHTML(6, 0, "ISBN / ISSN:");
			ft.setHTML(7, 0, "DOI:");
			ft.setHTML(8, 0, "Full cite:");
			ft.setHTML(9, 0, "Created by:");
			ft.setHTML(10, 0, "Created date:");

			for (int i=0; i<ft.getRowCount(); i++) {
				ft.getFlexCellFormatter().setStyleName(i, 0, "itemName");
			}
			ft.getFlexCellFormatter().setWidth(1, 0, "100px");

			ft.setHTML(1, 1, publication.getId()+" / <Strong>Ext. Id: </strong>"+publication.getExternalId()+" <Strong>System: </strong>"+SafeHtmlUtils.fromString(publication.getPublicationSystemName()).asString());
			ft.setHTML(2, 1, SafeHtmlUtils.fromString((publication.getTitle() != null) ? publication.getTitle() : "").asString());
			ft.setHTML(3, 1, SafeHtmlUtils.fromString((String.valueOf(publication.getYear()) != null) ? String.valueOf(publication.getYear()) : "").asString());
			ft.setHTML(4, 1, SafeHtmlUtils.fromString((publication.getCategoryName() != null) ? publication.getCategoryName() : "").asString());
			ft.setHTML(5, 1, SafeHtmlUtils.fromString(String.valueOf(publication.getRank()) + " (default is 0)").asString());
			ft.setHTML(6, 1, SafeHtmlUtils.fromString((publication.getIsbn() != null) ? publication.getIsbn() : "").asString());
			ft.setHTML(7, 1, SafeHtmlUtils.fromString((publication.getDoi() != null) ? publication.getDoi() : "").asString());
			ft.setHTML(8, 1, SafeHtmlUtils.fromString((publication.getMain() != null) ? publication.getMain() : "").asString());
			ft.setHTML(9, 1, SafeHtmlUtils.fromString((publication.getCreatedBy() != null) ? publication.getCreatedBy() : "").asString());
			ft.setHTML(10, 1, SafeHtmlUtils.fromString((String.valueOf(publication.getCreatedDate()) != null) ? String.valueOf(publication.getCreatedDate()) : "").asString());

		}

		// LOCK / UNLOCK button for PerunAdmin

		if (session.isPerunAdmin()) {
			final CustomButton lock;
			if (publication.getLocked()) {
				lock = new CustomButton("Unlock", "Allow editing of publication details (for users).", SmallIcons.INSTANCE.lockOpenIcon());
				ft.setWidget(0, 0, lock);
				ft.getFlexCellFormatter().setColSpan(0, 0, 1);
				ft.setWidget(0, 1, new HTML(new Image(SmallIcons.INSTANCE.lockIcon())+" Publication is locked."));
			} else {
				lock = new CustomButton("Lock", "Deny editing of publication details (for users).", SmallIcons.INSTANCE.lockIcon());
				ft.setWidget(0, 1, lock);
			}
			lock.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					LockUnlockPublications upCall = new LockUnlockPublications(JsonCallbackEvents.disableButtonEvents(lock, new JsonCallbackEvents(){
						public void onFinished(JavaScriptObject jso) {
							// refresh page content
							publication.setLocked(!publication.getLocked());
							draw();
						}
					}));
					Publication p = JsonUtils.clone(publication).cast();
					upCall.lockUnlockPublication(!publication.getLocked(), p);
				}
			});

		}

		DisclosurePanel dp = new DisclosurePanel();
		dp.setWidth("100%");
		dp.setContent(ft);
		dp.setOpen(true);

		FlexTable detailsHeader = new FlexTable();
		detailsHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.bookIcon()));
		detailsHeader.setHTML(0, 1, "<h3>Details</h3>");
		dp.setHeader(detailsHeader);

		vp.add(dp);
		vp.add(loadAuthorsSubTab());
		vp.add(loadThanksSubTab());

		this.contentWidget.setWidget(sp);

		return getWidget();

	}

	/**
	 * Returns widget with authors management for publication
	 *
	 * @return widget
	 */
	private Widget loadAuthorsSubTab(){

		DisclosurePanel dp = new DisclosurePanel();
		dp.setWidth("100%");
		dp.setOpen(true);
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");
		dp.setContent(vp);

		FlexTable header = new FlexTable();
		header.setWidget(0, 0, new Image(LargeIcons.INSTANCE.userGreenIcon()));
		header.setHTML(0, 1, "<h3>Authors / Reported by</h3>");
		dp.setHeader(header);

		// menu
		TabMenu menu = new TabMenu();

		// callback
		final FindAuthorsByPublicationId call = new FindAuthorsByPublicationId(publication.getId());
		call.setCheckable(false);

		if (!publication.getLocked()) {
			// editable if not locked
			vp.add(menu);
			vp.setCellHeight(menu, "30px");
			call.setCheckable(true);
		}

		final CustomButton addButton = new CustomButton("Add myself", "Add you as author of publication", SmallIcons.INSTANCE.addIcon());
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(call);
				CreateAuthorship request = new CreateAuthorship(JsonCallbackEvents.disableButtonEvents(addButton, events));
				request.createAuthorship(publicationId, session.getActiveUser().getId());
			}
		});
		menu.addWidget(addButton);

		CustomButton addOthersButton = new CustomButton("Add others", "Add more authors", SmallIcons.INSTANCE.addIcon());
		addOthersButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddAuthorTabItem(publication, JsonCallbackEvents.refreshTableEvents(call)), true);
			}
		});
		menu.addWidget(addOthersButton);

		// fill table
		CellTable<Author> table = call.getEmptyTable();
		call.retrieveData();

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove select author(s) from publication");
		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(call, table, removeButton);
		menu.addWidget(removeButton);

		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<Author> list = call.getTableSelectedList();
				String text = "Following users will be removed from publication's authors. They will lose any benefit granted by publication's rank.";
				UiElements.showDeleteConfirm(list, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
						for(int i=0; i<list.size(); i++){
							// calls the request
							if (i == list.size()-1) {
								DeleteAuthorship request = new DeleteAuthorship(JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(call)));
								request.deleteAuthorship(publicationId, list.get(i).getId());
							} else {
								DeleteAuthorship request = new DeleteAuthorship();
								request.deleteAuthorship(publicationId, list.get(i).getId());
							}
						}
					}
				});
			}
		});

		ScrollPanel sp = new ScrollPanel();
		sp.add(table);
		table.addStyleName("perun-table");
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);

		return dp;

	}

	/**
	 * Returns thanks management widget for publication
	 *
	 * @return widget
	 */
	private Widget loadThanksSubTab(){

		DisclosurePanel dp = new DisclosurePanel();
		dp.setWidth("100%");
		dp.setOpen(true);
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");
		dp.setContent(vp);

		FlexTable header = new FlexTable();
		header.setWidget(0, 0, new Image(LargeIcons.INSTANCE.smallBusinessIcon()));
		header.setHTML(0, 1, "<h3>Acknowledgement</h3>");
		dp.setHeader(header);

		// menu
		TabMenu menu = new TabMenu();

		// callback
		final GetRichThanksByPublicationId thanksCall = new GetRichThanksByPublicationId(publicationId);
		thanksCall.setCheckable(false);

		if (!publication.getLocked()) {
			// editable if not locked
			vp.add(menu);
			vp.setCellHeight(menu, "30px");
			thanksCall.setCheckable(true);
		}

		CellTable<Thanks> table = thanksCall.getTable();

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, "Add acknowledgement to publication", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new CreateThanksTabItem(publication, JsonCallbackEvents.refreshTableEvents(thanksCall)), true);
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove acknowledgement from publication");
		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(thanksCall, table, removeButton);
		menu.addWidget(removeButton);

		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<Thanks> list = thanksCall.getTableSelectedList();
				String text = "Following acknowledgements will be removed from publication.";
				UiElements.showDeleteConfirm(list, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
						for(int i=0; i<list.size(); i++){
							// calls the request
							if (i == list.size()-1) {
								DeleteThanks request = new DeleteThanks(JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(thanksCall)));
								request.deleteThanks(list.get(i).getId());
							} else {
								DeleteThanks request = new DeleteThanks(JsonCallbackEvents.disableButtonEvents(removeButton));
								request.deleteThanks(list.get(i).getId());
							}
						}
					}
				});

			}
		});

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel();
		sp.add(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);

		return dp;

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.bookIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 613;
		int result = 1;
		result = prime * result * 22 * publicationId;
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
		PublicationDetailTabItem other = (PublicationDetailTabItem)obj;
		if (publicationId != other.publicationId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		if (fromSelf) {
			session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, "My publications", CabinetTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"userpubs?user=" + session.getUser().getId(), publication.getTitle(), getUrlWithParameters());
		} else {
			session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Publications", CabinetTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"all", publication.getTitle(), getUrlWithParameters());
		}
	}

	public boolean isAuthorized() {
		if (session.isSelf()) {
			return true;
		} else {
			return false;
		}
	}

	public final static String URL = "pbl";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return CabinetTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + publicationId + "&self="+fromSelf;
	}

	static public PublicationDetailTabItem load(Map<String, String> parameters) {
		int pubId = Integer.parseInt(parameters.get("id"));
		boolean fromSelf = Boolean.parseBoolean(parameters.get("self"));
		return new PublicationDetailTabItem(pubId, fromSelf);
	}

}
