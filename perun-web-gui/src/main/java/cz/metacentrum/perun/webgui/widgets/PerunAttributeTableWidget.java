package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeDescriptionCell;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeNameCell;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeValueCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom cell for Perun attributes
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PerunAttributeTableWidget extends Composite {

	/**
	 * Save event
	 * @author Vaclav Mach <374430@mail.muni.cz>
	 */
	public interface SaveEvent {
		void save(ArrayList<Attribute> attrs);
	}

	/**
	 * Attrs list
	 */
	private ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	private Map<Integer, Object> originalAttributes = new HashMap<Integer, Object>();

	/**
	 * Main Widget
	 */
	final private FlexTable ft = new FlexTable();

	/**
	 * Save event
	 */
	private SaveEvent saveEvent;

	/**
	 * Whether description shown
	 */
	private boolean descriptionShown = false;

	/**
	 * IDS used for default Save action
	 */
	private Map<String, Integer> ids;

	private boolean dark = false;
	private CustomButton saveButton;
	private boolean displaySaveButton = true;

	/**
	 * Creates a new table
	 */
	public PerunAttributeTableWidget(){
		this.initWidget(ft);
	}

	/**
	 * Creates a new table
	 */
	public PerunAttributeTableWidget(Map<String, Integer> ids){
		this.initWidget(ft);
		this.ids = ids;
	}

	/**
	 * Creates a new tabel with save event
	 * @param saveEvent
	 */
	public PerunAttributeTableWidget(Map<String, Integer> ids, SaveEvent saveEvent){
		this(ids);
		this.saveEvent = saveEvent;
	}

	/**
	 * Creates a new tabel with save event
	 * @param saveEvent
	 * @param descriptionShown
	 * @param attributes
	 */
	public PerunAttributeTableWidget(Map<String, Integer> ids, SaveEvent saveEvent, boolean descriptionShown, ArrayList<Attribute> attributes){
		this(ids, saveEvent);
		this.descriptionShown = descriptionShown;
		this.add(attributes);
	}

	/**
	 * Whether to show description
	 *
	 * @param descriptionShown
	 */
	public void setDescriptionShown(boolean descriptionShown){
		this.descriptionShown = descriptionShown;
	}

	/**
	 * Adds attributes
	 */
	public void add(ArrayList<Attribute> attributes){
		this.attributes.addAll(attributes);
		for (Attribute a : attributes) {
			this.originalAttributes.put(a.getId(), a.getValue());
		}
		build();
	}

	/**
	 * Add single attribute without rebuild
	 */
	public void add(Attribute attribute){
		this.attributes.add(attribute);
		this.originalAttributes.put(attribute.getId(), attribute.getValue());
	}

	/**
	 * Removes all attributes
	 */
	public void clear(){
		this.originalAttributes.clear();
		this.attributes.clear();
		build();
	}

	/**
	 * Removes an attribute
	 */
	public void remove(Attribute attr){
		this.originalAttributes.remove(attr.getId());
		this.attributes.remove(attr);
		build();
	}

	public FlexTable getWidget(){
		return this.ft;
	}

	public boolean isDark() {
		return this.dark;
	}

	public void setDark(boolean dark) {
		this.dark = dark;
	}

	public boolean isDisplaySaveButton() {
		return displaySaveButton;
	}

	public void setDisplaySaveButton(boolean displaySaveButton) {
		this.displaySaveButton = displaySaveButton;
	}

	public CustomButton getSaveButton() {
		return this.saveButton;
	}

	public void setIds(Map<String, Integer> ids) {
		this.ids = ids;
	}

	public void setEvents(SaveEvent saveEvent) {
		this.saveEvent = saveEvent;
	}

	/**
	 * Builds the table
	 */
	public void build() {

		ft.clear(true);
		if (!dark) {
			ft.setStyleName("inputFormFlexTable");
		} else {
			ft.setStyleName("inputFormFlexTableDark");
		}

		int nameCol = 0;
		int valCol = 1;
		int descCol = -1;


		if(descriptionShown) {
			nameCol = 0;
			descCol = 2;
			valCol = 1;
		}

		int row = 0;

		final Map<Attribute, PerunAttributeValueCell> valueCells = new HashMap<Attribute, PerunAttributeValueCell>();

		// save button
		saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes");
		saveButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {

				// saving
				ArrayList<Attribute> newAttributes = new ArrayList<Attribute>();

				// for each find
				for(Map.Entry<Attribute, PerunAttributeValueCell> entry : valueCells.entrySet()) {
					Attribute attrOld = entry.getKey();
					PerunAttributeValueCell valueCell = entry.getValue();

					// save the value
					Attribute attr = valueCell.getValue(attrOld);
					newAttributes.add(attr);
				}

				save(newAttributes);

			}
		});

		if (displaySaveButton) {
			ft.setWidget(row, 0, saveButton);
			row++;
		}

		for (Attribute attr : attributes) {

			PerunAttributeNameCell nameCell = new PerunAttributeNameCell();
			PerunAttributeValueCell valueCell = new PerunAttributeValueCell();

			// name
			SafeHtml nameCellHtml = nameCell.getRenderer().render(attr);
			ft.setHTML(row, nameCol, nameCellHtml.asString()+"<strong>:</strong>");
			ft.getFlexCellFormatter().setStyleName(row, nameCol, "itemName");

			// value
			SafeHtml valueCellHtml = valueCell.getRenderer().render(attr);
			ft.setHTML(row, valCol, valueCellHtml);
			valueCells.put(attr, valueCell);

			// description
			if(descriptionShown){
				PerunAttributeDescriptionCell descCell = new PerunAttributeDescriptionCell();
				SafeHtml descCellHtml = descCell.getRenderer().render(attr);
				ft.setHTML(row, descCol, descCellHtml);
			}

			row++;
		}

	}

	/**
	 * Saves the attributes
	 * If attribute with value null, asks if remove it
	 * Called recursively
	 *
	 * @param attrs
	 */
	private void save(final ArrayList<Attribute> attrs) {

		// call the method
		if(saveEvent == null) {
			// ids must be set
			if (ids == null || ids.isEmpty()) return;

			final ArrayList<Attribute> toSet = new ArrayList<Attribute>();
			final ArrayList<Attribute> toRemove = new ArrayList<Attribute>();

			for (Attribute a : attrs) {
				Object oldValue = originalAttributes.get(a.getId());
				if (a.getValue().equals(oldValue)) {
					// do not save not changed
				} else if (a.getValueAsObject() == null) {
					toRemove.add(a);
				} else {
					toSet.add(a);
				}
			}

			if (!toSet.isEmpty()) {
				SetAttributes request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(saveButton, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						// for all attributes to be saved/removed
						for (Attribute a : toSet) {
							originalAttributes.put(a.getId(), a.getValueAsObject());
						}
					}
				}));
				request.setAttributes(ids, toSet);
			}
			if (!toRemove.isEmpty()) {
				RemoveAttributes request2 = new RemoveAttributes(JsonCallbackEvents.disableButtonEvents(saveButton, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						// for all attributes to be saved/removed
						for (Attribute a : toRemove) {
							originalAttributes.put(a.getId(), a.getValueAsObject());
						}
					}
				}));
				request2.removeAttributes(ids, toRemove);
			}

			if (toSet.isEmpty() && toRemove.isEmpty()) {
				UiElements.generateAlert("No changes", "No changes to save.");
			}

			return;

		}

		saveEvent.save(attrs);

	}

}
