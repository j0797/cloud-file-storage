package com.example.cloudstorage.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserRegisterDto(@NotBlank(message = "Login is required.")
                              @Length(
                                      min = 6,
                                      max = 50,
                                      message = "Login length must be between {min} and {max} characters."
                              )
                              @Pattern(
                                      regexp = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$",
                                      message = "Login can contain only English letters, digits and underscores, and must not start or end with an underscore."
                              ) String username,
                              @NotBlank(message = "Password is required.")
                              @Length(
                                      min = 6,
                                      max = 255,
                                      message = "Password length must be between {min} and {max} characters."
                              ) String password) {
}