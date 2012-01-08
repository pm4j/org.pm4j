package org.pm4j.core.util.reflection;

public interface BeanAttrAccessor {

  /**
   * Provides the name of the represented field or getter/setter pair.<br>
   * In case of a field, it provides the name of the field.<br>
   * In case of a getter/setter pair it returns the property name according
   * to the java bean naming conventions.
   * 
   * @return The name of the property which is addressed by this instance. 
   */
  String getName();
  
  /**
   * Gets the attribute value object from the given bean.
   * 
   * @param <T>
   *          The expected value fieldClass.
   * @param bean
   *          The bean to get the value from. Should not be <code>null</code>.
   * @return The attribute value object.
   */
  <T> T getBeanAttrValue(Object bean);

  /**
   * Sets the bean attribute value.
   * <p>
   * Precondition: A setter method must be defined.
   * 
   * @param bean
   *          The bean to manipulate.
   * @param value
   *          The new attribute value.
   */
  void setBeanAttrValue(Object bean, Object value);

  /**
   * @return <code>true</code> when {@link #setBeanAttrValue(Object, Object)}
   *         can be called.
   */
  boolean canSet();

  /**
   * @return The fieldClass of the accessible field.
   */
  Class<?> getFieldClass();
}