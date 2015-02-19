package org.pm4j.core.pm.impl;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.exception.PmException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviRuleLink;
import org.pm4j.navi.impl.NaviLinkImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PmCommandImplTest {

  private static final NaviLink FIX_LINK = new NaviLinkImpl("link1");

  @Test
  // TODO oboede: ignored when changing the doitImpl execption
  @Ignore()
  public void testCommands() {
    TestPm pm = new TestPm();

    PmCommand cmd = pm.cmdStaticNavi.doIt();
    assertEquals(FIX_LINK, cmd.getNaviLink());

    assertNull(pm.cmdDynNavi.getNaviLink());
    cmd = pm.cmdDynNavi.doIt();
    assertNull("No navigation when dynLinkTargetString is null.", cmd.getNaviLink());

    pm.dynLinkTargetString.setValue("dynLink1");
    assertEquals(new NaviLinkImpl("dynLink1"), pm.cmdDynNavi.doIt().getNaviLink());

    pm.dynLinkTargetString.setValue("dynLink2");
    assertEquals(new NaviLinkImpl("dynLink2"), pm.cmdDynNavi.doIt().getNaviLink());

    assertNull("Dynamic navigation should have no side effect on the original command.",
            pm.cmdDynNavi.getNaviLink());

    pm.dynLinkTargetString.setValue("ruleLink1");
    assertEquals("ruleLink1", pm.cmdDynNaviRule.doItReturnString());

    pm.dynLinkTargetString.setValue("ruleLink2");
    assertEquals("ruleLink2", pm.cmdDynNaviRule.doItReturnString());

    pm.dynLinkTargetString.resetPmValues();
    assertNull(pm.cmdDynNaviRule.doItReturnString());


    // Error handling:
    pm.dynLinkTargetString.setValue("ruleLinkX");

    // Command throws an exception that should show a message for the user:
    pm.successKind.setValue(SuccessKind.USER_MSG_EXCEPTION);
    assertEquals(null, pm.cmdDynNaviRule.doItReturnString());
    assertEquals(1, PmMessageApi.getMessages(pm.getPmConversation(), Severity.ERROR).size());
    assertEquals(1, PmMessageApi.getMessages(pm.cmdDynNaviRule.getPmConversation(), Severity.ERROR).size());

    PmMessageApi.clearPmTreeMessages(pm.getPmConversation());

    // Command throws an internal exception that can't be handled well.

    pm.successKind.setValue(SuccessKind.OTHER_EXCEPTION);
    assertEquals("to_error_page", pm.cmdDynNaviRule.doItReturnString());
    // FIXME olaf: The default exception handler should leave an error message somewhere...
    //             (Der Plattform ExceptionHandler schmeisst einfach weiter.)
    assertEquals(0, PmMessageApi.getMessages(pm.getPmConversation(), Severity.ERROR).size());

  }


  enum SuccessKind { SUCCESS, USER_MSG_EXCEPTION, OTHER_EXCEPTION };

  public static class TestPm extends PmConversationImpl {

    @PmAttrCfg(defaultValue="SUCCESS")
    public final PmAttrEnum<SuccessKind> successKind = new PmAttrEnumImpl<SuccessKind>(this, SuccessKind.class);
    public final PmAttrString dynLinkTargetString = new PmAttrStringImpl(this);

    /** Navigates to a fix page via NaviLink. */
    public final PmCommand cmdStaticNavi = new PmCommandImpl(this, FIX_LINK) {
      @Override
      protected void doItImpl()  {
        doSomething(this);
      };
    };

    /** Navigates to calculated page via NaviLink. */
    public final PmCommand cmdDynNavi = new PmCommandImpl(this) {
      @Override
      protected void doItImpl()  {
        doSomething(this);

        if (dynLinkTargetString.getValue() != null) {
          navigateTo(new NaviLinkImpl(dynLinkTargetString.getValue()));
        }
      }
    };

    /** Navigates to calculated page via NaviRule. */
    public final PmCommand cmdDynNaviRule = new PmCommandImpl(this) {
      @Override
      protected void doItImpl()  {
        doSomething(this);

        if (dynLinkTargetString.getValue() != null) {
          navigateTo(new NaviRuleLink(dynLinkTargetString.getValue()));
        }
      }
    };

    @SuppressWarnings("incomplete-switch")
    private void doSomething(PmCommand cmd) {
      switch (successKind.getValue()) {
        case USER_MSG_EXCEPTION: throw new PmRuntimeException(cmd, "pmCommandImplTest.something_failed");
        case OTHER_EXCEPTION:    throw new PmRuntimeException(cmd, "internal program failure");
      }
    }

  }

}
