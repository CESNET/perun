package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.ConsentsManager;
import cz.metacentrum.perun.core.bl.ConsentsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;

/**
 * Consents entry logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentsManagerEntry implements ConsentsManager {

	private ConsentsManagerBl consentsManagerBl;
	private PerunBl perunBl;

	public ConsentsManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.consentsManagerBl = perunBl.getConsentsManagerBl();
	}

	public ConsentsManagerEntry() {}

	public ConsentsManagerBl getConsentsManagerBl() {
		return this.consentsManagerBl;
	}

	public void setConsentsManagerBl(ConsentsManagerBl consentsManagerBl) {
		this.consentsManagerBl = consentsManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}
}
