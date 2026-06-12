package com.github.manager.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RepoNameGeneratorTest {

    private final RepoNameGenerator generator = new RepoNameGenerator();

    @Test
    void generatesNamesWithDefaultPadding() {
        List<String> names = generator.generateFromPattern("lab", 3, 1, 2);
        assertThat(names).containsExactly("lab-01", "lab-02", "lab-03");
    }

    @Test
    void generatesNamesWithCustomStartIndex() {
        List<String> names = generator.generateFromPattern("hw", 3, 5, 2);
        assertThat(names).containsExactly("hw-05", "hw-06", "hw-07");
    }

    @Test
    void generatesNamesWithSingleDigitPadding() {
        List<String> names = generator.generateFromPattern("repo", 3, 1, 1);
        assertThat(names).containsExactly("repo-1", "repo-2", "repo-3");
    }

    @Test
    void generatesNamesWithWidePadding() {
        List<String> names = generator.generateFromPattern("task", 2, 99, 3);
        assertThat(names).containsExactly("task-099", "task-100");
    }

    @Test
    void generatesSingleName() {
        List<String> names = generator.generateFromPattern("solo", 1, 1, 2);
        assertThat(names).containsExactly("solo-01");
    }

    @Test
    void throwsOnBlankBaseName() {
        assertThatThrownBy(() -> generator.generateFromPattern("", 3, 1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseName");
    }

    @Test
    void throwsOnInvalidCount() {
        assertThatThrownBy(() -> generator.generateFromPattern("test", 0, 1, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromListFiltersBlankAndComments() {
        List<String> result = generator.fromList(List.of("repo1", "", "  ", "# comment", "repo2", "repo1"));
        assertThat(result).containsExactly("repo1", "repo2");
    }
}
