package org.pm4j.common.query;

import junit.framework.Assert;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;

public class SomeEvaluationTest {

  private SqlEvalCtxt ctxt = new SqlEvalCtxt();

  @Test
  public void testBuildSqlAndSerialization() {
    FilterExpression expr = new FilterAnd(
        new FilterCompare(new QueryAttr("i", Integer.class), CompOpEquals.class, 1),
        new FilterCompare(new QueryAttr("j", Integer.class), CompOpEquals.class, 3)
        );

    Assert.assertEquals("select * from x where i=1 and j=3",
                        ctxt.evaluate("select * from x where ", expr));

    byte[] bytes = SerializationUtils.serialize(expr);
    expr = (FilterExpression) SerializationUtils.deserialize(bytes);

    Assert.assertEquals("select * from x where i=1 and j=3",
        ctxt.evaluate("select * from x where ", expr));
  }



  class SqlEvalCtxt {

    private QueryEvaluatorSet evaluatorSet;

    public SqlEvalCtxt() {
      evaluatorSet = makeEvaluatorSet();
    }

    public String evaluate(String baseQuery, FilterExpression expr) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(baseQuery);

      getExprEvaluator(expr).build(expr, stringBuilder);

      return stringBuilder.toString();
    }

    public SqlExprEvaluator<? extends FilterExpression> getExprEvaluator(FilterExpression expr) {
      return (SqlExprEvaluator<?>) evaluatorSet.getExprEvaluator(expr);
    }

    public SqlCompOpEvaluator<? extends CompOp> getCompOpEvaluator(FilterCompare co) {
      return (SqlCompOpEvaluator<?>) evaluatorSet.getCompOpEvaluator(co);
    }


    private QueryEvaluatorSet makeEvaluatorSet() {
      QueryEvaluatorSet set = new QueryEvaluatorSet();

      set.addExprEvaluator(FilterAnd.class, new SqlExprEvaluator<FilterAnd>() {
        @Override
        protected void buildSql(FilterAnd expr, StringBuilder sb) {
          for (int i=0; i<expr.getExpressions().size(); ++i) {
            if (i > 0) {
              sb.append(" and ");
            }

            FilterExpression e = expr.getExpressions().get(i);
            getExprEvaluator(e).build(e, sb);
          }
        }
       });

      set.addExprEvaluator(FilterCompare.class, new SqlExprEvaluator<FilterCompare>() {
        @Override
        protected void buildSql(FilterCompare expr, StringBuilder sb) {
          QueryAttr attr = expr.getAttr();
          sb.append(attr.getName());
          getCompOpEvaluator(expr).build(expr.getCompOp(), expr.getValue(), sb);
        }
       });

      set.addCompOpEvaluator(CompOpEquals.class, new SqlCompOpEvaluator<CompOpEquals>() {
       @Override
        protected void buildSql(CompOpEquals co, Object value, StringBuilder sb) {
          sb.append("=").append(value);
        }
      });

      return set;
    }

    abstract class SqlExprEvaluator<T_EXPR extends FilterExpression> implements FilterExpressionEvaluator {
      @SuppressWarnings("unchecked")
      public void build(FilterExpression e, StringBuilder sb) {
        buildSql((T_EXPR)e, sb);
      }
      protected abstract void buildSql(T_EXPR expr, StringBuilder sb);
    }

    abstract class SqlCompOpEvaluator<T_CO extends CompOp> implements CompOpEvaluator {
      @SuppressWarnings("unchecked")
      public void build(CompOp co, Object value, StringBuilder sb) {
        buildSql((T_CO)co, value, sb);
      }
      protected abstract void buildSql(T_CO co, Object value, StringBuilder sb);
    }


  }

}
