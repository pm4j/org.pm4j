package org.pm4j.swing.pb;

import java.awt.Container;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.core.pm.PmTreeNode;
import org.pm4j.swing.pb.base.PbComponentToPmBase;

public class PbJTree extends PbComponentToPmBase<JTree, PmTreeNode> {

  @Override
  public JTree makeView(Container parent, PmTreeNode pm) {
    JTree tree = new JTree();
    parent.add(tree);
    return tree;
  }

  @Override
  protected PbBinding makeBinding(PmTreeNode pm) {
    return new Binding();
  }
  
  protected class Binding extends PbComponentToPmBase<JTree, PmTreeNode>.Binding {
    @Override
    public void bind() {
      super.bind();
      view.setModel(new PmTreeModel(pm));
    }
  }
  
  public static class PmTreeModel implements TreeModel {

    protected final PmTreeNode pm;
    
    public PmTreeModel(PmTreeNode pm) {
      this.pm = pm;
    }
    
    @Override
    public Object getRoot() {
      return pm;
    }

    @Override
    public Object getChild(Object parent, int index) {
      List<PmTreeNode> childList = getChildModels(parent);
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
      return ((PmTreeNode)node).isPmTreeLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
      // TODO Auto-generated method stub
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
      int i = 0;
      for (PmTreeNode n : getChildModels(parent)) {
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

    protected List<PmTreeNode> getChildModels(Object parent) {
      return ((PmTreeNode)parent).getPmChildNodes();
    }
  }
  
}
