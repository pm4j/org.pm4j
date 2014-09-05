package org.pm4j.core.pm.joda.impl;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.pm4j.core.exception.PmValidationException;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.api.PmMessageApi;

/**
 * A {@link LocalDateTime} attribute that has embedded sub models for the date- and the time- part.
 * <p>
 * TODO oboede: missing support for default/optional time part.
 *
 * @author Olaf Kossak
 * @author Olaf Boede
 */
public class PmAttrLocalDateAndTimeImpl extends PmAttrLocalDateTimeImpl {

    /** The attribute PM for managing the date part. */
    public final PmAttrLocalDatePart datePart = this.makeDatePart();

    /** The attribute PM for managing the time part. */
    public final PmAttrLocalTimePart timePart = this.makeTimePart();

    /** Inner class for local temporary store. */
    private final LocalStore localStore = new LocalStore();

    /**
     * @param pmParent
     *            The PM parent.
     */
    public PmAttrLocalDateAndTimeImpl(PmObject pmParent) {
        super(pmParent);
        // After setting (and re-loading) a complete date-time value all locally stored partial
        // values are no longer relevant.
        PmEventApi.addPmEventListener(this, PmEvent.VALUE_CHANGE, new PmEventListener() {
            @Override
            public void handleEvent(PmEvent event) {
                localStore.reset();
            }
        });
    }

    /** Does not work with the deprecated attribute validation logic. */
    @Override
    protected boolean isDeprValidation() {
      return false;
    }

    /**
     * Factory method to create date part, can be redefined.
     *
     * @return The date part as instance of inner class PmAttrLocalDatePart.
     */
    protected PmAttrLocalDatePart makeDatePart() {
        return new PmAttrLocalDatePart(this);
    }

    /**
     * Factory method to create time part, can be redefined.
     *
     * @return The time part as instance of inner class PmAttrLocalTimePart.
     */
    protected PmAttrLocalTimePart makeTimePart() {
        return new PmAttrLocalTimePart(this);
    }

    /** Inner class for date part. */
    @PmAttrCfg(required=true)
    public class PmAttrLocalDatePart extends PmAttrLocalDateImpl {

        /**
         * @param pmParent
         *            The PM parent.
         */
        public PmAttrLocalDatePart(PmObject pmParent) {
            super(pmParent);
        }

        @Override
        protected LocalDate getBackingValueImpl() {
            return localStore.getDate();
        }

        @Override
        protected void setBackingValueImpl(LocalDate newValue) {
            localStore.setDate(newValue);
        }

        // TODO: move to project specific implementation.
        @Override
        protected String getPmTitleImpl() {
            // the title is used for validation error messages
            // it is assembled by a localized title for the date time object
            // and a generic localized string for the date part
            return PmAttrLocalDateAndTimeImpl.this.getPmTitle() + " / " + PmLocalizeApi.localize(this, "pmAttrDateTime_datePart");
        }

        @Override
        protected void validate(LocalDate value) throws PmValidationException {
          super.validate(value);
          // This part gets required if the other part has entered data.
          if (value == null && timePart.getValue() != null) {
            throw new PmValidationException(PmMessageApi.makeRequiredMessageResData(this));
          }
        }

    }

    /** Inner class for time part. */
    @PmAttrCfg(required=true)
    public class PmAttrLocalTimePart extends PmAttrLocalTimeImpl {

        /**
         * @param pmParent
         *            The PM parent.
         */
        public PmAttrLocalTimePart(PmObject pmParent) {
            super(pmParent);
        }

        @Override
        protected LocalTime getBackingValueImpl() {
            return localStore.getTime();
        }

        @Override
        protected void setBackingValueImpl(LocalTime newValue) {
            localStore.setTime(newValue);
        }

        @Override
        protected void validate(LocalTime value) throws PmValidationException {
          super.validate(value);
          // This part gets required if the other part has entered data.
          if (value == null && datePart.getValue() != null) {
            throw new PmValidationException(PmMessageApi.makeRequiredMessageResData(this));
          }
        }

