package org.pm4j.core.sample.album;

import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.sample.album.domain.AlbumService;

public class AlbumSessionPm extends PmConversationImpl {

  public AlbumSessionPm() {
    // The services may be provided by a container such as spring etc.
    // For this simple demo, the services are simply provided as named PM session
    // properties. - A simple option that may be fine for some small applications.
    setPmNamedObject("albumService", new AlbumService());
  }
  
  /**
   * A simple test PM factory.
   * @return An album form PM.
   */
  public static AlbumFormPm makeAlbumFormPm() {
    return new AlbumFormPm(new AlbumSessionPm());
  }
  
}
