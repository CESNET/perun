/**
 *
 */

package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy ExtSource - X.508
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 */
public class ExtSourceX509 extends ExtSourceImpl implements ExtSourceSimpleApi {

  private static final Logger LOG = LoggerFactory.getLogger(ExtSourceX509.class);

  @Override
  public void close() throws ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }

  @Override
  public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults)
      throws ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }

  @Override
  public List<Map<String, String>> findSubjectsLogins(String searchString)
      throws ExtSourceUnsupportedOperationException {
    return findSubjectsLogins(searchString, 0);
  }

  @Override
  public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes)
      throws ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }

  @Override
  public Map<String, String> getSubjectByLogin(String login) throws ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }

  @Override
  public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes)
      throws ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }

  @Override
  public List<Map<String, String>> getUsersSubjects() throws ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }
}
