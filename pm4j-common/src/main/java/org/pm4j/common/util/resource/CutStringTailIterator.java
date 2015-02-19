package org.pm4j.common.util.resource;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

/**
 * An iterator that provides substring of a base string based on termination
 * sequences found within the base string.
 * <p>
 * Example - The following code:
 * <pre>
 *  Iterator<String> i = CutStringTailIterator("1/2/3", "/");
 *  while (i.hasNext()) {
 *    System.out.println(i.next());
 *  }
 * </pre>
 * ...will print the following three lines:
 * <pre>
 *   1/2/3
 *   1/2
 *   1
 * </pre>
 */
class CutStringTailIterator implements Iterator<String> {

  private String  longString;

  private String  termString;

  private int     currPos;

  private int     nextPos;

  private boolean isBeforeFirstPos = true;

  public CutStringTailIterator(String longString, String termString) {
    assert !StringUtils.isEmpty(longString);
    assert !StringUtils.isEmpty(termString);

    this.longString = longString;
    this.termString = termString;
  }

  public boolean hasNext() {
    return (nextPos != -1);
  }

  public String next() {
    if (currPos == -1) {
      throw new RuntimeException("Iteration behind last position is not allowed.");
    }

    if (isBeforeFirstPos) {
      currPos = longString.length();
      nextPos = longString.lastIndexOf(termString);
      isBeforeFirstPos = false;
    }
    else {
      currPos = nextPos;
      if (nextPos != -1) {
        nextPos = longString.lastIndexOf(termString, nextPos - 1);
      }
    }

    return longString.substring(0, currPos);
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

}
