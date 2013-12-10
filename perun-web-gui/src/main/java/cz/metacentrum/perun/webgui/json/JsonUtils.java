package cz.metacentrum.perun.webgui.json;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.SplitResult;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableTextCell;

import java.util.*;

/**
 * Utility functions for handling JSON requests, arrays, building tables etc.
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 17237cc31fe9d79e96ca64551a2d13ef8f8c5ee1 $
 */
public class JsonUtils {
	
	/**
	 * Whether is extended info shown
	 */
	static private boolean extendedInfoVisible = false;
	
	/**
	 * Returns a column with a header.
	 * 
	 * @param <R> the row type
	 * @param <R> the cell type
	 * @param cell the cell used to render the column
	 * @param headerText the header string
	 * @param getValue the value getter for the cell
	 * @deprecated Shouldn't be used when returning the same object - use the method without Getter in parameter instead	 * 
	 */
	static public <R> Column<R, R> addColumn(Cell<R> cell,
			String headerText, final GetValue<R, R> getValue,
			final FieldUpdater<R, String> tableFieldUpdater, boolean custom) {
		Column<R, R> column = new Column<R, R>(cell) {
			@Override
			public R getValue(R object) {
				return object;
			}
            @Override
            public String getCellStyleNames(Cell.Context context, R object) {

                if (tableFieldUpdater != null) {
                    return super.getCellStyleNames(context, object) + " pointer";
                } else {
                    return super.getCellStyleNames(context, object);
                }
            }
		};
		
		if(tableFieldUpdater != null){
	
			 FieldUpdater<R, R> tableFieldUpdater2 = new FieldUpdater<R, R>() {
						public void update(int index, R object,
								R value) {
							GeneralObject go = (GeneralObject) value;
							tableFieldUpdater.update(index, object, go.getName());
						}
						
					};
					
			column.setFieldUpdater(tableFieldUpdater2);
		}
		return column;
	}
	
	/**
	 * Returns a column with a header.
	 * 
	 * @param <R> the row type
	 * @param <C> the cell type
	 * @param cell the cell used to render the column
	 * @param headerText the header string
	 * @param getter the value getter for the cell
	 * @deprecated Shouldn't be used when returning string - use the method without Cell in parameter instead or just use method without header
	 */
	static public <R, C> Column<R, C> addColumn(Cell<C> cell, String headerText, final GetValue<R, C> getter, final FieldUpdater<R, C> fieldUpdater) {
		Column<R, C> column = new Column<R, C>(cell) {
			@Override
			public C getValue(R object) {
				return getter.getValue(object);
			}
            @Override
            public String getCellStyleNames(Cell.Context context, R object) {

                if (fieldUpdater != null) {
                    return super.getCellStyleNames(context, object) + " pointer";
                } else {
                    return super.getCellStyleNames(context, object);
                }
            }
		};
		if(fieldUpdater != null){
			column.setFieldUpdater(fieldUpdater);
		}
		return column;
	}
	
	/**
	 * Returns a column with a custom Cell
	 * 
	 * @param <R> the row type
	 * @param <C> the cell type
	 * @param cell the cell used to render the column
	 * @param getter the value getter for the cell
	 */
	static public <R, C> Column<R, C> addColumn(Cell<C> cell, final GetValue<R, C> getter, final FieldUpdater<R, C> fieldUpdater) {
		Column<R, C> column = new Column<R, C>(cell) {
			@Override
			public C getValue(R object) {
				return getter.getValue(object);
			}
            @Override
            public String getCellStyleNames(Cell.Context context, R object) {

                if (fieldUpdater != null) {
                    return super.getCellStyleNames(context, object) + " pointer";
                } else {
                    return super.getCellStyleNames(context, object);
                }
            }
		};
		if(fieldUpdater != null){
			column.setFieldUpdater(fieldUpdater);
		}
		return column;
	}
	
	/**
	 * Returns a column with Object as value which is rendered by custom Cell
	 * 
	 * @param <R> the row type
	 * @param cell the cell used to render the column
	 * @param fieldUpdater Field updater - on click action
	 */
	static public <R> Column<R, R> addColumn(Cell<R> cell, final FieldUpdater<R, R> fieldUpdater) {
		Column<R, R> column = new Column<R, R>(cell) {
			@Override
			public R getValue(R object) {
				return object;
			}
            @Override
            public String getCellStyleNames(Cell.Context context, R object) {

                if (fieldUpdater != null) {
                    return super.getCellStyleNames(context, object) + " pointer";
                } else {
                    return super.getCellStyleNames(context, object);
                }
            }
		};
		if(fieldUpdater != null){
			column.setFieldUpdater(fieldUpdater);
		}
		return column;
	}
	
