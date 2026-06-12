package com.github.manager.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class UsernameParser {

    public List<String> parse(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (String line : raw.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            String username = trimmed.startsWith("@") ? trimmed.substring(1) : trimmed;
            username = username.trim();
            if (!username.isEmpty()) {
                seen.add(username);
            }
        }
        return new ArrayList<>(seen);
    }
}
