package io.cornerstone.core.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.Nullable;

import org.springframework.util.CollectionUtils;

@SuppressWarnings("unchecked")
public interface Treeable<T extends Treeable<T, ID>, ID> {

	@Nullable default ID getId() {
		return null;
	}

	@Nullable String getName();

	@Nullable T getParent();

	void setParent(@Nullable T parent);

	@Nullable Collection<T> getChildren();

	void setChildren(@Nullable Collection<T> children);

	default void addChild(T... children) {
		for (T child : children) {
			child.setParent((T) this);
			Collection<T> coll = getChildren();
			if (coll == null) {
				coll = new ArrayList<>();
				setChildren(coll);
			}
			coll.add(child);
		}
	}

	default Integer getLevel() {
		int level = 1;
		T parent = getParent();
		while (parent != null) {
			level++;
			parent = parent.getParent();
		}
		return level;
	}

	@JsonIgnore
	default boolean isLeaf() {
		return CollectionUtils.isEmpty(getChildren());
	}

	@JsonIgnore
	default boolean isRoot() {
		return this.getParent() == null;
	}

	default T findDescendantOrSelfById(ID id) {
		if (id == null) {
			throw new IllegalArgumentException("id must not be null");
		}
		if (id.equals(this.getId())) {
			return (T) this;
		}
		Collection<T> children = getChildren();
		if (children != null) {
			for (T t : children) {
				T tt = t.findDescendantOrSelfById(id);
				if (tt != null) {
					return tt;
				}
			}
		}
		return null;
	}

	default T findDescendantOrSelfByName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name must not be null");
		}
		if (name.equals(this.getName())) {
			return (T) this;
		}
		Collection<T> children = getChildren();
		if (children != null) {
			for (T t : children) {
				T tt = t.findDescendantOrSelfByName(name);
				if (tt != null) {
					return tt;
				}
			}
		}
		return null;
	}

	default List<T> findDescendants() {
		List<T> list = new ArrayList<>();
		if (!this.isLeaf()) {
			Collection<T> children = getChildren();
			if (children != null) {
				for (T obj : children) {
					collect(obj, list);
				}
			}
		}
		return list;
	}

	default List<T> findDescendantsAndSelf() {
		List<T> list = new ArrayList<>();
		collect((T) this, list);
		return list;
	}

	private void collect(T node, Collection<T> coll) {
		coll.add(node);
		if (node.isLeaf()) {
			return;
		}
		Collection<T> children = node.getChildren();
		if (children != null) {
			for (T obj : children) {
				collect(obj, coll);
			}
		}
	}

	default boolean isAncestorOrSelfOf(T t) {
		T parent = t;
		while (parent != null) {
			ID parentId = parent.getId();
			if ((parentId != null) && parentId.equals(this.getId())) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	default boolean isDescendantOrSelfOf(T t) {
		return (t != null) && t.isAncestorOrSelfOf((T) this);
	}

	default T findAncestor(int level) {
		if ((level < 1) || (level > this.getLevel())) {
			return null;
		}
		T parent = (T) this;
		while (parent != null) {
			if (parent.getLevel() == level) {
				return parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

}
