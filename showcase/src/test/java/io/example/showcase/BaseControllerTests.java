package io.example.showcase;

import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.ControllerTestBase;

@ContextConfiguration(classes = MainApplication.class)
public abstract class BaseControllerTests extends ControllerTestBase {

}
