package cz.metacentrum.perun.webgui.client.passwordresetresources;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;

import java.util.ArrayList;

/**
 * Left menu used in password reset form gui
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PasswordResetLeftMenu extends Composite {

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
	public PasswordResetLeftMenu(){

		this.initWidget(stackPanel);

		listOfLinks.clear();

		SimplePanel menuContentsWrapper= new SimplePanel(menuContents);
		menuContentsWrapper.setHeight("100%");
		stackPanel.addStyleName("menuStackPanel");

		// stack panel header & image
		Widget header = new HTML("<span class=\"menu-label\">" + "Menu" + "</span>");
		header.addStyleName("stackPanelHeaderLink");
		Element i = new Image(LargeIcons.INSTANCE.keyIcon()).getElement();
		i.addClassName("stackPanelHeaderImage");
		header.getElement().appendChild(i);

		// creating the section
		stackPanel.add(menuContentsWrapper, header.getElement().getString(), true);
		stackPanel.setHeight("100%");
		stackPanel.setWidth("240px");

		menuContents.setWidth("100%");
		menuContents.setStyleName("mainMenuItems");

	}


	/**
	 * Adds the item to the menu
	 *
	 * @param label
	 * @param res
	 * @param w
	 */
	public Anchor addMenuContents(String label, ImageResource res, final Widget w) {

		int rowCount = menuContents.getRowCount();
		menuContents.setWidget(rowCount, 0, new Image(res));


		// user click on the menu item
		final Anchor link = new Anchor(label);
		listOfLinks.add(link);
		link.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (Anchor l : listOfLinks) {
					// remove others
					l.removeStyleName("mainMenuActive");
				}
				// bold to selected
				link.addStyleName("mainMenuActive");
				content.setWidget(w);
			}
		});

		menuContents.setWidget(rowCount, 1, link);

		return link;

	}

	/**
	 * Adds the item
	 *
	 * @param label label
	 * @param res image
	 * @param w content
	 */
	public void addItem(String label, ImageResource res, Widget w){

		addMenuContents(label, res, w);

		// if content empty, add
		if(content.getWidget() == null)
		{
			content.setWidget(w);
		}
	}

	/**
	 * Returns the page contents
	 * @return
	 */
	public Widget getContent() {
		return this.content;
	}

}
