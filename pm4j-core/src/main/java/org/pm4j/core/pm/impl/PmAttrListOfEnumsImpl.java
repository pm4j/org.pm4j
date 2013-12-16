package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.annotation.PmAttrListCfg;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.options.PmOptionImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetImpl;

/**
 * Binds to a {@link Collection} of {@link Enum}s.
 *
 * @param T_ENUM Type of the handled enum items.
 *
 * @author olaf boede
 */
@PmAttrListCfg(itemStringConverter=PmAttrListOfEnumsImpl.Converter.class)
public class PmAttrListOfEnumsImpl<T_ENUM extends Enum<?>> extends PmAttrListImpl<T_ENUM> {

  private final Class<T_ENUM> enumClass;

  /**
   * @param pmParent The PM hierarchy parent.
   * @param enumClass The class of the handled enum type.
   */
  public PmAttrListOfEnumsImpl(PmElementBase pmParent, Class<T_ENUM> enumClass) {
    super(pmParent);
    this.enumClass = enumClass;
  }

  @Override
  protected PmOptionSet getOptionSetImpl() {
    List<T_ENUM> optionValues = getEnumOptionValues();
    PmOptionSetImpl os = new PmOptionSetImpl();
    for (T_ENUM e : optionValues) {
      os.addOption(makeEnumOption(e));
    }
    return os;
  }

  /**
   * Generates an option for an enum value.
   *
   * @param value The enum value.
   * @return The option to display for the value.
   */
  protected PmOption makeEnumOption(T_ENUM value) {
    return new PmOptionImpl(value.name(), PmLocalizeApi.localizeEnumValue(this, value), value, value);
  }


  /**
   * Provides the set of enums that is not yet part of the list.
   * <p>
   * The set can be filtered by defining some filter logic within {@link #isAnOption(Enum)}.
   * <p>
   * By overriding isExcludeSelectedItemsFromOptionSet() (Standard: true) it's possible to
   * include already selected items in the list.
   *
   * @return The set of enums that can be selected by the user.
   */
  protected List<T_ENUM> getEnumOptionValues() {
      List<T_ENUM> optionEnums = new ArrayList<T_ENUM>();
      HashSet<T_ENUM> valueSet = new HashSet<T_ENUM>(getValue());
      boolean excludeSelectedItemsFromOptionSet = isExcludeSelectedItemsFromOptionSet();

      for (T_ENUM e : enumClass.getEnumConstants()) {
          if (isAnOption(e) && !(excludeSelectedItemsFromOptionSet && valueSet.contains(e))) {
              optionEnums.add(e);
          }
      }

      return optionEnums;
  }

  /**
   * Configures, whether already selected items should be excluded from the option set.
   * <p>
   * Override and set to false to include.
   * <p>
   * (Standard: true)
   *
   * @return whether to exclude already selected items from the option set
   */
  protected boolean isExcludeSelectedItemsFromOptionSet() {
      return true;
  }

  /**
   * This method may be overridden to filter the set of enums available for this
   * attribute.
   *
   * @return <code>true</code> if the value can be used. <br>
   *         <code>false</code> if the value should not be provided as an option
   *         for the user.
   */
  protected boolean isAnOption(T_ENUM value) {
    return true;
  }

  /** Translates between strings and enums. */
  public static class Converter extends AttrStringConverterBase<Enum<?>> {

      @Override
      @SuppressWarnings({ "unchecked", "rawtypes" })
      protected Enum<?> stringToValueImpl(AttrConverterCtxt ctxt, String s) {
          return (s != null && s.length() > 0)
              ? Enum.valueOf(((PmAttrListOfEnumsImpl) ctxt.getPmAttr()).enumClass, s)
              : null;
      }

      @Override
      protected String valueToStringImpl(AttrConverterCtxt ctxt, Enum<?> v) {
          return (v != null) ? v.name() : null;
      }

  }
}