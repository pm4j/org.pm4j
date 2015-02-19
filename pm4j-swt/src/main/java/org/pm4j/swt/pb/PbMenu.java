package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.pm4j.core.pb.PbMatcherMapped;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommand.CommandSet;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.pm.impl.commands.PmCommandGroup;
import org.pm4j.core.pm.impl.commands.PmCommandSeparator;
import org.pm4j.swt.pb.base.PbWidgetToPmBase;

public class PbMenu extends PbWidgetToPmBase<Menu, Control, PmObject> {

  public static class Bar extends PbMenu {
    public Bar() {
      super(SWT.BAR, CommandSet.MENU_BAR);
      getPvMap().addMatcher(PmCommand.class, new PbMenuItem(SWT.DROP_DOWN));
    }
  }

  public static class Popup extends PbMenu {
    public Popup() {
      super(SWT.POP_UP, CommandSet.POPUP);
      getPvMap().addMatcher(PmCommandSeparator.class, new PbMenuItem.Separator());
      getPvMap().addMatcher(PmCommandGroup.class, new PbMenuItem.Cascade());
      getPvMap().addMatcher(PmCommand.class, new PbMenuItem.Push());
    }
  }

  private PbMatcherMapped pvMap = new PbMatcherMapped();

  public void setPvMap(PbMatcherMapped pvMap) {
    this.pvMap = pvMap;
  }

  private int style;
  private CommandSet pmCommandSet;

  public PbMenu(int style, CommandSet pmCommandSet) {
    this.style = style;
  }

  @Override
  public Menu makeView(Control parentComponent, PmObject pm) {
    // FIXME olaf: The shell is not always the parent to use...
    return new Menu(parentComponent.getShell(), style);
  }

  @Override
  protected PbBinding makeBinding(PmObject pm) {
    return new Binding();
  }

  public PbMatcherMapped getPvMap() {
    return pvMap;
  }

  public class Binding extends PbWidgetToPmBase<Menu, Control, PmObject>.Binding {

    @Override
    public void bind() {
      super.bind();

      view.setEnabled(pm.isPmEnabled());

      for (PmCommand c : PmUtil.getVisiblePmCommands(pm, pmCommandSet)) {
        if (c.isPmVisible()) {
          pvMap.getPbFactory(c).build(view, c);
        }
      }
      view.setVisible(true);
    }
  }

}
