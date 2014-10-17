package cz.metacentrum.perun.webgui.client.applicationresources.pages;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.ApplicationFormGui;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ApplicationMessages;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationDataById;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationsForUserForAppFormGui;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PerunPrincipal;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Page with list of user applications
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class UsersApplicationsPage extends ApplicationPage {

	private VerticalPanel bodyContents = new VerticalPanel();
	private PerunWebSession session = PerunWebSession.getInstance();
	final CustomButton backButton = new CustomButton(ApplicationMessages.INSTANCE.backButton(), ApplicationMessages.INSTANCE.backButtonMessage(), SmallIcons.INSTANCE.arrowLeftIcon());

	/**
	 * User's applications page
	 */
	public UsersApplicationsPage(){

		this.initWidget(bodyContents);
		bodyContents.setWidth("100%");
		bodyContents.setStyleName("mainPanel");
		bodyContents.setSpacing(5);
	}

	/**
	 * Refresh the page
	 */
	private void refresh()
	{

		bodyContents.clear();

		String user = "";

		if (session.getUser() != null) {
			user = this.session.getUser().getFullNameWithTitles().trim();
		} else {
			PerunPrincipal pp = session.getPerunPrincipal();
			if (!pp.getAdditionInformations("displayName").equals("")) {
				user = pp.getAdditionInformations("displayName");
			} else if (!pp.getAdditionInformations("cn").equals("")) {
				user = pp.getAdditionInformations("cn");
			} else {
				if (pp.getExtSourceType().equals("cz.metacentrum.perun.core.impl.ExtSourceX509")) {
					user = Utils.convertCertCN(pp.getActor());
				} else {
					user = pp.getActor();
				}
			}
		}

		bodyContents.add(new HTML("<h1>" + ApplicationMessages.INSTANCE.applicationsForUser(user) + "</h1>"));

		// callback
		int userId = 0;
		if (session.getUser() != null) {
			userId = session.getUser().getId();
		}

		final GetApplicationsForUserForAppFormGui req = new GetApplicationsForUserForAppFormGui(userId);
		final ListBox listBox = new ListBox();

		req.setEvents(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				ArrayList<Application> appls = JsonUtils.jsoAsList(jso);
				ArrayList<String> vos = new ArrayList<String>();
				for (Application app : appls) {
					if (!vos.contains(app.getVo().getName())) {
						vos.add(app.getVo().getName());
					}
				}
				Collections.sort(vos);
				for (String s : vos) {
					listBox.addItem(s);
				}
				if (listBox.getItemCount() > 0) {
					listBox.insertItem(WidgetTranslation.INSTANCE.listboxAll(), 0);
				}
				for (int i=0; i<listBox.getItemCount(); i++) {
					if (listBox.getItemText(i).equals(ApplicationFormGui.getVo().getName())) {
						listBox.setSelectedIndex(i);
						req.filterTable(ApplicationFormGui.getVo().getName());
						break;
					}
				}
			}
			@Override
			public void onError(PerunError error) {
			}
			@Override
			public void onLoadingStart() {
				listBox.clear();
			}

		});
		req.setCheckable(false);

		listBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				if (listBox.getSelectedIndex() > 0) {
					req.filterTable(listBox.getItemText(listBox.getSelectedIndex()));
				} else {
					// show all
					req.filterTable("");
				}

			}
		});

		final TabMenu tabMenu = new TabMenu();

		tabMenu.addWidget(new HTML("<strong>"+ApplicationMessages.INSTANCE.filterByVo()+":</strong>"));
		tabMenu.addWidget(listBox);
		tabMenu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
		tabMenu.addWidget(new HTML("<strong>"+ApplicationMessages.INSTANCE.clickOnApplicationToSee()+"</strong>"));
		tabMenu.addStyleName("tabMenu");

		final VerticalPanel applicationsWrapper = new VerticalPanel();
		applicationsWrapper.setSize("100%", "100%");

		applicationsWrapper.add(tabMenu);

		final CellTable<Application> appsTable = req.getTable(new FieldUpdater<Application, String>() {
			public void update(int index, Application object, String value) {
				applicationsWrapper.clear();
				applicationsWrapper.add(backButton);
				applicationsWrapper.add(getApplicationDetailWidget(object));
			}
		});
		appsTable.addStyleName("perun-table");
		applicationsWrapper.add(appsTable);

		backButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				applicationsWrapper.clear();
				applicationsWrapper.add(tabMenu);
				applicationsWrapper.add(appsTable);
			}
		});


		bodyContents.add(applicationsWrapper);
	}


	/**
	 * Returns application detail
	 * @param app
	 * @return
	 */
	protected Widget getApplicationDetailWidget(Application app) {

		GetApplicationDataById data = new GetApplicationDataById(app.getId());
		data.setShowAdminItems(false);
		data.retrieveData();

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		if (app.getGroup() == null) {
			vp.add(new HTML("<h2>" + ApplicationMessages.INSTANCE.applicationDetail(Application.getTranslatedType(app.getType()), app.getVo().getName())+"</h2>"));
		} else {
			vp.add(new HTML("<h2>" + ApplicationMessages.INSTANCE.applicationDetailGroup(Application.getTranslatedType(app.getType()), app.getGroup().getName(), app.getVo().getName())+"</h2>"));
		}
		vp.add(new HTML(ApplicationMessages.INSTANCE.applicationDetailMessage(app.getCreatedAt().substring(0, app.getCreatedAt().indexOf(".")), Application.getTranslatedState(app.getState()))));
		vp.add(new HTML("<hr size=\"1\" style=\"color #ccc;\"/>"));
		vp.add(data.getContents());

		return vp;
	}

	@Override
	public void menuClick() {
		refresh();
	}

}
