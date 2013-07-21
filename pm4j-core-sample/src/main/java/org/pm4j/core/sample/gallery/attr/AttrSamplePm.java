package org.pm4j.core.sample.gallery.attr;

import org.pm4j.core.pm.PmAttrBigDecimal;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrDouble;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrLong;
import org.pm4j.core.pm.PmAttrShort;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrDoubleImpl;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrLongImpl;
import org.pm4j.core.pm.impl.PmAttrShortImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmContainerImpl;

public class AttrSamplePm extends PmContainerImpl {

  /**
   * A string value attribute.
   */
  public final PmAttrString stringAttr = new PmAttrStringImpl(this);

  public final PmAttrInteger integerAttr = new PmAttrIntegerImpl(this);
  public final PmAttrShort shortAttr = new PmAttrShortImpl(this);
  public final PmAttrLong longAttr = new PmAttrLongImpl(this);
  public final PmAttrDouble doubleAttr = new PmAttrDoubleImpl(this);
  public final PmAttrBigDecimal bigDecimalAttr = new PmAttrBigDecimalImpl(this);
  public final PmAttrBoolean booleanAttr = new PmAttrBooleanImpl(this);
  


  public AttrSamplePm(PmObject pmParent) {
    super(pmParent);
  }

}
