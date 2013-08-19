package org.pm4j.core.pm;

import junit.framework.TestCase;

import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmCommandSetValueUndoRedoTest extends TestCase {

  public void testDoUndo() {
    TestPm pm = new TestPm();

    pm.string.setValue("123");

    assertEquals("123", pm.string.getValue());
    assertEquals(1, pm.getPmCommandHistory().getUndoList().size());
    assertEquals(0, pm.getPmCommandHistory().getRedoList().size());

    pm.getPmCommandHistory().undoNext();
    assertEquals(null, pm.string.getValue());

// TODO olaf: re-enable undo!
//    assertEquals(0, pm.getPmCommandHistory().getUndoList().size());
//    assertEquals(1, pm.getPmCommandHistory().getRedoList().size());
//
//    pm.getPmCommandHistory().redoNext();
//    assertEquals("123", pm.string.getValue());
//    assertEquals(1, pm.getPmCommandHistory().getUndoList().size());
//    assertEquals(0, pm.getPmCommandHistory().getRedoList().size());
//
//    pm.getPmCommandHistory().undoNext();
//    assertEquals(null, pm.string.getValue());
//    assertEquals(0, pm.getPmCommandHistory().getUndoList().size());
//    assertEquals(1, pm.getPmCommandHistory().getRedoList().size());
  }

  public static class TestPm extends PmConversationImpl {
    public final PmAttrString string = new PmAttrStringImpl(this);
  }

}
