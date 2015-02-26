package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.junit.Test;

public class AnnotationUtilTest {

  /**
   * Test that the findAnnotationsInClassTree method finds two annotations, whereat the result list is sorted from the 
   * root class annotation to the parent class annotation.
   */
  @Test
  public void shouldFindTwoAnnotationsInClassTree() {
    // When
    List<Author> annotations = AnnotationUtil.findAnnotationsInClassTree(Cherry.class, Author.class);
    
    // Then
    assertEquals(2, annotations.size());
    assertEquals("Peter Pan", annotations.get(0).name());
    assertEquals("Max Mustermann", annotations.get(1).name());
  }
  
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Author {
    
    String name() default "";
  }
  
  @Author(name="Max Mustermann")  
  private static class Food {
    
  }
  
  private static class Fruit extends Food  {
    
  }
  
  @Author(name="Peter Pan") 
  private static class Cherry extends Fruit  {
    
  }
}
