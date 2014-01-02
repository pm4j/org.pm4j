package org.pm4j.core.pm.remote;

import org.pm4j.core.pm.PmObject;

/**
 * Interface to remote PMs.
 * Usually used for client server communication.
 * <p>
 * Client server communication usually comprises the following tasks/phases:
 * <ol>
 *  <li>attach to a server session (may include osiv support)</li>
 *  <li>resolve server PM</li>
 *  <li>receive values</li>
 *  <li>execute command (includes optional validation)</li>
 *  <li>apply changed server values to the client object</li>
 *  <li>add PM messages to the response</li>
 * </ol>
 * 
 * @author olaf boede
 *
 */
public interface DeprPmRemoteChannel {

  /**
   * Sends the following data to the server:
   * <ol>
   *   <li>The (configurable) content of the given PM</li>
   *   <li>The name of the server command to call.</li>
   * </ol>
   * Receives the following data from the server:
   * <ol>
   *   <li>The (configurable) content of the server PM.</li>
   *   <li>The the PM messages that are generated during command execution.</li>
   * </ol>
   * 
   * @param pmPath
   *          The path of the addressed server PM.
   * @param pm
   *          The client PM to get the values from. It will be updated with the result values of the server PM.
   * @param cmdToCall
   *          Name of the command to call on the server PM.
   */
  void sendValuesAndCallServerCommand(String pmPath, PmObject pm, String cmdToCall);

}