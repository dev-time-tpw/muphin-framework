package de.devtime.test.muphin.core.workflow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import org.junit.Test;

import de.devtime.muphin.core.workflow.AbstractWorkflow;
import de.devtime.test.muphin.helper.TestPhaseA;
import de.devtime.test.muphin.helper.TestPhaseB;

public class AbstractWorkflowTest {

  private static final String NAME = "Workflow";

  private class TestWorkflow extends AbstractWorkflow {

    protected TestWorkflow() {
      super(NAME, Arrays.asList(TestPhaseA.class, TestPhaseB.class));
    }
  }

  @Test
  public void testConstruction() {
    TestWorkflow sut = new TestWorkflow();
    assertThat(sut.getName(), is(equalTo(NAME)));
    assertThat(sut.getPhases(), hasSize(2));
    assertThat(sut.getPhases().get(0).getClass(), is(equalTo(TestPhaseA.class)));
    assertThat(sut.getPhases().get(1).getClass(), is(equalTo(TestPhaseB.class)));
  }
}
