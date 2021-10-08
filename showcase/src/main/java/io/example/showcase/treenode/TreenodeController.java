package io.example.showcase.treenode;

import io.cornerstone.core.web.AbstractTreeableEntityController;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class TreenodeController extends AbstractTreeableEntityController<Treenode> {

}
