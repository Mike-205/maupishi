package ke.co.maupishi.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ke.co.maupishi.app.model.Allergen
import ke.co.maupishi.app.model.ApproxMetric
import ke.co.maupishi.app.model.Category
import ke.co.maupishi.app.model.CostEstimate
import ke.co.maupishi.app.model.Ingredient
import ke.co.maupishi.app.model.LocalizedText
import ke.co.maupishi.app.model.MeasureUnit
import ke.co.maupishi.app.model.Occasion
import ke.co.maupishi.app.model.Recipe
import ke.co.maupishi.app.model.RecipeImage
import ke.co.maupishi.app.model.RecipeSource
import ke.co.maupishi.app.model.Region
import ke.co.maupishi.app.model.Step
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Runs the real Room code against an in-memory SQLite database. */
@RunWith(RobolectricTestRunner::class)
class RecipeStorageTest {

    private lateinit var db: MaupishiDatabase
    private val recipes get() = db.recipeDao()

    @Before
    fun openDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MaupishiDatabase::class.java).build()
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    private fun text(en: String) = LocalizedText(en, "$en (sw)")

    private fun fullRecipe(id: String = "full") = Recipe(
        id = id,
        source = RecipeSource.BUNDLED,
        verified = true,
        name = text("Name $id"),
        description = text("Description"),
        region = Region.COAST,
        community = "Community",
        categories = setOf(Category.MAIN, Category.SOUP_STEW),
        occasions = setOf(Occasion.EVERYDAY, Occasion.EID),
        allergens = setOf(Allergen.DAIRY, Allergen.GLUTEN),
        prepMinutes = 10,
        cookMinutes = 20,
        servings = 4,
        cost = CostEstimate(minKes = 100, maxKes = 200, basis = text("Cost basis")),
        safetyNote = text("Safety"),
        culturalNote = text("Culture"),
        credit = "Credit",
        image = RecipeImage(file = "full.webp", alt = text("Alt")),
        ingredients = listOf(
            Ingredient(
                name = text("Metric"),
                quantity = 250.0,
                unit = MeasureUnit.G,
                group = text("Group"),
                prepNote = text("Prep"),
                substitution = text("Swap"),
            ),
            Ingredient(
                name = text("Local"),
                quantity = 2.0,
                unit = MeasureUnit.KIKOMBE,
                approxMetric = ApproxMetric(300.0, MeasureUnit.ML),
            ),
            Ingredient(name = text("To taste"), quantity = null, unit = null),
        ),
        steps = listOf(
            Step(text("First"), timerSeconds = 600),
            Step(text("Second")),
            Step(text("Third")),
        ),
    )

    private fun minimalRecipe(id: String = "minimal") = Recipe(
        id = id,
        source = RecipeSource.BUNDLED,
        verified = false,
        name = LocalizedText("Name $id"),
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
        credit = "Credit",
        image = null,
        ingredients = listOf(Ingredient(name = LocalizedText("Item"), quantity = 1.0, unit = MeasureUnit.PIECE)),
        steps = listOf(Step(LocalizedText("Do it"))),
    )

    private suspend fun save(recipe: Recipe) = recipes.replace(recipe.toStored())

    private suspend fun load(id: String): Recipe? = recipes.getById(id)?.toModel()

    private fun count(table: String): Int =
        db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table").use {
            it.moveToFirst()
            it.getInt(0)
        }

    @Test
    fun recipeWithEveryField_readsBackUnchanged() = runTest {
        val recipe = fullRecipe()
        save(recipe)
        assertEquals(recipe, load("full"))
    }

    @Test
    fun recipeWithOnlyRequiredFields_readsBackUnchanged() = runTest {
        val recipe = minimalRecipe()
        save(recipe)
        assertEquals(recipe, load("minimal"))
    }

    @Test
    fun unknownId_readsAsNull() = runTest {
        assertNull(recipes.getById("missing"))
    }

    @Test
    fun ingredientsAndSteps_keepTheirOrder() = runTest {
        save(fullRecipe())
        val loaded = load("full")!!
        assertEquals(listOf("Metric", "Local", "To taste"), loaded.ingredients.map { it.name.en })
        assertEquals(listOf("First", "Second", "Third"), loaded.steps.map { it.text.en })
    }

    @Test
    fun enumValues_areStoredAsTextCodes() = runTest {
        save(fullRecipe())
        val region = db.openHelper.readableDatabase.query("SELECT region FROM recipes").use {
            it.moveToFirst()
            it.getString(0)
        }
        assertEquals("COAST", region)
    }

    @Test
    fun replacingARecipe_rewritesItsChildRowsWithoutDuplicates() = runTest {
        save(fullRecipe())
        val updated = fullRecipe().copy(
            categories = setOf(Category.SIDE),
            ingredients = listOf(Ingredient(name = text("Only"), quantity = 1.0, unit = MeasureUnit.CUP)),
            steps = listOf(Step(text("Only step"))),
        )
        save(updated)

        assertEquals(updated, load("full"))
        assertEquals(1, count("recipes"))
        assertEquals(1, count("recipe_categories"))
        assertEquals(1, count("ingredients"))
        assertEquals(1, count("steps"))
    }

    @Test
    fun deletingARecipe_removesItsChildRows() = runTest {
        save(fullRecipe())
        recipes.deleteById("full")

        assertNull(load("full"))
        for (table in listOf("recipe_categories", "recipe_occasions", "recipe_allergens", "ingredients", "steps")) {
            assertEquals("$table should be empty", 0, count(table))
        }
    }

    @Test
    fun observeAll_listsRecipesByNameIgnoringCase() = runTest {
        save(minimalRecipe("b").copy(name = LocalizedText("banana")))
        save(minimalRecipe("a").copy(name = LocalizedText("Apple")))
        save(minimalRecipe("c").copy(name = LocalizedText("Cherry")))

        val names = recipes.observeAll().first().map { it.toModel().name.en }
        assertEquals(listOf("Apple", "banana", "Cherry"), names)
    }

    @Test
    fun observeById_emitsTheStoredRecipe() = runTest {
        save(fullRecipe())
        assertEquals(fullRecipe(), recipes.observeById("full").first()?.toModel())
    }

    @Test
    fun favorite_canPointAtARecipeThatDoesNotExist() = runTest {
        db.favoriteDao().add(FavoriteEntity(recipeId = "no-such-recipe", addedAt = 1L))
        assertEquals(listOf("no-such-recipe"), db.favoriteDao().observeAll().first().map { it.recipeId })
    }

    @Test
    fun favorite_survivesDeletingItsRecipe() = runTest {
        save(fullRecipe())
        db.favoriteDao().add(FavoriteEntity(recipeId = "full", addedAt = 1L))

        recipes.deleteById("full")

        assertEquals(listOf("full"), db.favoriteDao().observeAll().first().map { it.recipeId })
    }

    @Test
    fun favorites_areListedNewestFirstAndCanBeRemoved() = runTest {
        val dao = db.favoriteDao()
        dao.add(FavoriteEntity("old", addedAt = 1L))
        dao.add(FavoriteEntity("new", addedAt = 2L))
        assertEquals(listOf("new", "old"), dao.observeAll().first().map { it.recipeId })

        dao.remove("new")
        assertEquals(listOf("old"), dao.observeAll().first().map { it.recipeId })
    }

    @Test
    fun contentMeta_isStoredAndOverwritten() = runTest {
        val dao = db.contentMetaDao()
        assertNull(dao.get("contentVersion"))

        dao.put(ContentMetaEntity("contentVersion", "1"))
        dao.put(ContentMetaEntity("contentVersion", "2"))

        assertEquals("2", dao.get("contentVersion"))
    }
}
