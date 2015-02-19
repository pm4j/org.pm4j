package org.pm4j.core.pm.annotation;

/**
 * A three state boolean which may be used in boolean-kind annotations that 
 * need an undefined default value.
 *   
 * @author olaf boede
 */
public enum PmBoolean {
  TRUE,
  FALSE,
  UNDEFINED
}
