package de.devtime.muphin.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.MethodSorter;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.devtime.muphin.core.annotation.Phase;
import de.devtime.muphin.core.annotation.WorkflowClasses;
import de.devtime.muphin.core.annotation.WorkflowTest;
import de.devtime.muphin.core.phase.AbstractPhase;
import de.devtime.muphin.core.phase.NoPhase;
import de.devtime.muphin.core.phase.SetupPhase;
import de.devtime.muphin.core.phase.TearDownPhase;
import de.devtime.muphin.core.workflow.AbstractWorkflow;

/**
 * Implements the muphin standard test case runner that executes all test methods in the correct workflow and phases
 * order.
 *
 * <p>
 * You can use this runner in different ways.
 *
 * <p>
 * <b>Suite variant</b><br>
 * Use the runner on an empty test class analogous to a {@link Suite}. If no concrete workflow classes are specified,
 * all test classes on the classpath that have an {@link WorkflowTest} annotation are used for a test.
 *
 * <pre>
 * &#64;RunWith(WorkflowRunner.class)
 * public class MyTestSuite {
 *
 * }
 * </pre>
 *
 * <p>
 * <b>Suite variant with specific classes</b><br>
 * Use the runner on an empty test class analogous to a {@link Suite} and annotate this class with
 * {@link WorkflowClasses}. All test classes specified here are taken into account in the workflow test run.
 *
 * <pre>
 * &#64;RunWith(WorkflowRunner.class)
 * &#64;WorkflowClasses({
 *     TestClassA.class, TestClassB.class
 * })
 * public class MyTestSuite {
 *
 * }
 * </pre>
 *
 * <p>
 * <b>Filtered variant</b><br>
 * Use the runner on a test class, to run only this test class or a single test method from this test class. In this
 * variant, the complete workflow of the test class is run through, but only the filtered test methods are called.
 *
 * <pre>
 * &#64;RunWith(WorkflowRunner.class)
 * &#64;WorkflowTest(WorkflowA.class)
 * public class TestClassA {
 *
 *   &#64;Test
 *   &#64;Phase(beforePhase = SetupPhase.class)
 *   public void testWorkflowABeforeSetupPhase() {
 *     // any test here ...
 *   }
 * }
 * </pre>
 *
 * @author morrigan
 * @since 0.0.1
 */
public class WorkflowRunner extends Runner implements Filterable {

  // checkstyle:WriteTag OFF

  private interface Callback {
    default void workflow(AbstractWorkflow workflow) {
      // default implementation that does nothing to allow only override necessary methods
    }

    default void phase(AbstractPhase phase) {
      // default implementation that does nothing to allow only override necessary methods
    }

    default void runBeforePhase(FrameworkMethod method, Iterator<FrameworkMethod> iter) {
      // default implementation that does nothing to allow only override necessary methods
    }

    default void runPhase(AbstractWorkflow workflow, AbstractPhase phase) {
      // default implementation that does nothing to allow only override necessary methods
    }

    default void runAfterPhase(FrameworkMethod method, Iterator<FrameworkMethod> iter) {
      // default implementation that does nothing to allow only override necessary methods
    }
  }

  // checkstyle:WriteTag ON

  private static final Logger LOG = LoggerFactory.getLogger(WorkflowRunner.class);

  private static final InstanceManager INSTANCES = InstanceManager.getInstance();

  private final Lock childrenLock = new ReentrantLock();
  private TestClass testClass;
  private int testClassesAmount;

  // Guarded by childrenLock
  private Map<Class<? extends AbstractWorkflow>, Map<String, List<FrameworkMethod>>> filteredChildren;

  private final ConcurrentMap<FrameworkMethod, Description> methodDescriptions = new ConcurrentHashMap<>();

  /**
   * Creates a new instance of this workflow runner with an underlying test class.
   *
   * <p>
   * This test class controls the execution of the various flows. If the test class is annotated with
   * {@link WorkflowTest} then only the tests of this class in the context of the associated workflow are executed. If
   * the annotation is not present, all classes on the classpath with this annotation are searched for and executed.<br>
   * This distinction is necessary in order to have all tests executed via some kind of suite class and, on the other
   * hand, to be able to test only individual test classes via the IDE during development.
   *
   * @param testClass a test class
   * @since 0.0.1
   */
  public WorkflowRunner(Class<?> testClass) {
    super();

    this.testClass = new TestClass(testClass);
  }

