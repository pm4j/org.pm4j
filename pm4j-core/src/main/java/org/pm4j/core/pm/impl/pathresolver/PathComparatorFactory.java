package org.pm4j.core.pm.impl.pathresolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.common.expr.parser.ParseCtxt;
import org.pm4j.common.expr.parser.ParseException;
import org.pm4j.common.util.CompareUtil;

public class PathComparatorFactory {

  private static class CompareItem {
    final PathResolver path;
    final boolean ascending;

    public CompareItem(PathResolver compareByPath, boolean ascending) {
      this.path = compareByPath;
      this.ascending = ascending;
    }
  }

  private final CompareItem[] compareItems;


  private PathComparatorFactory(List<CompareItem> itemList) {
    this.compareItems = itemList.toArray(new CompareItem[itemList.size()]);
  };


  public Comparator<Object> getComparator(final Object ctxtObj) {
    return new Comparator<Object>() {
      @SuppressWarnings("unchecked")
      public int compare(Object lhs, Object rhs) {
        if (lhs == rhs) {
          return 0;
        }

        for (CompareItem i : compareItems) {
          Comparable<Object> lval = (Comparable<Object>)i.path.getValue(ctxtObj, lhs);
          Comparable<Object> rval = (Comparable<Object>)i.path.getValue(ctxtObj, rhs);
          // the last parameter ensures that <code>null</code> values are always on top.
          int result = CompareUtil.compare(lval, rval, i.ascending);

          if (result != 0) {
            return i.ascending
                      ? result
                      : -result;
          }
        }

        return 0;
      }
    };
  }

  public static final PathComparatorFactory parse(String compareString, SyntaxVersion syntaxVersion) {
    List<CompareItem> compareItems = new ArrayList<CompareItem>();
    ParseCtxt ctxt = new ParseCtxt(compareString, syntaxVersion);

    while (!ctxt.isDone()) {
      PathResolver expr = PmExpressionPathResolver.parse(ctxt);
      boolean ascending = true;

      if (ctxt.skipBlanks().isOnChar(',')) {
        if (ctxt.skipBlanks().isDone()) {
          throw new ParseException(ctxt, "Unexpected end of compare attribute list.");
        }
      }
      else {
        String s = ctxt.readNameString();
        if (s != null) {
          if (s.equals("desc")) {
            ascending = false;
          }
          else if (!s.equals("asc")) {
            throw new ParseException(ctxt, "'asc' or 'desc' expected. Found '" + s + "'.");
          }
        }
      }

      compareItems.add(new CompareItem(expr, ascending));

      if (ctxt.skipBlanks().isOnChar(',')) {
        ctxt.readChar(',');
        ctxt.skipBlanks();
      }
      else {
        // all items are processed.
        break;
      }
    }

    // Empty sortBy string: Compare the passed instances. No navigation
    // within the object.
    if (compareItems.size() == 0) {
      compareItems.add(new CompareItem(PassThroughPathResolver.INSTANCE, true));
    }

    return new PathComparatorFactory(compareItems);
  }

}
