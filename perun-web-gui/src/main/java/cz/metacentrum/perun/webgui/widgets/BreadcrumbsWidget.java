package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.*;

/**
 * Breadcrumbs widget used in GUI header panel
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class BreadcrumbsWidget extends Composite {

	private FlexTable mainWidget = new FlexTable();

	/**
	 * Breadcrumbs widget
	 */
	public BreadcrumbsWidget() {

		this.initWidget(mainWidget);
		FlexTable.FlexCellFormatter fct = mainWidget.getFlexCellFormatter();
		this.setWidth("100%");
		fct.setWidth(0, 1, "100%");

	}

	public void setLocation(int menuSectionRole, String main, String mainLink) {
		setLocation(menuSectionRole, main, mainLink, "", "");
	}

	public void setLocation(int menuSectionRole, String main, String mainLink, String sub, String subLink) {

		sub = SafeHtmlUtils.fromString(sub).asString();
		main = SafeHtmlUtils.fromString(main).asString();

		mainWidget.clear();

		if (MainMenu.VO_ADMIN == menuSectionRole) {
			Image image = new Image(LargeIcons.INSTANCE.buildingIcon());
			mainWidget.setWidget(0, 0, image);
		} else if (MainMenu.PERUN_ADMIN == menuSectionRole) {
			Image image = new Image(LargeIcons.INSTANCE.perunIcon());
			mainWidget.setWidget(0, 0, image);
		} else if (MainMenu.GROUP_ADMIN == menuSectionRole) {
			Image image = new Image(LargeIcons.INSTANCE.groupIcon());
			mainWidget.setWidget(0, 0, image);
		} else if (MainMenu.FACILITY_ADMIN == menuSectionRole) {
			Image image = new Image(LargeIcons.INSTANCE.databaseServerIcon());
			mainWidget.setWidget(0, 0, image);
		} else if (MainMenu.USER == menuSectionRole) {
			Image image = new Image(LargeIcons.INSTANCE.userGrayIcon());
			mainWidget.setWidget(0, 0, image);
		} else if (MainMenu.SECURITY_ADMIN == menuSectionRole) {
			Image image = new Image(LargeIcons.INSTANCE.userPoliceEnglandIcon());
			mainWidget.setWidget(0, 0, image);
		}

		HTML text = new HTML();
		text.setStyleName("now-managing");
		String innerHtml = "";

		if (main != null && !main.isEmpty()) {
			if (mainLink != null && !mainLink.isEmpty()) {
				String active = "";
				if (mainLink.contains("?")) { active = "&active=1;"; } else { active = "?active=1;"; }
				innerHtml += "<a title=\""+main+"\" style=\"now-managing\" href=\"#"+mainLink+active+"\">"+Utils.getStrippedStringWithEllipsis(main, 40)+"</a>";
			} else {
				innerHtml += Utils.getStrippedStringWithEllipsis(main, 40);
			}
		}
		if (sub != null && !sub.isEmpty()) {
			if (subLink != null && !subLink.isEmpty()) {
				String active = "";
				if (subLink.contains("?")) { active = "&active=1;"; } else { active = "?active=1;"; }
				innerHtml += " &gt; <a title=\""+sub+"\" style=\"now-managing\" href=\"#"+subLink+active+"\">"+Utils.getStrippedStringWithEllipsis(sub, 40)+"</a>";
			} else {
				innerHtml += " &gt; "+Utils.getStrippedStringWithEllipsis(sub, 40);
			}
		}

		text.setHTML(innerHtml);
		ScrollPanel sp = new ScrollPanel();
		sp.setSize("100%", "20px");
		sp.setWidget(text);
		sp.addStyleName("perun-header-link");

		mainWidget.setWidget(0, 1, sp);
		mainWidget.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

	}

	public void setLocation(VirtualOrganization vo, String subSection, String subSectionLink) {

		subSection = SafeHtmlUtils.fromString(subSection).asString();

		mainWidget.clear();

		Image image = new Image(LargeIcons.INSTANCE.buildingIcon());
		mainWidget.setWidget(0, 0, image);

		HTML text = new HTML();
		text.setStyleName("now-managing");

		String innerHtml = "<a title=\""+vo.getName()+"\" style=\"now-managing\" href=\"#"+VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + "detail" + "?id=" + vo.getId()+"&active=1;\" >";
		innerHtml += Utils.getStrippedStringWithEllipsis(vo.getName(), 40);
		innerHtml += "</a>";

		if (subSection != null && !subSection.isEmpty()) {
			if (subSectionLink != null && !subSectionLink.isEmpty()) {
				String active = "";
				if (subSectionLink.contains("?")) { active = "&active=1;"; } else { active = "?active=1;"; }
				innerHtml += " &gt; <a style=\"now-managing\" href=\"#"+subSectionLink+active+"\" >"+subSection+"</a>";
				text.setHTML(innerHtml);
			} else {
				text.setHTML(innerHtml + " &gt; " +subSection);
			}
		} else {
			text.setHTML(innerHtml);
		}

		ScrollPanel sp = new ScrollPanel();
		sp.setSize("100%", "20px");
		sp.setWidget(text);
		sp.addStyleName("perun-header-link");

		mainWidget.setWidget(0, 1, sp);
		mainWidget.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

	}

	public void setLocation(Facility facility, String subSection, String subSectionLink) {

		subSection = SafeHtmlUtils.fromString(subSection).asString();

		mainWidget.clear();

		Image image = new Image(LargeIcons.INSTANCE.databaseServerIcon());
		mainWidget.setWidget(0, 0, image);

		HTML text = new HTML();
		text.setStyleName("now-managing");

		String innerHtml = "<a title=\""+facility.getName()+"\" style=\"now-managing\" href=\"#"+ FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + "detail" + "?id=" + facility.getId()+"&active=1;\" >";
		innerHtml += Utils.getStrippedStringWithEllipsis(facility.getName(), 40);
		innerHtml += "</a>";

		if (subSection != null && !subSection.isEmpty()) {
			if (subSectionLink != null && !subSectionLink.isEmpty()) {
				String active = "";
				if (subSectionLink.contains("?")) { active = "&active=1;"; } else { active = "?active=1;"; }
				innerHtml += " &gt; <a style=\"now-managing\" href=\"#"+subSectionLink+active+"\" >"+subSection+"</a>";
				text.setHTML(innerHtml);
			} else {
				text.setHTML(innerHtml + " &gt; " +subSection);
			}
		} else {
			text.setHTML(innerHtml);
		}

		ScrollPanel sp = new ScrollPanel();
		sp.setSize("100%", "20px");
		sp.setWidget(text);
		sp.addStyleName("perun-header-link");

		mainWidget.setWidget(0, 1, sp);
		mainWidget.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

	}

	public void setLocation(User user, String subSection, String subSectionLink) {

		subSection = SafeHtmlUtils.fromString(subSection).asString();

		mainWidget.clear();

		Image image = new Image(LargeIcons.INSTANCE.userGrayIcon());
		mainWidget.setWidget(0, 0, image);

		HTML text = new HTML();
		text.setStyleName("now-managing");

		String innerHtml = "<a title=\""+user.getFullNameWithTitles()+"\" style=\"now-managing\" href=\"#"+ UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + "info" + "?id=" + user.getId()+"&active=1;\" >";
		innerHtml += Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles(), 40);
		innerHtml += "</a>";

		if (subSection != null && !subSection.isEmpty()) {
			if (subSectionLink != null && !subSectionLink.isEmpty()) {
				String active = "";
				if (subSectionLink.contains("?")) { active = "&active=1;"; } else { active = "?active=1;"; }
				innerHtml += " &gt; <a style=\"now-managing\" href=\"#"+subSectionLink+active+"\" >"+subSection+"</a>";
				text.setHTML(innerHtml);
			} else {
				text.setHTML(innerHtml + " &gt; " +subSection);
			}
		} else {
			text.setHTML(innerHtml);
		}

		ScrollPanel sp = new ScrollPanel();
		sp.setSize("100%", "20px");
		sp.setWidget(text);
		sp.addStyleName("perun-header-link");

		mainWidget.setWidget(0, 1, sp);
		mainWidget.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

	}

	public void setLocation(Group group, String subSection, String subSectionLink) {

		subSection = SafeHtmlUtils.fromString(subSection).asString();

		mainWidget.clear();

		Image image = new Image(LargeIcons.INSTANCE.groupIcon());
		mainWidget.setWidget(0, 0, image);

		HTML text = new HTML();
		text.setStyleName("now-managing");

		String innerHtml = "<a title=\""+group.getName()+"\" style=\"now-managing\" href=\"#"+ GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + "detail?id=" + group.getId()+"&active=1;\" >";
		innerHtml += Utils.getStrippedStringWithEllipsis(group.getName(), 40);
		innerHtml += "</a>";

		if (subSection != null && !subSection.isEmpty()) {
			if (subSectionLink != null && !subSectionLink.isEmpty()) {
				String active = "";
				if (subSectionLink.contains("?")) { active = "&active=1;"; } else { active = "?active=1;"; }
				innerHtml += " &gt; <a style=\"now-managing\" href=\"#"+subSectionLink+active+"\" >"+subSection+"</a>";
				text.setHTML(innerHtml);
			} else {
				text.setHTML(innerHtml + " &gt; " +subSection);
			}
		} else {
			text.setHTML(innerHtml);
		}

		ScrollPanel sp = new ScrollPanel();
		sp.setSize("100%", "20px");
		sp.setWidget(text);
		sp.addStyleName("perun-header-link");

		mainWidget.setWidget(0, 1, sp);
		mainWidget.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

	}

	public void setLocation(SecurityTeam securityTeam, String subSection, String subSectionLink) {

		subSection = SafeHtmlUtils.fromString(subSection).asString();

		mainWidget.clear();

		Image image = new Image(LargeIcons.INSTANCE.userPoliceEnglandIcon());
		mainWidget.setWidget(0, 0, image);

		HTML text = new HTML();
		text.setStyleName("now-managing");

		String innerHtml = "<a title=\""+securityTeam.getName()+"\" style=\"now-managing\" href=\"#"+ SecurityTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + "detail?id=" + securityTeam.getId()+"&active=1;\" >";
		innerHtml += Utils.getStrippedStringWithEllipsis(securityTeam.getName(), 40);
		innerHtml += "</a>";

		if (subSection != null && !subSection.isEmpty()) {
			if (subSectionLink != null && !subSectionLink.isEmpty()) {
				String active = "";
				if (subSectionLink.contains("?")) { active = "&active=1;"; } else { active = "?active=1;"; }
				innerHtml += " &gt; <a style=\"now-managing\" href=\"#"+subSectionLink+active+"\" >"+subSection+"</a>";
				text.setHTML(innerHtml);
			} else {
				text.setHTML(innerHtml + " &gt; " +subSection);
			}
		} else {
			text.setHTML(innerHtml);
		}

		ScrollPanel sp = new ScrollPanel();
		sp.setSize("100%", "20px");
		sp.setWidget(text);
		sp.addStyleName("perun-header-link");

		mainWidget.setWidget(0, 1, sp);
		mainWidget.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

	}

	public void clearLocation() {
		mainWidget.clear();
	}

}
