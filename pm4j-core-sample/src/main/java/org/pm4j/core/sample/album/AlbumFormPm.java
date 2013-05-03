package org.pm4j.core.sample.album;

import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.CLEAR;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.sample.album.domain.Album;
import org.pm4j.core.sample.album.domain.AlbumService;
import org.pm4j.standards.PmConfirmedCommand;

public class AlbumFormPm extends PmElementImpl {

  public AlbumFormPm(PmObject pmParent) {
    super(pmParent);
  }

  /** The service used to store and load the album entities. */
  @PmInject private AlbumService albumService;

  /** PM of the currently opened/edited album. */
  @PmTitleCfg(resKeyBase="albumPm")
  public final AlbumPm album = new AlbumPm(this, null);

  /** This single select attribute selects the album to show/edit. */
  public final PmAttr<Album> albumSelection = new PmAttrImpl<Album>(this) {
    @Override
    protected void onPmValueChange(PmEvent event) {
      album.setPmBean(getValue());
    }
    @PmOptionCfg(id="id", title="title", sortBy="title", nullOption=NullOption.NO)
    public Iterable<Album> getOptionValues() {
      return albumService.getAlbumList();
    }
  };

  /** Updates or creates the edited album. */
  public final PmCommand cmdSave = new PmCommandImpl(this) {

    @Override
    protected void doItImpl() {
      albumService.save(album.getPmBean());
      PmEventApi.firePmEvent(albumSelection, PmEvent.OPTIONSET_CHANGE);
    }

    protected boolean isPmEnabledImpl() {
      return album.isPmValueChanged();
    }
  };

  /**
   * Clears the edited album.<br>
   * Asks the user for confirmation if the currently edited album is changed.
   */
  @PmCommandCfg(beforeDo=CLEAR)
  public final PmCommand cmdCancel = new PmConfirmedCommand(this) {

    protected boolean isPmEnabledImpl() {
      return album.getPmBean().getId() != null ||
             album.isPmValueChanged();
    }

    @Override
    protected boolean shouldAskForConfirmation() {
      return album.isPmValueChanged();
    }

    @Override
    protected void doItImpl() {
      albumSelection.setValue(null);
      album.setPmBean(null);
    }
  };

  protected void onPmInit() {
    // XXX olaf: this is some very common ui functionality.
    //           it should be supported directly by an event dependency specification.
    //           In addition the number of fired events should be minimized.
    //           In this case the enablement change event should only be fired if the
    //           enabled state really changes.
    PmEventApi.addHierarchyListener(this, PmEvent.VALUE_CHANGED_STATE_CHANGE,
        new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        if (event.pm != cmdSave && event.pm != cmdCancel) {
          PmEventApi.firePmEvent(cmdSave, PmEvent.ENABLEMENT_CHANGE);
          PmEventApi.firePmEvent(cmdCancel, PmEvent.ENABLEMENT_CHANGE);
        }
      }
    });
  }
}
