package org.pm4j.core.pm.api;

import java.util.Comparator;

import org.pm4j.core.pm.PmMessage;

/**
 * Used to order PmMessage. Sorts first for severity descending and second for
 * message text alphabetically.
 * 
 * @author okossak
 */
public class PmMessageComparator implements Comparator<PmMessage> {

  /**
   * {@inheritDoc}
   */
  @Override
  public int compare(PmMessage o1, PmMessage o2) {

    int severityDiff = -o1.getSeverity().compareTo(o2.getSeverity());
    // @formatter:off
    return (severityDiff == 0)
        ? o1.getTitle().compareTo(o2.getTitle())
        : severityDiff;
    // @formatter:on
  }
}
