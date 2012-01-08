package org.pm4j.web;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage;

/**
 * Generates additional style classes according to the state of the given PM.
 *
 * @author olaf boede
 */
public interface PmViewMapper {

	/**
	 * Provides addional style classes for rendering the given PM.
	 * TODOC:
	 * @param pm
	 * @return
	 */
  String styleClassForPm(PmAttr<?> pmAttr);


  String styleClassForPm(PmCommand pmCommand);

  String styleClassForPmMsg(PmMessage pmMsg);

}
