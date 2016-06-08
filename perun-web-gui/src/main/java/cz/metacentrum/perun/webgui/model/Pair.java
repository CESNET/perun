package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class Pair<T,E> extends JavaScriptObject {

	protected Pair() {
	}

	public final native T getLeft() /*-{
		return this.left;
	}-*/;

	public final native E getRight() /*-{
		return this.right;
	}-*/;

}
