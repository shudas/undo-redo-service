package com.shudas.rewind.undoredo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;


@Slf4j
public abstract class BaseTest {
    private BaseTestModule testModeule;
    protected Injector injector;

    protected BaseTest() {
        testModeule = new BaseTestModule();
        injector = Guice.createInjector(testModeule);
    }

    @Before
    public void baseItemTestSetup() {
        injector.injectMembers(this);
    }

    @After
    public void cleanup() {
        testModeule.cleanup();
    }
}
