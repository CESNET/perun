package cz.metacentrum.perun.notif.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import java.util.List;

/**
 * Exception is thrown when regex is tried to remove but is still referenced
 * from templates User: tomastunkl Date: 21.10.12 Time: 15:14
 */
public class PerunNotifRegexUsedException extends PerunException {

	private List<Integer> referencedTemplateIds;

	public PerunNotifRegexUsedException(List<Integer> referencedTemplateIds) {
		super();
		this.referencedTemplateIds = referencedTemplateIds;
	}

	public PerunNotifRegexUsedException(String message, List<Integer> referencedTemplateIds) {
		super(message);
		this.referencedTemplateIds = referencedTemplateIds;
	}

	public List<Integer> getReferencedTemplateIds() {
		return referencedTemplateIds;
	}
}
