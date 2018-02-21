package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.model.User;

import java.util.ArrayList;

/**
 * Widget for listing added / removed items
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class AddRemoveItemsTable<T extends JavaScriptObject> extends Composite {

	private ScrollPanel widget = new ScrollPanel();
	private ArrayList<T> list = new ArrayList<T>();
	private HandleItemsAction events = new HandleItemsAction<T>() {
		@Override
		public void onAdd(T object) {
			// default empty
		}

		@Override
		public void onRemove(T object) {
			// default empty
		}
	};

	// list items vertically = true / horizontally = false
	private boolean vertical = true;

	/**
	 * Creates a new instance of NotAuthorizeWidget
	 */
	public AddRemoveItemsTable(boolean vertical) {
		this.vertical = vertical;
		this.initWidget(widget);
		PerunWebSession.getInstance().getUiElements().resizeSmallTabPanel(widget, 350);
		buildWidget();
	}

	/**
	 * Sets actions done onAdd and onRemove
	 * @param action actions
	 */
	public void setEvents(HandleItemsAction action){
		this.events = action;
	}

	/**
	 * Add object to widget and rebuild it
	 * if item is already present no action is done
	 *
	 * @param object item
	 */
	public void addItem(T object) {
		if (list.contains(object)) { return; }
		list.add(object);
		buildWidget();
		events.onAdd(object);
	}

	/**
	 * Add list of objects to widget and rebuild it
	 * if item is already present no action is done
	 *
	 * @param newList list of items to add
	 */
	public void addItems(ArrayList<T> newList) {

		boolean wasAdded = false;
		for (T object : newList) {
			if (list.contains(object)) {
				continue;
			} else {
				wasAdded = true;
				list.add(object);
				events.onAdd(object);
			}
		}
		if (wasAdded) {
			buildWidget();
		}

	}

	/**
	 * Removes object from widget and rebuild it
	 *
	 * @param object item
	 */
	public void removeItem(T object) {
		list.remove(object);
		events.onRemove(object);
		buildWidget();
	}

	/**
	 * Rebuild widget GUI
	 */
	private void buildWidget() {

		FlexTable ft = new FlexTable();
		ft.setStyleName("perun-table");
		widget.clear();
		widget.add(ft);

		if (list.isEmpty()) {
			ft.setHTML(0, 0, "<strong>No items found.</strong>");
			ft.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
		}

		int row = 0;
		int column = 0;
		for (final T object : list) {

			GeneralObject go = object.cast();
			String name = "";
			if ("User".equalsIgnoreCase(go.getObjectType())) {
				User u = go.cast();
				name = u.getFullNameWithTitles();
			} else if ("RichUser".equalsIgnoreCase(go.getObjectType())) {
				User u = go.cast();
				name = u.getFullNameWithTitles();
			} else if ("RichMember".equalsIgnoreCase(go.getObjectType())) {
				RichMember m = go.cast();
				name = m.getUser().getFullNameWithTitles();
			} else {
				name = ((GeneralObject)object).getName();
			}

			CustomButton rb = new CustomButton("", "Remove item", SmallIcons.INSTANCE.deleteIcon());
			rb.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent clickEvent) {
					removeItem(object);
				}
			});

			if (vertical) {
				ft.setHTML(row, 0, SafeHtmlUtils.fromString(name).asString());
				ft.setWidget(row, 1, rb);
				row++;
			} else {
				ft.setHTML(row, column, SafeHtmlUtils.fromString(name).asString());
				ft.setWidget(row, column+1, rb);
				column = column + 2;
			}

		}

		widget.setSize("100%", "100%");
		widget.setStyleName("perun-tableScrollPanel", true);
		UiElements.runResizeCommands(true);

	}

	/**
	 * Return list of all objects in widget
	 *
	 * @return list of items
	 */
	public ArrayList<T> getList() {
		return this.list;
	}

	public interface HandleItemsAction<T> {

		/**
		 * Action triggered on adding item
		 * @param object
		 */
		public void onAdd(T object);

		/**
		 * Action triggered on removing item
		 * @param object
		 */
		public void onRemove(T object);

	}

}
