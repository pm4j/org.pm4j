package org.pm4j.core.pm.impl.pathresolver;

import org.pm4j.common.expr.Expression.SyntaxVersion;

/**
 * Common implementation parts for concrete resolver classes.
 *
 * @author olaf boede
 */
public abstract class PathResolverBase implements PathResolver {

  private boolean nullAllowed = true;
  private final SyntaxVersion syntaxVersion;

  public PathResolverBase(SyntaxVersion syntaxVersion) {
    this.syntaxVersion = syntaxVersion;
  }

  @Override
  public boolean isNullAllowed() {
    return nullAllowed;
  }

  @Override
  public void setNullAllowed(boolean allowed) {
    this.nullAllowed = allowed;
  }

  /**
   * @return the syntaxVersion
   */
  public SyntaxVersion getSyntaxVersion() {
    return syntaxVersion;
  }

}
