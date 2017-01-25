package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.tabs.TabItem;

import java.util.ArrayList;

/**
 * TabPanel extension for including TabItems
 * Automatically handles drawing and resizing
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class TabPanelForTabItems extends TabLayoutPanel {

	/**
	 * Default tab size
	 */
	static private double DEFAULT_SIZE_PX = 33;   // when changed, update CSS property ".smallTabPanel .gwt-TabLayoutPanelTabs"

	/**
	 * Tab items
	 */
	private ArrayList<TabItem> innerTabs = new ArrayList<TabItem>();

	/**
	 * Simple panels which contains the tabs
	 */
	private ArrayList<SimplePanel> innerTabsSimplePanels = new ArrayList<SimplePanel>();

	/**
	 * Last selected item (used when refresing)
	 */
	private int lastTabId = 0;

	/**
	 * Whether adding finished
	 */
	private boolean addingFinished = false;
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Tab containing this TabPanelForTabItems
	 */
	private TabItem parentTab;


	/**
	 * Creates a new "smallTabPanel" for including TabItems.
	 * Automatically handles drawing and resizing
	 * Default size
	 *
	 * @param parentTab Parent tab, necessary for resizing
	 */
	public TabPanelForTabItems(final TabItem parentTab)
	{
		this(parentTab, DEFAULT_SIZE_PX, Unit.PX);
	}

	/**
	 * Creates a new "smallTabPanel" for including TabItems.
	 * Automatically handles drawing and resizing
	 *
	 * @param parentTab Parent tab, necessary for resizing
	 * @param barHeight Bar height
	 * @param barUnit Bar width
	 */
	public TabPanelForTabItems(final TabItem parentTab, double barHeight, Unit barUnit)
	{
		super(barHeight, barUnit);

		this.setSize("100%", "100%");

		// styles, resizing
		this.addStyleName("smallTabPanel");
		session.getUiElements().resizeSmallTabPanel(this, 100, parentTab);
		this.parentTab = parentTab;

		// selection handler draws the tab
		this.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {

				// adding finished?
				if(!addingFinished) {
					return;
				}
				int i = event.getSelectedItem();
				runOnSelectEvent(i);
			}
		});

		// if parent resized, must resize active widget
		UiElements.addResizeCommand(new Command() {

			public void execute() {

				// adding finished?
				if(!addingFinished)
				{
					return;
				}

				int i = getLastTabId();

				// check size
				if(innerTabs.size() < (i + 1))
				{
					return;
				}

				// retrieves the tab item
				TabItem tab = innerTabs.get(i);

				// check null
				if(tab == null){
					return;
				}

				UiElements.runResizeCommands(tab);
			}
		}, parentTab);


	}


	/**
	 * Adds a new tab to the panel
	 *
	 * @param tabItem
	 * @param caption
	 */
	public void add(TabItem tabItem, String caption)
	{
		SimplePanel sp = new SimplePanel();
		innerTabsSimplePanels.add(sp);
		innerTabs.add(tabItem);
		super.add(sp, caption);
		sp.addStyleName("smallTabPanel");
	}

	/**
	 * Clears the tab panel keeping last ID
	 */
	public void clear()
	{
		addingFinished = false;
		super.clear();
		innerTabs.clear();
		innerTabsSimplePanels.clear();
	}


	/**
	 * Sets the last selected tab
	 *
	 * @param i
	 * @param force
	 */
	public void setLastTabId(int i, boolean force)
	{
		if(addingFinished || force)
		{
			lastTabId = i;
		}
	}


	/**
	 * Sets the last selected tab, FORCED
	 *
	 * @param i
	 */
	public void setLastTabId(int i)
	{
		setLastTabId(i, true);
	}

	/**
	 * Returns the last selected tab
	 *
	 * @return
	 */
	private int getLastTabId()
	{
		return lastTabId;
	}

	/**
	 * Selects the last active tab (before clearing)
	 */
	public void finishAdding() {
		addingFinished = true;

		this.selectTab(this.getLastTabId(), false);  // select the last tab, DO NOT CALL THE event

		// call on select
		runOnSelectEvent(this.getLastTabId());
	}


	/**
	 * When selected tab, draw and resize it
	 *
	 * @param i
	 */
	protected void runOnSelectEvent(int i)
	{

		// run resize commands for parent tab
		if (parentTab != null) {
			UiElements.runResizeCommands(parentTab);
		} else {
			UiElements.runResizeCommands();
		}

		// selected tab
		setLastTabId(i, false);

		// check size
		if((innerTabs.size() < (i + 1)) || (innerTabsSimplePanels.size() < (i + 1)))
		{
			return;
		}

		// if widget not drawn - draw
		SimplePanel sp = innerTabsSimplePanels.get(i);
		TabItem tab = innerTabs.get(i);

		// check null
		if(sp == null || tab == null){
			return;
		}

		// if widget null, call draw
		if(sp.getWidget() == null){
			sp.setWidget(tab.draw());
		}

		UiElements.runResizeCommands(tab);

	}

	public TabItem getSelectedTabItem() {

		return innerTabs.get(getSelectedIndex());

	}

	public TabItem getTabItem(int index) {

		return innerTabs.get(index);

	}

}
