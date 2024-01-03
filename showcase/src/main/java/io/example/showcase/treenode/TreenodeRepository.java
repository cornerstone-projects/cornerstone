package io.example.showcase.treenode;

import io.cornerstone.core.persistence.repository.TreeableRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TreenodeRepository
		extends JpaRepository<Treenode, Long>, JpaSpecificationExecutor<Treenode>, TreeableRepository<Treenode> {

}
