package org.pm4j.common.expr;

import org.pm4j.common.expr.NameWithModifier.Modifier;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.util.reflection.BeanAttrAccessor;
import org.pm4j.common.util.reflection.BeanAttrAccessorImpl;
import org.pm4j.common.util.reflection.ReflectionException;

/**
 * A path that uses a public field or getter/setter to resolve the path string.
 *
 * @author olaf boede
 */
public class AttributeExpr extends OptionalExpressionBase<ExprExecCtxt> {

  private BeanAttrAccessor accessor;

  public AttributeExpr(ParseCtxt ctxt, NameWithModifier name, BeanAttrAccessor accessor) {
    this(ctxt, name);
    this.accessor = accessor;
  }

  public AttributeExpr(ParseCtxt ctxt, NameWithModifier name) {
    super(ctxt, name);
  }

  public AttributeExpr(SyntaxVersion syntaxVersion, NameWithModifier name, BeanAttrAccessor accessor) {
    this(syntaxVersion, name);
    this.accessor = accessor;
  }

  public AttributeExpr(SyntaxVersion syntaxVersion, NameWithModifier name) {
    super(syntaxVersion, name);
  }

  @Override
  public Object execImpl(ExprExecCtxt ctxt) {
    BeanAttrAccessor accessor = ensureAccessor(ctxt);
    if (accessor == null) {
      return null;
    }

    Object currentObj = ctxt.getCurrentValue();

    if (currentObj == null) {
      if (!hasNameModifier(Modifier.OPTIONAL)) {
        throw new ExprExecExeption(ctxt, "Unable to resolve expression part '" + nameWithModifier + "' on a 'null' value.");
      } else {
        return null;
      }
    }

    return accessor.getBeanAttrValue(currentObj);
  }

  @Override
  protected void execAssignImpl(ExprExecCtxt ctxt, Object value) {
    ensureAccessor(ctxt).setBeanAttrValue(ctxt.getCurrentValue(), value);
  }

  private BeanAttrAccessor ensureAccessor(ExprExecCtxt ctxt) {
    // FIXME: check in addition the used class of the accessor!
    if (accessor == null) {
      try {
        accessor = new BeanAttrAccessorImpl(ctxt.getCurrentValue().getClass(), nameWithModifier.getName());
      } catch (ReflectionException e) {
    	// Property does not exist. Check if that's ok.
    	// Consider the old meaning of 'o' which also worked like an 'x'.
        if(getSyntaxVersion() == SyntaxVersion.VERSION_1) {
          if (!(hasNameModifier(Modifier.OPTIONAL) || hasNameModifier(Modifier.EXISTS_OPTIONALLY))) {
              throw new ExprExecExeption(ctxt, "Unable to resolve expression part '" + nameWithModifier + "'.", e);
          }
        } else {
          if (!hasNameModifier(Modifier.EXISTS_OPTIONALLY)) {
              throw new ExprExecExeption(ctxt, "Unable to resolve expression part '" + nameWithModifier + "'.", e);
          }
        }
        return null;
      }
    }
    return accessor;
  }

  public static AttributeExpr parse(ParseCtxt ctxt) {
    NameWithModifier n = NameWithModifier.parseNameAndModifier(ctxt);
    return (n != null) ? new AttributeExpr(ctxt, n) : null;
  }

}
