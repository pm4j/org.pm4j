package org.pm4j.common.query;

/**
 * @deprecated please use {@link QueryAttr}
 *
 * @author olaf boede
 */
public class AttrDefinition extends QueryAttr {

  private static final long serialVersionUID = 1L;

  public AttrDefinition(String pathName, Class<?> type) {
    super(pathName, type);
  }

  public AttrDefinition(String name, String pathName, Class<?> type, String title) {
    super(name, pathName, type, title);
  }

  public AttrDefinition(String name, String pathName, Class<?> type) {
    super(name, pathName, type);
  }

}
