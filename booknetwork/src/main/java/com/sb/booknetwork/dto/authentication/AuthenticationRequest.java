package com.sb.booknetwork.dto.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    @NotBlank(message = "email is mandatory")
    @Email(message = "email is not valid")
    private String email;

    @NotBlank(message = "password is mandatory")
    @Size(min = 8, message = "password must be at least 8 characters long")
    private String password;

}
