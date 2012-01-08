package org.pm4j.core.pm.impl.options;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;

public class PmOptionSetUtil {

  public static List<?> getOptionValues(PmOptionSet os) {
    List<Object> values = new ArrayList<Object>();

    for (PmOption o : os.getOptions()) {
      values.add(o.getValue());
    }

    return values;
  }

  public static List<String> getOptionIds(PmOptionSet os) {
    List<String> values = new ArrayList<String>();

    for (PmOption o : os.getOptions()) {
      values.add(o.getIdAsString());
    }

    return values;
  }

  public static List<String> getOptionTitles(PmOptionSet os) {
    List<String> values = new ArrayList<String>();

    for (PmOption o : os.getOptions()) {
      values.add(o.getPmTitle());
    }

    return values;
  }


}
