package org.pm4j.core.pm;

/**
 * A PM that is intended to be just a container for other PM items.
 * <p>
 * An example:<br>
 * A data entry form PM may be implemented as a {@link PmContainer} with
 * {@link PmAttr}s used for data entry and a {@link PmCommand} that is
 * responsible for storing the entered data.
 * <p>
 * TODO olaf boede: this will be the replacement for {@link PmElement} in the
 * next release.
 *
 * @author olaf boede
 */
public interface PmContainer extends PmElement {

}
