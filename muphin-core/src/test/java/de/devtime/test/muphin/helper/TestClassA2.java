package de.devtime.test.muphin.helper;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.devtime.muphin.core.WorkflowRunner;
import de.devtime.muphin.core.annotation.Phase;
import de.devtime.muphin.core.annotation.WorkflowTest;
import de.devtime.muphin.core.phase.SetupPhase;
import de.devtime.muphin.core.phase.TearDownPhase;

@RunWith(WorkflowRunner.class)
@WorkflowTest(WorkflowA.class)
public class TestClassA2 {

  @Test
  @Phase(beforePhase = SetupPhase.class)
  public void testTC2WorkflowABeforeSetupPhase() {
    assertTrue(true);
  }

  @Test
  @Phase(afterPhase = SetupPhase.class)
  public void testTC2WorkflowAAfterSetupPhase() {
    assertTrue(true);
  }

  @Test
  @Phase(beforePhase = TearDownPhase.class)
  public void testTC2WorkflowABeforeTearDownPhase() {
    assertTrue(true);
  }

  @Test
  @Phase(afterPhase = TearDownPhase.class)
  public void testTC2WorkflowAAfterTearDownPhase() {
    assertTrue(true);
  }

  @Test
  @Phase(beforePhase = TestPhaseA.class)
  public void testTC2WorkflowABeforeTestPhaseA() {
    assertTrue(true);
  }

  @Test
  @Phase(afterPhase = TestPhaseA.class)
  public void testTC2WorkflowAAfterTestPhaseA() {
    assertTrue(true);
  }

  @Test
  @Phase(beforePhase = TestPhaseB.class)
  public void testTC2WorkflowABeforeTestPhaseB() {
    assertTrue(true);
  }

  @Test
  @Phase(afterPhase = TestPhaseB.class)
  public void testTC2WorkflowAAfterTestPhaseB() {
    assertTrue(true);
  }
}