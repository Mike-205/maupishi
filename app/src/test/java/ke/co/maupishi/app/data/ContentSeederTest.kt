package ke.co.maupishi.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.io.IOException
import ke.co.maupishi.app.content.ContentFile
import ke.co.maupishi.app.content.ContentManifest
import ke.co.maupishi.app.content.ContentSource
import ke.co.maupishi.app.content.ParseResult
import ke.co.maupishi.app.content.RecipeParser
import ke.co.maupishi.app.model.LocalizedText
import ke.co.maupishi.app.model.Region
import ke.co.maupishi.app.model.Recipe
import ke.co.maupishi.app.model.RecipeSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** An in-memory content source that tests can change between seeding runs. */
private class FakeContentSource(
    var version: Int = 1,
    var manifestError: Exception? = null,
    private val files: MutableMap<String, String> = linkedMapOf(),
) : ContentSource {

    fun put(id: String, name: String = "Name $id") {
        files["$id.json"] = recipeJson(id, name)
    }

    fun putRaw(fileName: String, text: String) {
        files[fileName] = text
    }

    fun remove(id: String) {
        files.remove("$id.json")
    }

    override suspend fun contentVersion(): Int = manifestError?.let { throw it } ?: version

    override suspend fun recipeFiles(): List<ContentFile> = files.map { (name, text) -> ContentFile(name, text) }

    override fun imageExists(name: String): Boolean = true
}

private fun recipeJson(id: String, name: String) = """
    {
      "id": "$id",
      "name": { "en": "$name" },
      "description": { "en": "Description" },
      "region": "NATIONWIDE",
      "prepMinutes": 1,
      "cookMinutes": 1,
      "servings": 1,
      "credit": "Credit",
      "ingredients": [ { "name": { "en": "Item" }, "quantity": 1, "unit": "PIECE" } ],
      "steps": [ { "text": { "en": "Step" } } ]
    }
""".trimIndent()

@RunWith(RobolectricTestRunner::class)
class ContentSeederTest {

    private lateinit var db: MaupishiDatabase
    private val source = FakeContentSource()
    private lateinit var seeder: ContentSeeder

