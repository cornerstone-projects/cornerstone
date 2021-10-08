package io.example.showcase;

import io.cornerstone.test.ControllerTestBase;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = MainApplication.class)
public abstract class BaseControllerTests extends ControllerTestBase {

}
