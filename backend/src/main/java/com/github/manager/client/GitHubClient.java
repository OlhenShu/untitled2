package com.github.manager.client;

import com.github.manager.dto.InviteResult;
import com.github.manager.dto.RepoResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class GitHubClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);
    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 1000;

    private final RestClient restClient;

    @Value("${github.token:}")
    private String envToken;

    public GitHubClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    private String resolveToken(String requestToken) {
        String token = (requestToken != null && !requestToken.isBlank()) ? requestToken : envToken;
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "GitHub token is required. Set GITHUB_TOKEN env variable or provide X-GitHub-Token header.");
        }
        return token;
    }

    @SuppressWarnings("unchecked")
    public RepoResult createRepoFromTemplate(
            String token,
            String templateOwner, String templateRepo,
            String targetOrg, String repoName,
            String description, boolean includeAllBranches, boolean isPrivate) {

        String resolved = resolveToken(token);

        Map<String, Object> body = new HashMap<>();
        body.put("owner", targetOrg);
        body.put("name", repoName);
        body.put("include_all_branches", includeAllBranches);
        body.put("private", isPrivate);
        if (description != null && !description.isBlank()) {
            body.put("description", description.replace("{name}", repoName));
        }

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                ResponseEntity<Map> response = restClient.post()
                        .uri("/repos/{to}/{tr}/generate", templateOwner, templateRepo)
                        .header("Authorization", "Bearer " + resolved)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toEntity(Map.class);

                String url = null;
                if (response.getBody() != null) {
                    url = (String) response.getBody().get("html_url");
                }
                return RepoResult.created(repoName, url);

            } catch (HttpClientErrorException e) {
                int status = e.getStatusCode().value();
                if (status == 422) {
                    return RepoResult.alreadyExists(repoName);
                }
                if (status == 401) {
                    return RepoResult.failed(repoName, "Invalid token (401 Unauthorized)");
                }
                if ((status == 403 || status == 429) && attempt < MAX_RETRIES - 1) {
                    log.warn("Rate limited ({}), attempt {}/{}. Backing off...", status, attempt + 1, MAX_RETRIES);
                    sleep(backoff(attempt));
                    continue;
                }
                return RepoResult.failed(repoName, "HTTP " + status + ": " + truncate(e.getResponseBodyAsString()));

            } catch (HttpServerErrorException e) {
                if (attempt < MAX_RETRIES - 1) {
                    sleep(backoff(attempt));
                    continue;
                }
                return RepoResult.failed(repoName, "Server error " + e.getStatusCode());
            } catch (Exception e) {
                return RepoResult.failed(repoName, "Unexpected error: " + e.getMessage());
            }
        }
        return RepoResult.failed(repoName, "Max retries exceeded");
    }

    public InviteResult inviteCollaborator(
            String token, String owner, String repo, String username, String permission) {

        String resolved = resolveToken(token);
        String repository = owner + "/" + repo;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                ResponseEntity<Void> response = restClient.put()
                        .uri("/repos/{owner}/{repo}/collaborators/{username}", owner, repo, username)
                        .header("Authorization", "Bearer " + resolved)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("permission", permission))
                        .retrieve()
                        .toBodilessEntity();

                if (response.getStatusCode().value() == 204) {
                    return InviteResult.alreadyCollaborator(repository, username);
                }
                return InviteResult.invited(repository, username);

            } catch (HttpClientErrorException e) {
                int status = e.getStatusCode().value();
                if (status == 404) {
                    return InviteResult.userNotFound(repository, username);
                }
                if (status == 401) {
                    return InviteResult.failed(repository, username, "Invalid token (401 Unauthorized)");
                }
                if ((status == 403 || status == 429) && attempt < MAX_RETRIES - 1) {
                    log.warn("Rate limited ({}), attempt {}/{}. Backing off...", status, attempt + 1, MAX_RETRIES);
                    sleep(backoff(attempt));
                    continue;
                }
                return InviteResult.failed(repository, username, "HTTP " + status + ": " + truncate(e.getResponseBodyAsString()));

            } catch (HttpServerErrorException e) {
                if (attempt < MAX_RETRIES - 1) {
                    sleep(backoff(attempt));
                    continue;
                }
                return InviteResult.failed(repository, username, "Server error " + e.getStatusCode());
            } catch (Exception e) {
                return InviteResult.failed(repository, username, "Unexpected error: " + e.getMessage());
            }
        }
        return InviteResult.failed(repository, username, "Max retries exceeded");
    }

    public boolean userExists(String token, String username) {
        try {
            restClient.get()
                    .uri("/users/{username}", username)
                    .header("Authorization", "Bearer " + resolveToken(token))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.debug("Could not verify user existence for '{}': {}", username, e.getMessage());
            return true;
        }
    }

    private long backoff(int attempt) {
        return BASE_BACKOFF_MS * (long) Math.pow(2, attempt);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String truncate(String s) {
        if (s == null) return "";
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }
}