package org.pm4j.common.util;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;

/**
 * A call counter intended for test cases.
 *
 * @author oboede
 *
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

  /**
   * Asserts whether the call counters match the expected value 
   * @param expected expected value, e.g. "{findIds=2, getItemForId=7}"
   */
  public void assertCalls(String expected) {
    assertCalls("Call count stability check.", expected);
  }

  /**
   * Asserts whether the call counters match the expected value
   * 
   * @param message special message to show on failure 
   * @param expected expected value, e.g. "{findIds=2, getItemForId=7}"
   */
  public void assertCalls(String message, String expected) {
    Assert.assertEquals(message, expected, toString());
  }
}
