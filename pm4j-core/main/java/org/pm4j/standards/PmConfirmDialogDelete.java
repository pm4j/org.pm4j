package org.pm4j.standards;

import org.pm4j.core.pm.annotation.PmTitleCfg;

/**
 * This confirm dialog implementation only provides some fix default resource keys
 * that may be used for delete scenarios.
 *
 * @author olaf boede
 */
// FIXME olaf: split resKey form resKeyBase for child PMs to prevent unwanted side effects.
@PmTitleCfg(resKey="pmConfirmDialogDelete")
public abstract class PmConfirmDialogDelete extends PmConfirmDialog {

	public PmConfirmDialogDelete(PmConfirmedCommand cmdToConfirm) {
		super(cmdToConfirm);
	}

}
