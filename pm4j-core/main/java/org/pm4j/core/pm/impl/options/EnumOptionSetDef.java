package org.pm4j.core.pm.impl.options;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.pm4j.common.util.collection.IterableUtil;
import org.pm4j.common.util.resource.ClassPathResourceStringUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.annotation.PmOptionCfg;
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

    List<Class<?>> resCtxtClasses = forAttr.getPmResLoaderCtxtClasses();
    Locale locale = forAttr.getPmConversation().getPmLocale();
    String resKeyPfx = ResKeyUtil.shortResKeyForClass(enumClass) + ".";
    for (Enum<?> e : values) {
      options.add(makeEnumOption(e, resKeyPfx, resCtxtClasses, locale));
    }
    return new PmOptionSetImpl(options);
  }

  @Override
  public String getNullOptionTitle(PmAttrEnumImpl<?> forAttr) {
    // TODO: The null-option-title should be provided by a @PmOptionCfg annotation.
    String title = forAttr.findLocalization(ResKeyUtil.shortResKeyForClass(enumClass) + NULL_OPTION_RESKEY_POSTFIX);

    if (title == null) {
      title = forAttr.findLocalization(PmOptionCfg.NULL_OPTION_DEFAULT_RESKEY);
    }

    return title != null
              ? title
              : "";
  }

  private PmOptionImpl makeEnumOption(Enum<?> value, String resKeyPfx, List<Class<?>> resCtxtClasses, Locale locale) {
    String resKey = resKeyPfx + value.name();
    String title = ClassPathResourceStringUtil.getString(locale, resCtxtClasses, resKey);
    return new PmOptionImpl(value.name(), title, value);
  }

}

