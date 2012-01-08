package org.pm4j.core.pm;

import java.util.List;

// TODO olaf: Suggestion for a full row PM that supports positionable cells as well as row selection.
public interface PmTableRowPm extends PmElement {

  List<PmObject> getCells();

  PmObject getCell(int colIdx);

  PmAttrBoolean getSelected();
}
