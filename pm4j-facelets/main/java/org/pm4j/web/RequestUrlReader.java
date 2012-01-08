package org.pm4j.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.pm4j.jsf.util.PmJsfUtil;
import org.pm4j.navi.impl.NaviRuntimeException;

/**
 * Algorithm that reads the requested URL from an http request.
 * <br>
 * TODO: dokumentiere die Logik der fest im Pfad codierten URL Parameter.
 * TODO: write a test case
 *
 * @author olaf boede
 */
public class RequestUrlReader {

  public static final RequestUrlReader INSTANCE = new RequestUrlReader();

  /**
   *
   * @param httpRequest
   * @param pathParamsToBeShiftedToParamMap
   * @return
   */
  public UrlInfo readUrlInfoPutAllParamsToMap(HttpServletRequest httpRequest) {
    return readUrlInfo(httpRequest, "*");
  }

  /**
   *
   * @param httpRequest
   * @param pathParamsToBeShiftedToParamMap
   * @return
   */
  public UrlInfo readUrlInfo(HttpServletRequest httpRequest, String... pathParamsToBeShiftedToParamMap) {
    boolean pm4jParamInUrl = false;
    UrlInfo requestUrlInfo = new UrlInfo(httpRequest);

    // Fall back solution:
    // Try to get the parameter set from the referrer if not provided in the
    // request.
    // That may happen in case of simple 'hand made' links without version
    // parameter.
    //
    String referrer = httpRequest.getHeader("REFERER");
    if (referrer != null) {
      UrlInfo referrerUrlInfo = new UrlInfo(referrer);

      // When the request navigates to the same page:
      // Preserve all other referrer parameters (except the navigation version).
      // That prevents that keep-alive pings will destroy the existing page
      // parameter set.
      if (referrerUrlInfo.getPath().endsWith(requestUrlInfo.getPath())) {
        // change from absolute to relative path:
        referrerUrlInfo.setPath(requestUrlInfo.getPath());

        // All parameters that will be part of the URL need to be updated
        // to their actual values from the request.
        for (Iterator<Map.Entry<String, String>> iter = referrerUrlInfo.getParams().entrySet().iterator();
             iter.hasNext(); ) {
          Map.Entry<String, String> e = iter.next();
          String reqValue = requestUrlInfo.getParams().get(e.getKey());

          boolean paramShifted = false;
          for (String s : pathParamsToBeShiftedToParamMap) {
            // preserve the parameter, but make sure that it will not be part of the path.
            if (s.equals("*") || s.equals(e.getKey())) {
              if (reqValue == null) {
                requestUrlInfo.addParam(e.getKey(), e.getValue());
              }
              iter.remove();
              paramShifted = true;
            }
          }

          if (!paramShifted && reqValue != null) {
            e.setValue(reqValue);
            // Prevent duplication in request.
            // Would be especially bad for the pm4j-Parameter that would be
            // added
            // to the URL twice.
            requestUrlInfo.removeParam(e.getKey());
          }
        }

        // build the fix path part that may be used to compare page addresses.
        // includes fix parameters...
        requestUrlInfo.setPath(referrerUrlInfo.buildUrl());
        pm4jParamInUrl = referrerUrlInfo.getParams().containsKey(
            PmJsfUtil.PM4J_REQUEST_PARAM);
      }

      requestUrlInfo.setFragment(referrerUrlInfo.getFragment());
    }

    // Preserve the pm4j parameter in case it was send via HTTP-post only.
    if ((!pm4jParamInUrl)
        && !requestUrlInfo.getParams().containsKey(PmJsfUtil.PM4J_REQUEST_PARAM)) {
      String paramValueString = httpRequest.getParameter(PmJsfUtil.PM4J_REQUEST_PARAM);
      if (StringUtils.isNotBlank(paramValueString)) {
        try {
          paramValueString = URLEncoder.encode(paramValueString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          throw new NaviRuntimeException("Unable to encode '" + paramValueString + "'.", e);
        }
        requestUrlInfo.addParam(PmJsfUtil.PM4J_REQUEST_PARAM, paramValueString);
      }
    }

    // The request URL (incl. preserved URL parameter set from referrer).
    return requestUrlInfo;
  }

}
