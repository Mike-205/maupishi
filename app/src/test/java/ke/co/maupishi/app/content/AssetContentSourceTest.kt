package ke.co.maupishi.app.content

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Runs [AssetContentSource] against the real merged assets of the debug build,
 * and checks it finds exactly the files that are in the source tree.
 */
@RunWith(RobolectricTestRunner::class)
class AssetContentSourceTest {

    private val source = AssetContentSource(ApplicationProvider.getApplicationContext<Context>().assets)

    private val moduleRoot: File = listOf(File("."), File("app")).first { File(it, "src/main").isDirectory }

    private fun sourceTreeRecipeFiles(): List<File> = listOf("main", "debug").flatMap { sourceSet ->
        File(moduleRoot, "src/$sourceSet/assets/recipes")
            .listFiles { file -> file.extension == "json" }
            .orEmpty()
            .toList()
    }.sortedBy { it.name }

    @Test
    fun contentVersion_matchesTheManifestFile() = runTest {
        val manifest = Json.decodeFromString<ContentManifest>(
            File(moduleRoot, "src/main/assets/content-manifest.json").readText(),
        )
        assertEquals(manifest.contentVersion, source.contentVersion())
    }

    @Test
    fun recipeFiles_areExactlyTheFilesInTheSourceTree() = runTest {
        val expected = sourceTreeRecipeFiles()
        assertTrue("Expected the debug placeholders to be present", expected.isNotEmpty())

        val found = source.recipeFiles()

        assertEquals(expected.map { it.name }, found.map { it.name })
        assertEquals(expected.map { it.readText() }, found.map { it.text })
    }

    @Test
    fun manifest_isNotMistakenForARecipeFile() = runTest {
        assertFalse(source.recipeFiles().any { it.name.contains("manifest") })
    }

    @Test
    fun imageExists_isFalseForAMissingImage() {
        assertFalse(source.imageExists("no-such-image.webp"))
    }
}
