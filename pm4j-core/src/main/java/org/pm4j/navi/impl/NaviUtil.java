package org.pm4j.navi.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.pm4j.common.util.CloneUtil;
import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.core.pm.PmObject;


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
    boolean canClone = canCloneOrSerialize(pValue) || pValue instanceof PmObject;
    if (! canClone) {
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

  /**
   * Puts all entries of srcMap to targetMap. The values will be cloned using
   * {@link ClassUtil#cloneOrSerialize(Object)}.
   *
   * @param srcMap
   *          A map with the values to clone.
   * @param targetMap
   *          The map to put the cloned entries to.
   * @return A reference to the targetMap.
   */
  static <K, V> Map<K, V> deepCloneValues(Map<K, V> srcMap, Map<K, V> targetMap) {
    for (Map.Entry<K, V> e : srcMap.entrySet()) {
      targetMap.put(e.getKey(), cloneOrSerialize(e.getValue()));
    }
    return targetMap;
  }

  @SuppressWarnings("unchecked")
  private static final Set<Class<?>> IMMUTABLE_CLASSES = new HashSet<Class<?>>(Arrays.asList(
      String.class,
      Integer.class, Long.class, Short.class, Double.class, Float.class,
      Class.class));


  /**
   * Generates a clone for the given object.<br>
   * If the object is {@link Cloneable}, its <code>clone</code> method will be
   * called.<br>
   * If the object is not {@link Cloneable} but {@link Serializable}, a clone
   * will be created using the serialization and de-serialization.
   *
   * @param ori
   *          The object to copy.
   * @return The copy.
   * @throws IllegalArgumentException
   *           if the given object can't be copied because it is not cloneable
   *           or serializable.
   */
  @SuppressWarnings("unchecked")
  private static <T> T cloneOrSerialize(T ori) {
    if (ori == null) {
      return null;
    }

    Class<?> objClass = ori.getClass();

    if (IMMUTABLE_CLASSES.contains(objClass)) {
      return ori;
    }

    Object clone = null;

    if (ori instanceof Cloneable) {
      clone = CloneUtil.clone((Cloneable)ori);
    }
    else if (ori instanceof Serializable) {
      clone = SerializationUtils.clone((Serializable)ori);
    }
    else if (ori instanceof PmObject) {
      // Exception for PMs: If they are not marked as cloneable, they will be used
      // as shared instances.
      // FIXME olaf: this code is very PM logic specific. Find a way to express it not PM-specific
      //             in a technology utility.
      // alternative: Simply don't clone not cloneable stuff.
      //              Disadvantage: Provides tricky error scenarios with unintended shared instances...
      clone = ori;
    }
    else {
      throw new IllegalArgumentException("Class '" + objClass.getName() + "' does neither implement Cloneable nor Serializable.");
    }

    return (T) clone;
  }

  /**
   * @param o The instance to test. May be <code>null</code>.
   * @return <code>true</code> if the given object is a formally correct
   *         argument for {@link #cloneOrSerialize(Object)}.
   */
  private static boolean canCloneOrSerialize(Object o) {
    return o == null ||
           o instanceof Cloneable ||
           o instanceof Serializable;
  }


  private static boolean isVersionDigit(char c) {
    return ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'));
  }

}
