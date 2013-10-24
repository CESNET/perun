package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.RichMember;

/**
 * Custom GWT widget, which handles
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class MembershipExpirationWidget extends Composite {

    // the widget itself
    private FlexTable statusWidget = new FlexTable();
    private RichMember member;

    /**
     * Creates the new status widget
     * @param m member to show expiration for
     */
    public MembershipExpirationWidget(RichMember m) {
        this.member = m;
        this.initWidget(statusWidget);
        this.build();
    }

    /**
     * Builds the widget
     */
    private void build() {

        statusWidget.clear(true);
        statusWidget.setCellSpacing(0);
        statusWidget.setCellPadding(0);
        statusWidget.setStyleName("membership-expiration");

        if (member != null) {

            Attribute expire = member.getAttribute("urn:perun:member:attribute-def:def:membershipExpiration");
            if (expire != null && !"null".equalsIgnoreCase(expire.getValue())) {
                statusWidget.setHTML(0, 0, expire.getValue());
            } else {
                statusWidget.setHTML(0, 0, "<i>never</i>");
            }
            /*
            TODO - re-think this since there might not be any attribute
            if (expire != null && expire.isWritable()) {
                Anchor change = new Anchor("change");
                change.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        PerunWebSession.getInstance().getTabManager().addTabToCurrentTab(new MembershipExpirationTabItem(member, null));
                    }
                });
                statusWidget.setWidget(0, 1, change);
                statusWidget.getFlexCellFormatter().setStyleName(0, 1, "change");
            }
            */
        } else {

        }

    }

}
