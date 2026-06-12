package com.github.manager.controller;

import com.github.manager.dto.InviteRequest;
import com.github.manager.dto.InviteResponse;
import com.github.manager.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final InvitationService invitationService;

    public InviteController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/send")
    public ResponseEntity<InviteResponse> sendInvites(
            @RequestHeader(value = "X-GitHub-Token", required = false) String token,
            @Valid @RequestBody InviteRequest request) {

        InviteResponse response = invitationService.sendInvites(token, request);
        return ResponseEntity.ok(response);
    }
}
