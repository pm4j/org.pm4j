package org.pm4j.navi.impl;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.util.reflection.ClassUtil;


public final class NaviUtil {

  public static final char VERSION_STRING_TERM_CHAR = '.';

  /**
   * Checks if the given parameter may be used as a navigation scope parameter.
   *
   * @param pName
   *          Name of the parameter (for error reporting).
   * @param pValue
   *          The parameter value to check.
   */
  public static void checkNaviScopeParam(String pName, Object pValue) {
    if (! ClassUtil.canCloneOrSerialize(pValue)) {
      throw new IllegalArgumentException("The passed navigation scope parameter '" +
          pName + "' is not cloneable and not serializable. Passed value was: " + pValue);
    }
  }

  public static String makeVersionString(String sessionId, String versionId) {
    return new StringBuilder().append(sessionId).append(VERSION_STRING_TERM_CHAR).append(versionId).toString();
  }

  public static String makeVersionString(String[] versArr) {
    return versArr != null
      ? makeVersionString(versArr[0], versArr[1])
      : null;
  }

  public static String[] splitVersionString(String versionString) {
    String[] sarr = StringUtils.split(versionString, VERSION_STRING_TERM_CHAR);
    if (sarr.length != 2) {
      throw new NaviRuntimeException("Invalid version string format: '" + versionString +
          "'. A string formatted like '12" + VERSION_STRING_TERM_CHAR + "34' expected.");
    }
    return sarr;
  }

  public static String nextId(String prevCallId) {
    int prevId = idToInt(prevCallId);
    return intToId(prevId+1);
  }

  public static boolean isNextId(String prev, String next) {
    return idToInt(prev)+1 == idToInt(next);
  }

  public static int idToInt(String s) {
    return Integer.parseInt(s, Character.MAX_RADIX);
  }

  public static String intToId(int i) {
    return Integer.toString(i, Character.MAX_RADIX);
  }

  public static String parseVersionString(String srcString) {
    String[] sarr = parseVersionStringArray(srcString);
    return makeVersionString(sarr[0], sarr[1]);
  }

  public static String[] parseVersionStringArray(String srcString) {
    String[] sarr = splitVersionString(srcString);
    String versionBase = sarr[1];
    StringBuilder versBuilder = new StringBuilder();

    for (int i=0; i<versionBase.length(); ++i) {
      while (i < srcString.length()) {
        char c = srcString.charAt(i);
        if (isVersionDigit(c)) {
          versBuilder.append(c);
          ++i;
        }
        else {
          break;
        }
      }
    }
    sarr[1] = versBuilder.toString();
    return sarr;
  }

  private static boolean isVersionDigit(char c) {
    return ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'));
  }

}
