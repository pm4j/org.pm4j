package org.pm4j.core.navi;

import java.util.Iterator;

import junit.framework.TestCase;

import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.NaviHistoryCfg;
import org.pm4j.navi.NaviHistoryCfg.SessionIdGenStrategy;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviManager;
import org.pm4j.navi.impl.NaviLinkImpl;
import org.pm4j.navi.impl.NaviManagerImpl;
import org.pm4j.navi.impl.NaviUtil;

public class NaviHistoryTest extends TestCase {

  static final NaviLink LINK_1 = new NaviLinkImpl("link1");
  static final NaviLink LINK_2 = new NaviLinkImpl("link2");
  static final NaviLink LINK_2_POS1 = new NaviLinkImpl("link2", "pos1");
  static final NaviLink LINK_2_POS2 = new NaviLinkImpl("link2", "pos2");
  static final NaviLink LINK_3 = new NaviLinkImpl("link3");
  static final NaviLink LINK_3_PAR1a = new NaviLinkImpl("link3", "par1", "a");
  static final NaviLink LINK_3_PAR1b = new NaviLinkImpl("link3", "par1", "b");

  private NaviHistoryCfg naviCfg;

  @Override
  protected void setUp() throws Exception {
    naviCfg = new NaviHistoryCfg();
    naviCfg.setSessionIdGenStrategy(SessionIdGenStrategy.SEQUENTIAL);
  }

