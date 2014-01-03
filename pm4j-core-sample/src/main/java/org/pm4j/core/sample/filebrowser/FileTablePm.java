package org.pm4j.core.sample.filebrowser;

import java.io.File;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.annotation.PmTableCfg;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;

/**
 * PM for a table of files.
 */
@PmTableCfg(sortable=true)
public class FileTablePm extends PmTableImpl<FilePmBase, File> {

  public FileTablePm(PmObject pmCtxt) {
    super(pmCtxt);
  }

  //@PmTableColCfg(prefSize="50")
  public final PmTableCol name = new PmTableColImpl(this);
  //@PmTableColCfg(prefSize="100pt")
  public final PmTableCol lastModified = new PmTableColImpl(this);
}
