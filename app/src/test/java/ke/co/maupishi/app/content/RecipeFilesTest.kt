package ke.co.maupishi.app.content

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parses every bundled recipe file, so a broken file fails the build before it
 * can reach a phone. Verified recipes live in `src/main/assets/recipes` and
 * drafts and placeholders in `src/debug/assets/recipes`.
 */
class RecipeFilesTest {

    private val moduleRoot: File = listOf(File("."), File("app"))
        .first { File(it, "src/main").isDirectory }

    private fun recipeFiles(sourceSet: String): List<File> =
        File(moduleRoot, "src/$sourceSet/assets/recipes")
            .listFiles { file -> file.extension == "json" }
            .orEmpty()
            .sortedBy { it.name }

    private val allFiles: List<File> get() = recipeFiles("main") + recipeFiles("debug")

    // Where images live is decided with the first real photo (M2).
    private fun imageExists(name: String): Boolean = listOf("main", "debug").any { sourceSet ->
        File(moduleRoot, "src/$sourceSet/assets/images/$name").isFile
    }

    @Test
    fun everyRecipeFile_parsesAndIsValid() {
        assertTrue("Expected to find recipe files to check", allFiles.isNotEmpty())

        val problems = allFiles.flatMap { file ->
            when (val result = RecipeParser.parse(file.readText(), file.name, ::imageExists)) {
                is ParseResult.Success -> emptyList()
                is ParseResult.Failure -> result.errors.map { "${file.name} $it" }
            }
        }

        assertTrue("Recipe files with problems:\n" + problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun recipeIds_areUniqueAcrossAllFiles() {
        val ids = allFiles.map { it.name.removeSuffix(".json") }
        val duplicates = ids.groupBy { it }.filterValues { it.size > 1 }.keys
        assertEquals("Duplicate recipe ids: $duplicates", emptySet<String>(), duplicates)
    }

    @Test
    fun recipesInMainAssets_areAllVerified() {
        val unverified = recipeFiles("main").filter { file ->
            val result = RecipeParser.parse(file.readText(), file.name, ::imageExists)
            result is ParseResult.Success && !result.recipe.verified
        }
        assertTrue(
            "Release builds may only ship Verified recipes: ${unverified.map { it.name }}",
            unverified.isEmpty(),
        )
    }

    @Test
    fun placeholderRecipes_areOnlyInDebugAssets() {
        val misplaced = recipeFiles("main").filter { it.name.startsWith("placeholder-") }
        assertTrue("PLACEHOLDER recipes must not be in main assets: ${misplaced.map { it.name }}", misplaced.isEmpty())
    }
}
