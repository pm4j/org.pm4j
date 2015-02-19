package org.pm4j.core.pm.impl;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmObject;

/**
 * Bean validation (JSR-303) utility methods.
 * <p>
 * TODO olaf: change to a pluggable bean validator that may be used for customization...
 *
 * @author Olaf Boede
 */
public class BeanValidationPmUtil {

  private static Logger LOG = LoggerFactory.getLogger(BeanValidationPmUtil.class);

  /** The optional JSR-303 bean validator to be considered. */
  private static Validator beanValidator = getBeanValidator();

  /**
   * Triggers a bean validation call for the given bean property and reports the found validations
   * as {@link PmValidationMessage}s for the given pmCtxt.
   *
   * @param pmCtxt The PM to report found validation errors for.
   * @param bean The bean to validate.
   * @param propertyName The bean property to validate.
   */
  public static void validateProperty(PmObject pmCtxt, Object bean, String propertyName) {
    Validator validator = BeanValidationPmUtil.getBeanValidator();
    if (validator != null) {
      @SuppressWarnings("unchecked")
      Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>)(Object)validator.validateProperty(bean, propertyName);
      beanConstraintViolationsToPmMessages(pmCtxt, violations);
    }
  }

  /**
   * Triggers a bean validation call for the given bean and reports the found validations
   * as {@link PmValidationMessage}s for the given pmCtxt.
   *
   * @param pmCtxt The PM to report found validation errors for.
   * @param bean The bean to validate.
   */
  public static void validateBean(PmObject pmCtxt, Object bean) {
    Validator validator = BeanValidationPmUtil.getBeanValidator();
    if (validator != null) {
      @SuppressWarnings("unchecked")
      Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>)(Object) validator.validate(bean);
      beanConstraintViolationsToPmMessages(pmCtxt, violations);
    }
  }

  static Validator getBeanValidator() {
    if (beanValidator == null) {
      try {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        return f.getValidator();
      }
      catch (ValidationException e) {
        LOG.info("No JSR-303 bean validation configuration found: " + e.getMessage());
        return null;
      }
    }
    return beanValidator;
  }

  /**
   * Translates the given set of bean validation messages to {@link PmValidationMessage}s.
   *
   * @param pm The PM to report the messages for.
   * @param violations The set of bean constraint validations to translate.
   */
  static void beanConstraintViolationsToPmMessages(PmObject pm, Set<ConstraintViolation<?>> violations) {
    if (violations != null) {
      for (ConstraintViolation<?> v : violations) {
        // TODO olaf: add handling for translated strings.
        //             what is the result of:
        //System.out.println("v.getMessageTemplate: '" + v.getMessageTemplate() + "'  message: " + v.getMessage());
        PmValidationMessage msg = new PmValidationMessage(pm, PmConstants.MSGKEY_FIRST_MSG_PARAM, v.getMessage(), v);
        pm.getPmConversation().addPmMessage(msg);
      }
    }
  }

}
