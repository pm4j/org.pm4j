package org.pm4j.core.xml;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTab;
import org.pm4j.core.pm.PmTabSet;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHierarchyCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;
import org.pm4j.core.pm.impl.PmTableUtil;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;
import org.pm4j.core.xml.bean.XmlPmAttr;
import org.pm4j.core.xml.bean.XmlPmCommand;
import org.pm4j.core.xml.bean.XmlPmConversation;
import org.pm4j.core.xml.bean.XmlPmMessage;
import org.pm4j.core.xml.bean.XmlPmObject;
import org.pm4j.core.xml.bean.XmlPmTab;
import org.pm4j.core.xml.bean.XmlPmTabSet;
import org.pm4j.core.xml.bean.XmlPmTable;
import org.pm4j.core.xml.bean.XmlPmTableCol;
import org.pm4j.core.xml.bean.XmlPmTableRow;

/**
 * A visitor that reports the visible state of a PM sub-tree to
 * XML beans.
 * <p>
 * It attempts to report minimal redundancy by not mentioning default information.
 * E.g. the 'not enabled' state of a PM in an read-only tree will not be reported.
 *
 * @author Olaf Boede
 */
public class ToXmlVisitorCallBack implements PmVisitHierarchyCallBack {

  private XmlPmObject xmlRoot, newestXml;
  private Deque<XmlPmObject> parents = new LinkedList<XmlPmObject>();

  @Override
  public PmVisitResult visit(PmObject pm) {
    if (pm instanceof PmAttr) {
      newestXml = visitAttr((PmAttr<?>) pm);
    } else if (pm instanceof PmCommand) {
      newestXml = visitObject(pm, new XmlPmCommand());
    } else if (pm instanceof PmConversation) {
      newestXml = visitObject(pm, new XmlPmConversation());
    } else if (pm instanceof PmTable) {
      newestXml = visitObject(pm, new XmlPmTable());
    } else if (pm instanceof PmTableCol) {
      newestXml = visitObject(pm, new XmlPmTableCol());
    }
    // XML readability: Rows are easier it identify if marked as 'row' element
    else if (PmTableUtil.findIndexOfRowOnCurrentPage(pm) != -1) {
      newestXml = visitTableRow(pm);
    } else if (pm instanceof PmTabSet) {
      newestXml = visitObject(pm, new XmlPmTabSet());
    }
    // XML readability: Tabs (really used within a tab set) are reported as 'tab'
    else if (pm instanceof PmTab && pm.getPmParent() instanceof PmTabSet) {
      newestXml = visitObject(pm, new XmlPmTab());
    } else {
      newestXml = visitObject(pm, new XmlPmObject());
    }

    if (xmlRoot == null) {
      xmlRoot = newestXml;
    }

    return PmVisitResult.CONTINUE;
  }

  protected XmlPmObject visitObject(PmObject pm, XmlPmObject xmlObject) {
    boolean pmReadonly = pm.isPmReadonly();

    if (getParent() != null) {
      getParent().children.add(xmlObject);
    }

    xmlObject.name = pm.getPmName();
    // Enabled is only important if it is different to the read-only tree state.
    // Otherwise it's redundant.
    if (pmReadonly == pm.isPmEnabled()) {
      xmlObject.enabled = pm.isPmEnabled();
    }
    xmlObject.title = pm.getPmTitle();
    xmlObject.tooltip = pm.getPmTooltip();
    xmlObject.icon = pm.getPmIconPath();
    if (!pm.getPmStyleClasses().isEmpty()) {
      xmlObject.styleClass = StringUtils.join(pm.getPmStyleClasses(), ", ");
    }

    // The iteration assumes at the root element a write-enabled tree.
    // Otherwise it will be reported.
    boolean parentReadOnly = (getParent() != null)
          ? pm.getPmParent().isPmReadonly()
          : false;
    // Only read-only switches for a sub-tree area will be reported.
    if (parentReadOnly != pmReadonly) {
      xmlObject.readOnly = pmReadonly;
    }

    for (PmMessage m : PmMessageApi.getMessages(pm)) {
      XmlPmMessage xmlMsg = new XmlPmMessage();
      xmlMsg.severity = m.getSeverity().toString();
      xmlMsg.title = m.getTitle();
      xmlObject.messages.add(xmlMsg);
    }

    return xmlObject;
  }

  protected XmlPmAttr visitAttr(PmAttr<?> pm) {
    XmlPmAttr xmlPmAttr = new XmlPmAttr();
    visitObject(pm, xmlPmAttr);
    List<String> oTitles = PmOptionSetUtil.getOptionTitles(pm.getOptionSet());
    if (!oTitles.isEmpty()) {
      xmlPmAttr.options = StringUtils.join(oTitles, "|");
    }
    xmlPmAttr.value = pm.getValueLocalized();
    return xmlPmAttr;
  }

  protected XmlPmTableRow visitTableRow(PmObject pm) {
    XmlPmTableRow xmlRow = new XmlPmTableRow();
    visitObject(pm, xmlRow);
    return xmlRow;
  }

  @Override
  public PmVisitResult enterChildren(PmObject pmParent, Iterable<PmObject> pmChildren) {
    parents.addLast(newestXml);
    return PmVisitResult.CONTINUE;
  }

  @Override
  public void leaveChildren(PmObject pmParent, Iterable<PmObject> pmChildren) {
    parents.removeLast();
  }

  /**
   * @return the xmlRoot object
   */
  public final XmlPmObject getXmlRoot() {
    return xmlRoot;
  }

  protected final XmlPmObject getParent() {
    return parents.isEmpty()
        ? null
        : parents.getLast();
  }
}
