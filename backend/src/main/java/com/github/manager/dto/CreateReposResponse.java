package com.github.manager.dto;

import java.util.List;
import java.util.Map;

public record CreateReposResponse(List<RepoResult> results, Map<String, Long> summary) {

    public static CreateReposResponse of(List<RepoResult> results) {
        Map<String, Long> summary = Map.of(
                "total", (long) results.size(),
                "created", results.stream().filter(r -> "created".equals(r.status())).count(),
                "already_exists", results.stream().filter(r -> "already_exists".equals(r.status())).count(),
                "failed", results.stream().filter(r -> "failed".equals(r.status())).count()
        );
        return new CreateReposResponse(results, summary);
    }
}