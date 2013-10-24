package org.pm4j.jsf.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.impl.NaviLinkImpl;

public class PmJsfUtilTest {
  @Test
  public void testRelUrlForNaviLink(){
    NaviLink naviLink = new NaviLinkImpl("myPath", "myName", "myValue");
    String url = PmJsfUtil.relUrlForNaviLink(naviLink, false);
    assertEquals("Test JSon String", "myPath?pm4j=%7B%22myName%22%3A%22myValue%22%7D", url);
  }
}