    @Before
    fun openDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MaupishiDatabase::class.java).build()
        seeder = ContentSeeder(db, source)
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    private suspend fun storedNames(): Map<String, String> =
        db.recipeDao().observeAll().first().associate { it.recipe.id to it.recipe.nameEn }

    private suspend fun storedVersion(): String? = db.contentMetaDao().get("contentVersion")

    private fun userRecipe(id: String) = Recipe(
        id = id,
        source = RecipeSource.USER,
        verified = false,
        name = LocalizedText("User $id"),
        description = LocalizedText("Description"),
        region = Region.NATIONWIDE,
        community = null,
        categories = emptySet(),
        occasions = emptySet(),
        allergens = emptySet(),
        prepMinutes = 1,
        cookMinutes = 1,
        servings = 1,
        cost = null,
        safetyNote = null,
        culturalNote = null,
        credit = "Me",
        image = null,
        ingredients = emptyList(),
        steps = emptyList(),
    )

    @Test
    fun firstLaunch_loadsEveryBundledRecipe() = runTest {
        source.put("a")
        source.put("b")

        assertEquals(SeedResult.Applied(recipeCount = 2, removedCount = 0), seeder.seed())

        assertEquals(mapOf("a" to "Name a", "b" to "Name b"), storedNames())
        assertEquals("1", storedVersion())
    }

    @Test
    fun secondLaunch_withTheSameVersion_doesNothing() = runTest {
        source.put("a")
        seeder.seed()

        assertEquals(SeedResult.UpToDate, seeder.seed())
        assertEquals(mapOf("a" to "Name a"), storedNames())
    }

    @Test
    fun changedContentWithTheSameVersion_isNotApplied() = runTest {
        source.put("a", name = "Old")
        seeder.seed()

        source.put("a", name = "New")

        assertEquals(SeedResult.UpToDate, seeder.seed())
        assertEquals(mapOf("a" to "Old"), storedNames())
    }

    @Test
    fun bumpedVersion_updatesAddsAndRemovesBundledRecipes() = runTest {
        source.put("keep", name = "Old")
        source.put("gone")
        seeder.seed()

        source.version = 2
        source.put("keep", name = "New")
        source.put("added")
        source.remove("gone")

        assertEquals(SeedResult.Applied(recipeCount = 2, removedCount = 1), seeder.seed())
        assertEquals(mapOf("keep" to "New", "added" to "Name added"), storedNames())
        assertEquals("2", storedVersion())
    }

    @Test
    fun forceSync_appliesChangesWithoutAVersionBump() = runTest {
        source.put("a", name = "Old")
        seeder.seed()

        source.put("a", name = "New")

        assertEquals(SeedResult.Applied(recipeCount = 1, removedCount = 0), seeder.seed(forceSync = true))
        assertEquals(mapOf("a" to "New"), storedNames())
    }

    @Test
    fun userRecipes_areNeverChangedOrRemoved() = runTest {
        db.recipeDao().replace(userRecipe("mine").toStored())
        source.put("a")

        seeder.seed()
        source.version = 2
        source.remove("a")
        seeder.seed()

        assertEquals(mapOf("mine" to "User mine"), storedNames())
    }

    @Test
    fun favorites_surviveUpdatesAndRemovals() = runTest {
        source.put("a")
        seeder.seed()
        db.favoriteDao().add(FavoriteEntity(recipeId = "a", addedAt = 1L))

        source.version = 2
        source.put("a", name = "Changed")
        seeder.seed()
        source.version = 3
        source.remove("a")
        seeder.seed()

        assertTrue(storedNames().isEmpty())
        assertEquals(listOf("a"), db.favoriteDao().observeAll().first().map { it.recipeId })
    }

    @Test
    fun oneBadFile_appliesNothingAndKeepsTheVersion() = runTest {
        source.put("a", name = "Old")
        seeder.seed()

        source.version = 2
        source.put("a", name = "New")
        source.put("b")
        source.putRaw("bad.json", """{ "id": "bad" }""")

        val result = seeder.seed()

        assertTrue("Expected Failed but was $result", result is SeedResult.Failed)
        assertTrue((result as SeedResult.Failed).errors.all { it.startsWith("bad.json") })
        assertEquals(mapOf("a" to "Old"), storedNames())
        assertEquals("1", storedVersion())
    }

    @Test
    fun aFileWithTheIdOfAUserRecipe_failsWithoutOverwriting() = runTest {
        db.recipeDao().replace(userRecipe("clash").toStored())
        source.put("clash")
        source.put("other")

        val result = seeder.seed()

        assertTrue("Expected Failed but was $result", result is SeedResult.Failed)
        assertEquals(mapOf("clash" to "User clash"), storedNames())
        assertNull(storedVersion())
    }

    @Test
    fun anUnreadableManifest_failsWithoutChangingAnything() = runTest {
        source.put("a")
        seeder.seed()

        source.manifestError = IOException("no manifest")
        source.version = 2
        source.put("b")

        val result = seeder.seed()

        assertEquals(SeedResult.Failed(listOf("manifest: no manifest")), result)
        assertEquals(mapOf("a" to "Name a"), storedNames())
    }

    // Seeds the real files in the source tree, as the app will on first launch.
    @Test
    fun theRealBundledFiles_seedAndReadBack() = runTest {
        val moduleRoot = listOf(File("."), File("app")).first { File(it, "src/main").isDirectory }
        val manifestText = File(moduleRoot, "src/main/assets/content-manifest.json").readText()
        val manifest = Json.decodeFromString<ContentManifest>(manifestText)
        assertTrue("contentVersion must be positive", manifest.contentVersion > 0)

        val files = listOf("main", "debug").flatMap { sourceSet ->
            File(moduleRoot, "src/$sourceSet/assets/recipes")
                .listFiles { file -> file.extension == "json" }
                .orEmpty()
                .toList()
        }
        val realSource = object : ContentSource {
            override suspend fun contentVersion() = manifest.contentVersion
            override suspend fun recipeFiles() = files.map { ContentFile(it.name, it.readText()) }
            override fun imageExists(name: String) = true
        }

        val result = ContentSeeder(db, realSource).seed()

        assertEquals(SeedResult.Applied(recipeCount = files.size, removedCount = 0), result)
        val expected = files.associate { file ->
            val parsed = RecipeParser.parse(file.readText(), file.name) as ParseResult.Success
            parsed.recipe.id to parsed.recipe
        }
        val stored = db.recipeDao().observeAll().first().associate { it.recipe.id to it.toModel() }
        assertEquals(expected, stored)
    }
}
