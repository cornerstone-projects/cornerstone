package com.example.demo.user;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;

@Data
public class PasswordChangeRequest implements Serializable {

	private static final long serialVersionUID = 7840779509938663757L;

	private @Size(min = 6, max = 50) String currentPassword;

	@JsonView(UserController.class)
	private @NotEmpty @Size(min = 6, max = 50) String password;

	@JsonView(UserController.class)
	private @NotEmpty @Size(min = 6, max = 50) String confirmedPassword;

	@JsonIgnore
	public boolean isWrongConfirmedPassword() {
		return !password.equals(confirmedPassword);
	}

}