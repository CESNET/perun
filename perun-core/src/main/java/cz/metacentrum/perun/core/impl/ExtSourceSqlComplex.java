package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import java.util.List;
import java.util.Map;

/**
 * Complex SQL extSource is extended SQL extSource with ability to get all subjects with all needed attributes by one
 * query.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ExtSourceSqlComplex extends ExtSourceSql implements ExtSourceApi {

  @Override
  public List<Map<String, String>> findSubjects(String searchString)
      throws ExtSourceUnsupportedOperationException {
    return findSubjectsLogins(searchString);
  }
}
