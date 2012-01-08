package org.pm4j.core.sample.admin.user.service;

public class Address {

  private City   city;
  private String street;
  private String nr;

  public City getCity() {
    return city;
  }
  public void setCity(City city) {
    this.city = city;
  }
  public String getStreet() {
    return street;
  }
  public void setStreet(String street) {
    this.street = street;
  }
  public String getNr() {
    return nr;
  }
  public void setNr(String nr) {
    this.nr = nr;
  }
}
