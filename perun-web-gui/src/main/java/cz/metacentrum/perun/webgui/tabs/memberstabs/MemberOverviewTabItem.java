package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetListOfAttributes;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.MembershipExpirationWidget;
import cz.metacentrum.perun.webgui.widgets.PerunStatusWidget;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Displays members overview
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MemberOverviewTabItem implements TabItem {

	private RichMember member;
	private int memberId;
	private PerunWebSession session = PerunWebSession.getInstance();
	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Loading member details");
	private int groupId = 0;

	/**
	 * Constructor
	 *
	 * @param member RichMember object, typically from table
	 */
	public MemberOverviewTabItem(RichMember member, int groupId){
		this.member = member;
		this.memberId = member.getId();
		this.groupId = groupId;
	}

	public boolean isPrepared(){
		return !(member == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(member.getUser().getFullNameWithTitles().trim()));

		// main widget panel
		ScrollPanel vp = new ScrollPanel();
		vp.setSize("100%","100%");

		VerticalPanel innerVp = new VerticalPanel();
		innerVp.setSize("100%", "100%");
		vp.add(innerVp);

		TabMenu menu = new TabMenu();
		innerVp.add(menu);
		innerVp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		session.getUiElements().resizeSmallTabPanel(vp, 400);

		FlexTable layout = new FlexTable();
		layout.setSize("100%","100%");

		innerVp.add(layout);

		layout.setHTML(0, 0, "<p>Personal:");
		layout.setHTML(0, 1, "<p>Membership:");
		layout.getFlexCellFormatter().setWidth(0, 0, "50%");
		layout.getFlexCellFormatter().setWidth(0, 1, "50%");

		layout.getFlexCellFormatter().setStyleName(0, 0, "subsection-heading");
		layout.getFlexCellFormatter().setStyleName(0, 1, "subsection-heading");

		// if attribute not set
		final String notSet = "<i>N/A</i>";

		final FlexTable personalLayout = new FlexTable();
		layout.setWidget(1, 0, personalLayout);
		personalLayout.setStyleName("inputFormFlexTableDark");

		personalLayout.setHTML(0, 0, "Organization:");
		personalLayout.setHTML(1, 0, "Workplace:");
		personalLayout.setHTML(2, 0, "Research group:");
		personalLayout.setHTML(3, 0, "Preferred mail:");
		personalLayout.setHTML(4, 0, "Mail:");
		personalLayout.setHTML(5, 0, "Phone:");
		personalLayout.setHTML(6, 0, "Address:");
		personalLayout.setHTML(7, 0, "Preferred language:");
		personalLayout.setHTML(8, 0, "LoA:");
		personalLayout.setHTML(9, 0, "EDU person affiliation:");

		// one empty cell to create empty column
		personalLayout.setHTML(0, 1, "&nbsp;");
		personalLayout.getFlexCellFormatter().setWidth(0, 1, "70%");

		// style personal table
		for (int i=0; i<personalLayout.getRowCount(); i++) {
			personalLayout.getFlexCellFormatter().addStyleName(i, 0, "itemName");
		}

		// Membership table
		final FlexTable memberLayout = new FlexTable();
		layout.setWidget(1, 1, memberLayout);
		layout.getFlexCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
		memberLayout.setStyleName("inputFormFlexTableDark");

		memberLayout.setHTML(0, 0, "Status:");
		final PerunStatusWidget<RichMember> statusWidget;
		if (session.isVoAdmin(member.getVoId())) {
			JsonCallbackEvents event = new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					// UPDATE OBJECT
					Member m = jso.cast();
					member.setStatus(m.getStatus());
				}
			};
			statusWidget = new PerunStatusWidget<RichMember>(member, member.getUser().getFullName(), event);
		} else {
			statusWidget = new PerunStatusWidget<RichMember>(member, member.getUser().getFullName(), null);
		}
		memberLayout.setWidget(0, 1, statusWidget);
		memberLayout.getFlexCellFormatter().setRowSpan(0, 0, 2);

		if (member.getStatus().equalsIgnoreCase("VALID")) {
			memberLayout.setHTML(1, 0, "Member is properly configured and have access on provided resources.");
		} else if (member.getStatus().equalsIgnoreCase("INVALID")) {
			memberLayout.setHTML(1, 0, "Member have configuration error and DON'T have access on provided resources. You can check what is wrong by changing member's status to VALID. If possible, procedure will configure all necessary settings by itself.");
		} else if (member.getStatus().equalsIgnoreCase("SUSPENDED")) {
			memberLayout.setHTML(1, 0, "Member violated some rules and DON'T have access on provided resources.");
		} else if (member.getStatus().equalsIgnoreCase("EXPIRED")) {
			memberLayout.setHTML(1, 0, "Member didn't extend membership and DON'T have access on provided resources.");
		} else if (member.getStatus().equalsIgnoreCase("DISABLED")) {
			memberLayout.setHTML(1, 0, "Member didn't extend membership long time ago or was manually disabled and DON'T have access on provided resources.");
		}
		memberLayout.getFlexCellFormatter().setStyleName(1, 0, "inputFormInlineComment");

		memberLayout.setHTML(2, 0, "Expiration:");
		memberLayout.setHTML(3, 0, "Member type:");
		if (member.getUser().isServiceUser()) {
			memberLayout.setHTML(3, 1, "Service");
		} else if (member.getUser().isSponsoredUser()) {
			memberLayout.setHTML(3, 1, "Sponsored");
		} else {
			memberLayout.setHTML(3, 1, "Person");
		}
		memberLayout.setHTML(4, 0, "Sponsored by:");
		memberLayout.setHTML(5, 0, "Member ID:");
		memberLayout.setHTML(5, 1, member.getId()+"");
		memberLayout.setHTML(6, 0, "User ID:");
		memberLayout.setHTML(6, 1, member.getUser().getId()+"");

		if (session.isVoAdmin(member.getVoId())) {

			CustomButton resetButton = new CustomButton("Send password reset request…", "", SmallIcons.INSTANCE.keyIcon(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					session.getTabManager().addTabToCurrentTab(new SendPasswordResetRequestTabItem(member));
				}
			});

			memberLayout.setHTML(7, 0, "Password reset");
			memberLayout.setWidget(7, 1, resetButton);

		}

		// style member table
		for (int i=0; i<memberLayout.getRowCount(); i++) {
			if (i != 1) {
				memberLayout.getFlexCellFormatter().addStyleName(i, 0, "itemName");
			}
		}

		// attributes to load
		ArrayList<String> attrs = new ArrayList<String>();
		// TODO - switch all personal to member attrs
		attrs.add("urn:perun:user:attribute-def:def:organization");
		attrs.add("urn:perun:user:attribute-def:def:workplace");
		attrs.add("urn:perun:user:attribute-def:opt:researchGroup");
		attrs.add("urn:perun:member:attribute-def:def:mail");
		attrs.add("urn:perun:user:attribute-def:def:preferredMail");
		attrs.add("urn:perun:user:attribute-def:def:phone");
		attrs.add("urn:perun:user:attribute-def:def:address");
		attrs.add("urn:perun:user:attribute-def:def:preferredLanguage");
		attrs.add("urn:perun:member:attribute-def:virt:loa");
		attrs.add("urn:perun:member:attribute-def:def:membershipExpiration");
		attrs.add("urn:perun:member:attribute-def:opt:eduPersonAffiliation");
		attrs.add("urn:perun:member:attribute-def:def:sponzoredMember");

		HashMap<String, Integer> ids = new HashMap<String, Integer>();
		ids.put("member", memberId);
		ids.put("workWithUserAttributes", 1);

		GetListOfAttributes attrsCall = new GetListOfAttributes();

		attrsCall.setEvents(new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				ArrayList<Attribute> list = JsonUtils.jsoAsList(jso);
				if (list != null && !list.isEmpty()) {
					for (Attribute a : list) {
						String value = SafeHtmlUtils.fromString((a.getValue() != null) ? a.getValue() : "").asString();
						if (a.getName().equalsIgnoreCase("urn:perun:user:attribute-def:def:organization")) {
							if (!"null".equals(value)) {
								personalLayout.setHTML(0, 1, value);
							} else {
								personalLayout.setHTML(0, 1, notSet);
							}
							// set default value width
						} else if (a.getName().equalsIgnoreCase("urn:perun:user:attribute-def:def:workplace")) {
							if (!"null".equals(value)) {
								personalLayout.setHTML(1, 1, value);
							} else {
								personalLayout.setHTML(1, 1, notSet);
							}
						} else if (a.getName().equalsIgnoreCase("urn:perun:user:attribute-def:opt:researchGroup")) {
							if (!"null".equals(value)) {
								personalLayout.setHTML(2, 1, value);
							} else {
								personalLayout.setHTML(2, 1, notSet);
							}
						} else if (a.getName().equalsIgnoreCase("urn:perun:user:attribute-def:def:preferredMail")) {
							if (!"null".equals(value)) {
								personalLayout.setHTML(3, 1, value);
							} else {
								personalLayout.setHTML(3, 1, notSet);
							}
						} else if (a.getName().equalsIgnoreCase("urn:perun:member:attribute-def:def:mail")) {
							if (!"null".equals(value)) {
								personalLayout.setHTML(4, 1, value);
							} else {
								personalLayout.setHTML(4, 1, notSet);
							}
						} else if (a.getName().equalsIgnoreCase("urn:perun:user:attribute-def:def:phone")) {
							if (!"null".equals(value)) {
								personalLayout.setHTML(5, 1, value);
							} else {
								personalLayout.setHTML(5, 1, notSet);
							}
						} else if (a.getName().equalsIgnoreCase("urn:perun:user:attribute-def:def:address")) {
							if (!"null".equals(value)) {
								personalLayout.setHTML(6, 1, value);
							} else {
								personalLayout.setHTML(6, 1, notSet);
							}
						} else if (a.getName().equalsIgnoreCase("urn:perun:user:attribute-def:def:preferredLanguage")) {
							if (!"null".equals(value)) {
								personalLayout.setHTML(7, 1, value);
							} else {
								personalLayout.setHTML(7, 1, notSet);
							}
						} else if (a.getName().equalsIgnoreCase("urn:perun:member:attribute-def:virt:loa")) {
							if (!"null".equals(value)) {
								String text = "";
								if (value.equals("0")) {
									text = " (not verified = default)";
								} else if (value.equals("1")) {
									text = " (verified email)";
								} else if (value.equals("2")) {
									text = " (verified identity)";
								} else if (value.equals("3")) {
									text = " (verified identity, strict password strength)";
								}
								personalLayout.setHTML(8, 1, value + text);
							} else {
								personalLayout.setHTML(8, 1, notSet);
							}
						} else if (a.getName().equalsIgnoreCase("urn:perun:member:attribute-def:opt:eduPersonAffiliation")) {
							if (!"null".equals(value)) {
								personalLayout.setHTML(9, 1, value);
							} else {
								personalLayout.setHTML(9, 1, notSet);
							}
						} else if (a.getName().equalsIgnoreCase("urn:perun:member:attribute-def:def:membershipExpiration")) {
							// set attribute inside member
							member.setAttribute(a);
							memberLayout.setWidget(2, 1, new MembershipExpirationWidget(member));
						} else if (a.getName().equalsIgnoreCase("urn:perun:member:attribute-def:def:sponzoredMember")) {
							if (!"null".equals(value)) {
								memberLayout.setHTML(4, 1, value + " (ID of RT ticket with explanation)");
							} else {
								memberLayout.setHTML(4, 1, "<i>N/A</i>");
							}

						}

					}
				}
			}

		@Override
		public void onError(PerunError error) {
			String text = "<span style=\"color: red\">Error while loading";
			for (int i=0; i<personalLayout.getRowCount(); i++) {
				personalLayout.setHTML(i, 1, text);
			}
			memberLayout.setHTML(2, 1, text);
			memberLayout.setHTML(4, 1, text);
		}

		@Override
		public void onLoadingStart() {
			for (int i=0; i<personalLayout.getRowCount(); i++) {
				personalLayout.setWidget(i, 1, new Image(AjaxLoaderImage.SMALL_IMAGE_URL));
			}
			memberLayout.setWidget(2, 1, new Image(AjaxLoaderImage.SMALL_IMAGE_URL));
			memberLayout.setWidget(4, 1, new Image(AjaxLoaderImage.SMALL_IMAGE_URL));
		}
		});

		attrsCall.getListOfAttributes(ids, attrs);

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
		return SmallIcons.INSTANCE.userGreenIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1453;
		int result = 1;
		result = prime * result + memberId;
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
		MemberOverviewTabItem other = (MemberOverviewTabItem) obj;
		if (memberId != other.memberId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {

	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(member.getVoId()) || session.isVoObserver(member.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

}
