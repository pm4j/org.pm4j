package org.pm4j.web;

import org.pm4j.web.UrlInfo;

import junit.framework.TestCase;

public class UrlInfoTest extends TestCase {

  public void testUrlToNaviLink_simple() {
    UrlInfo u = new UrlInfo("/page1/page2");
    assertEquals("/page1/page2", u.getPath());
    assertNull(u.getFragment());
    assertEquals(0, u.getParams().size());
  }

  public void testUrlToNaviLink_param() {
    UrlInfo u = new UrlInfo("/page1/page2?p1=123");
    assertEquals("/page1/page2", u.getPath());
    assertNull(u.getFragment());
    assertEquals(1, u.getParams().size());
    assertEquals("123", u.getParams().get("p1"));
  }

  public void testUrlToNaviLink_param3() {
    UrlInfo u = new UrlInfo("/page1/page2?p1=123&p2=abc&p3=def");
    assertEquals("/page1/page2", u.getPath());
    assertNull(u.getFragment());
    assertEquals(3, u.getParams().size());
    assertEquals("123", u.getParams().get("p1"));
    assertEquals("abc", u.getParams().get("p2"));
    assertEquals("def", u.getParams().get("p3"));
  }

  public void testUrlToNaviLink_param3_fragment() {
    UrlInfo u = new UrlInfo("/page1/page2?p1=123&p2=abc&p3=def#frag1");
    assertEquals("/page1/page2", u.getPath());
    assertEquals("frag1", u.getFragment());
    assertEquals(3, u.getParams().size());
    assertEquals("123", u.getParams().get("p1"));
    assertEquals("abc", u.getParams().get("p2"));
    assertEquals("def", u.getParams().get("p3"));
  }

  public void testUrlToNaviLink_fragment() {
    UrlInfo u = new UrlInfo("/page1/page2#frag1");
    assertEquals("/page1/page2", u.getPath());
    assertEquals("frag1", u.getFragment());
  }

  /** Tests the kind of pseudo-fragments that IE often sends in its referrer information. */
  public void testUrlToNaviLink_IE_fragment() {
    UrlInfo u = new UrlInfo("/page1/page2#");
    assertEquals("/page1/page2", u.getPath());
    assertEquals(null, u.getFragment());
  }

  public void testUrlToNaviLink_emptyString() {
    UrlInfo u = new UrlInfo("");
    assertEquals("", u.getPath());
    assertNull(u.getFragment());
  }

  public void testBuildUrlWithIe6FragmentHandling() {
    UrlInfo u = new UrlInfo("/page?p1=123#frag1");
    assertEquals("/page?p1=123&#frag1", u.buildUrl());
  }

  public void testBuildUrlWithIe6FragmentHandlingWithoutParameter() {
    UrlInfo u = new UrlInfo("/page#frag1");
    assertEquals("/page#frag1", u.buildUrl());
  }

  public void testParseUrlWithEmptyIe6Param() {
    UrlInfo u = new UrlInfo("/page?p1=123&#frag1");
    assertEquals("/page", u.getPath());
    assertEquals("frag1", u.getFragment());
    assertEquals(1, u.getParams().size());
    assertEquals("123", u.getParams().get("p1"));
  }

}
