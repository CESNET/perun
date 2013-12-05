package cz.metacentrum.perun.webgui.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.SelfDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.*;

import java.util.*;

/**
 * Class for handling all major GUI elements like: Menu, Header, Footer, Content.
 * 
 * Provides base content handling (appending and removing tabs & inner tabs for main content)
 * Provides automatic resize function for elements when page size is changed.
 * Handles devel log panel.
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class UiElements {
	
	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	
	// constants
	static public final int FOOTER_HEIGHT = 30;
	static public final String TITLE_SUFFIX = " | Perun GUI";
	static private final int MOVE_TABS = 200; // How much pixels move the tabs
	
	// main Ui elements
	private AbsolutePanel contentPanel; // main content panel
	private TabLayoutPanel tabPanel; // tab panel inside content panel used for handling tabs
	private MainMenu menu;
		
	// devel log
	private ScrollPanel log;
	private VerticalPanel logInside = new VerticalPanel();
	private boolean logVisible = false; // log not visible by default
	
	// pending requests widget displayed in menu
	//private GetPendingRequests pendingRequests;
		
	// tabs
	private int tabCount = 0;
	private HashMap<Integer, Widget>  allTabs = new HashMap<Integer, Widget>(); // key is UNIQUE ID, value is content widget
	private ArrayList<Integer> tabsHistory = new ArrayList<Integer>(); // History of opened tabs (UNIQUE ID)
	
	// minor Ui elements
	private SimplePanel status = new SimplePanel(); // perun status
	private FlexTable loggedUserInfo = new FlexTable(); // table with loged user info

	// widget with entities history
    private BreadcrumbsWidget breadcrumbs;
	
	// resize commands called when page size changes
	static private Set<Command> resizeCommands = new HashSet<Command>();
	
	// resize commands called when page size changes
	static private HashMap<TabItem, Set<Command>> resizeCommandsForTabs = new HashMap<TabItem, Set<Command>>();
	
	// footer buttons
	private ToggleButton logButton;
	private SimplePanel logButtonWrapper = new SimplePanel();
	private ToggleButton extendedInfoButton = new ToggleButton(new Image(SmallIcons.INSTANCE.attributesDisplayIcon()));
	private ToggleButton languageButton = new ToggleButton(new Image(SmallIcons.INSTANCE.flagCzechBritainIcon()));

    // Switch identity helping variabless
    private int userCallcounter = 0;
    ArrayList<User> usersList = new ArrayList<User>();


    /**
	 * Creates a new instance of UiElements
	 *
     * @param cp The content panel
     */
	public UiElements(AbsolutePanel cp) {
		
		// SETUP
		this.contentPanel = cp; // set content panel
		this.menu = new MainMenu(); // create instance of menu
		//this.pendingRequests = new GetPendingRequests(session);
		
		// Window resizing
		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
                // run resize only for opened tab/overlay + shared commands
				runResizeCommands();
			}
		});

		// Callback notification displayed when onSuccess()
		status.getElement().setId("perun-status");
		status.setWidget(new Label(""));
		
		// Devel log
		this.log = new ScrollPanel();
		log.getElement().setId("perun-log");
		log.setStyleName("log", true);
		log.setWidth("100%");
		log.add(logInside);
		// toggle log button
		prepareToggleLogButton(SmallIcons.INSTANCE.bulletArrowUpIcon(), SmallIcons.INSTANCE.bulletArrowDownIcon());
		
		// no content
		if(contentPanel == null) return;
		
		// TABS
		
		// define tab panel
		tabPanel = new TabLayoutPanel(44, Unit.PX);
		tabPanel.setSize("100%", "100%");
		tabPanel.setStyleName("mainTabPanel", true);
		tabPanel.setAnimationDuration(0);

		// when changed selection, calls the method open and updates the history
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
                // run resize only for opened tab/overlay + shared commands
				UiElements.runResizeCommands();
				// open proper tab when selected and change tab history
				int tabId = getTabId(event.getSelectedItem());
				if(tabId >= 0) {
					session.getTabManager().openTab(tabId);
					tabsHistory.remove((Object) tabId);
					tabsHistory.add(tabId);
				}
				// move tab's header to be visible all the time
				showCurrentTabIfHidden(); 
			}
		});
		
		// TAB HANDLING BUTTONS
		
		Button moveLeft = new Button("&lt;");
		moveLeft.setPixelSize(24, 30);
		moveLeft.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				moveTabs(MOVE_TABS, false);
			}
		});
        moveLeft.setTitle(WidgetTranslation.INSTANCE.moveLeftButton());
		
		Button moveRight = new Button("&gt;");
		moveRight.setPixelSize(24, 30);
		moveRight.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				moveTabs(-MOVE_TABS, false);
			}
		});
        moveRight.setTitle(WidgetTranslation.INSTANCE.moveRightButton());

		// close all tabs but selected
		PushButton closeAllButActiveButton = new PushButton(new Image(SmallIcons.INSTANCE.crossIcon()));
		closeAllButActiveButton.setStyleName("gwt-Button");
		closeAllButActiveButton.getElement().getStyle().setProperty("padding", "6px 5px 2px 5px");
		closeAllButActiveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {			
				closeAllTabButActive();
			}
		});
		closeAllButActiveButton.setTitle(WidgetTranslation.INSTANCE.closeOthersButton());

		// buttons wrapper
		final FlexTable moveButtonsFt = new FlexTable();
		FlexCellFormatter fcf = moveButtonsFt.getFlexCellFormatter();
		fcf.setHorizontalAlignment(0,0, HasHorizontalAlignment.ALIGN_RIGHT);
		fcf.setWidth(0, 0, "30px");

		// adds the buttons to the widget
		moveButtonsFt.setWidget(0,0, moveRight);
		moveButtonsFt.setWidget(0,1, closeAllButActiveButton);

		// styling
		moveButtonsFt.getElement().getStyle().setProperty("backgroundColor", "#cccccc");
		moveButtonsFt.getElement().getStyle().setProperty("paddingRight", "5px");
		moveButtonsFt.setWidth("60px");
		moveButtonsFt.setHeight("45px");

        // update move buttons position to be on right
		UiElements.addResizeCommand(new Command() {
			public void execute() {
				int clientWidth = (Window.getClientWidth() > WebGui.MIN_CLIENT_WIDTH) ?  Window.getClientWidth() : WebGui.MIN_CLIENT_WIDTH;
                // 90 = buttons width
                // 208 = main menu width
                int left = clientWidth - 60 - 208;
                contentPanel.setWidgetPosition(moveButtonsFt, left, 0);
            }
        });

        final FlexTable moveButtonsLeft = new FlexTable();
        FlexCellFormatter fcfl = moveButtonsLeft.getFlexCellFormatter();
        fcfl.setHorizontalAlignment(0,1, HasHorizontalAlignment.ALIGN_RIGHT);
        fcfl.setWidth(0, 0, "30px");
        moveButtonsLeft.getElement().getStyle().setProperty("backgroundColor", "#cccccc");
        moveButtonsLeft.getElement().getStyle().setProperty("paddingLeft", "5px");
        moveButtonsLeft.setWidth("30px");
        moveButtonsLeft.setHeight("45px");

        moveButtonsLeft.setWidget(0, 0, moveLeft);

        // SET TAB PANEL AS CONTENT
        contentPanel.add(tabPanel, 0, 0);

        // adds the buttons
        contentPanel.add(moveButtonsFt, 0, 0);
        contentPanel.add(moveButtonsLeft, 0, 0);

        // set initial position of tab panel
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                moveTabs(30, false);
            }
        });

    }

    /**
     * Generates standardized alert box for error messages based on passed params
     * It's used for GUI internal events, not for errors from RPC.
     *
     * @param header
     * @param text
     */
    static public void generateAlert(String header, String text) {

        FlexTable layout = new FlexTable();

        layout.setWidget(0, 0, new HTML("<p>"+new Image(LargeIcons.INSTANCE.errorIcon())));
        layout.setHTML(0, 1, "<p>"+text);

        layout.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
        layout.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
        layout.getFlexCellFormatter().setStyleName(0, 0, "alert-box-image");

        Confirm c = new Confirm(header, layout, true);
        c.setNonScrollable(true);
        c.show();

    }

    /**
     * Generates standardized alert box for error messages based on passed params
     * It's used for GUI internal events, not for errors from RPC.
     *
     * @param header
     * @param text
     */
    static public void generateAlert(String header, String text, ClickHandler handler) {

        FlexTable layout = new FlexTable();

        layout.setWidget(0, 0, new HTML("<p>"+new Image(LargeIcons.INSTANCE.errorIcon())));
        layout.setHTML(0, 1, "<p>"+text);

        layout.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
        layout.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
        layout.getFlexCellFormatter().setStyleName(0, 0, "alert-box-image");

        Confirm c = new Confirm(header, layout, handler, true);
        c.setNonScrollable(true);
        c.show();

    }

    /**
     * Generates standardized info box for info messages based on passed params
     * It's used for GUI internal events, not for errors from RPC.
     *
     * @param header
     * @param text
     */
    static public void generateInfo(String header, String text) {

        FlexTable layout = new FlexTable();

        layout.setWidget(0, 0, new HTML("<p>"+new Image(LargeIcons.INSTANCE.informationIcon())));
        layout.setHTML(0, 1, "<p>"+text);

        layout.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
        layout.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
        layout.getFlexCellFormatter().setStyleName(0, 0, "alert-box-image");

        Confirm c = new Confirm(header, layout, true);
        c.setNonScrollable(true);
        c.show();

    }

    /**
     * Shows dialog box with notice, that new value of attribute can't be saved
     *
     * @param object Attribute to display errorMessageFor
     */
    static public void cantSaveAttributeValueDialogBox(Attribute object){

        String text = "";
        String name = object.getDisplayName();
        if (object.getType().equalsIgnoreCase("java.lang.String")) {
            text = WidgetTranslation.INSTANCE.cantSaveAttributeValueDialogBoxWrongString(name);
        } else if (object.getType().equalsIgnoreCase("java.lang.Integer")) {
            text = WidgetTranslation.INSTANCE.cantSaveAttributeValueDialogBoxWrongInteger(name);
        } else if (object.getType().equalsIgnoreCase("java.util.ArrayList")) {
            text = WidgetTranslation.INSTANCE.cantSaveAttributeValueDialogBoxWrongList(name);
        } else {
            text = WidgetTranslation.INSTANCE.cantSaveAttributeValueDialogBoxGeneral(name);
        }
        generateAlert(WidgetTranslation.INSTANCE.cantSaveAttributeValueDialogBoxHeader(), text);

    }

    /**
     * Return false is list to be processed is null or empty and shows dialog box to users.
     *
     * @param list of objects to be processed
     * @return true list is ok / false if list null or empty
     */
    static public <T extends JavaScriptObject> boolean cantSaveEmptyListDialogBox(ArrayList<T> list){

        if (list == null || list.isEmpty()) {
            generateAlert(WidgetTranslation.INSTANCE.cantSaveEmptyListConfirmHeader(),
                    WidgetTranslation.INSTANCE.cantSaveEmptyListConfirmMessage());
            return false;
        }
        return true;

    }

    /**
     * Check if search string is empty or not. If empty, raise confirm dialog.
     *
     * @param searchString to check
     * @return true if searchString is not empty, falso if is empty
     */
    static public boolean searchStingCantBeEmpty(String searchString) {

        if (searchString == null || searchString.isEmpty()) {
            generateAlert(WidgetTranslation.INSTANCE.searchStringCantBeEmptyHeader(),
                    WidgetTranslation.INSTANCE.searchStringCantBeEmptyMessage());
            return false;
        } else {
            return true;
        }

    }

    /**
     * Display delete confirm, passed list is checked for null and emptiness
     * (if so, "cantSaveEmptyListDialogBox" method is called instead)
     *
     * @param list list of items meant for deletion (used to get display data)
     * @param okClickHandler OK action aka delete action itself
     */
    static public <T extends JavaScriptObject> void showDeleteConfirm(ArrayList<T> list, ClickHandler okClickHandler) {
        showDeleteConfirm(list, "", okClickHandler);
    }

    /**
     * Display delete confirm, passed list is checked for null and emptiness
     * (if so, "cantSaveEmptyListDialogBox" method is called instead)
     *
     * @param list list of items meant for deletion (used to get display data)
     * @param text custom text displayed above items list
     * @param okClickHandler OK action aka delete action itself
     */
    static public <T extends JavaScriptObject> void showDeleteConfirm(ArrayList<T> list, String text, ClickHandler okClickHandler) {

        if (cantSaveEmptyListDialogBox(list)) {

            String header = WidgetTranslation.INSTANCE.deleteConfirmTitle();

            FlexTable layout = new FlexTable();
            layout.setWidget(0, 0, new HTML("<p>"+new Image(LargeIcons.INSTANCE.errorIcon())));

            if (text == null || text.isEmpty()) {
                // default text
                layout.setHTML(0, 1, "<p><strong>"+WidgetTranslation.INSTANCE.deleteConfirmText()+"</strong>");
            } else {
                // custom text
                layout.setHTML(0, 1, "<p>"+text+"</p><p><strong>Do you want to proceed?</strong></p>");
            }

            layout.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
            layout.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
            layout.getFlexCellFormatter().setStyleName(0, 0, "alert-box-image");

            String items = new String("<ul>");
            for (JavaScriptObject object : list) {
                GeneralObject go = object.cast();
                if (go.getObjectType().equalsIgnoreCase("RichMember")) {
                    RichMember rm = go.cast();
                    items = items.concat("<li>"+rm.getUser().getFullName()+"</li>");
                } else if (go.getObjectType().equalsIgnoreCase("User") || go.getObjectType().equalsIgnoreCase("RichUser")) {
                    User u = go.cast();
                    items = items.concat("<li>"+u.getFullName()+"</li>");
                } else if (go.getObjectType().equalsIgnoreCase("RichDestination")) {
                    Destination d = go.cast();
                    items = items.concat("<li>"+d.getDestination()+" / "+d.getType()+" / "+d.getService().getName()+"</li>");
                } else if (go.getObjectType().equalsIgnoreCase("Host")) {
                    Host h = go.cast();
                    items = items.concat("<li>"+h.getName()+"</li>");
                } else if (go.getObjectType().equalsIgnoreCase("Facility") || go.getObjectType().equalsIgnoreCase("RichFacility")) {
                    Facility f = go.cast();
                    items = items.concat("<li>"+f.getName()+" ("+f.getType()+")"+"</li>");
                } else if (go.getObjectType().equalsIgnoreCase("Attribute") || go.getObjectType().equalsIgnoreCase("AttributeDefinition")) {
                    Attribute a = go.cast();
                    items = items.concat("<li>"+a.getDisplayName()+"</li>");
                } else if (go.getObjectType().equalsIgnoreCase("ExecService")) {
                    ExecService e = go.cast();
                    items = items.concat("<li>"+e.getService().getName()+" ("+e.getType()+")</li>");
                } else if (go.getObjectType().equalsIgnoreCase("UserExtSource")) {
                    UserExtSource ues = go.cast();
                    items = items.concat("<li>"+ues.getLogin()+" / "+ues.getExtSource().getName()+"</li>");
                } else if (go.getObjectType().equalsIgnoreCase("ApplicationMail")) {
                    //items = items.concat("<li>"+go.getName()+"</li>");
                    ApplicationMail mail = go.cast();
                    items = items.concat("<li>"+ApplicationMail.getTranslatedMailType(mail.getMailType())+"</li>");
                } else if (go.getObjectType().equalsIgnoreCase("PublicationForGUI") || go.getObjectType().equalsIgnoreCase("Publication")) {
                    Publication pub = go.cast();
                    items = items.concat("<li>"+pub.getTitle()+"</li>");
                } else if (go.getObjectType().equalsIgnoreCase("ThanksForGUI")) {
                    Thanks th = go.cast();
                    items = items.concat("<li>"+th.getOwnerName()+"</li>");
                } else if (go.getObjectType().equalsIgnoreCase("Author")) {
                    Author aut = go.cast();
                    items = items.concat("<li>"+aut.getDisplayName()+"</li>");
                } else {
                    items = items.concat("<li>"+go.getName()+"</li>");
                }

            }
            items = items.concat("</ul>");

            ScrollPanel sp = new ScrollPanel();
            sp.setStyleName("border");
            sp.setSize("100%", "100px");
            sp.add(new HTML(items));

            layout.getFlexCellFormatter().setRowSpan(0, 0, 2);
            layout.setWidget(1, 0, sp);

            Confirm c = new Confirm(header, layout, okClickHandler, true);
            c.setNonScrollable(true);
            c.show();

        }

    }

    /**
	 * Sets the content - if we don't want to use tabs
	 * @param child The content - eg. SimplePanel
	 */	
	public void setContent(Widget child) {
		contentPanel.clear();
		contentPanel.add(child);	
	}

	/**
	 * Returns the content 
	 * @return content
	 */
	public Widget getContent() {
		return contentPanel;
	}
	
	/**
	 * Scrolls to the current tab if tab widget not visible
	 */
	protected void showCurrentTabIfHidden() {

		Widget tabWidget = tabPanel.getTabWidget(tabPanel.getSelectedIndex());
		
		int left = tabWidget.getAbsoluteLeft();
		int width =  tabWidget.getOffsetWidth();
		int innerLeft = getTabsLeftOffset();
		
		final int MENU_WIDTH = 208;
		final int PADDING = 5;
		
		boolean tooLeft = left < MENU_WIDTH + PADDING;
		boolean tooRight = left + width + PADDING > (Window.getClientWidth());
		
		// if not in place, move
		if(tooLeft || tooRight)
		{
			// ALIGN LEFT // int mov = - left + MENU_WIDTH + innerLeft;
			// ALIGN CENTER // int mov = - left + MENU_WIDTH + innerLeft + ((Window.getClientWidth() - MENU_WIDTH) / 2) - width / 2;
			int mov = - left + MENU_WIDTH + innerLeft + ((Window.getClientWidth() - MENU_WIDTH) / 2) - width / 2;
			if(mov > 0){
				mov = 0;
			}
			moveTabs(mov, true);
		}	
	}

	/**
	 * Closes all tab but active
	 */
	private void closeAllTabButActive() {
		if(tabPanel.getSelectedIndex() == -1) return;
		
		Widget widgetToKeep = tabPanel.getWidget(tabPanel.getSelectedIndex());
		int idToKeep = -1;
		
		for(Map.Entry<Integer, Widget> entry : allTabs.entrySet())
		{
			int tabId = entry.getKey();
			// compares the two widgets
			if (widgetToKeep == entry.getValue())
			{
				idToKeep = tabId;
			}
			else
			{
				//for()
				tabPanel.remove(entry.getValue());
				
				// remove from tab manager
				session.getTabManager().removeTab(tabId);
			}
		}
		
		allTabs.clear();
		
		if(idToKeep != -1)
		{
			allTabs.put(idToKeep, widgetToKeep);
		}
		
		showCurrentTabIfHidden();

        getMenu().updateLinks();

	}

    /**
     * Closes all tabs
     */
    private void closeAllTabs() {
        if(tabPanel.getSelectedIndex() == -1) return;

        for(Map.Entry<Integer, Widget> entry : allTabs.entrySet())
        {
            int tabId = entry.getKey();
            //for()
            tabPanel.remove(entry.getValue());

            // remove from tab manager
            session.getTabManager().removeTab(tabId);
        }

        allTabs.clear();
        getMenu().updateLinks();
        getBreadcrumbs().clearLocation();

    }

	/**
	 * Returns the UNIQUE ID for tab positioned on index in TabPanel
	 * 
	 * @param i index of tab
	 * @return UNIQUE ID
	 */
	protected int getTabId(int i) {
		
		Widget w = tabPanel.getWidget(i);
		for(Map.Entry<Integer, Widget> entry : allTabs.entrySet())
		{
			// compares the two widgets
			if (w == entry.getValue())
			{
				return entry.getKey();
			}
		}
		return -1;
		
	}
	
	/**
	 * Returns UNIQUE ID of currently selected tab
	 * 
	 * @return UNIQUE ID
	 */
	public int getSelectedTabUniqueId() {
		return this.getTabId(tabPanel.getSelectedIndex());
	}
	
	/**
	 * Reloads content of tab specified by an index in TabPanel
	 * 
	 * @param i index of tab
	 */
	protected void reloadTab(int i) {
		Widget w = tabPanel.getWidget(i);
		for(Map.Entry<Integer, Widget> entry : allTabs.entrySet())
		{
			// compares the two widgets
			if (w == entry.getValue())
			{
				int tabToReloadId = entry.getKey();
				session.getTabManager().reloadTab(tabToReloadId);						
				break;
			}
		}
	}

	/**
	 * Setup log toggle button
	 * 
	 * @param up image to be showed
	 * @param down image to be showed
	 */
	private void prepareToggleLogButton(ImageResource up, ImageResource down) {
		logButton = new ToggleButton(new Image(up), new Image(down));
		logButton.setDown(logVisible);
		logButton.setTitle("Show / hide log");
		logButton.setPixelSize(16, 16);
		logButton.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				logVisible = !logVisible;
				setLogVisible(logVisible);
			}
		});
		logButtonWrapper.setWidget(logButton);
	}
	
	
	/**
	 * Shows or hides devel log
	 * 
	 * @param visible true = show log / false = hide log
	 */
	private native void setLogVisible(boolean visible)/*-{
		var logHeight = 200;
		var logBottomSpace = 30;
		
		if(visible)
		{
			$wnd.jQuery("#perun-log").animate({ bottom : logBottomSpace + "px"}, 'fast');
		}
		else
		{
			$wnd.jQuery("#perun-log").animate({ bottom : (- logHeight - 10) + "px" }, 'fast');
		}
	}-*/;
	
	/**
	 * Adds log entry into the devel log widget.
	 * 
	 * @param text text to be inserted in gui's devel log
	 */
	public void setLogText(String text) {
		
		// add time
		text = this.getFormattedLogText(text);

		// removes error or success indicator 5sec after page is displayed,
		// but only if there was such indicator
		if (log.getStyleName().contains("log-success") ||
				log.getStyleName().contains("log-error")) {

			Scheduler.get().scheduleDeferred(new Command() { 
				public void execute() {
					Timer time = new Timer() {
						@Override
						public void run() {
							// sets the normal log icon
							prepareToggleLogButton(SmallIcons.INSTANCE.bulletArrowUpIcon(), SmallIcons.INSTANCE.bulletArrowDownIcon());
							log.setStyleName("log-success", false);   // remove success indicator
							log.setStyleName("log-error", false);   // remove error indicator
						}};
						time.schedule(5000);
				}
			});	
		}
		//adds log at top
		this.logInside.insert(new HTML(text), logInside.getWidgetCount()); 
		// scroll to bottom
		this.log.getElement().setScrollTop(this.logInside.getElement().getScrollHeight());

	}

	/**
	 * Adds a Success log entry into the devel log widget
	 * 
	 * @param text text to be inserted in gui's devel log
	 */
	public void setLogSuccessText(String text) {
		
		// Add the text to status
		setStatus(text);

		// remove Error indicator if it was there
		this.log.setStyleName("log-error", false);
		// add success indicator	
		text = "<span style=\"color: green;\">" + this.getFormattedLogText(text) + "</span>";
		this.log.setStyleName("log-success", true);   
		//adds log at top
		this.logInside.insert(new HTML(text), logInside.getWidgetCount());
		// scroll to bottom
		this.log.getElement().setScrollTop(this.logInside.getElement().getScrollHeight());
		
	}

	/**
	 * Adds an Error log entry into the devel log widget
	 * 
	 * @param text text to be inserted in gui's devel log
	 */
	public void setLogErrorText(String text) {
		
		// sets the log button icon
		this.prepareToggleLogButton(SmallIcons.INSTANCE.exclamationIcon(), SmallIcons.INSTANCE.exclamationIcon());
		
		// remove Success indicator if it was there
		this.log.setStyleName("log-success", false);

		// add Error indicator	
		text = "<span style=\"color: red;\">" + this.getFormattedLogText(text) + "</span>";
		this.log.setStyleName("log-error", true);   
		//adds log at top
		this.logInside.insert(new HTML(text), logInside.getWidgetCount());
		// scroll to bottom
		this.log.getElement().setScrollTop(this.logInside.getElement().getScrollHeight());
		
	}

	/**
	 * Returns the message with a timestamp.
	 * Used for devel log entries.
	 * 
	 * @param message message
	 * @return Message with a timestamp
	 */
	private String getFormattedLogText(String message){
		return DateTimeFormat.getFormat("HH:mm:ss").format(new Date()) + ": " + message;
	}

	/**
	 * Returns devel log for GUI
	 * 
	 * @return log devel log widget
	 */
	public Widget getLog(){
		return this.log;	
	}

	/**
	 * Returns callback status widget
	 * @return Status widget
	 */
	public Widget getStatus(){
		return this.status;	
	}

	/**
	 * Sets a status string
	 */
	public native void setStatus(String text)/*-{
	
		clearTimeout($wnd.hideStatusTimeout);
	
		$wnd.jQuery("#perun-status").text(text);
		$wnd.jQuery("#perun-status").animate({ top: "0px" }, 200);
		
		// after a while, hide it
		$wnd.hideStatusTimeout = setTimeout(function(){
			$wnd.jQuery("#perun-status").animate({ top: "-300px" }, 500);
			$wnd.jQuery("#perun-status").text("");
		}, 5000);
		
	}-*/;

	/**
	 * Returns the MainMenu class
	 * 
	 * @return MainMenu menu
	 */
	public MainMenu getMenu() {
		return menu;
	}
	
	/**
	 * Returns the header widget
	 * 
	 * @return widget header
	 */
	public Widget getHeader() {
		
		final AbsolutePanel layout = new AbsolutePanel();
        layout.setStyleName("perun-header");

		// devel server?
		if(PerunWebConstants.INSTANCE.isDevel()){
			layout.addStyleName("develbg");
		}
		
		// image
		Image img = new Image("img/logo11.png");
        layout.add(img);
        layout.setWidgetPosition(img, 10, 5);

        breadcrumbs = new BreadcrumbsWidget();
        layout.add(breadcrumbs);
        layout.setWidgetPosition(breadcrumbs, 210, 15);

        layout.add(loggedUserInfo);

        UiElements.addResizeCommand(new Command() {
            public void execute() {
                int clientWidth = (Window.getClientWidth() > WebGui.MIN_CLIENT_WIDTH) ?  Window.getClientWidth() : WebGui.MIN_CLIENT_WIDTH;

                int width = clientWidth - 210 - loggedUserInfo.getOffsetWidth();
                breadcrumbs.setWidth(width + "px");
            }
        });

		return layout;
		
	}
	
	/**
	 * Returns the footer widget
	 * 
	 * @return widget footer
	 */
	public Widget getFooter(){
		
		FlexTable ft = new FlexTable();
		ft.addStyleName("perunFooter");

		FlexCellFormatter ftf = ft.getFlexCellFormatter();

        Anchor a = new Anchor("Perun web", PerunWebConstants.INSTANCE.footerPerunLink());
        a.setTarget("_blank");
        Anchor mail = new Anchor("perun@cesnet.cz", "mailto:"+PerunWebConstants.INSTANCE.perunReportEmailAddress());

        HTML foot = new HTML("<strong>About: </strong>"+a+"<strong>&nbsp;|&nbsp;Support: </strong>"+mail);
        ft.setWidget(0, 0, foot);

		ft.setWidget(0, 1, new HTML(PerunWebConstants.INSTANCE.footerPerunCopyright() + ", version: "+PerunWebConstants.INSTANCE.guiVersion()));
		ft.setWidget(0, 2, new HTML("<strong>"+ ButtonTranslation.INSTANCE.settingsButton()+": </strong>"));

		ftf.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
		ftf.setWidth(0, 0, "30%");
		ftf.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		ftf.setWidth(0, 1, "40%");
		ftf.setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);

		// toggle languages
		//ft.setWidget(0, 3, getToggleLanguageButton());
		//ftf.setWidth(0, 3, "30px");
		//ftf.setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);
		
		// toggle ids button
		ft.setWidget(0, 3, getExtendedInfoButton());
		ftf.setWidth(0, 3, "30px");
		ftf.setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);

		if ("true".equalsIgnoreCase(Location.getParameter("log"))) {
			// toggle log button
			ft.setWidget(0, 4, getToggleLogButton());
			ftf.setWidth(0, 4, "30px");
			ftf.setHorizontalAlignment(0,4, HasHorizontalAlignment.ALIGN_LEFT);
		}
		
		return ft;
	
	}

	/**
	 * Returns the log toggle button without any setup
	 * 
	 * @return Toggle Log button widget
	 */
	public Widget getToggleLogButton(){
		return this.logButtonWrapper;	
	}
	
	/**
	 * Return extended info button widget without any setup.
	 * 
	 * @return button widget
	 */
	public ToggleButton getExtendedInfoButtonWidget(){
		return extendedInfoButton;
	}
	
	/**
	 * Setup and return the extended info toggle button
	 * 
	 * @return button widget
	 */
	public ToggleButton getExtendedInfoButton()
	{
		final ToggleButton button = getExtendedInfoButtonWidget();
		button.setTitle(WidgetTranslation.INSTANCE.showHideExtendedInfo());
		button.setPixelSize(16, 16);
		button.setDown(JsonUtils.isExtendedInfoVisible());
		button.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

                JsonUtils.setExtendedInfoVisible(button.isDown());

                // save to local storage if possible
                Storage localStorage;
                try {
                    localStorage = Storage.getLocalStorageIfSupported();
                    if (localStorage != null) {
                        localStorage.setItem("urn:perun:gui:preferences:extendedInfo", String.valueOf(button.isDown()));
                    }
                } catch (Exception ex) {
                    // pass
                }

				// reloads the current tab (including overlay)
                TabItem tab = session.getTabManager().getActiveOverlayTab();
                if (tab != null) {
                    session.getTabManager().reloadTab(tab);
                } else {
                    tab = session.getTabManager().getActiveTab();
                    if (tab != null) {
                        session.getTabManager().reloadTab(tab);
                    }
                }

                // update visibility in menu
                getMenu().showAdvanced(button.isDown());

			}
		});
		return button;
	}
	
	/**
	 * Return language toggle button widget without any setup
	 * 
	 * @return button widget
	 */
	public ToggleButton getToggleLanguageButtonWidget() {
		return languageButton;
	}

    /**
     * Setup and return button with "change language" ability
     *
     * @return button widget
     */
    public ToggleButton getToggleLanguageButton() {

        // default = load by browser
        // en = english strings
        // cs = czech strings

        // english is fallback in all cases

        languageButton.setVisible(true); // display for perun admin only in WebGui.class

        if (!LocaleInfo.getCurrentLocale().getLocaleName().equals("cs")) {
            languageButton.setTitle(WidgetTranslation.INSTANCE.changeLanguageToCzech());
        } else {
            languageButton.setTitle(WidgetTranslation.INSTANCE.changeLanguageToEnglish());
        }

        languageButton.setPixelSize(16, 16);
        languageButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {

                Confirm conf = new Confirm(languageButton.getTitle(), new HTML(WidgetTranslation.INSTANCE.changeLanguageConfirmText()), new ClickHandler(){
                    public void onClick(ClickEvent event) {
                        // on OK
                        // set proper locale
                        String localeName = LocaleInfo.getCurrentLocale().getLocaleName();
                        if (!localeName.equals("cs")) {
                            localeName = "cs";
                            languageButton.setTitle(WidgetTranslation.INSTANCE.changeLanguageToEnglish());
                        } else {
                            localeName = "en";
                            languageButton.setTitle(WidgetTranslation.INSTANCE.changeLanguageToCzech());
                        }

                        // set locale param to URL or local storage
                        try {
                            Storage localStorage;
                            localStorage =  Storage.getLocalStorageIfSupported();
                            if (localStorage != null) {
                                localStorage.setItem("urn:perun:gui:preferences:language", localeName);
                                Window.Location.reload();
                            } else {
                                UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", localeName);
                                Window.Location.replace(builder.buildString());
                            }
                        } catch (Exception ex) {
                            UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", localeName);
                            Window.Location.replace(builder.buildString());
                        }
                        languageButton.setDown(false); // unclick button
                    }}, new ClickHandler(){
                    public void onClick(ClickEvent event) {
                        // on CANCEL
                        languageButton.setDown(false);
                    }
                }, true);
                conf.setNonScrollable(true);
                conf.show();
            }
        });
        return languageButton;

    }
	
	/**
	 * Return widget (content) of selected Tab. Used for later decision - if tab is selected.
	 * 
	 * It must not be called inside deferred functions, because selected widget can be different
	 * from which deferred function is placed.
	 * 
	 * @return selected widget
	 */
	public Widget getSelectedWidget() {
		
		return tabPanel.getWidget(tabPanel.getSelectedIndex());
		
	}

	/**
	 * Opens a Tab addressed by UNIQUE ID
	 * 
	 * @param uniqueTabId UNIQUE ID given by contentAddTab method
	 * @return true/false whether tab opened
	 */
	public boolean openTab(int uniqueTabId) {
		
		Widget widgetToSelect = allTabs.get(uniqueTabId);
		
		if(widgetToSelect == null) {
			return false; // tab not present
		}
		
		Widget nowSelectedWidget = getSelectedWidget();
		
		// not same widgets
		if(!widgetToSelect.equals(nowSelectedWidget)) {
			tabPanel.selectTab(widgetToSelect);
			// store last opened tab in inner history
			tabsHistory.remove((Object) uniqueTabId);
			tabsHistory.add(uniqueTabId);
		} else {
			// if same, force reload
			session.getTabManager().reloadTab(uniqueTabId);
		}
		
		session.getTabManager().openTab(uniqueTabId);
		
		return true;
	}
	
	/**
	 * Closes a tab addressed by UNIQUE ID
	 * 
	 * @param uniqueTabId UNIQUE ID given by contentAddTab method
	 */
	public void closeTab(int uniqueTabId) {
		
		Widget tab = allTabs.get(uniqueTabId);
		
		// switching tabs only if count > 1
		if(tabPanel.getWidgetCount() > 1)
		{
			// if tab is selected
			if (tabPanel.getSelectedIndex() == tabPanel.getWidgetIndex(tab))
			{
				// open tab from history
				if (!openTab(tabsHistory.get(tabsHistory.size() - 2))) {
                }
			}
		}

		// is / isn't selected always remove
		tabPanel.remove(tab);

        // remove from tab manager
        session.getTabManager().removeTab(uniqueTabId);

        // clear active tab if it was last to properly update menu
        if (tabPanel.getWidgetCount() == 0) {
            session.getTabManager().clearActiveTabs();
            getBreadcrumbs().clearLocation();
        }


		// remove from all tabs
		allTabs.remove(uniqueTabId);
		
		// remove tab from history
		tabsHistory.remove((Object) uniqueTabId);

        // update menu
        session.getUiElements().getMenu().updateLinks();

	}
	
	/**
	 * Creates a new tab
	 * 
	 * @param child Element inside the tab, usually ScrollPanel, or SimplePanel
	 * @param title Title of the tab
	 * @param closeButtonEnabled Whether is the close button enabled
	 * @return the unique tab id
	 */
	public int contentAddTab(final Widget child, String title, boolean closeButtonEnabled, ImageResource imgres) {
		return contentAddTab(child, new HTML(title),  closeButtonEnabled,  imgres);		
	}
	
	/**
	 * Creates a new tab
	 * 
	 * @param child Element inside the tab, usually ScrollPanel, or SimplePanel
	 * @param title Title of the tab
	 * @param closeButtonEnabled Whether is the close button enabled
	 * @return the unique tab id
	 */
	public int contentAddTab(Widget child, String title, boolean closeButtonEnabled) {
		return this.contentAddTab(child, title, closeButtonEnabled, null);
	}
	
	/**
	 * Creates a new tab
	 * 
	 * @param child Element inside the tab, usually ScrollPanel, or SimplePanel
	 * @param titleWidget Title of the tab
	 * @param openAfterAdding Whether the tab should be opened after adding
	 * @return the unique tab id
	 */
	public int contentAddTab(final Widget child, Widget titleWidget, boolean openAfterAdding, ImageResource imgres) {

        tabCount++;
		/* Creating a special tab with close button */
		
		// unique tab ID
		final int TAB_ID = tabCount;

		// wrapps the content
        /*
		final SimplePanel wrapper = new SimplePanel();
        wrapper.add(child);
		wrapper.addStyleName("tab-wrapper-" + TAB_ID);
         */

		// CHILD = ABSOLUTE PANEL child id - tab
		child.getElement().setId("tab-" + TAB_ID);

        // set class to tab content
        child.getElement().getFirstChildElement().addClassName("tab-content");

		// close button always enabled
		boolean closeButtonEnabled = true;
		
		// flex table in title definition
		FlexTable ft = new FlexTable();
		ft.addStyleName("perun-tabPanelItem");
		int ftcount = 0;
		
		// if image resource null, select default
		if(imgres == null){
			imgres = SmallIcons.INSTANCE.pageIcon();
		}
		
		// ICON / refresh button
		final Image TAB_IMAGE =  new Image(imgres);
		final Image REFRESH = new Image(SmallIcons.INSTANCE.arrowRotateClockwiseIcon());

		final PushButton refreshButton = new PushButton(TAB_IMAGE);
		refreshButton.setTitle(WidgetTranslation.INSTANCE.refreshTabButton());
		refreshButton.setSize("25px", "16px");
		refreshButton.setStyleName("pointer");
		refreshButton.getElement().getStyle().setProperty("outline", "none");

		refreshButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// reload
				session.getTabManager().reloadTab(TAB_ID);						
			}
		});
		// when mouse enters, change to reload icon
		refreshButton.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				Scheduler.get().scheduleDeferred(new Command() {
					public void execute() {
						refreshButton.getElement().setInnerHTML(REFRESH.getElement().getString());
					}
				});
			}
		});
		
		// when mouse leave, set to default icon
		refreshButton.addMouseOutHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				Scheduler.get().scheduleDeferred(new Command() {
					public void execute() {
						refreshButton.getElement().setInnerHTML(TAB_IMAGE.getElement().getString());
					}
				});
				
			}
		});
				
		ft.setWidget(0, ftcount, refreshButton);
		ft.getFlexCellFormatter().setWidth(0, ftcount, "25px");
		ftcount++;
		
		// title widget
		titleWidget.setStyleName("tabPanelTitle");
		ft.setWidget(0, ftcount, titleWidget);
		ft.getFlexCellFormatter().setHeight(0, ftcount, "20px");
		ftcount++;
		
		// Close button
		Button closeButton = new Button(
				"X", new ClickHandler() {
					public void onClick(ClickEvent event) {
						closeTab(TAB_ID);
					}
				});

		closeButton.addStyleName("tabPanelCloseButton");
		closeButton.setVisible(closeButtonEnabled);
        closeButton.setTitle(WidgetTranslation.INSTANCE.closeThisTab());
		ft.setWidget(0, ftcount, closeButton);
		ft.getFlexCellFormatter().setWidth(0, ftcount, "20px");
		ftcount++;

		// sets the flextable format
		ft.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

        /* !!! WE DO SET PAGE WIDTH AND HEIGHT USING CSS  !!! */
        /* Chrome 19+, Firefox 14+, Explorer 9+ */

        /* Opera and IE8 fallback */
        if (isOperaBeforeFifteen() || isExplorerBeforeNine()) {

            // autoresize
            UiElements.addResizeCommand(new Command() {
                public void execute() {

                    try {
                        int clientHeight = (Window.getClientHeight() > WebGui.MIN_CLIENT_HEIGHT) ? Window
                                .getClientHeight() : WebGui.MIN_CLIENT_HEIGHT;
                        int height = clientHeight - tabPanel.getAbsoluteTop()
                                - FOOTER_HEIGHT;
                        tabPanel.setHeight(height + "px");
                        height = clientHeight - child.getAbsoluteTop() - FOOTER_HEIGHT;

                        int clientWidth = (Window.getClientWidth() > WebGui.MIN_CLIENT_WIDTH) ? Window
                                .getClientWidth() : WebGui.MIN_CLIENT_WIDTH;

                        int width = clientWidth - child.getAbsoluteLeft() -5;

                        // tab content size
                        ((AbsolutePanel)child).getWidget(0).setHeight(height -5 + "px");
                        ((AbsolutePanel)child).getWidget(0).setWidth(width -5 + "px");
                        // absolute panel size
                        child.setHeight(height + "px");
                        child.setWidth(width+ "px");

                    } catch (Exception e) {}

                }
            });

        }
		
		// add the tab panel to the hashmap
		allTabs.put(TAB_ID, child);
		//tabsHistory.add(TAB_ID);
		
		// add after current
		//int newTabPosition = tabPanel.getSelectedIndex() + 1;
		
		// add to end
		int newTabPosition = tabPanel.getWidgetCount();
		
		// adding and opening
        tabPanel.insert(child, ft, newTabPosition);
        if(openAfterAdding) {
			tabPanel.selectTab(newTabPosition);
		}
		
		return TAB_ID;
		
	}

    public static native boolean isOperaBeforeFifteen() /*-{

        if(typeof opera != "undefined"){
            //do stuffs, for example
            return ($wnd.opera.version().indexOf("15.") == -1);
        }
        return false;

    }-*/;

    public static native boolean isExplorerBeforeNine() /*-{

        if (navigator.appName.indexOf("Internet Explorer") != -1) {
            var number = navigator.appVersion.match(/MSIE ([\d.]+)/)[1];
            if (number != null) {
                if (number < 9.0) {
                    return true;
                }
            }
        }
        return false;

    }-*/;
	
	/**
	 * Changes the position of tab headers - when more tabs than page width
	 * @param position
	 * @boolean absolute;
	 */
	public static native void moveTabs(int position, boolean absolute) /*-{
		
		// get current
		var left;
		var newLeft;
		
		if (absolute) {
			newLeft = position;
			left = 30;
		} else {
			left = parseInt($wnd.jQuery(".mainTabPanel .gwt-TabLayoutPanelTabs").first().css("left"), 10);
		 	newLeft = left + position;
		}
		
		// if wrong value
		if(newLeft>30){
		
			// if already 0
			if(left == 30){
				return;
			}
			
			// if higher - move to 0
			newLeft = 30;
		}
		
		// update - without animation
		//$wnd.jQuery(".mainTabPanel .gwt-TabLayoutPanelTabs").first().css("left", newLeft + "px");
		
		// update - with animation
		$wnd.jQuery(".mainTabPanel .gwt-TabLayoutPanelTabs").first().animate({ left : newLeft }, 'fast');
		
	}-*/;
	
	/**
	 * Return tabs offset
	 * @return tabsLeftOffset
	 */
	public static native int getTabsLeftOffset() /*-{
		
		// get current
		return parseInt($wnd.jQuery(".mainTabPanel .gwt-TabLayoutPanelTabs").first().css("left"), 10);
		
	}-*/;


	
	/**
	 * Sets the current user information to the header
	 * based on received PerunPrincipal from RPC
	 * 
	 * @param pp PerunPrincipal
	 */
	public void setLoggedUserInfo(final PerunPrincipal pp) {

        loggedUserInfo.addStyleName("logged-user-info");

		final Anchor nameHyperlink;
		
		if(pp.getUser() != null)
		{
			// name
			nameHyperlink = new Anchor(pp.getUser().getFullNameWithTitles());
            nameHyperlink.setHTML("<strong>"+nameHyperlink.getHTML()+"</strong>");
			nameHyperlink.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent arg0) {
					if (session.getUser() != null) {
						session.getTabManager().addTab(new SelfDetailTabItem(session.getUser()));
					}
				}
			});
			
		}else{
			nameHyperlink = new Anchor(pp.getActor());
		}
		
		nameHyperlink.setTitle("Ext. source: " + pp.getExtSource() + " (" + pp.getActor() + ")");
		
		// process roles to display
		String roles = "";
        // only self
        if (session.isSelf() && !(session.isPerunAdmin() || session.isVoAdmin() || session.isGroupAdmin() || session.isFacilityAdmin()) ) {
            roles += "SELF";
        } else if (session.isPerunAdmin()) {
            roles += "PERUN ADMIN";
        } else {

            // display only if not Perun admin
            if (session.isVoAdmin()) { roles += "VO/"; }
            if (session.isGroupAdmin()) { roles += "GROUP/"; }
            if (session.isFacilityAdmin()) { roles += "FACILITY/"; }

            if (roles.length() >= 1) {
                roles = roles.substring(0, roles.length()-1);
                roles += " MANAGER";
            }

        }
        if (roles.length() == 0) { roles = "N/A"; }
		
		// set layout
		loggedUserInfo.setHTML(0, 0, "<strong>Name:</strong>");
		loggedUserInfo.setWidget(0, 1, nameHyperlink);

        loggedUserInfo.setHTML(1, 0, "<strong>Role:</strong>");
        loggedUserInfo.setText(1, 1, roles);

        // IF user can change identity
        /*
        if (session.getEditableUsers().size()>1) {

            // set change identity widget
            final FlexTable content = new FlexTable();
            final ListBoxWithObjects<User> users = new ListBoxWithObjects<User>();
            content.setCellPadding(2);
            content.setHTML(0, 0, "<strong>Select&nbsp;identity: </strong>");
            content.setWidget(0, 1, users);
            content.setWidget(1, 0, new Image(LargeIcons.INSTANCE.errorIcon()));
            content.setHTML(1, 1, "Change of identity will close all currently opened tabs !");
            content.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);

            // prepare link
            CustomButton selectButton = new CustomButton(ButtonTranslation.INSTANCE.selectIdentityButton(), ButtonTranslation.INSTANCE.select(), SmallIcons.INSTANCE.userGrayIcon(), new ClickHandler() {
                public void onClick(ClickEvent clickEvent) {

                    if (users.isEmpty()) {

                        users.addItem("Loading...");

                        for (int u : session.getEditableUsers()) {
                            // use cached callback
                            GetEntityById call = new GetEntityById(PerunEntity.USER, u, new JsonCallbackEvents(){
                                public void onFinished(JavaScriptObject jso){
                                    if (jso != null) {
                                        userCallcounter++;
                                        usersList.add((User)jso);
                                    }
                                }
                                public void onError(PerunError error) {
                                    // silently skip not loaded users
                                    userCallcounter++;
                                }
                            });
                            call.setCacheEnabled(true);
                            call.retrieveData();
                        }

                        // sort and select when finished
                        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
                            @Override
                            public boolean execute() {
                                if (userCallcounter == session.getEditableUsers().size()) {
                                    users.clear();
                                    usersList = new TableSorter<User>().sortByName(usersList);
                                    for (User u : usersList){
                                        users.addItem(u);
                                        if (u.getId() == session.getUser().getId()) {
                                            users.setSelected(u, true);
                                        }
                                    }
                                    usersList.clear();
                                    userCallcounter = 0;
                                    return false;
                                }
                                return true;
                            }
                        }, 200);

                    }

                    Confirm c = new Confirm("Select identity", content, new ClickHandler() {
                        public void onClick(ClickEvent clickEvent) {
                            if (session.getUser().getId() == users.getSelectedObject().getId()) {
                                // identity not changed
                                return;
                            }
                            // set user to perun principal
                            PerunPrincipal pp2 = session.getPerunPrincipal();
                            pp2.setUser(users.getSelectedObject());
                            session.setPerunPrincipal(pp2);
                            // update links in menu
                            session.getUiElements().getMenu().updateLinks();
                            closeAllTabs();
                            nameHyperlink.setText(users.getSelectedObject().getFullNameWithTitles());
                        }
                    }, true);

                    c.show();

                }
            });

            loggedUserInfo.setWidget(0, 2, selectButton);
            loggedUserInfo.getFlexCellFormatter().setWidth(0, 2, "60px");
            loggedUserInfo.getFlexCellFormatter().setRowSpan(0, 2, 2);
            LogoutButton logoutButton = new LogoutButton();
            loggedUserInfo.setWidget(0, 3, logoutButton);
            loggedUserInfo.getFlexCellFormatter().setWidth(0, 3, "60px");
            loggedUserInfo.getFlexCellFormatter().setRowSpan(0, 3, 2);

        } else {

            // user can't change identity
            LogoutButton logoutButton = new LogoutButton();
            loggedUserInfo.setWidget(0, 2, logoutButton);
            loggedUserInfo.getFlexCellFormatter().setRowSpan(0, 2, 2);
            loggedUserInfo.getFlexCellFormatter().setWidth(0, 2, "60px");

        }
        */

        // user can't change identity
        LogoutButton logoutButton = new LogoutButton();
        loggedUserInfo.setWidget(0, 2, logoutButton);
        loggedUserInfo.getFlexCellFormatter().setRowSpan(0, 2, 2);
        loggedUserInfo.getFlexCellFormatter().setWidth(0, 2, "60px");

    }
	
	
	/**
	 * Use this method to resize perun tables to their maximum possible size after page is fully loaded
	 * 
	 * @param perunTable - Widget element with perun table to be resized
	 * @param minHeight - integer that defines table minimal height (used for resize action of browser)
	 */
	public void resizePerunTable(final Widget perunTable, final int minHeight) {
		this.resizePerunTable(perunTable, minHeight, 0);
	}

	/**
	 * Use this method to resize perun tables to their maximum possible size after page is fully loaded
	 * 
	 * @param perunTable - Widget element with perun table to be resized
	 * @param minHeight - integer that defines table minimal height (used for resize action of browser)
	 * @param tabItem Tab to resize widget for
	 */
	public void resizePerunTable(final Widget perunTable, final int minHeight, TabItem tabItem) {
		this.resizePerunTable(perunTable, minHeight, 0, tabItem);
	}

	
	/**
	 * Use this method to resize perun tables to their maximum possible size after page is fully loaded
	 * Custom parameter - the space for menus etc.
	 * 
	 * @param perunTable - Widget element with perun table to be resized
	 * @param minHeight - integer that defines table minimal height (used for resize action of browser)
	 * @param correction - how many pixels keep free (correction)
	 */
	public void resizePerunTable(final Widget perunTable, final int minHeight, final int correction) {
		resizePerunTable(perunTable, minHeight, correction, null);
	}

	
	/**
	 * Use this method to resize perun tables to their maximum possible size after page is fully loaded
	 * Custom parameter - the space for menus etc.
	 * 
	 * @param perunTable - Widget element with perun table to be resized
	 * @param minHeight - integer that defines table minimal height (used for resize action of browser)
	 * @param correction - how many pixels keep free (correction)
	 * @param tabItem Tab item for which is resizing applied
	 */
	public void resizePerunTable(final Widget perunTable, final int minHeight, final int correction, final TabItem tabItem) {
		generateResizingCommand(perunTable, minHeight, correction, tabItem, true);
	}
	
	/**
	 * Use this method to resize small tab panels to their maximum possible size after page is fully loaded
	 * 
	 * @param tabPanel - Widget element with small tab panel to be resized
	 * @param minHeight - integer that defines table minimal height (used for resize action of browser)
	 */
	public void resizeSmallTabPanel(final Widget tabPanel, final int minHeight) {
		this.resizeSmallTabPanel(tabPanel, minHeight, 0);
	}
	
	/**
	 * Use this method to resize small tab panels to their maximum possible size after page is fully loaded
	 * 
	 * @param tabPanel - Widget element with small tab panel to be resized
	 * @param minHeight - integer that defines table minimal height (used for resize action of browser)
	 * @param tabItem Tab to resize widget for
	 */
	public void resizeSmallTabPanel(final Widget tabPanel, final int minHeight, TabItem tabItem) {
		this.resizeSmallTabPanel(tabPanel, minHeight, 0, tabItem);
	}


	/**
	 * Use this method to resize samll tab panels to their maximum possible size after page is fully loaded
	 * Custom parameter - the space for menus etc.
	 * 
	 * @param tabPanel - Widget element with small tab panelto be resized
	 * @param minHeight - integer that defines table minimal height (used for resize action of browser)
	 * @param correction - how many pixels keep free (correction)
	 */
	public void resizeSmallTabPanel(final Widget tabPanel, final int minHeight, final int correction) {
		resizeSmallTabPanel(tabPanel, minHeight, correction, null);
		
	}
	
	
	/**
	 * Use this method to resize samll tab panels to their maximum possible size after page is fully loaded
	 * Custom parameter - the space for menus etc.
	 * 
	 * @param tabPanel - Widget element with small tab panelto be resized
	 * @param minHeight - integer that defines table minimal height (used for resize action of browser)
	 * @param correction - how many pixels keep free (correction)
	 * @param tabItem Tab to resize widget for
	 */
	public void resizeSmallTabPanel(final Widget tabPanel, final int minHeight, final int correction, final TabItem tabItem) {
		generateResizingCommand(tabPanel, minHeight, correction, tabItem, false);
	}
	
	
	/**
	 * Private method for generating resizing commands for GUI
	 * 
	 * @param panel
	 * @param minHeight
	 * @param correction
	 * @param tabItem
	 * @param resizeWidth
	 */
	private void generateResizingCommand(final Widget panel, final int minHeight, final int correction, final TabItem tabItem, final boolean resizeWidth)
	{
		// resizes the perun table with 34px free space (footer)
		final int freeSpace = correction + 34; 
		
		// generates the command
		Command c = new Command() { 
			public void execute() {
				
				if(panel.getParent() == null) return; 

                int clientHeight = ( Window.getClientHeight() > WebGui.MIN_CLIENT_HEIGHT) ?  Window.getClientHeight() : WebGui.MIN_CLIENT_HEIGHT;
				int height = clientHeight-panel.getAbsoluteTop()-freeSpace;
				if (height>0) {
                    panel.setHeight(height+"px");
                }

                /* WE CAN SET WIDTH BY CSS NOW */
                if (isOperaBeforeFifteen() || isExplorerBeforeNine()) {
                    /* Opera and IE8 fallback */
                    if(resizeWidth) {
                        int clientWidth = (Window.getClientWidth() > WebGui.MIN_CLIENT_WIDTH) ?  Window.getClientWidth() : WebGui.MIN_CLIENT_WIDTH;
                        int width = clientWidth - MainMenu.MENU_WIDTH - 10;
                        if (panel.getParent().getElement().getOffsetWidth() < width) {
                            width = panel.getParent().getElement().getOffsetWidth()-10;
                        }
                        if (width>0) {
                            panel.setWidth(width+"px");
                        }
                    }
                }

			}
		};

		addResizeCommand(c, tabItem);
		
	}
	
	/**
	 * Returns the loading box for perun
	 * @return
	 */
	public PopupPanel perunLoadingBox() {
		return perunLoadingBox("Loading Perun");	
	}
	
	/**
	 * Returns the loading box for perun
	 * @param customText
	 * @return
	 */
	public PopupPanel perunLoadingBox(String customText)
	{
		PopupPanel box = new DecoratedPopupPanel();
		box.setGlassEnabled(true);
		
		VerticalPanel vp = new VerticalPanel();
		vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		vp.add(new HTML("<h2>" + customText + "</h2>"));
		vp.add(new Image(AjaxLoaderImage.IMAGE_URL));
		vp.setSpacing(10);
		
		box.add(vp);
		box.setModal(true);
		box.center();
		
		
		return box;
	}
	
	/**
	 * Returns the loading failed box for perun
	 * @return
	 */
	public PopupPanel perunLoadingFailedBox(String error)
	{
		PopupPanel box = new DecoratedPopupPanel();
		box.setGlassEnabled(true);
		
		VerticalPanel vp = new VerticalPanel();
		vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		vp.add(new HTML("<h2 class=\"serverResponseLabelError\">" + "Authentication failed: " + error +"</h2>"));
		
		box.add(vp);
		box.setModal(true);
		box.center();

		return box;
	}

	/**
	 * Updates the window title, appends UiElements.TITLE_SUFFIX
	 * @param title New title (eg. when page changed)
	 */
	public void setWindowTitle(String title){
		Window.setTitle(title + TITLE_SUFFIX);
	}
	
	
	/**
	 * Adds a resize command for a selected tab, which is executed when page rezises AND json callback finishes
	 * 
	 * @param command Command for resizing
	 * @param tabItem Tab item to add resize commands for
	 */
	static public void addResizeCommand(Command command, TabItem tabItem){
		Scheduler.get().scheduleDeferred(command);
		
		if(tabItem != null) {
			if(resizeCommandsForTabs.get(tabItem) == null){
				resizeCommandsForTabs.put(tabItem, new HashSet<Command>());				
			}
			resizeCommandsForTabs.get(tabItem).add(command);
		} else {
			resizeCommands.add(command);			
		}
	}
	
	/**
	 * Adds a resize command, which is executed when page rezises AND json callback finishes
	 * 
	 * @param command Command for resizing
	 */
	static public void addResizeCommand(Command command){
		addResizeCommand(command, null);
	}
	
	/**
	 * Runs the resize commands for active tabs + shared resize.
	 */
	static public void runResizeCommands(){
		runResizeCommands(false);
	}
	
	/**
	 * Runs the resize commands for all tabs.
     *
     * @param allTabs TRUE = all tabs / False = only shared resize + current tab
	 */
	static public void runResizeCommands(boolean allTabs){
		
		// SHARED COMMANDS
		for(Command command : resizeCommands) {
			Scheduler.get().scheduleDeferred(command);
		}
				
		// RUN ALL
		if(allTabs){
			for(Map.Entry<TabItem, Set<Command>> entry : resizeCommandsForTabs.entrySet()) {
				for(Command command : entry.getValue()){
					Scheduler.get().scheduleDeferred(command);
				}
			}
			// if all, exits
			return;
		}
		
		// RUN CUSTOM
		runResizeCommandsForCurrentTab();

	}
	
	
	/**
	 * Runs the resize commands for custom tab
     *
     * @param tab tab to trigger resize commands for
	 */
	static public void runResizeCommands(TabItem tab) {
		Set<Command> commandQueue = resizeCommandsForTabs.get(tab);
		if(commandQueue != null) {
			// run resize queue for each tab
			for(Command command : resizeCommandsForTabs.get(tab)){
				Scheduler.get().scheduleDeferred(command);
			}
		}
	}


    /**
     * Runs resize commands for the active tab
     */
    static public void runResizeCommandsForCurrentTab() {

        if(PerunWebSession.getInstance().getTabManager() == null) {
            return;
        }

        TabItem activeTab = PerunWebSession.getInstance().getTabManager().getActiveTab();
        TabItem activeOverlayTab = PerunWebSession.getInstance().getTabManager().getActiveOverlayTab();

        // if no active tab, do nothing
        if(activeTab != null){
            runResizeCommands(activeTab);
        }

        // if no active overlay tab, do nothing
        if(activeOverlayTab != null){
            runResizeCommands(activeOverlayTab);
        }

    }

    /**
     * Get breadcrumbs widget located in the top of gui
     *
     * @return breadcrumbs widget
     */
    public BreadcrumbsWidget getBreadcrumbs() {
        return this.breadcrumbs;
    }

}