package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Same widget as TextInputCell, only with tab index
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class TextInputCellWithTabIndex extends
AbstractInputCell<String, TextInputCellWithTabIndex.ViewData> {

	/**
	 * Count of current inputcells
	 */
	static private int count = 10;

	/**
	 * The {@code ViewData} for this cell.
	 */
	public static class ViewData {
		/**
		 * The last value that was updated.
		 */
		private String lastValue;

		/**
		 * The current value.
		 */
		private String curValue;

		/**
		 * Construct a ViewData instance containing a given value.
		 *
		 * @param value
		 *            a String value
		 */
		public ViewData(String value) {
			this.lastValue = value;
			this.curValue = value;
		}

		/**
		 * Return true if the last and current values of this ViewData object
		 * are equal to those of the other object.
		 */
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ViewData)) {
				return false;
			}
			ViewData vd = (ViewData) other;
			return equalsOrNull(lastValue, vd.lastValue)
				&& equalsOrNull(curValue, vd.curValue);
		}

		/**
		 * Return the current value of the input element.
		 *
		 * @return the current value String
		 * @see #setCurrentValue(String)
		 */
		public String getCurrentValue() {
			return curValue;
		}

		/**
		 * Return the last value sent to the {@link ValueUpdater}.
		 *
		 * @return the last value String
		 * @see #setLastValue(String)
		 */
		public String getLastValue() {
			return lastValue;
		}

		/**
		 * Return a hash code based on the last and current values.
		 */
		@Override
		public int hashCode() {
			return (lastValue + "_*!@HASH_SEPARATOR@!*_" + curValue).hashCode();
		}

		/**
		 * Set the current value.
		 *
		 * @param curValue
		 *            the current value
		 * @see #getCurrentValue()
		 */
		protected void setCurrentValue(String curValue) {
			this.curValue = curValue;
		}

		/**
		 * Set the last value.
		 *
		 * @param lastValue
		 *            the last value
		 * @see #getLastValue()
		 */
		protected void setLastValue(String lastValue) {
			this.lastValue = lastValue;
		}

		private boolean equalsOrNull(Object a, Object b) {
			return (a != null) ? a.equals(b) : ((b == null) ? true : false);
		}
	}

	interface Template extends SafeHtmlTemplates {
		@Template("<input type=\"text\" value=\"{0}\" tabindex=\"{1}\"></input>")
			SafeHtml input(String value, int tabIndex);
	}

	/**
	 * Creates a new TextInputCell with automatical tabindexing
	 */
	public TextInputCellWithTabIndex() {
		super("change", "keyup");
		if (template == null) {
			template = GWT.create(Template.class);
		}

	}


	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		// Get the view data.
		Object key = context.getKey();
		ViewData viewData = getViewData(key);
		if (viewData != null && viewData.getCurrentValue().equals(value)) {
			clearViewData(key);
			viewData = null;
		}

		String s = (viewData != null) ? viewData.getCurrentValue() : value;
		if (s != null) {
			count++;
			sb.append(template.input(s, count));
		} else {
			sb.appendHtmlConstant("<input type=\"text\" tabindex=\"-1\"></input>");
		}
	}

	private static Template template;



	@Override
	public void onBrowserEvent(Context context, Element parent, String value,
			NativeEvent event, ValueUpdater<String> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);

		// Ignore events that don't target the input.
		InputElement input = getInputElement(parent);
		Element target = event.getEventTarget().cast();
		if (!input.isOrHasChild(target)) {
			return;
		}

		String eventType = event.getType();
		Object key = context.getKey();
		if ("change".equals(eventType)) {
			finishEditing(parent, value, key, valueUpdater);
		} else if ("keyup".equals(eventType)) {
			// Record keys as they are typed.
			ViewData vd = getViewData(key);
			if (vd == null) {
				vd = new ViewData(value);
				setViewData(key, vd);
			}
			vd.setCurrentValue(input.getValue());
		}
	}

	@Override
	protected void finishEditing(Element parent, String value, Object key,
			ValueUpdater<String> valueUpdater) {
		String newValue = getInputElement(parent).getValue();

		// Get the view data.
		ViewData vd = getViewData(key);
		if (vd == null) {
			vd = new ViewData(value);
			setViewData(key, vd);
		}
		vd.setCurrentValue(newValue);

		// Fire the value updater if the value has changed.
		if (valueUpdater != null
				&& !vd.getCurrentValue().equals(vd.getLastValue())) {
			vd.setLastValue(newValue);
			valueUpdater.update(newValue);
				}

		// Blur the element.
		super.finishEditing(parent, newValue, key, valueUpdater);
	}

	@Override
	protected InputElement getInputElement(Element parent) {
		return super.getInputElement(parent).<InputElement> cast();
	}
}
