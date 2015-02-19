package org.pm4j.core.pm.impl;

/**
 * Defines what a PM proxy should do in case of a missing delegate.
 *
 * @author olaf boede
 */
public enum OnMissingDelegate {

  /** Shows the disabled command. */
  DISABLE,

  /** Makes the command invisible. */
  HIDE
}