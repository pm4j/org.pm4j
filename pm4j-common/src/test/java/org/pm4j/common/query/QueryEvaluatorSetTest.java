package org.pm4j.common.query;

import org.junit.Assert;
import org.junit.Test;

public class QueryEvaluatorSetTest {
  
  QueryEvaluatorSet testee = new QueryEvaluatorSet();
  
  @Test
  public void shouldAddAndGetNewValueTypeSpecificCompOpEvaluator() {
    // Given
    CompOpEvaluator evaluator = new CompOpEvaluator() {};
    QueryAttr attr = new QueryAttr("Path", String.class);
    QueryExprCompare compare = new QueryExprCompare(attr, CompOpEquals.class, null);
    
    // When
    testee.addCompOpEvaluatorForValueType(String.class, CompOpEquals.class, evaluator);
    CompOpEvaluator evaluatorFromSet = testee.getCompOpEvaluator(compare);
    
    // Then
    Assert.assertSame(evaluator, evaluatorFromSet);
  }
  
  @Test
  public void shouldAddAndGetReplacedValueTypeSpecificCompOpEvaluator() {
    // Given
    CompOpEvaluator evaluatorOld = new CompOpEvaluator() {};
    CompOpEvaluator evaluatorNew = new CompOpEvaluator() {};
    QueryAttr attr = new QueryAttr("Path", String.class);
    QueryExprCompare compare = new QueryExprCompare(attr, CompOpEquals.class, null);
    
    // When
    testee.addCompOpEvaluatorForValueType(String.class, CompOpEquals.class, evaluatorOld);
    testee.addCompOpEvaluatorForValueType(String.class, CompOpEquals.class, evaluatorNew);
    CompOpEvaluator evaluatorFromSet = testee.getCompOpEvaluator(compare);
    
    // Then
    Assert.assertNotSame(evaluatorOld, evaluatorFromSet);
    Assert.assertSame(evaluatorNew, evaluatorFromSet);
  }
  
  @Test(expected=RuntimeException.class)
  public void shouldThrowExceptionIfCompOpEvaluatorIsMissing() {
    // Given
    QueryAttr attr = new QueryAttr("Path", String.class);
    QueryExprCompare compare = new QueryExprCompare(attr, CompOpEquals.class, null);
    
    // When
    testee.getCompOpEvaluator(compare);
  }
}
