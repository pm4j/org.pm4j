package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.common.exception.CheckedExceptionWrapper;

public class QueryAttrWithAlternatePath implements QueryAttr.WithPath {

  private QueryAttr.WithPath mainAttr;
  private List<String> altPaths = new ArrayList<String>();

  public QueryAttrWithAlternatePath(QueryAttr.WithPath mainAttr) {
    assert mainAttr != null;
    this.mainAttr = mainAttr;
  }

  public QueryAttrWithAlternatePath(String name, String path, Class<?> attrType) {
    this(new AttrDefinition(name, path, attrType));
  }

  public QueryAttrWithAlternatePath(String name, Class<?> attrType) {
    this(new AttrDefinition(name, attrType));
  }

  public QueryAttrWithAlternatePath addAltPath(String path) {
    altPaths.add(path);
    return this;
  }

  public List<String> getAltPaths() {
    return altPaths;
  }

  @Override
  public String getName() {
    return mainAttr.getName();
  }

  @Override
  public String getTitle() {
    return mainAttr.getTitle();
  }

  @Override
  public String getPathName() {
    return mainAttr.getPathName();
  }

  @Override
  public Class<?> getType() {
    return mainAttr.getType();
  }

  @Override
  public QueryAttrWithAlternatePath clone() {
    try {
      QueryAttrWithAlternatePath clone = (QueryAttrWithAlternatePath) super.clone();

      return clone;
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }
}
