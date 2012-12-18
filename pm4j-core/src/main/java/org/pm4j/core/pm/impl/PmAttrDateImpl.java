package org.pm4j.core.pm.impl;

import java.util.Date;

import org.pm4j.core.pm.PmObject;

/**
 * Implements a PM attribute for {@link Date} values.
 *
 * @author olaf boede
 */
public class PmAttrDateImpl extends PmAttrDateBase<Date> {

  public PmAttrDateImpl(PmObject pmParent) {
    super(pmParent);
  }

}
