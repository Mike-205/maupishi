package ke.co.maupishi.app

import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Starts the real application class: the container, the asset reading, the
 * seeder and the repository all have to work together for the bundled recipes
 * to appear. The class is set here with `@Config`; [ApplicationManifestTest]
 * checks that the manifest names it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = MaupishiApplication::class)
class MaupishiApplicationTest {

    @Test
    fun startingTheApp_loadsTheBundledRecipesIntoTheRepository() {
        val app = ApplicationProvider.getApplicationContext<MaupishiApplication>()
        val moduleRoot = listOf(File("."), File("app")).first { File(it, "src/main").isDirectory }
        val expectedIds = listOf("main", "debug").flatMap { sourceSet ->
            File(moduleRoot, "src/$sourceSet/assets/recipes")
                .listFiles { file -> file.extension == "json" }
                .orEmpty()
                .map { it.name.removeSuffix(".json") }
        }.sorted()

        try {
            // Seeding runs in the background, so wait (in real time) for the recipes to arrive.
            val recipes = runBlocking {
                withTimeout(30_000) {
                    app.container.recipeRepository.observeRecipes().first { it.isNotEmpty() }
                }
            }
            assertEquals(expectedIds, recipes.map { it.id }.sorted())
        } finally {
            app.container.database.close()
        }
    }
}
