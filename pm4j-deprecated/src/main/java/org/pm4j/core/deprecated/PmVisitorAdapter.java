package org.pm4j.core.deprecated;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmLabel;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTable2;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.PmTreeNode;
import org.pm4j.core.pm.PmVisitor;

@Deprecated
public class PmVisitorAdapter implements PmVisitor {

  @Override
  public void visit(PmAttr<?> attr) {
    onVisit(attr);
  }

  @Override
  public void visit(PmElement element) {
    onVisit(element);
  }

  @Override
  public void visit(PmCommand command) {
    onVisit(command);
  }

  @Override
  public void visit(PmLabel label) {
    onVisit(label);
  }

  @Override
  public void visit(@SuppressWarnings("rawtypes") PmTable table) {
    onVisit(table);
  }

  @Override
  public void visit(PmTable2<?> table) {
    onVisit(table);
  }

  @Override
  public void visit(PmTableCol tableCol) {
    onVisit(tableCol);
  }

  @Override
  public void visit(PmObject pm) {
    onVisit(pm);
  }

  /**
   * Default call back for common {@link PmObject} related handling.
   * @param pm
   */
  protected void onVisit(PmObject pm) {
    if (pm instanceof PmTreeNode) {
      onVisitPmTreeNode((PmTreeNode)pm);
    }
  }

  /**
   * Call back for {@link PmTreeNode} related handling.
   *
   * @param pm
   */
  protected void onVisitPmTreeNode(PmTreeNode pm) {
  }

}
