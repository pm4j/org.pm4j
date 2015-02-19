package org.pm4j.swt.pb;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmUtil;

public class PbImageRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(PbImageRegistry.class);

  // FIXME: implement a package-structured registry tree...
  //        otherwise we violate the package oriented resource logic.
  private static ImageRegistry imageRegistry = new ImageRegistry();

  public static Image findImage(PmObject pm) {
    String iconPath = pm.getPmIconPath();
    Image image = null;
    if (iconPath != null) {
      image = imageRegistry.get(iconPath);
      if (image == null) {
        Class<?> pmClass = pm.getClass();
        URL iconUrl = pmClass.getResource(iconPath);
        if (iconUrl == null) {
          LOG.warn("Can't find icon file '" + iconPath + "' for: " + PmUtil.getPmLogString(pm));
        }
        else {
          imageRegistry.put(iconPath, ImageDescriptor.createFromURL(iconUrl));
          image = imageRegistry.get(iconPath);
        }
      }
    }
    return image;
  }

}
