package org.pm4j.core.pm.impl.options;

import java.lang.reflect.Method;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.impl.PmAttrBase;

/**
 * An algorithms that provides options for attribute values based on
 * the annotation {@link PmOptionCfg}.
 *
 * @author olaf boede
 */
public class GenericOptionSetDef extends OptionSetDefBase<PmAttrBase<?,?>> {

  public GenericOptionSetDef(PmOptionCfg cfg, Method getOptionValuesMethod) {
    super(cfg, getOptionValuesMethod);
  }

  @Override
  protected PmOption makeOption(PmAttrBase<?,?> forAttr, Object o) {
    Object id = idPath.getValue(o);
    Object title = titlePath.getValue(o);
    Object value = valuePath.getValue(o);
    return new PmOptionImpl(
                ObjectUtils.toString(id, ""),
                ObjectUtils.toString(title, ""),
                value);
  }


}
