package com.github.manager.dto;

import java.util.List;
import java.util.Map;

public record InviteResponse(List<InviteResult> results, Map<String, Long> summary) {

    public static InviteResponse of(List<InviteResult> results) {
        Map<String, Long> summary = Map.of(
                "total", (long) results.size(),
                "invited", results.stream().filter(r -> "invited".equals(r.status())).count(),
                "already_collaborator", results.stream().filter(r -> "already_collaborator".equals(r.status())).count(),
                "user_not_found", results.stream().filter(r -> "user_not_found".equals(r.status())).count(),
                "failed", results.stream().filter(r -> "failed".equals(r.status())).count()
        );
        return new InviteResponse(results, summary);
    }
}