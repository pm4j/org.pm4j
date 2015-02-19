package org.pm4j.core.pm.impl.options;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmOptionSet;


/**
 * Interface for algorithms that provide attribute value options.
 * <p>
 * TODO olaf: document the implicit specification protocol:
 * - default ctor
 * - public static instance() method or INSTANCE field
 *
 * @author olaf boede
 */
public interface PmOptionSetDef<T_ATTR extends PmAttr<?>> {

  /**
   * Generates the options for the attribute.
   *
   * @param forAttr
   *          The attribute to generate the options for.
   * @return The options or <code>null</code> when the attribute does not
   *         support options at all.
   */
  PmOptionSet makeOptions(T_ATTR forAttr);

  /**
   * @param forAttr
   *          The attribute to generate the <code>null</code>-option title for.
   * @return The title to display for the option that represents the <code>null</code> selection.
   */
  String getNullOptionTitle(T_ATTR forAttr);
}
