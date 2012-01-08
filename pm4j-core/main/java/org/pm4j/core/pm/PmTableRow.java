package org.pm4j.core.pm;

import java.util.List;

public interface PmTableRow {

  PmTable getPmTable();
  
  List<PmObject> getCells();

  PmObject getCell(int colIdx);
  
  

}
