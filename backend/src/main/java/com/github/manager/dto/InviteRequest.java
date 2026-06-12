package com.github.manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InviteRequest(
        @NotBlank(message = "rawUsernames is required") String rawUsernames,
        @NotEmpty(message = "At least one repository is required") List<String> repositories,
        String permission,
        InviteMode mode
) {
    public InviteRequest {
        if (permission == null || permission.isBlank()) {
            permission = "push";
        }
        if (mode == null) {
            mode = InviteMode.INDIVIDUAL;
        }
    }
}