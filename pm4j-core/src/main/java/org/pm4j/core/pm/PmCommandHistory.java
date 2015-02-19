package org.pm4j.core.pm;

import java.util.List;

/**
 * Provides the set of undo/redo-able commands.
 *
 * @author olaf boede
 */
public interface PmCommandHistory {

  List<PmCommand> getUndoList();

  List<PmCommand> getRedoList();

  void redoNext();

  void undoNext();
  
  /**
   * Informs about a just executed command. The session may use it to maintain
   * an undo list.
   *
   * @param command
   *          The just executed command.
   */
  void commandDone(PmCommand command);


}
