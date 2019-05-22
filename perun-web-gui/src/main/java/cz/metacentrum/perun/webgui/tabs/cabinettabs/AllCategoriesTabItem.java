package cz.metacentrum.perun.webgui.tabs.cabinettabs;

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
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.cabinetManager.DeleteCategory;
import cz.metacentrum.perun.webgui.json.cabinetManager.GetCategories;
import cz.metacentrum.perun.webgui.json.cabinetManager.UpdateCategory;
import cz.metacentrum.perun.webgui.model.Category;
import cz.metacentrum.perun.webgui.tabs.CabinetTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab for viewing all categories in Perun system
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class AllCategoriesTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Categories");

	/**
	 * Creates a tab instance
	 */
	public AllCategoriesTabItem(){}

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

		final GetCategories callback = new GetCategories();

		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(callback);

		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Add new category", new ClickHandler(){
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new CreateCategoryTabItem());
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.DELETE, "Delete selected categories");
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<Category> delete = callback.getTableSelectedList();
				String text = "Following categories will be deleted";
				UiElements.showDeleteConfirm(delete, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
						for (int i=0; i<delete.size(); i++ ) {
							if (i == delete.size()-1) {
								DeleteCategory request = new DeleteCategory(JsonCallbackEvents.disableButtonEvents(removeButton, events));
								request.deleteCategory(delete.get(i).getId());
							} else {
								DeleteCategory request = new DeleteCategory(JsonCallbackEvents.disableButtonEvents(removeButton));
								request.deleteCategory(delete.get(i).getId());
							}
						}
					}
				});
			}
		});
		menu.addWidget(removeButton);

		final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes in category ranks");
		saveButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<Category> list = callback.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
					for (int i=0; i<list.size(); i++ ) {
						if (i == list.size()-1) {
							UpdateCategory request = new UpdateCategory(JsonCallbackEvents.disableButtonEvents(saveButton, events));
							request.updateCategory(list.get(i));
						} else {
							UpdateCategory request = new UpdateCategory(JsonCallbackEvents.disableButtonEvents(saveButton));
							request.updateCategory(list.get(i));
						}
					}
				}
			}
		});
		menu.addWidget(saveButton);

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		CellTable<Category> table = callback.getTable();

		removeButton.setEnabled(false);
		saveButton.setEnabled(false);
		JsonUtils.addTableManagedButton(callback, table, removeButton);
		JsonUtils.addTableManagedButton(callback, table, saveButton);

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
		return SmallIcons.INSTANCE.bookshelfIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 593;
		int result = 12;
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

	public final static String URL = "categories";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return CabinetTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public AllCategoriesTabItem load(Map<String, String> parameters)
	{
		return new AllCategoriesTabItem();
	}

}
