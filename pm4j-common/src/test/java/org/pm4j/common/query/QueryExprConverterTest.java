package org.pm4j.common.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QueryExprConverterTest {
  
  final QueryAttr A = new QueryAttr("a", Integer.class);
  
  @Test
  public void convertExprCombination() {
    QueryExpr is1 = new QueryExprCompare(A, CompOpEquals.class, 1);
    QueryExpr is3 = new QueryExprCompare(A, CompOpEquals.class, 3);
    QueryExpr is1or3 = QueryExprOr.join(is1, is3);
    QueryExpr isNotNegative = new QueryExprNot(new QueryExprCompare(A, CompOpLt.class, 0));
    QueryExpr isOdd = new QueryExprInMemCondition<Integer>() {
      @Override
      public boolean eval(Integer item) {
        return item != null && item % 2 != 0;
      }
      @Override
      public String toString() {
        return "IS_ODD";
      }
    };
    QueryExpr is1or3AndNotNegativeAndOdd = QueryExprAnd.join(is1or3, isNotNegative, isOdd);
    
    assertEquals("AND(OR(a = 1, a = 3), NOT(a < 0), IS_ODD)", is1or3AndNotNegativeAndOdd.toString());
    
    QueryExpr converted = new QueryExprConverter().convert(is1or3AndNotNegativeAndOdd);
    assertEquals("AND(OR(a = 1, a = 3), NOT(a < 0), IS_ODD)", converted.toString());
  }

  @Test
  public void convertNull() {
    assertEquals(null, new QueryExprConverter().convert(null));
  }

  @Test(expected=IllegalArgumentException.class)
  public void convertUnknownExpr() {
    new QueryExprConverter().convert(new QueryExpr() {
    });
  }

}
