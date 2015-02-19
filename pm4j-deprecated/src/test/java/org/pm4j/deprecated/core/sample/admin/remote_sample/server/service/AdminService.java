package org.pm4j.deprecated.core.sample.admin.remote_sample.server.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminService {
  
  private Map<Long, Department> idToDepartmentMap = new LinkedHashMap<Long, Department>();
  private Map<Long, SampleUser> idToUserMap = new LinkedHashMap<Long, SampleUser>();
  
  public AdminService() {
    idToDepartmentMap.put(1L, new Department(1, "Production"));
    idToDepartmentMap.put(2L, new Department(2, "Sales"));
    
    idToUserMap.put(1L, new SampleUser() {{
      setId(1L);
      setLoginName("anna");
      setFirstName("Anna");
      setLastName("Müller");
      setDepartment(idToDepartmentMap.get(1L));
    }});

    idToUserMap.put(2L, new SampleUser() {{
      setId(2L);
      setLoginName("franz");
      setFirstName("Franz");
      setLastName("Meier");
      setDepartment(idToDepartmentMap.get(2L));
    }});
  }

  public Collection<Department> getDepartments() {
    return idToDepartmentMap.values();
  }
  
  public Collection<SampleUser> getUsers() {
    return idToUserMap.values();
  }
  
  public SampleUser getUser(long id) {
    return idToUserMap.get(id);
  }
  
  public SampleUser saveUser(SampleUser user) {
    if (user.getId() == null) {
      user.setId(getMaxUserId()+1);
    }
    idToUserMap.put(user.getId(), user);
    return user;
  }
  
  private long getMaxUserId() {
    long max = 0;
    for (Long l : idToUserMap.keySet())
      max = Math.max(max, l);
    return max;
  }
}
