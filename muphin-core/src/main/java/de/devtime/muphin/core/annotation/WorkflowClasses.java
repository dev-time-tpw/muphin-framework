package de.devtime.muphin.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to test specific test classes in a workflow test run.
 *
 * <p>
 * <b>Example</b><br>
 *
 * <pre>
 * &#64;RunWith(WorkflowRunner.class)
 * &#64;WorkflowClasses({
 *     TestClassA1.class, TestClassA2.class
 * })
 * public class MuphinTestSuite {
 *
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
public @interface WorkflowClasses {

  /**
   * Returns a list of Classes to be executed while testing a workflow.
   *
   * @return a list of classes
   * @since 0.0.1
   */
  Class<?>[] value();

}
