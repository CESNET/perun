package cz.metacentrum.perun.webgui.tabs;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.WebGui;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.widgets.NotAuthorizedWidget;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager for the tabs in the main tab panel
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlámal
 */
public class TabManager {

	public static final String ACTIVE_TOKEN = "active=1";

	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Map with the tabs
	 * KEY = tab, VALUE = unique tab ID
	 */
	private Map<TabItem, Integer> tabs = new HashMap<TabItem, Integer>();

	private boolean changePageEventLocked;

	private String currentUrlWithNoActive = "";

	private String currentUrlWithActive = "";

	private TabItem activeTab;

	private TabItem activeOverlayTab;

	static private final int CHECK_TAB_PREPARED_INTERVAL = 100;

	static private final String HELP_WIDGET_BUTTON_IMAGE = "img/help-widget-button-2.png";

	static private int helpCounter = 0;

	/**
	 * Tab overlays
	 * KEY = unique tab id, VALUE = tab overlay simple panel
	 */
	private Map<Integer, AbsolutePanel> tabOverlays = new HashMap<Integer, AbsolutePanel>();

	/**
	 * Child tabs - parent tabs
	 * KEY = child tab, VALUE = parent tab
	 */
	private Map<TabItem, TabItem> tabOverlaysTabItems = new HashMap<TabItem, TabItem>();

	public TabManager() {
	}

	/**
	 * Adds a new tab into the panel.
	 * If already exists, it only selects it.
	 *
	 * @param tab
	 * @return
	 */
	public boolean addTab(TabItem tab) {
		return addTab(tab, true);
	}

