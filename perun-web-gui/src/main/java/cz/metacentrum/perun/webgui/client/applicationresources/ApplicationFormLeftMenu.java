package cz.metacentrum.perun.webgui.client.applicationresources;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.applicationresources.pages.ApplicationPage;
import cz.metacentrum.perun.webgui.client.localization.ApplicationMessages;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;

import java.util.ArrayList;

/**
 * Left menu used in application form gui
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ApplicationFormLeftMenu extends Composite{
	
	/**
	 * Stack panel with currently one item - "Application form"
	 */
	private StackPanel stackPanel = new StackPanel();
	
	private FlexTable menuContents = new FlexTable();
	
	private SimplePanel content = new SimplePanel();

    private ArrayList<Anchor> listOfLinks = new ArrayList<Anchor>();
	
	/**
	 * Creates the left menu & navigation
	 */
	public ApplicationFormLeftMenu(){
		
		this.initWidget(stackPanel);
		
		listOfLinks.clear();

		SimplePanel menuContantsWrapper= new SimplePanel(menuContents);
		menuContantsWrapper.setHeight("100%");
		stackPanel.addStyleName("menuStackPanel");

		// stack panel header & image
		Widget header = new HTML("<span class=\"menu-label\">" + ApplicationMessages.INSTANCE.applicationForm() + "</span>");
		header.addStyleName("stackPanelHeaderLink");
		Element i = new Image(LargeIcons.INSTANCE.applicationFormIcon()).getElement();
		i.addClassName("stackPanelHeaderImage");
		header.getElement().appendChild(i);
		
		// creating the section
		stackPanel.add(menuContantsWrapper, header.getElement().getString(), true);
		stackPanel.setHeight("100%");
		stackPanel.setWidth("280px");
		
		menuContents.setWidth("100%");
		menuContents.setStyleName("mainMenuItems");

	}
	
	
	/**
	 * Adds the item to the menu
	 * 
	 * @param label
	 * @param res
	 * @param w
     * @return anochor we can click on
	 */
	private Anchor addMenuContents(String label, ImageResource res, final Widget w)
	{
		int i = menuContents.getRowCount();

        Image img = new Image(res);
        menuContents.setWidget(i, 0, img);
		
		// user click on the menu item
		final Anchor link = new Anchor(label);
		listOfLinks.add(link);
        link.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				
				if(w instanceof ApplicationPage)
				{
					ApplicationPage ap = (ApplicationPage) w;
					ap.menuClick();
				}
                for (Anchor l : listOfLinks) {
                    // remove others
                    l.removeStyleName("mainMenuActive");
                }
                // bold to selected
                link.addStyleName("mainMenuActive");
				content.setWidget(w);				
			}
		});
		
		menuContents.setWidget(i, 1, link);

        return link;
	}
	
	/**
	 * Adds the item
	 * 
	 * @param label label
	 * @param res image
	 * @param w content
     * @return anchor we can click on
	 */
	public Anchor addItem(String label, ImageResource res, Widget w){
		
		Anchor a = addMenuContents(label, res, w);
		
		// if content empty, add
		if(content.getWidget() == null)
		{
			content.setWidget(w);
		}

        return a;

	}

	/**
	 * Returns the page contents
	 * @return
	 */
	public Widget getContent() {
		return this.content;
	}

}
