package ke.co.maupishi.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Keeps the look replaceable: only `ui/designsystem/` may import Material or
 * write raw color and size values. Every other source file must go through the
 * design tokens and our own components.
 *
 * The scan is text-based, so it can flag a harmless line. If that happens,
 * rewrite the line to use a token rather than loosening a rule.
 */
class DesignBoundaryTest {

    private data class Rule(val name: String, val pattern: Regex)

    private val rules = listOf(
        Rule("imports Material", Regex("""^\s*import\s+androidx\.compose\.material3?\.""")),
        Rule("raw color value", Regex("""Color\(\s*0[xX]""")),
        Rule(
            "named color constant",
            Regex("""\bColor\.(Black|White|Red|Green|Blue|Yellow|Cyan|Magenta|Gray|DarkGray|LightGray)\b"""),
        ),
        Rule("literal dp or sp value", Regex("""\b\d+(\.\d+)?\.(dp|sp)\b""")),
    )

    private val sourceRoot: File = listOf("src/main/java", "app/src/main/java")
        .map(::File)
        .first { it.isDirectory }

    private val designSystemDir = File(sourceRoot, "ke/co/maupishi/app/ui/designsystem")

    private fun violationsIn(lines: List<String>): List<String> =
        lines.mapIndexedNotNull { index, line ->
            val code = line.trimStart()
            val isComment = code.startsWith("//") || code.startsWith("*") || code.startsWith("/*")
            val broken = if (isComment) null else rules.firstOrNull { it.pattern.containsMatchIn(line) }
            broken?.let { "line ${index + 1}: ${it.name}: ${line.trim()}" }
        }

    @Test
    fun sourcesOutsideDesignSystem_useOnlyTokensAndOurComponents() {
        val files = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" && !it.startsWith(designSystemDir) }
            .toList()

        assertTrue("Expected to find source files to check", files.isNotEmpty())
        assertTrue("Design system folder not found: $designSystemDir", designSystemDir.isDirectory)

        val problems = files.flatMap { file ->
            violationsIn(file.readLines()).map { "${file.path} $it" }
        }
        assertEquals(
            "Use design tokens and our own components instead:\n" + problems.joinToString("\n"),
            emptyList<String>(),
            problems,
        )
    }

    @Test
    fun rules_flagEachKindOfViolation() {
        val bad = listOf(
            "import androidx.compose.material3.Text",
            "import androidx.compose.material.Icon",
            "val c = Color(0xFF112233)",
            "val c = Color.Red",
            "Modifier.padding(16.dp)",
            "fontSize = 14.sp",
            "val x = 0.5.sp",
        )
        bad.forEach { line ->
            assertEquals("Should be flagged: $line", 1, violationsIn(listOf(line)).size)
        }
    }

    @Test
    fun rules_allowTokensComments_andNonColorNames() {
        val fine = listOf(
            "import androidx.compose.ui.graphics.Color",
            "val c = Color.Unspecified",
            "val c = Color.Transparent",
            "Modifier.padding(MaupishiTheme.spacing.md)",
            "// Color(0xFF112233) and 16.dp are explained here",
            " * uses 16.dp in the example",
        )
        fine.forEach { line ->
            assertEquals("Should not be flagged: $line", 0, violationsIn(listOf(line)).size)
        }
    }
}
