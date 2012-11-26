package org.pm4j.common.expr;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pm4j.common.expr.NameWithModifier.Modifier;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.expr.parser.ParseException;

/**
 * Invokes a method.
 * <p>
 * TODO: docu!
 *
 * @author olaf boede
 */
public class MethodCallExpr
    extends ExprBase<ExprExecCtxt>
    implements OptionalExpression {

  private static final Object[] EMPTY_OBJ_ARRAY = {};
  private static final Class<?>[] EMPTY_CLASS_ARRAY = {};

  private final NameWithModifier nameWithModifier;
  private Map<Class<?>, Method> classToMethodMap = new ConcurrentHashMap<Class<?>, Method>();
  private Expression[] paramExpressions = {};
  private Class<?>[] paramTypes = {};

  private MethodCallExpr(NameWithModifier nameWithModifier, Collection<Expression> paramExprList) {
    this.nameWithModifier = nameWithModifier.clone();

    int paramCount = paramExprList.size();
    if (paramCount > 0) {
      this.paramExpressions = paramExprList.toArray(new Expression[paramCount]);
      this.paramTypes = new Class<?>[paramCount];
      for (int i=0; i<paramTypes.length; ++i) {
        paramTypes[i] = Object.class;
      }
    }
  }

  /**
   * Parses a method call syntax strings such as: <code>myMethod()</code> or
   * <code>myMethod(parm1, param2)</code>.
   *
   * @param ctxt
   *          The parse context with the current parse position.
   * @return <code>null</code> if there was no method call found at the current
   *         parse position.
   * @throws ParseException
   *           if the syntax of the method call is not ok. E.g. in case of a
   *           missing closing brace.
   */
  public static MethodCallExpr parse(ParseCtxt ctxt) {
    int startPos = ctxt.getPos();
    NameWithModifier n = NameWithModifier.parseNameAndModifier(ctxt);
    List<Expression> paramList = new ArrayList<Expression>();

    if (n == null) {
      return null;
    }

    ctxt.skipBlanks();
    if (!ctxt.isOnChar('(')) {
      ctxt.setPos(startPos);
      return null;
    }

    ctxt.readChar('(');
    ctxt.skipBlanks();

    if (! ctxt.testAndReadChar(')')) {
      while (true) {
        Expression paramExpr = PathExpressionChain.parse(ctxt);
        if (paramExpr == null) {
          throw new ParseException(ctxt, "')' or method parameter expected.");
        }

        paramList.add(paramExpr);

        ctxt.skipBlanks();
        if (ctxt.isOnChar(',')) {
          ctxt.readChar(',');
        }
        else {
          ctxt.readChar(')');
          break;
        }
      }
    }

    return new MethodCallExpr(n, paramList);
  }


  @Override
  protected Object execImpl(ExprExecCtxt ctxt) {
    Object result = null;
    // remember the current ctxt object here, since the parameter evaluation
    // may change it.
    Object objToCallMethodFor = ctxt.getCurrentValue();

    Method m = getMethod(ctxt);

    if (m != null) {
      Object[] paramValues;
      if (paramExpressions.length > 0) {
        paramValues = new Object[paramExpressions.length];
        for (int i=0; i<paramExpressions.length; ++i) {
          ExprExecCtxt paramCtxt = ctxt.makeSubCtxt();
          paramValues[i] = paramExpressions[i].exec(paramCtxt);
        }
      }
      else {
        paramValues = EMPTY_OBJ_ARRAY;
      }

      try {
        result = m.invoke(objToCallMethodFor, paramValues);
      } catch (Exception e) {
        throw new ExprExecExeption(ctxt, "Failed to invoke method.", e);
      }
    }

    return result;
  }

  private static final boolean cacheMethods = true;

  /**
   * Provides the method to call for the current object of the
   * given execution context.
   *
   * @param ctxt The execution context with a current object to call the method for.
   * @return The method instance that matches the class of the current object.
   */
  private Method getMethod(ExprExecCtxt ctxt) {
    Object currentObj = ctxt.getCurrentValue();

    if (currentObj == null) {
      throw new ExprExecExeption(ctxt, "PM in expression context is 'null'.");
    }

    Class<?> objClass = currentObj.getClass();
    Method m = classToMethodMap.get(objClass);

    if (m == null) {
      try {
        if (paramTypes.length == 0) {
          m = objClass.getMethod(nameWithModifier.getName(), EMPTY_CLASS_ARRAY);
          m.setAccessible(true); // XXX olaf: should not be necessary for a public method...
          if (cacheMethods) {
            classToMethodMap.put(objClass, m);
          }
        }
        else {
          // TODO: missing support for polymorph parameter sets.
          //       takes just the first match...
          for (Method mm : objClass.getMethods()) {
            if (mm.getName().equals(nameWithModifier.getName()) &&
                mm.getParameterTypes().length == paramTypes.length) {
              m = mm;
              m.setAccessible(true); // XXX olaf: should not be necessary for a public method...
              if (cacheMethods) {
                classToMethodMap.put(objClass, m);
              }
              break;
            }
          }

          if (m == null &&
              ! hasNameModifier(Modifier.OPTIONAL)) {
            throw new ExprExecExeption(ctxt, "Method '" + nameWithModifier.getName() + "' not found in class: " + objClass.getName());
          }
        }
      } catch (SecurityException e) {
        throw new ExprExecExeption(ctxt, "Unable to access method.", e);
      } catch (NoSuchMethodException e) {
      	// Property does not exist. Check if that's ok.
      	// Consider the old meaning of 'o' which also worked like an 'x'.
    	  if(ParseCtxt.getSyntaxVersion() == SyntaxVersion.VERSION_1) {
              if (!(hasNameModifier(Modifier.OPTIONAL) || hasNameModifier(Modifier.EXISTS_OPTIONALLY))) {
                  throw new ExprExecExeption(ctxt, "Method '" + nameWithModifier.getName() + "' not found in class: " + objClass.getName());
                }
          } else {
              if (!hasNameModifier(Modifier.EXISTS_OPTIONALLY)) {
                  throw new ExprExecExeption(ctxt, "Method '" + nameWithModifier.getName() + "' not found in class: " + objClass.getName());
                }        	
          }
          return null;
      }
    }

    return m;
  }

  @Override
  public boolean hasNameModifier(Modifier nameModifier) {
    return nameWithModifier.getModifiers().contains(nameModifier);
  }

  @Override
  public String toString() {
    return nameWithModifier.toString();
  }

}
