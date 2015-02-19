package org.pm4j.deprecated.core.sample.admin.remote_sample.server.service;

public class Department {

  private Long id;
  private String name;

  public Department(long id, String name) {
    this.id   = id;
    this.name = name;
  }
  
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public void setId(Long id) { this.id = id; }
  public Long getId() { return id; }
}
