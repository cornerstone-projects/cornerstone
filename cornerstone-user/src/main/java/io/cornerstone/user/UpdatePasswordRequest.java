package io.cornerstone.user;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordRequest implements Serializable {

	private static final long serialVersionUID = 3567315541647375541L;

	private @NotEmpty @Size(min = 6, max = 50) String password;

	private @NotEmpty @Size(min = 6, max = 50) String confirmedPassword;

	@JsonIgnore
	public boolean isWrongConfirmedPassword() {
		return !this.password.equals(this.confirmedPassword);
	}

}
