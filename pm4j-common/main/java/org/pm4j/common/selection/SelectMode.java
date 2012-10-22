package org.pm4j.common.selection;

/**
 * The set of supported selection modes.
 */
public enum SelectMode {
  /** Only a single item may be selected. */
  SINGLE,
  /** More than one item may be selected. */
  MULTI,
  /** No item can be marked as selected. */
  NO_SELECTION,
  /** This value defines no specific mode. The default should be applied. */
  DEFAULT
}
