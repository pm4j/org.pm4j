package org.pm4j.core.pm.serialization;

import java.util.HashSet;
import java.util.Set;

import org.pm4j.core.pm.PmAspect;

public class PmContentCfg {

  private boolean onlyVisibleItems = true;
  private Set<PmAspect> aspects = new HashSet<PmAspect>();
  
  public PmContentCfg(PmAspect... aspectArray) {
    addAspects(aspectArray);
  }
  
  public void addAspects(PmAspect... aspectArray) {
    for (PmAspect a : aspectArray) {
      aspects.add(a);
    }
  }

  public boolean isOnlyVisibleItems() {
    return onlyVisibleItems;
  }

  public void setOnlyVisibleItems(boolean onlyVisibleItems) {
    this.onlyVisibleItems = onlyVisibleItems;
  }
  
  public boolean hasAspect(PmAspect aspect) {
    return aspects.contains(aspect);
  }

  public Set<PmAspect> getAspects() {
    return aspects;
  }
}
