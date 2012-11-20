package org.pm4j.core.pm.impl.converter;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;
import org.pm4j.core.pm.impl.pathresolver.ExpressionPathResolver;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;

/**
 * A converter that uses the option set of the attribute in combination
 * with a path that points to the option-id.
 *
 * @author olaf boede
 */
public class PmConverterOptionBased implements PmAttr.Converter<Object> {

  private static final Log log = LogFactory.getLog(PmConverterOptionBased.class);

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
  public Object stringToValue(PmAttr<?> pmAttr, String s) {
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
        value = o.getValue();
        value = ((PmAttrBase<?, Object>)pmAttr).convertBackingValueToPmValue(value);
      }
    }

    if (value == null && StringUtils.isNotEmpty(s)) {
      // FIXME olaf: add business scenario specific missing value handling here!
      log.warn("Can't set value of attribute '" + PmUtil.getPmLogString(pmAttr) +
          "No option exists for value '" + s + "'. Available option ids : " +
          (os != null
              ? PmOptionSetUtil.getOptionIds(os).toString()
              : "no options found"));
    }

    return value;
  }

  @Override
  public String valueToString(PmAttr<?> pmAttr, Object pmValue) {
    @SuppressWarnings("unchecked")
    Object backingValue = ((PmAttrBase<Object,?>)pmAttr).convertPmValueToBackingValue(pmValue);
    return ObjectUtils.toString(idPath.getValue(backingValue));
  }

  @Override
  public Serializable valueToSerializable(PmAttr<?> pmAttr, Object v) {
    return v != null
              ? valueToString(pmAttr, v)
              : null;
  }

  @Override
  public Object serializeableToValue(PmAttr<?> pmAttr, Serializable s) {
    return stringToValue(pmAttr, (String)s);
  }

}
