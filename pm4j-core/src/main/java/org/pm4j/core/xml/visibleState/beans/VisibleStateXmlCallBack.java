package org.pm4j.core.xml.visibleState.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import org.pm4j.core.pm.impl.PmTableUtil2;
import org.pm4j.core.pm.impl.PmVisitorImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;
import org.pm4j.core.xml.visibleState.VisibleStateAspect;
import org.pm4j.core.xml.visibleState.VisibleStateAspectMatcher;

/**
 * A visitor that reports the visible state of a PM sub-tree to
 * XML beans.
 * <p>
 * It attempts to report minimal redundancy by not mentioning default information.
 * E.g. the 'not enabled' state of a PM in an read-only tree will not be reported.
 *
 * @author Olaf Boede
 */
public class VisibleStateXmlCallBack implements PmVisitHierarchyCallBack {

  private XmlPmObjectBase xmlRoot, newestXml;
  private Deque<XmlPmObjectBase> parents = new LinkedList<XmlPmObjectBase>();
  private Collection<VisibleStateAspectMatcher> excludedProperties = new ArrayList<VisibleStateAspectMatcher>();

  /**
   * Defines filters for properties to exclude from the xml.
   * <p>
   * If you need to exclude complete PMs, please use {@link PmVisitorImpl#exclude(Collection)}.
   *
   * @param excludedProperties Match rules for the properties to exclude.
   * @return self reference for fluent programming.
   */
  public VisibleStateXmlCallBack exclude(Collection<VisibleStateAspectMatcher> excludedProperties) {
    if (excludedProperties != null) {
      this.excludedProperties.addAll(excludedProperties);
    }
    return this;
  }

  /**
   * Defines filters for properties to exclude from the xml.
   * <p>
   * If you need to exclude complete PMs, please use {@link PmVisitorImpl#exclude(Collection)}.
   *
   * @param excludedProperties Match rules for the properties to exclude.
   * @return self reference for fluent programming.
   */
  public VisibleStateXmlCallBack exclude(VisibleStateAspectMatcher... excludedProperties) {
    this.excludedProperties.addAll(Arrays.asList(excludedProperties));
    return this;
  }

  @Override
  public PmVisitResult visit(PmObject pm) {
    Set<VisibleStateAspect> hiddenProps = getHiddenProps(pm);

    if (pm instanceof PmAttr) {
      newestXml = visitAttr((PmAttr<?>) pm, hiddenProps);
    } else if (pm instanceof PmCommand) {
      newestXml = visitObject(pm, new XmlPmCommand(), hiddenProps);
    } else if (pm instanceof PmConversation) {
      newestXml = visitObject(pm, new XmlPmConversation(), hiddenProps);
    } else if (pm instanceof PmTable) {
      newestXml = visitTable((PmTable<?>) pm, hiddenProps);
    } else if (pm instanceof PmTableCol) {
      newestXml = visitObject(pm, new XmlPmTableCol(), hiddenProps);
    }
    // XML readability: Rows are easier it identify if marked as 'row' element
    else if (PmTableUtil2.findIndexOfRowOnCurrentPage(pm) != -1) {
      newestXml = visitObject(pm, new XmlPmTableRow(), hiddenProps);
    } else if (pm instanceof PmTabSet) {
      newestXml = visitObject(pm, new XmlPmTabSet(), hiddenProps);
    } else {
      newestXml = visitObject(pm, new XmlPmObject(), hiddenProps);
    }

    if (xmlRoot == null) {
      xmlRoot = newestXml;
    }

    return hiddenProps.contains(VisibleStateAspect.CHILDREN)
        ? PmVisitResult.SKIP_CHILDREN
        : PmVisitResult.CONTINUE;
  }

  // XXX oboede: change to positive logic.
  private Set<VisibleStateAspect> getHiddenProps(PmObject pm) {
    Set<VisibleStateAspect> props = new HashSet<VisibleStateAspect>();
    for (VisibleStateAspectMatcher m : excludedProperties) {
      if (m.getPmMatcher().doesMatch(pm)) {
        props.addAll(m.getProperties());
      }
    }
    return props;
  }

