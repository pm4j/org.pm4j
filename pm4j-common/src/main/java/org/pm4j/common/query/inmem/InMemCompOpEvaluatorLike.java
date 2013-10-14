package org.pm4j.common.query.inmem;

import java.util.Map;
import java.util.regex.Pattern;

import org.pm4j.common.query.CompOpLike;

/**
 * A like evaluator based no a solution found in:
 * http://stackoverflow.com/questions/898405/how-to-implement-a-sql-like-like-operator-in-java
 *
 * @author oboede
 *
 */
public class InMemCompOpEvaluatorLike extends InMemCompOpEvaluatorBase<CompOpLike, String> {

  private static final String LIKE_PATTERN_CACHE_KEY = "likePatterns";

  @Override
  protected boolean evalImpl(InMemQueryEvaluator<?> ctxt, CompOpLike compOp, String attrValue, String compareToValue) {
    return (attrValue != null)
        ? getPattern(ctxt, compareToValue).matcher(attrValue).matches()
        : false;
  }

  static Pattern getPattern(InMemQueryEvaluator<?> ctxt, final String expr) {
    Map<Object, Object> cache = ctxt.getCache(LIKE_PATTERN_CACHE_KEY);
    Pattern p = (Pattern) cache.get(expr);
    if (p == null) {
      String regex = quotemeta(expr);
      regex = regex.replace("_", ".").replace("%", ".*?");
      p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
      cache.put(expr, p);
    }
    return p;
  }

  static String quotemeta(String s) {
    if (s == null) {
      return "";
    }

    int len = s.length();
    if (len == 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder(len * 2);
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if ("[](){}.*+?$^|#\\".indexOf(c) != -1) {
        sb.append("\\");
      }
      sb.append(c);
    }
    return sb.toString();
  }
}
