package org.pm4j.core.pm;


public interface PmVisitor {
  void visit(PmAttr<?> attr);
  void visit(PmElement element);
  void visit(PmCommand command);
  void visit(PmLabel label);
  void visit(PmTable table);
  void visit(PmTable2<?> table);
  void visit(PmTableCol tableCol);
  /**
   * Fallback-callback for other PM types.
   * @param pm The visited PM.
   */
  void visit(PmObject pm);
}
