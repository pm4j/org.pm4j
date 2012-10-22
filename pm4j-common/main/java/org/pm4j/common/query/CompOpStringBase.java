package org.pm4j.common.query;


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
