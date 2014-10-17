package cz.metacentrum.perun.webgui.widgets.cells;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import cz.metacentrum.perun.webgui.model.AttributeDefinition;

/**
 * Custom cell for Perun attributes
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PerunAttributeFriendlyNameCell extends AbstractSafeHtmlCell<AttributeDefinition> {

	private static String style = "customClickableTextCell";

	/**
	 * Creates a new PerunAttributeValueCell with default renderer
	 */
	public PerunAttributeFriendlyNameCell() {
		this("");
	}

	/**
	 * Creates a new PerunAttributeValueCell with default renderer
	 */
	public PerunAttributeFriendlyNameCell(String style) {

		// custom renderer, creates a link from the object
		this(new SafeHtmlRenderer<AttributeDefinition>() {

			public SafeHtml render(AttributeDefinition object) {
				if (object != null) {
					SafeHtmlBuilder sb = new SafeHtmlBuilder();
					render(object, sb);
					return sb.toSafeHtml();
				}

				return SafeHtmlUtils.EMPTY_SAFE_HTML;
			}

			public void render(AttributeDefinition object, SafeHtmlBuilder sb) {
				if (object != null) {
					generateCode(sb, object);
				}
			}
		});

		this.style = style;

	}

	protected static void generateCode(SafeHtmlBuilder sb, AttributeDefinition attr) {
		sb.appendHtmlConstant("<div class=\""+style+"\" title=\""+attr.getName()+"\">");
		sb.appendHtmlConstant(attr.getFriendlyName());
		sb.appendHtmlConstant("</div>");
	}

	/**
	 * Construct a new HyperlinkCell that will use a given
	 * {@link com.google.gwt.text.shared.SafeHtmlRenderer}.
	 *
	 * @param renderer a {@link com.google.gwt.text.shared.SafeHtmlRenderer SafeHtmlRenderer<String>} instance
	 */
	public PerunAttributeFriendlyNameCell(SafeHtmlRenderer<AttributeDefinition> renderer) {
		super(renderer, CLICK, KEYDOWN);
	}

	/**
	 * Renders the object
	 */
	@Override
	protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
		if (value != null) {
			sb.append(value);
		}
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, AttributeDefinition value,
	                           NativeEvent event, ValueUpdater<AttributeDefinition> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		if (CLICK.equals(event.getType())) {
			onEnterKeyDown(context, parent, value, event, valueUpdater);
		}
	}

	@Override
	protected void onEnterKeyDown(Context context, Element parent, AttributeDefinition value,
	                              NativeEvent event, ValueUpdater<AttributeDefinition> valueUpdater) {
		if (valueUpdater != null) {
			valueUpdater.update(value);
		}
	}

}