package org.pm4j.core.pm;


public class PmConstants {

  /**
   * Post fix for tool tip resource keys.
   */
  public static final String RESKEY_POSTFIX_TOOLTIP = "_tooltip";

  /**
   * Post fix for simple title resource keys.
   * @see PmObject#getPmShortTitle()
   */
  public static final String RESKEY_POSTFIX_SHORT_TITLE = "_shortTitle";

  /**
   * Postfix for icon resource keys.
   */
  public static final String RESKEY_POSTFIX_ICON = "_icon";
  public static final String RESKEY_POSTFIX_ICON_DISABLED = "_iconDisabled";

  public static final String RESKEY_POSTFIX_FORMAT = "_format";

  public static final String RESKEY_POSTFIX_REQUIRED_MSG = "_required";

  /**
   * The resource key post fix for command execution success messages.<br>
   * A success message will be generated on successful execution if a matching
   * resource is defined.
   */
  public static final String SUCCESS_MSG_KEY_POSTFIX = "_successInfo";

  public static final String MSGKEY_VALIDATION_CONVERSION_FROM_STRING_FAILED = "pmAttr_validationConversionFromStringFailed";
  public static final String MSGKEY_VALIDATION_FORMAT_FAILURE                = "pmAttr_validationFormatFailure";
  public static final String MSGKEY_VALIDATION_MISSING_REQUIRED_VALUE        = "pmAttr_validationMissingRequiredValue";
  public static final String MSGKEY_VALIDATION_MISSING_REQUIRED_SELECTION    = "pmAttr_validationMissingRequiredSelection";
  public static final String MSGKEY_VALIDATION_VALUE_TOO_SHORT               = "pmAttr_validationValueTooShort";
  public static final String MSGKEY_VALIDATION_VALUE_TOO_LONG                = "pmAttr_validationValueTooLong";
  public static final String MSGKEY_VALIDATION_VALUE_TOO_LOW                 = "pmAttr_validationValueTooLow";
  public static final String MSGKEY_VALIDATION_VALUE_TOO_HIGH                = "pmAttr_validationValueTooHigh";
  public static final String MSGKEY_VALIDATION_READONLY                      = "pmAttr_validationReadonly";

}
