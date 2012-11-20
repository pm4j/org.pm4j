package org.pm4j.core.navi;

import junit.framework.TestCase;

import org.pm4j.navi.NaviHistory;
import org.pm4j.navi.NaviHistoryCfg;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviManager;
import org.pm4j.navi.NaviHistoryCfg.SessionIdGenStrategy;
import org.pm4j.navi.impl.NaviLinkImpl;
import org.pm4j.navi.impl.NaviManagerImpl;

public class NaviScopeTest extends TestCase {

  static final NaviLink L1 = new NaviLinkImpl("l1");
  static final String   V1 = "v1";
  static final NaviLink L1_VAL = NaviLinkImpl.makeNaviScopeParamLink("l1", "k1", V1);
  static final NaviLink L2 = new NaviLinkImpl("l2");
  static final Long   V2 = new Long(1234);
  static final Long   V2_CHANGED = new Long(12345);
  static final NaviLink L2_VAL = NaviLinkImpl.makeNaviScopeParamLink("l2", "k2", V2);
  static final NaviLink L3 = new NaviLinkImpl("l3");
  static final String   V3 = "v3";
  static final NaviLink L3_VAL = NaviLinkImpl.makeNaviScopeParamLink("l3", "k3", V3);

  private NaviManager m;

  @Override
  protected void setUp() throws Exception {
    NaviHistoryCfg naviCfg = new NaviHistoryCfg();
    naviCfg.setSessionIdGenStrategy(SessionIdGenStrategy.SEQUENTIAL);
    m = new NaviManagerImpl(naviCfg);
  }

  public void testPassValueToNextPage() {
    NaviHistory h;

    h = m.onNavigateTo(L1, null);

    // start new page and pass a value
    h = m.onNavigateTo(L2_VAL, "0.0");
    assertEquals(V2, h.getNaviScopeProperty("k2"));

    // continue navigation to another page, the value should still be there
    h = m.onNavigateTo(L3, "0.1");
    assertEquals(V2, h.getNaviScopeProperty("k2"));


    h = m.onNavigateTo(L1, "0.2");
    assertEquals(null, h.getNaviScopeProperty("k2"));

    // get the the naviScope properties back on request on an old version:
    h = m.onNavigateTo(L3, "0.1");
    assertEquals("1.0", h.getVersionString());
    assertEquals(V2, h.getNaviScopeProperty("k2"));

    h = m.onNavigateTo(L2, "0.1");
    assertEquals("2.0", h.getVersionString());
    assertEquals(V2, h.getNaviScopeProperty("k2"));
  }

  public void testPassValueToCurrentPage() {
    NaviHistory h;

    h = m.onNavigateTo(L1, null);

    h = m.onNavigateTo(L1_VAL, "0.0");
    assertEquals("v1", h.getNaviScopeProperty("k1"));

    h = m.onNavigateTo(L1, "0.1");
    assertEquals("v1", h.getNaviScopeProperty("k1"));

    h = m.onNavigateTo(L2, "0.1");
    assertEquals("v1", h.getNaviScopeProperty("k1"));

    h = m.onNavigateTo(L2_VAL, "0.2");
    assertEquals("v1", h.getNaviScopeProperty("k1"));
    assertEquals(V2, h.getNaviScopeProperty("k2"));

    h = m.onNavigateTo(L1, "0.3");
    assertEquals("v1", h.getNaviScopeProperty("k1"));
    assertEquals(null, h.getNaviScopeProperty("k2"));

    // get the the naviScope properties back on request to an old version:
    h = m.onNavigateTo(L1, "0.0");
    assertEquals("1.0", h.getVersionString());
    assertEquals(null, h.getNaviScopeProperty("k1"));
    assertEquals(null, h.getNaviScopeProperty("k2"));

    h = m.onNavigateTo(L1, "0.1");
    assertEquals("2.0", h.getVersionString());
    assertEquals("v1", h.getNaviScopeProperty("k1"));
    assertEquals(null, h.getNaviScopeProperty("k2"));

    h = m.onNavigateTo(L2, "0.3");
    assertEquals("3.0", h.getVersionString());
    assertEquals("v1", h.getNaviScopeProperty("k1"));
    assertEquals(V2, h.getNaviScopeProperty("k2"));

  }

  public void testNaviSessionIsolation() {
    NaviHistory h;

    h = m.onNavigateTo(L1, null);

    h = m.onNavigateTo(L1_VAL, "0.0");
    assertEquals("v1", h.getNaviScopeProperty("k1"));

    h = m.onNavigateTo(L2_VAL, "0.0");
    assertEquals("1.0", h.getVersionString());
    assertEquals(null, h.getNaviScopeProperty("k1"));
    assertEquals(V2, h.getNaviScopeProperty("k2"));

    h = m.onNavigateTo(L1, "0.0");
    assertEquals("2.0", h.getVersionString());
    assertEquals(null, h.getNaviScopeProperty("k1"));
    assertEquals(null, h.getNaviScopeProperty("k2"));

    h = m.onNavigateTo(L1, "0.1");
    assertEquals("0.1", h.getVersionString());
    assertEquals("v1", h.getNaviScopeProperty("k1"));
    assertEquals(null, h.getNaviScopeProperty("k2"));
  }

  public void testHideValue() {
    NaviHistory h;

    h = m.onNavigateTo(L1, null);

    // pass the first value
    h = m.onNavigateTo(L1_VAL, "0.0");
    assertEquals("v1", h.getNaviScopeProperty("k1"));

    // go to another page and pass another value for the same key
    h = m.onNavigateTo(NaviLinkImpl.makeNaviScopeParamLink("l2", "k1", "v1'"), "0.1");
    assertEquals("v1'", h.getNaviScopeProperty("k1"));

    // use the first page and version -> the first value should be still there
    h = m.onNavigateTo(L1, "0.1");
    assertEquals("1.0", h.getVersionString());
    assertEquals("v1", h.getNaviScopeProperty("k1"));

    // ping the second page and version -> the second value should exist there
    h = m.onNavigateTo(L2, "0.2");
    assertEquals("v1'", h.getNaviScopeProperty("k1"));
  }


  public void testPassNaviscopeValToPageChangeItAndPerformNavigationLoop() {
    NaviHistory h;

    h = m.onNavigateTo(L1, null);

    // start new page and pass a value
    h = m.onNavigateTo(L2_VAL, "0.0");
    assertTrue("navigation link adds a property",
                 V2 == h.getNaviScopeProperty("k2"));

    // change the value
    h.setNaviScopeProperty("k2", V2_CHANGED);
    assertTrue("property changed using the history interface",
                 V2_CHANGED == h.getNaviScopeProperty("k2"));

    // do a navigation loop:
    h = m.onNavigateTo(L3, "0.1");
    assertEquals("forward navigation should keep the propterty alive",
                 V2_CHANGED, h.getNaviScopeProperty("k2"));

    h = m.onNavigateTo(L2, "0.2");
    assertEquals("find the value on back navigation to the position where the parameter did already existed",
                 V2_CHANGED, h.getNaviScopeProperty("k2"));

    h = m.onNavigateTo(L1, "0.3");
    assertEquals("the value should not exist after back navigation to a position where it did not exist",
                 null, h.getNaviScopeProperty("k2"));

    h = m.onNavigateTo(L2, "0.4");
    assertEquals("a new navigation to the page L2 (not back-navigation!) without navi-parameter will " +
                 "not find the value of the old navigation loop again",
                 null, h.getNaviScopeProperty("k2"));
}


}
