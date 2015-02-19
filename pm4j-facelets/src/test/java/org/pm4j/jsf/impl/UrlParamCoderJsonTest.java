package org.pm4j.jsf.impl;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlParamCoderJsonTest extends TestCase {

  private static final Logger LOG = LoggerFactory.getLogger(UrlParamCoderJsonTest.class);
  
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
