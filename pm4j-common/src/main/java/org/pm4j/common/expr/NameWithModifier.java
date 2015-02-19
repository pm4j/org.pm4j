package org.pm4j.common.expr;

import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.expr.parser.ParseException;

import java.util.HashSet;
import java.util.Set;

/**
 * Parses a name with its modifier specification.
 * <p>
 * Examples:
 * <pre>
 *   expression without modifier:         myVar
 *   expression for a variable:           #myVar
 *   expression for something optional (may be null):   (o)myVar
 *   expression for a field or getter that may exist: (x)myVar
 *   expression for a field or getter that may exist or may be null: (x,o)myVar.x
 *   expression for an optional variable: (o)#myVar
 *   expression with an alias name: (as:myAlias)myVar
 * </pre>
 *
 * @author olaf boede
 */
public class NameWithModifier implements Cloneable {

  /** Delimiter between between multiple modifiers and the closing brace */
  private static final char[] PART_DELIMITER = new char[]{',', ')'};


  private Set<Modifier> modifiers = new HashSet<Modifier>();
  private boolean variable;
  private String name;
  private String alias;

  public Set<Modifier> getModifiers() { return modifiers; }
  public boolean isOptional() { return modifiers.contains(Modifier.OPTIONAL); }
  public boolean isVariable() { return variable; }
  public void setVariable(boolean variable) { this.variable = variable; }
  public String getName() { return name; }
  public String getAlias() { return alias; }
  private void setAlias(String alias) { this.alias = alias; }


  @Override
  public NameWithModifier clone() {
    try {
      return (NameWithModifier) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  public static enum Modifier {
    OPTIONAL("o"),
    EXISTS_OPTIONALLY("x"),
    ALIAS("as:") {
      @Override
      protected boolean applyModifierIfMatching(ParseCtxt ctxt, String nextPart, NameWithModifier n) {
        boolean matches = nextPart.startsWith(getId());
        if ( matches ) {
          if ( n.getAlias() != null ) {
            throw new ParseException(ctxt, "invalid 'as:', alias already set");
          }
          n.modifiers.add(this);

          // as there is always a ':', the 2nd part must exists, it just could be empty
          String[] aliasParts = nextPart.split(":", 2);
          if ( aliasParts.length < 2 || aliasParts[1].isEmpty() ) {
            throw new ParseException(ctxt, "invalid 'as:', alias must not be empty");
          }
          n.setAlias(aliasParts[1]);
        }
        return matches;
      }
    };

    private final String id;

    private Modifier(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    static Modifier parse(ParseCtxt ctxt, NameWithModifier n) {
      String token = ctxt.skipBlanks().readCharsAndAdvanceUntil(PART_DELIMITER);
      return Modifier.byToken(ctxt, token, n);
   }

    private static Modifier byToken(ParseCtxt ctxt, String token, NameWithModifier n) {
      for (Modifier m : values()) {
        if (m.applyModifierIfMatching(ctxt, token, n)) {
          return m;
        }
      }

      throw new ParseException(ctxt, "Unknown modifier '" + token + "' found.");
    }

    protected boolean applyModifierIfMatching(ParseCtxt ctxt, String token, NameWithModifier n) {
      if ( id.equals(token) ) {
        n.modifiers.add(this);
        return true;
      }
      return false;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (!modifiers.isEmpty()) {
      sb.append("(");
      for (Modifier m : Modifier.values()) {
        if (modifiers.contains(m)) {
          sb.append(m.id);
        }
      }
      sb.append(")");
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
        Modifier.parse(ctxt, n);

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