package cz.metacentrum.perun.webgui.tabs.cabinettabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.tabs.CabinetTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;

import java.util.Map;

/**
 * Page with Publications management.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class PublicationsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Publications");

	/**
	 * Small tab panel
	 */
	private TabPanelForTabItems tabPanel;


	/**
	 * Creates a tab instance
	 */
	public PublicationsTabItem(){
		this.tabPanel = new TabPanelForTabItems(this);
	}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		AbsolutePanel dp = new AbsolutePanel();
		final FlexTable menu = new FlexTable();
		menu.setCellSpacing(5);
		menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.booksIcon()));
		menu.setHTML(0, 1, "Publications");
		menu.getFlexCellFormatter().setStyleName(0, 1, "now-managing");

		menu.setHTML(0, 2, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, 2, "25px");

		dp.add(menu);
		firstTabPanel.add(dp);
		firstTabPanel.setCellHeight(dp, "30px");

		// prepare panel
		tabPanel.clear();

		// adds small tabs
		tabPanel.add(new AllPublicationsTabItem(), "All publications");
		tabPanel.add(new AllAuthorsTabItem(), "Authors");
		tabPanel.add(new AllCategoriesTabItem(), "Categories");
		tabPanel.add(new PublicationSystemsTabItem(), "Publication systems");

		// select last active tab before clearing
		tabPanel.finishAdding();

		firstTabPanel.add(tabPanel);
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
		final int prime = 617;
		int result = 1;
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

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Publications", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "all";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return CabinetTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public PublicationsTabItem load(Map<String, String> parameters) {
		return new PublicationsTabItem();
	}

}
