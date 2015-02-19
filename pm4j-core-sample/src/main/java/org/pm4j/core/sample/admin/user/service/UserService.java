package org.pm4j.core.sample.admin.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class UserService {

  private Map<String, User>     nameToUserMap = new TreeMap<String, User>();
  private Map<String, City>     nameToCityMap = new TreeMap<String, City>();
  private Map<String, City>     zipToCityMap = new TreeMap<String, City>();

  public List<User> getUserList() {
    return new ArrayList<User>(nameToUserMap.values());
  }

  public void saveUser(User user) {
    nameToUserMap.put(user.getName(), user);
    user.setId(user.getName());
  }

  public void deleteUser(User user) {
    nameToUserMap.remove(user.getName());
  }

  public User findUserByName(String name) {
    return nameToUserMap.get(name);
  }

  public List<City> getCityList() {
    return new ArrayList<City>(nameToCityMap.values());
  }

  public City findCityByZipCode(String zipCode) {
    return zipToCityMap.get(zipCode);
  }

}
