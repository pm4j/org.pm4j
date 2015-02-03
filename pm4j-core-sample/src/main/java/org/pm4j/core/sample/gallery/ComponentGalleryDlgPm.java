package org.pm4j.core.sample.gallery;

import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmConversationImpl;

@PmTitleCfg(resKeyBase="galleryDlgPm")
public class ComponentGalleryDlgPm extends PmConversationImpl {

  public final GalleryTabSetPm galleryTabSet = new GalleryTabSetPm(this);

}
