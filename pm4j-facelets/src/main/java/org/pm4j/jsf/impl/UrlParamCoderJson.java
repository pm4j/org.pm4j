package org.pm4j.jsf.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.jsf.UrlParamCoder;

/**
 * Encodes the parameter using json and url coding.
 *
 * @author olaf boede
 */
public class UrlParamCoderJson implements UrlParamCoder {

  @Override
  public String mapToParamValue(Map<String, Object> paramMap) {
    if (paramMap == null || paramMap.size() == 0) {
      return null;
    }
    else {
      try {
        JSONObject jsonObj = new JSONObject(paramMap);
        String jsonString = jsonObj.toString();

        return jsonString;
      } catch (Exception e) {
        throw new CheckedExceptionWrapper(e);
      }
    }
  }

  @Override
  public Map<String, Object> paramValueToMap(String urlParam) {
    Map<String, Object> map = Collections.emptyMap();

    if (! StringUtils.isEmpty(urlParam)) {
      try {
        JSONObject jsonObj = new JSONObject(urlParam);

        map = new HashMap<String, Object>();
        @SuppressWarnings("unchecked")
        Iterator<String> keyIter = jsonObj.keys();
        while (keyIter.hasNext()) {
          String key = keyIter.next();
          Object value = jsonObj.get(key);
          // XXX olaf: json seems to have problems to re-identify a long type.
          //           To prevent problems in our value compare operations, we handle
          //           integers generally as long's now.
          // FIXME: that may provide problems in case of parameters that should have an integer type...
          if (value instanceof Integer) {
            value = new Long(((Integer) value).longValue());
          }
          map.put(key, value);
        }
      }
      catch (Exception e) {
        throw new RuntimeException("Unable to convert the pm4j parameter: " + urlParam, e);
      }
    }

    return map;
  }

}
