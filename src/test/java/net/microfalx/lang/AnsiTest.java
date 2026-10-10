package net.microfalx.lang;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.microfalx.lang.RichText.Color;
import static net.microfalx.lang.RichText.Style;
import static org.junit.jupiter.api.Assertions.*;

class AnsiTest {

    private boolean enabled;

    @BeforeEach
    void setup() {
        enabled = Ansi.isEnabled();
        Ansi.setEnabled(true);
    }

    @AfterEach
    void tearDown() {
        Ansi.setEnabled(enabled);
    }

    @Test
    void colors() {
        assertEquals("\u001B[31m", Ansi.foreground(Color.RED));
        assertEquals("\u001B[41m", Ansi.background(Color.RED));
        assertEquals("\u001B[91m", Ansi.foreground(Color.BRIGHT_RED));
        assertEquals("\u001B[107m", Ansi.background(Color.BRIGHT_WHITE));
        assertEquals("\u001B[39m", Ansi.foreground(Color.DEFAULT));
        assertEquals(Color.BRIGHT_BLACK, Color.GRAY);
    }

    @Test
    void extendedColors() {
        assertEquals("\u001B[38;5;208m", Ansi.foreground(208));
        assertEquals("\u001B[48;5;208m", Ansi.background(208));
        assertEquals("\u001B[38;2;255;136;0m", Ansi.foreground(255, 136, 0));
        assertEquals("\u001B[38;2;255;136;0m", Ansi.foreground("#ff8800"));
        assertEquals("\u001B[48;2;255;136;0m", Ansi.background("f80"));
        assertThrows(IllegalArgumentException.class, () -> Ansi.foreground(256));
        assertThrows(IllegalArgumentException.class, () -> Ansi.foreground(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> Ansi.foreground("#ff88"));
        assertThrows(IllegalArgumentException.class, () -> Ansi.foreground("#gg8800"));
    }

    @Test
    void color() {
        assertEquals("\u001B[31merror\u001B[39m", Ansi.color("error", Color.RED));
        assertEquals("\u001B[30m\u001B[43mwarn\u001B[39m\u001B[49m", Ansi.highlight("warn"));
        assertEquals("\u001B[44minfo\u001B[49m", Ansi.highlight("info", Color.BLUE));
        assertEquals("", Ansi.color("", Color.RED));
    }

    @Test
    void style() {
        assertEquals("\u001B[1mbold\u001B[22m", Ansi.bold("bold"));
        assertEquals("\u001B[3mitalic\u001B[23m", Ansi.italic("italic"));
        assertEquals("\u001B[1m\u001B[4mtext\u001B[24m\u001B[22m", Ansi.style("text", Style.BOLD, Style.UNDERLINE));
    }

    @Test
    void builder() {
        assertEquals("\u001B[31m\u001B[1merror\u001B[0m: failed",
                Ansi.ansi().fg(Color.RED).bold().a("error").reset().a(": failed").toString());
    }

    @Test
    void disabled() {
        Ansi.setEnabled(false);
        assertEquals("error", Ansi.color("error", Color.RED));
        assertEquals("bold", Ansi.bold("bold"));
        assertEquals("error: failed", Ansi.ansi().fg(Color.RED).bold().a("error").reset().a(": failed").toString());
    }

    @Test
    void strip() {
        assertNull(Ansi.strip(null));
        assertEquals("plain", Ansi.strip("plain"));
        assertEquals("error", Ansi.strip(Ansi.bold(Ansi.color("error", Color.RED))));
        assertEquals("orange", Ansi.strip(Ansi.color("orange", "#ff8800")));
        assertEquals("site", Ansi.strip(Ansi.link("site", "https://example.com")));
        assertEquals("ab", Ansi.strip("a" + Ansi.CLEAR_LINE + Ansi.cursorTo(1, 1) + Ansi.CURSOR_SAVE + "b"));
        assertEquals(5, Ansi.length(Ansi.highlight("error")));
    }

}