  /**
   * Returns a {@link TestClass} object wrapping the class to be executed.
   *
   * @return a test class
   * @since 0.0.1
   */
  public final TestClass getTestClass() {
    return this.testClass;
  }

  /**
   * Returns a {@link Description} showing the tests to be run by the receiver.
   *
   * @return a description
   * @since 0.0.1
   */
  @Override
  public Description getDescription() {
    Class<?> clazz = getTestClass().getJavaClass();
    Description description;
    description = Description.createSuiteDescription(clazz, this.testClass.getAnnotations());

    // checkstyle:WriteTag OFF
    iterateThroughChildren(getFilteredChildren(), new Callback() {
      private Description workflowDescription;
      private Description phaseDescription;

      @Override
      public void workflow(AbstractWorkflow workflow) {
        this.workflowDescription = Description.createTestDescription(workflow.getClass(),
            workflow.getClass().getSimpleName());
        description.addChild(this.workflowDescription);
      }

      @Override
      public void phase(AbstractPhase phase) {
        this.phaseDescription = Description.createTestDescription(phase.getClass(),
            phase.getClass().getSimpleName());
        this.workflowDescription.addChild(this.phaseDescription);
      }

      @Override
      public void runBeforePhase(FrameworkMethod method, Iterator<FrameworkMethod> iter) {
        this.phaseDescription.addChild(describeChild(method));
      }

      @Override
      public void runAfterPhase(FrameworkMethod method, Iterator<FrameworkMethod> iter) {
        this.phaseDescription.addChild(describeChild(method));
      }
    });
    // checkstyle:WriteTag ON

    return description;
  }

  /**
   * Iterates through all workflows, executes the actions of the phases and runs all tests before and after each phase.
   *
   * @param notifier will be notified of events while tests are being run--tests being started, finishing, and failing
   * @since 0.0.1
   */
  @Override
  public void run(RunNotifier notifier) {
    printMuphin();
    Map<Class<? extends AbstractWorkflow>, Map<String, List<FrameworkMethod>>> children = getFilteredChildren();
    LOG.info("{} workflow{} in {} test class{} found.", children.size(), children.size() > 1 ? "s" : "",
        this.testClassesAmount, this.testClassesAmount > 1 ? "es" : "");
    MuphinSession session = MuphinSession.getInstance();
    // checkstyle:WriteTag OFF
    iterateThroughChildren(children, new Callback() {

      @Override
      public void workflow(AbstractWorkflow workflow) {
        session.setCurrentWorkflow(workflow);
        printWorkflowHeader(workflow);
      }

      @Override
      public void phase(AbstractPhase phase) {
        session.setCurrentPhase(phase);
        printPhaseHeader(phase);
      }

      @Override
      public void runPhase(AbstractWorkflow workflow, AbstractPhase phase) {
        LOG.info("Execute all actions of the current phase");
        phase.execute();
      }

      @Override
      public void runBeforePhase(FrameworkMethod method, Iterator<FrameworkMethod> iter) {
        LOG.info("Run before test: {}", method);
        runChild(method, notifier);
      }

      @Override
      public void runAfterPhase(FrameworkMethod method, Iterator<FrameworkMethod> iter) {
        LOG.info("Run after test: {}", method);
        runChild(method, notifier);
      }
    });
    // checkstyle:WriteTag ON
    session.setCurrentWorkflow(null);
    session.setCurrentPhase(null);
  }

