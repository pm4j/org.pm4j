package org.pm4j.swing.pb;

import org.pm4j.core.pb.PbDefaultsBase;


/**
 * Configurable default settings for SWT presentation bindings.
 * 
 * @author olaf boede
 */
public class PbSwingDefaults extends PbDefaultsBase<PbSwingWidgetFactorySet> {
  
  /** The defaults for this VM. */
  private static PbSwingDefaults instance;

  @Override
  protected PbSwingWidgetFactorySet makeWidgetFactorySet() {
    return new PbSwingWidgetFactorySet();
  }

  /**
   * Provides the default presentation binding settings for this VM.<br>
   * This default instance may be configured with another instance by calling
   * {@link #setInstance(PbSwingDefaults)}. 
   * 
   * @return The default presentation binding settings for this VM.
   */
  public static PbSwingDefaults getInstance() {
    if (instance == null) {
      instance = new PbSwingDefaults();
    }
    return instance;
  }

  /**
   * Defines presentation binding defaults for this VM.
   * @param newInstance The new presentation binding defaults.
   */
  public static void setInstance(PbSwingDefaults newInstance) {
    instance = newInstance;
  }

}
