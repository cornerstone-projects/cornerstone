package io.cornerstone.core.web.controller.treeable;

import io.cornerstone.core.web.AbstractTreeableEntityController;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.web.bind.annotation.RestController;

@TestComponent
@RestController
class TestEntityController extends AbstractTreeableEntityController<TestEntity> {

}
