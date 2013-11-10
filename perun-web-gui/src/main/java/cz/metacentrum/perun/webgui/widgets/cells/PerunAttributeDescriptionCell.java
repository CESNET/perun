package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import cz.metacentrum.perun.webgui.model.Attribute;


/**
 * Custom cell for Perun attributes
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class PerunAttributeDescriptionCell extends AbstractSafeHtmlCell<cz.metacentrum.perun.webgui.model.Attribute>
{
	
	/**
	 * Creates a new PerunAttributeValueCell with default renderer
	 */
	public PerunAttributeDescriptionCell()
	{
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
					generateCode(sb, object);
				}
			}
		});
	}
	
	

	protected static void generateCode(SafeHtmlBuilder sb, Attribute attr) {
		sb.appendHtmlConstant(attr.getDescription());
	}
		
	/**
	 * Construct a new HyperlinkCell that will use a given
	 * {@link SafeHtmlRenderer}.
	 * 
	 * @param renderer a {@link SafeHtmlRenderer SafeHtmlRenderer<String>} instance
	 */
	public PerunAttributeDescriptionCell(SafeHtmlRenderer<Attribute> renderer) {
		super(renderer);
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