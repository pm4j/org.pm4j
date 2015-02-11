package org.pm4j.swing.pb;

import java.awt.Container;
import java.util.Collections;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.core.pm.PmObject;
import org.pm4j.swing.pb.base.PbComponentToPmBase;

public class PbJTree extends PbComponentToPmBase<JTree, PmObject> {

  @Override
  public JTree makeView(Container parent, PmObject pm) {
    JTree tree = new JTree();
    parent.add(tree);
    return tree;
  }

  @Override
  protected PbBinding makeBinding(PmObject pm) {
    return new Binding();
  }

  protected class Binding extends PbComponentToPmBase<JTree, PmObject>.Binding {
    @Override
    public void bind() {
      super.bind();
      view.setModel(new PmTreeModel(pm));
    }
  }

  public static class PmTreeModel implements TreeModel {

    protected final PmObject pm;

    public PmTreeModel(PmObject pm) {
      this.pm = pm;
    }

    @Override
    public Object getRoot() {
      return pm;
    }

    @Override
    public Object getChild(Object parent, int index) {
      List<PmObject> childList = getChildModels(parent);
      if (index >= childList.size()) {
        // This method should not return null. So we provide
        // the last item or this instance as a replacement...
        return (childList.size() > 0)
                  ? childList.get(childList.size() - 1)
                  : this;
      }
      return childList.get(index);
    }

    @Override
    public int getChildCount(Object parent) {
      return getChildModels(parent).size();
    }

    @Override
    public boolean isLeaf(Object node) {
      return ((PmObject)node).isPmTreeLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
      // TODO Auto-generated method stub
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
      int i = 0;
      for (PmObject n : getChildModels(parent)) {
        if (ObjectUtils.equals(n, child)) {
          return i;
        }
        ++i;
      }
      return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
      // TODO Auto-generated method stub
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
      // TODO Auto-generated method stub
    }

    @SuppressWarnings("unchecked")
    protected List<PmObject> getChildModels(Object parent) {
      List<PmObject> list = (List<PmObject>) ((PmObject)parent).getPmChildNodes();
      return list != null ? list : Collections.EMPTY_LIST;
    }
  }

}
