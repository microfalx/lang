package net.microfalx.lang;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.microfalx.lang.RichText.Color;
import static net.microfalx.lang.RichText.Format;
import static net.microfalx.lang.RichText.Style;
import static org.junit.jupiter.api.Assertions.*;

class RichTextTest {

    private static final String NL = System.lineSeparator();

    private boolean ansiEnabled;
    private boolean htmlEnabled;
    private Format defaultFormat;

    @BeforeEach
    void setup() {
        ansiEnabled = Ansi.isEnabled();
        htmlEnabled = Html.isEnabled();
        defaultFormat = RichText.getDefaultFormat();
        Ansi.setEnabled(true);
        Html.setEnabled(true);
    }

    @AfterEach
    void tearDown() {
        Ansi.setEnabled(ansiEnabled);
        Html.setEnabled(htmlEnabled);
        RichText.setDefaultFormat(defaultFormat);
    }

    @Test
    void text() {
        RichText text = status();
        assertEquals("Status: OK (3 tests)", text.render(Format.TEXT));
        assertEquals("Status: \u001B[32m\u001B[1mOK\u001B[0m (3 tests)", text.render(Format.ANSI));
        assertEquals("Status: <span style=\"color:#0dbc79;font-weight:bold\">OK</span> (3 tests)", text.render(Format.HTML));
        assertEquals(20, text.length());
        assertFalse(text.isEmpty());
        assertTrue(RichText.create().isEmpty());
    }

    @Test
    void defaultFormat() {
        assertEquals("Status: OK (3 tests)", status().toString());
        RichText.setDefaultFormat(Format.ANSI);
        assertEquals("Status: \u001B[32m\u001B[1mOK\u001B[0m (3 tests)", status().toString());
    }

    @Test
    void extendedColors() {
        RichText text = RichText.create().fg(208).a("a").fg(255, 136, 0).a("b").bg("#F80").a("c");
        assertEquals("\u001B[38;5;208ma\u001B[0m\u001B[38;2;255;136;0mb\u001B[0m\u001B[38;2;255;136;0m\u001B[48;2;255;136;0mc\u001B[0m",
                text.render(Format.ANSI));
        assertEquals("<span style=\"color:#ff8700\">a</span><span style=\"color:#ff8800\">b</span>"
                + "<span style=\"color:#ff8800;background-color:#f80\">c</span>", text.render(Format.HTML));
        assertThrows(IllegalArgumentException.class, () -> RichText.create().fg(256));
        assertThrows(IllegalArgumentException.class, () -> RichText.create().bg("#gg8800"));
    }

    @Test
    void helpers() {
        assertEquals("\u001B[31merror\u001B[0m", RichText.color("error", Color.RED).render(Format.ANSI));
        assertEquals("<span style=\"color:#000000;background-color:#e5e510\">warn</span>",
                RichText.highlight("warn").render(Format.HTML));
        assertEquals("\u001B[1m\u001B[4mtext\u001B[0m", RichText.style("text", Style.BOLD, Style.UNDERLINE).render(Format.ANSI));
        assertEquals("text", RichText.bold("text").render(Format.TEXT));
    }

    @Test
    void nested() {
        RichText text = RichText.create().bold().a("a ").a(RichText.color("b", Color.RED)).a(" c");
        assertEquals("a b c", text.render(Format.TEXT));
        assertEquals("\u001B[1ma \u001B[0m\u001B[31m\u001B[1mb\u001B[0m\u001B[1m c\u001B[0m", text.render(Format.ANSI));
        assertEquals("<span style=\"font-weight:bold\">a </span><span style=\"color:#cd3131;font-weight:bold\">b</span>"
                + "<span style=\"font-weight:bold\"> c</span>", text.render(Format.HTML));
    }

    @Test
    void newline() {
        RichText text = RichText.create().bg(Color.BLUE).a("a").newline().a("b");
        assertEquals("a" + NL + "b", text.render(Format.TEXT));
        assertEquals("\u001B[44ma\u001B[0m" + NL + "\u001B[44mb\u001B[0m", text.render(Format.ANSI));
        assertEquals("<span style=\"background-color:#2472c8\">a<br>b</span>",
                text.render(Format.HTML));
        assertEquals(3, text.length());
    }

    @Test
    void link() {
        RichText text = RichText.create().a("see ").link("site", "https://example.com?a=1&b=2");
        assertEquals("see site (https://example.com?a=1&b=2)", text.render(Format.TEXT));
        assertEquals("see \u001B]8;;https://example.com?a=1&b=2\u001B\\site\u001B]8;;\u001B\\", text.render(Format.ANSI));
        assertEquals("see <a href=\"https://example.com?a=1&amp;b=2\">site</a>", text.render(Format.HTML));
        assertEquals("https://example.com", RichText.create().link("https://example.com", "https://example.com").render(Format.TEXT));
    }

    @Test
    void escape() {
        RichText text = RichText.color("<b>", Color.RED);
        assertEquals("<b>", text.render(Format.TEXT));
        assertEquals("<span style=\"color:#cd3131\">&lt;b&gt;</span>", text.render(Format.HTML));
    }

    @Test
    void disabled() {
        Ansi.setEnabled(false);
        Html.setEnabled(false);
        assertEquals("Status: OK (3 tests)", status().render(Format.ANSI));
        assertEquals("Status: OK (3 tests)", status().render(Format.HTML));
    }

    private RichText status() {
        return RichText.create().a("Status: ").fg(Color.GREEN).bold().a("OK").reset().a(" (3 tests)");
    }

}
