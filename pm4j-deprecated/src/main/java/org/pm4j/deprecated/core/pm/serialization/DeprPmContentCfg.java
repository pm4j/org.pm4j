package org.pm4j.deprecated.core.pm.serialization;

import java.util.HashSet;
import java.util.Set;

import org.pm4j.deprecated.core.pm.DeprPmAspect;

public class DeprPmContentCfg {

  private boolean onlyVisibleItems = true;
  private Set<DeprPmAspect> aspects = new HashSet<DeprPmAspect>();
  
  public DeprPmContentCfg(DeprPmAspect... aspectArray) {
    addAspects(aspectArray);
  }
  
  public void addAspects(DeprPmAspect... aspectArray) {
    for (DeprPmAspect a : aspectArray) {
      aspects.add(a);
    }
  }

  public boolean isOnlyVisibleItems() {
    return onlyVisibleItems;
  }

  public void setOnlyVisibleItems(boolean onlyVisibleItems) {
    this.onlyVisibleItems = onlyVisibleItems;
  }
  
  public boolean hasAspect(DeprPmAspect aspect) {
    return aspects.contains(aspect);
  }

  public Set<DeprPmAspect> getAspects() {
    return aspects;
  }
}
