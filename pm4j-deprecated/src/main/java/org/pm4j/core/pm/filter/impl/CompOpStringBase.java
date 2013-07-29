package org.pm4j.core.pm.filter.impl;


public abstract class CompOpStringBase extends CompOpBase<String> {

  private boolean ignoreCase = true;

  private boolean ignoreSpaces = true;

  public CompOpStringBase(String name, String title) {
    super(name, title);
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
