package org.pm4j.core.pm.serialization;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.pm4j.core.pm.PmAspect;

/**
 * DTO for the content of a single PM.
 * 
 * @author olaf boede
 */
public class PmContentContainer implements Serializable {
  private static final long serialVersionUID = 1L;

  /** The path of the PM that sends/receives the content. */
  private String pmPath;
  
  /** Stores {@link PmAspect}s like value, title etc. */
  private Map<PmAspect, Serializable> aspectMap = Collections.emptyMap();
  
  /** Stores content of named objects. The attributes and named sub-elements are usually stored here. */
  private Map<String, PmContentContainer> namedChildContentMap = Collections.emptyMap();
  
  public void addAspect(PmAspect aspect, Serializable value) {
    if (aspectMap.size() == 0) {
      aspectMap = new HashMap<PmAspect, Serializable>();
    }
    aspectMap.put(aspect, value);
  }
  
  public Serializable getAspect(PmAspect aspect) {
    return aspectMap.get(aspect);
  }

  public PmContentContainer addNamedChildContent(String childName) {
    if (namedChildContentMap.size() == 0) {
      namedChildContentMap = new HashMap<String, PmContentContainer>();
    }
    PmContentContainer c = new PmContentContainer();
    namedChildContentMap.put(childName, c);
    return c;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(aspectMap.toString());
    return sb.toString();
  }

  public Map<PmAspect, Serializable> getAspectMap() {
    return aspectMap;
  }

  public Map<String, PmContentContainer> getNamedChildContentMap() {
    return namedChildContentMap;
  }

  public String getPmPath() {
    return pmPath;
  }

  public void setPmPath(String pmPath) {
    this.pmPath = pmPath;
  }
  
}
