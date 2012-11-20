package org.pm4j.core.pm.impl;

import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.DO_NOTHING;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmCommandDecorator;
import org.pm4j.core.pm.PmLabel;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmPager2;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmLocalizeApi;

/**
 * Implementation for standard pager functionality.
 *
 * @author olaf boede
 *
 * @param <T_ITEM>
 */
@PmTitleCfg(resKeyBase = "pmPager")
@PmBeanCfg(beanClass=PageableCollection2.class)
public class PmPagerImpl2
          extends PmBeanBase<PageableCollection2<?>>
          implements PmPager2 {

  /** The pager visibility condition. */
  private PagerVisibility pagerVisibility = PagerVisibility.ALWAYS;

  /**
   * The changed state of this element does usually not indicate a real data
   * change Thus it is by default configured to NOT report its changes in
   * {@link #isPmValueChanged()}.<br>
   * However, this definition may be changed by setting
   * {@link #propagateChangedStateToParent} to <code>true</code>.
   */
  private boolean propagateChangedStateToParent = false;

  private PmCommandDecoratorSetImpl pageChangeDecorators = new PmCommandDecoratorSetImpl();

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdFirst = new PmCommandImpl(this) {
    @Override
    protected boolean isPmEnabledImpl() {
        return PageableCollectionUtil2.hasPrevPage(getPmBean());
    }

    @Override
    protected void doItImpl() {
        PageableCollectionUtil2.navigateToFirstPage(getPmBean());
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdPrev = new PmCommandImpl(this) {
    @Override
    protected boolean isPmEnabledImpl() {
        return PageableCollectionUtil2.hasPrevPage(getPmBean());
    }

    @Override
    protected void doItImpl() {
        PageableCollectionUtil2.navigateToPrevPage(getPmBean());
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdNext = new PmCommandImpl(this) {
    @Override
    protected boolean isPmEnabledImpl() {
      return PageableCollectionUtil2.hasNextPage(getPmBean());
    }

    @Override
    protected void doItImpl() {
      PageableCollectionUtil2.navigateToNextPage(getPmBean());
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdLast = new PmCommandImpl(this) {
      @Override
      protected boolean isPmEnabledImpl() {
          return PageableCollectionUtil2.hasNextPage(getPmBean());
      }

      @Override
      protected void doItImpl() {
          PageableCollectionUtil2.navigateToLastPage(getPmBean());
      }
  };

  public final PmLabel itemXtillYofZ = new PmLabelImpl(this) {
    @Override
    protected String getPmTitleImpl() {
        PageableCollection2<?> ps = getPmBean();
        return PmLocalizeApi.localize(this, getPmResKey(),
                PageableCollectionUtil2.getIdxOfFirstItemOnPage(ps),
                PageableCollectionUtil2.getIdxOfLastItemOnPage(ps),
                ps.getNumOfItems());
    }
  };

  public final PmAttrInteger currentPageIdx = new PmAttrIntegerImpl(this) {
    @Override
    protected boolean isPmReadonlyImpl() {
        return getNumOfPages() < 2;
    }

    /**
     * Simply ignores invalid values.
     */
    protected boolean beforeValueChange(Integer oldValue, Integer newValue) {
      return  (newValue != null) &&
              (newValue > 0) &&
              (newValue <= getNumOfPages());
    }

    /** Is not required. Even if the bound value is an 'int' scalar. */
    @Override
    protected boolean isRequiredImpl() {
      return false;
    }

    /**
     * Refuses values out of range and considers the restrictions of registered page change decorators.
     */
    @Override
    protected void onPmInit() {
      addValueChangeDecorator(pageChangeDecorators);
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdSelectAllOnPage = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      PageableCollectionUtil2.setAllOnPageSelected(getPmBean(), Boolean.TRUE);
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdDeSelectAllOnPage = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      PageableCollectionUtil2.setAllOnPageSelected(getPmBean(), Boolean.FALSE);
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdSelectAll = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      getPmBean().getSelectionHandler().selectAll(true);
    }
  };

  @PmCommandCfg(beforeDo=DO_NOTHING)
  public final PmCommand cmdDeSelectAll = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      getPmBean().getSelectionHandler().selectAll(false);
    }
  };

  public PmPagerImpl2(PmObject parent) {
    super(parent, null);
    cmdFirst.addCommandDecorator(pageChangeDecorators);
    cmdPrev.addCommandDecorator(pageChangeDecorators);
    cmdNext.addCommandDecorator(pageChangeDecorators);
    cmdLast.addCommandDecorator(pageChangeDecorators);
  }

  @Override
  protected boolean isPmVisibleImpl() {
    switch (pagerVisibility) {
      case ALWAYS:                  return true;
      case WHEN_SECOND_PAGE_EXISTS: return getPmBean().getNumOfItems() > getPageSize();
      case WHEN_TABLE_IS_NOT_EMPTY: return getPmBean().getNumOfItems() > 0;
      case NEVER:                   return false;
      default: throw new PmRuntimeException(this, "Unknown enum value: " + pagerVisibility);
    }
  }

  @Override
  public int getPageSize() {
    PageableCollection2<?> pc = getPmBean();
    return pc != null
        ? getPmBean().getPageSize()
        : 0;
  }

  @Override
  public int getNumOfItems() {
    PageableCollection2<?> pc = getPmBean();
    return pc != null
        ? (int)getPmBean().getNumOfItems()
        : 0;
  }

  @Override
  public int getNumOfPages() {
    PageableCollection2<?> pc = getPmBean();
    return pc != null
        ? PageableCollectionUtil2.getNumOfPages(getPmBean())
        : 0;
  }

  /**
   * The changed state of this element does usually not indicate a real data
   * change Thus it is by default configured to NOT report its changes in
   * {@link #isPmValueChanged()}.<br>
   * However, this definition may be changed by setting
   * {@link #propagateChangedStateToParent} to <code>true</code>.
   */
  @Override
  protected boolean isPmValueChangedImpl() {
    return  propagateChangedStateToParent &&
            super.isPmValueChanged();
  }

  @Override
  public void setPageableCollection(PageableCollection2<?> pageableCollection) {
    setPmBean(pageableCollection);
  }

  @Override
  public void addPageChangeDecorator(PmCommandDecorator decorator) {
    pageChangeDecorators.addDecorator(decorator);
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
  public PmCommand getCmdSelectAllOnPage() { return cmdSelectAllOnPage; }
  @Override
  public PmCommand getCmdDeSelectAllOnPage() { return cmdDeSelectAllOnPage; }
  @Override
  public PmCommand getCmdSelectAll() { return cmdSelectAll; }
  @Override
  public PmCommand getCmdDeSelectAll() { return cmdDeSelectAll; }
  @Override
  public PagerVisibility getPagerVisibility() { return pagerVisibility; }
  @Override
  public void setPagerVisibility(PagerVisibility pagerVisibility) { this.pagerVisibility = pagerVisibility; }
}
