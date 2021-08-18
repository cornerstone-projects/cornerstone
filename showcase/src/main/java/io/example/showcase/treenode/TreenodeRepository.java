package io.example.showcase.treenode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.cornerstone.core.repository.TreeableRepository;

public interface TreenodeRepository
		extends JpaRepository<Treenode, Long>, JpaSpecificationExecutor<Treenode>, TreeableRepository<Treenode> {

}
