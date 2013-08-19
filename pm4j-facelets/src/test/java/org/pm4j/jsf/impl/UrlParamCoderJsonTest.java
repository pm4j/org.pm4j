package org.pm4j.jsf.impl;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrlParamCoderJsonTest extends TestCase {

  private static final Log LOG = LogFactory.getLog(UrlParamCoderJsonTest.class);
  
  public void testCoder() {
    UrlParamCoderJson coder = new UrlParamCoderJson();
    
    Map<String, Object> oriMap = new HashMap<String, Object>();
    oriMap.put("a1", "v1");
    oriMap.put("a2", "v2%");
    
    String paramValue = coder.mapToParamValue(oriMap);
    LOG.info("encoded param: " + paramValue);
    
    Map<String, Object> copyMap = coder.paramValueToMap(paramValue);
    
    assertEquals(oriMap.size(), copyMap.size());
    assertEquals("v1", copyMap.get("a1"));
    assertEquals("v2%", copyMap.get("a2"));
  }
}
