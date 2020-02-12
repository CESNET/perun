package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.ChangeStatusTabItem;

/**
 * Custom GWT cell, which displays current status of the element
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @param <T>
 */
public class PerunStatusWidget<T extends JavaScriptObject> extends Composite {

	static private final ImageResource VALID = SmallIcons.INSTANCE.acceptIcon();
	static private final ImageResource INVALID = SmallIcons.INSTANCE.flagRedIcon();
	static private final ImageResource SUSPENDED = SmallIcons.INSTANCE.stopIcon();
	static private final ImageResource EXPIRED = SmallIcons.INSTANCE.flagYellowIcon();
	static private final ImageResource DISABLED = SmallIcons.INSTANCE.binClosedIcon();

	// the object which is used for settings
	private GeneralObject object;

	// the widget itself
	private FlexTable statusWidget = new FlexTable();

	// the callback to be called when user click on the change status and confirms it
	JsonCallbackEvents events;

	// item's name
	private String objectName = "item";

	private TabItem containingTabItem = null;

	/**
	 * Creates the new status widget
	 * @param object Object to show status for
	 */
	public PerunStatusWidget(T object) {
		this.object = object.cast();
		this.checkStatus();
		this.initWidget(statusWidget);
		this.build();
	}

	/**
	 * Creates the new status widget
	 * @param object Object to show status for
	 * @param name The object's name.
	 * @param events Events triggered on status change
	 */
	public PerunStatusWidget(T object, String name, JsonCallbackEvents events) {
		this.object = object.cast();
		this.checkStatus();
		this.initWidget(statusWidget);
		this.objectName = name;
		this.events = events;
		this.build();
	}

	/**
	 * Creates the new status widget
	 * @param object Object to show status for
	 * @param name The object's name.
	 * @param events Events triggered on status change
	 */
	public PerunStatusWidget(T object, String name, JsonCallbackEvents events, TabItem tabItem) {
		this.object = object.cast();
		this.checkStatus();
		this.initWidget(statusWidget);
		this.objectName = name;
		this.events = events;
		this.containingTabItem = tabItem;
		this.build();
	}

	/**
	 * Builds the widget
	 */
	private void build() {

		statusWidget.clear(true);
		statusWidget.setStyleName("member-status");
		statusWidget.setCellSpacing(0);
		statusWidget.setCellPadding(0);

		// image
		statusWidget.setWidget(0, 0, getImage());
		statusWidget.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);

		// text
		HTML stat = new HTML(object.getStatus());
		statusWidget.setWidget(0, 1, stat);
		statusWidget.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

		// if a callback set
		if(events != null) {

			// FIXME better usage
			if (object.getObjectType().equals("Member") || object.getObjectType().equals("RichMember")) {

				Member m = object.cast();
				if (PerunWebSession.getInstance().isVoAdmin(m.getVoId())) {
					// 	change button
					statusWidget.setWidget(0, 2, getChangeStatusButton());
					statusWidget.getFlexCellFormatter().setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_MIDDLE);
				}

			} else {
				// 	change button
				statusWidget.setWidget(0, 2, getChangeStatusButton());
				statusWidget.getFlexCellFormatter().setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_MIDDLE);
			}

		}

	}

	/**
	 * Return the widget with image
	 * @return image
	 */
	private Widget getImage() {

		// Status
		String status = object.getStatus();

		// selects the image according to the status
		ImageResource ir = null;

		if(status.equalsIgnoreCase("VALID")){
			ir = VALID;
		} else if (status.equalsIgnoreCase("INVALID")){
			ir = INVALID;
		} else if (status.equalsIgnoreCase("SUSPENDED")){
			ir = SUSPENDED;
		} else if (status.equalsIgnoreCase("EXPIRED")){
			ir = EXPIRED;
		} else if (status.equalsIgnoreCase("DISABLED")){
			ir = DISABLED;
		}

		// if status not available
		if(ir == null){
			return new HTML("");
		}

		// return the image
		Image im = new Image(ir);
		return im;
	}

	/**
	 * Changing status button
	 * @return the widget
	 */
	private Widget getChangeStatusButton() {
		Anchor button = new Anchor("change");
		button.setTitle("Change status for " + objectName + ".");
		button.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				JsonCallbackEvents newEvent = JsonCallbackEvents.mergeEvents(events, new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						if (jso != null) {
							// fixme - since we pass this event to more tabs and update expiration (set attributes)
							//  passed object might not be relevant for this action
							Member m = jso.cast();
							object.setStatus(m.getStatus());
						}
						build();
						// forcefully draw content
						if (containingTabItem != null) {
							containingTabItem.draw();
						}
					}
				});
				PerunWebSession.getInstance().getTabManager().addTabToCurrentTab(new ChangeStatusTabItem(object.cast(), newEvent));
			}
		});
		return button;
	}

	/**
	 * Checks object's status
	 */
	private void checkStatus() {
		if(object.getStatus() == null){
			throw new RuntimeException("Item's status cannot be null.");
		}
	}

}
