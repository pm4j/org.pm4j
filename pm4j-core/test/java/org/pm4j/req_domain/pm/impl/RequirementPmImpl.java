package org.pm4j.req_domain.pm.impl;

import java.io.Serializable;

import org.pm4j.core.pm.PmAttrLong;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.impl.PmAttrLongImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.req_domain.model.Requirement;
import org.pm4j.req_domain.pm.RequirementPm;


@PmBeanCfg(beanClass=Requirement.class)
public class RequirementPmImpl extends PmBeanBase<Requirement> implements RequirementPm {

  public final PmAttrLongImpl id = new IdPm(this);

  public final PmAttrString name = new NamePm(this);

  public final PmAttrString description = new DescriptionPm(this);

  public final PmAttrString author = new AuthorPm(this);

  public PmAttrLong getId() {
    return id;
  }

  public PmAttrString getName() {
    return name;
  }

  public PmAttrString getDescription() {
    return description;
  }

  public PmAttrString getAuthor() {
    return author;
  }

  @Override
  public Serializable getPmKey() {
    return getPmBean().getId();
  }

  private class IdPm extends PmAttrLongImpl {
    public IdPm(RequirementPmImpl pmParentBean) {
      super(pmParentBean);
    }

    @Override
    protected Long getBackingValueImpl() {
      return getPmBean().getId();
    }

    @Override
    protected void setBackingValueImpl(Long fieldVal) {
      getPmBean().setId(fieldVal);
    }
  }

  private class NamePm extends PmAttrStringImpl {
    public NamePm(RequirementPmImpl pmParentBean) {
      super(pmParentBean);
    }

    @Override
    protected String getBackingValueImpl() {
      return getPmBean().getName();
    }

    @Override
    protected void setBackingValueImpl(String fieldVal) {
      getPmBean().setName(fieldVal);
    }
  }

  private class DescriptionPm extends PmAttrStringImpl {
    public DescriptionPm(RequirementPmImpl pmParentBean) {
      super(pmParentBean);
    }

    @Override
    public int getMaxLen() {
      return 1000;
    }

    @Override
    protected String getBackingValueImpl() {
      return getPmBean().getDescription();
    }

    @Override
    protected void setBackingValueImpl(String fieldVal) {
      getPmBean().setDescription(fieldVal);
    }
  }

  private class AuthorPm extends PmAttrStringImpl {
    public AuthorPm(RequirementPmImpl pmParentBean) {
      super(pmParentBean);
    }

    @Override
    protected String getBackingValueImpl() {
      return getPmBean().getAuthor();
    }

    @Override
    protected void setBackingValueImpl(String fieldVal) {
      getPmBean().setAuthor(fieldVal);
    }
  }

}

