package io.example.showcase.treenode;

import javax.persistence.Entity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;

import io.cornerstone.core.hibernate.domain.AbstractTreeableEntity;
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

	interface View {

		interface Editable {

		}

		interface Creation extends Editable {

		}

		interface Update extends Editable {

		}

		interface List extends Persistable<Long>, Pageable, Creation, Update {

		}

	}
}