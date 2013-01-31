package org.pm4j.core.sample.album;

import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.sample.album.domain.AlbumService;

public class AlbumConversationPm extends PmConversationImpl {

  public AlbumConversationPm() {
    // The services may be provided by a container such as cdi or spring.
    // For this simple demo, the services are simply provided as named PM conversation
    // properties. - A simple option that may be fine for some small applications.
    setPmNamedObject("albumService", new AlbumService());
  }

}