	/**
	 * Returns a column with Object as value which is rendered by custom Cell
	 * 
	 * @param <R> the row type
	 * @param cell the cell used to render the column
	 * @param fieldUpdater Field updater - on click action
	 */
	static public <R> Column<R, R> addCustomCellColumn(Cell<R> cell, final FieldUpdater<R, String> fieldUpdater) {
		Column<R, R> column = new Column<R, R>(cell) {
			@Override
			public R getValue(R object) {
				return object;
			}
            @Override
            public String getCellStyleNames(Cell.Context context, R object) {

                if (fieldUpdater != null) {
                    return super.getCellStyleNames(context, object) + " pointer";
                } else {
                    return super.getCellStyleNames(context, object);
                }
            }
		};
		if(fieldUpdater != null){
			 FieldUpdater<R, R> tableFieldUpdater2 = new FieldUpdater<R, R>() {
						public void update(int index, R object,
								R value) {
							GeneralObject go = (GeneralObject) value;
							fieldUpdater.update(index, object, go.getName());
						}
						
					};
			column.setFieldUpdater(tableFieldUpdater2);
		}
		return column;
	}
	
	/**
	 * Returns a column with Object as value which is rendered by custom Cell
	 * 
	 * @param <R> the row type
	 * @param cell the cell used to render the column
	 */
	static public <R> Column<R, R> addColumn(Cell<R> cell) {
		return addColumn(cell, null);
	}
	
	
	/**
	 * Returns a column with a header and with automatic cell selection
	 * 
	 * @param <R> the row type
	 * @param getter the value getter for the cell
	 * @param fieldUpdater Field updater - on click action
	 */
	static public <R> Column<R, String> addColumn(final GetValue<R, String> getter, final FieldUpdater<R, String> fieldUpdater) {
		
		Cell<String> cell;
		
		if(fieldUpdater == null){
			cell = new TextCell();
		}else{
			cell = new CustomClickableTextCell();
		}
		
		Column<R, String> column = new Column<R, String>(cell) {
			@Override
			public String getValue(R object) {
				return getter.getValue(object);
			}
            @Override
            public String getCellStyleNames(Cell.Context context, R object) {

                if (fieldUpdater != null) {
                    return super.getCellStyleNames(context, object) + " pointer";
                } else {
                    return super.getCellStyleNames(context, object);
                }
            }
        };
		if(fieldUpdater != null){
			column.setFieldUpdater(fieldUpdater);
		}
		return column;
	}


    /**
     * Returns JS Array made from returned text (JSON)
     *
     * @param json text to deserialize
     * @return JSArray<T> array of objects to return
     */
    public static final native <T extends JavaScriptObject> JsArray<T> jsonAsArray(String json) /*-{
        return eval(json);
    }-*/;


    /**
     * Takes a string and parses it
     * If not in JSON format, it creates a BasicOverlayType
     *
     * @param data that you trust
     * @return JavaScriptObject that you can cast to an Overlay Type
     */
    public static native JavaScriptObject parseJson(String data) /*-{
        var response = $wnd.jQuery.parseJSON(data);
        if(typeof response === 'object') {
            return response;
        }
        return {"value":data};
    }-*/;

    /**
     * Returns JS Array made from returned JSON (javascript) object
     *
     * @param jso Unknown javascript object
     * @return JSArray<UnknownObject> array of Unknown javascript objects
     */
    public static native <T extends JavaScriptObject> JsArray<T> jsoAsArray(JavaScriptObject jso) /*-{
        return jso;
    }-*/;


    /**
     * Returns passed unknown javascript object as ArrayList<T>
     *
     * @param jso Unknown javascript object
     * @return ArrayList<UnknownObject> list of unknown objects
     */
    public static <T extends JavaScriptObject> ArrayList<T> jsoAsList(JavaScriptObject jso) {

        JsArray<T> arr = jsoAsArray(jso);
        ArrayList<T> l = new ArrayList<T>();
        for (int i = 0; i < arr.length(); i++) {
            l.add(arr.get(i));
        }
        return l;

    }

    /**
	 * Get a cell value from a record.
	 * 
	 * @param <C> the cell type
	 */
	static public interface GetValue<R, C> {
		C getValue(R object);
	}

