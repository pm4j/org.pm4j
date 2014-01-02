package org.pm4j.core.pm.serialization;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.pm4j.core.pm.DeprPmAspect;

/**
 * DTO for the content of a single PM.
 * 
 * @author olaf boede
 */
public class DeprPmContentContainer implements Serializable {
  private static final long serialVersionUID = 1L;

  /** The path of the PM that sends/receives the content. */
  private String pmPath;
  
  /** Stores {@link DeprPmAspect}s like value, title etc. */
  private Map<DeprPmAspect, Serializable> aspectMap = Collections.emptyMap();
  
  /** Stores content of named objects. The attributes and named sub-elements are usually stored here. */
  private Map<String, DeprPmContentContainer> namedChildContentMap = Collections.emptyMap();
  
  public void addAspect(DeprPmAspect aspect, Serializable value) {
    if (aspectMap.size() == 0) {
      aspectMap = new HashMap<DeprPmAspect, Serializable>();
    }
    aspectMap.put(aspect, value);
  }
  
  public Serializable getAspect(DeprPmAspect aspect) {
    return aspectMap.get(aspect);
  }
  
  public void initNamedChildContentMap() {
    if (namedChildContentMap.size() == 0) {
      namedChildContentMap = new HashMap<String, DeprPmContentContainer>();
    }
  }

  public DeprPmContentContainer addNamedChildContent(String childName) {
    initNamedChildContentMap();
    DeprPmContentContainer c = new DeprPmContentContainer();
    namedChildContentMap.put(childName, c);
    return c;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(aspectMap.toString());
    return sb.toString();
  }

  public Map<DeprPmAspect, Serializable> getAspectMap() {
    return aspectMap;
  }

  public Map<String, DeprPmContentContainer> getNamedChildContentMap() {
    return namedChildContentMap;
  }

  public String getPmPath() {
    return pmPath;
  }

  public void setPmPath(String pmPath) {
    this.pmPath = pmPath;
  }
  
}
