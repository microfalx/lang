package net.microfalx.lang;

import static net.microfalx.lang.ArgumentUtils.requireBounded;

/**
 * Common Unicode glyphs (symbols and emojis) used to decorate text, in logs, terminals or web pages.
 * <p>
 * Glyphs come in two flavors, and each group below documents which one it uses:
 * <ul>
 *     <li><b>text</b> glyphs take one column in a terminal and use the current text color; characters which can
 *     also be rendered as emojis are followed by the text presentation selector (U+FE0E), so they always render
 *     as text</li>
 *     <li><b>emoji</b> glyphs take two columns in a terminal and have their own colors; characters which can also
 *     be rendered as text are followed by the emoji presentation selector (U+FE0F), so they always render as
 *     emojis</li>
 * </ul>
 * Avoid mixing the two flavors on lines which should be aligned (like a list of statuses).
 *
 * @see Logger
 * @see RichText
 */
@SuppressWarnings({"unused"})
public final class Glyph {

    private static final String TEXT = "\uFE0E";
    private static final String EMOJI = "\uFE0F";

    private Glyph() {
    }

    // ----------------------------------------------------
    // BULLETS / LIST MARKERS (text)
    // ----------------------------------------------------

    public static final String BULLET = "•";
    public static final String TRIANGULAR_BULLET = "‣";
    public static final String WHITE_BULLET = "◦";
    public static final String LARGE_CIRCLE = "○";
    public static final String LARGE_BLACK_CIRCLE = "●";
    public static final String HALF_CIRCLE = "◐";
    public static final String SMALL_SQUARE = "▪" + TEXT;
    public static final String WHITE_SMALL_SQUARE = "▫" + TEXT;
    public static final String TRIANGLE_RIGHT = "▶" + TEXT;
    public static final String DIAMOND = "◆";
    public static final String WHITE_DIAMOND = "◇";
    public static final String STAR = "★";
    public static final String WHITE_STAR = "☆";

    // ----------------------------------------------------
    // SUCCESS / FAILURE (text)
    // ----------------------------------------------------

    public static final String CHECK = "✓";
    public static final String CHECK_HEAVY = "✔" + TEXT;
    public static final String CROSS = "✗";
    public static final String CROSS_HEAVY = "✖" + TEXT;
    public static final String BALLOT = "☐";
    public static final String BALLOT_CHECK = "☑" + TEXT;
    public static final String BALLOT_X = "☒";

    // ----------------------------------------------------
    // SUCCESS / FAILURE (emoji)
    // ----------------------------------------------------

    public static final String CHECK_MARK = "✅";
    public static final String CROSS_MARK = "❌";
    public static final String NO_ENTRY = "⛔";
    public static final String PROHIBITED = "🚫";
    public static final String STOP_SIGN = "🛑";
    public static final String THUMBS_UP = "👍";
    public static final String THUMBS_DOWN = "👎";

    // ----------------------------------------------------
    // STATUS DOTS (emoji)
    // ----------------------------------------------------

    public static final String GREEN_CIRCLE = "🟢";
    public static final String YELLOW_CIRCLE = "🟡";
    public static final String ORANGE_CIRCLE = "🟠";
    public static final String RED_CIRCLE = "🔴";
    public static final String BLUE_CIRCLE = "🔵";
    public static final String WHITE_CIRCLE = "⚪";
    public static final String BLACK_CIRCLE = "⚫";

    // ----------------------------------------------------
    // WARNING / INFO (text)
    // ----------------------------------------------------

    public static final String WARNING = "⚠" + TEXT;
    public static final String INFO = "ℹ" + TEXT;

    // ----------------------------------------------------
    // ALERTS / HINTS (emoji)
    // ----------------------------------------------------

    public static final String QUESTION = "❓";
    public static final String EXCLAMATION = "❗";
    public static final String ALARM = "🚨";
    public static final String BELL = "🔔";
    public static final String LIGHT_BULB = "💡";
    public static final String MEMO = "📝";
    public static final String SPARKLES = "✨";
    public static final String FIRE = "🔥";

    // ----------------------------------------------------
    // ARROWS / FLOW (text)
    // ----------------------------------------------------

    public static final String ARROW_RIGHT = "→";
    public static final String ARROW_LEFT = "←";
    public static final String ARROW_UP = "↑";
    public static final String ARROW_DOWN = "↓";
    public static final String ARROW_LEFT_RIGHT = "↔" + TEXT;
    public static final String DOUBLE_ARROW_RIGHT = "⇒";
    public static final String HEAVY_ARROW_RIGHT = "➜";
    public static final String LONG_ARROW_RIGHT = "⟶";
    public static final String RETURN = "↵";
    public static final String REFRESH = "↻";
    public static final String UNDO = "↺";

    // ----------------------------------------------------
    // ACTIONS / LIFECYCLE (emoji)
    // ----------------------------------------------------

    public static final String ADD = "➕";
    public static final String REMOVE = "➖";
    public static final String EDIT = "✏" + EMOJI;
    public static final String DELETE = "🗑" + EMOJI;
    public static final String RETRY = "🔄";
    public static final String REPEAT = "🔁";
    public static final String RECYCLE = "♻" + EMOJI;
    public static final String SKIP = "⏭" + EMOJI;
    public static final String FAST_FORWARD = "⏩";
    public static final String ROCKET = "🚀";
    public static final String TEST_TUBE = "🧪";

    // ----------------------------------------------------
    // PROGRESS / STATUS (emoji)
    // ----------------------------------------------------

