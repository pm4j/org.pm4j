package org.pm4j.core.pm.impl.pathresolver;

import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.ExprExecExeption;
import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.common.expr.ThisExpr;

/**
 * Just passes the given object back.<br>
 * Used for scenarios, where the start object of the path should
 * be used.
 * <p>
 * Does not support {@link PathResolver#setValue(Object, Object)}.
 *
 * @author olaf boede
 *
 */
public class PassThroughPathResolver extends PathResolverBase {

  public PassThroughPathResolver() {
    super(SyntaxVersion.VERSION_2);
  }

  /** It's an immutable algorithm. It may be used as a singleton. */
  public static final PassThroughPathResolver INSTANCE = new PassThroughPathResolver();

  @Override
  public Object getValue(Object startObj) {
    return startObj;
  }

  @Override
  public Object getValue(Object pmParent, Object startObj) {
    return startObj;
  }

  @Override
  public void setValue(Object startObj, Object value) {
    throw new ExprExecExeption(new ExprExecCtxt(startObj), "Unable to set a value on an empty (pass-through) path.");
  }

  @Override
  public String toString() {
    return ThisExpr.THIS_KEYWORD;
  }
}
