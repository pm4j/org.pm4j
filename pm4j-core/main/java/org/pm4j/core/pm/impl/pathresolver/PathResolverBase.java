package org.pm4j.core.pm.impl.pathresolver;

/**
 * Common implementation parts for concrete resolver classes.
 *
 * @author olaf boede
 */
public abstract class PathResolverBase implements PathResolver {

  private boolean nullAllowed = true;

  @Override
  public boolean isNullAllowed() {
    return nullAllowed;
  }

  @Override
  public void setNullAllowed(boolean allowed) {
    this.nullAllowed = allowed;
  }

}
