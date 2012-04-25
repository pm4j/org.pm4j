package org.pm4j.swing.sample.album;

import java.awt.Container;
import java.awt.Frame;

import javax.swing.JDialog;

import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmEventApiHandler;
import org.pm4j.core.sample.album.AlbumFormPm;
import org.pm4j.core.sample.album.AlbumPm;
import org.pm4j.core.sample.album.AlbumSessionPm;
import org.pm4j.swing.pb.base.PbFormBase;

public class AlbumFormPb extends PbFormBase<AlbumForm, AlbumFormPm> {

  @Override
  public AlbumForm makeView(Container parent, AlbumFormPm pm) {
    return new AlbumForm();
  }

  @Override
  protected void bindImpl(AlbumForm view, AlbumFormPm pm) {

    AlbumPm albumPm = pm.album;

    bindAttr(null, view.albumSelectionList, pm.albumSelection);
    bindAttr(view.titleLabel, view.titleText, albumPm.title);
    bindAttr(view.artistLabel, view.artistText, albumPm.artist);
    bindAttr(view.classicalLabel, view.classicalCheckbox, albumPm.classical);
    bindAttr(view.composerLabel, view.composerText, albumPm.composer);

    bindCommand(view.btnCancel, pm.cmdCancel);
    bindCommand(view.btnSave, pm.cmdSave);
  }


  public static void main(String[] args) {
    JDialog dlg = new JDialog((Frame) null, "Album Demo Application", true);

    AlbumForm form = new AlbumFormPb().build(null,
    AlbumSessionPm.makeAlbumFormPm());

    dlg.setContentPane(form);
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dlg.setSize(450, 200);
    dlg.setLocationRelativeTo(null); // centered
    dlg.setVisible(true);
    PmEventApi.setApiHandler(new PmEventApiHandler.WithThreadLocalEventSource());
  }

}
