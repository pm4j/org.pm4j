package org.pm4j.core.pm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmInject.Mode;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * Demonstrates the injection of a parent interface within a PM subtree member.
 *
 * @author olaf boede
 */
public class PmInjectParentOfTypeTest {

  /**
   *
   */
  @Test
  public void testInjectedParentInterface() {
    MyConversationPm conversationPm = new MyConversationPm();
    MyElementPm elemPm = new MyElementPm(conversationPm);

    assertEquals("Current User Name: unknown", elemPm.userInfo.getPmTitle());

    conversationPm.userName = "Mr. X";
    assertEquals("Current User Name: Mr. X", elemPm.userInfo.getPmTitle());
  }

  /** An interface that needs to be injected somewhere within the PM tree. */
  interface UserInfoProvider {
    String getUserName();
  }

  /** A node within the PM tree that implements an interface neede somewhere within the sub tree. */
  class MyConversationPm extends PmConversationImpl implements UserInfoProvider {

    private String userName = "unknown";

    @Override
    public String getUserName() {
      return userName;
    }
  }

  public static class MyElementPm extends PmElementImpl {

    public final PmObject userInfo = new PmObjectBase(this) {
      @PmInject(mode=Mode.PARENT_OF_TYPE)
      UserInfoProvider userInfoProvider;

      @Override
      protected String getPmTitleImpl() {
        return "Current User Name: " + userInfoProvider.getUserName();
      }
    };

    public MyElementPm(PmObject parentPm) {
      super(parentPm);
    }
  }

}
