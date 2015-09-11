package org.pm4j.common.pageable;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.common.query.QueryAttr;

public class TestBean {
  public Integer id;
  public final String name;
  public static final QueryAttr ATTR_ID = new QueryAttr("id", String.class);
  public static final QueryAttr ATTR_NAME = new QueryAttr("name", String.class);

  public TestBean(String name) {
    this.name = name;
  }

  public TestBean(int id, String name) {
    this(name);
    this.id = id;
  }

  @Override
  public String toString() {
    return name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof TestBean)
            ? ObjectUtils.equals(id, ((TestBean)obj).id)
            : super.equals(obj);
    }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(id);
  }

}