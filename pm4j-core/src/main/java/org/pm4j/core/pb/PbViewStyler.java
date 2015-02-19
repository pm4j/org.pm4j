package org.pm4j.core.pb;

import org.pm4j.core.pm.PmObject;

/**
 * Common interface for classes that apply styles to PM-views.
 *  
 * @author olaf boede
 */
public interface PbViewStyler {

  /**
   * Applies the style(s) provided by
   * {@link PmObject#getPmStyleClasses()} to the given view.
   * 
   * @param view
   *          The view to apply the style for.
   * @param pm
   *          The PM that provides the style information.
   */
  void applyStyle(Object view, PmObject pm);

}
