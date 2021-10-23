package de.devtime.muphin.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.devtime.muphin.core.WorkflowRunner;
import de.devtime.muphin.core.workflow.AbstractWorkflow;

/**
 * Use this annotation to bind a test class to an existing workflow.
 *
 * <p>
 * You can split several tests that belongs to the same workflow into multiple test classes. The {@link WorkflowRunner}
 * will collect all test classes that belongs to a workflow. Specify with the {@code workflow} parameter the workflow
 * for a test class.
 *
 * <p>
 *
 * <pre>
 * &#64;WorkflowTest(MyCustomWorkflow.class)
 * public class MyCustomWorkflowTest {
 *   // add here your test methods with &#64;Phase annotations ...
 * }
 * </pre>
 *
 * @author morrigan
 * @since 0.0.1
 */
@Target({
    ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface WorkflowTest {

  /**
   * Returns a workflow to that this test class belongs.
   *
   * @return a workflow
   * @since 0.0.1
   */
  Class<? extends AbstractWorkflow> value();
}