        // TODO: move to project specific implementation.
        @Override
        protected String getPmTitleImpl() {
            // the title is used for validation error messages
            // it is assembled by a localized title for the date time object
            // and a generic localized string for the time part
            return PmAttrLocalDateAndTimeImpl.this.getPmTitle() + " / " + PmLocalizeApi.localize(this, "pmAttrDateTime_timePart");
        }

    }

    /**
     * Inner class for handling incomplete date-time value states.
     * <p>
     * It handles the scenario of having an entered entered date or time value. In this case the
     * main attribute value can't be set until the user entered the missing value.
     * <p>
     * This class ensures that the value state is consistently either stored within main
     * attribute value or (in case of incomplete value state) within this instance.
     */
    private class LocalStore {

        private LocalDate date;
        private LocalTime time;
        private boolean resetLocked;

        public void reset() {
            if (!resetLocked) {
                this.date = null;
                this.time = null;
            }
        }

        /**
         * Gets the information from the main attribute. If that's <code>null</code> it will be
         * read from the local {@link #date} attribute.
         * <p>
         * In case of a found main attribute value it also ensures that all local values are
         * cleared. This allows to handle scenarios where the programmer changes backing values
         * on bean level.
         *
         * @return The date information part.
         */
        public LocalDate getDate() {
            final LocalDateTime value = PmAttrLocalDateAndTimeImpl.this.getValue();
            if (value != null) {
                this.reset();
                return value.toLocalDate();
            } else {
                return this.date;
            }
        }

        /**
         * Gets the information from the main attribute. If that's <code>null</code> it will be
         * read from the local {@link #time} attribute.
         * <p>
         * In case of a found main attribute value it also ensures that all local values are
         * cleared. This allows to handle scenarios where the programmer changes backing values
         * on bean level.
         *
         * @return The time information part.
         */
        public LocalTime getTime() {
            final LocalDateTime value = PmAttrLocalDateAndTimeImpl.this.getValue();
            if (value != null) {
                this.reset();
                return value.toLocalTime();
            } else {
                return this.time;
            }
        }

        public void setDate(LocalDate date) {
            restore();
            this.date = date;
            store();
        }

        public void setTime(LocalTime time) {
            restore();
            this.time = time;
            store();
        }

        /**
         * Tries to assemble a new backing {@link LocalDateTime} based on the locally stored
         * {@link #date} and {@link #time} values. Uses optionally the backing value of the
         * attribute to complement missing information.
         * <p>
         * After the method call there are the following possible value states:
         * <ul>
         * <li>The date-time information is complete:<br>
         * The information is stored within the attribute's backing value.<br>
         * The local {@link #date} and {@link #time} values are <code>null</code>.</li>
         * <li>The date-time information is incomplete:<br>
         * The information is stored within the local {@link #date} and {@link #time} values.<br>
         * The backing value is <code>null</code></li>
         * <li>All values may also be <code>null</code>.</li>
         * </ul>
         */
        private void store() {
            if (date != null && time != null) {
                // The value change listener (see ctor) resets the local store.
                PmAttrLocalDateAndTimeImpl.this.setValue(date.toLocalDateTime(time));
            } else {
                // Prevent that the value change listener clears partial date/time values:
                resetLocked = true;
                try {
                    PmAttrLocalDateAndTimeImpl.this.setValue(null);
                } finally {
                    resetLocked = false;
                }
            }
        }

        /**
         * Copies the value of the main attribute to the local date and time value parts.<br>
         * Does nothing if the main attribute value is <code>null</code>.
         */
        private void restore() {
            final LocalDateTime value = PmAttrLocalDateAndTimeImpl.this.getValue();
            if (value != null) {
                this.date = value.toLocalDate();
                this.time = value.toLocalTime();
            }
        }
    }

}
