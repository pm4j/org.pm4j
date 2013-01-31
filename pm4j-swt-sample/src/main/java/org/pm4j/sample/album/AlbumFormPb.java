package org.pm4j.sample.album;

import org.eclipse.swt.widgets.Composite;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmEventApiHandler;
import org.pm4j.core.sample.album.AlbumConversationPm;
import org.pm4j.core.sample.album.AlbumFormPm;
import org.pm4j.core.sample.album.AlbumPm;
import org.pm4j.swt.pb.base.PbFormBase;
import org.pm4j.swt.testtools.SwtTestShell;

public class AlbumFormPb extends PbFormBase<AlbumForm, AlbumFormPm> {

  @Override
  public AlbumForm makeView(Composite parent, AlbumFormPm pm) {
    return new AlbumForm(parent, swtStyle);
  }

  @Override
  protected void bindImpl(AlbumForm view, AlbumFormPm pm) {
    AlbumPm albumPm = pm.album;

    bindAttr(view.titleLabel, view.titleText, albumPm.title);
    bindAttr(view.artistLabel, view.artistText, albumPm.artist);
    bindAttr(view.classicalLabel, view.classicalCheckbox, albumPm.classical);
    bindAttr(view.composerLabel, view.composerText, albumPm.composer);
    bindAttr(null, view.albumSelectionList, pm.albumSelection);

    bindCommand(view.btnCancel, pm.cmdCancel);
    bindCommand(view.btnSave, pm.cmdSave);
  }


  public static void main(String[] args) {
    PmEventApi.setApiHandler(new PmEventApiHandler.WithThreadLocalEventSource());
    SwtTestShell s = new SwtTestShell(450, 200, "Album Demo Application");
    AlbumFormPm albumFormPm = new AlbumFormPm(new AlbumConversationPm());
    new AlbumFormPb().build(s.getShell(), albumFormPm);
    s.show();
  }

}
