package org.pm4j.swt.pb;

import org.pm4j.core.pb.PbDefaultsBase;


/**
 * Configurable default settings for SWT presentation bindings.
 * 
 * @author olaf boede
 */
public class PbSwtDefaults extends PbDefaultsBase<PbSwtWidgetFactorySet> {
  
  /** The defaults for this VM. */
  private static PbSwtDefaults instance;

  @Override
  protected PbSwtWidgetFactorySet makeWidgetFactorySet() {
    return new PbSwtWidgetFactorySet();
  }

  /**
   * Provides the default presentation binding settings for this VM.<br>
   * This default instance may be configured with another instance by calling
   * {@link #setInstance(PbSwtDefaults)}. 
   * 
   * @return The default presentation binding settings for this VM.
   */
  public static PbSwtDefaults getInstance() {
    if (instance == null) {
      instance = new PbSwtDefaults();
    }
    return instance;
  }

  /**
   * Defines presentation binding defaults for this VM.
   * @param newInstance The new presentation binding defaults.
   */
  public static void setInstance(PbSwtDefaults newInstance) {
    instance = newInstance;
  }

}
