package org.pm4j.common.query;

/**
 * Converts a QueryExpr to a new one.
 * 
 * May be used to exchange some expression parts.
 * E.g. to replace DTO conditions by corresponding
 * entity conditions.
 * 
 * Subclasses may override some convert-methods to achieve that effectively.
 * 
 * @author Olaf Boede
 */
public class QueryExprConverter {

  /**
   * @param qe
   *            the query expression to convert.
   * @return the query expression that can be used by for DAOs that use physical keys.
   */
  public QueryExpr convert(QueryExpr qe) {
      if (qe == null) {
          return null;
      } else if (qe instanceof QueryExprCompare) {
          return convertCompExpr((QueryExprCompare) qe);
      } else if (qe instanceof QueryExprAnd) {
          return convertAndExpr((QueryExprAnd) qe);
      } else if (qe instanceof QueryExprOr) {
          return convertOrExpr((QueryExprOr) qe);
      } else if (qe instanceof QueryExprNot) {
          return convertNotExpr((QueryExprNot) qe);
      } else if (qe instanceof QueryExprInMemCondition) {
        return convertInMemExpr((QueryExprInMemCondition<?>) qe);
      } else {
        return convertUnknownExpr(qe);
      }
  }
  
  protected QueryExprCompare convertCompExpr(QueryExprCompare compExp) {
      return compExp;
  }

  protected QueryExprAnd convertAndExpr(QueryExprAnd andExpr) {
      QueryExprAnd newAnd = new QueryExprAnd();
      for (QueryExpr i : andExpr.getExpressions()) {
          newAnd.add(convert(i));
      }
      return newAnd;
  }

  protected QueryExprOr convertOrExpr(QueryExprOr orExpr) {
      QueryExprOr newOr = new QueryExprOr();
      for (QueryExpr i : orExpr.getExpressions()) {
          newOr.add(convert(i));
      }
      return newOr;
  }

  protected QueryExprNot convertNotExpr(QueryExprNot notExpr) {
      return new QueryExprNot(convert(notExpr.getBaseExpression()));
  }

  protected QueryExpr convertInMemExpr(QueryExprInMemCondition<?> qe) {
    return qe;
  }

  protected QueryExpr convertUnknownExpr(QueryExpr expr) {
    throw new IllegalArgumentException("Unable to convert expression: " + expr);
  }

}
