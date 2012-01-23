package org.pm4j.core.pm.pageable;

import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmLabel;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrCfg.AttrAccessKind;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmLabelImpl;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.pm.impl.SetValueContainer;

@PmTitleCfg(resKeyBase = "pmPager")
@PmBeanCfg(beanClass=PageableCollection.class)
public class PmPagerImpl<T_ITEM>
          extends PmBeanBase<PageableCollection<T_ITEM>>
          implements PmPager<T_ITEM> {

  // TODO olaf: move to meta data
  private int pageSize = 10; // local member, should be initialized by meta data
  // another option: onlyVisibleForMoreThanOnePage
  private boolean visibleForEmptyCollection = false;

  /**
   * The changed state of this element does usually not indicate a real data
   * change Thus it is by default configured to NOT report its changes in
   * {@link #isPmValueChanged()}.<br>
   * However, this definition may be changed by setting
   * {@link #propagateChangedStateToParent} to <code>true</code>.
   */
  private boolean propagateChangedStateToParent = false;

  public PmPagerImpl(PmObject parent) {
    super(parent, null);
  }

  @Override
  protected boolean isPmVisibleImpl() {
    return visibleForEmptyCollection ||
           (getPmBean().getNumOfItems() > 0);
  }

  public final PmCommand cmdFirst = new PmCommandImpl(this) {
    @Override
    protected boolean isPmEnabledImpl() {
        return PageableCollectionUtil.hasPrevPage(getPmBean());
    }

    @Override
    protected void doItImpl() throws Exception {
        PageableCollectionUtil.navigateToFirstPage(getPmBean());
    }
  };

  public final PmCommand cmdPrev = new PmCommandImpl(this) {
    @Override
    protected boolean isPmEnabledImpl() {
        return PageableCollectionUtil.hasPrevPage(getPmBean());
    }

    @Override
    protected void doItImpl() throws Exception {
        PageableCollectionUtil.navigateToPrevPage(getPmBean());
    }
  };

  public final PmCommand cmdNext = new PmCommandImpl(this) {
    @Override
    protected boolean isPmEnabledImpl() {
      return PageableCollectionUtil.hasNextPage(getPmBean());
    }

    @Override
    protected void doItImpl() throws Exception {
      PageableCollectionUtil.navigateToNextPage(getPmBean());
    }
  };

  public final PmCommand cmdLast = new PmCommandImpl(this) {
      @Override
      protected boolean isPmEnabledImpl() {
          return PageableCollectionUtil.hasNextPage(getPmBean());
      }

      @Override
      protected void doItImpl() throws Exception {
          PageableCollectionUtil.navigateToLastPage(getPmBean());
      }
  };

  public final PmLabel itemXtillYofZ = new PmLabelImpl(this) {
    @Override
    protected String getPmTitleImpl() {
        PageableCollection<T_ITEM> ps = getPmBean();
        return localize(getPmResKey(),
                PageableCollectionUtil.getIdxOfFirstItemOnPage(ps),
                PageableCollectionUtil.getIdxOfLastItemOnPage(ps),
                ps.getNumOfItems());
    }
  };

  public final PmAttrInteger currentPageIdx = new PmAttrIntegerImpl(this) {
      @Override
      protected boolean isPmEnabledImpl() {
          return getNumOfPages() > 1;
      }

      /**
       * Simply ignores invalid values.
       * <p>
       * TODO olaf: does not handle converter problems. (non numeric values)
       */
      @Override
      protected boolean setValueImpl(SetValueContainer<Integer> value) {
          Integer newValue = value.getPmValue();
          if (newValue != null &&
              newValue > 0 &&
              newValue <= getNumOfPages() ) {
              return super.setValueImpl(value);
          }
          else {
              return false;
          }
      }
  };

  @PmAttrCfg(accessKind = AttrAccessKind.OVERRIDE)
  public final PmAttrBoolean allOnPageSelected = new PmAttrBooleanImpl(this) {
      @Override
      protected boolean isPmVisibleImpl() {
          return getPmBean().isMultiSelect();
      }

      /**
       * Is only enabled if the pager is enabled.
       */
      @Override
      protected boolean isPmEnabledImpl() {
        return super.isPmEnabledImpl() &&
               getPmParent().isPmEnabled();
      };

      @Override
      protected Boolean getBackingValueImpl() {
          return PageableCollectionUtil.isAllOnPageSelected(getPmBean());
      }

      @Override
      protected void setBackingValueImpl(Boolean value) {
          if (value != null) {
              PageableCollectionUtil.setAllOnPageSelected(getPmBean(), value.booleanValue());
          }
      }
  };

  @PmCommandCfg(requiresValidValues=false)
  public final PmCommand cmdSelectAllOnPage = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      PageableCollectionUtil.setAllOnPageSelected(getPmBean(), Boolean.TRUE);
    }
    @Override
    protected boolean isPmEnabledImpl() {
      return allOnPageSelected.isPmEnabled();
    }
  };

  @PmCommandCfg(requiresValidValues=false)
  public final PmCommand cmdDeSelectAllOnPage = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      PageableCollectionUtil.setAllOnPageSelected(getPmBean(), Boolean.FALSE);
    }
    @Override
    protected boolean isPmEnabledImpl() {
      return allOnPageSelected.isPmEnabled();
    }
  };

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public void setPageSize(int pageSize) {
    boolean changed = (this.pageSize != pageSize);

    getPmBean().setPageSize(pageSize);
    this.pageSize = pageSize;

    // TODO olaf: add change notification.
//    if (changed &&
//        pm)
  }

  @Override
  public int getNumOfItems() {
      return getPmBean().getNumOfItems();
  }

  @Override
  public int getNumOfPages() {
      return PageableCollectionUtil.getNumOfPages(getPmBean());
  }

  /** Provides an initial empty backing bean if there is none. */
  @Override
  protected PageableCollection<T_ITEM> getPmBeanImpl() {
      return new PageableListImpl<T_ITEM>(null, 1, false);
  }

  /**
   * The changed state of this element does usually not indicate a real data
   * change Thus it is by default configured to NOT report its changes in
   * {@link #isPmValueChanged()}.<br>
   * However, this definition may be changed by setting
   * {@link #propagateChangedStateToParent} to <code>true</code>.
   */
  @Override
  public boolean isPmValueChanged() {
    return  propagateChangedStateToParent &&
            super.isPmValueChanged();
  }

  /**
   * PM base class for items that support item selection functionality.
   *
   * @param <T_BEAN>
   *            Type of the items.
   */
  public static class SelectableItemPm<T_BEAN> extends PmBeanBase<T_BEAN> {

      public final PmAttrBoolean selected = new PmAttrBooleanImpl(this) {
          @Override
          protected Boolean getBackingValueImpl() {
              return getPageableObjectSet().isSelected(getPmBean());
          }

          @Override
          protected void setBackingValueImpl(Boolean value) {
              if (value == Boolean.TRUE) {
                  getPageableObjectSet().select(getPmBean());
              } else {
                  getPageableObjectSet().deSelect(getPmBean());
              }
          }

          private PageableCollection<T_BEAN> getPageableObjectSet() {
              @SuppressWarnings("unchecked")
              PmPagerImpl<T_BEAN> parent = PmUtil.getPmParentOfType(this, PmPagerImpl.class);
              return parent.getPmBean();
          }
      };

      public PmAttrBoolean getSelected() {
          return selected;
      }
  }


  // -- getter / setter --

  @Override
  public PmCommand getCmdFirstPage() { return cmdFirst; }
  @Override
  public PmCommand getCmdPrevPage() { return cmdPrev; }
  @Override
  public PmCommand getCmdNextPage() { return cmdNext; }
  @Override
  public PmCommand getCmdLastPage() { return cmdLast; }
  @Override
  public PmLabel getItemXtillYofZ() { return itemXtillYofZ; }
  @Override
  public PmAttrInteger getCurrentPageIdx() { return currentPageIdx; }
  @Override
  public PmAttrBoolean getAllOnPageSelected() { return allOnPageSelected; }

  public PmCommand getCmdSelectAllOnPage() { return cmdSelectAllOnPage; }
  public PmCommand getCmdDeSelectAllOnPage() { return cmdDeSelectAllOnPage; }

}
