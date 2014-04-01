package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import cz.metacentrum.perun.webgui.model.Group;

/**
 * Custom GWT cell, which displays the group name with the correct indent.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class PerunGroupCell extends AbstractCell<Group> {


	/**
	 * Creates a new Group cell
	 */
	public PerunGroupCell() {
		// consumes click and keydown
		super("click", "keydown");
	}

	/**
	 * Renders the widget.
	 */
	@Override
	public void render(Context context, Group group, SafeHtmlBuilder sb) {

		// builds the indent
		String indent = "";
		for (int i = 2; i < group.getIndent(); i++) {
			indent += "&nbsp;&nbsp;&nbsp;&nbsp;";
		}
		if (group.getIndent() > 2) {
			indent += "|__&nbsp;";
		}
		// styles
		String style = "customClickableTextCell";

		// core group
		if(group.isCoreGroup()){
			style += " bold";
		}

		// builds the widget text
		String value = indent + group.getName();

		// adds the group element
		sb.appendHtmlConstant("<div class=\"" + style + "\">");
		sb.appendHtmlConstant(value);
		sb.appendHtmlConstant("</div>");
	}


	/**
	 * When user clicks on the cell, fire the enterKeyDownEvent
	 */
	@Override
	public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context,
			Element parent, Group value, NativeEvent event,
			ValueUpdater<Group> valueUpdater) {
		if ("click".equals(event.getType())) {
			onEnterKeyDown(context, parent, value, event, valueUpdater);

		}
		super.onBrowserEvent(context, parent, value, event, valueUpdater);

	}

	/**
	 * When user clicks on the row, fire the value updater
	 */
	@Override
	protected void onEnterKeyDown(Context context, Element parent, Group value,
			NativeEvent event, ValueUpdater<Group> valueUpdater) {
		if (valueUpdater != null) {
			valueUpdater.update(value);
		}
	}
};
