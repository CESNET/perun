package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.model.RichGroup;
import cz.metacentrum.perun.webgui.widgets.ImageResourceAltRenderer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom cell widget for images.
 * Allows definition of events to handle.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CustomClickableInfoCellWithImageResource extends AbstractCell<RichGroup>{


	private static ImageResourceAltRenderer renderer;
	private Set<String> consumedEvents;

	public CustomClickableInfoCellWithImageResource(String... consumedEvents) {
		// set renderer
		if (renderer == null) {
			renderer = new ImageResourceAltRenderer();
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
	public void render(Context context, RichGroup value, SafeHtmlBuilder sb) {
		if (value != null) {
			ImageResource resource = SmallIcons.INSTANCE.bulletWhiteIcon();
			if (value.isSyncEnabled()) {
				// FIXME - remove old way of determining sync state once values for new attribute are generated.
				if ((value.getLastSynchronizationState() != null && value.getLastSynchronizationState().equals("OK")) ||
						(value.getLastSynchronizationState() == null && value.getLastSuccessSynchronizationTimestamp() != null)) {
					resource = SmallIcons.INSTANCE.bulletGreenIcon();
					if (value.getAuthoritativeGroup() != null && value.getAuthoritativeGroup().equals("1")) {
						sb.append(renderer.render(resource, "Synchronized - OK / Authoritative sync"));
						sb.append(renderer.render(SmallIcons.INSTANCE.bulletStarIcon(), "Synchronized - OK / Authoritative sync"));
					} else {
						sb.append(renderer.render(resource, "Synchronized - OK"));
					}
				} else {
					resource = SmallIcons.INSTANCE.bulletRedIcon();
					if (value.getAuthoritativeGroup() != null && value.getAuthoritativeGroup().equals("1")) {
						sb.append(renderer.render(resource, "Synchronized - Error / Authoritative sync"));
						sb.append(renderer.render(SmallIcons.INSTANCE.bulletStarIcon(), "Synchronized - Error / Authoritative sync"));
					} else {
						sb.append(renderer.render(resource, "Synchronized - Error"));
					}
				}
			} else {
				if (value.getAuthoritativeGroup() != null && value.getAuthoritativeGroup().equals("1")) {
					sb.append(renderer.render(resource, "Not synchronized / Authoritative sync"));
				} else {
					sb.append(renderer.render(resource, "Not synchronized"));
				}
			}
		}
	}

	public Set<String> getConsumedEvents() {
		return consumedEvents;
	}

	@Override
	public boolean isEditing(com.google.gwt.cell.client.Cell.Context context, Element parent, RichGroup value) {
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

	@Override
	public void onBrowserEvent(Context context, Element parent, RichGroup value,
	                           NativeEvent event, ValueUpdater<RichGroup> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);

		if (event.getType().equals("click")) {
			valueUpdater.update(value);
		}
	}

}