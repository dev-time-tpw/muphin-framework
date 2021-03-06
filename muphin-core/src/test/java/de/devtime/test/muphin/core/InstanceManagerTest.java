package de.devtime.test.muphin.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.junit.Before;
import org.junit.Test;

import de.devtime.muphin.core.InstanceManager;
import de.devtime.muphin.core.phase.AbstractPhase;
import de.devtime.muphin.core.phase.SetupPhase;
import de.devtime.muphin.core.workflow.AbstractWorkflow;
import de.devtime.test.muphin.helper.TestPhaseA;
import de.devtime.test.muphin.helper.TestPhaseX;
import de.devtime.test.muphin.helper.WorkflowA;
import de.devtime.test.muphin.helper.WorkflowB;
import de.devtime.test.muphin.helper.WorkflowX;

public class InstanceManagerTest {

  private static final InstanceManager SUT = InstanceManager.getInstance();

  @Before
  public void setup() {
    SUT.clear();
  }

  @Test
  public void testGetWorkflowsAfterConstruction() {
    List<AbstractWorkflow> workflows = SUT.getWorkflows();
    assertThat(workflows, hasSize(0));
  }

  @Test
  public void testGetWorkflows() {
    AbstractWorkflow workflowA = SUT.getWorkflow(WorkflowA.class);
    AbstractWorkflow workflowB = SUT.getWorkflow(WorkflowB.class);
    assertThat(workflowA, is(notNullValue()));
    assertThat(workflowA.getClass(), is(equalTo(WorkflowA.class)));
    assertThat(workflowB, is(notNullValue()));
    assertThat(workflowB.getClass(), is(equalTo(WorkflowB.class)));

    List<AbstractWorkflow> workflows = SUT.getWorkflows();
    assertThat(workflows, hasSize(2));
    assertThat(workflows, containsInAnyOrder(workflowA, workflowB));
  }

  @Test
  public void testGetWorkflowWithInvalidWorkflowClass() {
    IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, () -> SUT.getWorkflow(WorkflowX.class));
    assertThat(iae.getMessage(),
        containsString("Can't create a new instance from class de.devtime.test.muphin.helper.WorkflowX"));
  }

  @Test
  public void testGetPhase() {
    AbstractPhase setupPhase = SUT.getPhase(SetupPhase.class);
    AbstractPhase testPhaseA = SUT.getPhase(TestPhaseA.class);
    assertThat(setupPhase, is(notNullValue()));
    assertThat(setupPhase.getClass(), is(equalTo(SetupPhase.class)));
    assertThat(testPhaseA, is(notNullValue()));
    assertThat(testPhaseA.getClass(), is(equalTo(TestPhaseA.class)));
  }

  @Test
  public void testGetPhaseWithInvalidPhaseClass() {
    IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, () -> SUT.getPhase(TestPhaseX.class));
    assertThat(iae.getMessage(),
        containsString("Can't create a new instance from class de.devtime.test.muphin.helper.TestPhaseX"));
  }

  @Test
  public void testClear()
      throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
    SUT.getWorkflow(WorkflowA.class);
    SUT.getPhase(TestPhaseA.class);
    assertThat(SUT.getWorkflows(), hasSize(1));
    Field field = InstanceManager.class.getDeclaredField("phases");
    field.setAccessible(true);
    ConcurrentMap<?, ?> phases = (ConcurrentMap<?, ?>) field.get(SUT);
    assertThat(phases.size(), is(equalTo(1)));

    SUT.clear();

    assertThat(SUT.getWorkflows(), hasSize(0));
    assertThat(phases.size(), is(equalTo(0)));
  }
}
