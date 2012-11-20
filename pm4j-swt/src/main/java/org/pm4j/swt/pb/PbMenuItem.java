package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.pm4j.core.pb.PbMatcherMapped;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.pm.impl.commands.PmCommandGroup;
import org.pm4j.core.pm.impl.commands.PmCommandSeparator;
import org.pm4j.swt.pb.base.PbWidgetToPmBase;
import org.pm4j.swt.pb.listener.CommandExecListener;

public class PbMenuItem extends PbWidgetToPmBase<MenuItem, Menu, PmCommand>
{

  public static class Push extends PbMenuItem {
    public Push() {
      super(SWT.PUSH);
    }
  }

  public static class Cascade extends PbMenuItem {
    private PbMatcherMapped binderMap;

    public Cascade() {
      super(SWT.CASCADE);
      binderMap = new PbMatcherMapped();
      binderMap.addMatcher(PmCommandSeparator.class, new PbMenuItem.Separator());
      binderMap.addMatcher(PmCommandGroup.class, this);
      binderMap.addMatcher(PmCommand.class, new PbMenuItem.Push());
    }

    @Override
    public MenuItem makeView(Menu parentComponent, PmCommand pm) {
      MenuItem item = super.makeView(parentComponent, pm);
      item.setMenu(new Menu(item));
      return item;
    }

    @Override
    protected PbBinding makeBinding(PmCommand pm) {
      return new Binding();
    }

    class Binding extends PbMenuItem.Binding {
      @Override
      public void bind() {
        super.bind();
        Menu parentMenu = view.getMenu();
        for (PmCommand c : PmUtil.getVisiblePmCommands(pm)) {
          binderMap.getPbFactory(c).build(parentMenu, c);
        }
      }
    }
  }

  public static class Separator extends PbMenuItem {
    public Separator() {
      super(SWT.SEPARATOR);
    }

    @Override
    protected PbBinding makeBinding(PmCommand pm) {
      return new Binding();
    }

    class Binding extends PbMenuItem.Binding {
      @Override
      public void bind() {
        super.bind();
        view.setText(pm.getPmTitle());
        view.setEnabled(pm.isPmEnabled());
      }
    }
  }

  private final int style;

  public PbMenuItem(int style) {
    this.style = style;
  }

  @Override
  public MenuItem makeView(Menu parentComponent, PmCommand pm) {
    return new MenuItem(parentComponent, style);
  }

  @Override
  protected PbBinding makeBinding(PmCommand pm) {
    return new Binding();
  }

  public class Binding extends PbWidgetToPmBase<MenuItem, Menu, PmCommand>.Binding {

    @Override
    public void bind() {
      super.bind();
      view.setText(pm.getPmTitle());
      view.setEnabled(pm.isPmEnabled());
      view.setData(pm);
      view.setImage(PbImageRegistry.findImage(pm));
      if (view.isEnabled()) {
        view.addSelectionListener(new CommandExecListener());
      }
    }
  }

}
