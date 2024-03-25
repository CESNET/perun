package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.*;

import java.util.ArrayList;

/**
 * Object definition for primitive types and lists of them.
 * Can return Int, String, Boolean, Float from itself.
 *
 * @author Vaclav Mach  <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class BasicOverlayType extends JavaScriptObject {

  protected BasicOverlayType() {
  }

  public final native boolean isNull() /*-{
		return this === null;
	}-*/;

  public final native String getString() /*-{
		return this.value;
	}-*/;

  public final native boolean getBoolean() /*-{
		return this.value;
	}-*/;

  public final native int getInt() /*-{
		return this.value;
	}-*/;

  public final native float getFloat() /*-{
		return this.value;
	}-*/;

  public final ArrayList<String> getListOfStrings() {

    JsArrayString array = this.cast();
    ArrayList<String> list = new ArrayList<String>();
    if (array != null) {
      for (int i = 0; i < array.length(); i++) {
        list.add(array.get(i));
      }
      return list;
    } else {
      return null;
    }
  }

  public final ArrayList<Integer> getListOfIntegers() {

    JsArrayInteger array = this.cast();
    ArrayList<Integer> list = new ArrayList<Integer>();
    if (array != null) {
      for (int i = 0; i < array.length(); i++) {
        list.add(array.get(i));
      }
      return list;
    } else {
      return null;
    }
  }

  public final ArrayList<Boolean> getListOfBooleans() {

    JsArrayBoolean array = this.cast();
    ArrayList<Boolean> list = new ArrayList<Boolean>();
    if (array != null) {
      for (int i = 0; i < array.length(); i++) {
        list.add(array.get(i));
      }
      return list;
    } else {
      return null;
    }
  }

  public final native String getCustomProperty(String property) /*-{
		if (!this[property]) return "";
		if (typeof this[property] === 'undefined') return "";
		if (this[property] === null) return "";
		return this[property];
	}-*/;

  public final native String getGDPRproperty(String property) /*-{
		if (property === "version") {
			return $wnd.GDPR_VERSION;
		}
		if (property === "text") {
			return $wnd.GDPR_TEXT;
		}
		if (property === "agree") {
			return $wnd.GDPR_AGREE;
		}
		if (property === "disagree") {
			return $wnd.GDPR_DISAGREE;
		}
		if (property === "disagree_result") {
			return $wnd.GDPR_DISAGREE_RESULT;
		}
		if (property === "title") {
			return $wnd.GDPR_TITLE;
		}
		if (property === "check_failed") {
			return $wnd.GDPR_FAILED;
		}
		if (property === "disagree_back") {
			return $wnd.GDPR_DISAGREE_RESULT_BACK;
		}
		return null;
	}-*/;

}
