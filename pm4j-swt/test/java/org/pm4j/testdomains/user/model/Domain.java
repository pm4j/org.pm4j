package org.pm4j.testdomains.user.model;

import java.util.ArrayList;
import java.util.List;


public class Domain {
  
  public static final String ATTR_ID  = "id";
  public static final String ATTR_NAME  = "name";
  public static final String ATTR_DESCRIPTION  = "description";
  public static final String ATTR_USERS  = "users";
  public static final String ATTR_SUBDOMAINS  = "subDomains";
  public static final String ATTR_PARENTDOMAIN  = "parentDomain";

  private Long id;
  
  private String name;

  private String description;

  private List<User> users = new ArrayList<User>();

  private List<Domain> subDomains = new ArrayList<Domain>();

  private Domain parentDomain;

  public Domain(String name) {
    this.name = name;
  }
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public List<User> getUsers() {
    return users;
  }

  protected void setUsers(List<User> userList) {
    this.users = userList;
  }
  
  public User addToUsers(User user) {
    if (! users.contains(user)) {
      user.setDomain(this);
      users.add(user);
    }
    return user;
  }

  public boolean removeFromUsers(User user) {
    boolean wasInList = users.remove(user);
    if (wasInList)
      user.setDomain(null);
    return wasInList; 
  }

  public List<Domain> getSubDomains() {
    return subDomains;
  }

  public void setSubDomains(List<Domain> subDomains) {
    this.subDomains = subDomains;
  }

  public Domain addToSubDomains(Domain domain) {
    if (! subDomains.contains(domain)) {
      domain.setParentDomain(this);
      subDomains.add(domain);
    }
    return domain;
  }

  public boolean removeFromSubDomains(Domain domain) {
    boolean wasInList = subDomains.remove(domain);
    if (wasInList)
      domain.setParentDomain(null);
    return wasInList; 
  }

  public Domain getParentDomain() {
    return parentDomain;
  }

  public void setParentDomain(Domain parentDomain) {
    this.parentDomain = parentDomain;
  }

}
