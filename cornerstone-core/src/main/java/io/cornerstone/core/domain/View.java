package io.cornerstone.core.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;

public interface View {

	interface Edit {

	}

	interface Creation extends Edit {

	}

	interface Update extends Edit {

	}

	interface List extends Persistable<Long>, Pageable, Creation, Update {

	}

}