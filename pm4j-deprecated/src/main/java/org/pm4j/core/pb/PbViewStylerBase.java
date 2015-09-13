package org.pm4j.core.pb;

import org.pm4j.core.pm.PmObject;

/**
 * Base class with type safe apply-style 
 *
 * @param <VIEW>
 * @param <PM>
 */
public abstract class PbViewStylerBase<VIEW, PM extends PmObject> 
    implements PbViewStyler {

  @Override @SuppressWarnings("unchecked")
  public void applyStyle(Object view, PmObject pm) {
    applyStyleImpl((VIEW)view, (PM)pm);
  }

  protected abstract void applyStyleImpl(VIEW view, PM pm);
  
}
