package org.pm4j.core.pm;


/**
 * The set of common pm4j resource string constants.
 *
 * @author Olaf Boede
 */
public final class PmConstants {

  /**
   * Post fix for tool tip resource keys.
   */
  public static final String RESKEY_POSTFIX_TOOLTIP = "_tooltip";

  /**
   * Post fix for simple title resource keys.
   * @see PmObject#getPmShortTitle()
   * @deprecated Please use getPmTitle() instead.
   */
  @Deprecated public static final String RESKEY_POSTFIX_SHORT_TITLE = "_shortTitle";

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

  /** Default message: Unable to convert value in field "{0}". */
  public static final String MSGKEY_VALIDATION_CONVERSION_FROM_STRING_FAILED = "pmAttr_validationConversionFromStringFailed";
  /** Default message: Unable to convert the entered string to a numeric value in field "{0}". */
  public static final String MSGKEY_VALIDATION_NUMBER_CONVERSION_FROM_STRING_FAILED = "pmAttr_validationNumberConversionFromStringFailed";
  /** Default message: The value of the field "{0}" cannot be interpreted. Please use the format "{1}". */
  public static final String MSGKEY_VALIDATION_FORMAT_FAILURE                = "pmAttr_validationFormatFailure";
  /** Default message: Please enter a value into "{0}". */
  public static final String MSGKEY_VALIDATION_MISSING_REQUIRED_VALUE        = "pmAttr_validationMissingRequiredValue";
  /** Default message: Please select a value for "{0}". */
  public static final String MSGKEY_VALIDATION_MISSING_REQUIRED_SELECTION    = "pmAttr_validationMissingRequiredSelection";
  /** Default message: Please enter at least {0} characters in field "{1}". */
  public static final String MSGKEY_VALIDATION_VALUE_TOO_SHORT               = "pmAttr_validationValueTooShort";
  /** Default message: Please enter maximal {0} characters in field "{1}". */
  public static final String MSGKEY_VALIDATION_VALUE_TOO_LONG                = "pmAttr_validationValueTooLong";
  /** Default message: Please enter a number not less than {0} in field "{1}". */
  public static final String MSGKEY_VALIDATION_VALUE_TOO_LOW                 = "pmAttr_validationValueTooLow";
  /** Default message: Please enter a number not more than {0} in field "{1}". */
  public static final String MSGKEY_VALIDATION_VALUE_TOO_HIGH                = "pmAttr_validationValueTooHigh";
  /** Default message: The field "{0}" cannot be altered. */
  public static final String MSGKEY_VALIDATION_READONLY                      = "pmAttr_validationReadonly";

  /** The title that gets displayed when no option is selected. */
  public static final String MSGKEY_NULL_OPTION                              = "pmAttr_nullOption";

  /** Prints just the first message parameter: "{0}". */
  public static final String MSGKEY_FIRST_MSG_PARAM                          = "firstParamOnlyResource";

  /**
   * A resource key for cached exceptions. The first Parameter of the resource is used to transfer the exception message.
   * @deprecated Please use {@link #MSGKEY_FIRST_MSG_PARAM}.
   */
  @Deprecated
  public static final String MSGKEY_EXCEPTION                                = "pmException";
}
