package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmLabel;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmVisitor;

/**
 * A label implementation.
 *
 * @author olaf boede
 */
public class PmLabelImpl extends PmObjectBase implements PmLabel {

  /**
   * @param pmParentElement
   *          The parent element or session context to get some data like
   *          the current language.<br>
   *          It also provides the resource loading context.
   */
  public PmLabelImpl(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  public void clearPmInvalidValues() {
    // nothing to do.
  }

  @Override
  public void accept(PmVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  protected MetaData makeMetaData() {
    return new MetaData();
  }
}
