package org.pm4j.swt.pb;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommand.CommandSet;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.swt.pb.base.PbViewerToPmBase;

public class PbTree extends PbViewerToPmBase<TreeViewer, Tree, PmObject>  {

  @Override
  protected TreeViewer makeViewerImpl(Composite parentCtxt, PmObject pm) {
    TreeViewer tv = new TreeViewer(parentCtxt);

    tv.setContentProvider(new ContentProvider());

    tv.setLabelProvider(new LabelProvider() {
      @Override public Image getImage(Object element) {
        return PbImageRegistry.findImage((PmObject) element);
      }
      @Override
      public String getText(Object element) {
        return ((PmObject)element).getPmTitle();
      }
    });

    tv.getTree().addMouseListener(new PopupMouseListener());

    return tv;
  }

  public static class PmTreeViewer extends TreeViewer {
    public PmTreeViewer(Composite parent) {
      super(parent);
    }
  }


  @Override
  protected PbBinding makeBinding(PmObject pm) {
    return new Binding();
  }

  /**
   * Handles PM events as well as the SWT modification and focus event.
   */
  public class Binding extends PbViewerToPmBase<TreeViewer, Tree, PmObject>.Binding {

    @Override
    public void bind() {
      super.bind();
      viewer.setInput(pm);
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      viewer.refresh();
    }
  }


  public static class PopupMouseListener extends MouseAdapter {
    private PbMenu pvPopupMenu = new PbMenu.Popup();

    /**
     * Right mouse button click shows the popup menu of the tree node PM.
     */
    @Override
    public void mouseDown(MouseEvent e) {
      if (e.button == 3) {
        Tree tree = (Tree)e.widget;
        TreeItem[] selection = tree.getSelection();
        if (selection.length == 1) {
          if (selection[0].getData() instanceof PmObject) {
            PmObject selectedPm = (PmObject) selection[0].getData();
            List<PmCommand> cmdList = PmUtil.getVisiblePmCommands(selectedPm, CommandSet.POPUP);
            if (cmdList.size() > 0) {
              pvPopupMenu.build(tree, selectedPm);
            }
          }
        }
      }
    }
  };

  private static final Object[] EMPTY_OBJ_ARR = {};
  public static class ContentProvider implements ITreeContentProvider {

    public Object[] getChildren(Object element) {
      if (element instanceof PmObject) {
        return ((PmObject) element).getPmChildNodes().toArray();
      }
      else {
        return EMPTY_OBJ_ARR;
      }
    }

    public Object[] getElements(Object element) {
      return getChildren(element);
    }

    public boolean hasChildren(Object element) {
      return getChildren(element).length > 0;
    }

    public Object getParent(Object element) {
      return ((PmObject) element).getPmParent();
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object old_input, Object new_input) {
    }
  }

}
