package de.devtime.test.muphin.helper;

import org.junit.runner.RunWith;

import de.devtime.muphin.core.WorkflowRunner;
import de.devtime.muphin.core.annotation.WorkflowClasses;

@RunWith(WorkflowRunner.class)
@WorkflowClasses({
    TestClassA1.class, TestClassA2.class
})
public class MuphinTestSuiteA {

}
