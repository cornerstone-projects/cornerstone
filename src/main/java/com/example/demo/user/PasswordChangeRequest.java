package com.example.demo.user;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class PasswordChangeRequest implements Serializable {

	private static final long serialVersionUID = 7840779509938663757L;

	private @Size(min = 6) String currentPassword;

	private @NotEmpty @Size(min = 6) String password;

	private @NotEmpty @Size(min = 6) String confirmedPassword;

	public boolean isWrongConfirmedPassword() {
		return !password.equals(confirmedPassword);
	}

}