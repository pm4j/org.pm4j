package org.pm4j.core.pm.impl.expr;

import org.pm4j.common.expr.AttributeExpr;
import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.ExprExecExeption;
import org.pm4j.common.expr.Expression;
import org.pm4j.common.expr.NameWithModifier;
import org.pm4j.common.expr.NameWithModifier.Modifier;
import org.pm4j.common.expr.OptionalExpression;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.core.util.reflection.BeanAttrAccessor;
import org.pm4j.core.util.reflection.BeanAttrAccessorImpl;
import org.pm4j.core.util.reflection.ReflectionException;

/**
 * An expression that gets initialized by a name that may represent
 * an attribute or a PM variable.<br>
 * The expression searches at runtime first for an attribute
 * and if that's not found, for a PM variable.
 *
 * @author olaf boede
 */
public class PmVariableOrAttributeExpr implements OptionalExpression {

  private final NameWithModifier nameWithModifier;
  private Expression concreteExpr;

  public PmVariableOrAttributeExpr(NameWithModifier nameWithModifier) {
    this.nameWithModifier = nameWithModifier;
  }

  @Override
  public Object exec(ExprExecCtxt ctxt) {
    ensureResolver(ctxt);
    try {
      return concreteExpr.exec(ctxt);
    }
    catch (ReflectionException e) {
      // TODO: try it again with a new resolver for the concrete start object.
      throw e;
    }
  }

  @Override
  public void execAssign(ExprExecCtxt ctxt, Object value) {
    ensureResolver(ctxt);
    if (concreteExpr != null) {
      concreteExpr.execAssign(ctxt, value);
    }
  }

  @Override
  public boolean hasNameModifier(Modifier nameModifier) {
    return nameWithModifier.getModifiers().contains(nameModifier);
  }

  private void ensureResolver(ExprExecCtxt ctxt) {
    // TODO: enhance performance by checking if the current expression matches...
    BeanAttrAccessor accessor = null;
    Class<?> classOfCurrentValue = ctxt.getCurrentValue().getClass();
    try {
      String name = nameWithModifier.getName();
      accessor = new BeanAttrAccessorImpl(classOfCurrentValue, name);
      concreteExpr = new AttributeExpr(nameWithModifier, accessor);
    }
    catch (ReflectionException e) {
      if (ctxt instanceof PmExprExecCtxt) {
        concreteExpr = new PmVariableExpr(nameWithModifier);
      }
      else {
        if (! nameWithModifier.isOptional()) {
          throw new ExprExecExeption(ctxt, "Unable to resolve '" + nameWithModifier + "' in class '" +
                                            classOfCurrentValue + "'.", e);
        }
      }
    }
  }

  @Override
  public String toString() {
    return nameWithModifier.toString();
  }

  public static OptionalExpression parse(ParseCtxt ctxt) {
    NameWithModifier n = NameWithModifier.parseNameAndModifier(ctxt);

    if (n == null) {
      return null;
    }

    // Decides if its a variable or Attribut Expression.
    if(n.isVariable()) {
      return new PmVariableExpr(n);
    }
    else {
      return (ParseCtxt.getSyntaxVersion() == SyntaxVersion.VERSION_2)
        // Strict mode. 
        ? new AttributeExpr(n)
        // Compatibility mode.
        : new PmVariableOrAttributeExpr(n);
    }
  }
  
}