	/**
	 * Adds a new tab into the panel.
	 * If already exists, it only selects it.
	 *
	 * @param tab
	 * @param open Whether the tab should be opened after adding.
	 * @return
	 */
	public boolean addTab(final TabItem tab, final boolean open) {

		// already created?
		if (!tab.multipleInstancesEnabled() && this.tabs.containsKey(tab)) {
			if (open) {
				int tabId = this.tabs.get(tab);
				session.getUiElements().openTab(tabId);
			}
			return false;
		}

		Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
			public boolean execute() {
				if (tab.isPrepared()) {
					addTabWhenPrepared(tab, open);
					return false;
				}
				return true;
			}
		}, CHECK_TAB_PREPARED_INTERVAL);
		return true;
	}

	/**
	 * Adding a tab
	 * With this method, the authorization must be already granted
	 *
	 * @param tab
	 * @param open
	 * @return
	 */
	private boolean addTabWhenPrepared(TabItem tab, boolean open) {

		// store place
		final Widget widget;
		ImageResource tabIcon;

		// overlay tab
		final SimplePanel overlayTab = new SimplePanel();
		overlayTab.addStyleName("tab-overlay");

		final AbsolutePanel overlayShield = new AbsolutePanel();
		overlayShield.addStyleName("tab-overlay-shield");

		overlayShield.add(overlayTab);
		overlayTab.getElement().getStyle().setLeft(5, Unit.PCT);

		// authorization when not already opened
		if (tab.isAuthorized() == true) {

			// widget itself
			widget = tab.getWidget();
			tabIcon = tab.getIcon();
			tab.draw();  // load page content
		} else {
			widget = new NotAuthorizedWidget();
			tabIcon = SmallIcons.INSTANCE.errorIcon();
		}

		// panel with overlay and widget contents
		final AbsolutePanel ap = new AbsolutePanel();
		ap.add(widget, 5, 5);
		ap.add(overlayShield, 0, -2000);
		//overlayShield.getElement().getStyle().setLeft(5, Unit.PCT);

		// set sizes
		ap.setSize("100%", "100%");
		widget.setWidth("100%");

		// update overlay position
		UiElements.addResizeCommand(new Command() {
			@Override
			public void execute() {
				// empty
				if (overlayTab.getWidget() == null) {
					return;
				}

				int clientWidth = (Window.getClientWidth() > WebGui.MIN_CLIENT_WIDTH) ? Window.getClientWidth() : WebGui.MIN_CLIENT_WIDTH;

				// if small, then fixed size in the center
				if (!overlayTab.getStyleName().contains("tab-overlay-large")) {
					int overlayWidth = overlayTab.getElement().getOffsetWidth();

					// main menu width
					int left = (clientWidth - MainMenu.MENU_WIDTH - overlayWidth) / 2;
					overlayShield.setWidgetPosition(overlayTab, left, overlayShield.getWidgetTop(overlayTab));

					return;
				}
			}
		}, tab);

		// has a help widget?
		if (tab instanceof TabItemWithHelp) {

			helpCounter++;
			final String helpWrapperId = "helpWidgetWrapper-" + helpCounter;

			TabItemWithHelp tabWithHelp = (TabItemWithHelp) tab;

			final AbsolutePanel helpWidgetWrapper = new AbsolutePanel();
			ap.add(helpWidgetWrapper, 100000, 0);
			helpWidgetWrapper.addStyleName("helpWidgetWrapper");
			helpWidgetWrapper.addStyleName(helpWrapperId);

			// help widget
			//FlexTable helpWidget = new FlexTable();
			VerticalPanel helpWidget = new VerticalPanel();
			helpWidget.setWidth("100%");
			final ScrollPanel sp = new ScrollPanel(helpWidget);

			// header
			//Image img = new Image(LargeIcons.INSTANCE.helpIcon());
			//helpWidget.setWidget(0, 0, img);
			//helpWidget.getCellFormatter().setWidth(0, 0, "40px");
			HTML helpHeader = new HTML("<h3>Quick help</h3>");
			helpWidget.add(helpHeader);
			helpWidget.setCellHeight(helpHeader, "45px");

			// content
			//helpWidget.getFlexCellFormatter().setColSpan(1, 0, 2);
			//helpWidget.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
			helpWidget.add(tabWithHelp.getHelpWidget());


			final SimplePanel helpWidgetInnerWrapper = new SimplePanel(sp);
			helpWidgetInnerWrapper.setStyleName("helpWidget");
			helpWidgetWrapper.add(helpWidgetInnerWrapper, 0, 0);
			/*helpWidgetInnerWrapper.setSize("100%", "100%");*/

			// open help widget button
			PushButton helpWidgetButton = new PushButton(new Image(HELP_WIDGET_BUTTON_IMAGE));
			final SimplePanel helpWidgetButtonWrapper = new SimplePanel(helpWidgetButton);
			helpWidgetButton.setStyleName("helpWidgetButton");
			helpWidgetButton.setTitle("Open / close Quick help");
			helpWidgetWrapper.add(helpWidgetButtonWrapper, -30, 0);
			helpWidgetButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					toggleHelp(helpWrapperId);
				}
			});

			UiElements.addResizeCommand(new Command() {
				public void execute() {

					int helpWidgetWidth = 400;
					int clientWidth = (Window.getClientWidth() > WebGui.MIN_CLIENT_WIDTH) ? Window.getClientWidth() : WebGui.MIN_CLIENT_WIDTH;
					//int clientHeight = (Window.getClientHeight() > WebGui.MIN_CLIENT_HEIGHT) ?  Window.getClientHeight() : WebGui.MIN_CLIENT_HEIGHT;

					clientWidth -= MainMenu.MENU_WIDTH;

					int newLeft = clientWidth;

					if (isHelpOpened(helpWrapperId)) {
						newLeft -= helpWidgetWidth;

					}
					// update widget position
					ap.setWidgetPosition(helpWidgetWrapper, newLeft, 0);


					session.getUiElements().resizeSmallTabPanel(sp, 200);

					// update button top position
					//helpWidgetWrapper.setWidgetPosition(helpWidgetButtonWrapper, helpWidgetWrapper.getWidgetLeft(helpWidgetButtonWrapper), (clientHeight - 200) / 2);

				}
			});
		}

		// add
		int tabId = session.getUiElements().contentAddTab(ap, tab.getTitle(), open, tabIcon);

		// add to current map
		this.tabs.put(tab, tabId);
		if (open) {
			activeTab = tab;
			// call the event
			if (tab.isAuthorized()) {
				tab.open();
			}
			// DO NOT CHANGE MENU, IT'S HANDLED BY TAB
			// ON IT'S OWN VIA tab.open()
			//session.getUiElements().getMenu().updateLinks();
		}

		tabOverlays.put(tabId, overlayShield);

		// refresh URL
		refreshUrl();
		return true;

	}

	/**
	 * Returns the active tab
	 *
	 * @return
	 */
	public TabItem getActiveTab() {
		return this.activeTab;
	}

	/**
	 * Clear active tabs
	 */
	public void clearActiveTabs() {
		this.activeTab = null;
		this.activeOverlayTab = null;
	}

	/**
	 * Returns the active overlay tab
	 *
	 * @return
	 */
	public TabItem getActiveOverlayTab() {
		return this.activeOverlayTab;

	}

	/**
	 * Opens and closes the help panel
	 *
	 * @param id TabItemWithHelp panel ID
	 */
	static private native void toggleHelp(String id) /*-{
        var helpWidgetWidth = 400;
        var helpWidgetWrapper = $wnd.jQuery("." + id);
        var left = parseInt($wnd.jQuery(helpWidgetWrapper).css("left"), 10);
        if ($wnd.jQuery(helpWidgetWrapper).hasClass("opened")) {
            $wnd.jQuery(helpWidgetWrapper).removeClass("opened");
            $wnd.jQuery(helpWidgetWrapper).animate({ left: left + helpWidgetWidth + "px" }, 500);
        } else {
            $wnd.jQuery(helpWidgetWrapper).addClass("opened");
            $wnd.jQuery(helpWidgetWrapper).animate({ left: left - helpWidgetWidth + "px" }, 500);
        }
    }-*/;

	/**
	 * Whether is the help opened
	 *
	 * @param id TabItemWithHelp panel ID
	 * @return
	 */
	static private native boolean isHelpOpened(String id) /*-{
        return $wnd.jQuery("." + id).hasClass("opened");
    }-*/;

	/**
	 * Adds a "small tab" as overlay to current tab
	 *
	 * @param tab
	 * @return
	 */
	public boolean addTabToCurrentTab(final TabItem tab) {
		return addTabToCurrentTab(tab, false);
	}

	/**
	 * Adds tab as overlay to current tab
	 *
	 * @param tab
	 * @param large Whether the tab should fill all the parent
	 * @return
	 */
	public boolean addTabToCurrentTab(final TabItem tab, final boolean large) {

		Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
			public boolean execute() {
				if (tab.isPrepared()) {
					session.getUiElements().setLogText("Tab is prepared, adding tab.");

					addTabToCurrentTabAuthorized(tab, large);
					return false;
				}
				session.getUiElements().setLogText("Tab not prepared, waiting.");
				return true;
			}
		}, CHECK_TAB_PREPARED_INTERVAL);
		return true;

	}

	/**
	 * Change style of overlay (inner) tab.
	 * Modify only currently selected tab (it's overlay).
	 *
	 * @param large true = large (wide) style / false = small centered style
	 */
	public void changeStyleOfInnerTab(boolean large) {

		final int selectedTabUniqueId = session.getUiElements().getSelectedTabUniqueId();

		// find overlay
		final AbsolutePanel overlay = tabOverlays.get(selectedTabUniqueId);
		if (overlay == null) {
			return;
		}

		if (large) {
			overlay.getWidget(0).addStyleName("tab-overlay-large");
		} else {
			overlay.getWidget(0).removeStyleName("tab-overlay-large");
		}

	}

	/**
	 * The implementation of addTabToCurrentTab
	 *
	 * @param tab
	 * @param large whether tab large
	 * @return
	 */
	private boolean addTabToCurrentTabAuthorized(TabItem tab, boolean large) {

		final int selectedTabUniqueId = session.getUiElements().getSelectedTabUniqueId();

		// find overlay
		final AbsolutePanel overlay = tabOverlays.get(selectedTabUniqueId);
		if (overlay == null) {
			return false;
		}

		if (large) {
			overlay.getWidget(0).addStyleName("tab-overlay-large");
		} else {
			overlay.getWidget(0).removeStyleName("tab-overlay-large");
		}

		// find parent
		TabItem parent = getTabItemByUniqueId(selectedTabUniqueId);

		// store place
		Widget widget;

		// authorization when not already opened
		if (tab.isAuthorized() == true) {
			// widget itself
			widget = tab.getWidget();
			tab.draw();  // load page content
		} else {
			widget = new NotAuthorizedWidget();
		}

		// close button
		Button closeTabButton = new Button("X");
		closeTabButton.addStyleName("tabPanelCloseButton");
		closeTabButton.addStyleName("tab-overlay-close-button");
		closeTabButton.getElement().setAttribute("onclick", "jQuery(\"#tab-" + selectedTabUniqueId + " .tab-overlay-shield\").animate({ top : \"-1000px\" }, 'fast', function(){ jQuery(\"#tab-" + selectedTabUniqueId + " .tab-overlay-shield\").hide(''); }); jQuery(\"#tab-" + selectedTabUniqueId + " .tab-overlay\").animate({ top : \"-1000px\" }, 'fast', function(){ jQuery(\"#tab-" + selectedTabUniqueId + " .tab-overlay\").hide(''); }); jQuery(\"#tab-" + selectedTabUniqueId + " .tab-content\").fadeTo('fast', 1.0);");
		closeTabButton.setTitle(WidgetTranslation.INSTANCE.closeThisTab());

		// title
		final Widget title = tab.getTitle();
		title.addStyleName("tab-overlay-title");

		if (!overlay.getWidget(0).getStyleName().contains("tab-overlay-large")) {
			// shorten title only for small tabs
			title.getElement().setAttribute("style", "max-width: 330px;");
		}

		if (overlay.getWidget(0).getElement().hasChildNodes()) {
			overlay.getWidget(0).getElement().getFirstChildElement().removeFromParent();
		}
		if (overlay.getWidget(0).getElement().hasChildNodes()) {
			overlay.getWidget(0).getElement().getFirstChildElement().removeFromParent();
		}

		overlay.getWidget(0).getElement().appendChild(title.getElement());
		overlay.getWidget(0).getElement().appendChild(closeTabButton.getElement());

		// add
		((SimplePanel) overlay.getWidget(0)).setWidget(widget);

		// shows the tab
		setTabOverlayVisible(selectedTabUniqueId, true);

		// local
		tabOverlaysTabItems.put(tab, parent);

		// set it as active overlay tab
		this.activeOverlayTab = tab;

		// run resize commands immediately
		UiElements.runResizeCommands(true);

		// run resize commands, after 400ms, when panel fully opened
		Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
			@Override
			public boolean execute() {
				UiElements.runResizeCommandsForCurrentTab();
				return false;
			}
		}, 400);

		return true;

	}

	/**
	 * Sets tab overlay visibility
	 *
	 * @param tabId
	 * @param visible
	 */
	private native final void setTabOverlayVisible(int tabId, boolean visible) /*-{
        var overlay = "#tab-" + tabId + " .tab-overlay";
        var shield = "#tab-" + tabId + " .tab-overlay-shield";
        var content = "#tab-" + tabId + " .tab-content";
        var height = 1000;
        var topSpace = 15;

        if (visible) {
            $wnd.jQuery(overlay).show();
            $wnd.jQuery(overlay).animate({ top: topSpace + "px"}, 200);

            $wnd.jQuery(shield).show();
            $wnd.jQuery(shield).animate({ top: topSpace + "px"}, 200);

            $wnd.jQuery(content).fadeTo("slow", 0.4);
        } else {
            $wnd.jQuery(content).fadeTo("slow", 1);
            $wnd.jQuery(overlay).animate({ top: (-height + topSpace) + "px" }, 'fast', function () {
                $wnd.jQuery(overlay).hide();
            });
            $wnd.jQuery(shield).animate({ top: (-height + topSpace) + "px" }, 'fast', function () {
                $wnd.jQuery(shield).hide();
            });
        }

    }-*/;


	/**
	 * Refreshes URL
	 */
	private void refreshUrl() {

		// LOCKING GUI FOR URL CHANGE
		this.changePageEventLocked = true;

		String url = "";
		String urlWithNoActive = "";
		String urlWithActive = "";

		for (Map.Entry<TabItem, Integer> entry : tabs.entrySet()) {
			TabItem tabItem = entry.getKey();
			if (tabItem instanceof TabItemWithUrl) {
				TabItemWithUrl tab = (TabItemWithUrl) tabItem;
				String thisUrl = tab.getUrlWithParameters();
				urlWithNoActive += thisUrl + UrlMapper.TAB_SEPARATOR;
				if (tab == activeTab) {
					thisUrl = updateUrlToActive(thisUrl);
				}
				urlWithActive += thisUrl + UrlMapper.TAB_SEPARATOR;

				url += thisUrl + UrlMapper.TAB_SEPARATOR;
			}
		}

		this.currentUrlWithNoActive = urlWithNoActive;
		this.currentUrlWithActive = urlWithActive;

		// changing url
		History.newItem(url, false);

		// REMOVING LOCK
		this.changePageEventLocked = false;

	}


	/**
	 * Reloads the tab specified by the unique tab ID.
	 *
	 * @param tabId
	 * @return
	 */
	public boolean reloadTab(int tabId) {
		TabItem tabToReload = getTabItemByUniqueId(tabId);

		if (tabToReload == null) {
			return false;
		}

		tabToReload.draw(); // load page content
		if (tabToReload.isAuthorized()) {
			tabToReload.open(); // sets page context to the rest of GUI (show proper menu, set breadcrumbs)
		}
		return true;

	}

	/**
	 * Returns the tab item by the id.
	 * <p/>
	 * TODO/FIXME: Can't process overlay tabs since they do not have Unique ID !!
	 *
	 * @param uniqueTabId Unique tab ID
	 * @return tabItem if found, null otherwise
	 */
	private TabItem getTabItemByUniqueId(int uniqueTabId) {
		TabItem tabToReload = null;
		for (Map.Entry<TabItem, Integer> entry : this.tabs.entrySet()) {
			if (uniqueTabId == entry.getValue()) {
				tabToReload = entry.getKey();
				break;
			}
		}
		return tabToReload;
	}

	/**
	 * Reloads the tab specified by the TabItem
	 *
	 * @param tab Tab to reload content for
	 * @return true if reloaded, false if not
	 */
	public boolean reloadTab(TabItem tab) {
		for (TabItem key : this.tabs.keySet()) {
			if (key.equals(tab)) {
				key.draw(); // load page content
				if (key.isAuthorized()) {
					key.open(); // sets page context to the rest of GUI (show proper menu, set breadcrumbs)
				}
				return true;
			}
		}
		// reload also overlay tabs (key is child tab = content of overlay tab)
		for (TabItem key : this.tabOverlaysTabItems.keySet()) {
			if (key.equals(tab)) {
				key.draw();
				if (key.isAuthorized()) {
					key.open();
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * Removes a tab from map
	 *
	 * @param tabId
	 * @return
	 */
	public boolean removeTab(int tabId) {
		for (Map.Entry<TabItem, Integer> entry : this.tabs.entrySet()) {
			if (tabId == entry.getValue()) {
				this.tabs.remove(entry.getKey());
				refreshUrl();
				return true;
			}
		}
		return false;
	}

	/**
	 * Closes a tab, specified in the parameter.
	 * It compares it with current tabs and closes it
	 */
	public void closeTab(TabItem tabItem) {

		closeTab(tabItem, true);
	}

	/**
	 * Closes a tab, specified in the parameter.
	 * It compares it with current tabs and closes it
	 *
	 * @param tabItem
	 * @param refreshParent Whether to refresh parent tab (default = true)
	 */
	public void closeTab(TabItem tabItem, boolean refreshParent) {

		if (!this.tabs.containsKey(tabItem)) {
			// if not in regular tabs, try child tabs
			if (!this.tabOverlaysTabItems.containsKey(tabItem)) {
				return;
			}

			// if contains, refresh parent after tab overlay close
			TabItem parent = this.tabOverlaysTabItems.get(tabItem);

			// overlay close
			int parentUniqueId = this.tabs.get(parent);
			setTabOverlayVisible(parentUniqueId, false);

			// remove from overlay tab
			this.activeOverlayTab = null;

			// refresh
			if (refreshParent) {
				parent.draw();
			}
			return;
		}

		// close tabs
		int id = this.tabs.get(tabItem);
		//this.tabs.remove(tabItem);
		// refresh url
		refreshUrl();

		session.getUiElements().closeTab(id);

	}

	/**
	 * Whether can browser change the page
	 *
	 * @return
	 */
	public boolean isChangePageEventLocked() {
		return changePageEventLocked;
	}


	/**
	 * Locks the page chaning
	 *
	 * @param changePageEventLocked
	 */
	public void setChangePageEventLocked(boolean changePageEventLocked) {
		this.changePageEventLocked = changePageEventLocked;
	}

	/**
	 * Returns the link for the TAB, which includes the current state
	 *
	 * @param tabItem
	 * @return
	 */
	public String getLinkForTab(TabItemWithUrl tabItem) {
		return this.currentUrlWithNoActive + updateUrlToActive(tabItem.getUrlWithParameters());
	}

	/**
	 * Returns the link for the TAB, without current state
	 *
	 * @param tabItem
	 * @return
	 */
	public String getLinkOnlyForTab(TabItemWithUrl tabItem) {
		return updateUrlToActive(tabItem.getUrlWithParameters());
	}


	/**
	 * Sets the tab to opened state
	 *
	 * @param uniqueTabId
	 * @return whether tab found and opened
	 */
	public boolean openTab(int uniqueTabId) {

		TabItem tabOpen = getTabItemByUniqueId(uniqueTabId);
		if (tabOpen == null) {
			return false;
		}

		this.activeTab = tabOpen;
		refreshUrl();
		if (tabOpen.isAuthorized()) {
			tabOpen.open();
		}

		UiElements.runResizeCommands(activeTab);

		// find and resize overlay tab
		for (Map.Entry<TabItem, TabItem> entry : tabOverlaysTabItems.entrySet()) {
			if (entry.getValue() == tabOpen) {
				this.activeOverlayTab = entry.getKey();
				UiElements.runResizeCommands(activeOverlayTab);
				break;
			}
		}

		// DO NOT REFRESH ALL LINKS IN MENU,
		// TABS DO IT ON IT'S OWN VIA tabOpen.open()
		//session.getUiElements().getMenu().updateLinks();

		return true;
	}

	/**
	 * Appends the active token to the input page URL
	 *
	 * @param url
	 * @return
	 */
	private String updateUrlToActive(String url) {
		if (url.contains("?")) {
			return url + "&" + ACTIVE_TOKEN;
		} else {
			return url + "?" + ACTIVE_TOKEN;

		}
	}

	/**
	 * Returns current browser URL
	 *
	 * @return
	 */
	public String getCurrentUrl() {
		return getCurrentUrl(false);
	}

	/**
	 * Returns current browser URL
	 *
	 * @param active Whether to include &active=1 to the selected tab
	 * @return
	 */
	public String getCurrentUrl(boolean active) {
		if (!active) {
			return this.currentUrlWithNoActive;
		} else {
			return this.currentUrlWithActive;
		}
	}

}