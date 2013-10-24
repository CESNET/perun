package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.userstabs.*;

import java.util.Map;

/**
 * Users
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class UsersTabs {

	private PerunWebSession session = PerunWebSession.getInstance();
	
	static public final String URL = "usr";
	
	/**
	 * Creates a new instance of pages
     */
	public UsersTabs(){}

	/**
	 * Loads the page
	 * 
	 * @return true on success / false otherwise
	 */
	public boolean loadTab(final String tab, final Map<String, String> parameters) {
	
		if(tab == null){
			return false;				
		}
		// if active
		boolean open = ("1".equals(parameters.get("active")));
					
				
		if (tab.equals(UserDetailTabItem.URL)) {
			session.getTabManager().addTab(UserDetailTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(SelfDetailTabItem.URL)) {
			session.getTabManager().addTab(SelfDetailTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(AddUserExtSourceTabItem.URL)) {
			session.getTabManager().addTab(AddUserExtSourceTabItem.load(parameters), open);
			return true;
		}
		
		
		if (tab.equals(SelfSettingsTabItem.URL)) {
			session.getTabManager().addTab(SelfSettingsTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(IdentitySelectorTabItem.URL)) {
			session.getTabManager().addTab(IdentitySelectorTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(UsersTabItem.URL)) {
			session.getTabManager().addTab(UsersTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(SelfPasswordTabItem.URL)) {
			session.getTabManager().addTab(SelfPasswordTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(SelfApplicationsTabItem.URL)) {
			session.getTabManager().addTab(SelfApplicationsTabItem.load(parameters), open);
			return true;
		}
		
		if (tab.equals(SelfApplicationDetailTabItem.URL)) {
			session.getTabManager().addTab(SelfApplicationDetailTabItem.load(parameters), open);
			return true;
		}

        if (tab.equals(SelfServiceUsersTabItem.URL)) {
            session.getTabManager().addTab(SelfServiceUsersTabItem.load(parameters), open);
            return true;
        }
		
		return false;
		
		
		
	}
	
}