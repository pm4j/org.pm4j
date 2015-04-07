package org.pm4j.tools.test;

import java.util.Map;
import java.util.TreeMap;

/**
 * A call counter intended for test cases.
 *
 * @author Olaf Boede
 */
public class CallCounter {

  private Map<String, Integer> nameToCallCountMap = new TreeMap<String, Integer>();

  public void incCallCount(String callName) {
    Integer count = nameToCallCountMap.get(callName);
    count = (count == null)
        ? 1
        : count+1;
    nameToCallCountMap.put(callName, count);
  }

  public int getCallCount(String callName) {
    return nameToCallCountMap.get(callName);
  }

  /**
   * If used with no parameters it will reset all call counters.<br>
   * Otherwise the counters for the given call names.
   *
   * @param callNames
   */
  public void reset(String... callNames) {
    if (callNames.length == 0) {
      nameToCallCountMap.clear();
    } else {
      for (String n : callNames) {
        nameToCallCountMap.remove(n);
      }
    }
  }

  /** Provides a string containing all name-to-call-count map items sorted by name. */
  @Override
  public String toString() {
    return nameToCallCountMap.toString();
  }
}
