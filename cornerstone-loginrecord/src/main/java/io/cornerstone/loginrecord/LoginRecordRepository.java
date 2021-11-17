package io.cornerstone.loginrecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface LoginRecordRepository extends JpaRepository<LoginRecord, Long> {

}
