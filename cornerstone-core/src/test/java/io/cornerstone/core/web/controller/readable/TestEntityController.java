package io.cornerstone.core.web.controller.readable;

import io.cornerstone.core.web.AbstractReadableEntityController;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.web.bind.annotation.RestController;

@TestComponent
@RestController
class TestEntityController extends AbstractReadableEntityController<TestEntity, Long> {

}
