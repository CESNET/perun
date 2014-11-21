package cz.metacentrum.perun.webgui.tabs.perunadmintabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.searcher.GetUsers;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.PerunAdminTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.PerunSearchParametersWidget;

import java.util.Map;

/**
 * Searcher page
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class SearcherTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Searcher");

	/**
	 * Creates a tab instance
	 */
	public SearcherTabItem(){ }

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		// request
		final GetUsers request = new GetUsers();

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		PerunSearchParametersWidget params = new PerunSearchParametersWidget(PerunEntity.USER, new PerunSearchParametersWidget.SearchEvent() {

			public void search(Map<String, String> map) {

				request.clearParameters();

				for(Map.Entry<String, String> entry : map.entrySet())
		{
			request.addSearchParameter(entry.getKey(), entry.getValue());
		}

		request.search();
			}
		});

		firstTabPanel.add(params);

		// get the table
		final CellTable<User> table = request.getEmptyTable(new FieldUpdater<User, String>() {
			public void update(int index, User object, String value) {
				// opens the tab
				session.getTabManager().addTab(new UserDetailTabItem(object));
			}
		});

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");

		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		firstTabPanel.add(sp);
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
		return SmallIcons.INSTANCE.magnifierIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 953;
		int result = 1;
		result = prime * result + 122341;
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
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Searcher", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "searcher";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return PerunAdminTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public SearcherTabItem load(Map<String, String> parameters)
	{
		return new SearcherTabItem();
	}
}
