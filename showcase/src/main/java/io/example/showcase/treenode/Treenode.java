package io.example.showcase.treenode;

import io.cornerstone.core.persistence.domain.AbstractTreeableEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Treenode extends AbstractTreeableEntity<Treenode> {

	private static final long serialVersionUID = 1L;

	public Treenode(String name) {
		this.name = name;
	}

	public Treenode(String name, int displayOrder) {
		this.name = name;
		this.displayOrder = displayOrder;
	}

	public Treenode(Treenode parent, String name, int displayOrder) {
		this.parent = parent;
		this.name = name;
		this.displayOrder = displayOrder;
	}

}
