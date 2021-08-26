package io.example.showcase.treenode;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import io.cornerstone.core.web.AbstractTreeableEntityController;

@RestController
@Validated
public class TreenodeController extends AbstractTreeableEntityController<Treenode> {

}
