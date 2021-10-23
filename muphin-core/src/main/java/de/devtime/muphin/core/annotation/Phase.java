package de.devtime.muphin.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.devtime.muphin.core.phase.AbstractPhase;
import de.devtime.muphin.core.phase.NoPhase;

/**
 * Use this annotation to bind a test method to an existing phase that exists in a workflow.
 *
 * <p>
 * The parameters {@code beforePhase} and {@code afterPhase} can be used to specify at which phase the test method
 * should be executed before or after.
 *
 * <p>
 * <b>Usage</b>
 *
 * <pre>
 * &#64;Test
 * &#64;Phase(beforePhase = MyCustomPhase.class)
 * public void testMyWorkflowBeforeMyCustomPhase() {
 *   // implement your tests here ...
 * }
 * </pre>
 *
 * @author morrigan
 * @since 0.0.1
 */
@Target({
    ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Phase {

  /**
   * Returns a phase to which this test method is previously executed.
   *
   * @return a phase
   * @since 0.0.1
   */
  Class<? extends AbstractPhase> beforePhase() default NoPhase.class;

  /**
   * Returns a phase to which this test method is executed afterwards.
   *
   * @return a phase
   * @since 0.0.1
   */
  Class<? extends AbstractPhase> afterPhase() default NoPhase.class;
}
