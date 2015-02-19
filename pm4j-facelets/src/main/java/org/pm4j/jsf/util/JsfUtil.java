package org.pm4j.jsf.util;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.navi.impl.NaviRuntimeException;
import org.pm4j.web.UrlInfo;

/**
 * Some convenience functions for JSF Api handling.
 *
 * @author olaf boede
 */
public class JsfUtil {

  private static final Logger LOG = LoggerFactory.getLogger(JsfUtil.class);

  private static final String HIDDEN_VALUE="<value not displayed>";

	/**
	 * Helper to read the argument from the RequestParameterMap
	 *
	 * @param nameOfArgument
	 *            the name of the argument in the map
	 * @return the value of the argument
	 */
	public static String readReqParam(String nameOfArgument) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getExternalContext().getRequestParameterMap()
				.get(nameOfArgument);
	}

  /**
   * Helper to read and convert the argument from the RequestParameterMap
   *
   * @param nameOfArgument
   *          the name of the argument in the map
   * @return the {@link Long} value of the argument or <code>null</code>.
   * @exception NumberFormatException
   *              if the parameter is not <code>null</code> but does not
   *              contain a parseable <code>long</code>.
   */
	public static Long readReqParamAsLong(String nameOfArgument) {
		return readReqParamAsLong(nameOfArgument, null);
	}

  /**
   * Helper to read and convert the argument from the RequestParameterMap
   *
   * @param nameOfArgument
   *          the name of the argument in the map
   * @param defaultValue
   *          the default that will be used when there is no parameter with the given name in the request.
   * @return the {@link Long} value of the argument or the default.
   * @exception NumberFormatException
   *              if the parameter is not <code>null</code> but does not
   *              contain a parseable <code>long</code>.
   */
  public static Long readReqParamAsLong(String nameOfArgument, Long defaultValue) {
    String argString = readReqParam(nameOfArgument);
    return argString != null ? new Long(argString) : defaultValue;
  }

  /**
   * Helper to read and convert the argument from the RequestParameterMap
   *
   * @param nameOfArgument
   *          the name of the argument in the map
   * @return the {@link Integer} value of the argument or <code>null</code>.
   * @exception NumberFormatException
   *              if the parameter is not <code>null</code> but does not
   *              contain a parseable <code>long</code>.
   */
  public static Integer readReqParamAsInt(String nameOfArgument) {
    return readReqParamAsInt(nameOfArgument, null);
  }

  /**
   * Helper to read and convert the argument from the RequestParameterMap
   *
   * @param nameOfArgument
   *          the name of the argument in the map
   * @param defaultValue
   *          the default that will be used when there is no parameter with the given name in the request.
   * @return the {@link Integer} value of the argument or the default.
   * @exception NumberFormatException
   *              if the parameter is not <code>null</code> but does not
   *              contain a parseable <code>long</code>.
   */
  public static Integer readReqParamAsInt(String nameOfArgument, Integer defaultValue) {
    String argString = readReqParam(nameOfArgument);
    return argString != null ? new Integer(argString) : defaultValue;
  }

  /**
   * Tries to read the parameter from the request.
   * If it was not found there, the REFERRER header
   * attribute will be used.
   * <p>
   * Useful for a4j scenarios, where the page URL parameter set will
   * not be passed to the server as part of the request parameter set.
   *
   * @param nameOfArgument
   *            the name of the argument in the map
   * @return the value of the argument
   */
  public static String readReqOrReferrerParam(String paramName) {
    HttpServletRequest httpRequest = JsfUtil.getHttpRequest();
    String value = httpRequest.getParameter(paramName);
    if (value == null) {
      String referrer = httpRequest.getHeader("REFERER");
      if (referrer != null) {
        UrlInfo referrerUrlInfo = new UrlInfo(referrer);
        value = referrerUrlInfo.getParam(paramName);
      }
    }
    return value;
  }

  /**
   * @return The JSF external context object.
   */
  public static ExternalContext getExternalContext() {
    return  FacesContext.getCurrentInstance().getExternalContext();
  }

  /**
   * Gets the http request from the faces context.
   *
   * @return The current http Request.
   */
  public static HttpServletRequest getHttpRequest() {
    return (HttpServletRequest)getExternalContext().getRequest();
  }

  /**
   * Gets the http response from the faces context.
   *
   * @return The current http Response.
   */
  public static HttpServletResponse getHttpResponse() {
    return (HttpServletResponse)getExternalContext().getResponse();
  }

  /**
   * Redirects to the provided absolute navigation path.
   *
   * @param uri
   *          An absolute navigation path.
   */
  public static void redirect(String uri) {
    FacesContext fc = FacesContext.getCurrentInstance();
    // If somebody has called responseComplete, he will not call this.
    // So than this must be a call from the framework (e.g. the
    // PmConnectorForJsf), but in that case we want to avoid
    // second-guessing the original user.
    if (fc.getResponseComplete()) {
      LOG.debug("Response is already complete, *not* redirecting to: " + uri);
      return;
    }

    try {
      String urlWithServletName = relUrlWithServletName(uri);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Redirecting to: " + uri + " (URI with servlet name: " + urlWithServletName + ")");
      }

      fc.getExternalContext().redirect(urlWithServletName);
    } catch (IOException ioException) {
      throw new NaviRuntimeException("Unable to redirect to: " + uri, ioException);
    }
  }

  /**
   * Converts a relative application URL to another relative URL that contains
   * the application URL.
   * <p>
   * Example: "my/page.jsf" will be converted to "/myservletname/my/page.jsf"
   *
   * @param relUrl The relative application URL.
   * @return The URL which includes the servlet path.
   */
  public static String relUrlWithServletName(String relUrl) {
    String ctxtPath = StringUtils.defaultString(getExternalContext().getRequestContextPath());
    StringBuilder sb = new StringBuilder(ctxtPath.length() + 1 + relUrl.length());

    sb.append(ctxtPath);
    if (! relUrl.startsWith("/")) {
      sb.append("/");
    }
    sb.append(relUrl);

    return sb.toString();
  }

  /**
   * Wandelt die gegebene relative URL in eine absolute URL um und fügt dabei den ContextPath ein
   * @param relUrl die relative URL
   * @return die absolute URL
   */
  public static String relUrlToExternalUrl(String relUrl){
    return relUrlToExternalUrl(relUrl,true);
  }

  /**
   * Wandelt die gegebene relative URL in eine absolute URL um. Wenn der Schalter <code>addContextPath</code>
   * auf <code>true</code> steht, wird der ContextPath in die URL eingefügt.
   * @param relUrl die relative URL
   * @param addContextPath <code>true</code>, wenn der ContextPath eingefügt werden soll, ansonsten <code>false/<code>
   * @return die absolute URL
   */
  public static String relUrlToExternalUrl(String relUrl, boolean addContextPath){
    HttpServletRequest request = getHttpRequest();

    String absUrl = "";
    String helper = relUrl;

    try {
      URI uri = new URI(request.getRequestURL().toString());
      if(addContextPath){
        helper = relUrlWithServletName(relUrl);
      }
      absUrl = uri.resolve(helper).toASCIIString();
    }
    catch(URISyntaxException use){
      CheckedExceptionWrapper.throwAsRuntimeException(use);
    }

    return absUrl;
  }

  /**
   * Downloads data from the given URL.
   *
   * @param srcUrl
   * @param contentType
   * @param fileName
   * @throws IOException
   */
  public static void download(URL srcUrl, String contentType, String fileName) throws IOException {
    URLConnection urlConnection = srcUrl.openConnection();
    download(urlConnection.getInputStream(), contentType, fileName, urlConnection.getContentLength());
  }

  public static void downloadAndComplete(URL srcUrl, String contentType, String fileName) throws IOException {
    download(srcUrl, contentType, fileName);
    FacesContext.getCurrentInstance().responseComplete();
  }


  /**
   * Downloads data from the given stream.
   *
   * @param iData
   *          Die Daten.
   * @param contentType
   *          Der http content Typ.
   * @param fileName
   *          Der Name der Zieldatei.
   * @param contentLen
   *          Die Größe des Download Inhaltes.
   * @throws IOException
   *           bei IO Fehlern.
   */
  public static void download(InputStream iData, String contentType, String fileName, int contentLen) throws IOException {
    Validate.notNull(iData);
    Validate.notEmpty(contentType);
    Validate.notEmpty(fileName);

    try {
      HttpServletResponse response = getHttpResponse();

      response.setContentType(contentType);
      response.setHeader("Content-disposition", "attachment; filename=" + fileName);
      if (contentLen > 0) {
        response.setContentLength(contentLen);
      }

      OutputStream os = response.getOutputStream();
      IOUtils.copy(iData, os);
      os.flush();
    }
    finally {
      IOUtils.closeQuietly(iData);
    }
  }



  @SuppressWarnings("unchecked")
  public static String getRequestDataLogString() {
    StringBuilder sb = new StringBuilder(2000);
    FacesContext fc = FacesContext.getCurrentInstance();
    ExternalContext externalContext = fc.getExternalContext();
    HttpServletRequest httpRequest = (HttpServletRequest)externalContext.getRequest();
    Enumeration<String> eN = httpRequest.getAttributeNames();
    while (eN.hasMoreElements()) {
      String key = eN.nextElement();
      sb.append("\nRequest Attribute Name: " + key + "   Value: ");
      if(hideValue(key)){
        sb.append(HIDDEN_VALUE);
      }else{
        sb.append(httpRequest.getAttribute(key));
      }
    }
    eN = httpRequest.getParameterNames();
    while (eN.hasMoreElements()) {
      String key = eN.nextElement();
      sb.append("\nRequest Paramter: " + key + "   Value: ");
      if(hideValue(key)){
        sb.append(HIDDEN_VALUE);
      }else{
        sb.append(httpRequest.getParameter(key));
      }
      // LOG.trace("Request Paramter toUTF8: " + toUTF8(httpRequest.getParameter(key)));
    }

    Cookie[] cookies = httpRequest.getCookies();
    if (cookies != null) {
      for (Cookie c : httpRequest.getCookies()) {
        sb.append("\nRequest Cookie: " + c.getName() + "   Value: " + c.getValue());
      }
    }

    sb.append("\nRequest encoding: " + httpRequest.getCharacterEncoding());
    sb.append("\nRequest uri: " + httpRequest.getRequestURI());
    sb.append("\nRequest url: " + httpRequest.getRequestURL());
    sb.append("\nhttpRequest.getHeader('REFERER')=" + httpRequest.getHeader("REFERER"));

    return sb.toString();
  }

  // -- internal helper --

  private static boolean hideValue(String key){
    return key.toLowerCase().contains("password");
  }

  static String toUTF8(String isoString) {
    String utf8String = null;
    if (null != isoString && !isoString.equals("")) {
      try {
        byte[] stringBytesISO = isoString.getBytes("ISO-8859-1");
        utf8String = new String(stringBytesISO, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        // TODO: This should never happen. The UnsupportedEncodingException
        // should be propagated instead of swallowed. This error would indicate
        // a severe misconfiguration of the JVM.

        // As we can't translate just send back the best guess.
        LOG.error("Can't encode: " + isoString, e);
        utf8String = isoString;
      }
    } else {
      utf8String = isoString;
    }
    return utf8String;
  }
}
