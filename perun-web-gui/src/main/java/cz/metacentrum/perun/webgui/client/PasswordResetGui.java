package cz.metacentrum.perun.webgui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.passwordresetresources.PasswordResetFormPage;
import cz.metacentrum.perun.webgui.client.passwordresetresources.PasswordResetLeftMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.authzResolver.GetPerunPrincipal;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PerunPrincipal;
import cz.metacentrum.perun.webgui.widgets.NotUserOfPerunWidget;

/**
 * The main Password Reset GUI class. It's GWT Entry point.
 * 
 * Loads whole GUI, makes login to RPC by calling GetPerunPrincipal
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PasswordResetGui implements EntryPoint {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

    private static boolean checkPending = false;
	
	private PasswordResetLeftMenu leftMenu; 
	/**
	 * Main content panel
	 */
	private ScrollPanel contentPanel = new ScrollPanel();

	/**
	 * This is ENTRY POINT method. It's called automatically when web page
	 * containing this GUI is loaded in browser.
	 */
	public void onModuleLoad() {

		// perun web session
		session = PerunWebSession.getInstance();
		
		// basic settings
		session.setUiElements(new UiElements(null));

		// Get web page's BODY
		RootLayoutPanel body = RootLayoutPanel.get();
		body.setStyleName("mainPanel");
		
		// check RPC url
		if(session.getRpcUrl().isEmpty()){
			VerticalPanel bodyContents = new VerticalPanel();
			bodyContents.setSize("100%", "300px");
			bodyContents.add(new HTML(new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>RPC SERVER NOT FOUND !</h2>"));
			bodyContents.setCellHorizontalAlignment(bodyContents.getWidget(0), HasHorizontalAlignment.ALIGN_CENTER);
			bodyContents.setCellVerticalAlignment(bodyContents.getWidget(0), HasVerticalAlignment.ALIGN_BOTTOM);
			body.add(bodyContents);			
			return;
		}
		
		// WEB PAGE SPLITTER
		DockLayoutPanel bodySplitter = new DockLayoutPanel(Unit.PX);
		body.add(bodySplitter);
		
		// left menu
		//leftMenu = new PasswordResetLeftMenu();
		// bodySplitter.addWest(leftMenu, 240);

		// MAIN CONTENT
		contentPanel.setSize("100%", "100%");
		//contentPanel.add(leftMenu.getContent());
		bodySplitter.add(contentPanel);
				
		loadPerunPrincipal();
		

	}
	
	/**
	 * Performs a login into the RPC, loads user and his roles into session and enables GUI.
	 */
	private void loadPerunPrincipal() {

		// show loading box
		final PopupPanel loadingBox = session.getUiElements().perunLoadingBox();
		loadingBox.show();		

		// events after getting PerunPrincipal from RPC
		final JsonCallbackEvents events = new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso) {
				
				// store perun principal into session for future use
				PerunPrincipal pp = (PerunPrincipal)jso;
				session.setPerunPrincipal(pp);
				
				// check if user exists
                if (session.getUser() == null && !pp.getRoles().hasAnyRole()) {
					// if not and no role, redraw page body
					RootLayoutPanel body = RootLayoutPanel.get();
					loadingBox.hide();
					body.clear();
					body.add(new NotUserOfPerunWidget());
					return;					
				}

				// store users roles and editable entities into session
				session.setRoles(pp.getRoles());
				
				// display logged user
				session.getUiElements().setLoggedUserInfo(pp);

				// hides the loading box
				loadingBox.hide();
				
				// add menu item and load content
                contentPanel.setWidget(new PasswordResetFormPage().getContent());
                //Anchor a = leftMenu.addMenuContents("Password reset", SmallIcons.INSTANCE.keyIcon(), new PasswordResetFormPage().getContent());
                //a.fireEvent(new ClickEvent(){});

            }

			public void onError(PerunError error){
				// hides the loading box
				loadingBox.hide();

				// shows error box
				PopupPanel loadingFailedBox;
				if (error == null) {
					loadingFailedBox = session.getUiElements().perunLoadingFailedBox("Request timeout exceeded.");
				} else {
					if (error.getName().contains("UserNotExistsException")) {
						loadingFailedBox = session.getUiElements().perunLoadingFailedBox("You are not registered to any Virtual Organization.</br></br>" + error.getErrorInfo());
					} else {
						loadingFailedBox = session.getUiElements().perunLoadingFailedBox(error.getErrorInfo());						
					}
				}
				loadingFailedBox.show();
				
				leftMenu.addItem("Password reset", SmallIcons.INSTANCE.keyIcon(), null);
				
			}
		};	
		GetPerunPrincipal loggedUserRequest = new GetPerunPrincipal(events);
		loggedUserRequest.retrieveData();
	}

}