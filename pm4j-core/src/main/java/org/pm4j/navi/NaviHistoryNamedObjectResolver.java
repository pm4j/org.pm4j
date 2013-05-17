package org.pm4j.navi;

import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.connector.NamedObjectResolver;
import org.pm4j.core.pm.impl.connector.PmToViewTechnologyConnector;

/**
 * Provides a named object that was stored within the navigation context.
 *
 * @author olaf boede
 */
public class NaviHistoryNamedObjectResolver implements NamedObjectResolver {

  private PmConversationImpl pmConversation;

  public NaviHistoryNamedObjectResolver(PmConversation pmConversation) {
    assert pmConversation != null;

    this.pmConversation = (PmConversationImpl) pmConversation;
  }

  @Override
  public Object findObject(String name) {
    Object result = null;
    PmToViewTechnologyConnector viewTechnologyConnector = pmConversation.getPmToViewTechnologyConnector();
    NaviHistory h = viewTechnologyConnector.getNaviHistory();

    if (h != null) {
      result = h.getNaviScopeProperty(name);

      if (result == null) {
        result = h.getConversationProperty(name);
      }
    }

    return result;
  }

}
