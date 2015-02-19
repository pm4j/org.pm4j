package org.pm4j.tools.scaffold;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.impl.ResKeyUtil;

/**
 * Helper class that may be used to generate default resource file content.
 * <p>
 * Uses the names of public field definitions.
 * 
 * @author olaf boede
 */
public class ResKeyReporter {

  /**
   * Postfix for PM class names.
   */
  private String pmPostfix = "Pm";
  
  public String reportResKeysToString(String... classNames) {
    Class<?>[] classes = new Class<?>[classNames.length];
    for (int i=0; i<classNames.length; ++i) {
      String clsName = classNames[i];
      try {
        classes[i] = Class.forName(clsName);
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("Class not found: " + clsName);
      }
    }
    return reportResKeysToString(classes);
  }
    
  public String reportResKeysToString(Class<?>... classes) {
    StringBuilder sb = new StringBuilder(classes.length * 300);
    for (Class<?> cls : classes) {
      String clsResKey = stripPmPostfix(ResKeyUtil.shortResKeyForClass(cls));
      String clsDefaultTitle = StringUtils.capitalize(clsResKey);
      
      sb.append(clsResKey+'='+clsDefaultTitle);
      sb.append('\n');
      for (String fName : fieldResKeys(cls)) {
        sb.append(clsResKey+'.'+fName+'='+StringUtils.capitalize(fName)+'\n');
      }
      sb.append('\n');
    }
    return sb.toString();
  }
   
  private String stripPmPostfix(String string) {
    if (string.endsWith(pmPostfix)) {
      return string.substring(0, string.length()-pmPostfix.length());
    }
    else {
      return string;
    }
  }
  
  public List<String> fieldResKeys(Class<?> cls) {
    List<String> keys = new ArrayList<String>();
    for (Field f : cls.getFields()) {
      if (PmAttr.class.isAssignableFrom(f.getType())) {
        keys.add(f.getName());
      }
    }
    return keys;
  }
  
  public static String reportKeys(Class<?>... classes) {
    String report = new ResKeyReporter().reportResKeysToString(classes);
    return report;
  }
  
  public static void main(String[] args) {
    ResKeyReporter reporter = new ResKeyReporter();
    String report = reporter.reportResKeysToString(args);
    System.out.println(report);
  }

  public void setPmPostfix(String pmPostfix) {
    this.pmPostfix = pmPostfix;
  }
  
}
