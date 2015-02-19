package org.pm4j.jsontest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonTest extends TestCase {

  public void testMap() throws JSONException, UnsupportedEncodingException {
    Map<String, Object> oriMap = new HashMap<String, Object>();
    oriMap.put("key1", "value1");
    oriMap.put("key2", "äö ü");
    oriMap.put("key3", Boolean.TRUE);
    
    JSONObject jsonObj = new JSONObject(oriMap);
    String jsonString = jsonObj.toString();
    System.out.println(jsonString);
    
    String encoded = URLEncoder.encode(jsonString, "UTF-8");
    System.out.println(encoded);
    System.out.println(URLEncoder.encode(jsonString, "ISO-8859-1"));
    
    String decoded = URLDecoder.decode(encoded, "UTF-8");
    assertEquals(jsonString, decoded);
    
    JSONObject jsonObj2 = new JSONObject(decoded);
    @SuppressWarnings("unchecked")
    Iterator<String> keyIter = jsonObj2.keys();
    while (keyIter.hasNext()) {
      String key = keyIter.next();
      Object val = jsonObj2.get(key);
      
      assertEquals(oriMap.get(key), val);
      System.out.println(" Key: " + key + " val: " + val + " valClass: " + val.getClass());
    }
    
  }
  
}
