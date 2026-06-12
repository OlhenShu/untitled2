package com.github.manager.controller;

import com.github.manager.dto.CreateReposRequest;
import com.github.manager.dto.CreateReposResponse;
import com.github.manager.service.RepoCreationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repos")
public class RepoController {

    private final RepoCreationService repoCreationService;

    public RepoController(RepoCreationService repoCreationService) {
        this.repoCreationService = repoCreationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<CreateReposResponse> generateRepos(
            @RequestHeader(value = "X-GitHub-Token", required = false) String token,
            @Valid @RequestBody CreateReposRequest request) {

        CreateReposResponse response = repoCreationService.createRepos(token, request);
        return ResponseEntity.ok(response);
    }
}