    static public HandlerRegistration addTableManagedButton(final JsonCallbackTable<? extends JavaScriptObject> callback, final CellTable<? extends JavaScriptObject> table, final CustomButton cb) {

        return table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                if (callback.getTableSelectedList().size() > 0) {
                    cb.setEnabled(true);
                } else {
                    cb.setEnabled(false);
                }
            }
        });

    }

	/**
	 * Returns a Java List from JsArrayString.
	 * 
	 * @param jsa javascript array of strings
	 * @return ArrayList<String> list of strings
	 */
	static public ArrayList<String> listFromJsArrayString(JsArrayString jsa) {
		ArrayList<String> arrayList = new ArrayList<String>();
		for (int i = 0; i < jsa.length(); i++) {
			String str = jsa.get(i).toString();
			arrayList.add(str);
		}
		return arrayList;
	}

	/**
	 * Returns true if the JS object is an array
	 * 
	 * @param jso javscript object to check
	 * @return TRUE if JS object is an array / FALSE otherwise
	 */
	static public native boolean isJsArray(JavaScriptObject jso) /*-{
																	
		return !(jso.constructor.toString().indexOf("Array") == -1);
																	
	}-*/;

	/**
	 * Returns the formatted pager, with the edit box.
	 * 
	 * @param session Perun Web Session
	 * @param pager SimplePager
	 * @return
	 */
	static public Widget getFormattedPager(final PerunWebSession session, final SimplePager pager) {
		// when next, remove previous query
		
		// UI
		HorizontalPanel pagerPanel = new HorizontalPanel();
		pagerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		pagerPanel.setSpacing(2);
		pagerPanel.add(pager);
		pagerPanel.setHeight("50px");

		// On page label
		Label onPageLabel = new Label("On page");

		// add the input for pages
		final TextBox onPageTextBox = new TextBox();
		onPageTextBox.setWidth("30px");
		onPageTextBox.setText(String.valueOf(pager.getPageSize()));

		// search box on enter
		onPageTextBox.addKeyPressHandler(new KeyPressHandler() {
			private void parseNumber() {
				try {
					int pageSize = Integer.parseInt(onPageTextBox.getText());
					JsonRpcSourceData.setDefaultPageSize(pageSize);
					pager.setPageSize(pageSize);
				} catch (Exception e) {
					session.getUiElements().setLogErrorText("Not a number.");
				}
			}

			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					parseNumber();
				}
			}
		});

		Button onPageButton = new Button("Set");
		onPageButton.addClickHandler(new ClickHandler() {
			private void parseNumber() {
				try {
					int pageSize = Integer.parseInt(onPageTextBox.getText());
					JsonRpcSourceData.setDefaultPageSize(pageSize);
					pager.setPageSize(pageSize);
				} catch (Exception e) {
					session.getUiElements().setLogErrorText("Not a number.");
				}
			}

			public void onClick(ClickEvent event) {
				parseNumber();
			}
		});

		pagerPanel.add(onPageLabel);
		pagerPanel.add(onPageTextBox);
		pagerPanel.add(onPageButton);

		return pagerPanel;
	}

	/**
	 * Show / hide advanced info in tables
	 * @return
	 */
	public static void toggleExtendedInfo(){
		extendedInfoVisible = !extendedInfoVisible;
		setExtendedInfoVisible(extendedInfoVisible);
	}
	
	/**
	 * Show / hide advanced info in tables.
	 * @param show Whether to show extended info.
	 * @return
	 */
	public static void setExtendedInfoVisible(boolean show){
		extendedInfoVisible = show;		
	}
	
	/**
	 * Whether should be extended info visible
	 * @return
	 */
	public static boolean isExtendedInfoVisible()
	{
		return extendedInfoVisible;
	}
	
	
	/**
	 * Returns a set as a list
	 * @param set Input set
	 * @return Output list 
	 */
	static public <T> ArrayList<T> setToList(Set<T> set){
		ArrayList<T> list = new ArrayList<T>();
		for (T attr : set) {
			list.add(attr);
		}
		return list;
	}
	
	/**
	 * Check if String input can be parsed as Integer
	 * 
	 * @param value String input from some text box
	 * @return true if input is number (integer), false otherwise
	 */
	static final public native boolean checkParseInt(String value)/*-{
	
		// true on any number format, false otherwise
			if (!isNaN(parseFloat(value)) && isFinite(value)) {   
				return true;
			} else {
				return false;
			}

	}-*/;
	
	/**
	 * Provides popup notification 
	 * 
	 * @param origin (you can specified origin of value (name of check box) 
	 * @param value value that can't be parsed
	 */
	static final public void cantParseIntConfirm(String origin, String value) {
		
		FlexTable ft = new FlexTable();
		ft.setHTML(0, 0, "Value \""+value+"\" can't be parsed as number." );
		if (origin.length() > 0) {
			ft.setHTML(1, 0, "Input name: "+origin);
		}
		
		Confirm conf = new Confirm("Can't parse value", ft, true);
		conf.setNonScrollable(true);
		conf.show();
		
	}
	
	/**
	 * Parses a JavaScript map into a Java map.
	 * @param str
	 * @return
	 */
	static final public Map<String, JSONValue> parseJsonToMap(String str)
	{
		return parseJsonToMap(parseJson(str));
	}
	
	
	/**
	 * Parses a JavaScript map into a Java map.
	 * @param jso
	 * @return
	 */
	static final public Map<String, JSONValue> parseJsonToMap(JavaScriptObject jso)
	{
		if (jso == null) {
			return new HashMap<String, JSONValue>();   // when null, return empty map
		}
		
		JSONObject obj = new JSONObject(jso);
		
		HashMap<String, JSONValue> m = new HashMap<String, JSONValue>();
		for (String key : obj.keySet()) {
			m.put(key, obj.get(key));
		}
		return m;
		
	}
	
	/**
	 * Clones the JavaScriptObject
	 * @param obj
	 */
	public static native final JavaScriptObject clone(JavaScriptObject obj)/*-{
		return $wnd.jQuery.extend(true, {}, obj);
	}-*/;


    /**
	 * Replacement for String.format in Java
	 * @param format Source format
	 */
	public static String stringFormat(final String format, final Object... args) {
		final RegExp regex = RegExp.compile("%[a-z]");
		final SplitResult split = regex.split(format);
		final StringBuffer msg = new StringBuffer();
		for (int pos = 0; pos < split.length() - 1; pos += 1) {
			msg.append(split.get(pos));
			msg.append(args[pos].toString());
		}
		msg.append(split.get(split.length() - 1));
		return msg.toString();
	}
	
	
	/**
	 * Validates an e-mail address
	 * @param email
	 * @return
	 */
	public native static boolean isValidEmail(String email) /*-{
    	//var reg1 = /(@.*@)|(\.\.)|(@\.)|(\.@)|(^\.)/; // not valid
   		//var reg2 = /^.+\@(\[?)[a-zA-Z0-9\-\.]+\.([a-zA-Z]{2,3}|[0-9]{1,3})(\]?)$/; // valid
    	//return !reg1.test(email) && reg2.test(email);
        var reg4 = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        return reg4.test(email);
	}-*/;
	
	/**
	 * Joins array of strings to string
	 * 
	 * @param s
	 * @param delimiter
	 * @return
	 */
	public static String join(Iterable<String> s, String delimiter) {
	    if (s == null) return "";
	    Iterator<String> iter = s.iterator();
	    StringBuilder builder = new StringBuilder(iter.next());
	    while( iter.hasNext() )
	    {
	        builder.append(delimiter).append(iter.next());
	    }
	    return builder.toString();
	}
	
	
	/**
	 * Whether string contains IDs separated by a comma
	 * 
	 * @param text
	 * @return
	 */
	static public boolean isStringWithIds(String text)
	{
		final String IDS_PATTERN = "^[0-9, ]+$";
		RegExp regExp = RegExp.compile(IDS_PATTERN);
		MatchResult matcher = regExp.exec(text);
		return (matcher != null);	
	}

    /**
     * Return current year as Integer
     * @return current year
     */
    public static final native int getCurrentYear()  /*-{
        return new Date().getFullYear()
    }-*/;


    /**
     * Returns list of attribute URNs (strings)
     * They are used to get members of group/vo with specific attributes
     *
     * @return list of URNs
     */
    public static ArrayList<String> getAttributesListForMemberTables() {

        ArrayList<String> attributes = new ArrayList<String>();
        for (String a : PerunWebConstants.INSTANCE.getAttributesListForMemberTables()) {
            attributes.add(a);
        }
        return attributes;

    }

    /**
     * Returns list of attribute URNs (strings)
     * They are used to get users of group/vo admins etc. with specific attributes
     *
     * @return list of URNs
     */
    public static ArrayList<String> getAttributesListForUserTables() {

        ArrayList<String> attributes = new ArrayList<String>();
        for (String a : PerunWebConstants.INSTANCE.getAttributesListForUserTables()) {
            attributes.add(a);
        }
        return attributes;

    }

}