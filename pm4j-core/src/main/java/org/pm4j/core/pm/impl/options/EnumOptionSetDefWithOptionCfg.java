package org.pm4j.core.pm.impl.options;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.ResKeyUtil;

/**
 * Provides all items of an <code>enum</code> definition as options.
 * <p>
 * The provided option titles are based on string resource definitions.
 * <p>
 * Resource definition sample for an <code>enum</code> type, defined in an own
 * .java-file:
 *
 * <pre>
 *  myEnum.VALUE_ONE       = One
 *  myEnum.VALUE_TWO       = Two
 *  myEnum_nullOptionTitle = Please select 'One' or 'Two'.
 * </pre>
 *
 * Resource definition sample for an <code>enum</code> type, defined within
 * another type:
 *
 * <pre>
 *  myClass.MyEnum.VALUE_ONE       = One
 *  myClass.MyEnum.VALUE_TWO       = Two
 *  myClass.MyEnum_nullOptionTitle = Please select 'One' or 'Two'.
 * </pre>
 *
 * The definition of a 'nullOptionTitle' is optional. If omitted, the
 * (optional) default resource key
 * {@link PmOptionCfg#NULL_OPTION_DEFAULT_RESKEY} will be used.
 *
 * @author olaf boede
 */
public class EnumOptionSetDefWithOptionCfg extends OptionSetDefBase<PmAttrEnumImpl<?>> {

  /**
   * This postfix may be used to define a <code>null</code> option title for a
   * specific enum type.
   */
  public static final String NULL_OPTION_RESKEY_POSTFIX = "_nullOptionTitle";


  private final Class<?> enumClass;
  private final String resKeyPfx;


  public EnumOptionSetDefWithOptionCfg(PmAttr<?> pmAttr, Class<?> enumClass, PmOptionCfg optionCfg, Method getOptionValuesMethod) {
    super(pmAttr, optionCfg, getOptionValuesMethod);
    assert enumClass != null;
    assert enumClass.getEnumConstants() != null : "The class must represent an enum definition.";

    this.enumClass = enumClass;
    this.resKeyPfx = ResKeyUtil.shortResKeyForClass(enumClass) + ".";

    if (StringUtils.isNotEmpty(optionCfg.values()))
      throw new PmRuntimeException("values() annotation attribute is not yet supported for enum attributes.");
    if (StringUtils.isNotEmpty(optionCfg.id()))
      throw new PmRuntimeException("id() annotation attribute is not yet supported for enum attributes.");
    if (StringUtils.isNotEmpty(optionCfg.title()))
      throw new PmRuntimeException("title() annotation attribute is not yet supported for enum attributes.");
  }

  @Override
  public PmOptionSet makeOptions(PmAttrEnumImpl<?> forAttr) {
    if (optionsPath != null ||
        getOptionValuesMethod != null) {
      return super.makeOptions(forAttr);
    }
    else {
      // XXX in parts redundant to the base class implementation...
      Enum<?>[] values = (Enum<?>[])enumClass.getEnumConstants();
      List<PmOption> options = new ArrayList<PmOption>(values.length);

      if (shouldMakeNullOption(forAttr)) {
        options.add(new PmOptionImpl("", getNullOptionTitle(forAttr), null));
      }

      for (Enum<?> e : values) {
        options.add(makeOption(forAttr, e));
      }
      return new PmOptionSetImpl(options);
    }
  }

  @Override
  public String getNullOptionTitle(PmAttrEnumImpl<?> forAttr) {
    if (PmOptionCfg.NULL_OPTION_DEFAULT_RESKEY.equals(nullOptionTitleResKey)) {
      String title = PmLocalizeApi.findLocalization(forAttr, ResKeyUtil.shortResKeyForClass(enumClass) + NULL_OPTION_RESKEY_POSTFIX);

      return title != null
                ? title
                : PmLocalizeApi.findLocalization(forAttr, PmOptionCfg.NULL_OPTION_DEFAULT_RESKEY);
    }
    else {
      return PmLocalizeApi.localize(forAttr, nullOptionTitleResKey);
    }
  }

  @Override
  protected Iterable<?> getOptionValues(PmAttrEnumImpl<?> forAttr) {
    return Arrays.asList(enumClass.getEnumConstants());
  }

  @Override
  protected PmOption makeOption(PmAttrEnumImpl<?> forAttr, Object o) {
    Enum<?> e = (Enum<?>)o;
    String resKey = resKeyPfx + e.name();
    String title = PmLocalizeApi.localize(forAttr, resKey);
    return new PmOptionImpl(e.name(), title, e);
  }

}

