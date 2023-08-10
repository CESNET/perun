package cz.metacentrum.perun.registrar.modules;

/**
 * Module for VO lifescience_hostel on LifeScience acceptance Perun machine
 *
 * On approval create UES with LS Hostel identity and add user to the lifescience VO directly.
 *
 * @author Pavel Vyskocil <Pavel.Vyskocil@cesnet.cz>
 */
public class LifeScienceHostelRIAcc extends AbstractLifeScienceHostelRI {

	private final static String HOSTEL_HOSTNAME = "hostel.acc.aai.lifescience-ri.eu";

	private final static String LS_HOSTEL_SCOPE = "@" + HOSTEL_HOSTNAME;

	private final static String LS_HOSTEL_EXT_SOURCE_NAME = "https://" + HOSTEL_HOSTNAME + "/lshostel/";

	@Override
	protected String getScope() {
		return LS_HOSTEL_SCOPE;
	}

	@Override
	protected String getExtSourceName() {
		return LS_HOSTEL_EXT_SOURCE_NAME;
	}

}

