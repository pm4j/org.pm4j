package org.pm4j.core.pm.impl.options;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.pm4j.common.util.collection.IterableUtil;
import org.pm4j.core.exception.PmRuntimeException;
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
public class EnumOptionSetDef implements PmOptionSetDef<PmAttrEnumImpl<?>> {

  /**
   * This postfix may be used to define a <code>null</code> option title for a
   * specific enum type.
   */
  public static final String NULL_OPTION_RESKEY_POSTFIX = "_nullOptionTitle";


  private final Class<?> enumClass;
  private Method getOptionValuesMethod;


  public EnumOptionSetDef(Class<?> enumClass, Method getOptionValuesMethod) {
    assert enumClass != null;
    assert enumClass.getEnumConstants() != null : "The class must represent an enum definition.";

    this.enumClass = enumClass;
    this.getOptionValuesMethod = getOptionValuesMethod;
  }

  public PmOptionSet makeOptions(PmAttrEnumImpl<?> forAttr) {
    Enum<?>[] values;

    if (getOptionValuesMethod == null) {
      values = (Enum<?>[])enumClass.getEnumConstants();
    }
    else {
      try {
        Iterable<?> i = (Iterable<?>) getOptionValuesMethod.invoke(forAttr);
        values = (i != null)
              ? IterableUtil.shallowCopy(i).toArray(new Enum<?>[]{})
              : new Enum<?>[]{};
      } catch (Exception e) {
        throw new PmRuntimeException(forAttr, "Unable to execute the option providing method: " +
            getOptionValuesMethod, e);
      }
    }

    List<PmOption> options = new ArrayList<PmOption>(values.length);

    if (forAttr.getNullOptionDefault() != PmOptionCfg.NullOption.NO &&
        ! forAttr.isRequired()) {
      options.add(new PmOptionImpl("", getNullOptionTitle(forAttr), null));
    }

    String resKeyPfx = ResKeyUtil.shortResKeyForClass(enumClass) + ".";
    for (Enum<?> e : values) {
      String resKey = resKeyPfx + e.name();
      options.add(new PmOptionImpl(e.name(), PmLocalizeApi.localize(forAttr, resKey), e));
    }
    return new PmOptionSetImpl(options);
  }

  @Override
  public String getNullOptionTitle(PmAttrEnumImpl<?> forAttr) {
    // TODO: The null-option-title should be provided by a @PmOptionCfg annotation.
    String title = PmLocalizeApi.findLocalization(forAttr, ResKeyUtil.shortResKeyForClass(enumClass) + NULL_OPTION_RESKEY_POSTFIX);

    if (title == null) {
      title = PmLocalizeApi.findLocalization(forAttr, PmOptionCfg.NULL_OPTION_DEFAULT_RESKEY);
    }

    return title != null
              ? title
              : "";
  }
}

