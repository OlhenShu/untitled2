package com.github.manager.dto;

public record InviteResult(
        String repository,
        String username,
        String status,
        String message
) {
    public static InviteResult invited(String repository, String username) {
        return new InviteResult(repository, username, "invited", null);
    }

    public static InviteResult alreadyCollaborator(String repository, String username) {
        return new InviteResult(repository, username, "already_collaborator", null);
    }

    public static InviteResult userNotFound(String repository, String username) {
        return new InviteResult(repository, username, "user_not_found", "GitHub user not found: " + username);
    }

    public static InviteResult failed(String repository, String username, String message) {
        return new InviteResult(repository, username, "failed", message);
    }
}