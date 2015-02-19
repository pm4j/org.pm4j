package org.pm4j.req_domain.model;

import java.util.Date;

public class Requirement {

  public static final String ATTR_ID = "id";

  public static final String ATTR_MODIFIED = "modified";

  public static final String ATTR_NAME = "name";

  public static final String ATTR_DESCRIPTION = "description";

  public static final String ATTR_AUTHOR = "author";

  private Long id;

  private Date modified;

  private String name;

  private String description;

  private String author;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getModified() {
    return modified;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

}
