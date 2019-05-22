package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetListOfAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab which allow to set VO mails footer
 * !!! USE AS INNER TAB ONLY !!!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class EditMailFooterTabItem implements TabItem {

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("Edit mail footer");

	/**
	 * Entity ID to set
	 */
	private VirtualOrganization vo;
	private int voId = 0;

	private Group group = null;
	private int groupId = 0;

	/**
	 * Creates a tab instance
	 *
	 * @param vo
	 */
	public EditMailFooterTabItem(VirtualOrganization vo){
		this.voId = vo.getId();
		this.vo = vo;
	}

	public EditMailFooterTabItem(Group group) {
		this.voId = group.getVoId();
		this.groupId = group.getId();
		this.group = group;
	}

	public boolean isPrepared(){
		return true;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		final FlexTable content = new FlexTable();
		content.setStyleName("inputFormFlexTable");
		content.setWidth("360px");
		final TextArea footer = new TextArea();
		footer.setSize("340px", "200px");

		content.setHTML(0, 0,"Footer text:");
		content.getFlexCellFormatter().setStyleName(0, 0, "itemName");
		content.setWidget(1, 0, footer);
		content.setHTML(2, 0, "This text will be added as footer for all email notifications (replacing {mailFooter} tag in mail definition).");
		content.getFlexCellFormatter().setStyleName(2, 0, "inputFormInlineComment");

		final Map<String, Integer> ids = new HashMap<String,Integer>();
		if (group == null) {
			ids.put("vo", voId);
		} else {
			ids.put("group", groupId);
		}

		final ArrayList<Attribute> list = new ArrayList<Attribute>();

		final GetListOfAttributes call = new GetListOfAttributes(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				list.addAll(JsonUtils.<Attribute>jsoAsList(jso));
				// only if attribute exists
				if (list != null && !list.isEmpty()) {
					for (Attribute a : list) {
						if (a.getFriendlyName().equalsIgnoreCase("mailFooter")) {
							// if value not null - enter
							if (!a.getValue().equalsIgnoreCase("null")) {
								footer.setText(a.getValue());
							}
						}
					}
				}
			}
		});

		ArrayList<String> l = new ArrayList<String>();
		if (group == null) {
			l.add("urn:perun:vo:attribute-def:def:mailFooter");
		} else {
			l.add("urn:perun:group:attribute-def:def:mailFooter");
		}
		call.getListOfAttributes(ids, l);

		final TabItem tab = this;

		VerticalPanel vp = new VerticalPanel();
		TabMenu menu = new TabMenu();

		final CustomButton save = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveMailFooter());
		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {

				ArrayList<Attribute> toSend = new ArrayList<Attribute>(); // will be set
				ArrayList<Attribute> toRemove = new ArrayList<Attribute>(); // will be removed

				for (Attribute a : list) {

					String oldValue = a.getValue();
					String newValue = "";

					if (a.getFriendlyName().equalsIgnoreCase("mailFooter")) {
						newValue = footer.getText();
					} else {
						continue; // other than mailFooter attributes must be skipped
					}

					if (oldValue.equalsIgnoreCase(newValue) || (oldValue.equalsIgnoreCase("null") && newValue.equalsIgnoreCase(""))) {
						// if both values are the same or both are "empty"
						continue; // skip this cycle
					} else {
						if (newValue.equalsIgnoreCase("")) {
							toRemove.add(a); // value was cleared
						} else {
							a.setValue(newValue); // set value
							toSend.add(a); // value was changed / added
						}
					}
				}

				// requests
				SetAttributes request = new SetAttributes(JsonCallbackEvents.closeTabDisableButtonEvents(save, tab));
				RemoveAttributes removeRequest = new RemoveAttributes(JsonCallbackEvents.closeTabDisableButtonEvents(save, tab));
				// send if not empty
				if (!toRemove.isEmpty()) {
					removeRequest.removeAttributes(ids, toRemove);
				}
				if (!toSend.isEmpty()) {
					request.setAttributes(ids, toSend);
				}
			}
		});

		menu.addWidget(save);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		vp.add(content);
		vp.add(menu);
		vp.setCellHeight(menu, "30px");
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.addIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 997;
		int result = 1;
		result = prime * result + 6786786;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EditMailFooterTabItem create = (EditMailFooterTabItem) obj;
		if (voId != create.voId || groupId != create.groupId){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		// no open for inner tab
	}

	public boolean isAuthorized() {
		return (session.isVoAdmin(voId) || session.isGroupAdmin(groupId));
	}

}
