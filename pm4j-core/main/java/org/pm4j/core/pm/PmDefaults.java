package org.pm4j.core.pm;

import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.impl.PmObjectBase.NameBuilder;
import org.pm4j.core.pm.impl.PmObjectBase.NameBuilderAbsoluteName;
import org.pm4j.core.pm.impl.title.AsteriskAttrTitleProvider;
import org.pm4j.core.pm.impl.title.PmTitleProvider;
import org.pm4j.core.pm.impl.title.TitleProviderPmResBased;

/**
 * A singleton that knows about the application wide defined default
 * presentation model implementation strategies.
 *
 * @author olaf boede
 */
@SuppressWarnings("rawtypes")
public class PmDefaults {

  /**
   * The singleton.
   */
  private static PmDefaults instance;

  /**
   * Defines the used default title provider for PMs.<br>
   * Default value for this attribute: {@link TitleProviderPmResBased}.
   */
  private PmTitleProvider pmTitleProvider = TitleProviderPmResBased.INSTANCE;

  /**
   * Defines the used default title provider for {@link PmAttr}'s.<br>
   * Some applications use specific title provider for attributes to reflect the
   * changed state of attributes (see {@link AsteriskAttrTitleProvider}).<br>
   * Default value for this attribute: {@link TitleProviderPmResBased}.
   */
  private PmTitleProvider pmAttrTitleProvider = TitleProviderPmResBased.INSTANCE;

  /**
   * Defines the algorithm used for the toString() implementation of PMs.<br>
   * The default implementation provides a name that reflects the complete PM hierarchy.
   */
  private NameBuilder toStringNameBuilder = NameBuilderAbsoluteName.INSTANCE;

  /**
   * Defines the algorithm used for building the name of PMs as it will appear in the logs.<br>
   * The default implementation provides a name that reflects the complete PM hierarchy.
   */
  private NameBuilder logStringBuilder = NameBuilderAbsoluteName.INSTANCE;

  private PmOptionCfg.NullOption enumNullOptionDefault = PmOptionCfg.NullOption.NO;

  /**
   * Defines the default {@link PmCommandCfg#beforeDo()} setting for commands
   * that do not define this attribute explicitly.
   */
  private PmCommandCfg.BEFORE_DO beforeDoCommandDefault = BEFORE_DO.VALIDATE;

  /**
   * The event mask to be fired on validation state changes.<br>
   * Is configurable to support information about changing style classes,
   * tooltips etc.<br>
   * This allows to fire only a single event with an event mask that
   * informs all relevant listeners.
   */
  public int validationChangeEventMask = PmEvent.VALIDATION_STATE_CHANGE
                                       | PmEvent.STYLECLASS_CHANGE
                                       | PmEvent.TOOLTIP_CHANGE;

  /**
   * Defines, if validation error messages should be added to the tool tip text of
   * the affected attributes.
   */
  public boolean addErrorMessagesToTooltip = true;

  // TODO olaf: add something to the command that allows to configure that. - An application default may also be useful...
//  /**
//   * Defines if the application supports commands that can be undone.
//   * The related overhead and programming restrictions have'nt to be considered if undo is not needed for an application.
//   * <p>
//   * Undoable commands usually have to be cloned before the actually get executed.<br>
//   * This also implies that any state change, done within a command only affects the clone...
//   */
//  public boolean supportUndoableCommands = true;

  @Deprecated
  private boolean elementsInheritAnnotationsOnlyFromSession = false;

  /**
   * @return The application wide default strategies.
   */
  public static PmDefaults getInstance() {
    if (instance == null) {
      instance = new PmDefaults();
    }
    return instance;
  }

  /**
   * Defines a new set of default strategies.
   *
   * @param newInstance The new definition.
   */
  public static void setInstance(PmDefaults newInstance) {
    instance = newInstance;
  }

  // -- getter / setter --

  public PmTitleProvider getPmTitleProvider() {
    return pmTitleProvider;
  }

  public void setPmTitleProvider(PmTitleProvider pmTitleProvider) {
    assert pmTitleProvider != null;

    this.pmTitleProvider = pmTitleProvider;
  }

  public PmTitleProvider getPmAttrTitleProvider() {
    return pmAttrTitleProvider;
  }

  public void setPmAttrTitleProvider(PmTitleProvider pmAttrTitleProvider) {
    this.pmAttrTitleProvider = pmAttrTitleProvider;
  }

  public PmOptionCfg.NullOption getEnumNullOptionDefault() {
    return enumNullOptionDefault;
  }

  public void setEnumNullOptionDefault(PmOptionCfg.NullOption enumNullOption) {
    this.enumNullOptionDefault = enumNullOption;
  }

  public NameBuilder getToStringNameBuilder() {
    return toStringNameBuilder;
  }

  public void setToStringNameBuilder(NameBuilder toStringNameBuilder) {
    this.toStringNameBuilder = toStringNameBuilder;
  }

  public NameBuilder getLogStringBuilder() {
    return logStringBuilder;
  }

  public void setLogStringBuilder(NameBuilder logStringBuilder) {
    this.logStringBuilder = logStringBuilder;
  }

  @Deprecated
  public boolean isElementsInheritAnnotationsOnlyFromSession() {
    return elementsInheritAnnotationsOnlyFromSession;
  }

  @Deprecated
  public void setElementsInheritAnnotationsOnlyFromSession(boolean elementsInheritAnnotationsOnlyFromSession) {
    this.elementsInheritAnnotationsOnlyFromSession = elementsInheritAnnotationsOnlyFromSession;
  }

  public PmCommandCfg.BEFORE_DO getBeforeDoCommandDefault() {
    return beforeDoCommandDefault;
  }

  public void setBeforeDoCommandDefault(PmCommandCfg.BEFORE_DO beforeDoCommandDefault) {
    this.beforeDoCommandDefault = beforeDoCommandDefault;
  }

}
