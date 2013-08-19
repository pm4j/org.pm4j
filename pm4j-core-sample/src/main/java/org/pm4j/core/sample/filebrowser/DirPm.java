package org.pm4j.core.sample.filebrowser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.util.collection.ListUtil;
import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTreeNode;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.sample.filebrowser.FileUtil.ListKind;
import org.pm4j.standards.PmConfirmedCommand;
import org.pm4j.standards.PmInputDialog;

/**
 * PM for a directories.
 */
public class DirPm extends FilePmBase {

  /** The set of subdirectories. */
  @PmFactoryCfg(beanPmClasses=DirPm.class)
  public final PmAttrPmList<DirPm> subDirs = new PmAttrPmListImpl<DirPm, File>(this) {

    @Override protected Collection<File> getBackingValueImpl() {
      return FileUtil.getSubFiles(getPmBean(), ListKind.DIR);
    };

    /** Show details as a table of directories. */
    @Override protected PmObject getNodeDetailsPmImpl() {
      return new FileTablePm(this) {
        @SuppressWarnings("unchecked")
        protected Collection<File> getPmBeansImpl() {
          return (Collection<File>)(Object) subDirs.getValue();
        }
      };
    }
  };

  /** The set of files within the directory. */
  @PmFactoryCfg(beanPmClasses=FilePm.class)
  public final PmAttrPmList<FilePm> files = new PmAttrPmListImpl<FilePm, File>(this) {

    @Override protected Collection<File> getBackingValueImpl() {
      return FileUtil.getSubFiles(getPmBean(), ListKind.FILE);
    };

    /** Show details as a table of files. */
    @Override protected PmObject getNodeDetailsPmImpl() {
      return new FileTablePm(this) {
        @SuppressWarnings("unchecked")
        protected Collection<File> getPmBeansImpl() {
          return (Collection<File>)(Object) files.getValue();
        }
      };
    }
  };

  public final PmCommand cmdMakeSubDir = new PmConfirmedCommand(this) {
    @Override
    protected void doItImpl() throws Exception {
      System.out.println("Stelle Unterverzeichnis her in : " + getPmBean());
    };

    @Override
    protected PmElement makeConfirmDialogPm() {
      return new PmInputDialog(this) {
        @Override
        protected void onOk() {
          // FIXME: call the doIt ?
          File file = new File(getPmBean(), this.name.getValue());
          try {
            file.createNewFile();
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      };
    }
  };

  @Override
  protected List<PmCommand> getVisiblePmCommands(PmCommand.CommandSet commandSet) {
    return Arrays.asList(cmdMakeSubDir, cmdDelete);
  };

  /** Provides the tree-view sub-nodes. */
  @Override
  protected List<? extends PmTreeNode> getPmChildNodesImpl() {
    return ListUtil.collectionsToList(subDirs.getValue(), files.getValue());
  };

  /** Show details as a table of files. */
  @Override
  public PmObject getNodeDetailsPm() {
    return files.getNodeDetailsPm();
  };

  @Override
  public boolean isPmTreeLeaf() {
    return false;
  }

  /** Convenience PM factory method. */
  public static DirPm makeDirPm(String path) {
    PmConversation s = new PmConversationImpl(DirPm.class);
    return PmFactoryApi.getPmForBean(s, new File(path));
  }

}
