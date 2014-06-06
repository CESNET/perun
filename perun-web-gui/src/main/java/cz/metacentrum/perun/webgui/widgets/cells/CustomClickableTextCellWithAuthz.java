package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import cz.metacentrum.perun.webgui.json.columnProviders.IsClickableCell;
import cz.metacentrum.perun.webgui.model.*;

/**
 * Custom cell with value optionally displayed as clickable
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CustomClickableTextCellWithAuthz<T extends JavaScriptObject> extends AbstractSafeHtmlCell<T> {

	private static IsClickableCell<GeneralObject> authz;
	private static String style;

	/**
	 * Creates a new CustomClickableTextCellWithAuthz with default renderer
	 */
	public CustomClickableTextCellWithAuthz(final IsClickableCell authz, final String attrName) {

		// custom renderer, creates a link from the object
		this(new SafeHtmlRenderer<T>() {

			public SafeHtml render(T object) {
				if (object != null) {
					GeneralObject go = object.cast();
					SafeHtmlBuilder sb = new SafeHtmlBuilder();
					generateCode(sb, go, authz, attrName);
					return sb.toSafeHtml();
				}

				return SafeHtmlUtils.EMPTY_SAFE_HTML;
			}

			public void render(T object, SafeHtmlBuilder sb) {
				if (object != null) {
					GeneralObject go = object.cast();
					generateCode(sb, go, authz, attrName);
				}
			}

		});

		this.authz = authz;

	}


	/**
	 * Generates the code to be included in the cell
	 *
	 * @param sb safe html builder
	 * @param go object
	 * @param authz decide if cell is clickable and where it leads
	 * @param attrName what object parameter value should be displayed as text
	 */
	private static void generateCode(SafeHtmlBuilder sb, GeneralObject go, IsClickableCell<GeneralObject> authz, String attrName){

		if(go == null) return;

		if(authz.isClickable(go)){
			sb.appendHtmlConstant("<div class=\"customClickableTextCell "+getAdditionalStyle(go, attrName)+"\">");
			sb.appendHtmlConstant(getValue(go, attrName));
			sb.appendHtmlConstant("</div>");
		} else {
			//sb.appendHtmlConstant("<div class=\"customClickableTextCell\">");
			sb.appendHtmlConstant(getValue(go, attrName));
			//sb.appendHtmlConstant("</div>");
		}

	}

	/**
	 * Construct a new CustomClickableTextCellWithAuthz that will use a given
	 * {@link com.google.gwt.text.shared.SafeHtmlRenderer}.
	 *
	 * @param renderer a {@link com.google.gwt.text.shared.SafeHtmlRenderer SafeHtmlRenderer<String>} instance
	 */
	public CustomClickableTextCellWithAuthz(SafeHtmlRenderer<T> renderer) {
		super(renderer, "click", "keydown");
	}

	/**
	 * Called when a browser event occurs
	 */
	@Override
	public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		GeneralObject go = value.cast();
		if ("click".equals(event.getType())) {
			if (authz.isClickable(go)) {
				// click
				onEnterKeyDown(context, parent, value, event, valueUpdater);
			} else {
				// un-click
				event.stopPropagation();
			}
		}
	}

	/**
	 * Called a browser event occurs
	 */
	@Override
	protected void onEnterKeyDown(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
		if (valueUpdater != null) {
			valueUpdater.update(value);
		}
	}

	/**
	 * Return displayed value.
	 *
	 * Object param name is passed as attrName
	 *
	 * @param go GeneralObject
	 * @return attrName which object attribute is taken as value
	 */
	private static String getValue(GeneralObject go, String attrName){

		if (go == null) {
			return "";
		}

		if(go.getObjectType().equals("Vo")) {

			VirtualOrganization object = go.cast();
			if (attrName.equalsIgnoreCase("name")) {
				return object.getName();
			} else if (attrName.equalsIgnoreCase("shortName")) {
				return object.getShortName();
			} else if (attrName.equalsIgnoreCase("id")) {
				return ""+object.getId();
			}

		} else if(go.getObjectType().equals("Group")){

			Group object = go.cast();
			if (attrName.equalsIgnoreCase("name")) {
				return object.getName();
			} else if (attrName.equalsIgnoreCase("description")) {
				return object.getDescription();
			} else if (attrName.equalsIgnoreCase("id")) {
				return ""+object.getId();
			}

		} else if (go.getObjectType().equals("Facility")) {

			Facility object = go.cast();
			if (attrName.equalsIgnoreCase("name")) {
				return object.getName();
			} else if (attrName.equalsIgnoreCase("id")) {
				return ""+object.getId();
			} else if (attrName.equalsIgnoreCase("technicalOwners")) {
				String text = "";
				JsArray<Owner> owners = object.getOwners();
				for (int i=0; i<owners.length(); i++) {
					if ("technical".equals(owners.get(i).getType())) {
						text = text + owners.get(i).getName()+", ";
					}
				}
				if (text.length() >= 2) {
					text = text.substring(0, text.length()-2);
				}
				return text;
			}

		} else if (go.getObjectType().equals("RichMember")) {

			RichMember object = go.cast();
			if (attrName.equalsIgnoreCase("id")) {
				return ""+object.getId();
			} else if (attrName.equalsIgnoreCase("userId")) {
				return ""+object.getUserId();
			} else if (attrName.equalsIgnoreCase("status")) {
				return object.getStatus();
			} else if (attrName.equalsIgnoreCase("name")) {
				return object.getUser().getFullNameWithTitles();
			}  else if (attrName.equalsIgnoreCase("organization")) {

				Attribute at = object.getAttribute("urn:perun:member:attribute-def:def:organization");
				if (at != null && at.getValue() != null && !"null".equalsIgnoreCase(at.getValue())) {
					return at.getValue();
				} else {
					at = object.getAttribute("urn:perun:user:attribute-def:def:organization");
					if (at != null && at.getValue() != null && !"null".equalsIgnoreCase(at.getValue())) {
						return at.getValue();
					}
				}
				return "";

			} else if (attrName.equalsIgnoreCase("email")) {

				Attribute at = object.getAttribute("urn:perun:user:attribute-def:def:preferredMail");
				if (at != null && at.getValue() != null && !"null".equalsIgnoreCase(at.getValue())) {
					return at.getValue().replace(",", " ");
				} else {
					at = object.getAttribute("urn:perun:member:attribute-def:def:mail");
					if (at != null && at.getValue() != null && !"null".equalsIgnoreCase(at.getValue())) {
						return at.getValue().replace(",", " ");
					}
				}
				return "";

			}  else if (attrName.equalsIgnoreCase("logins")) {
				return object.getUserLogins();
			}

		}

		return "";

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

	/**
	 * Return additional style for cell based on Object and Attribute
	 *
	 * @return additional style name or empty
	 */
	public static String getAdditionalStyle(GeneralObject go, String attrName) {

		if (go.getObjectType().equalsIgnoreCase("RichMember")) {

			RichMember object = go.cast();
			// show rich members cells italic if INDIRECT membership
			if ("INDIRECT".equalsIgnoreCase(object.getMembershipType())) {
				return "italic";
			} else {
				return "";
			}

		}

		return "";

	}

}
