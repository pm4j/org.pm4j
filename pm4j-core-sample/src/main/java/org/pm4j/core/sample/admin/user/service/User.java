package org.pm4j.core.sample.admin.user.service;

import java.util.Date;
import java.util.List;

/**
 * A user class for demonstration purpose...
 */
public class User {

  public static enum Gender {
    MALE, FEMALE
  };

  private String        id;
  private String        name;
  private City          city;
  private Date          birthday;
  private Gender        gender;
  private List<Address> addresses;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getBirthday() {
    return birthday;
  }

  public void setBirthday(Date birthday) {
    this.birthday = birthday;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public City getCity() {
    return city;
  }

  public void setCity(City city) {
    this.city = city;
  }

  public List<Address> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Address> addresses) {
    this.addresses = addresses;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
