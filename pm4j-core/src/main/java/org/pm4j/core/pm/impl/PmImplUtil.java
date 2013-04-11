package org.pm4j.core.pm.impl;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.pm.PmObject;

/**
 * Internally used helper methods.
 * <p>
 * TODO olaf: change to a pluggable bean validator that may be used for customization...
 *
 * @author olaf boede
 */
class PmImplUtil {

  /** The optional JSR-303 bean validator to be considered. */
  private static Validator beanValidator = getBeanValidator();
  private static Log LOG = LogFactory.getLog(PmImplUtil.class);

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

  static void beanConstraintViolationsToPmMessages(PmObject pm, Set<ConstraintViolation<?>> violations) {
    if (violations != null) {
      for (ConstraintViolation<?> v : violations) {
        // FIXME olaf: add handling for translated strings.
        //             what is the result of:
        //System.out.println("v.getMessageTemplate: '" + v.getMessageTemplate() + "'  message: " + v.getMessage());
        PmValidationMessage msg = new PmValidationMessage(pm, v.getMessage(), v);
        pm.getPmConversation().addPmMessage(msg);
      }
    }
  }

}
