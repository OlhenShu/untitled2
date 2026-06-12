package com.github.manager.dto;

public record RepoResult(
        String repoName,
        String status,
        String message,
        String url
) {
    public static RepoResult created(String repoName, String url) {
        return new RepoResult(repoName, "created", null, url);
    }

    public static RepoResult alreadyExists(String repoName) {
        return new RepoResult(repoName, "already_exists", "Repository already exists", null);
    }

    public static RepoResult failed(String repoName, String message) {
        return new RepoResult(repoName, "failed", message, null);
    }
}