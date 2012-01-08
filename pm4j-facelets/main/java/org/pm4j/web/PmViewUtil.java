package org.pm4j.web;

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
   */
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

  public static String styleClassForPmMsg(PmViewMapper viewMap, PmMessage pmMsg) {
    if ((viewMap != null) && (pmMsg != null)) {
      return viewMap.styleClassForPmMsg(pmMsg);
    }
    else {
      return "";
    }
  }

}
