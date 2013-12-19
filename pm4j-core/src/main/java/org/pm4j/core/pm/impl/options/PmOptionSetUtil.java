package org.pm4j.core.pm.impl.options;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.api.PmLocalizeApi;

/**
 * Helper methods for attribute options.
 *
 * @author olaf boede
 */
public final class PmOptionSetUtil {

  /**
   * Generates the localized options for a boolean attribute.
   *
   * @param pmAttr An attribute to generate the options for.
   * @return The generated options.
   */
  public static List<PmOption> makeBooleanOptions(PmAttr<?> pmAttr, boolean withNullOption) {
    List<PmOption> options = new ArrayList<PmOption>();

    if (withNullOption) {
      options.add(new PmOptionImpl("", getNullOptionTitle(pmAttr), null));
    }
    options.add(new PmOptionImpl(Boolean.TRUE.toString(), PmLocalizeApi.localizeBooleanValue(pmAttr, Boolean.TRUE), Boolean.TRUE));
    options.add(new PmOptionImpl(Boolean.FALSE.toString(), PmLocalizeApi.localizeBooleanValue(pmAttr, Boolean.FALSE), Boolean.FALSE));

    return options;
  }

  /**
   * Helper for manual enum option set creation.
   *
   * @param values The enum values to get an option for.
   * @return The option set for the given enum set.
   */
  public static List<PmOption> makeEnumOptions(PmAttr<?> pmAttr, Enum<?>... values) {
    List<PmOption> options = new ArrayList<PmOption>();

    for (Enum<?> v : values) {
      if (v == null) {
        options.add(new PmOptionImpl("", getNullOptionTitle(pmAttr), null));
      }
      options.add(new PmOptionImpl(v.name(), PmLocalizeApi.localizeEnumValue(pmAttr, v), v));
    }

    return options;
  }

  /**
   * Generates the title to display for the option that defines <code>null</code> value.
   *
   * @param forAttr The attribute to get the title for.
   * @return The localized null-option title.
   */
  public static String getNullOptionTitle(PmAttr<?> forAttr) {
    return getNullOptionTitle(forAttr, PmOptionCfg.NULL_OPTION_DEFAULT_RESKEY);
  }

  /**
   * Generates the title to display for the option that defines <code>null</code> value.
   *
   * @param forAttr The attribute to get the title for.
   * @param nullOptionTitleResKey The specific null-option resource key to use.
   * @return The localized null-option title.
   */
  public static String getNullOptionTitle(PmAttr<?> forAttr, String nullOptionTitleResKey) {
    return PmOptionCfg.NULL_OPTION_DEFAULT_RESKEY.equals(nullOptionTitleResKey)
              // default key must not exist.
              ? PmLocalizeApi.findLocalization(forAttr, nullOptionTitleResKey)
              // user-defined key should exist. -> debug-title and a log message will appear.
              : PmLocalizeApi.localize(forAttr, nullOptionTitleResKey);
  }

  /**
   * Extracts the backing values from the given option set.
   *
   * @param os The option set to get the backing values from.
   * @return The backing values for all options.
   */
  public static List<?> getOptionBackingValues(PmOptionSet os) {
    List<Object> values = new ArrayList<Object>();

    for (PmOption o : os.getOptions()) {
      values.add(o.getBackingValue());
    }

    return values;
  }

  /**
   * Extracts the id's from the given option set.
   *
   * @param os The option set to get the id's from.
   * @return The id's for all options.
   */
  public static List<String> getOptionIds(PmOptionSet os) {
    List<String> values = new ArrayList<String>();

    for (PmOption o : os.getOptions()) {
      values.add(o.getIdAsString());
    }

    return values;
  }

  /**
   * Extracts the titles from the given option set.
   *
   * @param os The option set to get the titles from.
   * @return The titles for all options.
   */
  public static List<String> getOptionTitles(PmOptionSet os) {
    List<String> values = new ArrayList<String>();

    for (PmOption o : os.getOptions()) {
      values.add(o.getPmTitle());
    }

    return values;
  }


}
