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
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.cabinetManager.DeleteAuthorship;
import cz.metacentrum.perun.webgui.json.cabinetManager.DeletePublication;
import cz.metacentrum.perun.webgui.json.cabinetManager.GetCategories;
import cz.metacentrum.perun.webgui.json.cabinetManager.FindPublicationsByGUIFilter;
import cz.metacentrum.perun.webgui.model.Category;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Publication;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Page with Publications management for users.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UsersPublicationsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading user's publications");

	// DATA
	private int userId;
	private User user;

	private String lastTitle = "";
	private boolean lastIsbnOrDoi = true; // DEFAULT TRUE = ISBN / FALSE = DOI
	private String lastIsbnOrDoiValue = "";
	private String lastYear = "";
	private String lastYearSince = "";
	private String lastYearTill= "";
	private int lastCategoryId = 0; // 0 = default "not selected"
	private Map<String, Object> lastIds = new HashMap<String, Object>();

	/**
	 * Creates a tab instance for self role
	 */
	public UsersPublicationsTabItem(){
		user = session.getUser();
		userId = user.getId();
	}

	/**
	 * Creates a tab instance with custom user
	 *
	 * @param user
	 */
	public UsersPublicationsTabItem(User user){
		this.user = user;
		userId = user.getId();
	}

	/**
	 * Creates a tab instance with custom user
	 *
	 * @param userId
	 */
	public UsersPublicationsTabItem(int userId){
		this.userId = userId;
		new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				user = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return !(user == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()) + ": Publications");

		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// CALLBACK
		final Map<String, Object> ids = new HashMap<String, Object>();
		ids.put("userId", user.getId());
		ids.put("authors", 1);

		final FindPublicationsByGUIFilter callback = new FindPublicationsByGUIFilter(ids);
		final JsonCallbackEvents refreshTable = JsonCallbackEvents.refreshTableEvents(callback);

		// GET CORRECT TABLE
		CellTable<Publication> table;
		table = callback.getTable(new FieldUpdater<Publication, String>() {
			public void update(int index, Publication object, String value) {
				session.getTabManager().addTab(new PublicationDetailTabItem(object, true));
			}
		});

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel();
		sp.add(table);
		sp.addStyleName("perun-tableScrollPanel");

		// ADD, REMOVE menu
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));
		// FILTER MENU
		final TabMenu filterMenu = new TabMenu();
		filterMenu.setVisible(false); // not visible at start

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Add new Publication", new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTab(new AddPublicationsTabItem(user));
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove selected publication(s)");
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<Publication> list = callback.getTableSelectedList();
				UiElements.showDeleteConfirm(list, "Following publications will be removed.", new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
						for (int i=0; i<list.size(); i++) {
							if (list.get(i).getLocked() && !session.isPerunAdmin()) {
								// skip locked pubs
								UiElements.generateAlert("Publication locked", "Publication <strong>" + SafeHtmlUtils.fromString(list.get(i).getTitle()).asString() + "</strong> is locked by " +
									"administrator and can't be deleted. Please notify administrator about your request.");
								continue;
							} else {

								if (list.get(i).getCreatedBy().equalsIgnoreCase(session.getPerunPrincipal().getActor())
									|| session.isPerunAdmin()
									|| session.getActiveUser().getId() == list.get(i).getCreatedByUid()) {
									// delete whole publication
									if (i == list.size()-1) {
										DeletePublication request = new DeletePublication(JsonCallbackEvents.disableButtonEvents(removeButton, refreshTable));
										request.deletePublication(list.get(i).getId());
									} else {
										DeletePublication request = new DeletePublication(JsonCallbackEvents.disableButtonEvents(removeButton));
										request.deletePublication(list.get(i).getId());
									}
								} else {
									// else delete only himself
									if (i == list.size()-1) {
										DeleteAuthorship request = new DeleteAuthorship(JsonCallbackEvents.disableButtonEvents(removeButton, refreshTable));
										request.deleteAuthorship(list.get(i).getId(), userId);
									} else {
										DeleteAuthorship request = new DeleteAuthorship(JsonCallbackEvents.disableButtonEvents(removeButton));
										request.deleteAuthorship(list.get(i).getId(), userId);
									}
								}
							}
						}
					}
				});
			}});
		menu.addWidget(removeButton);
		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(callback, table, removeButton);

		// fill category listbox
		final ListBoxWithObjects<Category> filterCategory = new ListBoxWithObjects<Category>();
		final GetCategories call = new GetCategories(new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				filterCategory.clear();
				filterCategory.addNotSelectedOption();
				ArrayList<Category> list = JsonUtils.jsoAsList(jso.cast());
				list = new TableSorter<Category>().sortByName(list);
				for (int i=0; i<list.size(); i++){
					filterCategory.addItem(list.get(i));
					// set last selected
					if (lastCategoryId != 0) {
						if (list.get(i).getId() == lastCategoryId) {
							filterCategory.setSelected(list.get(i), true);
						}
					}
				}
				if (lastCategoryId == 0) {
					filterCategory.setSelectedIndex(0);
				}
			}
			public void onError(PerunError error) {
				filterCategory.clear();
				filterCategory.removeNotSelectedOption();
				filterCategory.addItem("Error while loading");
			}
			@Override
			public void onLoadingStart() {
				filterCategory.clear();
				filterCategory.removeNotSelectedOption();
				filterCategory.addItem("Loading...");
			}
		});

		final CustomButton showButton = new CustomButton("Show / Hide filter", "Show / Hide filtering options", SmallIcons.INSTANCE.filterIcon());
		showButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				// showing and hiding filter menu
				if (filterMenu.isVisible()) {
					filterMenu.setVisible(false);
				} else {
					filterMenu.setVisible(true);
					if (filterCategory.isEmpty()) {
						call.retrieveData();
					}
				}
			}
		});
		menu.addWidget(showButton);

		// filter objects
		final TextBox filterTitle = new TextBox();
		filterTitle.setWidth("80px");
		filterTitle.setText(lastTitle);
		final TextBox filterYear = new TextBox();
		filterYear.setMaxLength(4);
		filterYear.setWidth("30px");
		filterYear.setText(lastYear);
		final TextBox filterIsbn = new TextBox();
		filterIsbn.setWidth("60px");
		filterIsbn.setText(lastIsbnOrDoiValue);
		final TextBox filterSince = new TextBox();
		filterSince.setMaxLength(4);
		filterSince.setWidth("30px");
		filterSince.setText(lastYearSince);
		final TextBox filterTill = new TextBox();
		filterTill.setMaxLength(4);
		filterTill.setWidth("30px");
		filterTill.setText(lastYearTill);

		final ListBox codeBox = new ListBox();
		codeBox.addItem("ISBN/ISSN:");
		codeBox.addItem("DOI:");
		if (!lastIsbnOrDoi) {
			codeBox.setSelectedIndex(1);
		}

		// buttons
		CustomButton applyFilter = new CustomButton("Apply", "Shows publications based on filter", SmallIcons.INSTANCE.filterAddIcon());
		CustomButton clearFilter = new CustomButton("Clear","Shows all publications of User",SmallIcons.INSTANCE.filterDeleteIcon());

		// clear filter action

		clearFilter.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {

				// clear last values
				lastCategoryId = 0;
				lastIsbnOrDoi = true;
				lastIsbnOrDoiValue = "";
				lastTitle = "";
				lastYear = "";
				lastYearTill = "";
				lastYearSince = "";

				filterTitle.setText("");
				filterYear.setText("");
				filterIsbn.setText("");
				filterSince.setText("");
				filterTill.setText("");
				filterCategory.setSelectedIndex(0);
				ids.clear();
				ids.put("userId", user.getId());
				ids.put("authors", 0);
				lastIds = ids;
				callback.setIds(ids);
				callback.clearTable();
				callback.retrieveData();

			}
		});

		// apply filter action

		applyFilter.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				// refresh ids
				ids.clear();
				ids.put("userId", user.getId());
				ids.put("authors", 1);

				// checks input
				if (!filterTitle.getText().isEmpty()) {
					ids.put("title", filterTitle.getText());
					lastTitle = filterTitle.getText();
				} else {
					lastTitle = "";
				}
				if (!filterYear.getText().isEmpty()) {
					if (!JsonUtils.checkParseInt(filterYear.getText())) {
						JsonUtils.cantParseIntConfirm("YEAR", filterYear.getText());
						lastYear = "";
						return;
					} else {
						ids.put("year", filterYear.getText());
						lastYear = filterYear.getText();
					}
				}
				if (!filterIsbn.getText().isEmpty()) {
					if (codeBox.getSelectedIndex() == 0) {
						// ISBN/ISSN selected
						lastIsbnOrDoi = true;
						ids.put("isbn", filterIsbn.getText());
					} else {
						// DOI selected
						lastIsbnOrDoi = false;
						ids.put("doi", filterIsbn.getText());
					}
					lastIsbnOrDoiValue = filterIsbn.getText();
				}
				if (!filterSince.getText().isEmpty()) {
					if (!JsonUtils.checkParseInt(filterSince.getText())) {
						JsonUtils.cantParseIntConfirm("YEAR SINCE", filterSince.getText());
						lastYearSince = "";
						return;
					} else {
						ids.put("yearSince", filterSince.getText());
						lastYearSince = filterSince.getText();
					}
				}
				if (!filterTill.getText().isEmpty()) {
					if (!JsonUtils.checkParseInt(filterTill.getText())) {
						JsonUtils.cantParseIntConfirm("YEAR TILL", filterTill.getText());
						lastYearTill = "";
						return;
					} else {
						ids.put("yearTill", filterTill.getText());
						lastYearTill = filterTill.getText();
					}
				}
				if (filterCategory.getSelectedIndex() > 0) {
					ids.put("category", filterCategory.getSelectedObject().getId());
					lastCategoryId = filterCategory.getSelectedObject().getId();
				} else {
					lastCategoryId = 0;
				}

				lastIds = ids;
				callback.setIds(ids);
				callback.clearTable();
				callback.retrieveData();

			}
		});

		// add to filter menu
		filterMenu.addWidget(new HTML("<strong>Title:</strong>"));
		filterMenu.addWidget(filterTitle);

		filterMenu.addWidget(codeBox);
		filterMenu.addWidget(filterIsbn);
		filterMenu.addWidget(new HTML("<strong>Category:</strong>"));
		filterMenu.addWidget(filterCategory);
		filterMenu.addWidget(new HTML("<strong>Year:</strong>"));
		filterMenu.addWidget(filterYear);
		filterMenu.addWidget(new HTML("<strong>Year&nbsp;between:</strong>"));
		filterMenu.addWidget(filterSince);
		filterMenu.addWidget(new HTML("&nbsp;/&nbsp;"));
		filterMenu.addWidget(filterTill);
		filterMenu.addWidget(applyFilter);
		filterMenu.addWidget(clearFilter);

		firstTabPanel.add(menu);
		firstTabPanel.add(filterMenu);
		firstTabPanel.add(sp);
		firstTabPanel.setCellHeight(menu, "30px");
		firstTabPanel.setCellHeight(sp, "100%");

		// resize perun table to correct size on screen
		session.getUiElements().resizePerunTable(sp, 350, this);

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
		return SmallIcons.INSTANCE.booksIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 631;
		int result = 1;
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
		if (this.userId != ((UsersPublicationsTabItem)obj).userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.setActiveUser(user);
		session.getUiElements().getMenu().openMenu(MainMenu.USER);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()), UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId, "Publications", getUrlWithParameters());
	}

	public boolean isAuthorized() {
		if (session.isSelf(userId)) {
			return true;
		} else {
			return false;
		}
	}

	public final static String URL = "userpubs";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return CabinetTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?user=" + userId;
	}

	static public UsersPublicationsTabItem load(Map<String, String> parameters) {
		if(parameters.containsKey("user")){
			return new UsersPublicationsTabItem(Integer.valueOf(parameters.get("user")));
		}
		return new UsersPublicationsTabItem();
	}

}
