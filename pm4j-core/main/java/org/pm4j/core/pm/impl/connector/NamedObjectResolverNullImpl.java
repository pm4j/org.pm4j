package org.pm4j.core.pm.impl.connector;

/**
 * This resolver is only intended as a place holder.
 * It does never find an object.
 *
 * @author olaf boede
 */
public class NamedObjectResolverNullImpl implements NamedObjectResolver {

  @Override
  public Object findObject(String name) {
    return null;
  }

}
