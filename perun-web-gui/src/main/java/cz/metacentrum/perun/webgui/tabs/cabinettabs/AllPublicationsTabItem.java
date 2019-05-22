package cz.metacentrum.perun.webgui.tabs.cabinettabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.cabinetManager.DeletePublication;
import cz.metacentrum.perun.webgui.json.cabinetManager.FindAllAuthors;
import cz.metacentrum.perun.webgui.json.cabinetManager.GetCategories;
import cz.metacentrum.perun.webgui.json.cabinetManager.FindPublicationsByGUIFilter;
import cz.metacentrum.perun.webgui.json.cabinetManager.LockUnlockPublications;
import cz.metacentrum.perun.webgui.model.Author;
import cz.metacentrum.perun.webgui.model.Category;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Publication;
import cz.metacentrum.perun.webgui.tabs.CabinetTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab for viewing all publications in Perun system
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class AllPublicationsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("All publications");

	private String lastTitle = "";
	private boolean lastIsbnOrDoi = true; // DEFAULT TRUE = ISBN / FALSE = DOI
	private String lastIsbnOrDoiValue = "";
	private String lastYear = "";
	private String lastYearSince = String.valueOf(JsonUtils.getCurrentYear());
	private String lastYearTill= "";
	private int lastCategoryId = 0; // 0 = default "not selected"
	private int lastUserId = 0;     // 0 = default "not selected"
	private Map<String, Object> lastIds = new HashMap<String, Object>();

	/**
	 * Creates a tab instance
	 */
	public AllPublicationsTabItem(){}

	public boolean isPrepared(){
		return true;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// CALLBACK
		final Map<String, Object> ids = new HashMap<String, Object>();
		ids.put("authors", 1);
		ids.put("yearSince", JsonUtils.getCurrentYear());

		final FindPublicationsByGUIFilter callback = new FindPublicationsByGUIFilter(ids);

		// menus
		TabMenu menu = new TabMenu();
		final HTML userHtml = new HTML("<strong>User:</strong>");
		userHtml.setVisible(false);
		final ListBoxWithObjects<Author> users = new ListBoxWithObjects<Author>();
		users.setVisible(false);
		final TabMenu filterMenu = new TabMenu();
		filterMenu.setVisible(false);

		menu.addWidget(UiElements.getRefreshButton(this));

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Create new publication", new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTab(new AddPublicationsTabItem(session.getUser()));
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove selected publication(s)");
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<Publication> list = callback.getTableSelectedList();
				String text = "Following publications will be removed.";
				UiElements.showDeleteConfirm(list, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						// TODO - should have only one callback to core
						for (int i=0; i<list.size(); i++) {
							if (i == list.size()-1) {
								DeletePublication request = new DeletePublication(JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(callback)));
								request.deletePublication(list.get(i).getId());
							} else {
								DeletePublication request = new DeletePublication(JsonCallbackEvents.disableButtonEvents(removeButton));
								request.deletePublication(list.get(i).getId());
							}
						}
					}
				});
			}
		});
		menu.addWidget(removeButton);

		// fill users listbox
		final FindAllAuthors userCall = new FindAllAuthors(new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				users.removeNotSelectedOption();
				users.clear();
				users.addNotSelectedOption();
				ArrayList<Author> list = JsonUtils.jsoAsList(jso.cast());
				list = new TableSorter<Author>().sortByName(list);
				for (int i=0; i<list.size(); i++){
					users.addItem(list.get(i));
					if (lastUserId != 0) {
						if (lastUserId == list.get(i).getId()) {
							users.setSelected(list.get(i), true);
						}
					}
				}
				if (lastUserId ==0) {
					users.setSelectedIndex(0);
				}
			}
			public void onError(PerunError error) {
				users.clear();
				users.removeNotSelectedOption();
				users.addItem("Error while loading");
			}
			public void onLoadingStart() {
				users.clear();
				users.removeNotSelectedOption();
				users.addItem("Loading...");
			}
		});

		// fill category listbox
		final ListBoxWithObjects<Category> filterCategory = new ListBoxWithObjects<Category>();
		final GetCategories call = new GetCategories(new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				filterCategory.removeNotSelectedOption();
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
			public void onLoadingStart() {
				filterCategory.clear();
				filterCategory.removeNotSelectedOption();
				filterCategory.addItem("Loading...");
			}
		});

		// switch lock state button
		final CustomButton lock = new CustomButton("Lock", "Lock selected publications", SmallIcons.INSTANCE.lockIcon());
		lock.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				ArrayList<Publication> list = callback.getTableSelectedList();
				if (list != null && !list.isEmpty()) {
					LockUnlockPublications request = new LockUnlockPublications(JsonCallbackEvents.disableButtonEvents(lock, JsonCallbackEvents.refreshTableEvents(callback)));
					request.lockUnlockPublications(true, list);
				}
			}
		});

		// switch lock state button
		final CustomButton unlock = new CustomButton("Unlock", "Unlock selected publications", SmallIcons.INSTANCE.lockOpenIcon());
		unlock.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				ArrayList<Publication> list = callback.getTableSelectedList();
				if (list != null && !list.isEmpty()) {
					LockUnlockPublications request = new LockUnlockPublications(JsonCallbackEvents.disableButtonEvents(unlock, JsonCallbackEvents.refreshTableEvents(callback)));
					request.lockUnlockPublications(false, list);
				}
			}
		});

		menu.addWidget(lock);
		menu.addWidget(unlock);

		CustomButton filter = new CustomButton("Show / Hide filter", SmallIcons.INSTANCE.filterIcon(), new ClickHandler(){
			public void onClick(ClickEvent event) {
				if (filterMenu.isVisible() == false) {
					filterMenu.setVisible(true);
					userHtml.setVisible(true);
					users.setVisible(true);
					if (users.isEmpty()){
						userCall.retrieveData();
					}
					if (filterCategory.isEmpty()){
						call.retrieveData();
					}
				} else {
					filterMenu.setVisible(false);
					userHtml.setVisible(false);
					users.setVisible(false);
				}
			}
		});
		menu.addWidget(filter);

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

		// add users filter in upper menu
		menu.addWidget(userHtml);
		menu.addWidget(users);

		// buttons
		CustomButton applyFilter = new CustomButton("Apply", "Show publications based on filter", SmallIcons.INSTANCE.filterAddIcon());
		CustomButton clearFilter = new CustomButton("Clear","Show all publications",SmallIcons.INSTANCE.filterDeleteIcon());

		clearFilter.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				// clear last values
				lastCategoryId = 0;
				lastIsbnOrDoi = true;
				lastIsbnOrDoiValue = "";
				lastTitle = "";
				lastUserId = 0;
				lastYear = "";
				lastYearTill = "";
				lastYearSince = "";

				// clear form
				filterTitle.setText("");
				filterYear.setText("");
				filterIsbn.setText("");
				filterSince.setText("");
				filterTill.setText("");
				filterCategory.setSelectedIndex(0);
				users.setSelectedIndex(0);
				ids.clear();
				ids.put("authors", 1);
				lastIds = ids;
				callback.setIds(ids);
				callback.clearTable();
				callback.retrieveData();
			}
		});

		applyFilter.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				// refresh ids
				ids.clear();
				if (users.getSelectedIndex() > 0) {
					ids.put("userId", users.getSelectedObject().getId());
					lastUserId = users.getSelectedObject().getId();
				} else {
					ids.put("userId", 0);
					lastUserId = 0;
				}
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

		vp.add(menu);
		vp.setCellHeight(menu, "50px");
		vp.add(filterMenu);

		if (!lastIds.isEmpty()) {
			callback.setIds(lastIds);
		}
		CellTable<Publication> table = callback.getTable(new FieldUpdater<Publication, String>() {
			public void update(int index, Publication object, String value) {
				session.getTabManager().addTab(new PublicationDetailTabItem(object));
			}
		});

		removeButton.setEnabled(false);
		lock.setEnabled(false);
		unlock.setEnabled(false);
		JsonUtils.addTableManagedButton(callback, table, removeButton);
		JsonUtils.addTableManagedButton(callback, table, lock);
		JsonUtils.addTableManagedButton(callback, table, unlock);

		table.addStyleName("perun-table");

		ScrollPanel sp = new ScrollPanel();
		sp.add(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);

		// resize perun table to correct size on screen
		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

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
		return SmallIcons.INSTANCE.booksIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 599;
		int result = 10;
		result = prime * result * 22;
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

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN);
	}

	public boolean isAuthorized() {
		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}
	}

	public final static String URL = "publications";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return CabinetTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public AllPublicationsTabItem load(Map<String, String> parameters)
	{
		return new AllPublicationsTabItem();
	}

}
