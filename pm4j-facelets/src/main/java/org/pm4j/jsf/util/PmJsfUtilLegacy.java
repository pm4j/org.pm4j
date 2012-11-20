package org.pm4j.jsf.util;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.navi.impl.NaviRuntimeException;

/**
 * Some very project specific methods are moved to this class.<br>
 * It will be removed as soon as these definitions are moved to the related project.
 */
@Deprecated
public class PmJsfUtilLegacy {
  /**
   * Provides a PM property value.
   *
   * @param pm
   *          The PM context object to get the property from.
   * @param key
   *          The property key.
   * @return The found value or <code>null</code>.
   * @deprecated Expressions can be handled within PMs. There should be need to express them in view code.
   */
  @Deprecated
  public static Object findPmProperty(PmObject pm, String key) {
    return PmExpressionApi.findByExpression(pm, key);
  }

  /**
   * Provides a PM property value.
   *
   * @param pm
   *          The PM context object to get the property from.
   * @param key
   *          The property key.
   * @return The found value.
   * @throws PmRuntimeException
   *           when no value exists for the given key.
   * @deprecated Expressions can be handled within PMs. There should be need to express them in view code.
   */
  @Deprecated
  public static Object getPmProperty(PmObject pm, String key) {
    return PmExpressionApi.getByExpression(pm, key);
  }

  /**
   * Sets a session context property value.
   *
   * @param pmConversation
   *          The session context that stores the parameter value.
   * @param key
   *          Name of the property to set.
   * @param value
   *          The new property value.
   * @return The value of the session context property.
   * @deprecated Expressions can be handled within PMs. There should be need to express them in view code.
   */
  @Deprecated
  public static Object setPmProperty(PmConversation pmConversation, String key, Object value) {
    if (pmConversation == null) {
      throw new NaviRuntimeException("Parameter 'pmConversation' should not be null.");
    }
    if (StringUtils.isBlank(key)) {
      throw new NaviRuntimeException("Parameter 'key' should not be empty or null.");
    }

    pmConversation.setPmNamedObject(key, value);

    return value;
  }

}
