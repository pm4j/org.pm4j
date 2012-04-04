package org.pm4j.web;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmObject;

/**
 * Some helper methods for web view creation.
 *
 * @author olaf boede
 */
public final class PmViewUtil {

  /**
   * Provides the a space separated lists of style classes for the given PM.
   *
   * @param viewMap
   *          An optional mapper that generates style classes according to PM
   *          states.
   * @param pm
   *          The PM that provides the style classes (by calling {@link PmObject#getPmStyleClasses()}.
   * @param fixStyleClass
   *          An optional string with fix style definitions to be added to the PM provided style classes.
   * @return All style classes to be applied to the PM.
   */
  public static String pmStyleClasses(PmObject pm, String fixStyleClass) {
    if (pm != null) {
      Set<String> styleClasses = pm.getPmStyleClasses();
      if (! styleClasses.isEmpty()) {
        StringBuilder sb = new StringBuilder(StringUtils.join(styleClasses, " "));
        if (StringUtils.isNotBlank(fixStyleClass)) {
          sb.append(" ").append(fixStyleClass);
        }
        return sb.toString();
      }
    }

    return StringUtils.defaultString(fixStyleClass);
  }

  /**
   * Generates a list of style classes based on a fix definition and some

   * dynamic logic that depends on the state of the given presentation model.
   *
   * @param viewMap
   *          An optional mapper that generates style classes according to PM
   *          states.
   * @param pm
   *          The PM to render.
   * @param fixStyleClass
   *          A set of fix style definitions to be applied to the PM.
   * @return All style classes to be applied to the PM.
   * @deprecated Please use {@link #pmStyleClasses(PmObject, String)}.
   */
  @Deprecated
  public static String styleClassForPm(PmViewMapper viewMap, PmObject pm, String fixStyleClass) {
	  String result = StringUtils.defaultString(fixStyleClass);

	  if ((viewMap != null) && (pm != null)) {
	    // FIXME olaf: handling for labels etc. not yet implemented
	    if (pm instanceof PmAttr) {
  	    String dynStyleClass = viewMap.styleClassForPm((PmAttr<?>)pm);

  	    if (! StringUtils.isBlank(dynStyleClass)) {
  	      result += " " + dynStyleClass.trim();
  	    }
	    }else if(pm instanceof PmCommand){
        String dynStyleClass = viewMap.styleClassForPm((PmCommand)pm);

        if (! StringUtils.isBlank(dynStyleClass)) {
          result += " " + dynStyleClass.trim();
        }
	    }
	  }

	  return result;
	}

  @Deprecated
  public static String styleClassForPmMsg(PmViewMapper viewMap, PmMessage pmMsg) {
    if ((viewMap != null) && (pmMsg != null)) {
      return viewMap.styleClassForPmMsg(pmMsg);
    }
    else {
      return "";
    }
  }

}
