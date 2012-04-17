package org.pm4j.core.pm;

import java.io.Serializable;
import java.util.List;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * A presentation model that supports a set of attributes.
 * <p>
 * Such models and their attributes are intended to be visualized in form displays.
 *
 * @author olaf boede
 */
public interface PmElement extends PmObject, PmDataInput, PmTreeNode {

  /**
   * @return The set of attributes.
   * @deprecated Please use {@link PmUtil#getPmAttributes(PmObject))}.
   */
  @Deprecated
  List<PmAttr<?>> getPmAttributes();

  /**
   * @param attrName
   *          Name of the requested attribute.
   * @return The matching attribute instance. Never <code>null</code>.
   * @throws PmRuntimeException
   *           when there is no matching attribute.
   * @deprecated Please use {@link PmUtil#getPmAttribute(PmObject, String)}.
   */
  @Deprecated
  PmAttr<?> getPmAttribute(String attrName);

  /**
   * @return A key that identifies the element within the session.
   */
  Serializable getPmKey();

}
