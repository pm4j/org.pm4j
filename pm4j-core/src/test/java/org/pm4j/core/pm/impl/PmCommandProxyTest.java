package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommand.CommandState;
import org.pm4j.core.pm.annotation.PmTitleCfg;

public class PmCommandProxyTest {

  private PmCommand lastExcecutedCommand;

  @Test
  public void testProxyForSuccessfulCommand() {
    MyPm pm = new MyPm();
    assertEquals(true, pm.cmdSuccessProxy.isPmEnabled());
    assertEquals(CommandState.EXECUTED, pm.cmdSuccessProxy.doIt().getCommandState());
    assertEquals("cmdSuccess", lastExcecutedCommand.getPmName());
  }

  @Test
  public void testProxyShowsTitleAndTooltipOfDelegate() {
    MyPm pm = new MyPm();
    assertEquals("SuccessCommand", pm.cmdSuccessProxy.getPmTitle());
    assertEquals("SuccessCommand tooltip", pm.cmdSuccessProxy.getPmTooltip());
  }

  @Test
  public void testProxyWithoutDelegateIsNotEnabled() {
    MyPm pm = new MyPm();
    assertEquals(false, pm.cmdNoDelegateProxy.isPmEnabled());
  }



  public class MyPm extends PmConversationImpl {
    public final PmCommandProxy cmdSuccessProxy = new PmCommandProxy(this);
    public final PmCommandProxy cmdNoDelegateProxy = new PmCommandProxy(this);

    @PmTitleCfg(title="SuccessCommand", tooltip="SuccessCommand tooltip")
    public final PmCommand cmdSuccess = new PmCommandImpl(this) {
      @Override
      protected void doItImpl()  {
       lastExcecutedCommand = this;
      }
    };


    protected void onPmInit() {
      cmdSuccessProxy.setDelegateCmd(cmdSuccess);
    };
  }
}
