
package cz.metacentrum.perun.webgui.json;


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PerunRequest;

import java.util.Date;

/**
 * Class with a client for JSON calls. For each call a new instance must be created.
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class GetPendingRequests implements JsonCallback{

	// JSON URL
	public static final String JSON_URL = "getPendingRequests";

	// default interval for JSON calls
	public final static int DEFAULT_INTERVAL = ((PerunWebConstants) GWT.create(PerunWebConstants.class)).pendingRequestsRefreshInterval();

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	
	// panel with requests
	private VerticalPanel panelWithRequests = new VerticalPanel();
	
	// widget
	private FlexTable widget = new FlexTable();
	
	// requests running?
	private boolean requestsRefreshing;
	
	
	public GetPendingRequests(){
		
		// set refreshing
		this.setRequestsRefreshing(false); // true to start pending requests
		
		
		final ToggleButton startStopButton = new ToggleButton("Pause requests feed", "Resume requests feed");
		startStopButton.setDown(!isRequestsRefreshing());
		startStopButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				setRequestsRefreshing(!startStopButton.isDown());
			}
		});
		
		
		widget.setWidget(0, 0, new HTML("<strong>Pending requests</strong>"));
		widget.setWidget(1, 0, panelWithRequests);
		widget.setWidget(2, 0, startStopButton);
	}
	
	public void setRequestsRefreshing(boolean requestsRefreshing)
	{
		if(requestsRefreshing && !this.requestsRefreshing){
			Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
				public boolean execute() {
					retrieveData();
					return isRequestsRefreshing();
				}
			}, DEFAULT_INTERVAL);
		}
		
		this.requestsRefreshing = requestsRefreshing;
	}
	
	protected boolean isRequestsRefreshing(){
		return requestsRefreshing;
	}
	

	public void retrieveData()
	{
		JsonClient client = new JsonClient();
		client.setSilent(true);
		client.retrieveData(JSON_URL, this);	
	}
	
	
	private void updateTable(JsArray<PerunRequest> requests){
		panelWithRequests.clear();
		
		if(requests.length() == 0){
			Widget label = new Label("No requests");
			panelWithRequests.add(label);
		}
		
		for(int i = 0; i< requests.length(); i++){
			PerunRequest req = requests.get(i);
			
			FlexTable ft = new FlexTable();
			
			// MAIN INFO
			HTML a = new HTML("<strong>" + req.getManager() + "</strong>");
			HTML b = new HTML("<strong>" + req.getMethod() + "</strong>");
			
			long startTime = ((long) req.getStartTime());
			long currentTime = new Date().getTime();
			long elapsedTime = currentTime - startTime;
			int elapsedSeconds = (int) (elapsedTime / 1000);
					
			HTML c = new HTML(elapsedSeconds + "s");

			ft.setWidget(0, 0, a);
			ft.setWidget(1, 0, b);
			ft.setWidget(2, 0, c);
			
			
			// PARAMETERS
			ft.setTitle(req.getParamsString());

			panelWithRequests.add(ft);
			
		}
	}
	
	public Widget getWidget()
	{
		return widget;
	}


	public void onFinished(JavaScriptObject jso) {
		JsArray<PerunRequest> reqs = JsonUtils.jsoAsArray(jso);
		updateTable(reqs);
		session.getUiElements().setLogText("Active requests retrieved - count: " + reqs.length());
	}


	public void onError(PerunError error) {		
	}


	public void onLoadingStart() {
	}
}





