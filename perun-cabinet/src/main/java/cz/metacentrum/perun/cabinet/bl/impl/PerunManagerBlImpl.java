package cz.metacentrum.perun.cabinet.bl.impl;

import cz.metacentrum.perun.cabinet.bl.PerunManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class which provides connection to the rest of Perun.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class PerunManagerBlImpl implements PerunManagerBl {

  private static final String ATTR_COEF_TYPE = "java.lang.String";
  private static final String ATTR_COEF_NAMESPACE = "urn:perun:user:attribute-def:def";
  private static final String ATTR_COEF_FRIENDLY_NAME = "priorityCoeficient";
  private static final String ATTR_COEF_DESCRIPTION = "Priority coefficient based on user's publications.";
  private static final String ATTR_COEF_DISPLAY_NAME = "Priority coefficient";

  private static final String ATTR_PUBS_TYPE = "java.util.LinkedHashMap";
  private static final String ATTR_PUBS_NAMESPACE = "urn:perun:user:attribute-def:def";
  private static final String ATTR_PUBS_FRIENDLY_NAME = "publications";
  private static final String ATTR_PUBS_DESCRIPTION = "Number of acknowledgements per resource provider.";
  private static final String ATTR_PUBS_DISPLAY_NAME = "Publications";

  // debug vars, delete it
  public static int CACHE_INVOKED_TOTAL_COUNT = 0;
  public static int CACHE_MISS_TOTAL_COUNT = 0;

  @Autowired
  private PerunBl perun;

  private Logger log = LoggerFactory.getLogger(getClass());


  // methods ------------------------


}
