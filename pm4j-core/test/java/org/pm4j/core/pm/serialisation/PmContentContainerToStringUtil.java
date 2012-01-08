package org.pm4j.core.pm.serialisation;

import java.util.Map;

import org.pm4j.core.pm.serialization.PmContentContainer;

public class PmContentContainerToStringUtil {

  public static String toString(PmContentContainer content) {
    return toString(new StringBuilder(), content, 0).toString();
  }
  
  public static StringBuilder toString(StringBuilder sb, PmContentContainer content, int indent) {
    sb.append(content.getAspectMap());
    
    for (Map.Entry<String, PmContentContainer> e : content.getNamedChildContentMap().entrySet()) {
      if (sb.length() != 0)
        sb.append("\n");
      
      indent(sb, indent).append(e.getKey()).append(": ");
      toString(sb, e.getValue(), indent+2);
    }
    
    return sb;
  }
  
  private static StringBuilder indent(StringBuilder sb, int indent) {
    for (int i=0; i<indent; ++i)
      sb.append(' ');
    return sb;
  }
  
}
