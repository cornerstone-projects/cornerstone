package com.example.demo.user;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class UpdatePasswordRequest implements Serializable {

	private static final long serialVersionUID = 3567315541647375541L;

	private @NotEmpty @Size(min = 6, max = 50) String password;

	private @NotEmpty @Size(min = 6, max = 50) String confirmedPassword;

	@JsonIgnore
	public boolean isWrongConfirmedPassword() {
		return !password.equals(confirmedPassword);
	}

}