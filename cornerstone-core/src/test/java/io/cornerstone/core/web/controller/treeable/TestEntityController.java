package io.cornerstone.core.web.controller.treeable;

import io.cornerstone.core.web.AbstractTreeableEntityController;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@TestComponent
@RestController
@Validated
class TestEntityController extends AbstractTreeableEntityController<TestEntity> {

}
