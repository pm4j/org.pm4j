package org.pm4j.core.pm.impl.changehandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pm4j.common.selection.SelectMode;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;

public class MasterPmHandlerTestDlg extends PmConversationImpl {

  @PmTableCfg(rowSelectMode = SelectMode.SINGLE, valuePath="masterTableBeans")
  public final MasterTablePm masterTable = new MasterTablePm(this);
  @PmBeanCfg(beanClass=Bean.class)
  public final PmBean<Bean> detailsArea = new PmBeanImpl<Bean>(this);

  public List<Bean> masterTableBeans = Arrays.asList(new Bean("a"), new Bean("b"));

  MasterSwitchRecordingDetailsHandler detailsAreaHandler = new MasterSwitchRecordingDetailsHandler(detailsArea);
  MasterPmHandler masterTableHandler;

  // -- Test infrastructure --

  @Override
  protected void onPmInit() {
    super.onPmInit();
    masterTableHandler = new MasterPmTableHandlerImpl<Bean>(masterTable, detailsAreaHandler);
    masterTableHandler.startObservers();
  }

  public static class MasterSwitchRecordingDetailsHandler extends DetailsPmBeanHandlerImpl<Bean> {

    List<List<Bean>> beforeCalls = new ArrayList<List<Bean>>();

    public MasterSwitchRecordingDetailsHandler(PmBean<Bean> detailsPm) {
      super(detailsPm);
    }

    @Override
    protected boolean beforeMasterRecordChangeImpl(Bean oldMasterBean, Bean newMasterRecord) {
      beforeCalls.add(Arrays.asList(oldMasterBean, newMasterRecord));
      return super.beforeMasterRecordChangeImpl(oldMasterBean, newMasterRecord);
    }
  }

  public static class Bean {
    public String f1;
    public Bean(String f1) {
      this.f1 = f1;
    }
    @Override
    public String toString() { return f1; }
  }


  @PmBeanCfg(beanClass=Bean.class)
  public static class MasterRowPm extends PmBeanImpl<Bean> {
    public final PmAttrString f1 = new PmAttrStringImpl(this);
  }

  @PmFactoryCfg(beanPmClasses=MasterRowPm.class)
  public static class MasterTablePm extends PmTableImpl<MasterRowPm, Bean> {
    public final PmTableCol f1 = new PmTableColImpl(this);

    public MasterTablePm(PmObject pmParent) {
      super(pmParent);
    }
  }
}
