/**
 * 
 */
package cz.metacentrum.perun.core.api;

/**
 * Class containing user's authentication credentials, e.g. SSH key, PGP, X.508 certificate, ...
 * 
 * @author Michal Prochazka michalp@ics.muni.cz
 *
 */
public class MemberCredential {
	private int id;
	// TODO postupne dodelat


	public MemberCredential() {

	}

	public MemberCredential(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String toString() {
		return getClass().getSimpleName() + ":[id='" + id + "']";
	}
}
