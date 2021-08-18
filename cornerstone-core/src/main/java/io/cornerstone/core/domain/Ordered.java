package io.cornerstone.core.domain;

public interface Ordered<T extends Ordered<T>> extends Comparable<T> {

	Integer getDisplayOrder();

	@Override
	default int compareTo(T o) {
		if (o == null)
			return 1;
		int o1 = this.getDisplayOrder() != null ? this.getDisplayOrder() : 0;
		int o2 = o.getDisplayOrder() != null ? o.getDisplayOrder() : 0;
		if (o1 != o2)
			return o1 - o2;
		return this.toString().compareTo(o.toString());
	}

}