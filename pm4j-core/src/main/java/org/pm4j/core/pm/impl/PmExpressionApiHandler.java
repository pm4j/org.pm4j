package org.pm4j.core.pm.impl;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;
import org.pm4j.navi.NaviHistoryNamedObjectResolver;

public class PmExpressionApiHandler {

  /**
   * @param expression
   *          expression for the object to find.
   * @return the found object or <code>null</code> when not found.
   */
  public Object findByExpression(PmObject pm, String expression) {
    if (StringUtils.isBlank(expression)) {
      throw new PmRuntimeException(pm, "'null' and blank property keys are not supported.");
    }

    PathResolver pr = PmExpressionPathResolver.parse(
                          expression,
                          PmExpressionApi.getSyntaxVersion(pm));
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
    PmConversation pmConversation = (PmConversationImpl)pm.getPmConversation();

    // TODO olaf: move that out as a very optional configuration part of the conversation or view connector...
    result = new NaviHistoryNamedObjectResolver(pmConversation).findObject(objName);

    if (result == null) {
      result = pmConversation.getPmNamedObject(objName);
    }

    return result;
  }


}