  public void testForwardBackward() {
    NaviManager m = new NaviManagerImpl(naviCfg);

    // -- Initial Navigation --
    check(m.onNavigateTo(LINK_1, null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_1, "0.0"), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.0"), "0.1", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_3, "0.1"), "0.2", LINK_1, LINK_2, LINK_3);
    check(m.onNavigateTo(LINK_3, "0.2"), "0.2", LINK_1, LINK_2, LINK_3);
    check(m.onNavigateTo(LINK_2, "0.2"), "0.3", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_3, "0.3"), "0.4", LINK_1, LINK_2, LINK_3);
    check(m.onNavigateTo(LINK_1, "0.4"), "0.5", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.5"), "0.6", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_2_POS1, "0.6"), "0.6", LINK_1, LINK_2_POS1);
    check(m.onNavigateTo(LINK_2_POS2, "0.6"), "0.6", LINK_1, LINK_2_POS2);
    check(m.onNavigateTo(LINK_3_PAR1a, "0.6"), "0.7", LINK_1, LINK_2_POS2, LINK_3_PAR1a);
    check(m.onNavigateTo(LINK_3_PAR1b, "0.7"), "0.8", LINK_1, LINK_2_POS2, LINK_3_PAR1a, LINK_3_PAR1b);
    check(m.onNavigateTo(LINK_2, "0.8"), "0.9", LINK_1, LINK_2);
  }

  public void testForwardBackwardWithNewVersionOnPagePosChange() {
    naviCfg.setNewNaviVersionOnPagePosChange(true);
    NaviManager m = new NaviManagerImpl(naviCfg);

    // -- Initial Navigation --
    check(m.onNavigateTo(LINK_1, null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_1, "0.0"), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.0"), "0.1", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_3, "0.1"), "0.2", LINK_1, LINK_2, LINK_3);
    check(m.onNavigateTo(LINK_3, "0.2"), "0.2", LINK_1, LINK_2, LINK_3);
    check(m.onNavigateTo(LINK_2, "0.2"), "0.3", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_3, "0.3"), "0.4", LINK_1, LINK_2, LINK_3);
    check(m.onNavigateTo(LINK_1, "0.4"), "0.5", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.5"), "0.6", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_2_POS1, "0.6"), "0.7", LINK_1, LINK_2_POS1);
    check(m.onNavigateTo(LINK_2_POS2, "0.7"), "0.8", LINK_1, LINK_2_POS2);
    check(m.onNavigateTo(LINK_3_PAR1a, "0.8"), "0.9", LINK_1, LINK_2_POS2, LINK_3_PAR1a);
    check(m.onNavigateTo(LINK_3_PAR1b, "0.9"), "0.a", LINK_1, LINK_2_POS2, LINK_3_PAR1a, LINK_3_PAR1b);
    check(m.onNavigateTo(LINK_2, "0.a"), "0.b", LINK_1, LINK_2);
  }

  public void testForks() {
    NaviManager m = new NaviManagerImpl(naviCfg);

    // -- Initial Navigation --
    check(m.onNavigateTo(LINK_1, null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.0"), "0.1", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_3, "0.1"), "0.2", LINK_1, LINK_2, LINK_3);

    // -- Move from old version to a new link. -> Second session fork. --
    check(m.onNavigateTo(LINK_2, "0.0"), "1.0", LINK_1, LINK_2);

    // -- Ping an old link on the first session. -> Third session fork. --
    check(m.onNavigateTo(LINK_1, "0.0"), "2.0", LINK_1);

    // -- Normal back-navigation on the second session. --
    check(m.onNavigateTo(LINK_1, "1.0"), "1.1", LINK_1);

    // -- Ping a link on the third session. --
    check(m.onNavigateTo(LINK_1, "2.0"), "2.0", LINK_1);

    // -- Ping an old link on the first session a second time.
    //    -> Find and use forked session. --
    check(m.onNavigateTo(LINK_1, "0.0"), "2.0", LINK_1);
  }

  public void testForkFromLoop() {
    NaviManager m = new NaviManagerImpl(naviCfg);

    // -- Initial Navigation --
    check(m.onNavigateTo(LINK_1, null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.0"), "0.1", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_1, "0.1"), "0.2", LINK_1);

    // -- Ping a version from the 'moved-out' loop history items.
    //     -> Session fork based on the 'moved-out' history. --
    check(m.onNavigateTo(LINK_2, "0.1"), "1.0", LINK_1, LINK_2);
  }

  // TODO: Szenario beschreiben
  public void testReAttachToOldVersion() {
    NaviManager m = new NaviManagerImpl(naviCfg);

    // -- Initial Navigation --
    check(m.onNavigateTo(LINK_1, "1.6"), "1.6", LINK_1);
    check(m.onNavigateTo(LINK_2, "1.6"), "1.7", LINK_1, LINK_2);

    // -- The same again, but now with an existing session id an an unknown version:
    //     -> The session is already in use. A brand new session has to be used.
    check(m.onNavigateTo(LINK_1, "1.2"), "0.0", LINK_1);

    // -- The same again:
    //     -> The session is already in use. A brand new session has to be used.
    //        Since session '0' and '1' are already used, a session ID '2' will be used now.
    //
    //    That's the worst situation with dead sessions for now.
    //    FIXME olaf: An enhanced implementation might remember that the dead version '1.2'
    //                is already mapped to version '0.0'...
    check(m.onNavigateTo(LINK_1, "1.2"), "2.0", LINK_1);
  }

  public void testRemoveOldHistories() throws InterruptedException {
    int removeIntervalMs = 100;
    naviCfg.setUnusedLinkLiveTimeMs(removeIntervalMs);
    NaviManagerImpl m = new NaviManagerImpl(naviCfg);

    // -- Initial Navigation loop --
    check(m.onNavigateTo(LINK_1, null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.0"), "0.1", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_3, "0.1"), "0.2", LINK_1, LINK_2, LINK_3);
    check(m.onNavigateTo(LINK_1, "0.2"), "0.3", LINK_1);

    // -- Ping an old history state --
    check(m.onNavigateTo(LINK_3, "0.2"), "1.0", LINK_1, LINK_2, LINK_3);

    // -- Wait longer than the remove interval --
    Thread.sleep(removeIntervalMs * 2);

    System.out.println("before gc\n"+m.getTraceString());

    // First ping will currently still find the history, since it
    // the remove operation will be the last activity in onNavigateTo.
    check(m.onNavigateTo(LINK_3, "0.2"), "1.0", LINK_1, LINK_2, LINK_3);

    System.out.println("after gc\n"+m.getTraceString());

    // A second ping to another moved-out version will not succeed,
    // since it was removed by the previous call.
    // --> Creates a new session with the requested id (which was released meanwhile).
    check(m.onNavigateTo(LINK_3, "0.1"), "0.1", LINK_3);
  }

  public void testPopupNavigation() {
    NaviManager m = new NaviManagerImpl(naviCfg);

    // -- Initial Navigation --
    check(m.onNavigateTo(LINK_1, null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.0"), "0.1", LINK_1, LINK_2);
    // -- some popups --
    check(m.onNavigateTo(LINK_3, "0.1", NaviManager.NaviMode.POPUP), "1.0", LINK_3);
    check(m.onNavigateTo(LINK_3, "0.1", NaviManager.NaviMode.POPUP), "2.0", LINK_3);
    check(m.onNavigateTo(LINK_3, "0.0", NaviManager.NaviMode.POPUP), "3.0", LINK_3);
    check(m.onNavigateTo(LINK_3, "2.0", NaviManager.NaviMode.POPUP), "4.0", LINK_3);
  }

  public void testGetPrevOrStartLink() {
    NaviManager m = new NaviManagerImpl(naviCfg);
    check(m.onNavigateTo(LINK_1, null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.0"), "0.1", LINK_1, LINK_2);

    assertEquals(LINK_1, m.getCurrentHistoryOfSession("0").getPrevOrStartLink());
  }

  public void testGetPrevOrStartLinkAndSkipItem() {
    NaviManager m = new NaviManagerImpl(naviCfg);
    check(m.onNavigateTo(LINK_1, null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.0"), "0.1", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_3, "0.1"), "0.2", LINK_1, LINK_2, LINK_3);

    assertEquals(LINK_1, m.getCurrentHistoryOfSession("0").getPrevOrStartLink(LINK_2));
  }

  public void testReverseIterator() {
    NaviManager m = new NaviManagerImpl(naviCfg);
    check(m.onNavigateTo(LINK_1, null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2, "0.0"), "0.1", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_3, "0.1"), "0.2", LINK_1, LINK_2, LINK_3);

    Iterator<NaviLink> iter = m.getCurrentHistoryOfSession("0").getReverseIterator();
    assertEquals(LINK_3, iter.next());
    assertEquals(LINK_2, iter.next());
    assertEquals(LINK_1, iter.next());
    assertFalse(iter.hasNext());

  }

  public void testNaviLoopWithChangedPos() {
    NaviManager m = new NaviManagerImpl(naviCfg);
    check(m.onNavigateTo(LINK_1,        null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2_POS1,   "0.0"), "0.1", LINK_1, LINK_2_POS1);
    check(m.onNavigateTo(LINK_3,        "0.1"), "0.2", LINK_1, LINK_2_POS1, LINK_3);
    check(m.onNavigateTo(LINK_2,        "0.2"), "0.3", LINK_1, LINK_2);
    check(m.onNavigateTo(LINK_3,        "0.3"), "0.4", LINK_1, LINK_2, LINK_3);
    check(m.onNavigateTo(LINK_2_POS2,   "0.4"), "0.5", LINK_1, LINK_2_POS2);
  }

  public void testModifyCurrentPosShouldNotChangeTheOriginalLinkInstance() {
    naviCfg.setNewNaviVersionOnPagePosChange(true);
    NaviManager m = new NaviManagerImpl(naviCfg);
    check(m.onNavigateTo(LINK_1,        null ), "0.0", LINK_1);
    check(m.onNavigateTo(LINK_2,        "0.0"), "0.1", LINK_1, LINK_2);
    m.getCurrentHistoryOfSession("0").setPosOnPage("pos1");
    assertNull(LINK_2.getPosOnPage());

    check(m.onNavigateTo(LINK_3,        "0.1"), "0.2", LINK_1, LINK_2_POS1, LINK_3);
    check(m.onNavigateTo(LINK_2,        "0.2"), "0.3", LINK_1, LINK_2);

    assertNull(LINK_2.getPosOnPage());
  }

  // -- internal helper --

  private void check(NaviHistory h, String versionString, NaviLink... expectedLinks) {
    assertEquals(versionString, h.getVersionString());

    String[] sarr = NaviUtil.splitVersionString(versionString);
    String sessionId = sarr[0];
    String versionId = sarr[1];

    assertNotNull("History for navi session '" + sessionId + "' expected.", h);
    assertEquals("History version", versionId, h.getVersion());

    assertEquals("History size", expectedLinks.length, h.getSize());

    Iterator<NaviLink> iter = h.getItemIterator();
    for (int i=0; i<expectedLinks.length; ++i) {
      NaviLink e = expectedLinks[i];
      NaviLink f = iter.next();
      assertTrue("History item expected: " + e + " found: " + f, e.isLinkToSamePagePos(f));
    }
  }

}
