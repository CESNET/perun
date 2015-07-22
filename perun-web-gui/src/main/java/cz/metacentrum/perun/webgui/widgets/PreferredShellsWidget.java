package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Widget for setting preferred shell for users
 *
 * @author Pavel Zlamal <256627@email.muni.cz>
 */
public class PreferredShellsWidget extends Composite {

	private Attribute a;
	private SimplePanel widget = new SimplePanel();
	private FlexTable layout = new FlexTable();
	private ArrayList<String> possibleShells = new ArrayList<String>(Arrays.asList("/bin/bash", "/bin/csh", "/bin/ksh", "/bin/sh", "/bin/zsh"));

	private ArrayList<ListBox> shellList = new ArrayList<ListBox>();
	private Map<ListBox, ExtendedTextBox> customMap = new HashMap<ListBox, ExtendedTextBox>();

	private final String CUSTOM_TEXT = "-- custom value --";

	public PreferredShellsWidget() {
		initWidget(widget);
	}

	public PreferredShellsWidget(Attribute a) {
		initWidget(widget);
		if (a != null) {
			this.a = JsonUtils.clone(a).cast();
			buildWidget();
		}
	}

	public void setAttribute(Attribute a) {
		if (a != null) {
			this.a = JsonUtils.clone(a).cast();
		}
		buildWidget();
	}

	public Attribute getAttribute() {
		return a;
	}

	/**
	 * Rebuild widget based on current attribute value
	 */
	public void buildWidget() {

		shellList.clear();
		customMap.clear();
		layout = new FlexTable();
		setStyleName("preferredShellWidget");

		if (!a.getValue().equalsIgnoreCase("null"))  {

			for (int i=0; i<a.getValueAsJsArray().length(); i++) {

				final ListBox list = new ListBox();
				final ExtendedTextBox customValue = new ExtendedTextBox();
				list.setEnabled(a.isWritable());
				customValue.getTextBox().setEnabled((a.isWritable()));
				customValue.setValidator(new ExtendedTextBox.TextBoxValidator() {
					@Override
					public boolean validateTextBox() {
						// on change calculate value
						calculateAttrValue();
						return true;
					}
				});

				addRow(list, customValue, a.getValueAsJsArray().get(i));

				CustomButton removeButton = new CustomButton("", SmallIcons.INSTANCE.deleteIcon(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						shellList.remove(list);
						customMap.remove(list);
						calculateAttrValue();
						buildWidget();
					}
				});
				removeButton.setEnabled(a.isWritable());

				// fill table
				int rowId = layout.getRowCount();

				layout.setWidget(rowId, 0, list);
				layout.setWidget(rowId, 1, customValue);
				layout.setWidget(rowId, 2, removeButton);

			}

		}

		// button
		final int rowId = layout.getRowCount();
		final CustomButton addButton = new CustomButton("", SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ListBox list = new ListBox();
				final ExtendedTextBox customValue = new ExtendedTextBox();
				customValue.setValidator(new ExtendedTextBox.TextBoxValidator() {
					@Override
					public boolean validateTextBox() {
						// on change calculate value
						calculateAttrValue();
						return true;
					}
				});
				// add to values map
				addRow(list, customValue, null);
				// add to persistent storage
				calculateAttrValue();
				// rebuild widget
				buildWidget();
			}
		});
		addButton.setEnabled(a.isWritable());
		layout.setWidget(rowId, 0, addButton);

		widget.setWidget(layout);

	}

	/**
	 * Add input widgets for one entry
	 *
	 * @param list
	 * @param customValue
	 * @param value
	 */
	private void addRow(final ListBox list, final ExtendedTextBox customValue, final String value) {

		customValue.setVisible(false);
		shellList.add(list);
		customMap.put(list, customValue);

		// fill value
		for (int i=0; i<possibleShells.size(); i++) {
			list.addItem(possibleShells.get(i));
			if (possibleShells.get(i).equals(value)) {
				list.setSelectedIndex(i);
			}
		}
		list.addItem(CUSTOM_TEXT);

		if (!possibleShells.contains(value) && value != null) {
			// select custom value
			list.setSelectedIndex(list.getItemCount()-1);
			customValue.getTextBox().setText(value);
			customValue.setVisible(true);
		}

		list.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				customValue.setVisible(list.getValue(list.getSelectedIndex()).equals(CUSTOM_TEXT));
				calculateAttrValue();
			}
		});

	}

	/**
	 * Takes values from input boxes and
	 * stores them to attribute
	 */
	private void calculateAttrValue() {

		if (shellList.isEmpty()) {
			// clear value if empty
			a.setValueAsJso(null);
		} else {
			// fill value
			JsArrayString array = JavaScriptObject.createArray().cast();

			for (ListBox box : shellList) {
				String newValue = box.getValue(box.getSelectedIndex());
				if (newValue.equals(CUSTOM_TEXT)) {
					// process textbox
					if (!customMap.get(box).getTextBox().getText().trim().isEmpty()) {
						// store only non-empty values
						array.push(customMap.get(box).getTextBox().getText().trim());
					}
				} else {
					array.push(newValue);
				}
			}

			a.setValueAsJsArray(array);

		}

	}

}