  /**
   * Remove tests that don't pass the parameter filter.
   *
   * @param filter the Filter to apply
   * @throws NoTestsRemainException if all tests are filtered out
   * @since 0.0.1
   */
  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
    this.childrenLock.lock();
    try {
      Map<Class<? extends AbstractWorkflow>, Map<String, List<FrameworkMethod>>> children = getFilteredChildren();
      // checkstyle:WriteTag OFF
      iterateThroughChildren(children, new Callback() {

        @Override
        public void runBeforePhase(FrameworkMethod method, Iterator<FrameworkMethod> iter) {
          filter(filter, method, iter);
        }

        @Override
        public void runAfterPhase(FrameworkMethod method, Iterator<FrameworkMethod> iter) {
          filter(filter, method, iter);
        }

        private void filter(Filter filter, FrameworkMethod method, Iterator<FrameworkMethod> iter) {
          if (shouldRun(filter, method)) {
            try {
              filter.apply(method);
            } catch (NoTestsRemainException e) {
              iter.remove();
            }
          } else {
            iter.remove();
          }
        }
      });
      // checkstyle:WriteTag ON
      this.filteredChildren = Collections.unmodifiableMap(children);
      if (getFrameworkMethods(this.filteredChildren).isEmpty()) {
        throw new NoTestsRemainException();
      }
    } finally {
      this.childrenLock.unlock();
    }
  }

  /**
   * Describes the given test method.
   *
   * @param method a test method
   * @return a description
   * @since 0.0.1
   */
  protected Description describeChild(FrameworkMethod method) {
    Description description = this.methodDescriptions.get(method);

    if (description == null) {
      description = Description.createTestDescription(method.getMethod().getDeclaringClass(),
          method.getName(), method.getAnnotations());
      this.methodDescriptions.putIfAbsent(method, description);
    }

    return description;
  }

  /**
   * Runs a {@link Statement} that represents a leaf (aka atomic) test.
   *
   * @param statement a statement to evaluate
   * @param description a description
   * @param notifier a notifier of this test run
   * @since 0.0.1
   */
  protected final void runLeaf(Statement statement, Description description, RunNotifier notifier) {
    EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
    eachNotifier.fireTestStarted();
    try {
      statement.evaluate();
    } catch (AssumptionViolatedException e) {
      eachNotifier.addFailedAssumption(e);
    } catch (Throwable e) {
      eachNotifier.addFailure(e);
    } finally {
      eachNotifier.fireTestFinished();
    }
  }

  /**
   * Runs a test method.
   *
   * @param method a test method to execute
   * @param notifier a notifier of this test run
   * @since 0.0.1
   */
  protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);
    if (isIgnored(method)) {
      notifier.fireTestIgnored(description);
    } else {
      // checkstyle:WriteTag OFF
      Statement statement = new Statement() {
        @Override
        public void evaluate() throws Throwable {
          methodBlock(method).evaluate();
        }
      };
      // checkstyle:WriteTag ON
      runLeaf(statement, description, notifier);
    }
  }

  /**
   * Returns a new fixture for running a test. Default implementation executes the test class's no-argument constructor
   * (validation should have ensured one exists).
   *
   * @param method a test methode to execute
   * @return a new test instance
   * @since 0.0.1
   */
  protected Object createTest(FrameworkMethod method) throws Exception {
    return method.getDeclaringClass().getConstructor().newInstance();
  }

  /**
   * Create a new Statement for the given method block.
   *
   * @param method a test method
   * @return a statement
   * @since 0.0.1
   */
  protected Statement methodBlock(final FrameworkMethod method) {
    Object test;
    try {
      // checkstyle:WriteTag OFF
      test = new ReflectiveCallable() {
        @Override
        protected Object runReflectiveCall() throws Throwable {
          return createTest(method);
        }
      }.run();
      // checkstyle:WriteTag ON
    } catch (Throwable e) {
      return new Fail(e);
    }

    return methodInvoker(method, test);
  }

  /**
   * Returns a {@link Statement} that invokes {@code method} on {@code test}.
   *
   * @param method a test method to execute
   * @param test a class to that the test method belongs
   * @return a statement
   * @since 0.0.1
   */
  protected Statement methodInvoker(FrameworkMethod method, Object test) {
    return new InvokeMethod(method, test);
  }

  /**
   * Evaluates whether {@link FrameworkMethod}s are ignored based on the {@link Ignore} annotation.
   *
   * @param method a test method to check
   * @return {@code true} if the given test methode should be ignored, otherwise {@code false}
   * @since 0.0.1
   */
  protected boolean isIgnored(FrameworkMethod method) {
    return method.getAnnotation(Ignore.class) != null;
  }

  // checkstyle:WriteTag OFF

  private List<FrameworkMethod> getFrameworkMethods(
      Map<Class<? extends AbstractWorkflow>, Map<String, List<FrameworkMethod>>> children) {
    List<FrameworkMethod> frameworkMethods = new ArrayList<>();
    iterateThroughChildren(children, new Callback() {

      @Override
      public void runBeforePhase(FrameworkMethod method, Iterator<FrameworkMethod> iter) {
        frameworkMethods.add(method);
      }

      @Override
      public void runAfterPhase(FrameworkMethod method, Iterator<FrameworkMethod> iter) {
        frameworkMethods.add(method);
      }
    });
    return frameworkMethods;
  }

  private void iterateThroughChildren(
      Map<Class<? extends AbstractWorkflow>, Map<String, List<FrameworkMethod>>> children, Callback callback) {
    /*
     * Reduces complexity for several methods that iterates through the filteredChildren. This methods only have to
     * implement the needed callback methods.
     */
    for (AbstractWorkflow workflow : getWorkflows()) {
      callback.workflow(workflow);
      List<AbstractPhase> phases = getPhases(workflow);
      for (AbstractPhase phase : phases) {
        callback.phase(phase);
        Map<String, List<FrameworkMethod>> workflowChildren = children.get(workflow.getClass());
        if (workflowChildren != null) {
          for (Iterator<FrameworkMethod> iter = getIterator(workflowChildren, beforePhaseKey(phase)); iter.hasNext();) {
            FrameworkMethod testMethod = iter.next();
            callback.runBeforePhase(testMethod, iter);
          }
          callback.runPhase(workflow, phase);
          for (Iterator<FrameworkMethod> iter = getIterator(workflowChildren, afterPhaseKey(phase)); iter.hasNext();) {
            FrameworkMethod testMethod = iter.next();
            callback.runAfterPhase(testMethod, iter);
          }
        }
      }
    }
  }

  private List<AbstractWorkflow> getWorkflows() {
    return getFilteredChildren().keySet()
        .stream()
        .map(InstanceManager.getInstance()::getWorkflow)
        .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
        .collect(Collectors.toList());
  }

  private Iterator<FrameworkMethod> getIterator(Map<String, List<FrameworkMethod>> childrenOfWorkflow, String key) {
    List<FrameworkMethod> methods = childrenOfWorkflow.get(key);
    if (methods == null) {
      methods = new ArrayList<>();
    }
    return methods.iterator();
  }

  private Map<Class<? extends AbstractWorkflow>, Map<String, List<FrameworkMethod>>> getFilteredChildren() {
    if (this.filteredChildren == null) {
      this.childrenLock.lock();
      try {
        this.filteredChildren = Collections.unmodifiableMap(new HashMap<>(getChildren()));
      } finally {
        this.childrenLock.unlock();
      }
    }
    return this.filteredChildren;
  }

  private Map<Class<? extends AbstractWorkflow>, Map<String, List<FrameworkMethod>>> getChildren() {
    return scanForPhases(scanForWorkflowTestClasses());
  }

  private Set<Class<?>> scanForWorkflowTestClasses() {
    Set<Class<?>> workflowTestClasses = new HashSet<>();
    WorkflowTest workflowTestAnnotation = this.testClass.getJavaClass().getAnnotation(WorkflowTest.class);
    if (workflowTestAnnotation == null) {
      Reflections refUtil = new Reflections("");
      WorkflowClasses workflowClassesAnnotation = this.testClass.getJavaClass().getAnnotation(WorkflowClasses.class);
      Set<Class<?>> classesToExecute = new HashSet<>();
      if (workflowClassesAnnotation != null) {
        classesToExecute.addAll(Arrays.asList(workflowClassesAnnotation.value()));
      }
      try {
        Set<Class<?>> foundClasses = refUtil.getTypesAnnotatedWith(WorkflowTest.class);
        if (workflowClassesAnnotation == null) {
          workflowTestClasses.addAll(foundClasses);
        } else {
          workflowTestClasses = foundClasses.stream()
              .filter(classesToExecute::contains)
              .collect(Collectors.toSet());
        }
      } catch (ReflectionsException e) {
        throw new IllegalStateException("An error occurs while scanning classpath for test classes", e);
      }
    } else {
      workflowTestClasses.add(this.testClass.getJavaClass());
    }

    this.testClassesAmount = workflowTestClasses.size();
    return workflowTestClasses;
  }

  private Map<Class<? extends AbstractWorkflow>, Map<String, List<FrameworkMethod>>> scanForPhases(
      Set<Class<?>> workflowTestClasses) {
    Map<Class<? extends AbstractWorkflow>, Map<String, List<FrameworkMethod>>> children = new HashMap<>();

    /* Each test method is assigned to a workflow and a phase. Therefore, all workflow test classes must be searched
     * first. Then the @Phase annotation can be used to collect the test methods belonging to this workflow and assign
     * them to their phase.
     * A distinction must be made between test methods that are executed before a phase and test methods that are
     * executed after a phase.
     */
    for (Class<?> workflowTestClass : workflowTestClasses) {
      WorkflowTest annotation = workflowTestClass.getAnnotation(WorkflowTest.class);
      AbstractWorkflow workflow = INSTANCES.getWorkflow(annotation.value());
      children.compute(workflow.getClass(), (key, value) -> {
        if (value == null) {
          value = new HashMap<>();
        }

        Method[] methods = MethodSorter.getDeclaredMethods(workflowTestClass);
        for (Method method : methods) {
          Phase phaseAnnotation = method.getAnnotation(Phase.class);
          if (phaseAnnotation != null) {
            computePhases(value, method, phaseAnnotation, true);
            computePhases(value, method, phaseAnnotation, false);
          }
        }

        return value;
      });
    }
    LOG.trace("All test methods on the classpath grouped by workflow and phases: {}", children);
    return children;
  }

  private void computePhases(Map<String, List<FrameworkMethod>> value, Method method,
      Phase annotation, boolean before) {
    Class<? extends AbstractPhase> phaseClass = before ? annotation.beforePhase() : annotation.afterPhase();
    AbstractPhase phase = INSTANCES.getPhase(phaseClass);
    if (!ignorePhase(phase)) {
      String phaseKey = before ? beforePhaseKey(phase) : afterPhaseKey(phase);
      value.compute(phaseKey, (key, methodsOfPhase) -> {
        if (methodsOfPhase == null) {
          methodsOfPhase = new ArrayList<>();
        }
        methodsOfPhase.add(new FrameworkMethod(method));
        return methodsOfPhase;
      });
    }
  }

  private boolean ignorePhase(AbstractPhase phaseClass) {
    return phaseClass instanceof NoPhase;
  }

  private List<AbstractPhase> getPhases(AbstractWorkflow workflow) {
    List<AbstractPhase> phases = new ArrayList<>();
    phases.add(INSTANCES.getPhase(SetupPhase.class));
    phases.addAll(workflow.getPhases());
    phases.add(INSTANCES.getPhase(TearDownPhase.class));
    return phases;
  }

  private boolean shouldRun(Filter filter, FrameworkMethod each) {
    return filter.shouldRun(describeChild(each));
  }

  private String beforePhaseKey(AbstractPhase phase) {
    return StringUtils.join("before ", phase.getName());
  }

  private String afterPhaseKey(AbstractPhase phase) {
    return StringUtils.join("after ", phase.getName());
  }

  private void printMuphin() {
    LOG.info("");
    LOG.info("        ▟█████▙╗                                               ");
    LOG.info("     ▟█▛╝░●░░░░▜█▙╗                                            ");
    LOG.info("   ▟█╝░●░░░░░░●░░░█▙                             ‗     ‗       ");
    LOG.info("  ▟█╝░░░░░●░░░░░●░░█▙╗                          ║ ║   (‗)      ");
    LOG.info(" ██╝░●░░░░░░░░░░░░░░██╗    ‗ ‗‗ ‗‗‗  ‗   ‗ ‗ ‗‗ ║ ║‗‗  ‗ ‗ ‗‗  ");
    LOG.info(" ╚█▙▃▞║▚▃▞▚▃▞▚▃▞║▚▃▟▛╔╝   ║ '‗ ` ‗ ⑊║ ║ ║ ║ '‗ ⑊║ '‗ ⑊║ ║ '‗ ⑊ ");
    LOG.info("   ╚█░░║░░░║░░░║░░█╔╝     ║ ║ ║ ║ ║ ║ ║‗║ ║ ║‗) ║ ║ ║ ║ ║ ║ ║ ║");
    LOG.info("    ╚█░░║░░║░░║░░█╔╝      ║‗║ ║‗║ ║‗║⑊‗‗,‗║ .‗‗ ⃫║‗║ ║‗║‗║‗║ ║‗║");
    LOG.info("     ╚███████████╔╝                       ║‗║                  ");
    LOG.info("      ╚══════════╝  Simplify your process and workflow testing ");
    LOG.info("");
  }

  private void printWorkflowHeader(AbstractWorkflow workflow) {
    String line = StringUtils.rightPad("", workflow.getName().length() + 23, "#");
    LOG.info("");
    LOG.info("{}", line);
    LOG.info("#   Run Workflow '{}'   #", workflow.getName());
    LOG.info("{}", line);
  }

  private void printPhaseHeader(AbstractPhase phase) {
    String line = StringUtils.rightPad("", phase.getName().length() + 20, "-");
    LOG.info("{}", line);
    LOG.info("|   Run Phase '{}'   |", phase.getName());
    LOG.info("{}", line);
  }

  // checkstyle:WriteTag ON
}
