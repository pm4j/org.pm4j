package org.pm4j.core.pm;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for options sets.
 *
 * @author olaf boede
 */
public interface PmOptionSet {

  public static final String NULL_OPTION_ID = "";

  /**
   * @return <code>true</code> when more than one option may be selected.
   */
  boolean isMultiselection();

  /**
   * @return The set of options. Returns an empty list when there are no options.
   */
  List<PmOption> getOptions();

  /**
   * @return the number of options
   */
  Integer getSize();

  /**
   * @return The first option of the set. <code>null</code> if there is no option.
   */
  PmOption getFirstOption();

  /**
   * @return The value of the first option. <code>null</code> if there is no option.
   */
  Object getFirstOptionValue();

  /**
   * @return The last option of the set. <code>null</code> if there is no option.
   */
  PmOption getLastOption();

  /**
   * @return The value of the last option. <code>null</code> if there is no option.
   */
  Object getLastOptionValue();

  /**
   * TODOC:
   * @param id
   * @return
   */
  PmOption findOptionForId(Serializable id);
  PmOption getOptionForId(Serializable id);

  PmOption findOptionForIdString(String idString);
  PmOption getOptionForIdString(String idString);

  PmOption findOptionForTitle(String title);
  PmOption getOptionForTitle(String title);
}
