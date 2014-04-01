package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.ImageResourceRenderer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom cell widget for images.
 * Allows definition of events to handle.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CustomImageResourceCell extends AbstractCell<ImageResource>{

	private static ImageResourceRenderer renderer;
	private Set<String> consumedEvents;

	public CustomImageResourceCell(String... consumedEvents) {
		// set renderer
		if (renderer == null) {
			renderer = new ImageResourceRenderer();
		}
		// set events
		if (consumedEvents != null && consumedEvents.length > 0) {
			Set<String> set = new HashSet<String>();
			for (int i=0; i<consumedEvents.length; i++) {
				set.add(consumedEvents[i]);
			}
			this.consumedEvents = Collections.unmodifiableSet(set);
		}
	}

	@Override
	public void render(Context context, ImageResource value, SafeHtmlBuilder sb) {
		if (value != null) {
			sb.append(renderer.render(value));
		}
	}

	public Set<String> getConsumedEvents() {
		return consumedEvents;
	}

	@Override
	public boolean isEditing(com.google.gwt.cell.client.Cell.Context context, Element parent, ImageResource value) {
		return false;
	}

	@Override
	public boolean handlesSelection() {
		return false;
	}

	@Override
	public boolean dependsOnSelection() {
		return false;
	}

}
