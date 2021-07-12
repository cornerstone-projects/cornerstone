package com.example.demo.user;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChangePasswordRequest extends UpdatePasswordRequest {

	private static final long serialVersionUID = -1833548510727489592L;

	private @NotBlank String currentPassword;

}