package models.fakeapiuser;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoot{

	@JsonProperty("password")
	private String password;

	@JsonProperty("address")
	private Address address;

	@JsonProperty("phone")
	private String phone;

	@JsonProperty("__v")
	private int V;

	@JsonProperty("name")
	private Name name;

	@JsonProperty("id")
	private int id;

	@JsonProperty("email")
	private String email;

	@JsonProperty("username")
	private String username;
}