package org.pm4j.core.pm.impl;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.connector.PmToViewTechnologyConnector;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;
import org.pm4j.navi.NaviHistory;

public class PmExpressionApiHandler {

  /**
   * Finds a named property from the following scopes (if available):
   * <ol>
   * <li>navigation scope</li>
   * <li>conversation scope</li>
   * <li>@link {@link PmConversation} property</li>
   * <li>http-request and session properties</li>
   * <li>application configuration property (e.g. Spring)</li>
   * </ol>
   *
   * @param name
   *          Name of the property to find.
   * @return The found property value or <code>null</code> when not found.
   */
  public Object findByExpression(PmObject pm, String name) {
    if (StringUtils.isBlank(name)) {
      throw new PmRuntimeException(pm, "'null' and blank property keys are not supported.");
    }

    PathResolver pr = PmExpressionPathResolver.parse(
                          name,
                          true /* allow that the first expression part addresses an attribute of the given pm. */ );
    return pr.getValue(pm);
  }

  /**
   * Finds the object. Tries to resolve not found objects using
   * {@link #handleNamedPmObjectNotFound(String)}.
   */
  public Object findNamedObject(PmObject pm, String objName) {
    Object result = _findNamedObjectImpl(pm, objName);

    if (result == null) {
      // prevent double initialization:
      synchronized(PmUtil.getRootSession(pm)) {
        result = _findNamedObjectImpl(pm, objName);
        if (result == null) {
          PmConversationImpl s = (PmConversationImpl)pm.getPmConversation();
          s.handleNamedPmObjectNotFound(objName);
          result = _findNamedObjectImpl(pm, objName);
        }
      }
    }

    return result;
  }

  /**
   * Finds an object within the named object scopes of the application.
   *
   * @param objName Name of the object to find.
   * @return The found instance of <code>null</code>.
   */
  protected static Object _findNamedObjectImpl(PmObject pm, String objName) {
    Object result = null;
    PmConversationImpl pmConversation = (PmConversationImpl)pm.getPmConversation();
    PmToViewTechnologyConnector viewTechnologyConnector = pmConversation.getViewConnector();
    NaviHistory h = viewTechnologyConnector.getNaviHistory();

    if (h != null) {
      result = h.getNaviScopeProperty(objName);

      if (result == null) {
        result = h.getConversationProperty(objName);
      }
    }

    if (result == null) {
      result = pmConversation.getPmNamedObject(objName);
      if (result == null) {
        result = viewTechnologyConnector.findNamedObject(objName);
      }
    }

    return result;
  }


}
