package org.pm4j.core.sample.filebrowser;

import java.io.File;
import java.util.Date;

import org.pm4j.core.pm.PmAttrDate;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmAttrDateImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.commands.PmCommandSeparator;
import org.pm4j.standards.PmConfirmedCommand;

@PmBeanCfg(beanClass=File.class)
@PmTitleCfg(attrValue="name")
public class FilePmBase extends PmBeanBase<File> {

  /** References by reflection the 'name' property of the File object. */
  public final PmAttrString name = new PmAttrStringImpl(this);

  /** The file modification date. */
  @PmAttrCfg(readOnly=true)
  public final PmAttrDate lastModified = new PmAttrDateImpl(this) {
    @Override protected Date getBackingValueImpl() {
      return new Date(getPmBean().lastModified());
    };
  };

  @PmTitleCfg(resKey="cmd.delete")
  public final PmCommand cmdDelete = new PmConfirmedCommand(this) {

    @Override protected boolean isPmEnabledImpl() {
      return getPmBean().canWrite();
    }

    @Override protected void doItImpl() throws Exception {
      getPmBean().delete();
    }

    @Override protected String getNameOfThingToConfirm() {
      return getPmBean().getName();
    }
  };

  public final PmCommand cmdSeparator = new PmCommandSeparator(this);

}
