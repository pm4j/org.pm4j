package org.pm4j.deprecated.core.pm.filter.impl;

@Deprecated
public abstract class DeprCompOpStringBase extends DeprCompOpBase<String> {

  private boolean ignoreCase = true;

  private boolean ignoreSpaces = true;

  public DeprCompOpStringBase(String name, String title) {
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
