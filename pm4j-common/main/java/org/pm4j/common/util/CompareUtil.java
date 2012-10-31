package org.pm4j.common.util;

import java.text.Collator;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

/**
 * Utility methods to simplify compare tasks.
 *
 * @author olaf boede
 */
public class CompareUtil {

  /**
   * Compares two instances. Each of them may be <code>null</code>.
   * A <code>null</code> parameter is always <i>less</i> than a not
   * <code>null</code> parameter.
   * Provides:
   * <ul>
   *   <li>The result of o1.{@link Comparable#compareTo(Object)}(o2) if
   *       both parameters are not <code>null</code>.</li>
   *   <li><code>0</code> - if both parameters are <code>null</code>.</li>
   *   <li><code>1</code> - if only the second parameter is <code>null</code>.</li>
   *   <li><code>-1</code> - if only the first parameter is <code>null</code>.</li>
   * </ul>
   *
   * @param lhs left object to compare.
   * @param rhs right object to compare.
   * @return The compare result.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static int compare(Comparable lhs, Comparable rhs) {
    if (lhs != null) {
      return (rhs != null)
            ? lhs.compareTo(rhs)
            : 1;
    }
    else {
      return (rhs == null)
            ? 0
            : -1;
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static int compare(Comparable lhs, Comparable rhs, boolean isNullLess) {
    if (lhs != null) {
      return (rhs != null)
            ? lhs.compareTo(rhs)
            : (isNullLess ? 1 : -1);
    }
    else {
      return (rhs == null)
            ? 0
            : (isNullLess ? -1 : 1);
    }
  }

  /**
   * Compares two enum values based on their ordinal position of the given
   * enum values.
   * <p>
   * <code>null</code>s are handled the same way as in {@link #compare(Comparable, Comparable)}.
   *
   * @param lhs left object to compare.
   * @param rhs right object to compare.
   * @return The compare result.
   */
  public static int compare(Enum<?> lhs, Enum<?> rhs) {
    if (lhs != null) {
      if (rhs == null) {
        return 1;
      }
      else {
        return ((Integer)lhs.ordinal()).compareTo(rhs.ordinal());
      }
    }
    else {
      return (rhs == null)
            ? 0
            : -1;
    }
  }

  /**
   * Compares two <code>int</code> values.
   *
   * @param lhs left value to compare.
   * @param rhs right value to compare.
   * @return The compare result.
   */
  public static int compare(int lhs, int rhs) {
    return (lhs < rhs ? -1 : (lhs == rhs ? 0 : 1));
  }

  /**
   * Compares two string values based on collation order definition of the given
   * {@link Locale}.
   * <p>
   * <code>null</code>s are handled the same way as in
   * {@link #compare(Comparable, Comparable)}.
   *
   * @param lhs
   *          left object to compare.
   * @param rhs
   *          right object to compare.
   * @param locale
   *          The locale that is used to define the language specific sort order.
   * @return The compare result.
   */
  public static int compare(String lhs, String rhs, Locale locale) {
    if (lhs != null) {
      if (rhs == null) {
        return 1;
      }
      else {
        return Collator.getInstance(locale).compare(lhs, rhs);
      }
    }
    else {
      return (rhs == null)
            ? 0
            : -1;
    }
  }

  /**
   * An special equals implementation for strings.
   *
   * @param lhs
   * @param rhs
   * @param ignoreSpaces
   * @param ignoreCase
   * @return
   */
  public static boolean equalStrings(String lhs, String rhs, boolean ignoreSpaces, boolean ignoreCase) {
    if (lhs != null) {
      if (rhs == null) {
        return (ignoreSpaces)
            ? StringUtils.isBlank((String)lhs)
            : lhs == null;
      }
      else {
        String lhsString = lhs;
        String rhsString = rhs;
        if (ignoreSpaces) {
          lhsString = lhsString.trim();
          rhsString = rhsString.trim();
        }

        return ignoreCase
            ? lhsString.equalsIgnoreCase(rhsString)
            : rhsString.equals(rhsString);
      }
    }
    else {
      return (ignoreSpaces)
          ? StringUtils.isBlank((String)rhs)
          : rhs == null;
    }
  }

  /**
   * @param s
   *          the string to find the pattern in.
   * @param pattern
   *          the pattern to find within s.
   * @param ignoreSpaces
   *          <code>true</code> trims the pattern before applying the search.
   * @param ignoreCase
   *          <code>true</code> makes the match case insensitive.
   * @return the start position of the pattern within the given string <i>s</i>.<br>
   *         <code>-1</code> if the pattern was not found.
   */
  public static int indexOf(String s, String pattern, boolean ignoreSpaces, boolean ignoreCase) {
    if (ignoreSpaces) {
      s = StringUtils.trim(s);
      pattern = StringUtils.trim(pattern);
    }
    if (ignoreCase) {
      s = StringUtils.upperCase(s);
      pattern = StringUtils.upperCase(pattern);
    }
    return StringUtils.indexOf(s, pattern);
  }

  public static boolean endsWith(String s, String pattern, boolean ignoreSpaces, boolean ignoreCase) {
    int patternIdx = indexOf(s, pattern, ignoreSpaces, ignoreCase);
    if (patternIdx < 0) {
      return false;
    }
    else {
      int expectedStartPos = s.length() - pattern.length();
      return expectedStartPos == patternIdx;
    }
  }
}
