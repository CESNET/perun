package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.client.ui.Image;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Attribute;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Custom cell for Perun attributes. Draws input fields based on attribute value type.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PerunAttributeValueCell extends AbstractSafeHtmlCell<Attribute> {

	static private int cellsCount = 0;

	static public final String ADD_ICON = new Image(SmallIcons.INSTANCE.addIcon()).getElement().getString();
	static public final String REMOVE_ICON = new Image(SmallIcons.INSTANCE.deleteIcon()).getElement().getString();

	/*

	Note for GWT 2.5.1 -> 2.6.0

	Since GWT 2.6.0 impl requires event.currentTarget to be filled (which is not by jQuery) we can't use
	simple call "parent.change()". New way is:

	var event = jQuery.Event('change'); event.currentTarget = event.target = parent[0]; parent.trigger(event);

	*/

	static public final String LIST_ITEM_TABLE_ROW = "<tr %s>"
			+ "<td><input onkeydown=\"var event2 = jQuery.Event('keydown'); event2.keyCode = event.keyCode; jQuery(this).parents('.perunAttributeCell').keydown(); jQuery(this).parents('.perunAttributeCell').trigger(event2);\" onchange=\"$(this).parents('.perunAttributeCell').change();\" onclick=\"$(this).parents('.perunAttributeCell').click();\" onblur=\"$(this).parents('.perunAttributeCell').blur();\" onfocus=\"document.getElementById('.perunAttributeCell').focus();\" style=\"width:250px\" type=\"text\" class=\"list-item-value gwt-TextBox\" value=\"%s\" /></td>"
			+ "<td class=\"PerunAttributeRemoveButton\"><button title=\"" + WidgetTranslation.INSTANCE.removeValue() + "\" class=\"gwt-Button PerunAttributeControllButton\" onclick=\"var parent = $(this).parents('.perunAttributeCell'); $(this).parent().parent().remove(); var event = jQuery.Event('change'); event.currentTarget = event.target = parent[0]; parent.trigger(event); \">" + REMOVE_ICON + "</button></td>"
			+ "</tr>";

	static public final String MAP_ITEM_TABLE_ROW = "<tr %s>" + "<td><input style=\"width:200px\" type=\"text\" class=\"map-entry-key gwt-TextBox\" value=\"%s\" onkeydown=\"var event2 = jQuery.Event('keydown'); event2.keyCode = event.keyCode; jQuery(this).parents('.perunAttributeCell').keydown(); jQuery(this).parents('.perunAttributeCell').trigger(event2); \" onchange=\"$(this).parents('.perunAttributeCell').change();\" onclick=\"$(this).parents('.perunAttributeCell').click();\" onblur=\"$(this).parents('.perunAttributeCell').blur();\" onfocus=\"document.getElementById('.perunAttributeCell').focus();\" /></td>" +
			"<td>=</td>" + "<td><input style=\"width:200px\" type=\"text\" class=\"map-entry-value gwt-TextBox\" value=\"%s\" onkeydown=\"var event2 = jQuery.Event('keydown'); event2.keyCode = event.keyCode; jQuery(this).parents('.perunAttributeCell').keydown(); jQuery(this).parents('.perunAttributeCell').trigger(event2);\" onchange=\"$(this).parents('.perunAttributeCell').change();\" onclick=\"$(this).parents('.perunAttributeCell').click();\" onblur=\"$(this).parents('.perunAttributeCell').blur();\" onfocus=\"document.getElementById('.perunAttributeCell').focus();\" /></td>" +
			"<td class=\"PerunAttributeRemoveButton\"><button title=\"" + WidgetTranslation.INSTANCE.removeValue() + "\" class=\"gwt-Button PerunAttributeControllButton\" onclick=\"var parent = $(this).parents('.perunAttributeCell'); $(this).parent().parent().remove(); var event = jQuery.Event('change'); event.currentTarget = event.target = parent[0]; parent.trigger(event); \">" + REMOVE_ICON + "</button></td>" + "</tr>";

	static public final String LIST_ITEM_TABLE_ROW_READONLY = "<tr %s>"
			+ "<td><input title=\"" + WidgetTranslation.INSTANCE.notWritable() + "\" readonly onkeydown=\"var event2 = jQuery.Event('keydown'); event2.keyCode = event.keyCode; jQuery(this).parents('.perunAttributeCell').keydown(); jQuery(this).parents('.perunAttributeCell').trigger(event2);\" onchange=\"$(this).parents('.perunAttributeCell').change();\" onclick=\"$(this).parents('.perunAttributeCell').click();\" onblur=\"$(this).parents('.perunAttributeCell').blur();\" onfocus=\"document.getElementById('.perunAttributeCell').focus();\" style=\"width:250px\" type=\"text\" class=\"list-item-value gwt-TextBox gwt-TextBox-readonly\" value=\"%s\" /></td>"
			+ "<td class=\"PerunAttributeRemoveButton\"><button title=\"" + WidgetTranslation.INSTANCE.notWritable() + "\" class=\"gwt-Button PerunAttributeControllButton customButtonImageDisabled\">" + REMOVE_ICON + "</button></td>"
			+ "</tr>";

	static public final String MAP_ITEM_TABLE_ROW_READONLY = "<tr %s>" + "<td><input style=\"width:200px\" title=\"" + WidgetTranslation.INSTANCE.notWritable() + "\" readonly type=\"text\" class=\"map-entry-key gwt-TextBox gwt-TextBox-readonly\" value=\"%s\" onkeydown=\"var event2 = jQuery.Event('keydown'); event2.keyCode = event.keyCode; jQuery(this).parents('.perunAttributeCell').keydown(); jQuery(this).parents('.perunAttributeCell').trigger(event2); \" onchange=\"$(this).parents('.perunAttributeCell').change();\" onclick=\"$(this).parents('.perunAttributeCell').click();\" onblur=\"$(this).parents('.perunAttributeCell').blur();\" onfocus=\"document.getElementById('.perunAttributeCell').focus();\" /></td>" +
			"<td>=</td>" + "<td><input style=\"width:200px\" title=\"" + WidgetTranslation.INSTANCE.notWritable() + "\" readonly type=\"text\" class=\"map-entry-value gwt-TextBox gwt-TextBox-readonly\" value=\"%s\" onkeydown=\"var event2 = jQuery.Event('keydown'); event2.keyCode = event.keyCode; jQuery(this).parents('.perunAttributeCell').keydown(); jQuery(this).parents('.perunAttributeCell').trigger(event2);\" onchange=\"$(this).parents('.perunAttributeCell').change();\" onclick=\"$(this).parents('.perunAttributeCell').click();\" onblur=\"$(this).parents('.perunAttributeCell').blur();\" onfocus=\"document.getElementById('.perunAttributeCell').focus();\" /></td>" +
			"<td class=\"PerunAttributeRemoveButton\"><button title=\"" + WidgetTranslation.INSTANCE.notWritable() + "\" class=\"gwt-Button PerunAttributeControllButton customButtonImageDisabled\" \">" + REMOVE_ICON + "</button></td>" + "</tr>";

	private boolean editing = false;

	/**
	 * Construct a new PerunAttributeValueCell that will use a given
	 * {@link SafeHtmlRenderer}.
	 *
	 * @param renderer a {@link SafeHtmlRenderer SafeHtmlRenderer<String>} instance
	 */
	public PerunAttributeValueCell(SafeHtmlRenderer<Attribute> renderer) {
		super(renderer, "change", "click", "keydown", "blur", "focus");
	}

	/**
	 * Creates a new PerunAttributeValueCell with default renderer
	 */
	public PerunAttributeValueCell() {

		// custom renderer, creates a link from the object
		this(new SafeHtmlRenderer<Attribute>() {

			public SafeHtml render(Attribute object) {
				if (object != null) {
					SafeHtmlBuilder sb = new SafeHtmlBuilder();
					render(object, sb);
					return sb.toSafeHtml();
				}

				return SafeHtmlUtils.EMPTY_SAFE_HTML;
			}

			public void render(Attribute object, SafeHtmlBuilder sb) {
				if (object != null) {
					String w = getWidget(object);
					generateCode(sb, w, getUniqueCellId(object));
				}
			}
		});
	}

	/**
	 * Renders the object
	 */
	@Override
	protected void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml value, SafeHtmlBuilder sb) {
		if (value != null) {
			sb.append(value);
		}
	}

	/**
	 * Allow editing of content
	 */
	@Override
	public boolean isEditing(com.google.gwt.cell.client.Cell.Context context, Element parent, Attribute value) {
		return editing;
	}

	@Override
	public boolean dependsOnSelection() {
		return false;
	}

	@Override
	public boolean handlesSelection() {
		return false;
	}

	/**
	 * Returns unique cell ID based on the attribute
	 *
	 * @param attr
	 * @return
	 */
	static private String getUniqueCellId(Attribute attr) {

		return "perunAttributeValueCell-" + attr.getGuiUniqueId();

	}

	/**
	 * Generates the code to be included in the cell
	 *
	 * @param sb
	 * @param widget
	 * @param cellId unique cell id
	 */
	protected static void generateCode(SafeHtmlBuilder sb, String widget, String cellId) {

		if (widget == null || sb == null) {
			return;
		}

		sb.appendHtmlConstant("<div class=\"perunAttributeCell\" id=\"" + cellId + "\">");
		sb.append(SafeHtmlUtils.fromTrustedString(widget));
		sb.appendHtmlConstant("</div>");

	}

	/**
	 * Called when a browser event occurs
	 */
	@Override
	public void onBrowserEvent(Context context, Element parent, Attribute value, NativeEvent event, ValueUpdater<Attribute> valueUpdater) {

		if ("change".equals(event.getType())) {
			editing = false;
			valueChanged(value, valueUpdater);
		}

		// keydown event must be used for LIST and MAP elements
		if ("keydown".equals(event.getType())) {
			editing = true;
			// prevent special keys to enable editing
			if (KeyCodes.KEY_ENTER == event.getKeyCode()) {
				onEnterKeyDown(context, parent, value, event, valueUpdater);
			} else if (KeyCodes.KEY_ESCAPE == event.getKeyCode()) {
				// GWT 2.4.0 - condition for editing=false must be triggered for arrow keys too
				// GWT > 2.5.0 - working out of the box
				editing = false;
			}
		}

		// blur and focus for STRING and INTEGER elements
		if ("blur".equals(event.getType())) {
			editing = false;
		}
		if ("focus".equals(event.getType())) {
			editing = true;
		}

	}

	/**
	 * Called a browser event occurs, saves the value
	 */
	@Override
	protected void onEnterKeyDown(Context context, Element parent, Attribute value, NativeEvent event, ValueUpdater<Attribute> valueUpdater) {
		editing = false;
		valueChanged(value, valueUpdater);
	}

	/**
	 * Setting the value and calling the updater
	 *
	 * @param value
	 * @param valueUpdater
	 */
	private void valueChanged(Attribute value, ValueUpdater<Attribute> valueUpdater) {

		// if value OK
		if (setNewValue(value)) {
			value.setAttributeValid(true);
		} else {
			value.setAttributeValid(false);
			UiElements.cantSaveAttributeValueDialogBox(value);
			return;
		}
		valueUpdater.update(value); // update value if correct

	}

	/**
	 * Sets the new value from cells
	 *
	 * @param attr
	 * @return True if success
	 */
	private boolean setNewValue(Attribute attr) {

		if (attr.getType().equals("java.util.LinkedHashMap")) {
			return generateValueFromMap(attr, getUniqueCellId(attr));
		}
		if (attr.getType().equals("java.lang.Integer")) {
			return generateValueFromNumber(attr, getUniqueCellId(attr));
		}
		if (attr.getType().equals("java.lang.Boolean")) {
			return generateValueFromBoolean(attr, getUniqueCellId(attr));
		}
		if (attr.getType().equals("java.util.ArrayList")) {
			return generateValueFromList(attr, getUniqueCellId(attr));
		}

		return generateValueFromString(attr, getUniqueCellId(attr));

	}

	/**
	 * Gets the attribute with the new value
	 *
	 * @param attr
	 * @return True if success
	 */
	public Attribute getValue(Attribute attr) {
		boolean ok = setNewValue(attr);
		if (!ok) {
			attr.setValueAsJso(null);
		}
		return attr;
	}

	/**
	 * Gets the value from TextBox and saves it to the object
	 *
	 * @param attr
	 * @param uniqueId
	 * @return true if success
	 */
	private final native boolean generateValueFromString(Attribute attr, String uniqueId) /*-{
		var str = $wnd.jQuery("#" + uniqueId + " .textbox-value").val().trim();
		if (str.length != 0) {
			attr.value = str;
		} else {
			attr.value = null;
		}
		return true;
	}-*/;

	/**
	 * Gets the value from CheckBox and saves it to the object
	 *
	 * @param attr
	 * @param uniqueId
	 * @return true if success
	 */
	private final native boolean generateValueFromBoolean(Attribute attr, String uniqueId) /*-{
		var val = $wnd.jQuery("#" + uniqueId + " .checkbox-value").prop('checked')
		if (val === true) {
			// checked == true
			attr.value = true;
		} else {
			// unchecked == null == false
			attr.value = null;
		}
		return true;
	}-*/;

	/**
	 * Gets the value from Number TextBox and saves it to the object
	 *
	 * @param attr
	 * @param uniqueId
	 * @return true if success
	 */
	private final native boolean generateValueFromNumber(Attribute attr, String uniqueId) /*-{
		// gets the value
		var newValue = $wnd.jQuery("#" + uniqueId + " .numberbox-value").val().trim();
		// true on any number format, false otherwise
		if (!isNaN(parseFloat(newValue)) && isFinite(newValue)) {
			$wnd.jQuery("#" + uniqueId + " .numberbox-value").css("border-color", "");
			attr.value = parseInt(newValue);
			return true;
		}
		// true on empty value
		if (newValue.length == 0) {
			$wnd.jQuery("#" + uniqueId + " .numberbox-value").css("border-color", "");
			attr.value = null;
			return true;
		}
		// wrong
		$wnd.jQuery("#" + uniqueId + " .numberbox-value").css("border-color", "red");
		return false;
	}-*/;

	/**
	 * Gets the value from the list of TextBoxes and saves it to the object
	 *
	 * @param attr
	 * @param uniqueId
	 * @return true if success
	 */
	private final native boolean generateValueFromList(Attribute attr, String uniqueId) /*-{
		attr.value = [];
		var i = 0;
		$wnd.jQuery("#" + uniqueId + " .list-item-value").each(function () {
			var val = $wnd.jQuery(this).val();
			if (val != null && typeof val != undefined && val != "" && val.length != 0) {
				attr.value[i] = val.trim();
				i++;
			}
		});
		// empty value
		if (i == 0) {
			attr.value = null;
		}
		return true;
	}-*/;


	/**
	 * Gets the value from the list of TextBoxes and saves it to the object
	 *
	 * @param attr
	 * @param uniqueId
	 * @return true if success
	 */
	private final native boolean generateValueFromMap(Attribute attr, String uniqueId) /*-{
		attr.value = {};
		var i = 0;
		$wnd.jQuery("#" + uniqueId + " .map-entry").each(function () {
			var key = $wnd.jQuery(this).find(".map-entry-key").val().trim();
			var tempval = $wnd.jQuery(this).find(".map-entry-value").val().trim();
			// necessary for CERTIFICATE VALUES
			var val = tempval.replace(/\\n/g, '\n');
			if (key != "") {
				if (key != "Enter new key first!") {
					attr.value[key] = val;
				}
			}
			i++;
		});
		// if empty
		if (i == 0) {
			attr.value = null;
		}
		return true;
	}-*/;


	/**
	 * Creates a HTML from the object
	 *
	 * @param attr
	 * @return HTML contents
	 */
	private static String getWidget(Attribute attr) {

		if (attr.getType() == null) {
			return "type = null";
		}

		if (attr.getType().equals("java.util.LinkedHashMap")) {
			return generateMap(attr.getValueAsMap(), attr.isWritable());
		} else if (attr.getType().equals("java.lang.Integer")) {
			return generateNumberBox(attr.getValue(), attr.isWritable());
		} else if (attr.getType().equals("java.lang.Boolean")) {
			return generateCheckBox(attr.getValue(), attr.isWritable());
		} else if (attr.getType().equals("java.util.ArrayList")) {
			return generateList(attr.getValueAsJsArray(), attr.isWritable());
		}

		return generateTextBox(attr.getValue(), attr.isWritable());
	}

	/**
	 * TextBox
	 *
	 * @param value
	 * @return
	 */
	private static String generateTextBox(String value, boolean writable) {
		// check for emptiness
		if (value == null || value.equalsIgnoreCase("null")) {
			value = "";
		}

		// make " ' & and etc. symbols printable in text box
		value = SafeHtmlUtils.htmlEscape(value);

		if (writable) {
			return "<input type=\"text\" style=\"width:250px\" class=\"textbox-value gwt-TextBox \" value=\"" + value + "\" />";
		} else {
			return "<input title=\"" + WidgetTranslation.INSTANCE.notWritable() + "\" readonly type=\"text\" style=\"width:250px\" class=\"textbox-value gwt-TextBox gwt-TextBox-readonly\" value=\"" + value + "\" />";
		}

	}

	/**
	 * TextBox for number
	 *
	 * @param value
	 * @return
	 */
	private static String generateNumberBox(String value, boolean writable) {
		// check for emptiness
		if (value == null || value.equalsIgnoreCase("null")) {
			value = "";
		}

		// make " ' & and etc. symbols printable in text box
		value = SafeHtmlUtils.htmlEscape(value);

		if (writable) {
			return "<input type=\"text\" style=\"width:100px\" class=\"numberbox-value gwt-TextBox\" value=\"" + value + "\" />";
		} else {
			return "<input title=\"" + WidgetTranslation.INSTANCE.notWritable() + "\" readonly type=\"text\" style=\"width:100px\" class=\"numberbox-value gwt-TextBox gwt-TextBox-readonly\" value=\"" + value + "\" />";
		}

	}

	/**
	 * CheckBox for number
	 *
	 * @param value
	 * @return
	 */
	private static String generateCheckBox(String value, boolean writable) {
		if (value != null && value.equalsIgnoreCase("true")) {
			// only non-null true is considered checked
			value = "checked=\"checked\"";
		} else {
			// other values are considered false == null
			value = "";
		}

		if (writable) {
			return "<input type=\"checkbox\" class=\"checkbox-value gwt-CheckBox\" " + value + " />";
		} else {
			return "<input title=\"" + WidgetTranslation.INSTANCE.notWritable() + "\" readonly type=\"checkbox\" class=\"checkbox-value gwt-CheckBox gwt-CheckBox-readonly\" " + value + " />";
		}

	}


	/**
	 * List of textboxes
	 *
	 * @param list
	 * @return
	 */
	private static String generateList(JsArrayString list, boolean writable) {

		String output = "<table class=\"PerunAttributeTableBorder\" >";

		if (list != null) { // check for emptiness

			for (int i = 0; i < list.length(); i++) {
				String val = list.get(i);
				if (val == null) {
					val = "";
				}

				// make " ' & and etc. symbols printable in text box
				val = SafeHtmlUtils.htmlEscape(val);

				if (writable) {
					output += JsonUtils.stringFormat(LIST_ITEM_TABLE_ROW, "", val);
				} else {
					output += JsonUtils.stringFormat(LIST_ITEM_TABLE_ROW_READONLY, "", val);
				}


			}

		}

		// source for others
		if (writable) {
			output += JsonUtils.stringFormat(LIST_ITEM_TABLE_ROW, " style=\"display:none;\" class=\"attribute-list-item-source\"", "");
			output += "<tr>";
			output += "<td colspan=\"2\" style=\"text-align:center\"><button title=\"" + WidgetTranslation.INSTANCE.addValue() + "\" class=\"PerunAttributeAddButton gwt-Button PerunAttributeControllButton\" onclick=\"$(this).parent().parent().before('<tr>' + $('.attribute-list-item-source').html() + '</tr>');\">" + ADD_ICON + "</button></td>";

		} else {
			output += "<tr>";
			output += "<td colspan=\"2\" style=\"text-align:center\"><button title=\"" + WidgetTranslation.INSTANCE.notWritable() + "\" class=\"PerunAttributeAddButton gwt-Button PerunAttributeControllButton customButtonImageDisabled\">" + ADD_ICON + "</button></td>";
		}

		output += "</tr>";
		output += "</table>";
		return output;
	}

	/**
	 * List of mapped textboxes
	 *
	 * @param map
	 * @return
	 */
	private static String generateMap(Map<String, JSONValue> map, boolean writable) {

		String output = "<table class=\"PerunAttributeTableBorder\" >";

		// source for others
		if (writable) {
			output += JsonUtils.stringFormat(MAP_ITEM_TABLE_ROW, " style=\"display:none;\" class=\"attribute-map-item-source\"", "Enter new key first!", "Then enter new value.");
		}

		for (String key : map.keySet()) {
			String str = "";
			SafeHtmlBuilder sb = new SafeHtmlBuilder();
			if (map.get(key) != null) {
				// all JSONValues take as strings
				str = ((JSONString) map.get(key)).stringValue();
				// convert new lines
				sb.appendEscapedLines(str);
			}
			// convert them back to actually display them as \n without JSON default toString() malforming
			str = sb.toSafeHtml().asString().replace("<br>", "\\n");

			// add slash to " in key
			key = SafeHtmlUtils.htmlEscape(key);

			if (writable) {
				output += JsonUtils.stringFormat(MAP_ITEM_TABLE_ROW, "class=\"map-entry\"", key, str);
			} else {
				output += JsonUtils.stringFormat(MAP_ITEM_TABLE_ROW_READONLY, "class=\"map-entry\"", key, str);
			}
		}

		output += "<tr>";
		if (writable) {
			// adds default (hidden) definition of new row and adds it map-entry class param.
			output += "<td colspan=\"3\" style=\"text-align:center\"><button title=\"" + WidgetTranslation.INSTANCE.addValue() + "\" class=\"PerunAttributeAddButton gwt-Button PerunAttributeControllButton\" onclick=\"$(this).parent().parent().before('<tr>' + $('.attribute-map-item-source').html() + '</tr>'); $(this).parent().parent().prev().addClass('map-entry');   \">" + ADD_ICON + "</button></td>";
		} else {
			output += "<td colspan=\"3\" style=\"text-align:center\"><button title=\"" + WidgetTranslation.INSTANCE.notWritable() + "\" class=\"PerunAttributeAddButton gwt-Button PerunAttributeControllButton customButtonImageDisabled\">" + ADD_ICON + "</button></td>";
		}

		output += "</tr>";
		output += "</table>";

		return output;

	}

	@Override
	public Set<String> getConsumedEvents() {

		HashSet<String> set = new HashSet<String>();
		set.add("click");
		set.add("change");
		set.add("focus");
		set.add("blur");
		set.add("keydown");
		return set;
	}

}