  private XmlPmObjectBase visitObject(PmObject pm, XmlPmObjectBase xmlObject, Set<VisibleStateAspect> hiddenProps) {
    if (getParent() != null) {
      getParent().children.add(xmlObject);
    }

    if (!hiddenProps.contains(VisibleStateAspect.NAME)) {
      xmlObject.name = pm.getPmName();
    }

    if (!hiddenProps.contains(VisibleStateAspect.IS_TAB)) {
      if (pm instanceof PmTab && pm.getPmParent() instanceof PmTabSet) {
        xmlObject.isTab = Boolean.TRUE;
      }
    }

    // TODO: just report it for attr, command and tab
    if (!hiddenProps.contains(VisibleStateAspect.ENABLED)) {
      if (!pm.isPmEnabled()) {
        xmlObject.enabled = Boolean.FALSE;
      }
    }

    if (!hiddenProps.contains(VisibleStateAspect.TITLE)) {
      if (!StringUtils.isBlank(pm.getPmTitle())) {
        xmlObject.title = pm.getPmTitle();
      }
    }

    if (!hiddenProps.contains(VisibleStateAspect.SHORT_TITLE)) {
      if (!StringUtils.equals(pm.getPmTitle(), pm.getPmShortTitle())) {
        xmlObject.shortTitle = pm.getPmShortTitle();
      }
    }

    if (!hiddenProps.contains(VisibleStateAspect.TOOLTIP)) {
      if (!StringUtils.isBlank(pm.getPmTooltip())) {
        xmlObject.tooltip = pm.getPmTooltip();
      }
    }

    if (!hiddenProps.contains(VisibleStateAspect.ICON)) {
      xmlObject.icon = pm.getPmIconPath();
    }

    if (!hiddenProps.contains(VisibleStateAspect.STYLECLASS)) {
        if (!pm.getPmStyleClasses().isEmpty()) {
        xmlObject.styleClass = StringUtils.join(pm.getPmStyleClasses(), ", ");
      }
    }

    if (!hiddenProps.contains(VisibleStateAspect.MESSAGES)) {
      for (PmMessage m : PmMessageApi.getMessages(pm)) {
        XmlPmMessage xmlMsg = new XmlPmMessage();
        xmlMsg.severity = m.getSeverity().toString();
        xmlMsg.title = m.getTitle();
        xmlObject.messages.add(xmlMsg);
      }
    }

    return xmlObject;
  }

  private XmlPmObjectBase visitAttr(PmAttr<?> pm, Set<VisibleStateAspect> hideProps) {
    XmlPmAttr xmlPmAttr = new XmlPmAttr();

    if (!hideProps.contains(VisibleStateAspect.OPTIONS)) {
      List<String> oTitles = PmOptionSetUtil.getOptionTitles(pm.getOptionSet());
      if (!oTitles.isEmpty()) {
        xmlPmAttr.options = StringUtils.join(oTitles, "|");
      }
    }

    if (!hideProps.contains(VisibleStateAspect.VALUE)) {
      xmlPmAttr.value = pm.getValueLocalized();
    }
    return visitObject(pm, xmlPmAttr, hideProps);
  }

  private XmlPmObjectBase visitTable(PmTable<?> pm, Set<VisibleStateAspect> hideProps) {
    XmlPmTable xmlPmTable = new XmlPmTable();

    if (!hideProps.contains(VisibleStateAspect.NUM_OF_ROWS)) {
      xmlPmTable.rows = pm.getTotalNumOfPmRows();
    }
    return visitObject(pm, xmlPmTable, hideProps);
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
  public final XmlPmObjectBase getXmlRoot() {
    return xmlRoot;
  }

  protected final XmlPmObjectBase getParent() {
    return parents.isEmpty()
        ? null
        : parents.getLast();
  }
}
