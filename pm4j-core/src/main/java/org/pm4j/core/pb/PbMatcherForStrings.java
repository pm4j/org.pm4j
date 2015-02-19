package org.pm4j.core.pb;

import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;

/**
 * Special factory for string attributes. Depending on the result of
 * {@link PmAttrString#isMultiLine()} it they will be represented either using
 * the configurable {@link #textAreaBinder} or the configurable
 * {@link #textAreaBinder} respectively.
 * 
 * @author olaf boede
 */
public class PbMatcherForStrings extends PbMatcher {

  private PbFactory<?> textFieldBinder;
  private PbFactory<?> textAreaBinder;

  public PbMatcherForStrings(PbFactory<?> textFieldBinder, PbFactory<?> textAreaBinder) {
    this.textFieldBinder = textFieldBinder;
    this.textAreaBinder = textAreaBinder;
  }

  @Override
  public PbFactory<?> findPbFactory(PmObject pm) {
    if (pm instanceof PmAttrString) {
      return ((PmAttrString)pm).isMultiLine()
          ? textAreaBinder
          : textFieldBinder;
    }
    // no match
    return null;
  }

}
