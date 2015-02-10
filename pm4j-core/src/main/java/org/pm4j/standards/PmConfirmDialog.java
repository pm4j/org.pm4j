package org.pm4j.standards;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmObjectBase;

import java.util.ArrayList;
import java.util.List;

/**
 * A standard confirmation dialog PM.<br>
 * It asks the user to agree to execute a command.
 * <p>
 * TODO: the following docu was just copied from an old method. -> Make a clear docu here...
 * <p>
 * The default implementation concatenates the long name of the command to confirm
 * and '_confirmDialog'.<br>
 * This way it is only necessary to provide some resource keys as shown in the
 * following resource example:
 * <pre>
 *   myPm.cmdDoSomething=Do something...
 *   myPm.cmdDoSomething_confirmDialog=Confirm to do something.
 *   myPm.cmdDoSomething_confirmDialog_dialogMessage=Do you really want to do something?
 * </pre>
 * In case you provide some information by overriding {@link #getNameOfThingToConfirm()}
 * you may use that within your messages:
 * <pre>
 *   myPm.cmdDoSomething=Do something...
 *   myPm.cmdDoSomething_confirmDialog=Confirm to do something with {0}.
 *   myPm.cmdDoSomething_confirmDialog_dialogMessage=Do you really want to do something with {0}?
 * </pre>
 *
 * @author olaf boede
 */
public class PmConfirmDialog extends PmObjectBase {

  private PmConfirmedCommand cmdToConfim;

  /**
   * @param cmdToConfirm The command that will be executed if the user agrees.
   */
  public PmConfirmDialog(PmConfirmedCommand cmdToConfirm) {
    super(cmdToConfirm);
    this.cmdToConfim = cmdToConfirm;
  }

  @Override
  protected String getPmTitleImpl() {
    return PmLocalizeApi.localize(this, getLocalOrStandardDlgResKey(""), getNameOfThingToConfirm());
  }

  /**
   * Subclasses may override {@link PmConfirmDialog#getMessageString()} to
   * provide a more specific message content.
   */
  public final PmObject dialogMessage = new PmObjectBase(this) {
    @Override
    protected String getPmTitleImpl() {
      String s = getMessageString();
      return (s != null)
            ? s
            // TODO: Konstante!
            : PmLocalizeApi.localize(this, getLocalOrStandardDlgResKey("_dialogMessage"), getNameOfThingToConfirm());
    };
  };

  /** Executes the confirmed command. */
  public final PmCommand cmdYes = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      cmdToConfim.doIt();
    }

    /**
     * Check for valid values will be performed if the command to confirm
     * requires it.
     */
    @Override
    public boolean isRequiresValidValues() {
      return cmdToConfim.isRequiresValidValues();
    }

    @Override
    public String getPmResKey() {
      return getLocalOrStandardDlgResKey(".cmdYes");
    }
  };

  /** Should be mapped to a view 'closeView' that closes the dialog. */
  public final PmCommand cmdNo = new PmCommandImpl(this) {
    @Override
    public String getPmResKey() {
      return getLocalOrStandardDlgResKey(".cmdNo");
    }
  };

  // -- Helper implementation --

  /**
   * @return The name of the thing to confirm. May appear in the title and dialogMessage.
   */
  protected String getNameOfThingToConfirm() {
    return cmdToConfim.getNameOfThingToConfirm();
  }


  /**
   * Subclasses may provide here a very specific localized message that appears
   * in {@link #dialogMessage}.
   */
  protected String getMessageString() {
    return null;
  }


  /**
   * Provides the local resource key (build based on the command resource key)
   * if there is a matching resource string defined.
   * <p>
   * If there is no locally defined resource definition, a standard resource
   * key ('pmConfirmDialog') based string will be returned.
   *
   * @param postfix The postfix that will be added to the local or standard res-key.
   * @return The resource key that provides an existing resource string.
   */
  protected String getLocalOrStandardDlgResKey(String postfix) {
    // 1. command res key + "_confirmDialog" + postfix
    String dlgResKey = cmdToConfim.getPmResKeyBase() + "_confirmDialog" + postfix;
    if (PmLocalizeApi.findLocalization(cmdToConfim, dlgResKey) != null) {
      return dlgResKey;
    }

    // 2. getDialogResKeyBase() + postfix
    String resKeyBase = getDialogResKeyBase();
    if (resKeyBase != null) {
      dlgResKey = resKeyBase + postfix;
      if (PmLocalizeApi.findLocalization(this, dlgResKey) != null) {
        return dlgResKey;
      }
    }

    // 3. "pmConfirmDialog" + postfix
    return "pmConfirmDialog" + postfix;
  }

  protected String getDialogResKeyBase() {
    return null;
  }

  @Override
  public String getPmResKey() {
    String keyBase = getDialogResKeyBase();
    return keyBase != null
      ? keyBase
      : super.getPmResKey();
  }

  /** The dialog uses the resource directory of the command that will be confirmed. */
  @Override
  public List<Class<?>> getPmResLoaderCtxtClasses() {
    ArrayList<Class<?>> classes = new ArrayList<Class<?>>(cmdToConfim.getPmResLoaderCtxtClasses());
    classes.add(getClass());
    return classes;
  }

  // -- Getters for frameworks that can't access public fields. */

  public PmObject getDialogMessage() {
    return dialogMessage;
  }

  public PmCommand getCmdYes() {
    return cmdYes;
  }

  public PmCommand getCmdNo() {
    return cmdNo;
  }

}
