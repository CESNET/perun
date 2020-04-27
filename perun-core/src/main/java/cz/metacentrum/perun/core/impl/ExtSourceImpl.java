package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;

import java.util.Map;

/**
 * Common ancestor of ExtSource implementations.
 */
public abstract class ExtSourceImpl extends ExtSource {

	protected PerunBl perunBl;

	void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	private Map<String,String> extSourceAttributes;

	protected Map<String,String> getAttributes() throws InternalErrorException {
		if (extSourceAttributes == null) {
			extSourceAttributes = perunBl.getExtSourcesManagerBl().getAttributes(this);
		}
		return extSourceAttributes;
	}
}
