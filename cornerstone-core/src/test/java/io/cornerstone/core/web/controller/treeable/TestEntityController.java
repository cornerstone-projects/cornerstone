package io.cornerstone.core.web.controller.treeable;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import io.cornerstone.core.web.AbstractTreeableEntityController;

@TestComponent
@RestController
@Validated
public class TestEntityController extends AbstractTreeableEntityController<TestEntity> {

}
