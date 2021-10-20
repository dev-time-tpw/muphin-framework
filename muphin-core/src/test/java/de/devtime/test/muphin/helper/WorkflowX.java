package de.devtime.test.muphin.helper;

import java.util.List;

import de.devtime.muphin.core.phase.AbstractPhase;
import de.devtime.muphin.core.workflow.AbstractWorkflow;

public class WorkflowX extends AbstractWorkflow {

  public WorkflowX(List<Class<? extends AbstractPhase>> phases) {
    super("Workflow X", phases);
  }
}
