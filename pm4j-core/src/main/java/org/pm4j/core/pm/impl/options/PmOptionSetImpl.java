package org.pm4j.core.pm.impl.options;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;

public class PmOptionSetImpl implements PmOptionSet {

  private List<PmOption> options;
  private boolean multiselection = false;

  public PmOptionSetImpl(List<PmOption> pmOptions) {
    assert pmOptions != null;
    options = pmOptions;
  }

  public PmOptionSetImpl(PmOption... pmOptions) {
    options = new ArrayList<PmOption>();
    for (PmOption o : pmOptions) {
      options.add(o);
    }
  }

  public List<PmOption> getOptions() {
    return options;
  }

  public Integer getSize() {
    return options.size();
  }

  public void addOption(PmOption pmOption) {
    options.add(pmOption);
  }

  /**
   * Shortcut method to adds an option just by specifying an id and a value.
   * <p>
   * Uses the given id object as option-value and -backing value too.<br>
   * This means: This shortcut can be used only for simple type attributes where
   * the value and backing value type are the same and the value can also be
   * used as an option identifier.
   *
   * @param id
   *          The option id. (Will be used as option value's too. See above.)
   * @param title
   *          The option title.
   * @return The added option.
   */
  public PmOptionImpl addOption(Serializable id, String title) {
    PmOptionImpl o = new PmOptionImpl(id, title, id, id);
    addOption(o);
    return o;
  }

  /**
   * Shortcut method to adds an option.
   *
   * @param id
   *          The option id. (Will be used as option value's too. See above.)
   * @param title
   *          The option title.
   * @param value
   *          The attribute value that corresponds to this option. May be <code>null</code>.
   * @param backingValue
   *          The backing attribute value that corresponds to this option. May be <code>null</code>.
   * @return The added option.
   */
  public PmOptionImpl addOption(Serializable id, String title, Object value, Object backingValue) {
    PmOptionImpl o = new PmOptionImpl(id, title, value, backingValue);
    addOption(o);
    return o;
  }

  @Override
  public PmOption getFirstOption() {
    List<PmOption> options = getOptions();
    return (options.size() > 0)
        ? options.get(0)
        : null;
  }

  @Override
  public Object getFirstOptionValue() {
    PmOption o = getFirstOption();
    return o != null
        ? o.getValue()
        : null;
  }

  @Override
  public PmOption getLastOption() {
    List<PmOption> options = getOptions();
    return (options.size() > 0)
        ? options.get(options.size()-1)
        : null;
  }

  @Override
  public Object getLastOptionValue() {
    PmOption o = getLastOption();
    return o != null
        ? o.getValue()
        : null;
  }

  @Override
  public PmOption findOptionForId(Object id) {
    String idString = ObjectUtils.toString(id, NULL_OPTION_ID);
    return findOptionForIdString(idString);
  }

  @Override
  public PmOption findOptionForIdString(String idString) {
    List<PmOption> optionList = getOptions();
    for (PmOption o : optionList) {
      String optIdString = ObjectUtils.toString(o.getId());
      if (StringUtils.equals(optIdString, idString)) {
        return o;
      }
    }
    // not found:
    return null;
  }

  @Override
  public PmOption getOptionForId(Object id) {
    PmOption o = findOptionForId(id);
    if (o == null) {
      throw new PmRuntimeException(makeOptIdNotFoundMsg(id, getOptions()));
    }
    return o;
  }


  @Override
  public PmOption getOptionForIdString(String idString) {
    PmOption o = findOptionForIdString(idString);
    if (o == null) {
      throw new PmRuntimeException(makeOptIdNotFoundMsg(idString, getOptions()));
    }
    return o;
  }

  @Override
  public PmOption findOptionForTitle(String title) {
    List<PmOption> optionList = getOptions();
    for (PmOption o : optionList) {
      if (StringUtils.equals(o.getPmTitle(), title)) {
        return o;
      }
    }
    // not found:
    return null;
  }

  @Override
  public PmOption getOptionForTitle(String title) {
    PmOption o = findOptionForTitle(title);
    if (o == null) {
      throw new PmRuntimeException(makeOptIdNotFoundMsg(title, getOptions()));
    }
    return o;
  }

  public boolean isMultiselection() {
    return this.multiselection;
  }

  public void setMultiselection(boolean multiselection) {
    this.multiselection = multiselection;
  }

  private String makeOptIdNotFoundMsg(Object id, List<PmOption> pmOptions) {
    StringBuilder s = new StringBuilder();
    s.append("No option found for id '").append(id).append("'. Availvable ids are: [");
    for (int i=0 ; i<pmOptions.size(); ++i) {
      PmOption o = pmOptions.get(i);
      if (i>0)
        s.append(", ");
      s.append(o.getId());
    }
    return s.toString();
  }


  /**
   * A special option set that supports optimized association of an
   * option ID to the corresponding option value.
   */
  public static class WithIdMap extends PmOptionSetImpl {

    private Map<String, PmOption> idToOptionMap;

    public WithIdMap(List<PmOption> pmOptions) {
      super(pmOptions);
    }

    public WithIdMap(PmOption... pmOptions) {
      super(pmOptions);
    }

    @Override
    public PmOption findOptionForIdString(String id) {
      Map<String, PmOption> map = this.idToOptionMap;
      if (map == null) {
        List<PmOption> olist = getOptions();
        map = new HashMap<String, PmOption>(olist.size());
        for (PmOption o : olist) {
          map.put(o.getIdAsString(), o);
        }
        this.idToOptionMap = map;
      }
      return map.get(id);
    }

    @Override
    public void addOption(PmOption pmOption) {
      super.addOption(pmOption);
      idToOptionMap = null;
    }
  }
}
