package com.github.manager.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RepoNameGenerator {

    public List<String> generateFromPattern(String baseName, int count, int startIndex, int padding) {
        if (baseName == null || baseName.isBlank()) {
            throw new IllegalArgumentException("baseName must not be blank");
        }
        if (count < 1 || count > 200) {
            throw new IllegalArgumentException("count must be between 1 and 200");
        }
        if (padding < 1) padding = 1;

        String fmt = "%s-%0" + padding + "d";
        List<String> names = new ArrayList<>(count);
        for (int i = startIndex; i < startIndex + count; i++) {
            names.add(String.format(fmt, baseName, i));
        }
        return names;
    }

    public List<String> fromList(List<String> rawNames) {
        if (rawNames == null) return List.of();
        return rawNames.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank() && !s.startsWith("#"))
                .distinct()
                .toList();
    }
}
