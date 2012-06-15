package org.pm4j.core.pm.impl.title;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * A simple provider that provides titles based on attribute values.
 * <p>
 * FIXME olaf: No attribute value change listener implemented yet...
 */
public class TitleProviderAttrValueBased extends TitleProviderPmResBased<PmObjectBase> {

  private String attrName;
  private boolean usedForPmElement;

  /**
   * Default constructor.
   */
  public TitleProviderAttrValueBased(String attrName, boolean usedForPmElement) {
    super();
    assert StringUtils.isNotBlank(attrName);

    this.attrName = attrName;
    this.usedForPmElement = usedForPmElement;
  }

  /**
   * @return The value of the attribute with the name {@link #attrName}.
   */
  @Override
  public String getTitle(PmObjectBase pm) {
    return PmUtil.getPmChildOfType(getPmElement(pm), attrName, PmAttr.class).getValueAsString();
  }

  // FIXME olaf: Quick Hack for non-element types.
  // a) slow (using reflection)
  // b) not really easy to understand
  //  --> Write and initialize some specific title providers
  private PmElement getPmElement(PmObject pm) {
    if (usedForPmElement) {
      return (PmElement)pm;
    }
    else {
      PmObject p = pm.getPmParent();
      while (! (p instanceof PmElement)) {
        p = pm.getPmParent();
      }
      return (PmElement)p;
    }
  }

}
