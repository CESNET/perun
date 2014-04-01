package cz.metacentrum.perun.webgui.client.mainmenu;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;

import java.util.ArrayList;

/**
 * Section definition in the left menu
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MainMenuSection {

	private int rowCounter = 0;
	private FlexTable sectionPanel = new FlexTable();
	private ArrayList<MainMenuItem> items = new ArrayList<MainMenuItem>();
	private String title = "";
	private Image image;
	private TabItemWithUrl tabItem;
	private boolean displayAdvanced = JsonUtils.isExtendedInfoVisible();
	private int role;

	public MainMenuSection(String title, TabItemWithUrl tabItem){
		this.title = title;
		this.tabItem = tabItem;
		sectionPanel.addStyleName("mainMenuItems");
		sectionPanel.setWidth("100%");
	}

	public MainMenuSection(String title, TabItemWithUrl tabItem, Image image){
		this(title, tabItem);
		this.image = image;
	}

	public MainMenuSection(String title, TabItemWithUrl tabItem, ImageResource imageResource){
		this(title, tabItem, new Image(imageResource));
	}

	public MainMenuSection(String title, TabItemWithUrl tabItem, ImageResource imageResource, int role){
		this(title, tabItem, new Image(imageResource));
		this.role = role;
	}

	public MainMenuItem addItem(MainMenuItem item){

		this.sectionPanel.setWidget(rowCounter, 0, item.getIcon());
		this.sectionPanel.setWidget(rowCounter, 1, item.getWidget());

		items.add(item);

		this.sectionPanel.getFlexCellFormatter().setWidth(rowCounter, 0, "20px");
		this.sectionPanel.getFlexCellFormatter().setVerticalAlignment(rowCounter, 0, HasVerticalAlignment.ALIGN_TOP);
		this.sectionPanel.getFlexCellFormatter().setVerticalAlignment(rowCounter, 1, HasVerticalAlignment.ALIGN_TOP);

		rowCounter++;

		return item;

	}

	public void addAdvancedLink(boolean active) {

		if (!JsonUtils.isExtendedInfoVisible()) {

			if (active) {

				final Anchor a = new Anchor("");

				if (displayAdvanced) {
					a.setHTML("<i>&lt;&lt; hide advanced</i>");
				} else {
					a.setHTML("<i>show advanced &gt;&gt;</i>");
				}

				a.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						displayAdvanced = !displayAdvanced;
						// rebuild this menu
						PerunWebSession.getInstance().getUiElements().getMenu().saveAdvancedStateToBrowser(role+"", displayAdvanced);
						PerunWebSession.getInstance().getUiElements().getMenu().updateLinks(role);
					}
				});

				this.sectionPanel.setWidget(rowCounter, 0, a);
				this.sectionPanel.getFlexCellFormatter().setColSpan(rowCounter, 0, 2);

			} else {

				HTML a = new HTML();
				a.addStyleName("mainMenuNotActive");
				if (displayAdvanced) {
					a.setHTML("<i>&lt;&lt; hide advanced</i>");
				} else {
					a.setHTML("<i>show advanced &gt;&gt;</i>");
				}
				this.sectionPanel.setWidget(rowCounter, 0, a);
				this.sectionPanel.getFlexCellFormatter().setColSpan(rowCounter, 0, 2);

			}

			rowCounter++;

		}

	}

	public void addSplitter() {

		if (rowCounter > 0)  {
			sectionPanel.getFlexCellFormatter().addStyleName(rowCounter-1, 0, "mainMenuSplitter");
			sectionPanel.getFlexCellFormatter().addStyleName(rowCounter-1, 1, "mainMenuSplitter");
		}

	}

	public Widget getWidget()
	{
		return this.sectionPanel;
	}

	public String getHeader()
	{
		Widget header = new HTML("<span class=\"menu-label\">" + title + "</span>");
		header.addStyleName("stackPanelHeaderLink");
		//header.setWidth("185px");

		// appends image
		if(image != null){
			Element i = image.getElement();
			i.addClassName("stackPanelHeaderImage");
			header.getElement().appendChild(i);
		}

		return header.getElement().getString();
	}

	public ArrayList<MainMenuItem> getItems() {
		return items;
	}

	public TabItemWithUrl getTabItem() {
		return tabItem;
	}

	public void setTabItem(TabItemWithUrl tabItem) {
		this.tabItem = tabItem;
	}

	public void clear() {
		sectionPanel.removeAllRows();
		rowCounter = 0;
		this.items.clear();
	}

	public boolean isDisplayAdvanced() {
		return this.displayAdvanced;
	}

	public void setDisplayAdvanced(boolean advanced) {
		this.displayAdvanced = advanced;
	}

}
