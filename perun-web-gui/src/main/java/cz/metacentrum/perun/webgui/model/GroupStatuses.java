package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;

public class GroupStatuses extends JavaScriptObject {

  protected GroupStatuses() {
  }

  /**
   * Return IDS of all groups which are responsible for members resulting group membership status.
   */
  public final native JsArrayInteger keySet() /*-{
		var arr = [];
		var index = 0;
		for (var key in this) {
			if (this.hasOwnProperty(key)) {
				arr[index] = key;
				index = index+1;
			}
		}
		return arr;
	}-*/;

  /**
   * Return Group Membership status for specified group
   */
  public final native String getStatus(int groupId) /*-{
		if (this.hasOwnProperty(groupId)) {
			return this.groupId;
		}
		return null
	}-*/;

}
