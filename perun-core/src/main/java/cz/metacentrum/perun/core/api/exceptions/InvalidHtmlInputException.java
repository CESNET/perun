package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.impl.HTMLParser;

public class InvalidHtmlInputException extends WrongAttributeValueException{
	public InvalidHtmlInputException(String message) {
		super(message);
	}
	public InvalidHtmlInputException(String message, String[] unsafeTags) {
		super(message + " " + HTMLParser.getMessage(unsafeTags));
	}

	public InvalidHtmlInputException(String message, String rejectReason) {
		super(message + " " + rejectReason);
	}
}
