package org.pm4j.sample.filebrowser;

import org.pm4j.core.sample.filebrowser.DirPm;
import org.pm4j.core.sample.filebrowser.FilePm;
import org.pm4j.deprecated.core.pm.DeprPmTable;
import org.pm4j.swt.pb.PbTable;
import org.pm4j.swt.pb.PbTreeWithDetails;
import org.pm4j.swt.pb.composite.PbGridLayout;
import org.pm4j.swt.testtools.SwtTestShell;

public class FileBrowser {

  public static void main(String[] args) {
    SwtTestShell s = new SwtTestShell(500, 350, "File Tree Demo");

    PbTreeWithDetails pv = new PbTreeWithDetails();

    pv.getDetailsBinderMap().addKeyToViewMatches(
        DeprPmTable.class, new PbTable(),
        FilePm.class, new PbGridLayout.AllAttrs());
    pv.getDetailsBinderMap().
          addMatcher(DeprPmTable.class, new PbTable()).
          addMatcher(FilePm.class, new PbGridLayout.AllAttrs());

    pv.build(s.getShell(), DirPm.makeDirPm("/"));
    s.show();
  }

}
