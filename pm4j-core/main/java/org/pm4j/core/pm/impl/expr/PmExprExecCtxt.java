package org.pm4j.core.pm.impl.expr;

import org.pm4j.core.pm.PmObject;

public class PmExprExecCtxt extends ExprExecCtxt {

  private final PmObject pm;

  public PmExprExecCtxt(PmObject startObject) {
    super(startObject);
    this.pm = startObject;
  }

  public PmExprExecCtxt(PmObject pm, Object startObject) {
    super(startObject);
    this.pm = pm;
  }

  public PmObject getPm() {
    return pm;
  }

  @Override
  public ExprExecCtxt makeSubCtxt() {
    return new PmExprExecCtxt(pm);
  }
}
