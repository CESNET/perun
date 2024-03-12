package cz.metacentrum.perun.registrar.modules;

/**
 * Module for VO lifescience_hostel on LifeScience acceptance Perun machine
 * <p>
 * On approval create UES with LS Hostel identity and add user to the lifescience VO directly.
 *
 * @author Pavel Vyskocil <Pavel.Vyskocil@cesnet.cz>
 */
public class LifeScienceHostelRIAcc extends AbstractLifeScienceHostelRI {

  private static final String HOSTEL_HOSTNAME = "hostel.acc.aai.lifescience-ri.eu";

  private static final String LS_HOSTEL_SCOPE = "@" + HOSTEL_HOSTNAME;

  private static final String LS_HOSTEL_EXT_SOURCE_NAME = "https://" + HOSTEL_HOSTNAME + "/lshostel/";

  @Override
  protected String getExtSourceName() {
    return LS_HOSTEL_EXT_SOURCE_NAME;
  }

  @Override
  protected String getScope() {
    return LS_HOSTEL_SCOPE;
  }

}