    public static final String START = "▶" + EMOJI;
    public static final String STOP = "⏹" + EMOJI;
    public static final String PAUSE = "⏸" + EMOJI;
    public static final String RECORD = "⏺" + EMOJI;

    // ----------------------------------------------------
    // FLAGS / MARKERS (FLAG is text, the rest are emoji)
    // ----------------------------------------------------

    public static final String FLAG = "⚑";
    public static final String TRIANGULAR_FLAG = "🚩";
    public static final String LOCATION = "📍";
    public static final String PIN = "📌";
    public static final String TAG = "🏷" + EMOJI;

    // ----------------------------------------------------
    // SYSTEM / DEVOPS (emoji)
    // ----------------------------------------------------

    public static final String GEAR = "⚙" + EMOJI;
    public static final String HAMMER = "🔨";
    public static final String WRENCH = "🔧";
    public static final String TOOLBOX = "🧰";
    public static final String PACKAGE = "📦";
    public static final String LINK = "🔗";
    public static final String LOCK = "🔒";
    public static final String UNLOCK = "🔓";
    public static final String KEY = "🔑";
    public static final String SHIELD = "🛡" + EMOJI;
    public static final String USER = "👤";
    public static final String COMPUTER = "💻";

    // ----------------------------------------------------
    // FILES / STORAGE (emoji)
    // ----------------------------------------------------

    public static final String FILE = "📄";
    public static final String FOLDER = "📁";
    public static final String OPEN_FOLDER = "📂";
    public static final String CLIPBOARD = "📋";
    public static final String DATABASE = "🗄" + EMOJI;
    public static final String FLOPPY = "💾";

    // ----------------------------------------------------
    // NETWORK / CLOUD (emoji)
    // ----------------------------------------------------

    public static final String CLOUD = "☁" + EMOJI;
    public static final String NETWORK = "🌐";
    public static final String SATELLITE = "📡";
    public static final String WIFI = "📶";

    // ----------------------------------------------------
    // TIME / PERFORMANCE (emoji)
    // ----------------------------------------------------

    public static final String CLOCK = "🕒";
    public static final String STOPWATCH = "⏱" + EMOJI;
    public static final String TIMER = "⏲" + EMOJI;
    public static final String HOURGLASS = "⏳";
    public static final String CALENDAR = "📅";
    public static final String FAST = "⚡";

    // ----------------------------------------------------
    // METRICS (emoji)
    // ----------------------------------------------------

    public static final String CHART = "📊";
    public static final String CHART_UP = "📈";
    public static final String CHART_DOWN = "📉";

    // ----------------------------------------------------
    // DEBUG / DIAGNOSTICS (emoji)
    // ----------------------------------------------------

    public static final String BUG = "🐞";
    public static final String MICROSCOPE = "🔬";
    public static final String MAGNIFIER = "🔍";
    public static final String MAGNIFIER_RIGHT = "🔎";

    // ----------------------------------------------------
    // TREES / BOXES (text)
    // ----------------------------------------------------

    public static final String TREE_BRANCH = "├";
    public static final String TREE_LAST = "└";
    public static final String TREE_VERTICAL = "│";
    public static final String TREE_HORIZONTAL = "─";

    // ----------------------------------------------------
    // BLOCKS / PROGRESS BARS (text)
    // ----------------------------------------------------

    public static final String BLOCK_FULL = "█";
    public static final String BLOCK_DARK = "▓";
    public static final String BLOCK_MEDIUM = "▒";
    public static final String BLOCK_LIGHT = "░";

    // ----------------------------------------------------
    // SYMBOLS / UNITS (text)
    // ----------------------------------------------------

    public static final String ELLIPSIS = "…";
    public static final String MIDDLE_DOT = "·";
    public static final String MINUS = "−";
    public static final String MULTIPLY = "×";
    public static final String PLUS_MINUS = "±";
    public static final String APPROXIMATELY = "≈";
    public static final String NOT_EQUAL = "≠";
    public static final String LESS_OR_EQUAL = "≤";
    public static final String GREATER_OR_EQUAL = "≥";
    public static final String INFINITY = "∞";
    public static final String DELTA = "Δ";
    public static final String MICRO = "µ";
    public static final String DEGREE = "°";

    // ----------------------------------------------------
    // SPINNER (text)
    // ----------------------------------------------------

    private static final String[] SPINNER_FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};

    /**
     * Returns the number of frames of the spinner.
     *
     * @return a positive integer
     */
    public static int getSpinnerFrameCount() {
        return SPINNER_FRAMES.length;
    }

    /**
     * Returns a frame of a spinner (an animation used to show activity of unknown duration).
     *
     * @param frame the frame, any value (it wraps around the number of frames)
     * @return a non-null instance
     */
    public static String spinner(int frame) {
        return SPINNER_FRAMES[Math.floorMod(frame, SPINNER_FRAMES.length)];
    }

    /**
     * Returns a progress bar, using full blocks for the completed part and light blocks for the rest.
     *
     * @param ratio the progress, between 0 and 1 (values outside the range are clamped)
     * @param width the width of the bar, in characters
     * @return a non-null instance
     */
    public static String progress(double ratio, int width) {
        requireBounded(width, 0, Integer.MAX_VALUE);
        if (Double.isNaN(ratio)) ratio = 0;
        int completed = (int) Math.round(Math.max(0, Math.min(1, ratio)) * width);
        return BLOCK_FULL.repeat(completed) + BLOCK_LIGHT.repeat(width - completed);
    }
}
