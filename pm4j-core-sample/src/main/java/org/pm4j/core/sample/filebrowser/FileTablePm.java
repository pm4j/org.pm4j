package org.pm4j.core.sample.filebrowser;

import java.io.File;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol2;
import org.pm4j.core.pm.annotation.PmTableCfg2;
import org.pm4j.core.pm.impl.PmTableColImpl2;
import org.pm4j.core.pm.impl.PmTableImpl2;

/**
 * PM for a table of files.
 */
@PmTableCfg2(sortable=true)
public class FileTablePm extends PmTableImpl2<FilePmBase, File> {

  public FileTablePm(PmObject pmCtxt) {
    super(pmCtxt);
  }

  //@PmTableColCfg(prefSize="50")
  public final PmTableCol2 name = new PmTableColImpl2(this);
  //@PmTableColCfg(prefSize="100pt")
  public final PmTableCol2 lastModified = new PmTableColImpl2(this);
}
