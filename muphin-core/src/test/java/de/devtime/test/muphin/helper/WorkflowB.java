package de.devtime.test.muphin.helper;

import java.util.Arrays;

import de.devtime.muphin.core.workflow.AbstractWorkflow;

public class WorkflowB extends AbstractWorkflow {

  public WorkflowB() {
    super("Workflow B", Arrays.asList(TestPhaseA.class, TestPhaseC.class));
  }
}
