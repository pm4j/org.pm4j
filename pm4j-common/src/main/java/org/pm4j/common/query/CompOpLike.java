package org.pm4j.common.query;

import org.apache.commons.lang.StringUtils;

/**
 * A like compare operator.
 *
 * @author Olaf Boede
 */
public class CompOpLike extends CompOpBase<String> {

  private static final long serialVersionUID = 1L;
  public static final String NAME = "compOpLike";


  public CompOpLike() {
    super(NAME);
  }

  @Override
  protected boolean isEffectiveFilterValueImpl(String filterValue) {
    return ! StringUtils.isBlank(filterValue);
  }

}
