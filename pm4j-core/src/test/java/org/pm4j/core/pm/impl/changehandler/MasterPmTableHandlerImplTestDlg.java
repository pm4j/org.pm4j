package org.pm4j.core.pm.impl.changehandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pm4j.common.selection.SelectMode;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;
import org.pm4j.tools.test._RecordingPmEventListener;
import org.pm4j.tools.test._RecordingPropertyChangeListener;

/**
 * A test dialog that helps to demonstrate and test
 * {@link MasterPmTableHandlerImpl} in combination with a
 * {@link DetailsPmBeanHandlerImpl}.
 *
 * @author Olaf Boede
 */
public class MasterPmTableHandlerImplTestDlg extends PmConversationImpl {

  @PmTableCfg(valuePath="masterTableBeans", rowSelectMode = SelectMode.SINGLE)
  public final BeanTablePm masterTable = new BeanTablePm(this);

  @PmBeanCfg(beanClass=Bean.class)
  public final DetailsAreaPm detailsArea = new DetailsAreaPm(this);

  @PmTableCfg(valuePath="masterTable.(o)masterRowPmBean.items", rowSelectMode = SelectMode.SINGLE)
  public final BeanTablePm detailsTable = new BeanTablePm(this);

  @PmTableCfg(valuePath="detailsTable.(o)masterRowPmBean.items")
  public final BeanTablePm detailsDetailsTable = new BeanTablePm(this);

  public List<Bean> masterTableBeans = new ArrayList<Bean>(Arrays.asList(
      new Bean("a", new Bean("a.1", new Bean("a.1.1"))),
      new Bean("b", new Bean("b.1", new Bean("b.1.1")))));

  final InstrumentedDetailsPmBeanHandlerImpl detailsAreaHandler = new InstrumentedDetailsPmBeanHandlerImpl(detailsArea);
  final InstrumentedDetailsPmTableToTableHandler detailsTableHandler = new InstrumentedDetailsPmTableToTableHandler(masterTable, detailsTable);
  final InstrumentedDetailsPmTableToTableHandler detailsDetailsTableHandler = new InstrumentedDetailsPmTableToTableHandler(detailsTable, detailsDetailsTable);
  MasterPmHandler masterTableHandler;
  MasterPmHandler detailsAsMasterHandler;

  // -- Test infrastructure --

  @Override
  protected void onPmInit() {
    super.onPmInit();
    // XXX late ctor call because it currently gets the pageable collection.
    masterTableHandler = new MasterPmTableHandlerImpl<Bean>(masterTable, detailsAreaHandler, detailsTableHandler);
    detailsAsMasterHandler = new MasterPmTableHandlerImpl<Bean>(detailsTable, detailsDetailsTableHandler);

    masterTableHandler.startObservers();
    detailsAsMasterHandler.startObservers();
  }

  void cleanCallCounter() {
    masterTable.eventCalls.clear();

    detailsArea.eventCalls.clear();
    detailsAreaHandler.detailsCalls.clear();

    detailsTable.eventCalls.clear();
    detailsTableHandler.detailsCalls.clear();

    detailsDetailsTable.eventCalls.clear();
    detailsDetailsTableHandler.detailsCalls.clear();
  }

  public static class InstrumentedDetailsPmBeanHandlerImpl extends DetailsPmBeanHandlerImpl<Bean> {

    final DetailsHandlerCalls detailsCalls;

    public InstrumentedDetailsPmBeanHandlerImpl(PmBean<Bean> detailsPm) {
      super(detailsPm);
      detailsCalls = new DetailsHandlerCalls();
    }

    @Override
    protected boolean beforeMasterRecordChangeImpl(Bean oldMasterBean, Bean newMasterBean) {
      detailsCalls.before.add(Arrays.asList(oldMasterBean, newMasterBean));
      return super.beforeMasterRecordChangeImpl(oldMasterBean, newMasterBean);
    }

    @Override
    protected void afterMasterRecordChangeImpl(Bean oldMasterBean, Bean newMasterBean) {
      detailsCalls.after.add(Arrays.asList(oldMasterBean, newMasterBean));
      super.afterMasterRecordChangeImpl(oldMasterBean, newMasterBean);
    }
  }

  public static class InstrumentedDetailsPmTableToTableHandler extends DetailsPmTableToTableHandler<Bean, Bean> {

    final DetailsHandlerCalls detailsCalls;

