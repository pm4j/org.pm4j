package org.pm4j.jsf;

/**
 * Some constants that are used in the pm4j JSF context.
 *
 * @author olaf boede
 */
public abstract class Pm4jJsfConstants {

  /**
   * A parameter that is transmitted by a4j:keyLink tags.<br>
   * It sends the pmKey of the command's parent element to the server.<br>
   * This is usually used in list scenarios with list sets that are changed on
   * the server side. It allows to get some information about the really
   * referenced server list item, even if the item was scrolled to another
   * list position.
   */
  public static final String CMD_PARAM_ELEM_KEY = "cmd.parent.key";

}
