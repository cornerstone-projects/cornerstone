package io.cornerstone.core.hibernate.audit;

import java.time.LocalDateTime;

public interface Auditable {

	LocalDateTime getCreatedDate();

	LocalDateTime getLastModifiedDate();

	String getCreatedBy();

	String getLastModifiedBy();

}
