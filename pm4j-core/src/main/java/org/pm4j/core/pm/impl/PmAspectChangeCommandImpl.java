package org.pm4j.core.pm.impl;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;

public class PmAspectChangeCommandImpl extends PmCommandImpl {

  private final String pmAspectName;
  private final Object oldValue;
  private final Object newValue;

  public PmAspectChangeCommandImpl(PmObject pmParent, String pmAspectName, Object oldValue, Object newValue) {
    super(pmParent);
    assert StringUtils.isNotBlank(pmAspectName);
    this.pmAspectName = pmAspectName;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  protected void doItImpl() {
    throw new PmRuntimeException(this, "No do-implementation for aspect: " + pmAspectName);
  }

  public String getPmAspectName() {
    return pmAspectName;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }

}
