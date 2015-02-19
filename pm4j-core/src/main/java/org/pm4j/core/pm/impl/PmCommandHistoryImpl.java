package org.pm4j.core.pm.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandHistory;

public class PmCommandHistoryImpl implements PmCommandHistory {

  private int maxUndoItems = 3;
  private int maxRedoItems = 3;
  private Deque<PmCommand> undoList = new ArrayDeque<PmCommand>();
  private Deque<PmCommand> redoList = new ArrayDeque<PmCommand>();

  @Override
  public List<PmCommand> getRedoList() {
    return new ArrayList<PmCommand>(redoList);
  }

  @Override
  public List<PmCommand> getUndoList() {
    return new ArrayList<PmCommand>(undoList);
  }

  public void clear() {
    undoList.clear();
    redoList.clear();
  }

  public void setMaxRedoItems(int count) {
    assert count > -1;
    maxRedoItems = count;
  }

  public void setMaxUndoItems(int count) {
    assert count > -1;
    maxUndoItems = count;
  }

  @Override
  public void redoNext() {
    PmCommand cmd = redoList.peekFirst();
    if (cmd != null) {
      cmd.doIt();
      redoList.removeFirst();
    }
  }

  @Override
  public void undoNext() {
    PmCommand cmd = undoList.peekFirst();
    if (cmd != null) {
      PmCommand doneUndoCmd = ((PmCommandImpl)cmd.getUndoCommand()).doIt(false);
      undoList.removeFirst();
      addToRedoList(doneUndoCmd.getUndoCommand());
    }
  }

  @Override
  public void commandDone(PmCommand command) {
    if (command.getUndoCommand() != null) {
      addToUndoList(command);
    }
    else {
      clear();
    }
  }

  private void addToRedoList(PmCommand cmd) {
    if (cmd != null) {
      while (redoList.size() >= maxRedoItems) {
        redoList.removeLast();
      }

      redoList.addFirst(cmd);
    }
  }

  private void addToUndoList(PmCommand cmd) {
    while (undoList.size() >= maxUndoItems) {
      undoList.removeLast();
    }

    undoList.addFirst(cmd);
  }

}
