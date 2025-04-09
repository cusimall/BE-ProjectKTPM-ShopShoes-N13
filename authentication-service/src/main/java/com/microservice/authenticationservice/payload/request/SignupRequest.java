package com.microservice.authenticationservice.payload.request;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    @Email(message = "Invalid email format")
    private String email;

    private String address;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @NotBlank(message = "Gender cannot be empty")
    @Pattern(regexp = "^(Nam|Nu|Other)$", message = "Gender must be 'Nam', 'Nu', or 'Other'")
    private String gender;

    @NotBlank(message = "Birth date cannot be empty")
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "Birth date must be in dd/MM/yyyy format")
    private String birth;

    private Set<String> role;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;

    // Explicit getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public Set<String> getRole() {
        return role;
    }

    public void setRole(Set<String> role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
