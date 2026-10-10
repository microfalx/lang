package net.microfalx.lang;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.microfalx.lang.RichText.Color;
import static net.microfalx.lang.RichText.Style;
import static org.junit.jupiter.api.Assertions.*;

class HtmlTest {

    private boolean enabled;

    @BeforeEach
    void setup() {
        enabled = Html.isEnabled();
        Html.setEnabled(true);
    }

    @AfterEach
    void tearDown() {
        Html.setEnabled(enabled);
    }

    @Test
    void colors() {
        assertEquals("#cd3131", Html.toCss(Color.RED));
        assertEquals("#f14c4c", Html.toCss(Color.BRIGHT_RED));
        assertEquals("#ffffff", Html.toCss(Color.BRIGHT_WHITE));
        assertEquals("inherit", Html.toCss(Color.DEFAULT));
    }

    @Test
    void extendedColors() {
        assertEquals("#cd3131", Html.toCss(1));
        assertEquals("#ff8700", Html.toCss(208));
        assertEquals("#080808", Html.toCss(232));
        assertEquals("#eeeeee", Html.toCss(255));
        assertEquals("#ff8800", Html.toCss(255, 136, 0));
        assertEquals("#ff8800", Html.toCss("#FF8800"));
        assertEquals("#f80", Html.toCss("f80"));
        assertThrows(IllegalArgumentException.class, () -> Html.toCss(256));
        assertThrows(IllegalArgumentException.class, () -> Html.toCss(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> Html.toCss("#ff88"));
        assertThrows(IllegalArgumentException.class, () -> Html.toCss("#gg8800"));
    }

    @Test
    void color() {
        assertEquals("<span style=\"color:#cd3131\">error</span>", Html.color("error", Color.RED));
        assertEquals("<span style=\"color:#000000;background-color:#e5e510\">warn</span>", Html.highlight("warn"));
        assertEquals("<span style=\"background-color:#2472c8\">info</span>", Html.highlight("info", Color.BLUE));
        assertEquals("<span style=\"color:#ff8800\">orange</span>", Html.color("orange", "#ff8800"));
        assertEquals("", Html.color("", Color.RED));
    }

    @Test
    void style() {
        assertEquals("<span style=\"font-weight:bold\">bold</span>", Html.bold("bold"));
        assertEquals("<span style=\"font-weight:bold;text-decoration:underline line-through\">text</span>",
                Html.style("text", Style.BOLD, Style.UNDERLINE, Style.STRIKETHROUGH));
        assertEquals("<span style=\"color:#cd3131;font-weight:bold\">error</span>",
                Html.format("error", Color.RED, null, Style.BOLD));
    }

    @Test
    void escape() {
        assertEquals("", Html.escape(null));
        assertEquals("plain", Html.escape("plain"));
        assertEquals("a &lt;b&gt; &amp; &quot;c&quot; &#39;d&#39;", Html.escape("a <b> & \"c\" 'd'"));
        assertEquals("<span style=\"color:#cd3131\">&lt;error&gt;</span>", Html.color("<error>", Color.RED));
        assertEquals("<a href=\"https://example.com?a=1&amp;b=2\">site</a>", Html.link("site", "https://example.com?a=1&b=2"));
    }

    @Test
    void builder() {
        assertEquals("<span style=\"color:#cd3131;font-weight:bold\">error</span>: failed",
                Html.html().fg(Color.RED).bold().a("error").reset().a(": failed").toString());
        assertEquals("<span style=\"color:#cd3131\">a</span><span style=\"color:#cd3131;font-style:italic\">b</span>",
                Html.html().fg(Color.RED).a("a").italic().a("b").toString());
        assertEquals("<span style=\"color:#cd3131\">ab</span>", Html.html().fg(Color.RED).a("a").a("b").toString());
        assertEquals("x<br><span style=\"font-weight:bold\">y</span>",
                Html.html().a("x").newline().raw(Html.bold("y")).toString());
    }

    @Test
    void disabled() {
        Html.setEnabled(false);
        assertEquals("&lt;error&gt;", Html.color("<error>", Color.RED));
        assertEquals("bold", Html.bold("bold"));
        assertEquals("error: failed", Html.html().fg(Color.RED).bold().a("error").reset().a(": failed").toString());
    }

    @Test
    void strip() {
        assertNull(Html.strip(null));
        assertEquals("plain", Html.strip("plain"));
        assertEquals("<error> & co", Html.strip(Html.color("<error> & co", Color.RED)));
        assertEquals("site", Html.strip(Html.link("site", "https://example.com")));
        assertEquals("A B &unknown;", Html.strip("&#65;&nbsp;&#x42; &unknown;"));
        assertEquals(7, Html.length(Html.highlight("<error>")));
    }

}
