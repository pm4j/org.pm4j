package org.pm4j.core.pm.impl.converter;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.impl.AttrConverterCtxt;
import org.pm4j.core.pm.impl.AttrStringConverterBase;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmAttrUtil;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;
import org.pm4j.core.pm.impl.pathresolver.ExpressionPathResolver;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;

/**
 * A converter that uses the option set of the attribute in combination
 * with a path that points to the option-id.
 *
 * @author Olaf Boede
 */
public class PmConverterOptionBased extends AttrStringConverterBase<Object> {

  private static final Logger log = LoggerFactory.getLogger(PmConverterOptionBased.class);

  private PathResolver idPath;

  public PmConverterOptionBased(String idPathString) {
    this(ExpressionPathResolver.parse(idPathString));
  }

  public PmConverterOptionBased(PathResolver idPath) {
    assert idPath != null;

    this.idPath = idPath;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Object stringToValueImpl(AttrConverterCtxt ctxt, String s) {
    PmAttrBase<Object, Object> pmAttr = (PmAttrBase<Object, Object>) ctxt.getPmAttr();
    Object value = null;
    PmOptionSet os = pmAttr.getOptionSet();
    if (os == null) {
      if (StringUtils.isEmpty(s)) {
        return null;
      }
    }
    else {
      PmOption o = os.findOptionForIdString(s);
      if (o != null) {
        if (o.getValue() != null) {
          value = o.getValue();
        } else {
          value = PmAttrUtil.backingValueToValue(pmAttr, o.getBackingValue());
        }
      }
    }

    if (value == null && StringUtils.isNotEmpty(s)) {
      // FIXME olaf: add business scenario specific missing value handling here!
      log.warn("Can't set value of attribute '" + ctxt +
          "No option exists for value '" + s + "'. Available option ids : " +
          (os != null
              ? PmOptionSetUtil.getOptionIds(os).toString()
              : "no options found"));
    }

    return value;
  }

  @Override
  protected String valueToStringImpl(AttrConverterCtxt ctxt, Object pmValue) {
    return (pmValue != null)
            ? ObjectUtils.toString(idPath.getValue(pmValue))
            : null;
  }

}
