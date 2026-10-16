package lab.store.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NewUserRequest(@NotBlank String id, @NotBlank String name,
                             @Email @NotBlank String email) { }
