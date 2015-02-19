package org.pm4j.testdomains.user.model;

import java.util.List;



public class User {

  public static final String ATTR_NAME  = "name";
  public static final String ATTR_SALUTATION  = "salutation";
  public static final String ATTR_DESCRIPTION  = "description";
  public static final String ATTR_ASSOCIATE  = "associate";
  
  public enum Salutation { MR, MRS };
  
  private Domain domain;
  private String name;
  private Salutation salutation;
  private String description;
  private User associate;
  private List<String> languages;

  public User() {
  }

  public User(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Salutation getSalutation() {
    return salutation;
  }

  public void setSalutation(Salutation gender) {
    this.salutation = gender;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Domain getDomain() {
    return domain;
  }

  public void setDomain(Domain domain) {
    this.domain = domain;
  }

  public User getAssociate() {
    return associate;
  }

  public void setAssociate(User associate) {
    this.associate = associate;
  }

  public List<String> getLanguages() {
    return languages;
  }

  public void setLanguages(List<String> languages) {
    this.languages = languages;
  }

}
