package org.pm4j.core.pm.impl.expr;

import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.core.pm.impl.expr.parser.ParseCtxt;
import org.pm4j.core.pm.impl.expr.parser.ParseException;

/**
 * Parses a name with its modifier specification.
 * <p>
 * Examples:
 * <pre>
 *   expression without modifier:         myVar
 *   expression for a variable:           #myVar
 *   expression for something optional:   (o)myVar
 *   expression for an optional variable: (o)#myVar
 * </pre>
 *
 * @author olaf boede
 */
public class NameWithModifier implements Cloneable {

  private boolean optional;
  private boolean variable;
  private String name;

  public boolean isOptional() {
    return optional;
  }

  public boolean isVariable() {
    return variable;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  public void setVariable(boolean variable) {
    this.variable = variable;
  }

  public String getName() {
    return name;
  }

  @Override
  public NameWithModifier clone() {
    try {
      return (NameWithModifier) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  static enum Modifier {
    OPTIONAL("o") {
      @Override public void applyModifier(NameWithModifier n) {
        n.setOptional(true);
      }
    };

    private final String id;

    private Modifier(String id) {
      this.id = id;
    }

    public abstract void applyModifier(NameWithModifier n);

    static Modifier parse(ParseCtxt ctxt) {
      String s = ctxt.skipBlanksAndReadNameString();

      for (Modifier m : values()) {
        if (m.id.equals(s)) {
          return m;
        }
      }

      throw new ParseException(ctxt, s != null
                              ? "Unknown modifier '" + s + "' found."
                      		    : null);
    }

  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (optional) {
      sb.append("(o)");
    }

    if (variable) {
      sb.append('#');
    }

    sb.append(name);

    return sb.toString();
  }

  /**
   * Parses a name with modifiers.
   * <p>
   * Examples:
   * <ul>
   *   <li>'myName' - a simple name</li>
   *   <li>'#myVar' - a variable</li>
   *   <li>'(o)myName - an optional name</li>
   *   <li>'(o)#myName - an optional variable</li>
   * </ul>
   *
   * @param ctxt The current parse context.
   * @return The parsed syntax element.
   * @throws ParseException if the string at the current parse position is not a name.
   */
  public static NameWithModifier parseNameAndModifier(ParseCtxt ctxt) {
    NameWithModifier n = new NameWithModifier();

    ctxt.skipBlanks();
    if (ctxt.isOnChar('(')) {
      ctxt.readChar('(');

      boolean done = false;
      do {
        Modifier m = Modifier.parse(ctxt);
        m.applyModifier(n);

        ctxt.skipBlanks();
        switch (ctxt.currentChar()) {
          case ')': ctxt.readChar(')');
                    done = true;
                    break;
          case ',': ctxt.readChar(',');
                    break;
          default:  throw new ParseException(ctxt, "Can't interpret character '" +
                                             ctxt.currentChar() + "' in modifier list.");
        }
      }
      while(!done);

      ctxt.skipBlanks();
    }

    if (ctxt.readOptionalChar('#')) {
      n.setVariable(true);
    }

    n.name = ctxt.skipBlanksAndReadNameString();

    if (n.name == null) {
      throw new ParseException(ctxt, "Missing name string.");
    }

    return n;
  }

}