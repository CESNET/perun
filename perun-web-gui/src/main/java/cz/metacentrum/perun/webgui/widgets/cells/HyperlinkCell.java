package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoDetailTabItem;

/**
 * Custom GWT cell, which is clickable and looks like an anchor.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class HyperlinkCell<T extends JavaScriptObject> extends AbstractSafeHtmlCell<T>
{
	/**
	 * Perun Web Session
	 */
	private static PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Creates a new HyperlinkCell with default renderer
	 */
	public HyperlinkCell(final String attrName)
	{
		// custom renderer, creates a link from the object
		this(new SafeHtmlRenderer<T>() {

			public SafeHtml render(T object) {
				if (object != null) {

					GeneralObject go = object.cast();
					String url = getUrl(go);
					// TODO - better usage of methods in general object
					SafeHtmlBuilder sb = new SafeHtmlBuilder();
					generateCode(sb, url, go.getAttribute(attrName));
					return sb.toSafeHtml();
				}

				return SafeHtmlUtils.EMPTY_SAFE_HTML;
			}

			public void render(T object, SafeHtmlBuilder sb) {
				if (object != null) {
					GeneralObject go = object.cast();
					String url = getUrl(go);
					generateCode(sb, url, go.getName());
				}
			}
		});

	}


	/**
	 * Generates the code to be included in the cell
	 * @param sb
	 * @param url
	 * @param title
	 */
	protected static void generateCode(SafeHtmlBuilder sb, String url, String title){
		if(title == null || title.equals("null"))
		{
			return;
		}

		if(url.equals("")){
			sb.appendHtmlConstant("<div class=\"customClickableTextCell\">");
			sb.appendHtmlConstant(title);
			sb.appendHtmlConstant("</div>");
		}else{
			sb.appendHtmlConstant("<div class=\"customClickableTextCell\">");
			sb.appendHtmlConstant("<a href=\"#" + url + "\">" + title  + "</a>");
			sb.appendHtmlConstant("</div>");
		}
	}

	/**
	 * Construct a new HyperlinkCell that will use a given
	 * {@link SafeHtmlRenderer}.
	 *
	 * @param renderer a {@link SafeHtmlRenderer SafeHtmlRenderer<String>} instance
	 */
	public HyperlinkCell(SafeHtmlRenderer<T> renderer) {
		super(renderer, "click", "keydown");
	}

	/**
	 * Called when a browser event occurs
	 */
	@Override
	public void onBrowserEvent(Context context, Element parent, T value,
			NativeEvent event, ValueUpdater<T> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		if ("click".equals(event.getType())) {
			onEnterKeyDown(context, parent, value, event, valueUpdater);
		}
	}

	/**
	 * Called a browser event occurs
	 */
	@Override
	protected void onEnterKeyDown(Context context, Element parent,
			T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
		if (valueUpdater != null) {
			valueUpdater.update(value);
		}
	}


	/**
	 * Creates a URL from the object
	 * @param go GeneralObject
	 * @return URL to be used in link
	 */
	private static String getUrl(GeneralObject go){

		String url = "";

		if(go.getObjectType() == null){
			return url;
		}
		if(go.getObjectType().equals("VirtualOrganization")){
			url = session.getTabManager().getLinkForTab(new VoDetailTabItem(go.getId()));
		}else if(go.getObjectType().equals("Group")){
			url = session.getTabManager().getLinkForTab(new GroupDetailTabItem(go.getId()));
		}else if(go.getObjectType().equals("Facility")){
			//url = session.getTabManager().getLinkForTab(new FacilityDetailTabItem(session, go.getId()));
		}


		return url;
	}

	/**
	 * Renders the object
	 */
	@Override
	protected void render(com.google.gwt.cell.client.Cell.Context context,
			SafeHtml value, SafeHtmlBuilder sb) {
		if (value != null) {
			sb.append(value);
		}
	}
}
