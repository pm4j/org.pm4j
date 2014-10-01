package org.pm4j.core.sample.gallery.attr;

import org.pm4j.core.pm.PmAttrBigDecimal;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrDouble;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrList;
import org.pm4j.core.pm.PmAttrLong;
import org.pm4j.core.pm.PmAttrShort;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTab;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrDoubleImpl;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrListImpl;
import org.pm4j.core.pm.impl.PmAttrLongImpl;
import org.pm4j.core.pm.impl.PmAttrShortImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmElementBase;
import org.pm4j.core.pm.joda.PmAttrLocalDate;
import org.pm4j.core.pm.joda.PmAttrLocalTime;
import org.pm4j.core.pm.joda.impl.PmAttrLocalDateAndTimeImpl;
import org.pm4j.core.pm.joda.impl.PmAttrLocalDateImpl;
import org.pm4j.core.pm.joda.impl.PmAttrLocalTimeImpl;

public class AttrSamplesTabPm extends PmElementBase implements PmTab {

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

  enum MyEnum { VAL1, VAL2 };
  public final PmAttrEnum<MyEnum> enumAttr = new PmAttrEnumImpl<MyEnum>(this, MyEnum.class);

  public final PmAttrList<String> listAttr = new PmAttrListImpl<String>(this);

  public final PmAttrLocalDate localDateAttr = new PmAttrLocalDateImpl(this);

  public final PmAttrLocalTime localTimeAttr = new PmAttrLocalTimeImpl(this);

  public final PmAttrLocalDateAndTimeImpl localDateAndTimeAttr = new PmAttrLocalDateAndTimeImpl(this);

  public AttrSamplesTabPm(PmObject parentPm) {
    super(parentPm);
  }


}
