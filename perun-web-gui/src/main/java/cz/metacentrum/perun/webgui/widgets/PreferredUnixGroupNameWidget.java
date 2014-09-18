package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeValueCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Widget for setting preferred Unix group names for users
 *
 * @author Pavel Zlamal <256627@email.muni.cz>
 */
public class PreferredUnixGroupNameWidget extends Composite {

	private SimplePanel widget = new SimplePanel();
	private FlexTable layout = new FlexTable();

	private HashMap<Attribute, PerunAttributeValueCell> cells = new HashMap<Attribute, PerunAttributeValueCell>();

	public PreferredUnixGroupNameWidget() {
		initWidget(widget);
	}

	public PreferredUnixGroupNameWidget(ArrayList<Attribute> list) {
		this();
		if (list != null && !list.isEmpty()) {
			for (Attribute a : list) cells.put((Attribute)JsonUtils.clone(a), new PerunAttributeValueCell());
			buildWidget();
		}
	}

	public void setAttributes(ArrayList<Attribute> list) {
		if (list != null && !list.isEmpty()) {
			for (Attribute a : list) cells.put((Attribute)JsonUtils.clone(a), new PerunAttributeValueCell());
			buildWidget();
		}
	}

	public void setAttribute(Attribute a) {
		if (a != null) {
			cells.put((Attribute)JsonUtils.clone(a), new PerunAttributeValueCell());
			buildWidget();
		}
	}

	public Attribute getAttribute(String urn) {

		// for each find
		for(Map.Entry<Attribute, PerunAttributeValueCell> entry : cells.entrySet()) {
			Attribute attrOld = entry.getKey();
			if (attrOld.getName().equals(urn)) {
				PerunAttributeValueCell valueCell = entry.getValue();
				// save the value
				Attribute attr = valueCell.getValue(attrOld);
				return attr;
			}
		}

		return null;

	}

	public void clear() {
		cells.clear();
	}

	/**
	 * Rebuild widget based on current attribute value
	 */
	public void buildWidget() {

		layout = new FlexTable();
		widget.setWidget(layout);

		int line = 0;
		for (Attribute a : cells.keySet()) {

			SafeHtml valueCellHtml = cells.get(a).getRenderer().render(a);
			layout.setHTML(line, 0, "<strong>Group names in namespaces '"+a.getFriendlyNameParameter()+"':</strong>");
			layout.setHTML(line+1, 0, valueCellHtml);

			line = line+2;

		}

		if (cells.keySet().isEmpty()) {

			layout.setHTML(0, 0, "No namespace is supported.");
			layout.getFlexCellFormatter().setStyleName(0, 0, "inputFormInlineComment");

		}

	}

}
