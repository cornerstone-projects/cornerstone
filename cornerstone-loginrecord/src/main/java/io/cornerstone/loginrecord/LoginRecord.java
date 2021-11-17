package io.cornerstone.loginrecord;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.hibernate.domain.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "loginrecord", indexes = @Index(columnList = "date desc"))
@Getter
@Setter
public class LoginRecord extends AbstractEntity {

	@Column(nullable = false)
	@JsonView(View.List.class)
	private String username;

	@JsonView(View.List.class)
	private LocalDateTime date;

	@JsonView(View.List.class)
	private String address;

	private String sessionId;

	@JsonView(View.List.class)
	private Boolean failed;

	private String cause;

}
