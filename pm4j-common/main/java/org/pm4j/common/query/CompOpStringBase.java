package org.pm4j.common.query;


/**
 * A base class for string specific related compare operators.
 * <p>
 * May contain additional information like {@link #ignoreCase}.
 * <p>
 * TODO olaf: move to CompOpBase
 *
 * @author olaf boede
 */
public abstract class CompOpStringBase extends CompOpBase<String> {

  private boolean ignoreCase = true;

  private boolean ignoreSpaces = true;

  public CompOpStringBase(String name) {
    super(name);
  }

  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  public void setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  public boolean isIgnoreSpaces() {
    return ignoreSpaces;
  }

  public void setIgnoreSpaces(boolean ignoreSpaces) {
    this.ignoreSpaces = ignoreSpaces;
  }
}
