package org.pm4j.jface.pb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.core.pm.PmAttrLong;
import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmTreeNode;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmAttrLongImpl;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.swt.pb.PbTree;
import org.pm4j.swt.testtools.SwtTestShell;

public class FileTreeCheck {

  @PmBeanCfg(beanClass=File.class) @PmTitleCfg(attrValue="name")
  public static class FilePmBase extends PmBeanBase<File> {
    public final PmAttrString name = new PmAttrStringImpl(this);
    public final PmAttrLong totalSpace = new PmAttrLongImpl(this);
  }

  public static class DirPm extends FilePmBase {

    @PmFactoryCfg(beanPmClasses=DirPm.class)
    public final PmAttrPmList<DirPm> subDirs = new PmAttrPmListImpl<DirPm, File>(this) {
      protected Collection<File> getBackingValueImpl() {
        return getSubFiles(getPmBean(), ListKind.DIR);
      };
    };

    @PmFactoryCfg(beanPmClasses=FilePm.class)
    public final PmAttrPmList<FilePm> files = new PmAttrPmListImpl<FilePm, File>(this) {
      protected Collection<File> getBackingValueImpl() {
        return getSubFiles(getPmBean(), ListKind.FILE);
      };
    };

    protected List<? extends PmTreeNode> getPmChildNodesImpl() {
      return subDirs.getValue();
    };
  }

  public static class FilePm extends FilePmBase {
  }



  public static void main(String[] args) {
    SwtTestShell s = new SwtTestShell(500, 350, "File Tree Demo");
    new PbTree().build(s.getShell(), getRootDirPm("/prj/mecom"));
    s.show();
  }




  private static DirPm getRootDirPm(String path) {
    PmConversation s = new PmConversationImpl(DirPm.class);
    return PmFactoryApi.getPmForBean(s, new File(path));
  }

  enum ListKind { FILE, DIR, ALL };
  private static List<File> getSubFiles(File dir, ListKind listDirs) {
    List<File> list = new ArrayList<File>();
    File[] fileArray = dir.listFiles();
    if (fileArray != null) {
      for (File f : fileArray) {
        if ( (listDirs == ListKind.ALL) ||
             (listDirs == ListKind.DIR && f.isDirectory()) ||
             (listDirs == ListKind.FILE && !f.isDirectory())
           ) {
          list.add(f);
        }
      }
    }
    return list;
  }


}
