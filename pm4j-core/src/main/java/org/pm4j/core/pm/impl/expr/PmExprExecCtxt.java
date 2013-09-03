package org.pm4j.core.pm.impl.expr;

import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.PmObject;

/**
 * A PM specific expression execution context.
 * <p>
 * It extends the based class by defining
 * <ul>
 *  <li>a context PM that is used to resolve named object references and</li>
 *  <li>obtains an expression syntax version using the {@link PmDefaults} provided by the {@link PmConversation}.</li>
 * </ul>
 *
 * @author olaf boede
 */
public class PmExprExecCtxt extends ExprExecCtxt {

  private final PmObject pm;

  public PmExprExecCtxt(PmObject startObject) {
    this(startObject, startObject);
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
