package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import cz.metacentrum.perun.webgui.model.*;

/**
 * Custom GWT cell, which displays checkbox when Group not core Group
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PerunCheckboxCell<T extends JavaScriptObject> extends AbstractEditableCell<T, Boolean> {

	/**
	 * Whether to show the checkbox, if core group
	 */
	private boolean editable = false;

	/**
	 * An html string representation of a checked input box.
	 */
	private static final SafeHtml INPUT_CHECKED = SafeHtmlUtils
		.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked/>");

	/**
	 * An html string representation of an unchecked input box.
	 */
	private static final SafeHtml INPUT_UNCHECKED = SafeHtmlUtils
		.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\"/>");

	/**
	 * An html string representation of a disabled input box.
	 */
	private static final SafeHtml INPUT_DISABLED = SafeHtmlUtils
		.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled/>");


	private final boolean dependsOnSelection;
	private final boolean handlesSelection;

	/**
	 * Construct a new {@link CheckboxCell}.
	 */
	public PerunCheckboxCell() {
		this(false);
	}

	/**
	 * Construct a new {@link CheckboxCell} that optionally controls selection.
	 *
	 * @param isSelectBox
	 *            true if the cell controls the selection state
	 * @deprecated use {@link #CheckboxCell(boolean, boolean)} instead
	 */
	@Deprecated
	public PerunCheckboxCell(boolean isSelectBox) {
		this(isSelectBox, isSelectBox, false);
	}

	/**
	 * Construct a new {@link CheckboxCell} that optionally controls selection.
	 *
	 * @param dependsOnSelection
	 *            true if the cell depends on the selection state
	 * @param handlesSelection
	 *            true if the cell modifies the selection state
	 * @param editable
	 * 				true if show enabled checkboxes when core group
	 */
	public PerunCheckboxCell(boolean dependsOnSelection, boolean handlesSelection, boolean editable) {
		super("change", "keydown");
		this.dependsOnSelection = dependsOnSelection;
		this.handlesSelection = handlesSelection;
		this.editable = editable;
	}

	@Override
	public boolean dependsOnSelection() {
		return dependsOnSelection;
	}

	@Override
	public boolean handlesSelection() {
		return handlesSelection;
	}

	@Override
	public boolean isEditing(Context context, Element parent, JavaScriptObject value) {
		// A checkbox is never in "edit mode". There is no intermediate state
		// between checked and unchecked.
		return false;
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, T value,
			NativeEvent event, ValueUpdater<T> valueUpdater) {
		String type = event.getType();

		boolean enterPressed = "keydown".equals(type)
			&& event.getKeyCode() == KeyCodes.KEY_ENTER;
		if ("change".equals(type) || enterPressed) {
			InputElement input = parent.getFirstChild().cast();
			Boolean isChecked = input.isChecked();

			/*
			 * Toggle the value if the enter key was pressed and the cell
			 * handles selection or doesn't depend on selection. If the cell
			 * depends on selection but doesn't handle selection, then ignore
			 * the enter key and let the SelectionEventManager determine which
			 * keys will trigger a change.
			 */
			if (enterPressed && (handlesSelection() || !dependsOnSelection())) {
				isChecked = !isChecked;
				input.setChecked(isChecked);
			}

			/*
			 * Save the new value. However, if the cell depends on the
			 * selection, then do not save the value because we can get into an
			 * inconsistent state.
			 */
			if (((GeneralObject)value).isChecked() != isChecked && !dependsOnSelection()) {
				setViewData(context.getKey(), isChecked);
			} else {
				clearViewData(context.getKey());
			}

			if (valueUpdater != null) {
				((GeneralObject)value).setChecked(isChecked);
				valueUpdater.update(value);
			}
		}
	}

	@Override
	public void render(Context context, T value, SafeHtmlBuilder sb) {

		// Render disabled for different kinds of Types from Perun

		// is core group
		if (((GeneralObject)value).getObjectType().equalsIgnoreCase("Group") || ((GeneralObject)value).getObjectType().equalsIgnoreCase("RichGroup")) {
			if(((Group)value).isCoreGroup() && !editable){
				sb.append(INPUT_DISABLED);
				return;
			}
		}

		// is service disabled on facility
		if (((GeneralObject)value).getObjectType().equalsIgnoreCase("ServiceForGUI")) {
			if(((RichService)value).getAllowedOnFacility().equalsIgnoreCase("Denied") && !editable){
				sb.append(INPUT_DISABLED);
				return;
			}
		}

		// is user ext source persistent
		if (((GeneralObject)value).getObjectType().equalsIgnoreCase("UserExtSource")) {
			if(((UserExtSource)value).isPersistent() && !editable){
				sb.append(INPUT_DISABLED);
				return;
			}
		}

		// is service disabled on facility
		if (((GeneralObject)value).getObjectType().equalsIgnoreCase("RichMember")) {
			if(((RichMember)value).getMembershipType().equalsIgnoreCase("INDIRECT") && !editable){
				sb.append(INPUT_DISABLED);
				return;
			}
		}

		// attribute is read only (better don't allow selection)
		if (((GeneralObject)value).getObjectType().equalsIgnoreCase("Attribute")) {
			if(((Attribute)value).isWritable() == false){
				sb.append(INPUT_DISABLED);
				return;
			}
		}

		// Get the view data.
		Object key = context.getKey();
		Boolean viewData = getViewData(key);
		if (viewData != null && viewData.equals(((GeneralObject)value).isChecked())) {
			clearViewData(key);
			viewData = null;
		}

		if (value != null && ((viewData != null) ? viewData : ((GeneralObject)value).isChecked())) {
			sb.append(INPUT_CHECKED);
		} else {
			sb.append(INPUT_UNCHECKED);
		}
	}

}
