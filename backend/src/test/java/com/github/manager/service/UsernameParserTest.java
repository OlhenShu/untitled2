package com.github.manager.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class UsernameParserTest {

    private final UsernameParser parser = new UsernameParser();

    @Test
    void parsesBasicUsernames() {
        List<String> result = parser.parse("octocat\nhubot\ndefunkt");
        assertThat(result).containsExactly("octocat", "hubot", "defunkt");
    }

    @Test
    void removesAtPrefix() {
        List<String> result = parser.parse("@octocat\n@hubot");
        assertThat(result).containsExactly("octocat", "hubot");
    }

    @Test
    void ignoresCommentLines() {
        List<String> result = parser.parse("# this is a comment\noctocat\n# another comment\nhubot");
        assertThat(result).containsExactly("octocat", "hubot");
    }

    @Test
    void ignoresEmptyLines() {
        List<String> result = parser.parse("octocat\n\n\nhubot\n   \ndefunkt");
        assertThat(result).containsExactly("octocat", "hubot", "defunkt");
    }

    @Test
    void trimsWhitespace() {
        List<String> result = parser.parse("  octocat  \n\t hubot\t");
        assertThat(result).containsExactly("octocat", "hubot");
    }

    @Test
    void deduplicates() {
        List<String> result = parser.parse("octocat\noctocat\n@octocat\n  octocat  ");
        assertThat(result).containsExactly("octocat");
    }

    @Test
    void handlesWindowsLineEndings() {
        List<String> result = parser.parse("octocat\r\nhubot\r\ndefunkt");
        assertThat(result).containsExactly("octocat", "hubot", "defunkt");
    }

    @Test
    void returnsEmptyForNullInput() {
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    void returnsEmptyForBlankInput() {
        assertThat(parser.parse("   \n\n  ")).isEmpty();
    }

    @Test
    void parsesRealWorldExample() {
        String input = """
                # Students — Spring 2024
                octocat
                @hubot
                  defunkt
                """;
        List<String> result = parser.parse(input);
        assertThat(result).containsExactly("octocat", "hubot", "defunkt");
    }
}
