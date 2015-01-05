package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitCallBack;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitResult;

public class PmDataInputUtil {

  public static void rollbackBufferedPmChanges(PmObject rootPm) {
    PmVisitorApi.visit(rootPm, new PmVisitCallBack() {
      @Override
      public PmVisitResult visit(PmObject pm) {
        if (pm instanceof PmDataInput) {
          ((PmDataInput) pm).rollbackBufferedPmChanges();
        }
        return PmVisitResult.CONTINUE;
      }
    }, PmVisitHint.SKIP_CONVERSATION);
  }

  public static void commitBufferedPmChanges(PmObject rootPm) {
    PmVisitorApi.visit(rootPm, new PmVisitCallBack() {
      @Override
      public PmVisitResult visit(PmObject pm) {
        if (pm instanceof PmDataInput) {
          ((PmDataInput) pm).commitBufferedPmChanges();
        }
        return PmVisitResult.CONTINUE;
      }
    }, PmVisitHint.SKIP_CONVERSATION);
  }

}
