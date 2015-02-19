package org.pm4j.core.sample.album;

import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.sample.album.domain.Album;

@PmBeanCfg(beanClass=Album.class, autoCreateBean=true)
public class AlbumPm extends PmBeanBase<Album> {

  public AlbumPm(PmObject pmParent, Album bean) {
    super(pmParent, bean);
  }

  @PmAttrCfg(required=true)
  public final PmAttrString title = new PmAttrStringImpl(this);

  public final PmAttrString artist = new PmAttrStringImpl(this);

  public final PmAttrBoolean classical = new PmAttrBooleanImpl(this);

  public final PmAttrString composer = new PmAttrStringImpl(this);

}
