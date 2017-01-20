package cz.metacentrum.perun.webgui.tabs.cabinettabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.cabinetManager.FindAllAuthors;
import cz.metacentrum.perun.webgui.model.Author;
import cz.metacentrum.perun.webgui.tabs.CabinetTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Tab for viewing all authors in Perun system
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class AllAuthorsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Authors");

	/**
	 * Creates a tab instance
	 */
	public AllAuthorsTabItem(){
	}

	public boolean isPrepared(){
		return true;
	}


	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();
		vp.getElement().setAttribute("style", "padding-top: 5px;");
		vp.setSize("100%", "100%");

		// MENU
		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");
		menu.addWidget(UiElements.getRefreshButton(this));

		final FindAllAuthors callback = new FindAllAuthors();

		menu.addFilterWidget(new ExtendedSuggestBox(callback.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				callback.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterAuthors());


		CellTable<Author> table = callback.getTable(new FieldUpdater<Author, String>() {
			public void update(int index, Author object, String value) {
				if (session.isPerunAdmin()) {
					session.getTabManager().addTab(new UsersPublicationsTabItem(object.getId()));
				}
			}
		});

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
		return SmallIcons.INSTANCE.userRedIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 587;
		int result = 122;
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

	public final static String URL = "authors";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return CabinetTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public AllAuthorsTabItem load(Map<String, String> parameters)
	{
		return new AllAuthorsTabItem();
	}

}
