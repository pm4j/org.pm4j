package org.pm4j.core.pm.impl;

import java.util.List;
import java.util.Set;

import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmCacheApi;

/**
 * @deprecated Please use {@link org.pm4j.core.pm.impl.PmObjectBase}.
 */
@Deprecated
public abstract class PmElementBase
        extends PmObjectBase
        implements PmElement {

  /** Cached child nodes for tree interface. */
  private List<PmObject> pmChildNodes = null;

  /**
   * Constructor for dependency injection frameworks that are not able to use constructors
   * (e.g. JSF).<br>
   * Please make sure that the method {@link #setPmParent(PmObject)} is called
   * before this object will be used!
   */
  public PmElementBase() {
    this(null);
  }

  /**
   * @param pmParent
   *          The context, this pm was created in. E.g. a session, a command, a
   *          list field.
   */
  public PmElementBase(PmObject pmParent) {
    super(pmParent);
  }

  @Override
  protected void clearCachedPmValues(Set<PmCacheApi.CacheKind> cacheSet) {
    super.clearCachedPmValues(cacheSet);
    // re-create the cached tree child nodes on next getPmChildeNodes call.
    pmChildNodes = null;
  }

  @Override
  protected boolean isPmReadonlyImpl() {
    return super.isPmReadonlyImpl()
           || !isPmEnabled();
      // FIXME 00112472 oboede: this is a kind of bug. can lead to circular
      // enabled/readonly calls
      // current feature: setPmEnabled(false) or isPmEnabledImpl() make an area
      // read-only.
      //
      // This logic may be usedful for tabs but not for every PmContainer.
      //
      // We can get rid of that by finding all
      // subclasses implementing isPmEnabledImpl().
      // All found logic needs to be switched to isPmReadonlyImpl().
      // After that step this method should be deleted.
      // The tab case should be added to the base class (each PmObject may be a tab).
  }

  // ======== meta data ======== //

  @Override
  protected MetaData makeMetaData() {
    return new MetaData();
  }
  
  protected static class MetaData extends PmObjectBase.MetaData {
    @SuppressWarnings("unchecked")
    @Override
    protected List<PmObject> getPmChildNodes(PmObjectBase pm) {
      PmElementBase pe = (PmElementBase) pm;
      if (pe.pmChildNodes == null) {
        pe.pmChildNodes = (List<PmObject>)pe.getPmChildNodesImpl();
      }
      return pe.pmChildNodes;
    }
  }

}
