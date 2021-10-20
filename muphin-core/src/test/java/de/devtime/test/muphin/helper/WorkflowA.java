package de.devtime.test.muphin.helper;

import java.util.Arrays;

import de.devtime.muphin.core.workflow.AbstractWorkflow;

public class WorkflowA extends AbstractWorkflow {

  public WorkflowA() {
    super("Workflow A", Arrays.asList(TestPhaseA.class, TestPhaseB.class));
  }
}