    public InstrumentedDetailsPmTableToTableHandler(
        PmTableImpl<?, ? extends Bean> masterTablePm,
        PmTableImpl<?, ? extends Bean> detailsTablePm) {
      super(masterTablePm, detailsTablePm);
      detailsCalls = new DetailsHandlerCalls();
    }

    @Override
    protected boolean beforeMasterRecordChangeImpl(Bean oldMasterBean, Bean newMasterBean) {
      detailsCalls.before.add(Arrays.asList(oldMasterBean, newMasterBean));
      return super.beforeMasterRecordChangeImpl(oldMasterBean, newMasterBean);
    }

    @Override
    protected void afterMasterRecordChangeImpl(Bean oldMasterBean, Bean newMasterBean) {
      detailsCalls.after.add(Arrays.asList(oldMasterBean, newMasterBean));
      super.afterMasterRecordChangeImpl(oldMasterBean, newMasterBean);
    }
  }

  public static class Bean {
    public String name;
    public List<Bean> items;
    public Bean(String name, Bean... subBeans) {
      this.name = name;
      this.items = new ArrayList<Bean>(Arrays.asList(subBeans));
    }
    @Override
    public String toString() { return name; }
  }


  @PmBeanCfg(beanClass=Bean.class)
  public static class BeanRowPm extends PmBeanBase<Bean> {
    public final PmAttrString name = new PmAttrStringImpl(this);
  }

  @PmFactoryCfg(beanPmClasses=BeanRowPm.class)
  public static class BeanTablePm extends PmTableImpl<BeanRowPm, Bean> {
    public final PmTableCol name = new PmTableColImpl(this);

    DetailsPmEventCalls eventCalls = new DetailsPmEventCalls(this);

    public BeanTablePm(PmObject pmParent) {
      super(pmParent);
    }

    @Override
    protected void onPmInit() {
      super.onPmInit();
      getPmPageableCollection().getSelectionHandler().addPropertyAndVetoableListener(SelectionHandler.PROP_SELECTION, eventCalls.selectionPropertyListener);
      getPmPageableBeanCollection().getSelectionHandler().addSelectionHandlerCallback(new EnsureSingleSelectionCallback());
    }

    @Override
    protected void onPmDataExchangeEvent(PmEvent event) {
      super.onPmDataExchangeEvent(event);
      eventCalls.onPmDataExchangeCalls.add(event);
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      super.onPmValueChange(event);
      eventCalls.onPmValueChangeCalls.add(event);
    }
  }

  static class DetailsAreaPm extends PmBeanBase<Bean> {

    DetailsPmEventCalls eventCalls = new DetailsPmEventCalls(this);

    public DetailsAreaPm(PmObject parentPm) {
      super(parentPm);
    }

    @Override
    protected void onPmDataExchangeEvent(PmEvent event) {
      super.onPmDataExchangeEvent(event);
      eventCalls.onPmDataExchangeCalls.add(event);
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      super.onPmValueChange(event);
      eventCalls.onPmValueChangeCalls.add(event);
    }
  }

  static class DetailsPmEventCalls {
    List<PmEvent> onPmDataExchangeCalls = new ArrayList<PmEvent>();
    List<PmEvent> onPmValueChangeCalls = new ArrayList<PmEvent>();
    _RecordingPmEventListener valueChangeListener = new _RecordingPmEventListener();
    _RecordingPropertyChangeListener selectionPropertyListener = new _RecordingPropertyChangeListener();

    public DetailsPmEventCalls(PmObject pm) {
      PmEventApi.addPmEventListener(pm, PmEvent.VALUE_CHANGE, valueChangeListener);
    }

    public void clear() {
      onPmDataExchangeCalls.clear();
      onPmValueChangeCalls.clear();
      valueChangeListener.clear();
      selectionPropertyListener.clear();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      if (onPmDataExchangeCalls.size() > 0) {
        sb.append("onPmDataExchange=").append(onPmDataExchangeCalls.size()).append(" ");
      }
      if (onPmValueChangeCalls.size() > 0) {
        sb.append("onPmValueChange=").append(onPmValueChangeCalls.size()).append(" ");
      }
      return sb.toString().trim();
    }
  }

  static class DetailsHandlerCalls {
    List<List<Bean>> before = new ArrayList<List<Bean>>();
    List<List<Bean>> after = new ArrayList<List<Bean>>();

    public void clear() {
      before.clear();
      after.clear();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      if (before.size() > 0) {
        sb.append("beforeMsChange=").append(before.size()).append(" ");
      }
      if (after.size() > 0) {
        sb.append("afterMsChange=").append(after.size()).append(" ");
      }
      return sb.toString().trim();
    }
  }

}
