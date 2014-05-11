package cz.metacentrum.perun.notif.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for parsing methods
 *
 * @author tomas.tunkl
 *
 */
public class ParsedMethod {

	public enum MethodType {

		METHOD, STRING_PARAM, CLASS, INTEGER_PARAM
	}

	private String methodName;

	private MethodType methodType;

	private List<ParsedMethod> params = new ArrayList<ParsedMethod>();

	private ParsedMethod nextMethod;

	private int lastPosition;

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public MethodType getMethodType() {
		return methodType;
	}

	public void setMethodType(MethodType methodType) {
		this.methodType = methodType;
	}

	public ParsedMethod getNextMethod() {
		return nextMethod;
	}

	public void setNextMethod(ParsedMethod nextMethod) {
		this.nextMethod = nextMethod;
	}

	public List<ParsedMethod> getParams() {
		return params;
	}

	public void setParams(List<ParsedMethod> params) {
		this.params = params;
	}

	public void addParam(ParsedMethod method) {
		params.add(method);
	}

	public int getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(int lastPosition) {
		this.lastPosition = lastPosition;
	}
}
