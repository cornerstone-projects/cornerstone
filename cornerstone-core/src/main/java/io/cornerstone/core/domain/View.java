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

	@SuppressWarnings("rawtypes")
	interface List extends Persistable, Pageable, Creation, Update {

	}

}
