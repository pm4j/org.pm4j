package org.pm4j.core.sample.admin.remote_sample.server.service;

import org.pm4j.core.sample.admin.remote_sample.shared.Gender;

public class SampleUser {

  private Long id;
  private String loginName;
  private String firstName;
  private String lastName;
  private Gender gender;
  private Department department;
  private String notes;

  // getter and setter
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getLoginName() { return loginName; }
  public void setLoginName(String loginName) { this.loginName = loginName; }
  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public Department getDepartment() { return department; }
  public void setDepartment(Department department) { this.department = department; }
  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }
  public Gender getGender() { return gender; }
  public void setGender(Gender gender) { this.gender = gender; }
}
